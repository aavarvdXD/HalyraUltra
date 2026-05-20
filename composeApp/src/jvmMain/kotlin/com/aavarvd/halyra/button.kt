package com.aavarvd.halyra

import androidx.compose.runtime.Composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.material.*

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        elevation = ButtonDefaults.elevation(0.dp),
        border = BorderStroke(1.dp, Color.White),
        shape = RoundedCornerShape(4.dp),

        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF3C3F41),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontFamily = AppFonts.Inter
        )
    }
}