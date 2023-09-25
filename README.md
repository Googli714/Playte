# Playte

## **Music Playlist Downloader for Android built with Jetpack Compose**

Given a playlist URL, Playte finds which songs are missing from the music directory and downloads them

## Features
- It uses YoutubeDL to Fetch Playlist information and download the audio files
- Automatically scans music directory for playlist folder (if not present creates one) and checks missing files
- Ability to copy from the clipboard or share the link to the app for easier use
- Shows progress bar for each downloading file
- Ability to update the YoutubeDL binary
- Uses multithreading to download files much faster
- Warns if the user tries to download songs using mobile data
- Uses Material UI for consistent and pleasing UI
