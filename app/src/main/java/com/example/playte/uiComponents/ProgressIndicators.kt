package com.example.playte.uiComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ProgressIndicators(progressInfos: Map<String, Float>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val progressModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)


        items(progressInfos.entries.toList()) { entry ->
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(text = entry.key, maxLines = 1)
                LinearProgressIndicator(
                    progress = entry.value,
                    modifier = progressModifier,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Preview
@Composable
fun ProgressIndicatorsPreview() {
    ProgressIndicators(progressInfos = mapOf("Test" to 1f, "Test2" to 0.5f))
}