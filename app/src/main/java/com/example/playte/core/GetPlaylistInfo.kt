package com.example.playte.core

import com.example.playte.PlaylistInfo
import com.example.playte.getPlaylistResponse
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