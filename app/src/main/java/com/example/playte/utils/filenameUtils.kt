package com.example.playte.utils

    fun sanitizeFileName(filename: String?): String {
        if (filename != null) {
            return filename.split('"', '*', '/', ':', '<', '>', '?', '|', '\\').joinToString(separator = "")
        }
        return "" //this is bad for works for now
    }