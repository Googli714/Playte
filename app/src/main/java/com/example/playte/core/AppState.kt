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
    var progressInfos: SnapshotStateMap<String, ProgressInfo> = mutableStateMapOf()
    var toDownload by mutableStateOf(0)
    var downloaded by mutableStateOf(0)

    fun updateProgressInfo(key: String, progress: Float, progressState: ProgressState? = null) {
        progressInfos[key]!!.progress = progress
        if(progressState != null) {
            progressInfos[key]!!.progressState = progressState
        }
    }

    fun addProgressInfo(key: String, progressInfo: ProgressInfo) {
        progressInfos[key] = progressInfo
    }

    fun start() {
        toDownload = progressInfos.size
        downloaded = 0
    }

    fun clear() {
        progressInfos.clear()
        toDownload = 0
        downloaded = 0
    }

    fun progress(): Float {
        if(downloaded == 0) {
            return 0f
        }
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