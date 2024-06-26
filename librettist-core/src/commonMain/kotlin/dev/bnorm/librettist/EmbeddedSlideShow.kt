package dev.bnorm.librettist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.bnorm.librettist.show.AdvanceDirection
import dev.bnorm.librettist.show.ShowBuilder
import dev.bnorm.librettist.show.ShowState
import dev.bnorm.librettist.show.indices
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@Composable
fun EmbeddedSlideShow(
    theme: ShowTheme,
    slideSize: DpSize = DEFAULT_SLIDE_SIZE,
    showIndicators: Boolean = true,
    startSlide: Int = 0,
    builder: ShowBuilder.() -> Unit,
) {
    val showState = remember(builder, startSlide) {
        ShowState(builder).also { state ->
            val indices = state.slides.indices
            state.jumpToSlide(indices[startSlide.coerceIn(0, indices.size - 1)])
        }
    }
    var visibleIndicators by remember(showIndicators) { mutableStateOf(showIndicators) }
    var lastAdvancement by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    if (showIndicators && !visibleIndicators) {
        LaunchedEffect(lastAdvancement) {
            delay(10.seconds)
            visibleIndicators = true
        }
    }

    fun advance(direction: AdvanceDirection, jump: Boolean): Boolean {
        if (showState.advance(direction, jump)) {
            visibleIndicators = false
            lastAdvancement = TimeSource.Monotonic.markNow()
            return true
        } else {
            return false
        }
    }

    var keyHeld = false
    fun handleKeyEvent(event: KeyEvent): Boolean {
        // TODO rate-limit holding down the key?
        if (event.type == KeyEventType.KeyDown) {
            val wasHeld = keyHeld
            keyHeld = true

            when (event.key) {
                Key.DirectionRight,
                Key.Enter,
                Key.Spacebar,
                -> return advance(AdvanceDirection.Forward, jump = wasHeld)

                Key.DirectionLeft,
                Key.Backspace,
                -> return advance(AdvanceDirection.Backward, jump = wasHeld)
            }
        }

        if (event.type == KeyEventType.KeyUp) {
            keyHeld = false
        }

        return false
    }

    val focusRequester = remember { FocusRequester() }
    ShowTheme(theme) {
        Box(modifier = Modifier.focusRequester(focusRequester).focusTarget().onKeyEvent(::handleKeyEvent)) {
            SlideShowDisplay(
                showState = showState,
                slideSize = slideSize,
                modifier = Modifier.fillMaxSize()
            )

            MouseNavigationIndicators(onAdvancement = { advance(it, jump = false) }, visibleIndicators)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun MouseNavigationIndicators(onAdvancement: (AdvanceDirection) -> Unit, visibleIndicators: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxHeight().width(200.dp).align(Alignment.CenterStart)
                .clickable(interactionSource, indication = null) {
                    onAdvancement(AdvanceDirection.Backward)
                }
        ) {
            AnimatedVisibility(
                visible = visibleIndicators,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it },
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray.copy(alpha = 0.25f))) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(125.dp).align(Alignment.Center),
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxHeight().width(200.dp).align(Alignment.CenterEnd)
                .clickable(interactionSource, indication = null) {
                    onAdvancement(AdvanceDirection.Forward)
                }
        ) {
            AnimatedVisibility(
                visible = visibleIndicators,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray.copy(alpha = 0.25f))) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(125.dp).align(Alignment.Center),
                    )
                }
            }
        }
    }
}
