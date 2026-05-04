package com.dpch21tv.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "DPCH21TV"
            textSize = 34f
            setPadding(48, 120, 48, 48)
        }

        setContentView(title)
    }
}
