package com.example.musicfinder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.musicfinder.api.Song
import com.example.musicfinder.ui.MusicDashboard
import com.example.musicfinder.ui.SongDetailScreen
import java.net.URLEncoder

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {

        composable("dashboard") {
            MusicDashboard(onSongClick = { song ->
                // URL Encode parameters to handle special characters
                val title = URLEncoder.encode(song.trackName ?: "", "UTF-8")
                val artist = URLEncoder.encode(song.artistName ?: "", "UTF-8")
                val image = URLEncoder.encode(song.artworkUrl100 ?: "", "UTF-8")
                val preview = URLEncoder.encode(song.previewUrl ?: "", "UTF-8")
                navController.navigate("detail/$title/$artist/$image/$preview")
            })
        }

        composable("detail/{title}/{artist}/{image}/{preview}") { backStack ->
            SongDetailScreen(
                title = backStack.arguments?.getString("title") ?: "",
                artist = backStack.arguments?.getString("artist") ?: "",
                image = backStack.arguments?.getString("image") ?: "",
                preview = backStack.arguments?.getString("preview") ?: ""
            )
        }
    }
}
