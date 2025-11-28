package com.example.musicfinder.ui

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SongDetailScreen(
    title: String,
    artist: String,
    image: String,
    preview: String
) {

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(artist, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (!isPlaying) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(preview)
                        prepareAsync()
                        setOnPreparedListener {
                            start()
                            isPlaying = true
                        }
                        setOnCompletionListener {
                            isPlaying = false
                        }
                    }
                } else {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    isPlaying = false
                }
            }
        ) {
            Text(if (isPlaying) "Pause" else "Play Preview")
        }
    }
}