package com.example.playte

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.max


@Composable
fun ProgressIndicators(infos: Map<String, Float>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val progressModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
        val columnModifier = Modifier.padding(vertical = 2.dp)
        for (info in infos) {
            Column(modifier = columnModifier) {
                Text(text = info.key, maxLines = 1)
                LinearProgressIndicator(
                    progress = info.value,
                    modifier = progressModifier
                )
            }
        }
    }
}