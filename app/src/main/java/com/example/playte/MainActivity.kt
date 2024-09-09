package com.example.playte

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.example.playte.ToastUtil.makeToastSuspend
import com.example.playte.core.AppState
import com.example.playte.core.UIState
import com.example.playte.core.checkPermissionAndDownload
import com.example.playte.core.downloadPlaylist
import com.example.playte.ui.theme.PlayteTheme
import com.example.playte.uiComponents.ConnectivityDialog
import com.example.playte.uiComponents.DownloadButton
import com.example.playte.uiComponents.ErrorMessage
import com.example.playte.uiComponents.ProgressIndicators
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    var cs: CoroutineScope = CoroutineScope(SupervisorJob())

    var context = this

    private var receivedURL: MutableState<String> = mutableStateOf("")

    val appState: MutableState<AppState> = mutableStateOf(AppState(uiState = UIState.WAITING, info = ""))

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

            val keyboardController = LocalSoftwareKeyboardController.current

            val connectivityManager: ConnectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val clipboardManager = LocalClipboardManager.current

            var url by receivedURL

            val appState by appState

            @OptIn(ExperimentalPermissionsApi::class)
            val storagePermission =
                rememberPermissionState(permission = WRITE_EXTERNAL_STORAGE) { granted: Boolean ->
                    if (granted) {
                        cs.launch { downloadPlaylist(url, cs, appState) }
                    }
                    else {
                        makeToastSuspend(context, "Permission denied")
                    }
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
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            modifier = Modifier.padding(4.dp),
                            containerColor = Color.Transparent
                        )
                        {
                            DownloadButton(
                                appState.uiState != UIState.GETTINGPLAYLISTINFO && appState.uiState != UIState.DOWNLOADING,
                                onDownloadClick = {
                                    if (connectivityManager.isActiveNetworkMetered) {
                                        appState.uiState = UIState.SHOWCONNECTIVITYDIALOG
                                    }
                                    else {
                                        checkPermissionAndDownload(
                                            url,
                                            cs,
                                            appState,
                                            storagePermission,
                                            keyboardController
                                        )
                                    }
                                },
                                onClipboardClick = {
                                    clipboardManager.getText()?.text?.let {
                                        url = it
                                    }
                                }
                            ) {
                                when (appState.uiState) {
                                    UIState.GETTINGPLAYLISTINFO -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(30.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        )
                                        Text(
                                            text = "Fetching Data",
                                            fontSize = 24.sp
                                        )
                                    }

                                    UIState.DOWNLOADING -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(30.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        )
                                        Text(
                                            text = "Downloading Playlist",
                                            fontSize = 24.sp
                                        )
                                    }

                                    UIState.FINISHED -> Text(
                                        text = appState.info,
                                        fontSize = 24.sp
                                    )

                                    else -> Text(text = "Download", fontSize = 24.sp)
                                }
                            }
                        }
                    },
                    content = { pv ->
                        Column(
                            modifier = Modifier
                                .padding(pv)
                                .padding(horizontal = 24.dp),
                        ) {
                            Text(
                                text = "Playte",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                fontSize = 48.sp
                            )

                            OutlinedTextField(
                                isError = appState.uiState == UIState.ERROR,
                                value = url,
                                onValueChange = { url = it },
                                label = { Text(text = "Playlist link") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                            AnimatedVisibility(visible = appState.uiState == UIState.ERROR) {
                                ErrorMessage(
                                    errorInfo = appState.info,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            AnimatedVisibility(visible = appState.uiState == UIState.DOWNLOADING) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Songs downloaded ${appState.downloaded}/${appState.toDownload}",
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        progress = appState.progress(),
                                        strokeCap = StrokeCap.Round
                                    )
                                    Divider(modifier = Modifier.padding(bottom = 6.dp))
                                    ProgressIndicators(progressInfos = appState.progressInfo)
                                }
                            }
                        }
                    }
                )
                if (appState.uiState == UIState.SHOWCONNECTIVITYDIALOG) {
                    ConnectivityDialog(callback = {
                        checkPermissionAndDownload(
                            url,
                            cs,
                            appState,
                            storagePermission,
                            keyboardController
                        )
                    }, appState = appState)
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
                    receivedURL.value = intent.getStringExtra(Intent.EXTRA_TEXT).toString()
                }
                else {
                    makeToastSuspend(context, "Error getting text data")
                }
            }
        }
    }
}
