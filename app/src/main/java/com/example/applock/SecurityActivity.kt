package com.example.applock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SecurityActivity : AppCompatActivity() {

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildScreen()
        refreshSecurity()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) {
            refreshSecurity()
        }
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF6F7FB.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(18), dp(20), dp(12))
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(0xFF111318.toInt())

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Security Center"
            textSize = 25f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111318.toInt())
        }

        val subtitle = TextView(this).apply {
            text = "Check and maintain your protection"
            textSize = 14f
            setTextColor(0xFF737780.toInt())
            setPadding(0, dp(3), 0, 0)
        }

        titleBox.addView(title)
        titleBox.addView(subtitle)

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        root.addView(header)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(30))
        }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun refreshSecurity() {

        content.removeAllViews()

        addOverviewCard()

        addSecurityItem(
            title = "App Protection",
            description = "Accessibility service monitors protected apps.",
            status = if (isAccessibilityEnabled()) {
                "Active"
            } else {
                "Needs attention"
            },
            active = isAccessibilityEnabled(),
            buttonText = if (isAccessibilityEnabled()) {
                "Check"
            } else {
                "Enable"
            }
        ) {
            openAccessibilitySettings()
        }

        addSecurityItem(
            title = "System Authentication",
            description = "Uses your phone's fingerprint, face, PIN, pattern or password.",
            status = "System",
            active = true,
            buttonText = "Info"
        ) {
            showInfo(
                "System Authentication",
                "Farooqui App Lock uses Android's system authentication. The app does not create or store a separate App Lock PIN."
            )
        }

        val intruderEnabled =
            SettingsManager.isIntruderSelfieEnabled(this)

        addSecurityItem(
            title = "Intruder Detection",
            description = "Captures an intruder selfie after a genuine failed authentication attempt.",
            status = if (intruderEnabled) {
                "Enabled"
            } else {
                "Disabled"
            },
            active = intruderEnabled,
            buttonText = "Settings"
        ) {
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }

        val deviceAdminEnabled =
            isDeviceAdminEnabled()

        addSecurityItem(
            title = "Uninstall Protection",
            description = "Uses Android Device Administrator protection.",
            status = if (deviceAdminEnabled) {
                "Active"
            } else {
                "Not enabled"
            },
            active = deviceAdminEnabled,
            buttonText = if (deviceAdminEnabled) {
                "Active"
            } else {
                "Enable"
            }
        ) {
            openDeviceAdminSettings()
        }

        addSecurityItem(
            title = "Screen Security",
            description = "Sensitive App Lock screens can prevent screenshots and screen capture.",
            status = "Protected",
            active = true,
            buttonText = "Info"
        ) {
            showInfo(
                "Screen Security",
                "Farooqui App Lock can use Android's secure-window protection on sensitive screens."
            )
        }

        addSecurityItem(
            title = "Background Protection",
            description = "App monitoring depends on the Accessibility service remaining enabled.",
            status = if (isAccessibilityEnabled()) {
                "Running"
            } else {
                "Needs attention"
            },
            active = isAccessibilityEnabled(),
            buttonText = "Check"
        ) {
            openAccessibilitySettings()
        }

        addSecurityItem(
            title = "Privacy",
            description = "No account, ads, subscription or cloud upload by default.",
            status = "Local only",
            active = true,
            buttonText = "Info"
        ) {
            showInfo(
                "Privacy",
                "Farooqui App Lock is designed as a local-first app. Intruder photos are saved on the device in the Farooqui App Lock Gallery folder."
            )
        }
    }

    private fun addOverviewCard() {

        val card = MaterialCardView(this).apply {
            radius = dp(24).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(0xFFEFF6FF.toInt())
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val shield = TextView(this).apply {
            text = "🛡️"
            textSize = 32f
            gravity = Gravity.CENTER
        }

        row.addView(
            shield,
            LinearLayout.LayoutParams(dp(54), dp(54))
        )

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Security Overview"
            textSize = 19f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        }

        val status = TextView(this).apply {
            text = if (isAccessibilityEnabled()) {
                "Protection service is active"
            } else {
                "Complete setup to activate protection"
            }
            textSize = 14f
            setTextColor(0xFF4B5563.toInt())
            setPadding(0, dp(4), 0, 0)
        }

        textBox.addView(title)
        textBox.addView(status)

        row.addView(
            textBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        box.addView(row)

        val button = MaterialButton(this).apply {
            text = if (isAccessibilityEnabled()) {
                "Protection Active"
            } else {
                "Complete Setup"
            }

            isEnabled = !isAccessibilityEnabled()

            setOnClickListener {
                openAccessibilitySettings()
            }
        }

        box.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                topMargin = dp(16)
            }
        )

        card.addView(box)

        content.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
        )
    }

    private fun addSecurityItem(
        title: String,
        description: String,
        status: String,
        active: Boolean,
        buttonText: String,
        action: () -> Unit
    ) {

        val card = MaterialCardView(this).apply {
            radius = dp(20).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(0xFFFFFFFF.toInt())
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(17), dp(18), dp(17))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val indicator = View(this).apply {
            setBackgroundColor(
                if (active) {
                    0xFF16A34A.toInt()
                } else {
                    0xFFF59E0B.toInt()
                }
            )
        }

        top.addView(
            indicator,
            LinearLayout.LayoutParams(
                dp(8),
                dp(8)
            ).apply {
                rightMargin = dp(12)
            }
        )

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        }

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
            setPadding(0, dp(3), 0, 0)
        }

        textBox.addView(titleView)
        textBox.addView(descriptionView)

        top.addView(
            textBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        container.addView(top)

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(12), 0, 0)
        }

        val statusView = TextView(this).apply {
            text = status
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(
                if (active) {
                    0xFF15803D.toInt()
                } else {
                    0xFFB45309.toInt()
                }
            )
        }

        bottom.addView(
            statusView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val actionButton = MaterialButton(this).apply {
            text = buttonText
            minHeight = dp(42)
            setOnClickListener {
                action()
            }
        }

        bottom.addView(
            actionButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(44)
            )
        )

        container.addView(bottom)
        card.addView(container)

        content.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )
    }

    private fun isAccessibilityEnabled(): Boolean {

        val expected = ComponentName(
            this,
            AppAccessibilityService::class.java
        )

        val enabledServices =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

        return enabledServices
            .split(':')
            .any {
                it.equals(
                    expected.flattenToString(),
                    ignoreCase = true
                )
            }
    }

    private fun isDeviceAdminEnabled(): Boolean {

        val devicePolicyManager =
            getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        val adminComponent = ComponentName(
            this,
            MyDeviceAdminReceiver::class.java
        )

        return devicePolicyManager.isAdminActive(
            adminComponent
        )
    }

    private fun openAccessibilitySettings() {

        try {
            startActivity(
                Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                )
            )
        } catch (_: Exception) {
            showInfo(
                "Accessibility Settings",
                "Unable to open Accessibility Settings on this device."
            )
        }
    }

    private fun openDeviceAdminSettings() {

        try {

            val component = ComponentName(
                this,
                MyDeviceAdminReceiver::class.java
            )

            val intent = Intent(
                "android.app.action.ADD_DEVICE_ADMIN"
            ).apply {

                putExtra(
                    "android.app.extra.DEVICE_ADMIN",
                    component
                )

                putExtra(
                    "android.app.extra.ADD_EXPLANATION",
                    "Enable uninstall protection for Farooqui App Lock."
                )
            }

            startActivity(intent)

        } catch (_: Exception) {

            startActivity(
                Intent(
                    Settings.ACTION_SECURITY_SETTINGS
                )
            )
        }
    }

    private fun showInfo(
        title: String,
        message: String
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
