package com.example.playte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaylistInfoView(info: PlaylistInfo) {
    Surface(modifier = Modifier.padding(12.dp), contentColor = Color.Black) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Text("Title: ${info.title}")
            Divider()
            Text("Number of videos: ${info.entries?.size}")
        }
    }
}