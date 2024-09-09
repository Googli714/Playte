package com.example.playte.core

import android.os.Environment
import android.util.Log
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.example.playte.Entries
import com.example.playte.PlaylistInfo
import com.example.playte.utils.findMissingFiles
import com.example.playte.utils.generatePLSFile
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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File

suspend fun downloadPlaylist(
    url: String,
    cs: CoroutineScope,
    appState: AppState
) {
    appState.clear()

    val downloadPath = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Playte"
    )

    if (url.isBlank()) {
        appState.changeTo(UIState.ERROR, "The link cannot be empty")
        return
    }

    appState.changeTo(UIState.GETTINGPLAYLISTINFO, "Fetching playlist info")

    val playlistInfo: PlaylistInfo = getPlaylistInfo(url, appState) ?: return

    val localSongs = getPlaylistFilesInPlayteFolder(playlistInfo.title!!) // can title be null?

    val toDownload = findMissingFiles(playlistInfo.entries!!, localSongs, appState)

    if (toDownload.size == 0) {
        appState.changeTo(UIState.FINISHED, "Local playlist is up to date")
        generatePLSFile(playlistInfo.entries, downloadPath, playlistInfo.title)
        return
    }

    appState.start()
    appState.changeTo(UIState.DOWNLOADING, "Downloading")

    val semaphore = Semaphore(Runtime.getRuntime().availableProcessors() / 2)

    val jobs = toDownload.map { song ->
        cs.async {
            semaphore.withPermit {
                downloadSong(song, playlistInfo.title, downloadPath, appState)
            }
        }
    }

    jobs.awaitAll()

    generatePLSFile(playlistInfo.entries, downloadPath, playlistInfo.title)

    appState.changeTo(UIState.FINISHED, "Download Finished")
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
            Log.e("error", e.toString())
            Log.e("error", e.stackTrace.toString())
            Log.e("error", e.cause.toString())
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
        .addOption("--audio-format", "aac")
        .addOption("--audio-quality", 0)

    YoutubeDL.getInstance().execute(request)
    { progress: Float, _: Long, _: String ->
        //Log.i("d", "${sanitizeFileName(song.title)} - $progress")
        appState.updateProgressInfo(sanitizeFileName(song.title), progress / 100f)
    }
    appState.downloaded++
}
