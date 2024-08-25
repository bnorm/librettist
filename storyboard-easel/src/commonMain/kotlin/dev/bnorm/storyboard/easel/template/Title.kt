package dev.bnorm.storyboard.easel.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bnorm.storyboard.core.SlideScope

@Composable
fun Title(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxWidth()) {
        ProvideTextStyle(MaterialTheme.typography.h1) {
            content()
        }
    }
}

private object SharedTitleKey

@Composable
fun SlideScope<*>.SharedTitle(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Title(
        modifier = modifier.sharedElement(
            state = rememberSharedContentState(SharedTitleKey),
            animatedVisibilityScope = this
        ),
        content = content
    )
}