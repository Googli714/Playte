package com.example.playte.uiComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DownloadButton(
    enabled: Boolean,
    onDownloadClick: () -> Unit,
    onClipboardClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            enabled = enabled,
            onClick = onDownloadClick,
            modifier = Modifier
                .weight(1f)
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment =  Alignment.CenterHorizontally)
            ) {
                content()
            }

        }

        Button(
            enabled = enabled,
            onClick = onClipboardClick,
            modifier = Modifier
                .padding(0.dp)
                .size(58.dp)
                .padding(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Outlined.ContentPaste,
                contentDescription = "Paste",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}


@Preview
@Composable
private fun DownloadButtonPreview() {
    DownloadButton(false, onDownloadClick = {}, onClipboardClick = {}) {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(text = "Download", fontSize = 24.sp)
    }

}
