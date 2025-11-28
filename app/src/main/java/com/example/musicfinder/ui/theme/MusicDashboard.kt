package com.example.musicfinder.ui

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicfinder.api.RetrofitClient
import com.example.musicfinder.api.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


// =======================================================
// MULTI LANGUAGE TEXT
// =======================================================

data class LangPack(
    val appTitle: String,
    val subtitle: String,
    val searchHint: String,
    val searchButton: String,
    val placeholder: String,
    val errorMsg: String,
)

val LANG_ID = LangPack(
    appTitle = "MusicFinder",
    subtitle = "Temukan musik favoritmu 🎧",
    searchHint = "Cari lagu atau artis…",
    searchButton = "Cari Lagu",
    placeholder = "Mulai dengan mencari lagu…",
    errorMsg = "Gagal mengambil data!"
)

val LANG_EN = LangPack(
    appTitle = "MusicFinder",
    subtitle = "Find your favorite music 🎧",
    searchHint = "Search song or artist…",
    searchButton = "Search Song",
    placeholder = "Start by searching for a song…",
    errorMsg = "Failed to fetch data!"
)


// =======================================================
// DASHBOARD
// =======================================================
@Composable
fun MusicDashboard(
    onSongClick: (Song) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    var currentPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingTrack by remember { mutableStateOf<String?>(null) }

    // LANGUAGE STATE
    var currentLang by remember { mutableStateOf("id") }
    val lang = if (currentLang == "id") LANG_ID else LANG_EN

    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


        // HEADER BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        lang.appTitle,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        lang.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }

                // 🌍 LANGUAGE BUTTON
                IconButton(
                    onClick = {
                        currentLang = if (currentLang == "id") "en" else "id"
                    }
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Change Language",
                        tint = Color.White
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(20.dp))


        // SEARCH BAR
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it

                currentPlayer?.stop()
                currentPlayer?.release()
                currentPlayer = null
                currentlyPlayingTrack = null

                if (searchText.isBlank()) songs = emptyList()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(lang.searchHint) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchText.isNotBlank()) {
                        loadSongs(
                            searchText,
                            scope,
                            setLoading = { loading = it },
                            setSongs = { songs = it },
                            setError = { error = it },
                            onBeforeSearch = {
                                currentPlayer?.stop()
                                currentPlayer?.release()
                                currentPlayer = null
                                currentlyPlayingTrack = null
                            }
                        )
                    }
                }
            )
        )


        Spacer(modifier = Modifier.height(12.dp))


        // SEARCH BUTTON
        Button(
            onClick = {
                if (searchText.isNotBlank()) {
                    loadSongs(
                        searchText,
                        scope,
                        setLoading = { loading = it },
                        setSongs = { songs = it },
                        setError = { error = it },
                        onBeforeSearch = {
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            currentPlayer = null
                            currentlyPlayingTrack = null
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(lang.searchButton)
        }


        Spacer(modifier = Modifier.height(15.dp))


        // LOADING
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }


        // ERROR
        if (error.isNotEmpty()) {
            Text(lang.errorMsg, color = MaterialTheme.colorScheme.error)
        }


        // PLACEHOLDER
        if (!loading && songs.isEmpty() && searchText.isBlank()) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                lang.placeholder,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


        // SONG LIST
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(songs) { song ->
                SongCard(
                    song = song,
                    isPlaying = currentlyPlayingTrack == song.previewUrl,
                    onPlayPause = {

                        currentPlayer?.stop()
                        currentPlayer?.release()
                        currentPlayer = null

                        if (currentlyPlayingTrack == song.previewUrl) {
                            currentlyPlayingTrack = null
                            return@SongCard
                        }

                        val mp = MediaPlayer().apply {
                            setDataSource(song.previewUrl)
                            prepareAsync()
                            setOnPreparedListener { start() }
                            setOnCompletionListener {
                                currentlyPlayingTrack = null
                            }
                        }

                        currentPlayer = mp
                        currentlyPlayingTrack = song.previewUrl
                    },
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}



// LOAD SONG FUNCTION
fun loadSongs(
    query: String,
    scope: CoroutineScope,
    setLoading: (Boolean) -> Unit,
    setSongs: (List<Song>) -> Unit,
    setError: (String) -> Unit,
    onBeforeSearch: () -> Unit
) {
    setLoading(true)
    setError("")
    onBeforeSearch()

    scope.launch {
        try {
            val response = RetrofitClient.instance.searchSongs(query)
            setSongs(response.results)
        } catch (e: Exception) {
            setError("ERR")
        }
        setLoading(false)
    }
}



// =======================================================
// SONG CARD
// =======================================================
@Composable
fun SongCard(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = song.artworkUrl100,
                contentDescription = null,
                modifier = Modifier
                    .size(75.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(song.trackName ?: "No Title", fontWeight = FontWeight.Bold)
                Text(song.artistName ?: "Unknown Artist")
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}