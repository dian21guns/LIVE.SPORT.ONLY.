package com.dpch21tv.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import java.util.concurrent.Executors

data class Channel(
    val name: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val clearKey: String? = null
)

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
                "https://raw.githubusercontent.com/shareext-reborn/Shareext-LokalHiburan.m3u/refs/heads/main/Shareext%40LokalHiburan.m3u"

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
                        putExtra(PlayerActivity.EXTRA_REFERRER, selected.headers["Referer"])
                        putExtra(PlayerActivity.EXTRA_USER_AGENT, selected.headers["User-Agent"])
                        putExtra(PlayerActivity.EXTRA_CLEAR_KEY, selected.clearKey)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun parseM3uLines(lines: List<String>): List<Channel> {
        val channels = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingReferrer: String? = null
        var pendingUserAgent: String? = null
        var pendingClearKey: String? = null

        for (raw in lines) {
            val line = raw.trim()
            when {
                line.startsWith("#KODIPROP:inputstream.adaptive.license_key=", ignoreCase = true) -> {
                    pendingClearKey = line.substringAfter("=", "").trim().ifBlank { null }
                }

                line.startsWith("#EXTVLCOPT:http-referrer=", ignoreCase = true) -> {
                    pendingReferrer = line.substringAfter("=", "").trim().ifBlank { null }
                }

                line.startsWith("#EXTVLCOPT:http-user-agent=", ignoreCase = true) -> {
                    pendingUserAgent = line.substringAfter("=", "").trim().ifBlank { null }
                }

                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    pendingName = line.substringAfterLast(',').trim().ifBlank { "Unknown Channel" }
                }

                line.isNotBlank() && !line.startsWith("#") -> {
                    val headers = buildMap {
                        pendingReferrer?.let { put("Referer", it) }
                        pendingUserAgent?.let { put("User-Agent", it) }
                    }
                    channels.add(
                        Channel(
                            name = pendingName ?: "Unknown Channel",
                            url = line,
                            headers = headers,
                            clearKey = pendingClearKey
                        )
                    )
                    pendingName = null
                    pendingReferrer = null
                    pendingUserAgent = null
                    pendingClearKey = null
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
