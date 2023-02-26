package com.example.playte

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.example.playte.ToastUtil.makeToastSuspend
import com.example.playte.ui.theme.PlayteTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.String
import kotlin.Boolean
import kotlin.OptIn
import kotlin.Unit
import kotlin.let
import kotlin.toString


class MainActivity : ComponentActivity() {
    var cs: CoroutineScope = CoroutineScope(SupervisorJob())
    var context = this
    private val jsonFormat = Json {
        ignoreUnknownKeys = true
    }

    private var receivedURL: MutableState<kotlin.String> = mutableStateOf("")

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
        ExperimentalPermissionsApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onNewIntent(intent)

        try {
            YoutubeDL.getInstance().init(application)
            FFmpeg.getInstance().init(application)
            Aria2c.getInstance().init(application)
        }
        catch (e: YoutubeDLException) {
            Log.e(e.toString(), "failed to initialize youtubedl-android", e)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        setContent {
            val downloadPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Playte"
            )
            val keyboardController = LocalSoftwareKeyboardController.current


            val connectivityManager: ConnectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


            val clipboardManager = LocalClipboardManager.current


            var showPlaylistInfo by remember { mutableStateOf(false) }
            var playlistInfo by remember { mutableStateOf(PlaylistInfo()) }

            var showDialogForMeteredConnection by remember { mutableStateOf(false) }
            var showError by remember { mutableStateOf(false) }
            var errorInfo by remember { mutableStateOf("") }

            var url by receivedURL

            var songsToDownload by remember { mutableStateOf(0) }
            var songsLeftToDownload by remember { mutableStateOf(0) }
            var showDownloadProgress by remember { mutableStateOf(false) }
            var progressInfo = mutableMapOf<kotlin.String, Float>()

            fun getPlaylistInfo(url: kotlin.String): PlaylistInfo {
                val response = getPlaylistResponse(url)
                val serializedPlaylistInfo: PlaylistInfo =
                    jsonFormat.decodeFromString(response.out)
                if (serializedPlaylistInfo.type != "playlist") {
                    makeToastSuspend(
                        context,
                        "This is URL to a video, please enter a playlist URL"
                    )
                }
                else {
                    makeToastSuspend(context, "Playlist info loaded")
                    return serializedPlaylistInfo
                }
                return PlaylistInfo()
            }

            fun downloadMusic() {
                if (url.isBlank()) {
                    showError = true
                    errorInfo = "The link cannot be empty"
                    return
                }

                makeToastSuspend(context, "Fetching playlist info")
                playlistInfo = getPlaylistInfo(url)

                val localSongs = getFilesInPlayteFolder();
                if (localSongs == null) {
                    makeToastSuspend(context, "Error with local Music")
                    return
                }

                var toDownload = arrayListOf<Entries>()
                for (entry in playlistInfo.entries!!) {
                    if (!localSongs.contains(entry.title?.let { sanitizeFileName(it) })) {
                        toDownload.add(entry)
                        entry.title?.let { progressInfo.put(sanitizeFileName(it), 0f) }
                    }
                }

                if (toDownload.size == 0) {
                    makeToastSuspend(context, "Local playlist is up to date")
                    return
                }

                songsToDownload = toDownload.size
                songsLeftToDownload = toDownload.size
                showDownloadProgress = true
                makeToastSuspend(context, "Starting download")
                for (song in toDownload) {
                    cs.launch {
                        var request = YoutubeDLRequest(song.url)
                        request.addOption(
                            "-o",
                            downloadPath.absolutePath + "/${song.title?.let { sanitizeFileName(it) }}.%(ext)s"
                        )
                        request.addOption("-i")
                        //request.addOption("-f", "140")
                        request.addOption("--embed-thumbnail")
                        request.addOption("-x")
                        request.addOption("--audio-quality", "0")

                        YoutubeDL.getInstance().execute(request)
                        { progress: Float, etaInSeconds: Long, str: kotlin.String ->
                            song.title?.let { sanitizeFileName(it) }
                                ?.let { progressInfo.put(it, progress / 100f) }

                            //println("$progress% (ETA $etaInSeconds seconds) - $str")
                        }
                        songsLeftToDownload--
                    }
                }
            }

            val storagePermission =
                rememberPermissionState(permission = WRITE_EXTERNAL_STORAGE) { granted: Boolean ->
                    if (granted) {
                        println("Downloading")
                        cs.launch { downloadMusic() }
                    }
                    else {
                        makeToastSuspend(context, "Permission denied")
                    }
                }


            fun checkPermissionOrDownload() {
                cs.launch {
                    try {
                        progressInfo = mutableMapOf()
                        showDownloadProgress = false
                        showError = false
                        showPlaylistInfo = false
                        keyboardController?.hide()
                        if (storagePermission.status == PermissionStatus.Granted) {
                            downloadMusic()
                            while (songsLeftToDownload != 0 && !showError) {
                            }
                            if (!showError) {
                                makeToastSuspend(context, "Download finished")
                            }
                        }
                        else {
                            storagePermission.launchPermissionRequest()
                        }
                    }
                    catch (e: Exception) {
                        showError = true
                        errorInfo = e.message.toString()
                        makeToastSuspend(context, "${e.message}")
                    }
                }
            }

            fun getClipboardText() {
                clipboardManager.getText()?.text?.let { url = it }
            }

            PlayteTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {},
                            modifier = Modifier.padding(horizontal = 8.dp),
                            actions = {
                                IconButton(onClick = {
                                    cs.launch {
                                        UpdateUtil.updateYtDlp(context = context)
                                        YoutubeDL.getInstance().version(context)
                                            ?.let {
                                                makeToastSuspend(
                                                    context,
                                                    "YoutubeDL version: $it"
                                                )
                                            }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.FileDownload,
                                        contentDescription = "Update YoutubeDL"
                                    )
                                }
                            })

                    },

                    floatingActionButton = {
                        FABs(
                            modifier = Modifier.imePadding(),
                            downloadCallback = {
                                if (connectivityManager.isActiveNetworkMetered) {
                                    showDialogForMeteredConnection = true
                                }
                                else {
                                    checkPermissionOrDownload()
                                }
                            },
                            pasteCallback = { getClipboardText() })
                    },
                    content = { pv ->
                        Column(
                            modifier = Modifier
                                .padding(pv)
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "Playte",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                fontSize = 48.sp
                            )

                            OutlinedTextField(
                                isError = showError,
                                value = url,
                                onValueChange = { url = it },
                                label = { Text(text = "Playlist link") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (connectivityManager.isActiveNetworkMetered) {
                                            showDialogForMeteredConnection = true
                                        }
                                        else {
                                            checkPermissionOrDownload()
                                        }
                                    }
                                ),
                                trailingIcon = {
                                    if (url.isNotEmpty()) {
                                        IconButton(onClick = { url = "" }) {
                                            Icon(
                                                modifier = Modifier.size(24.dp),
                                                imageVector = Icons.Outlined.Cancel,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                maxLines = 3
                            )
                            AnimatedVisibility(visible = showPlaylistInfo) {
                                PlaylistInfoView(info = playlistInfo)
                            }
                            AnimatedVisibility(visible = showError) {
                                ErrorMessage(
                                    errorInfo = errorInfo,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            AnimatedVisibility(visible = showDownloadProgress) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Songs downloaded ${songsToDownload - songsLeftToDownload}/${songsToDownload}",
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        progress = 1 - (songsLeftToDownload.toFloat() / songsToDownload.toFloat())
                                    )
                                    Divider()
                                    ProgressIndicators(infos = progressInfo)
                                }
                            }
                        }
                    }
                )
                if (showDialogForMeteredConnection) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialogForMeteredConnection = false
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
                                    showDialogForMeteredConnection = false
                                    checkPermissionOrDownload()
                                }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showDialogForMeteredConnection = false
                                }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
        handleSendIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleSendIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleSendIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    println("New Intet $intent")
                    receivedURL.value = intent.getStringExtra(Intent.EXTRA_TEXT).toString()
                }
                else {
                    makeToastSuspend(context, "Error getting text data")
                }
            }
        }
    }

    @Preview
    @Composable
    fun FABs(
        modifier: Modifier = Modifier,
        downloadCallback: () -> Unit = {},
        pasteCallback: () -> Unit = {},
    ) {
        Column(
            modifier = modifier.padding(6.dp), horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = pasteCallback,
                content = {
                    Icon(
                        Icons.Outlined.ContentPaste, contentDescription = "Paste"
                    )
                },
                modifier = Modifier.padding(vertical = 12.dp),
                containerColor = MaterialTheme.colorScheme.primary
            )
            FloatingActionButton(
                onClick = downloadCallback, content = {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = "Download"
                    )
                },
                modifier = Modifier.padding(vertical = 12.dp),
                containerColor = MaterialTheme.colorScheme.primary
            )
        }

    }

    @Preview
    @Composable
    fun ErrorMessage(
        modifier: Modifier = Modifier,
        errorInfo: kotlin.String = ""
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Outlined.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                maxLines = 10,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 6.dp),
                text = errorInfo,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    private fun sanitizeFileName(filename: kotlin.String): kotlin.String {
        return String.join("", filename.split('"', '*', '/', ':', '<', '>', '?', '|', '\\'))
    }

    private fun getFilesInPlayteFolder(): List<kotlin.String>? {
        if (!File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Playte"
            ).exists()
        ) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Playte"
            ).mkdir()
        }
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Playte"
        )
            .listFiles { file -> file.isFile }?.map { file -> file.nameWithoutExtension }
    }
}
