package com.dpch21tv.app

import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME).orEmpty()
        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty()
        val referrer = intent.getStringExtra(EXTRA_REFERRER)
        val userAgent = intent.getStringExtra(EXTRA_USER_AGENT)
        val clearKey = intent.getStringExtra(EXTRA_CLEAR_KEY)

        title = if (channelName.isBlank()) getString(R.string.app_name) else channelName

        if (streamUrl.isBlank()) {
            Toast.makeText(this, "URL channel tidak ditemukan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val httpFactory = DefaultHttpDataSource.Factory()
        if (!userAgent.isNullOrBlank()) httpFactory.setUserAgent(userAgent)
        if (!referrer.isNullOrBlank()) httpFactory.setDefaultRequestProperties(mapOf("Referer" to referrer))

        val playerView = findViewById<PlayerView>(R.id.playerView)
        val mediaSourceFactory = DefaultMediaSourceFactory(httpFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().also { exoPlayer ->
                playerView.player = exoPlayer
                exoPlayer.setMediaItem(buildMediaItem(streamUrl, clearKey))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
    }

    private fun buildMediaItem(streamUrl: String, clearKey: String?): MediaItem {
        val baseBuilder = MediaItem.Builder().setUri(streamUrl)

        val keyPair = clearKey?.split(":")?.map { it.trim() } ?: emptyList()
        if (keyPair.size == 2) {
            val keyId = keyPair[0]
            val key = keyPair[1]
            val clearKeyJson = """{"keys":[{"kty":"oct","kid":"$keyId","k":"$key"}],"type":"temporary"}"""
            val licenseUri = "data:application/json;base64," +
                Base64.encodeToString(clearKeyJson.toByteArray(), Base64.NO_WRAP)

            baseBuilder.setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                    .setLicenseUri(licenseUri)
                    .build()
            )
        }

        return baseBuilder.build()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_REFERRER = "extra_referrer"
        const val EXTRA_USER_AGENT = "extra_user_agent"
        const val EXTRA_CLEAR_KEY = "extra_clear_key"
    }
}
