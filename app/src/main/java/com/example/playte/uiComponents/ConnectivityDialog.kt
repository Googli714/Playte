package com.example.playte.uiComponents

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.playte.core.AppState
import com.example.playte.core.UIState

@Composable
fun ConnectivityDialog(callback : () -> Unit, appState: AppState) {
    AlertDialog(
        onDismissRequest = {
            appState.uiState = UIState.WAITING
        },
        title = {
            Text(text = "Cellular network")
        },
        text = {
            Text("Download the playlist with cellular network?")
        },
        confirmButton = {
            Button(
                onClick = {
                    appState.uiState = UIState.DOWNLOADING
                    callback()
                }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    appState.uiState = UIState.WAITING
                }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
@Preview
fun ConnectivityDialogPreview() {
    ConnectivityDialog({}, appState = AppState(UIState.WAITING, "Test"))
}