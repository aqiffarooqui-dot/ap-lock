package com.example.applock

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F2F2F7"))
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }

        val titleView = TextView(this).apply {
            text = "About App"
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
            setPadding(0, 0, 0, 24)
        }
        mainLayout.addView(titleView)

        // Developer Info Card
        val devCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.settings_card_bg)
            setPadding(24, 24, 24, 24)
        }

        val appName = TextView(this).apply {
            text = "Farooqui App Lock"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
        devCard.addView(appName)

        val devName = TextView(this).apply {
            text = "Developer: Mohd Aqif Farooqui"
            textSize = 15f
            setTextColor(Color.parseColor("#3A3A3C"))
            setPadding(0, 8, 0, 0)
        }
        devCard.addView(devName)

        val currentVersion = TextView(this).apply {
            text = "Current Version: v1.0.2 (Build 3)"
            textSize = 14f
            setTextColor(Color.parseColor("#8E8E93"))
            setPadding(0, 4, 0, 0)
        }
        devCard.addView(currentVersion)

        mainLayout.addView(devCard)

        // Release History Header
        val historyTitle = TextView(this).apply {
            text = "VERSION HISTORY & CHANGES"
            textSize = 13f
            setTextColor(Color.parseColor("#6D6D72"))
            setPadding(16, 32, 16, 8)
        }
        mainLayout.addView(historyTitle)

        // Dynamic Release History Cards
        val releases = ReleaseManager.getReleaseHistory()
        for (release in releases) {
            val releaseCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.settings_card_bg)
                setPadding(24, 20, 24, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
            }

            val verHeader = TextView(this).apply {
                text = "${release.versionName} — ${release.releaseDate}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor("#007AFF"))
            }
            releaseCard.addView(verHeader)

            val changesListText = StringBuilder()
            for (change in release.changes) {
                changesListText.append("• $change\n")
            }

            val changesView = TextView(this).apply {
                text = changesListText.toString().trim()
                textSize = 14f
                setTextColor(Color.parseColor("#3A3A3C"))
                setPadding(0, 8, 0, 0)
            }
            releaseCard.addView(changesView)

            mainLayout.addView(releaseCard)
        }

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
}
