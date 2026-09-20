package com.example.applock

content.Intent
graphics.Color
os.Bundle
view.Gravity
widget.ImageView
widget.LinearLayout
widget.ScrollView
widget.TextView
appcompat.app.AppCompatActivity
appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // Option 1: Biometric Only / Both
        val optBiometric = createSettingRow("Biometric Lock (Face/Fingerprint)", true) { isChecked ->
            // Future logic for biometric toggle
        }
        securityCard.addView(optBiometric)

        // Divider
        securityCard.addView(createDivider())

        // Option 2: Intruder Selfie
        val optIntruder = createSettingRow("Intruder Selfie (Capture on Fail)", false) { isChecked ->
            // Future logic for intruder selfie
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

        // Option 3: Stealth Mode / Icon disguise
        val optStealth = createSettingRow("Disguise as Calculator", false) { isChecked ->
            // Future logic for icon disguise
        }
        privacyCard.addView(optStealth)

        // Divider
        privacyCard.addView(createDivider())

        // Option 4: Uninstall Protection
        val optUninstall = createSettingRow("Uninstall Protection", true) { isChecked ->
            // Future logic for device admin
        }
        privacyCard.addView(optUninstall)

        mainLayout.addView(privacyCard)

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
