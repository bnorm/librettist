package dev.bnorm.librettist.show

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

@Immutable
class SlideSection(
    val title: @Composable () -> Unit,
) {
    companion object {
        val Empty = SlideSection {}

        val header: @Composable () -> Unit
            @Composable
            get() = LocalSlideSection.current.title
    }
}

private val LocalSlideSection = compositionLocalOf { SlideSection.Empty }

@ShowBuilderDsl
fun ShowBuilder.section(
    title: String,
    block: ShowBuilder.() -> Unit,
) {
    section(
        title = { Text(title) },
        block = block,
    )
}

@ShowBuilderDsl
fun ShowBuilder.section(
    title: @Composable () -> Unit,
    block: ShowBuilder.() -> Unit,
) {
    val upstream = this
    val section = SlideSection(title)

    object : ShowBuilder {
        override fun slide(
            states: Int,
            enterTransition: (AdvanceDirection) -> EnterTransition,
            exitTransition: (AdvanceDirection) -> ExitTransition,
            content: SlideContent<SlideState<Int>>,
        ) {
            upstream.slide(states, enterTransition, exitTransition) {
                CompositionLocalProvider(LocalSlideSection provides section) {
                    content()
                }
            }
        }
    }.block()
}
