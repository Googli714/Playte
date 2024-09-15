package com.example.playte.utils

import com.example.playte.Entries
import com.example.playte.core.AppState
import com.example.playte.core.ProgressInfo
import com.example.playte.core.ProgressState


fun findMissingFiles(entries: List<Entries>, localEntries: List<String>, appState: AppState) : ArrayList<Entries> {
    val missingFiles = arrayListOf<Entries>()

    for (entry in entries) {
        if (!localEntries.contains(sanitizeFileName(entry.title))) {
            missingFiles.add(entry)
            appState.addProgressInfo(sanitizeFileName(entry.title), ProgressInfo(0.0f, ProgressState.INACTIVE, entry.thumbnails!!.first().url))
        }
    }

    return missingFiles
}