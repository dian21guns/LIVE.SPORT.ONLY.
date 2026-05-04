package com.dpch21tv.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME).orEmpty()
        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty()

        title = if (channelName.isBlank()) getString(R.string.app_name) else channelName

        if (streamUrl.isBlank()) {
            Toast.makeText(this, "URL channel tidak ditemukan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_STREAM_URL = "extra_stream_url"
    }
}
