package com.example.playte.core

import com.example.playte.PlaylistInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun getPlaylistInfo(url: String, appState: AppState): PlaylistInfo? {
    appState.uiState = UIState.GETTINGPLAYLISTINFO

    val jsonFormat = Json {
        ignoreUnknownKeys = true
    }

    val response = getPlaylistResponse(url)
    val serializedPlaylistInfo: PlaylistInfo = jsonFormat.decodeFromString(response.out)

    if (serializedPlaylistInfo.type != "playlist") {
        appState.uiState = UIState.ERROR
        appState.info = "This is URL to a video, please enter a playlist URL"
        return null
    }

    appState.uiState = UIState.DOWNLOADING
    return serializedPlaylistInfo

}

fun getPlaylistResponse(url: String): YoutubeDLResponse {
    val request = YoutubeDLRequest(url)
    request.addOption("--compat-options", "no-youtube-unavailable-videos")
    request.addOption("--flat-playlist")
    request.addOption("-J")
    request.addOption("-R", "1")
    request.addOption("--socket-timeout", "5")
    return YoutubeDL.getInstance().execute(request)
}