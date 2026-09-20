package com.example.applock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {

    private lateinit var everyTimeSwitch: SwitchCompat
    private lateinit var phoneLockedSwitch: SwitchCompat
    private lateinit var intruderSelfieSwitch: SwitchCompat
    private lateinit var calculatorSwitch: SwitchCompat
    private lateinit var uninstallSwitch: SwitchCompat

    private var updatingBehavior = false

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager =
            getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        adminComponent =
            ComponentName(
                this,
                MyDeviceAdminReceiver::class.java
            )

        buildSettingsScreen()
    }

    override fun onResume() {
        super.onResume()

        if (::uninstallSwitch.isInitialized) {
            uninstallSwitch.isChecked =
                devicePolicyManager.isAdminActive(
                    adminComponent
                )
        }
    }

    private fun buildSettingsScreen() {

        val scrollView =
            ScrollView(this).apply {
                setBackgroundColor(
                    Color.parseColor("#F7F8FC")
                )
            }

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(28),
                    dp(20),
                    dp(36)
                )
            }

        val title =
            TextView(this).apply {

                text = "Settings"
                textSize = 30f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#111827")
                )

                setPadding(
                    dp(4),
                    0,
                    dp(4),
                    dp(4)
                )
            }

        root.addView(title)

        val subtitle =
            TextView(this).apply {

                text =
                    "Control your privacy and protection preferences"

                textSize = 14f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                setPadding(
                    dp(4),
                    0,
                    dp(4),
                    dp(22)
                )
            }

        root.addView(subtitle)

        addSectionTitle(
            root,
            "APP LOCK"
        )

        val behaviorCard =
            createCard()

        val behaviorTitle =
            createCardTitle(
                "Unlock behavior"
            )

        behaviorCard.addView(
            behaviorTitle
        )

        val behaviorDescription =
            createDescription(
                "Choose when authentication is required."
            )

        behaviorCard.addView(
            behaviorDescription
        )

        everyTimeSwitch =
            createSwitchRow(
                behaviorCard,
                "Ask biometric every time",
                "Authenticate again when you leave the app."
            )

        phoneLockedSwitch =
            createSwitchRow(
                behaviorCard,
                "Stay unlocked until phone is locked",
                "Keep the app unlocked until the screen is locked."
            )

        val currentBehavior =
            SettingsManager.getUnlockBehavior(
                this
            )

        everyTimeSwitch.isChecked =
            currentBehavior == 0

        phoneLockedSwitch.isChecked =
            currentBehavior == 1

        everyTimeSwitch.setOnCheckedChangeListener {
                _,
                checked ->

            if (
                updatingBehavior ||
                !checked
            ) {
                return@setOnCheckedChangeListener
            }

            updatingBehavior = true

            phoneLockedSwitch.isChecked =
                false

            updatingBehavior = false

            SettingsManager.setUnlockBehavior(
                this,
                0
            )
        }

        phoneLockedSwitch.setOnCheckedChangeListener {
                _,
                checked ->

            if (
                updatingBehavior ||
                !checked
            ) {
                return@setOnCheckedChangeListener
            }

            updatingBehavior = true

            everyTimeSwitch.isChecked =
                false

            updatingBehavior = false

            SettingsManager.setUnlockBehavior(
                this,
                1
            )
        }

        root.addView(
            behaviorCard,
            cardParams()
        )

        addSectionTitle(
            root,
            "INTRUDER PROTECTION"
        )

        val intruderCard =
            createCard()

        intruderSelfieSwitch =
            createSwitchRow(
                intruderCard,
                "Intruder selfie",
                "Capture a photo after a failed authentication attempt."
            )

        intruderSelfieSwitch.isChecked =
            SettingsManager.isIntruderSelfieEnabled(
                this
            )

        intruderSelfieSwitch.setOnCheckedChangeListener {
                _,
                checked ->

            SettingsManager.setIntruderSelfieEnabled(
                this,
                checked
            )
        }

        root.addView(
            intruderCard,
            cardParams()
        )

        addSectionTitle(
            root,
            "DISGUISE"
        )

        val disguiseCard =
            createCard()

        calculatorSwitch =
            createSwitchRow(
                disguiseCard,
                "Calculator disguise",
                "Use the calculator launcher icon instead of the normal app icon."
            )

        calculatorSwitch.isChecked =
            SettingsManager.isCalculatorDisguiseEnabled(
                this
            )

        calculatorSwitch.setOnCheckedChangeListener {
                _,
                checked ->

            SettingsManager.setCalculatorDisguiseEnabled(
                this,
                checked
            )

            DisguiseHelper.switchIcon(
                this,
                if (checked) {
                    "calculator"
                } else {
                    "normal"
                }
            )
        }

        root.addView(
            disguiseCard,
            cardParams()
        )

        addSectionTitle(
            root,
            "DEVICE SECURITY"
        )

        val securityCard =
            createCard()

        uninstallSwitch =
            createSwitchRow(
                securityCard,
                "Uninstall protection",
                "Require device-admin removal before this app can be uninstalled."
            )

        uninstallSwitch.isChecked =
            devicePolicyManager.isAdminActive(
                adminComponent
            )

        uninstallSwitch.setOnCheckedChangeListener {
                _,
                checked ->

            if (checked) {

                val intent =
                    Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                    ).apply {

                        putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            adminComponent
                        )

                        putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "Enable uninstall protection for Farooqui App Lock."
                        )
                    }

                startActivity(intent)

            } else {

                if (
                    devicePolicyManager.isAdminActive(
                        adminComponent
                    )
                ) {

                    devicePolicyManager.removeActiveAdmin(
                        adminComponent
                    )
                }
            }
        }

        root.addView(
            securityCard,
            cardParams()
        )

        addSectionTitle(
            root,
            "TOOLS"
        )

        val toolsCard =
            createCard()

        addActionRow(
            toolsCard,
            "🛡️  Security Center",
            "Check protection status and required permissions."
        ) {
            startActivity(
                Intent(
                    this,
                    SecurityActivity::class.java
                )
            )
        }

        addActionRow(
            toolsCard,
            "📸  Intruder Gallery",
            "View and manage captured intruder photos."
        ) {
            startActivity(
                Intent(
                    this,
                    IntruderGalleryActivity::class.java
                )
            )
        }

        addActionRow(
            toolsCard,
            "ℹ️  About Farooqui App Lock",
            "Version information and release history."
        ) {
            startActivity(
                Intent(
                    this,
                    AboutActivity::class.java
                )
            )
        }

        root.addView(
            toolsCard,
            cardParams()
        )

        val privacyNote =
            TextView(this).apply {

                text =
                    "Privacy-first: your app-lock settings and intruder photos stay on your device."

                textSize = 12f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(12),
                    dp(24),
                    dp(12),
                    0
                )
            }

        root.addView(
            privacyNote
        )

        scrollView.addView(root)

        setContentView(scrollView)
    }

    private fun createCard(): MaterialCardView {

        return MaterialCardView(this).apply {

            radius =
                dp(20).toFloat()

            cardElevation = 0f

            strokeWidth = dp(1)

            strokeColor =
                Color.parseColor("#E5E7EB")

            setCardBackgroundColor(
                Color.WHITE
            )

            setContentPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
            )
        }
    }

    private fun createCardTitle(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = 18f

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            setTextColor(
                Color.parseColor("#111827")
            )

            setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(2)
            )
        }
    }

    private fun createDescription(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = 13f

            setTextColor(
                Color.parseColor("#6B7280")
            )

            setPadding(
                dp(4),
                dp(2),
                dp(4),
                dp(8)
            )
        }
    }

    private fun createSwitchRow(
        parent: LinearLayout,
        title: String,
        description: String
    ): SwitchCompat {

        val container =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(4),
                    dp(10),
                    dp(4),
                    dp(10)
                )
            }

        val textContainer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        val titleView =
            TextView(this).apply {

                text = title

                textSize = 15f

                setTextColor(
                    Color.parseColor("#111827")
                )
            }

        val descriptionView =
            TextView(this).apply {

                text = description

                textSize = 12f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                setPadding(
                    0,
                    dp(3),
                    0,
                    0
                )
            }

        textContainer.addView(
            titleView
        )

        textContainer.addView(
            descriptionView
        )

        val switch =
            SwitchCompat(this).apply {

                isClickable = true
            }

        container.addView(
            textContainer
        )

        container.addView(
            switch
        )

        parent.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return switch
    }

    private fun addActionRow(
        parent: LinearLayout,
        title: String,
        description: String,
        action: () -> Unit
    ) {

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(6),
                    dp(14),
                    dp(6),
                    dp(14)
                )

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    action()
                }
            }

        val titleView =
            TextView(this).apply {

                text = title

                textSize = 15f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#111827")
                )
            }

        val descriptionView =
            TextView(this).apply {

                text = description

                textSize = 12f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }

        row.addView(
            titleView
        )

        row.addView(
            descriptionView
        )

        parent.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        val section =
            TextView(this).apply {

                this.text = text

                textSize = 12f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                setPadding(
                    dp(6),
                    dp(18),
                    dp(6),
                    dp(8)
                )
            }

        parent.addView(section)
    }

    private fun cardParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {

            bottomMargin =
                dp(8)
        }
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
