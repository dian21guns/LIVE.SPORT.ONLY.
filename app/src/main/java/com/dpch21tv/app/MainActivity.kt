package com.dpch21tv.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.net.URL
import java.util.concurrent.Executors

data class Channel(
    val name: String,
    val url: String,
    val groupTitle: String? = null,
    val logoUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val clearKey: String? = null
)

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.channelRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadChannels()
    }

    private fun loadChannels() {
        executor.execute {
            val remotePlaylistUrl =
                "https://raw.githubusercontent.com/shareext-reborn/Shareext-LokalHiburan.m3u/refs/heads/main/Shareext%40LokalHiburan.m3u"

            val channels = try {
                parseM3uLines(URL(remotePlaylistUrl).readText().lines())
            } catch (_: Exception) {
                parseM3uLines(assets.open("channels.m3u").bufferedReader().use { it.readLines() })
            }

            runOnUiThread {
                if (channels.isEmpty()) {
                    Toast.makeText(this, "Channel tidak ditemukan", Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }
                recyclerView.adapter = ChannelAdapter(channels) { selected ->
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
        var pendingGroup: String? = null
        var pendingLogo: String? = null

        for (raw in lines) {
            val line = raw.trim()
            when {
                line.startsWith("#KODIPROP:inputstream.adaptive.license_key=", ignoreCase = true) ->
                    pendingClearKey = line.substringAfter("=", "").trim().ifBlank { null }
                line.startsWith("#EXTVLCOPT:http-referrer=", ignoreCase = true) ->
                    pendingReferrer = line.substringAfter("=", "").trim().ifBlank { null }
                line.startsWith("#EXTVLCOPT:http-user-agent=", ignoreCase = true) ->
                    pendingUserAgent = line.substringAfter("=", "").trim().ifBlank { null }
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    pendingName = line.substringAfterLast(',').trim().ifBlank { "Unknown Channel" }
                    pendingGroup = extractAttr(line, "group-title")
                    pendingLogo = extractAttr(line, "tvg-logo")
                }
                line.isNotBlank() && !line.startsWith("#") -> {
                    val headers = buildMap {
                        pendingReferrer?.let { put("Referer", it) }
                        pendingUserAgent?.let { put("User-Agent", it) }
                    }
                    channels.add(Channel(
                        name = pendingName ?: "Unknown Channel",
                        url = line,
                        groupTitle = pendingGroup,
                        logoUrl = pendingLogo,
                        headers = headers,
                        clearKey = pendingClearKey
                    ))
                    pendingName = null; pendingGroup = null; pendingLogo = null
                    pendingReferrer = null; pendingUserAgent = null; pendingClearKey = null
                }
            }
        }
        return channels
    }

    private fun extractAttr(line: String, key: String): String? {
        val regex = Regex("""$key=\"([^\"]+)\""", RegexOption.IGNORE_CASE)
        return regex.find(line)?.groupValues?.getOrNull(1)
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }

    inner class ChannelAdapter(
        private val items: List<Channel>,
        private val onClick: (Channel) -> Unit
    ) : RecyclerView.Adapter<ChannelViewHolder>() {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ChannelViewHolder {
            val view = layoutInflater.inflate(R.layout.item_channel, parent, false)
            return ChannelViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
            holder.bind(items[position], onClick)
        }
    }

    inner class ChannelViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val logo = itemView.findViewById<android.widget.ImageView>(R.id.channelLogo)
        private val name = itemView.findViewById<android.widget.TextView>(R.id.channelName)
        private val subtitle = itemView.findViewById<android.widget.TextView>(R.id.channelSubtitle)

        fun bind(item: Channel, onClick: (Channel) -> Unit) {
            name.text = item.name
            subtitle.text = item.groupTitle ?: "Live TV"
            if (!item.logoUrl.isNullOrBlank()) {
                Glide.with(itemView).load(item.logoUrl).into(logo)
            } else {
                logo.setImageDrawable(null)
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
