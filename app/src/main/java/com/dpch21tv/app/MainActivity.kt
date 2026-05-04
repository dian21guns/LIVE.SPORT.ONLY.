package com.dpch21tv.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

data class Channel(val name: String, val url: String)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val channels = loadChannelsFromPlaylist("channels.m3u")
        val listView = findViewById<ListView>(R.id.channelListView)

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

    private fun loadChannelsFromPlaylist(fileName: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = assets.open(fileName).bufferedReader().use { it.readLines() }

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
}
