package com.example.applock

import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    private lateinit var everyTimeSwitch: SwitchCompat
    private lateinit var phoneLockedSwitch: SwitchCompat
    private lateinit var intruderSelfieSwitch: SwitchCompat
    private lateinit var calculatorSwitch: SwitchCompat
    private lateinit var uninstallSwitch: SwitchCompat
    private lateinit var notificationPrivacySwitch: SwitchCompat
    private lateinit var scheduleSwitch: SwitchCompat

    private lateinit var intruderAlertSwitch: SwitchCompat
    private lateinit var intruderSoundSwitch: SwitchCompat
    private lateinit var intruderVibrationSwitch: SwitchCompat

    private lateinit var autoLockNewAppsSwitch: SwitchCompat
    private lateinit var systemAppsSwitch: SwitchCompat
    private lateinit var recentAppsSwitch: SwitchCompat
    private lateinit var lockOnRestartSwitch: SwitchCompat

    private lateinit var trustedUnlockSwitch: SwitchCompat

    private lateinit var scheduleTimeView: TextView
    private lateinit var scheduleStatusView: TextView
    private lateinit var themeStatusView: TextView
    private lateinit var trustedStatusView: TextView

    private var updatingBehavior = false
    private var updatingNotificationPrivacy = false
    private var updatingSchedule = false

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private val backgroundLight = Color.parseColor("#F5F6FA")
    private val surfaceLight = Color.WHITE
    private val primary = Color.parseColor("#2563EB")
    private val titleColor = Color.parseColor("#17181C")
    private val secondaryText = Color.parseColor("#737780")
    private val dividerColor = Color.parseColor("#E8E9ED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.applySavedTheme(this)

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
                devicePolicyManager.isAdminActive(adminComponent)
        }

        if (::notificationPrivacySwitch.isInitialized) {
            updateNotificationPrivacyState()
        }

        if (::scheduleSwitch.isInitialized) {
            updateScheduleState()
        }

        if (::themeStatusView.isInitialized) {
            updateThemeStatus()
        }

        if (::trustedStatusView.isInitialized) {
            updateTrustedStatus()
        }
    }

    private fun buildSettingsScreen() {

        val scrollView =
            ScrollView(this).apply {
                setBackgroundColor(backgroundLight)
                clipToPadding = false
                isFillViewport = true
            }

        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(26),
                    dp(20),
                    dp(40)
                )
            }

        root.addView(
            TextView(this).apply {
                text = "Settings"
                textSize = 32f
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(titleColor)
                includeFontPadding = true
                setPadding(
                    dp(2),
                    0,
                    dp(2),
                    dp(3)
                )
            }
        )

        root.addView(
            TextView(this).apply {
                text =
                    "Manage your security, privacy and App Lock experience."
                textSize = 14f
                setTextColor(secondaryText)
                includeFontPadding = true
                setPadding(
                    dp(2),
                    0,
                    dp(2),
                    dp(24)
                )
            }
        )

        addSectionTitle(root, "APP LOCK")

        val behaviorCard = createCard()

        addCardHeader(
            behaviorCard,
            "Unlock behavior",
            "Choose when authentication is required."
        )

        everyTimeSwitch =
            createSwitchRow(
                behaviorCard,
                "Ask biometric every time",
                "Authenticate again when you leave the protected app."
            )

        phoneLockedSwitch =
            createSwitchRow(
                behaviorCard,
                "Stay unlocked until phone is locked",
                "Keep the app unlocked until the screen is locked."
            )

        val currentBehavior =
            SettingsManager.getUnlockBehavior(this)

        everyTimeSwitch.isChecked = currentBehavior == 0
        phoneLockedSwitch.isChecked = currentBehavior == 1

        everyTimeSwitch.setOnCheckedChangeListener { _, checked ->

            if (updatingBehavior || !checked) {
                return@setOnCheckedChangeListener
            }

            updatingBehavior = true
            phoneLockedSwitch.isChecked = false
            updatingBehavior = false

            SettingsManager.setUnlockBehavior(this, 0)
            AppAccessibilityService.clearAllSessions()

            logSetting(
                "Unlock behavior changed",
                "Authenticate every time"
            )
        }

        phoneLockedSwitch.setOnCheckedChangeListener { _, checked ->

            if (updatingBehavior || !checked) {
                return@setOnCheckedChangeListener
            }

            updatingBehavior = true
            everyTimeSwitch.isChecked = false
            updatingBehavior = false

            SettingsManager.setUnlockBehavior(this, 1)
            AppAccessibilityService.clearAllSessions()

            logSetting(
                "Unlock behavior changed",
                "Stay unlocked until phone is locked"
            )
        }

        root.addView(
            behaviorCard,
            cardParams()
        )

        val rulesCard = createCard()

        addCardHeader(
            rulesCard,
            "Protection rules",
            "Control how App Lock behaves across installed apps."
        )

        autoLockNewAppsSwitch =
            createSwitchRow(
                rulesCard,
                "Auto-lock new apps",
                "Automatically lock newly detected launchable apps."
            )

        autoLockNewAppsSwitch.isChecked =
            AppLockRulesManager.isAutoLockNewAppsEnabled(this)

        autoLockNewAppsSwitch.setOnCheckedChangeListener { _, checked ->
            AppLockRulesManager.setAutoLockNewAppsEnabled(
                this,
                checked
            )

            logSetting(
                "Auto-lock new apps",
                if (checked) "Enabled" else "Disabled"
            )
        }

        systemAppsSwitch =
            createSwitchRow(
                rulesCard,
                "Allow system app locking",
                "Allow launchable system apps to be protected."
            )

        systemAppsSwitch.isChecked =
            AppLockRulesManager.isSystemAppLockEnabled(this)

        systemAppsSwitch.setOnCheckedChangeListener { _, checked ->
            AppLockRulesManager.setSystemAppLockEnabled(
                this,
                checked
            )

            logSetting(
                "System app locking",
                if (checked) "Enabled" else "Disabled"
            )
        }

        recentAppsSwitch =
            createSwitchRow(
                rulesCard,
                "Recent apps protection",
                "Keep protected-app sessions guarded when switching through Recents."
            )

        recentAppsSwitch.isChecked =
            AppLockRulesManager.isRecentAppsProtectionEnabled(this)

        recentAppsSwitch.setOnCheckedChangeListener { _, checked ->
            AppLockRulesManager.setRecentAppsProtectionEnabled(
                this,
                checked
            )

            logSetting(
                "Recent apps protection",
                if (checked) "Enabled" else "Disabled"
            )
        }

        lockOnRestartSwitch =
            createSwitchRow(
                rulesCard,
                "Lock after app restart",
                "Require authentication again after a protected app restarts."
            )

        lockOnRestartSwitch.isChecked =
            AppLockRulesManager.isLockOnRestartEnabled(this)

        lockOnRestartSwitch.setOnCheckedChangeListener { _, checked ->
            AppLockRulesManager.setLockOnRestartEnabled(
                this,
                checked
            )

            logSetting(
                "Lock after app restart",
                if (checked) "Enabled" else "Disabled"
            )
        }

        root.addView(
            rulesCard,
            cardParams()
        )

        addSectionTitle(root, "SCHEDULED APP LOCK")

        val scheduleCard = createCard()

        addCardHeader(
            scheduleCard,
            "Scheduled protection",
            "Automatically enforce app locks during a configured time window."
        )

        scheduleSwitch =
            createSwitchRow(
                scheduleCard,
                "Enable scheduled app lock",
                "Locked apps require authentication during the active schedule."
            )

        scheduleSwitch.isChecked =
            ScheduledLockManager.isEnabled(this)

        scheduleSwitch.setOnCheckedChangeListener { _, checked ->

            if (updatingSchedule) {
                return@setOnCheckedChangeListener
            }

            ScheduledLockManager.setEnabled(
                this,
                checked
            )

            AppAccessibilityService.clearAllSessions()

            logSetting(
                "Scheduled app lock",
                if (checked) "Enabled" else "Disabled"
            )

            updateScheduleState()
        }

        val scheduleTimeRow =
            createActionRow(
                scheduleCard,
                "Protection window",
                "Choose when scheduled protection should be active."
            ) {
                showScheduleEditor()
            }

        scheduleTimeView =
            TextView(this).apply {
                text =
                    ScheduledLockManager.getScheduleText(
                        this@SettingsActivity
                    )
                textSize = 14f
                setTextColor(primary)
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
                setPadding(
                    0,
                    dp(5),
                    0,
                    0
                )
            }

        scheduleStatusView =
            TextView(this).apply {
                textSize = 12f
                setTextColor(secondaryText)
                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }

        scheduleTimeRow.addView(scheduleTimeView)
        scheduleTimeRow.addView(scheduleStatusView)

        root.addView(
            scheduleCard,
            cardParams()
        )

        addSectionTitle(root, "INTRUDER PROTECTION")

        val intruderCard = createCard()

        addCardHeader(
            intruderCard,
            "Intruder detection",
            "Record failed authentication attempts locally on this device."
        )

        intruderSelfieSwitch =
            createSwitchRow(
                intruderCard,
                "Intruder selfie",
                "Capture a front-camera photo after failed authentication."
            )

        intruderSelfieSwitch.isChecked =
            SettingsManager.isIntruderSelfieEnabled(this)

        intruderSelfieSwitch.setOnCheckedChangeListener { _, checked ->
            SettingsManager.setIntruderSelfieEnabled(
                this,
                checked
            )

            logSetting(
                "Intruder selfie",
                if (checked) "Enabled" else "Disabled"
            )
        }

        intruderAlertSwitch =
            createSwitchRow(
                intruderCard,
                "Intruder alert",
                "Play a warning sound and/or vibration after an intruder attempt."
            )

        intruderAlertSwitch.isChecked =
            IntruderAlertManager.isEnabled(this)

        intruderAlertSwitch.setOnCheckedChangeListener { _, checked ->
            IntruderAlertManager.setEnabled(
                this,
                checked
            )

            logSetting(
                "Intruder alert",
                if (checked) "Enabled" else "Disabled"
            )
        }

        intruderSoundSwitch =
            createSwitchRow(
                intruderCard,
                "Warning sound",
                "Play a short alert sound."
            )

        intruderSoundSwitch.isChecked =
            IntruderAlertManager.isSoundEnabled(this)

        intruderSoundSwitch.setOnCheckedChangeListener { _, checked ->
            IntruderAlertManager.setSoundEnabled(
                this,
                checked
            )
        }

        intruderVibrationSwitch =
            createSwitchRow(
                intruderCard,
                "Vibration",
                "Vibrate the phone during an intruder alert."
            )

        intruderVibrationSwitch.isChecked =
            IntruderAlertManager.isVibrationEnabled(this)

        intruderVibrationSwitch.setOnCheckedChangeListener { _, checked ->
            IntruderAlertManager.setVibrationEnabled(
                this,
                checked
            )
        }

        addActionRow(
            intruderCard,
            "Failed-attempt threshold",
            "Capture and alert after a selected number of failed attempts."
        ) {
            showFailedAttemptDialog()
        }

        addActionRow(
            intruderCard,
            "Intruder Gallery",
            "View and manage locally stored intruder photos."
        ) {
            startActivity(
                Intent(
                    this,
                    IntruderGalleryActivity::class.java
                )
            )
        }

        root.addView(
            intruderCard,
            cardParams()
        )

        addSectionTitle(root, "NOTIFICATION PRIVACY")

        val notificationCard = createCard()

        addCardHeader(
            notificationCard,
            "Notification privacy",
            "Hide notifications posted by apps you have locked."
        )

        notificationPrivacySwitch =
            createSwitchRow(
                notificationCard,
                "Hide locked-app notifications",
                "Requires Android Notification Access permission."
            )

        notificationPrivacySwitch.isChecked =
            SettingsManager.isNotificationPrivacyEnabled(this) &&
                isNotificationAccessGranted()

        notificationPrivacySwitch.setOnCheckedChangeListener {
                _,
                checked ->

            if (updatingNotificationPrivacy) {
                return@setOnCheckedChangeListener
            }

            if (checked) {

                if (!isNotificationAccessGranted()) {

                    updatingNotificationPrivacy = true
                    notificationPrivacySwitch.isChecked = false
                    updatingNotificationPrivacy = false

                    SettingsManager.setNotificationPrivacyEnabled(
                        this,
                        false
                    )

                    openNotificationAccessSettings()

                } else {

                    SettingsManager.setNotificationPrivacyEnabled(
                        this,
                        true
                    )

                    NotificationPrivacyService.refreshLockedApps()

                    logSetting(
                        "Notification privacy",
                        "Enabled"
                    )
                }

            } else {

                SettingsManager.setNotificationPrivacyEnabled(
                    this,
                    false
                )

                NotificationPrivacyService.refreshLockedApps()

                logSetting(
                    "Notification privacy",
                    "Disabled"
                )
            }
        }

        addActionRow(
            notificationCard,
            "Notification Access",
            "Open Android settings to grant or manage Notification Access."
        ) {
            openNotificationAccessSettings()
        }

        root.addView(
            notificationCard,
            cardParams()
        )

        addSectionTitle(root, "TRUSTED UNLOCK")

        val trustedCard = createCard()

        addCardHeader(
            trustedCard,
            "Trusted environments",
            "Configure optional trusted Wi-Fi, Bluetooth or location sources."
        )

        trustedUnlockSwitch =
            createSwitchRow(
                trustedCard,
                "Enable trusted unlock",
                "Use configured trusted environments when safely supported."
            )

        trustedUnlockSwitch.isChecked =
            TrustedUnlockManager.isEnabled(this)

        trustedUnlockSwitch.setOnCheckedChangeListener { _, checked ->

            TrustedUnlockManager.setEnabled(
                this,
                checked
            )

            logSetting(
                "Trusted unlock",
                if (checked) "Enabled" else "Disabled"
            )

            updateTrustedStatus()
        }

        trustedStatusView =
            TextView(this).apply {
                textSize = 12f
                setTextColor(secondaryText)
                setPadding(
                    dp(16),
                    dp(4),
                    dp(16),
                    dp(12)
                )
            }

        trustedCard.addView(
            trustedStatusView,
            fullWidthParams()
        )

        addActionRow(
            trustedCard,
            "Trusted Wi-Fi",
            "Store the Wi-Fi network name you want to recognize."
        ) {
            showTrustedWifiDialog()
        }

        addActionRow(
            trustedCard,
            "Trusted Bluetooth",
            "Store a trusted Bluetooth device name."
        ) {
            showTrustedBluetoothDialog()
        }

        addActionRow(
            trustedCard,
            "Trusted location",
            "Store a local label for your trusted location."
        ) {
            showTrustedLocationDialog()
        }

        root.addView(
            trustedCard,
            cardParams()
        )

        addSectionTitle(root, "DISGUISE")

        val disguiseCard = createCard()

        addCardHeader(
            disguiseCard,
            "App disguise",
            "Change how Farooqui App Lock appears on your launcher."
        )

        calculatorSwitch =
            createSwitchRow(
                disguiseCard,
                "Calculator disguise",
                "Use the calculator launcher icon instead of the normal app icon."
            )

        calculatorSwitch.isChecked =
            SettingsManager.isCalculatorDisguiseEnabled(this)

        calculatorSwitch.setOnCheckedChangeListener { _, checked ->

            SettingsManager.setCalculatorDisguiseEnabled(
                this,
                checked
            )

            DisguiseHelper.switchIcon(
                this,
                if (checked) "calculator" else "normal"
            )

            if (checked) {
                DecoyManager.enableCalculator(this)
            } else {
                DecoyManager.disable(this)
            }

            logSetting(
                "Calculator disguise",
                if (checked) "Enabled" else "Disabled"
            )
        }

        addActionRow(
            disguiseCard,
            "Decoy mode",
            "Choose the configured disguise behavior."
        ) {
            showDecoyDialog()
        }

        root.addView(
            disguiseCard,
            cardParams()
        )

        addSectionTitle(root, "APPEARANCE")

        val appearanceCard = createCard()

        addCardHeader(
            appearanceCard,
            "Theme",
            "Choose how Farooqui App Lock looks."
        )

        themeStatusView =
            TextView(this).apply {
                textSize = 13f
                setTextColor(secondaryText)
                setPadding(
                    dp(16),
                    0,
                    dp(16),
                    dp(12)
                )
            }

        appearanceCard.addView(
            themeStatusView,
            fullWidthParams()
        )

        addActionRow(
            appearanceCard,
            "Light",
            "Use the light appearance."
        ) {
            changeTheme(ThemeManager.THEME_LIGHT)
        }

        addActionRow(
            appearanceCard,
            "Dark",
            "Use the dark appearance."
        ) {
            changeTheme(ThemeManager.THEME_DARK)
        }

        addActionRow(
            appearanceCard,
            "System default",
            "Follow your Android system theme."
        ) {
            changeTheme(ThemeManager.THEME_SYSTEM)
        }

        root.addView(
            appearanceCard,
            cardParams()
        )

        addSectionTitle(root, "DEVICE SECURITY")

        val securityCard = createCard()

        addCardHeader(
            securityCard,
            "Device security",
            "Protect App Lock against unauthorized changes."
        )

        uninstallSwitch =
            createSwitchRow(
                securityCard,
                "Uninstall protection",
                "Use Android Device Admin to require admin removal before uninstall."
            )

        uninstallSwitch.isChecked =
            devicePolicyManager.isAdminActive(adminComponent)

        uninstallSwitch.setOnCheckedChangeListener { _, checked ->

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

            logSetting(
                "Uninstall protection",
                if (checked) "Requested" else "Disabled"
            )
        }

        addActionRow(
            securityCard,
            "Security Center",
            "Check permissions, accessibility and service health."
        ) {
            startActivity(
                Intent(
                    this,
                    SecurityActivity::class.java
                )
            )
        }

        root.addView(
            securityCard,
            cardParams()
        )

        addSectionTitle(root, "PRIVACY & SYSTEM")

        val privacyCard = createCard()

        addActionRow(
            privacyCard,
            "Screen security",
            "Control screenshot and screen-recording protection."
        ) {
            showScreenSecurityDialog()
        }

        addActionRow(
            privacyCard,
            "Battery protection",
            "Open Android battery settings for background reliability."
        ) {
            PermissionStatusManager.openBatteryOptimizationSettings(this)
        }

        addActionRow(
            privacyCard,
            "Reset App Lock rules",
            "Restore App Lock rule preferences to their defaults."
        ) {
            confirmResetRules()
        }

        addActionRow(
            privacyCard,
            "Clear security activity",
            "Delete the local security activity timeline."
        ) {
            confirmClearActivity()
        }

        root.addView(
            privacyCard,
            cardParams()
        )

        addSectionTitle(root, "TOOLS")

        val toolsCard = createCard()

        addActionRow(
            toolsCard,
            "Security activity",
            "View recent unlock, lock and intrusion activity."
        ) {
            showActivityDialog()
        }

        addActionRow(
            toolsCard,
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

        addActionRow(
            toolsCard,
            "About Farooqui App Lock",
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

        root.addView(
            TextView(this).apply {
                text =
                    "Privacy-first • No account • No ads • No subscription • No cloud upload"
                textSize = 12f
                setTextColor(secondaryText)
                gravity = Gravity.CENTER
                setPadding(
                    dp(16),
                    dp(22),
                    dp(16),
                    0
                )
            }
        )

        scrollView.addView(root)
        setContentView(scrollView)

        updateScheduleState()
        updateThemeStatus()
        updateTrustedStatus()
    }

    private fun addCardHeader(
        card: MaterialCardView,
        title: String,
        description: String
    ) {

        val container =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(10)
                )
            }

        container.addView(
            TextView(this).apply {
                text = title
                textSize = 18f
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(titleColor)
                includeFontPadding = true
            }
        )

        container.addView(
            TextView(this).apply {
                text = description
                textSize = 13f
                setTextColor(secondaryText)
                includeFontPadding = true
                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }
        )

        card.addView(
            container,
            fullWidthParams()
        )
    }

    private fun createCard(): MaterialCardView {

        return MaterialCardView(this).apply {

            radius = dp(22).toFloat()
            cardElevation = 0f
            strokeWidth = dp(1)

            strokeColor = dividerColor

            setCardBackgroundColor(surfaceLight)

            setContentPadding(
                0,
                0,
                0,
                dp(6)
            )
        }
    }

    private fun createSwitchRow(
        parent: ViewGroup,
        title: String,
        description: String
    ): SwitchCompat {

        val container =
            LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                minimumHeight = dp(78)

                setPadding(
                    dp(16),
                    dp(10),
                    dp(14),
                    dp(10)
                )
            }

        val textContainer =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                setPadding(
                    0,
                    0,
                    dp(12),
                    0
                )
            }

        val titleView =
            TextView(this).apply {

                text = title
                textSize = 15f
                setTextColor(titleColor)
                includeFontPadding = true
                maxLines = 2
            }

        val descriptionView =
            TextView(this).apply {

                text = description
                textSize = 12f
                setTextColor(secondaryText)

                includeFontPadding = true
                maxLines = 3

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }

        textContainer.addView(
            titleView,
            fullWidthParams()
        )

        textContainer.addView(
            descriptionView,
            fullWidthParams()
        )

        val switch =
            SwitchCompat(this).apply {
                isFocusable = true
                contentDescription = title
            }

        container.addView(
            textContainer
        )

        container.addView(
            switch,
            LinearLayout.LayoutParams(
                dp(52),
                dp(48)
            )
        )

        parent.addView(
            container,
            fullWidthParams()
        )

        return switch
    }

    private fun createActionRow(
        parent: ViewGroup,
        title: String,
        description: String,
        action: () -> Unit
    ): LinearLayout {

        return LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            minimumHeight = dp(76)

            setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
            )

            isClickable = true
            isFocusable = true

            background =
                android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    cornerRadius = dp(14).toFloat()
                }

            setOnClickListener {
                action()
            }

            addView(
                TextView(
                    this@SettingsActivity
                ).apply {

                    text = title
                    textSize = 15f

                    setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )

                    setTextColor(titleColor)
                    includeFontPadding = true

                    maxLines = 2
                },
                fullWidthParams()
            )

            addView(
                TextView(
                    this@SettingsActivity
                ).apply {

                    text = description
                    textSize = 12f
                    setTextColor(secondaryText)

                    includeFontPadding = true
                    maxLines = 3

                    setPadding(
                        0,
                        dp(4),
                        0,
                        0
                    )
                },
                fullWidthParams()
            )
        }
    }

    private fun addActionRow(
        parent: ViewGroup,
        title: String,
        description: String,
        action: () -> Unit
    ) {

        parent.addView(
            createActionRow(
                parent,
                title,
                description,
                action
            ),
            fullWidthParams()
        )
    }

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        parent.addView(
            TextView(this).apply {

                this.text = text
                textSize = 12f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(secondaryText)

                letterSpacing = 0.08f

                setPadding(
                    dp(6),
                    dp(22),
                    dp(6),
                    dp(9)
                )
            }
        )
    }

    private fun cardParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        }
    }

    private fun fullWidthParams():
        ViewGroup.LayoutParams {

        return ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun updateScheduleState() {

        if (!::scheduleSwitch.isInitialized) {
            return
        }

        val enabled =
            ScheduledLockManager.isEnabled(this)

        scheduleTimeView.text =
            ScheduledLockManager.getScheduleText(this)

        scheduleStatusView.text =
            if (!enabled) {
                "Scheduled protection is turned off."
            } else if (
                ScheduledLockManager.isInsideSchedule(this)
            ) {
                "Protection is active now."
            } else {
                "Protection is currently outside the schedule."
            }

        updatingSchedule = true
        scheduleSwitch.isChecked = enabled
        updatingSchedule = false
    }

    private fun showScheduleEditor() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Protection schedule")
            .setItems(
                arrayOf(
                    "Change start time",
                    "Change end time"
                )
            ) { _, which ->

                if (which == 0) {
                    showStartTimePicker()
                } else {
                    showEndTimePicker()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStartTimePicker() {

        val dialog =
            TimePickerDialog(
                this,
                { _, hour, minute ->

                    ScheduledLockManager.setSchedule(
                        this,
                        hour,
                        minute,
                        ScheduledLockManager.getEndHour(this),
                        ScheduledLockManager.getEndMinute(this)
                    )

                    AppAccessibilityService.clearAllSessions()

                    logSetting(
                        "Schedule start",
                        String.format(
                            "%02d:%02d",
                            hour,
                            minute
                        )
                    )

                    updateScheduleState()
                },
                ScheduledLockManager.getStartHour(this),
                ScheduledLockManager.getStartMinute(this),
                false
            )

        dialog.setTitle("Schedule start time")
        dialog.show()
    }

    private fun showEndTimePicker() {

        val dialog =
            TimePickerDialog(
                this,
                { _, hour, minute ->

                    ScheduledLockManager.setSchedule(
                        this,
                        ScheduledLockManager.getStartHour(this),
                        ScheduledLockManager.getStartMinute(this),
                        hour,
                        minute
                    )

                    AppAccessibilityService.clearAllSessions()

                    logSetting(
                        "Schedule end",
                        String.format(
                            "%02d:%02d",
                            hour,
                            minute
                        )
                    )

                    updateScheduleState()
                },
                ScheduledLockManager.getEndHour(this),
                ScheduledLockManager.getEndMinute(this),
                false
            )

        dialog.setTitle("Schedule end time")
        dialog.show()
    }

    private fun showFailedAttemptDialog() {

        val current =
            AppLockRulesManager.getFailedAttemptLimit(this)

        val values =
            (1..10).map {
                if (it == 1) {
                    "After 1 failed attempt"
                } else {
                    "After $it failed attempts"
                }
            }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Failed-attempt threshold")
            .setSingleChoiceItems(
                values,
                current - 1
            ) { dialog, which ->

                val selected = which + 1

                AppLockRulesManager.setFailedAttemptLimit(
                    this,
                    selected
                )

                IntruderAlertManager.setFailedAttemptThreshold(
                    this,
                    selected
                )

                logSetting(
                    "Failed-attempt threshold",
                    selected.toString()
                )

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isNotificationAccessGranted(): Boolean {

        return PermissionStatusManager
            .isNotificationListenerEnabled(this)
    }

    private fun openNotificationAccessSettings() {

        try {
            PermissionStatusManager
                .openNotificationListenerSettings(this)
        } catch (_: Exception) {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }
    }

    private fun updateNotificationPrivacyState() {

        val enabled =
            SettingsManager
                .isNotificationPrivacyEnabled(this)

        val accessGranted =
            isNotificationAccessGranted()

        updatingNotificationPrivacy = true

        notificationPrivacySwitch.isChecked =
            enabled && accessGranted

        updatingNotificationPrivacy = false

        if (enabled && !accessGranted) {

            SettingsManager
                .setNotificationPrivacyEnabled(
                    this,
                    false
                )
        }
    }

    private fun updateTrustedStatus() {

        if (!::trustedStatusView.isInitialized) {
            return
        }

        val configured =
            TrustedUnlockManager
                .hasConfiguredTrustedSource(this)

        trustedStatusView.text =
            when {

                !TrustedUnlockManager.isEnabled(this) ->
                    "Trusted unlock is disabled."

                configured ->
                    "Trusted source configured."

                else ->
                    "No trusted source configured."
            }
    }

    private fun showTrustedWifiDialog() {

        val input =
            EditText(this).apply {

                hint = "Wi-Fi network name"
                setSingleLine(true)

                setPadding(
                    dp(20),
                    dp(12),
                    dp(20),
                    dp(12)
                )

                setText(
                    TrustedUnlockManager
                        .getTrustedWifiName(
                            this@SettingsActivity
                        )
                )
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Trusted Wi-Fi")
            .setMessage(
                "Enter the exact Wi-Fi network name."
            )
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                val name =
                    input.text.toString().trim()

                TrustedUnlockManager
                    .setTrustedWifiName(
                        this,
                        name
                    )

                TrustedUnlockManager
                    .setWifiEnabled(
                        this,
                        name.isNotBlank()
                    )

                updateTrustedStatus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTrustedBluetoothDialog() {

        val input =
            EditText(this).apply {

                hint = "Bluetooth device name"
                setSingleLine(true)

                setPadding(
                    dp(20),
                    dp(12),
                    dp(20),
                    dp(12)
                )

                setText(
                    TrustedUnlockManager
                        .getTrustedBluetoothName(
                            this@SettingsActivity
                        )
                )
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Trusted Bluetooth")
            .setMessage(
                "Enter the exact trusted Bluetooth device name."
            )
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                val name =
                    input.text.toString().trim()

                TrustedUnlockManager
                    .setTrustedBluetoothName(
                        this,
                        name
                    )

                TrustedUnlockManager
                    .setBluetoothEnabled(
                        this,
                        name.isNotBlank()
                    )

                updateTrustedStatus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTrustedLocationDialog() {

        val input =
            EditText(this).apply {

                hint = "Location label"
                setSingleLine(true)

                setPadding(
                    dp(20),
                    dp(12),
                    dp(20),
                    dp(12)
                )

                setText(
                    TrustedUnlockManager
                        .getTrustedLocationName(
                            this@SettingsActivity
                        )
                )
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Trusted location")
            .setMessage(
                "Store a label for your trusted location."
            )
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                val name =
                    input.text.toString().trim()

                TrustedUnlockManager
                    .setTrustedLocationName(
                        this,
                        name
                    )

                TrustedUnlockManager
                    .setLocationEnabled(
                        this,
                        name.isNotBlank()
                    )

                updateTrustedStatus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDecoyDialog() {

        val modes =
            arrayOf(
                "Disabled",
                "Calculator disguise",
                "Fake crash"
            )

        val currentMode =
            DecoyManager.getMode(this)

        val selected =
            when (currentMode) {

                DecoyManager.MODE_CALCULATOR ->
                    1

                DecoyManager.MODE_FAKE_CRASH ->
                    2

                else ->
                    0
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Decoy mode")
            .setSingleChoiceItems(
                modes,
                selected
            ) { dialog, which ->

                when (which) {

                    0 -> {
                        DecoyManager.disable(this)

                        SettingsManager
                            .setCalculatorDisguiseEnabled(
                                this,
                                false
                            )

                        DisguiseHelper.switchIcon(
                            this,
                            "normal"
                        )
                    }

                    1 -> {
                        DecoyManager.enableCalculator(this)

                        SettingsManager
                            .setCalculatorDisguiseEnabled(
                                this,
                                true
                            )

                        DisguiseHelper.switchIcon(
                            this,
                            "calculator"
                        )
                    }

                    2 -> {
                        DecoyManager.enableFakeCrash(this)

                        SettingsManager
                            .setCalculatorDisguiseEnabled(
                                this,
                                false
                            )

                        DisguiseHelper.switchIcon(
                            this,
                            "normal"
                        )
                    }

                    else -> Unit
                }

                logSetting(
                    "Decoy mode",
                    modes[which]
                )

                dialog.dismiss()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun updateThemeStatus() {

        if (!::themeStatusView.isInitialized) {
            return
        }

        themeStatusView.text =
            when (ThemeManager.getTheme(this)) {

                ThemeManager.THEME_LIGHT ->
                    "Current theme: Light"

                ThemeManager.THEME_DARK ->
                    "Current theme: Dark"

                else ->
                    "Current theme: System default"
            }
    }

    private fun changeTheme(theme: String) {

        ThemeManager.setTheme(
            this,
            theme
        )

        logSetting(
            "Theme",
            theme
        )

        recreate()
    }

    private fun showScreenSecurityDialog() {

        val enabled =
            ScreenSecurityManager.isEnabled(this)

        MaterialAlertDialogBuilder(this)
            .setTitle("Screen security")
            .setMessage(
                if (enabled)
                    "Screenshot and screen recording protection is currently enabled for App Lock screens."
                else
                    "Screenshot and screen recording protection is currently disabled."
            )
            .setPositiveButton(
                if (enabled) "Disable" else "Enable"
            ) { _, _ ->

                ScreenSecurityManager.setEnabled(
                    this,
                    !enabled
                )

                Toast.makeText(
                    this,
                    if (!enabled)
                        "Screen security enabled"
                    else
                        "Screen security disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun showActivityDialog() {

        val logs =
            SecurityActivityLogManager
                .getRecentLogs(
                    this,
                    20
                )

        if (logs.isEmpty()) {

            MaterialAlertDialogBuilder(this)
                .setTitle("Security Activity")
                .setMessage(
                    "No security activity recorded yet."
                )
                .setPositiveButton(
                    "Close",
                    null
                )
                .show()

            return
        }

        val text =
            buildString {

                logs.forEach { log ->

                    append(
                        formatTimestamp(
                            log.timestamp
                        )
                    )

                    append("  •  ")
                    append(log.title)

                    if (log.details.isNotBlank()) {
                        append("\n")
                        append(log.details)
                    }

                    append("\n\n")
                }
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Recent Security Activity")
            .setMessage(text)
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }

    private fun formatTimestamp(
        timestamp: Long
    ): String {

        val formatter =
            java.text.SimpleDateFormat(
                "dd MMM, hh:mm a",
                java.util.Locale.getDefault()
            )

        return formatter.format(
            java.util.Date(timestamp)
        )
    }

    private fun confirmResetRules() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Reset App Lock rules?")
            .setMessage(
                "This resets rule preferences such as auto-lock, system-app locking, recent-app protection and restart locking."
            )
            .setPositiveButton("Reset") { _, _ ->

                AppLockRulesManager
                    .resetToDefaults(this)

                Toast.makeText(
                    this,
                    "App Lock rules reset",
                    Toast.LENGTH_SHORT
                ).show()

                recreate()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun confirmClearActivity() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Clear security activity?")
            .setMessage(
                "This permanently removes the local security activity timeline."
            )
            .setPositiveButton("Clear") { _, _ ->

                SecurityActivityLogManager
                    .clearAll(this)

                Toast.makeText(
                    this,
                    "Security activity cleared",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun logSetting(
        title: String,
        details: String
    ) {

        SecurityActivityLogManager.addLog(
            this,
            SecurityActivityLogManager.TYPE_SETTING_CHANGED,
            title,
            details
        )
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
