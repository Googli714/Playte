package com.example.playte.utils

import android.os.Environment
import java.io.File

fun getPlaylistFilesInPlayteFolder(playlistName: String): List<String> {
    val playlistDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Playte/$playlistName")

    if (!playlistDirectory.exists())
    {
        playlistDirectory.mkdirs()
    }

    return playlistDirectory.listFiles{f -> f.isFile}!!.map{ f -> f.nameWithoutExtension}
}