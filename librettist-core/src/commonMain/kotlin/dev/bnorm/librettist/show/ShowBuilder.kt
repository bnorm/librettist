package dev.bnorm.librettist.show

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable

@DslMarker
annotation class ShowBuilderDsl

typealias SlideContent<T> = @Composable SlideScope<T>.() -> Unit

@ShowBuilderDsl
interface ShowBuilder {
    @ShowBuilderDsl
    fun slide(
        states: Int = 1,
        enterTransition: EnterTransition = EnterTransition.None,
        exitTransition: ExitTransition = ExitTransition.None,
        content: SlideContent<SlideState<Int>>,
    )
}

@ShowBuilderDsl
fun <T> ShowBuilder.slideForList(
    values: List<T>,
    enterTransition: EnterTransition = EnterTransition.None,
    exitTransition: ExitTransition = ExitTransition.None,
    content: SlideContent<SlideState<T>>,
) {
    slide(states = values.size, enterTransition, exitTransition) {
        createChildScope { state -> state.map { values[it] } }.content()
    }
}

@ShowBuilderDsl
inline fun <reified E : Enum<E>> ShowBuilder.slideForEnum(
    enterTransition: EnterTransition = EnterTransition.None,
    exitTransition: ExitTransition = ExitTransition.None,
    crossinline content: SlideContent<SlideState<E>>,
) {
    val values = enumValues<E>()
    slide(states = values.size, enterTransition, exitTransition) {
        createChildScope { state -> state.map { values[it] } }.content()
    }
}

@ShowBuilderDsl
fun ShowBuilder.slideForBoolean(
    enterTransition: EnterTransition = EnterTransition.None,
    exitTransition: ExitTransition = ExitTransition.None,
    content: SlideContent<SlideState<Boolean>>,
) {
    slide(states = 2, enterTransition, exitTransition) {
        createChildScope { state -> state.map { it > 0 } }.content()
    }
}
