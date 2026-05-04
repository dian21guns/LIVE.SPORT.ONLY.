package com.dpch21tv.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import java.util.concurrent.Executors

data class Channel(val name: String, val url: String)

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.channelListView)
        loadChannels()
    }

    private fun loadChannels() {
        executor.execute {
            val remotePlaylistUrl =
                "https://raw.githubusercontent.com/pk0979/m3u-live/refs/heads/main/all.m3u"

            val channels = try {
                val remoteContent = URL(remotePlaylistUrl).readText()
                parseM3uLines(remoteContent.lines())
            } catch (_: Exception) {
                val localLines = assets.open("channels.m3u").bufferedReader().use { it.readLines() }
                parseM3uLines(localLines)
            }

            runOnUiThread {
                if (channels.isEmpty()) {
                    Toast.makeText(this, "Channel tidak ditemukan", Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    channels.map { it.name }
                )
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val selected = channels[position]
                    val intent = Intent(this, PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, selected.name)
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, selected.url)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun parseM3uLines(lines: List<String>): List<Channel> {
        val channels = mutableListOf<Channel>()
        var pendingName: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    pendingName = line.substringAfterLast(',').trim().ifBlank { "Unknown Channel" }
                }

                line.isNotBlank() && !line.startsWith("#") -> {
                    val channelName = pendingName ?: "Unknown Channel"
                    channels.add(Channel(name = channelName, url = line.trim()))
                    pendingName = null
                }
            }
        }

        return channels
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }
}
