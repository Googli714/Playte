package com.example.playte.utils

import com.example.playte.Entries
import com.example.playte.core.AppState


fun findMissingFiles(entries: List<Entries>, localEntries: List<String>, appState: AppState) : ArrayList<Entries> {
    val missingFiles = arrayListOf<Entries>()

    for (entry in entries) {
        if (!localEntries.contains(sanitizeFileName(entry.title))) {
            missingFiles.add(entry)
            appState.updateProgressInfo(sanitizeFileName(entry.title), 0f)
        }
    }

    return missingFiles
}