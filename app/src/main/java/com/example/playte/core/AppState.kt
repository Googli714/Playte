package com.example.playte.core

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap

@Stable
class AppState(uiState: UIState, info: String) {
    var uiState by mutableStateOf(value = uiState)
    var info by mutableStateOf(value = info)
    var progressInfo: SnapshotStateMap<String, Float> = mutableStateMapOf()
    var toDownload by mutableStateOf(0)
    var downloaded by mutableStateOf(0)

    fun updateProgressInfo(key: String, value: Float) {
        progressInfo[key] = value
    }

    fun start() {
        toDownload = progressInfo.size
        downloaded = 0
    }

    fun clear() {
        progressInfo.clear()
        toDownload = 0
        downloaded = 0
    }

    fun progress(): Float {
        return downloaded.toFloat() / toDownload
    }

    fun changeTo(uiState:UIState, info: String = "") {
        this.uiState = uiState
        this.info = info
    }
}

enum class UIState {
    WAITING,
    ERROR,
    GETTINGPLAYLISTINFO,
    SHOWCONNECTIVITYDIALOG,
    DOWNLOADING,
    //LOCALPLAYLISTERROR,
    FINISHED
}