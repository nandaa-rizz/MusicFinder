package com.example.musicfinder.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") keyword: String,
        @Query("entity") entity: String = "song"
    ): SongResponse
}