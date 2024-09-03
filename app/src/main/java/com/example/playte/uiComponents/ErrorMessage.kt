package com.example.playte.uiComponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ErrorMessage(
    modifier: Modifier = Modifier,
    errorInfo: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Icon(
            Icons.Outlined.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
            text = errorInfo,
            color = MaterialTheme.colorScheme.error
        )
    }
}