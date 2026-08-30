package com.aavarvd.halyra.editor.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.material.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue

import com.aavarvd.halyra.ui.AppFonts

@Composable
fun FindBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF3C3F41))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Find:",
            color = Color(0xFFBBBBBB),
            fontFamily = AppFonts.Inter
        )

        Spacer(Modifier.width(8.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = AppFonts.JBMono
            ),
            modifier = Modifier
                .width(300.dp)
                .background(Color(0xFF2B2B2B))
                .focusRequester(focusRequester)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(Modifier.width(8.dp))

        TextButton(onClick = onClose) {
            Text(
                text = "Close",
                color = Color(0xFFBBBBBB)
            )
        }
    }
}