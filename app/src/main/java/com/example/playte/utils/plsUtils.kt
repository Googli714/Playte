package com.example.playte.utils

import android.os.Environment
import com.example.playte.Entries
import java.io.File

fun generatePLSFile(entries: List<Entries>, playlistName: String) {
    val downloadPath = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "Playte"
    )

    val sb = StringBuilder()

    sb.appendLine("[playlist]")

    entries.forEachIndexed{ index, entry ->
            sb.appendLine("File${index+1}=${downloadPath.absolutePath}/${playlistName}/${sanitizeFileName(entry.title)}.m4a")
    }

    sb.appendLine("NumberOfEntries=${entries.size}")
    sb.appendLine("Version=2")

    val plsFile = File("${downloadPath.absolutePath}/${playlistName}", "${playlistName}.pls")

    if(plsFile.exists()) {
        plsFile.delete()
    }
    else {
        plsFile.createNewFile()
    }

    plsFile.writeText(sb.toString())
}