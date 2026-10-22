package com.example.tvlive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val channels = remember { loadChannels(context) }
    val categories = remember { listOf("All") + channels.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf("All") }
    var playing by remember { mutableStateOf<Channel?>(null) }

    Box(Modifier.fillMaxSize().background(Color(0xFF101418))) {
        val current = playing
        if (current != null) {
            BackHandler { playing = null }
            PlayerScreen(current)
        } else {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("TV Live", fontSize = 28.sp, color = Color.White)
                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { cat ->
                        Button(onClick = { selectedCategory = cat }) {
                            Text(if (cat == selectedCategory) "● $cat" else cat)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                val shown = if (selectedCategory == "All") channels
                            else channels.filter { it.category == selectedCategory }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(shown) { ch ->
                        Card(onClick = { playing = ch }) {
                            Column(
                                Modifier.fillMaxWidth().height(110.dp).padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(ch.name, fontSize = 18.sp)
                                Text(ch.category, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(channel: Channel) {
    val context = LocalContext.current
    val player = remember(channel.url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(channel.url))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                isFocusable = true
                requestFocus()
            }
        }
    )
}
