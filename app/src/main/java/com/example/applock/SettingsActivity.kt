package com.example.applock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var componentName: ComponentName
    private lateinit var devicePolicyManager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F2F2F7")) // iOS Background color
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }

        // Title
        val titleView = TextView(this).apply {
            text = "Settings"
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(titleView)

        // --- Security Section Group ---
        val securitySectionTitle = TextView(this).apply {
            text = "SECURITY & AUTHENTICATION"
            textSize = 13f
            setTextColor(Color.parseColor("#6D6D72"))
            setPadding(16, 0, 16, 8)
        }
        mainLayout.addView(securitySectionTitle)

        val securityCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.settings_card_bg)
            setPadding(24, 16, 24, 16)
        }

        // Option 1: Biometric Mode Preference
        val optBiometric = createSettingRow("Biometric Lock (Face/Fingerprint)", true) { isChecked ->
            val mode = if (isChecked) 0 else 1
            SettingsManager.setBiometricMode(this, mode)
        }
        securityCard.addView(optBiometric)
        securityCard.addView(createDivider())

        // Option 2: Intruder Selfie Toggle
        val optIntruder = createSettingRow("Intruder Selfie (Capture on Fail)", true) { isChecked ->
            // Intruder selfie preference active/inactive logic
        }
        securityCard.addView(optIntruder)

        mainLayout.addView(securityCard)

        // --- Privacy & Stealth Section Group ---
        val privacySectionTitle = TextView(this).apply {
            text = "PRIVACY & PROTECTION"
            textSize = 13f
            setTextColor(Color.parseColor("#6D6D72"))
            setPadding(16, 32, 16, 8)
        }
        mainLayout.addView(privacySectionTitle)

        val privacyCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.settings_card_bg)
            setPadding(24, 16, 24, 16)
        }

        // --- UNLOCK BEHAVIOR SECTION ---
val unlockBehaviorTitle = TextView(this).apply {
    text = "UNLOCK BEHAVIOR"
    textSize = 13f
    setTextColor(Color.parseColor("#6D6D72"))
    setPadding(16, 32, 16, 8)
}

mainLayout.addView(
    unlockBehaviorTitle
)

val unlockBehaviorCard =
    LinearLayout(this).apply {

        orientation =
            LinearLayout.VERTICAL

        setBackgroundResource(
            R.drawable.settings_card_bg
        )

        setPadding(
            24,
            16,
            24,
            16
        )
    }

val currentBehavior =
    SettingsManager.getUnlockBehavior(
        this
    )

val everyTimeRow =
    createSettingRow(
        "Ask biometric every time",
        currentBehavior == 0
    ) { checked ->

        if (checked) {

            SettingsManager.setUnlockBehavior(
                this,
                0
            )
        }
    }

unlockBehaviorCard.addView(
    everyTimeRow
)

unlockBehaviorCard.addView(
    createDivider()
)

val untilPhoneLockedRow =
    createSettingRow(
        "Stay unlocked until phone is locked",
        currentBehavior == 1
    ) { checked ->

        if (checked) {

            SettingsManager.setUnlockBehavior(
                this,
                1
            )
        }
    }

unlockBehaviorCard.addView(
    untilPhoneLockedRow
)

mainLayout.addView(
    unlockBehaviorCard
)

        // Option 3: Disguise as Calculator Mode
        val optStealth = createSettingRow("Disguise Icon as Calculator", false) { isChecked ->
            if (isChecked) {
                DisguiseHelper.switchIcon(this, "calculator")
            } else {
                DisguiseHelper.switchIcon(this, "normal")
            }
        }
        privacyCard.addView(optStealth)
        privacyCard.addView(createDivider())

        // Option 4: Uninstall Protection (Device Administrator)
        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
        val optUninstall = createSettingRow("Uninstall Protection", isAdminActive) { isChecked ->
            if (isChecked) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable Uninstall Protection to prevent unauthorized uninstallation of Farooqui App Lock.")
                }
                startActivity(intent)
            } else {
                devicePolicyManager.removeActiveAdmin(componentName)
            }
        }
        privacyCard.addView(optUninstall)

        mainLayout.addView(privacyCard)

        // --- ABOUT SECTION GROUP (Yahan add kiya hai) ---
        val aboutSectionTitle = TextView(this).apply {
            text = "INFORMATION"
            textSize = 13f
            setTextColor(Color.parseColor("#6D6D72"))
            setPadding(16, 32, 16, 8)
        }
        mainLayout.addView(aboutSectionTitle)

        val aboutCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.settings_card_bg)
            setPadding(24, 16, 24, 16)
        }

        val aboutRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener {
                startActivity(Intent(this@SettingsActivity, AboutActivity::class.java))
            }
        }

        val aboutLabel = TextView(this).apply {
            text = "About & Version History"
            textSize = 16f
            setTextColor(Color.parseColor("#000000"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        aboutRow.addView(aboutLabel)

        val arrowView = TextView(this).apply {
            text = "›"
            textSize = 22f
            setTextColor(Color.parseColor("#C6C6C8"))
        }
        aboutRow.addView(arrowView)

        aboutCard.addView(aboutRow)
        mainLayout.addView(aboutCard)

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }

    private fun createSettingRow(title: String, initialChecked: Boolean, onToggle: (Boolean) -> Unit): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            gravity = Gravity.CENTER_VERTICAL
        }

        val label = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(Color.parseColor("#000000"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(label)

        val switchView = SwitchCompat(this).apply {
            isChecked = initialChecked
            setOnCheckedChangeListener { _, isChecked ->
                onToggle(isChecked)
            }
        }
        row.addView(switchView)

        return row
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                setMargins(0, 8, 0, 8)
            }
            setBackgroundColor(Color.parseColor("#C6C6C8"))
        }
    }
}
