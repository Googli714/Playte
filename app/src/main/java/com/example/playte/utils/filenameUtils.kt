package com.example.playte.utils

    fun sanitizeFileName(filename: String?): String {
        if (filename != null) {
            return filename.split('"', '*', '/', ':', '<', '>', '?', '|', '\\').joinToString()
        }
        return "" //this is bad for works for now
    }