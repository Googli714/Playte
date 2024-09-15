package com.example.playte.uiComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.playte.core.ProgressInfo
import com.example.playte.core.ProgressState

@Composable
fun ProgressIndicators(progressInfos: Map<String, ProgressInfo>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val progressModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)

        items(progressInfos.entries.toList()) { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = entry.value.url,
                    contentDescription = "Song thumbnail",
                    modifier = Modifier.size(42.dp).padding(2.dp)
                )
                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = entry.key, maxLines = 1)
                    LinearProgressIndicator(
                        progress = entry.value.progress,
                        modifier = progressModifier,
                        strokeCap = StrokeCap.Round,
                        color = if (entry.value.progressState == ProgressState.ERRORED) {
                            MaterialTheme.colorScheme.error
                        }
                        else {
                            MaterialTheme.colorScheme.primary
                        }

                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun ProgressIndicatorsPreview() {
    ProgressIndicators(progressInfos = mapOf("Test" to ProgressInfo(0.5f, ProgressState.ERRORED, ""), "Test2" to ProgressInfo(0.0f, ProgressState.DOWNLOADING,"")))
}