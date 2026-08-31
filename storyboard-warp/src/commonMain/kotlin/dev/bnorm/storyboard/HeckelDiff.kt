package dev.bnorm.storyboard

import androidx.collection.ScatterMap
import androidx.collection.mutableScatterMapOf


// This is an adaptation of the paper:
// P. Heckel, A technique for isolating differences between files
//Comm. ACM, 21, (4), 264–268 (1978).

// This can help compute structural similarities between 2 text sequences.
// Once we know this, we should be able to animate cleanly between these 2 sequences.

fun diff(
    previous: List<Token>,
    current: List<Token>,
    keys: Keys = Keys()
): List<State> {
    // Symbol tables
    val statesP: Array<State> = Array(previous.size) { State.Empty }
    val statesC: Array<State> = Array(current.size) { State.Empty }
    // Frequency + Context
    val (freqP, contextP) = frequencyAndContext(tokens = previous)
    val (freqC, _) = frequencyAndContext(tokens = current)
    // Phase 1: Find unique anchors such that frequency = 1 in both lists.
    // That is a match.
    current.forEachIndexed { index, token ->
        val c = freqC[token]
        val p = freqP[token]
        val context = contextP[token]
        if (c == 1 && p == 1 && context != null) {
            val indexP = context.index
            // Here we are also keeping track of the previous token.
            // This is because we exclude the actual start and end offsets from the equals()
            // to find high-quality anchor points.
            val tokenP = context.token
            val match = State.Match(
                previous = tokenP,
                previousIdx = indexP,
                current = token,
                currentIdx = index
            )
            statesP[match.previousIdx] = match
            statesC[match.currentIdx] = match
        }
    }
    // Phase 2: We found unique anchors.
    // Move forward and find the ones that are matching adjacent to the ones that already matched.
    for (index in 0 until statesC.lastIndex) {
        val state = statesC[index]
        val nextState = statesC[index + 1]
        if (state is State.Match && nextState == State.Empty) {
            val nextIndex = index + 1
            val nextPreviousIndex = state.previousIdx + 1
            val nextToken = current.getOrNull(nextIndex)
            val nextPreviousToken = previous.getOrNull(nextPreviousIndex)
            if (nextPreviousToken != null &&
                nextToken == nextPreviousToken &&
                statesC[nextIndex] == State.Empty &&
                statesP[nextPreviousIndex] == State.Empty
            ) {
                val match = State.Match(
                    previous = nextPreviousToken,
                    previousIdx = state.previousIdx + 1,
                    current = nextToken,
                    currentIdx = index + 1,
                )
                statesP[match.previousIdx] = match
                statesC[match.currentIdx] = match
            }
        }
    }
    // Phase 3: Same as Phase 2 but backwards.
    for (index in statesC.lastIndex downTo 1) {
        val state = statesC[index]
        val priorState = statesC[index - 1]
        if (state is State.Match && priorState == State.Empty) {
            val priorIndex = index - 1
            val nextPriorIndex = state.previousIdx - 1
            val priorToken = current.getOrNull(priorIndex)
            val priorPreviousToken = previous.getOrNull(nextPriorIndex)
            if (priorToken != null &&
                priorToken == priorPreviousToken &&
                statesC[priorIndex] == State.Empty &&
                statesP[nextPriorIndex] == State.Empty
            ) {
                val match = State.Match(
                    previous = priorPreviousToken,
                    previousIdx = state.previousIdx - 1,
                    current = priorToken,
                    currentIdx = index - 1
                )
                statesP[match.previousIdx] = match
                statesC[match.currentIdx] = match
            }
        }
    }
    // Phase 4: Final pass.
    // Anything that did not match in:
    // - statesP = Delete
    // - statesC = Insert
    for (i in statesP.indices) {
        if (statesP[i] == State.Empty) {
            statesP[i] = State.Delete(token = previous[i], index = i)
        }
    }
    val size = maxOf(statesP.size, statesC.size)
    // Keeps track of the last known deleted index in statesP
    var deleteIdx = 0
    // This is the final edit script.
    val edits = ArrayList<State>(size)
    for (i in 0 until size) {
        if (i < statesP.size && deleteIdx <= i && statesP[i] is State.Delete) {
            // We want to cluster all the deletes that occur next to each other.
            var j = i
            while (j < statesP.size && statesP[j] is State.Delete) {
                // Add deletions to the list of edits.
                edits += statesP[j]
                j += 1
            }
            deleteIdx = j
        }
        if (i < statesC.size) {
            val state = statesC[i]
            if (state is State.Match) {
                if (state.previous.hasKey()) {
                    // Propagate keys for matched tokens
                    state.current.assignKey(newKey = state.previous.key())
                }
            }
            if (statesC[i] == State.Empty) {
                // Assign a new key for newly inserted tokens
                keys.assignKey(current[i])
                statesC[i] = State.Insert(token = current[i], index = i)
            }
            edits += statesC[i]
        }
    }
    return edits
}

internal data class TokenContext(val token: Token, val index: Int)

internal fun frequencyAndContext(
    tokens: List<Token>
): Pair<ScatterMap<Token, Int>, ScatterMap<Token, TokenContext?>> {
    val freq = mutableScatterMapOf<Token, Int>()
    // Context for tokens whose count == 1
    val indexes = mutableScatterMapOf<Token, TokenContext?>()
    tokens.forEachIndexed { index, token ->
        val count = freq.getOrDefault(token, 0)
        if (count == 0) {
            indexes[token] = TokenContext(token = token, index = index)
        } else {
            // If this is not the first time we are seeing this,
            // We no longer care about tracking its index.
            indexes[token] = null
        }
        freq[token] = count + 1
    }
    return freq to indexes
}
