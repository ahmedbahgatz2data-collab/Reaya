package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BentoPrimary

@Composable
fun Modifier.pulseEffect(
    enabled: Boolean = true,
    minScale: Float = 1.0f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000
): Modifier {
    if (!enabled) return this

    val transition = rememberInfiniteTransition(label = "pulse_transition")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun PulsingCardWrapper(
    modifier: Modifier = Modifier,
    glowColor: Color = BentoPrimary,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "card_glow")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val scale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .border(
                width = 2.dp,
                color = glowColor.copy(alpha = alpha),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}
