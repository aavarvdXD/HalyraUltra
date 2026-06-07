package com.aavarvd.halyra

import androidx.compose.runtime.Composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.material.*

import androidx.compose.runtime.*

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Button(
        onClick = onClick,
        elevation = ButtonDefaults.elevation(0.dp),
        shape = RoundedCornerShape(4.dp),

        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (hovered) Color(0xFF515151) else Color(0xFF2B2B2B),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontFamily = AppFonts.Inter
        )
    }
}