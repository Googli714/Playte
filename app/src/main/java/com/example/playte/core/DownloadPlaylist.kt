package com.example.playte.core

import android.os.Environment
import android.util.Log
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.example.playte.Entries
import com.example.playte.PlaylistInfo
import com.example.playte.utils.getPlaylistFilesInPlayteFolder
import com.example.playte.utils.sanitizeFileName
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.File

suspend fun downloadPlaylist(
    url: String,
    cs: CoroutineScope,
    appState: AppState
) {
    appState.clear()

    if (url.isBlank()) {
        appState.changeTo(UIState.ERROR, "The link cannot be empty")
        return
    }

    appState.changeTo(UIState.GETTINGPLAYLISTINFO, "Fetching playlist info")

    val playlistInfo: PlaylistInfo = getPlaylistInfo(url, appState) ?: return

    val localSongs = getPlaylistFilesInPlayteFolder(playlistInfo.title!!) // can title be null?

    //if (localSongs == null) {
    //
    //    appState.uiState = UIState.LOCALPLAYLISTERROR
    //    appState.info = "Error with local Music"
    //    return
    //}

    val toDownload = arrayListOf<Entries>()

    for (entry in playlistInfo.entries!!) {
        if (!localSongs.contains(sanitizeFileName(entry.title))) {
            toDownload.add(entry)
            appState.updateProgressInfo(sanitizeFileName(entry.title),  0f)
        }
    }

    if (toDownload.size == 0) {
        appState.changeTo(UIState.FINISHED, "Local playlist is up to date")
        return
    }

    appState.start()
    appState.changeTo(UIState.DOWNLOADING, "Downloading")

    val downloadPath = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Playte"
    )

    val jobs = toDownload.map { song ->
        cs.async {
            downloadSong(song, playlistInfo.title, downloadPath, appState)
        }
    }

    jobs.awaitAll()

    appState.changeTo(UIState.FINISHED, "Download finished")
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
fun checkPermissionAndDownload(
    url: String,
    cs: CoroutineScope,
    appState: AppState,
    storagePermission: PermissionState,
    keyboardController: SoftwareKeyboardController?
) {
    cs.launch {
        try {
            appState.changeTo(UIState.GETTINGPLAYLISTINFO, "Fetching playlist info")
            keyboardController?.hide()
            if (storagePermission.status == PermissionStatus.Granted) {
                downloadPlaylist(url, cs, appState)
            }
            else {
                storagePermission.launchPermissionRequest()
            }
        }
        catch (e: Exception) {
            appState.changeTo(UIState.ERROR, e.message.toString())
            return@launch
        }
    }
}

fun downloadSong(song: Entries, playlistName: String, downloadPath: File, appState: AppState) {
    val request = YoutubeDLRequest(song.url.toString())
        .addOption(
            "-o",
            downloadPath.absolutePath + "/${playlistName}" + "/${sanitizeFileName(song.title)}.%(ext)s"
        )
        .addOption("-i")
        //.addOption("-f", "140")
        .addOption("--embed-thumbnail")
        .addOption("--embed-metadata")
        .addOption("-x")
        .addOption("--audio-format", "best")
        .addOption("--audio-quality", 0)

    YoutubeDL.getInstance().execute(request)
    { progress: Float, _: Long, _: String ->
        Log.i("d", "${sanitizeFileName(song.title)} - $progress")
        appState.updateProgressInfo(sanitizeFileName(song.title), progress / 100f)
    }
    appState.downloaded++
}
