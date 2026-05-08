package com.aavarvd.halyra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image

import androidx.compose.material.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2B2B2B)),
        contentAlignment = Alignment.Center
    ) {
        val splashBitmap = useResource("splash.png") { input ->
            loadImageBitmap(input)
        }

        Image(
            bitmap = splashBitmap,
            contentDescription = null,
        )
    }
}