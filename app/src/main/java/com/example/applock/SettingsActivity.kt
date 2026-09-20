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
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName

    private lateinit var everyTimeSwitch: SwitchCompat
    private lateinit var phoneLockedSwitch: SwitchCompat
    private lateinit var uninstallSwitch: SwitchCompat
    private lateinit var disguiseSwitch: SwitchCompat
    private lateinit var intruderSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE)
                    as DevicePolicyManager

        componentName =
            ComponentName(
                this,
                MyDeviceAdminReceiver::class.java
            )

        buildScreen()
    }

    override fun onResume() {
        super.onResume()

        if (::uninstallSwitch.isInitialized) {
            uninstallSwitch.setOnCheckedChangeListener(null)
            uninstallSwitch.isChecked =
                devicePolicyManager.isAdminActive(componentName)

            uninstallSwitch.setOnCheckedChangeListener { _, checked ->
                handleUninstallProtection(checked)
            }
        }
    }

    private fun buildScreen() {

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(
                Color.parseColor("#F5F7FB")
            )
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                dp(28),
                dp(20),
                dp(40)
            )
        }

        val title = TextView(this).apply {
            text = "Settings"
            textSize = 30f
            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
            setTextColor(
                Color.parseColor("#111318")
            )
        }

        root.addView(
            title,
            marginParams(bottom = 4)
        )

        val subtitle = TextView(this).apply {
            text = "Configure your privacy and protection"
            textSize = 14f
            setTextColor(
                Color.parseColor("#737780")
            )
        }

        root.addView(
            subtitle,
            marginParams(bottom = 18)
        )

        // SECURITY

        root.addView(
            sectionTitle("SECURITY")
        )

        val securityCard = createCard()

        val authInfo = TextView(this).apply {
            text =
                "Farooqui App Lock uses your phone's system biometric, PIN, password or pattern. No separate App Lock PIN is stored."
            textSize = 14f
            setTextColor(
                Color.parseColor("#555A64")
            )
            setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(8)
            )
        }

        securityCard.addView(authInfo)

        root.addView(
            securityCard,
            marginParams(bottom = 8)
        )

        // UNLOCK BEHAVIOR

        root.addView(
            sectionTitle("UNLOCK BEHAVIOR")
        )

        val behaviorCard = createCard()

        val currentBehavior =
            SettingsManager.getUnlockBehavior(this)

        everyTimeSwitch =
            createSwitchRow(
                "Ask biometric every time",
                "Leaving the app requires authentication again.",
                currentBehavior == 0
            )

        phoneLockedSwitch =
            createSwitchRow(
                "Stay unlocked until phone is locked",
                "Reopening from Recents stays unlocked until screen lock.",
                currentBehavior == 1
            )

        everyTimeSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                phoneLockedSwitch.setOnCheckedChangeListener(null)
                phoneLockedSwitch.isChecked = false

                SettingsManager.setUnlockBehavior(
                    this,
                    0
                )

                phoneLockedSwitch.setOnCheckedChangeListener(
                    phoneLockedListener()
                )
            }
        }

        phoneLockedSwitch.setOnCheckedChangeListener(
            phoneLockedListener()
        )

        behaviorCard.addView(
            rowContainer(
                "Ask biometric every time",
                "Leaving the app requires authentication again.",
                everyTimeSwitch
            )
        )

        behaviorCard.addView(createDivider())

        behaviorCard.addView(
            rowContainer(
                "Stay unlocked until phone is locked",
                "Reopening from Recents stays unlocked until screen lock.",
                phoneLockedSwitch
            )
        )

        root.addView(
            behaviorCard,
            marginParams(bottom = 8)
        )

        // PRIVACY

        root.addView(
            sectionTitle("PRIVACY & PROTECTION")
        )

        val privacyCard = createCard()

        intruderSwitch =
            createSwitchRow(
                "Intruder Selfie",
                "Capture a photo after a failed biometric attempt.",
                SettingsManager.isIntruderSelfieEnabled(this)
            )

        intruderSwitch.setOnCheckedChangeListener { _, checked ->
            SettingsManager.setIntruderSelfieEnabled(
                this,
                checked
            )
        }

        privacyCard.addView(
            rowContainer(
                "Intruder Selfie",
                "Capture a photo after a failed biometric attempt.",
                intruderSwitch
            )
        )

        privacyCard.addView(createDivider())

        disguiseSwitch =
            createSwitchRow(
                "Calculator Icon Disguise",
                "Hide the normal launcher identity behind a calculator icon.",
                false
            )

        disguiseSwitch.setOnCheckedChangeListener { _, checked ->
            DisguiseHelper.switchIcon(
                this,
                if (checked) "calculator" else "normal"
            )
        }

        privacyCard.addView(
            rowContainer(
                "Calculator Icon Disguise",
                "Hide the normal launcher identity behind a calculator icon.",
                disguiseSwitch
            )
        )

        privacyCard.addView(createDivider())

        uninstallSwitch =
            createSwitchRow(
                "Uninstall Protection",
                "Use Android Device Admin to make unauthorized removal harder.",
                devicePolicyManager.isAdminActive(componentName)
            )

        uninstallSwitch.setOnCheckedChangeListener { _, checked ->
            handleUninstallProtection(checked)
        }

        privacyCard.addView(
            rowContainer(
                "Uninstall Protection",
                "Use Android Device Admin to make unauthorized removal harder.",
                uninstallSwitch
            )
        )

        root.addView(
            privacyCard,
            marginParams(bottom = 8)
        )

        // INTRUDER CENTER

        root.addView(
            sectionTitle("SECURITY TOOLS")
        )

        val toolsCard = createCard()

        toolsCard.addView(
            createNavigationRow(
                "Intruder Gallery",
                "View and manage captured intruder photos."
            ) {
                startActivity(
                    Intent(
                        this,
                        IntruderGalleryActivity::class.java
                    )
                )
            }
        )

        toolsCard.addView(createDivider())

        toolsCard.addView(
            createNavigationRow(
                "Security Center",
                "Check App Lock protection and system permissions."
            ) {
                startActivity(
                    Intent(
                        this,
                        SecurityActivity::class.java
                    )
                )
            }
        )

        root.addView(
            toolsCard,
            marginParams(bottom = 8)
        )

        // ABOUT

        root.addView(
            sectionTitle("INFORMATION")
        )

        val aboutCard = createCard()

        aboutCard.addView(
            createNavigationRow(
                "About & Version History",
                "App information and release history."
            ) {
                startActivity(
                    Intent(
                        this,
                        AboutActivity::class.java
                    )
                )
            }
        )

        root.addView(aboutCard)

        scrollView.addView(root)

        setContentView(scrollView)
    }

    private fun phoneLockedListener():
            (CompoundButtonCompat<Boolean>)? {
        return null
    }

    private fun handlePhoneLockedChange(checked: Boolean) {
        if (!checked) return

        everyTimeSwitch.setOnCheckedChangeListener(null)
        everyTimeSwitch.isChecked = false

        SettingsManager.setUnlockBehavior(
            this,
            1
        )

        everyTimeSwitch.setOnCheckedChangeListener { _, value ->
            if (value) {
                phoneLockedSwitch.setOnCheckedChangeListener(null)
                phoneLockedSwitch.isChecked = false
                SettingsManager.setUnlockBehavior(this, 0)
                phoneLockedSwitch.setOnCheckedChangeListener { _, v ->
                    handlePhoneLockedChange(v)
                }
            }
        }
    }

    private fun handleUninstallProtection(
        checked: Boolean
    ) {
        if (checked) {

            val intent =
                Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                ).apply {

                    putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        componentName
                    )

                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Enable Device Admin protection for Farooqui App Lock."
                    )
                }

            startActivity(intent)

        } else {

            if (
                devicePolicyManager.isAdminActive(
                    componentName
                )
            ) {
                devicePolicyManager.removeActiveAdmin(
                    componentName
                )
            }
        }
    }

    private fun createCard(): MaterialCardView {

        return MaterialCardView(this).apply {
            radius = dp(20).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(Color.WHITE)

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
        }
    }

    private fun rowContainer(
        title: String,
        description: String,
        switch: SwitchCompat
    ): LinearLayout {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(16),
                dp(14),
                dp(12),
                dp(14)
            )
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
            setTextColor(
                Color.parseColor("#17191D")
            )
        }

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 12.5f
            setTextColor(
                Color.parseColor("#737780")
            )
            setPadding(
                0,
                dp(4),
                dp(8),
                0
            )
        }

        textBox.addView(titleView)
        textBox.addView(descriptionView)

        row.addView(textBox)

        row.addView(switch)

        return row
    }

    private fun createSwitchRow(
        title: String,
        description: String,
        checked: Boolean
    ): SwitchCompat {

        return SwitchCompat(this).apply {
            isChecked = checked
            contentDescription = title
        }
    }

    private fun createNavigationRow(
        title: String,
        description: String,
        action: () -> Unit
    ): LinearLayout {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(16),
                dp(16),
                dp(12),
                dp(16)
            )

            setOnClickListener {
                action()
            }
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
            setTextColor(
                Color.parseColor("#17191D")
            )
        }

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 12.5f
            setTextColor(
                Color.parseColor("#737780")
            )
            setPadding(0, dp(4), 0, 0)
        }

        box.addView(titleView)
        box.addView(descriptionView)

        row.addView(box)

        val arrow = TextView(this).apply {
            text = "›"
            textSize = 28f
            setTextColor(
                Color.parseColor("#9CA3AF")
            )
        }

        row.addView(
            arrow,
            LinearLayout.LayoutParams(
                dp(36),
                dp(48)
            )
        )

        return row
    }

    private fun sectionTitle(
        title: String
    ): TextView {

        return TextView(this).apply {
            text = title
            textSize = 12f
            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
            setTextColor(
                Color.parseColor("#6B7280")
            )
            setPadding(
                dp(8),
                dp(14),
                dp(8),
                dp(7)
            )
        }
    }

    private fun createDivider(): View {

        return View(this).apply {
            setBackgroundColor(
                Color.parseColor("#E5E7EB")
            )

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
        }
    }

    private fun marginParams(
        bottom: Int = 0
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(bottom)
        }
    }

    private fun dp(value: Int): Int =
        (
            value *
                    resources.displayMetrics.density
            ).toInt()
}
