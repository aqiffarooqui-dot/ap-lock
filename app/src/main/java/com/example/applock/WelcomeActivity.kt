package com.example.applock

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WelcomeActivity : AppCompatActivity() {

    private val prefName = "FarooquiAppLockPrefs"
    private val keyFirstLaunch = "is_first_launch"

    private val cameraPermissionRequestCode = 501

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs: SharedPreferences =
            getSharedPreferences(
                prefName,
                MODE_PRIVATE
            )

        val isFirstLaunch =
            prefs.getBoolean(
                keyFirstLaunch,
                true
            )

        if (!isFirstLaunch) {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
            return
        }

        buildWelcomeScreen()
    }

    private fun buildWelcomeScreen() {

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setBackgroundColor(
                    Color.parseColor("#F7F8FC")
                )

                setPadding(
                    dp(28),
                    dp(60),
                    dp(28),
                    dp(36)
                )
            }

        val icon =
            TextView(this).apply {

                text = "🛡️"

                textSize = 54f

                gravity = Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(16)
                )
            }

        root.addView(icon)

        val title =
            TextView(this).apply {

                text =
                    "Farooqui App Lock"

                textSize = 28f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#111827")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(10)
                )
            }

        root.addView(title)

        val subtitle =
            TextView(this).apply {

                text =
                    "Modern privacy protection for your apps"

                textSize = 16f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(8),
                    0,
                    dp(8),
                    dp(28)
                )
            }

        root.addView(subtitle)

        val features =
            TextView(this).apply {

                text =
                    "✓ System biometric / PIN / password protection\n\n" +
                    "✓ Ask biometric every time or stay unlocked\n\n" +
                    "✓ Intruder selfie protection\n\n" +
                    "✓ Security Center & Accessibility monitoring\n\n" +
                    "✓ Intruder photos saved to your Gallery\n\n" +
                    "✓ Privacy-first — no account required"

                textSize = 15f

                setTextColor(
                    Color.parseColor("#374151")
                )

                setPadding(
                    dp(12),
                    dp(12),
                    dp(12),
                    dp(28)
                )
            }

        root.addView(features)

        val permissionInfo =
            TextView(this).apply {

                text =
                    "Camera access is used only for optional intruder selfies."

                textSize = 13f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(8),
                    0,
                    dp(8),
                    dp(20)
                )
            }

        root.addView(permissionInfo)

        val getStarted =
            Button(this).apply {

                text = "Get Started"

                textSize = 16f

                isAllCaps = false

                setTextColor(Color.WHITE)

                setBackgroundColor(
                    Color.parseColor("#2563EB")
                )

                minimumHeight =
                    dp(52)

                setOnClickListener {

                    requestCameraPermissionIfNeeded()

                    completeFirstLaunch()
                }
            }

        root.addView(
            getStarted,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        setContentView(root)
    }

    private fun requestCameraPermissionIfNeeded() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                cameraPermissionRequestCode
            )
        }
    }

    private fun completeFirstLaunch() {

        getSharedPreferences(
            prefName,
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                keyFirstLaunch,
                false
            )
            .apply()

        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )

        finish()
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
