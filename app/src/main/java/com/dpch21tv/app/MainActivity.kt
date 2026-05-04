package com.dpch21tv.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val channelList = listOf(
        "RCTI",
        "SCTV",
        "Indosiar",
        "MNCTV",
        "GTV",
        "Trans TV",
        "Trans7",
        "TV One",
        "Kompas TV",
        "Metro TV"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.channelListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, channelList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "Pilih channel: ${channelList[position]}", Toast.LENGTH_SHORT).show()
        }
    }
}
