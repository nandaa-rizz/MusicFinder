package com.example.musicfinder.api

data class SongResponse(
    val resultCount: Int,
    val results: List<Song>
)

data class Song(
    val trackName: String?,
    val artistName: String?,
    val artworkUrl100: String?,
    val previewUrl: String?            // ⭐ TAMBAHKAN INI
)