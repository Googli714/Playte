package com.example.playte

import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistInfo(
    val uploader: String? = null,
    val availability: String? = null,
    val channel: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("_type") val type: String? = null,
    val entries: List<Entries>? = emptyList(),
    @SerialName("webpage_url") val webpageUrl: String? = null,
    @SerialName("extractor_key") val extractorKey: String? = null
)

@Serializable
data class Thumbnail(
    val url: String,
    val height: Int = 0,
    val width: Int = 0,
)

@Serializable
data class Entries(
    @SerialName("_type") val type: String? = null,
    val ieKey: String? = null,
    val id: String? = null,
    val url: String? = null,
    val title: String? = null,
    val duration: Double? = .0,
    val uploader: String? = null,
    val channel: String? = null,
    val thumbnails: List<Thumbnail>? = emptyList(),
)

fun getPlaylistResponse(url: String): YoutubeDLResponse {
    val request = YoutubeDLRequest(url)
    request.addOption("--compat-options", "no-youtube-unavailable-videos")
    request.addOption("--flat-playlist")
    request.addOption("-J")
    request.addOption("-R", "1")
    request.addOption("--socket-timeout", "5")
    return YoutubeDL.getInstance().execute(request)
}