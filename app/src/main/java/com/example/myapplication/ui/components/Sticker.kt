package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun Sticker(
    icon: ImageVector,
    habitColor: Color,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    size: Dp = 56.dp, // Default size
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "StickerScale"
    )
    val haptic = LocalHapticFeedback.current

    val shape = RoundedCornerShape(14.dp)
    
    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .shadow(
                elevation = if (isSelected) 8.dp else 4.dp,
                shape = shape,
                ambientColor = if (isSelected) habitColor.copy(alpha = 0.5f) else Color.Black,
                spotColor = if (isSelected) habitColor.copy(alpha = 0.5f) else Color.Black
            )
            .clip(shape)
            .background(Color(0xFF1A1A1A))
            .let {
                if (isSelected) {
                    it.border(2.dp, habitColor, shape)
                } else {
                    it
                }
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size / 2) // Icon is half the sticker size approx
        )
    }
}
