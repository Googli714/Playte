package com.example.playte.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProgressInfo(progress: Float, progressState: ProgressState = ProgressState.INACTIVE, url: String) {
    var progress by mutableStateOf(value = progress)
    var progressState by mutableStateOf(value = progressState)
    var url = url
}

enum class ProgressState {
    INACTIVE,
    ERRORED,
    DOWNLOADING,
    FINISSHED
}
