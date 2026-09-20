package com.example.applock

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private val PREF_NAME = "FarooquiAppLockPrefs"
    private val KEY_FIRST_LAUNCH = "is_first_launch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        // Agar pehli baar nahi hai, toh seedha MainActivity par bhej do
        if (!isFirstLaunch) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1C1C1E"))
            setPadding(48, 64, 48, 64)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Welcome to\nFarooqui App Lock"
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(title)

        val featuresText = TextView(this).apply {
            text = "• Zero-Delay Instant Privacy Protection\n\n" +
                   "• Biometric (Face/Fingerprint) + PIN Fallback\n\n" +
                   "• Intruder Selfie on Wrong Attempts\n\n" +
                   "• Stealth Mode & Icon Disguise\n\n" +
                   "• Device Admin Uninstall Protection"
            textSize = 15f
            setTextColor(Color.parseColor("#AEAEB2"))
            setPadding(16, 16, 16, 32)
        }
        mainLayout.addView(featuresText)

        val btnGetStarted = Button(this).apply {
            text = "Get Started"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#007AFF"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            }
        }
        mainLayout.addView(btnGetStarted)

        setContentView(mainLayout)
    }
}
