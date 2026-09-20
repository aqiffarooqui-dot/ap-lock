package com.example.applock

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private var authenticated = false
    private var authenticating = false
    private var shouldAuthenticate = true

    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDashboard()
    }

    override fun onStart() {
        super.onStart()

        if (shouldAuthenticate && !authenticated) {
            authenticateForAppLock()
        }
    }

    override fun onStop() {
        super.onStop()

        if (!isChangingConfigurations) {
            authenticated = false
            shouldAuthenticate = true
        }
    }

    // =====================================================
    // AUTHENTICATION
    // =====================================================

    private fun authenticateForAppLock() {

        if (authenticating) {
            return
        }

        authenticating = true

        val biometricManager = BiometricManager.from(this)

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate =
            biometricManager.canAuthenticate(authenticators)

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {

            authenticating = false
            showSecuritySetupDialog()
            return
        }

        val executor: Executor =
            ContextCompat.getMainExecutor(this)

        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)

                        authenticating = false
                        authenticated = true
                        shouldAuthenticate = false

                        refreshDashboard()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        authenticating = false

                        if (!authenticated) {

                            Toast.makeText(
                                this@MainActivity,
                                "Authentication required to open App Lock",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()

                        Toast.makeText(
                            this@MainActivity,
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Farooqui App Lock")
                .setSubtitle("Unlock App Lock")
                .setAllowedAuthenticators(authenticators)
                .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showSecuritySetupDialog() {

        AlertDialog.Builder(this)
            .setTitle("Screen Lock Required")
            .setMessage(
                "Please set a fingerprint, face unlock, PIN, password or pattern on your phone before using Farooqui App Lock."
            )
            .setPositiveButton("Open Security Settings") { _, _ ->

                try {

                    startActivity(
                        Intent(
                            Settings.ACTION_SECURITY_SETTINGS
                        )
                    )

                } catch (_: Exception) {

                    Toast.makeText(
                        this,
                        "Unable to open security settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private fun setupDashboard() {

        rootLayout =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                setBackgroundColor(
                    Color.parseColor("#F6F7FB")
                )
            }

        val scrollView =
            ScrollView(this).apply {

                isFillViewport = true
            }

        val content =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(24),
                    dp(20),
                    dp(100)
                )
            }

        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        val header =
            LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL

                gravity = Gravity.CENTER_VERTICAL

                setPadding(
                    0,
                    dp(12),
                    0,
                    dp(20)
                )
            }

        val titleContainer =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        val title =
            TextView(this).apply {

                text = "Farooqui App Lock"

                textSize = 26f

                setTextColor(
                    Color.parseColor("#111318")
                )

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

        titleContainer.addView(title)

        val subtitle =
            TextView(this).apply {

                text = "Your privacy, protected."

                textSize = 14f

                setTextColor(
                    Color.parseColor("#737780")
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }

        titleContainer.addView(subtitle)

        header.addView(titleContainer)

        val settingsButton =
            TextView(this).apply {

                text = "⚙"

                textSize = 25f

                gravity = Gravity.CENTER

                setTextColor(
                    Color.parseColor("#2563EB")
                )

                setPadding(
                    dp(12),
                    dp(8),
                    dp(8),
                    dp(8)
                )

                setOnClickListener {

                    startActivity(
                        Intent(
                            this@MainActivity,
                            SettingsActivity::class.java
                        )
                    )
                }
            }

        header.addView(settingsButton)

        content.addView(header)

        // -------------------------------------------------
        // PROTECTION CARD
        // -------------------------------------------------

        val protectionCard =
            createCard()

        val protectionHeader =
            LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL

                gravity = Gravity.CENTER_VERTICAL
            }

        val shield =
            TextView(this).apply {

                text = "🛡️"

                textSize = 34f

                gravity = Gravity.CENTER
            }

        protectionHeader.addView(
            shield,
            LinearLayout.LayoutParams(
                dp(55),
                dp(55)
            )
        )

        val protectionText =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    0,
                    0,
                    0
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        val protectionTitle =
            TextView(this).apply {

                text = "Protection Active"

                textSize = 18f

                setTextColor(
                    Color.parseColor("#111318")
                )

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

        protectionText.addView(protectionTitle)

        val protectionStatus =
            TextView(this).apply {

                text =
                    if (isAccessibilityServiceEnabled()) {
                        "App Lock service is running"
                    } else {
                        "Action required: Enable Accessibility"
                    }

                textSize = 13f

                setTextColor(
                    if (isAccessibilityServiceEnabled()) {
                        Color.parseColor("#16803C")
                    } else {
                        Color.parseColor("#C2410C")
                    }
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }

        protectionText.addView(protectionStatus)

        protectionHeader.addView(protectionText)

        protectionCard.addView(protectionHeader)

        val protectionAction =
            TextView(this).apply {

                text =
                    if (isAccessibilityServiceEnabled()) {
                        "Protection is ready"
                    } else {
                        "Enable Protection →"
                    }

                textSize = 13f

                setTextColor(
                    Color.parseColor("#2563EB")
                )

                setPadding(
                    0,
                    dp(16),
                    0,
                    0
                )

                setOnClickListener {

                    if (!isAccessibilityServiceEnabled()) {

                        startActivity(
                            Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                            )
                        )
                    }
                }
            }

        protectionCard.addView(protectionAction)

        content.addView(
            protectionCard,
            marginParams(bottom = 16)
        )

        // -------------------------------------------------
        // STATISTICS
        // -------------------------------------------------

        val statsRow =
            LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL
            }

        val lockedCount =
            AppLockPreferences
                .getLockedApps(this)
                .size

        val installedCount =
            getLaunchableAppsCount()

        statsRow.addView(
            createStatCard(
                "🔒",
                lockedCount.toString(),
                "Locked Apps"
            ),
            weightParams()
        )

        statsRow.addView(
            createStatCard(
                "📱",
                installedCount.toString(),
                "Available Apps"
            ),
            weightParams()
        )

        content.addView(
            statsRow,
            marginParams(bottom = 12)
        )

        val secondStatsRow =
            LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL
            }

        secondStatsRow.addView(
            createStatCard(
                "🚨",
                "0",
                "Intrusion Attempts"
            ),
            weightParams()
        )

        secondStatsRow.addView(
            createStatCard(
                "🔐",
                "System",
                "Authentication"
            ),
            weightParams()
        )

        content.addView(
            secondStatsRow,
            marginParams(bottom = 20)
        )

        // -------------------------------------------------
        // QUICK ACTIONS
        // -------------------------------------------------

        content.addView(
            createSectionTitle("Quick Actions")
        )

        content.addView(
            createActionCard(
                "🔒",
                "Lock Apps",
                "Choose which apps should be protected"
            ) {

                startActivity(
                    Intent(
                        this,
                        AppsActivity::class.java
                    )
                )
            },
            marginParams(bottom = 12)
        )

        content.addView(
            createActionCard(
                "🛡️",
                "Security Center",
                "Check protection and device security"
            ) {

                startActivity(
                    Intent(
                        this,
                        SecurityActivity::class.java
                    )
                )
            },
            marginParams(bottom = 12)
        )

        content.addView(
            createActionCard(
                "📸",
                "Intruder Gallery",
                "View captured intrusion attempts"
            ) {

                startActivity(
                    Intent(
                        this,
                        IntruderGalleryActivity::class.java
                    )
                )
            },
            marginParams(bottom = 12)
        )

        content.addView(
            createActionCard(
                "⚙️",
                "Settings",
                "Configure App Lock and privacy"
            ) {

                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }
        )

        scrollView.addView(content)

        rootLayout.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // -------------------------------------------------
        // BOTTOM NAVIGATION
        // -------------------------------------------------

        rootLayout.addView(
            createBottomNavigation()
        )

        setContentView(rootLayout)
    }

    // =====================================================
    // BOTTOM NAVIGATION
    // =====================================================

    private fun createBottomNavigation(): LinearLayout {

        val navigation =
            LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL

                gravity = Gravity.CENTER

                setBackgroundColor(Color.WHITE)

                setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    dp(10)
                )

                elevation = dp(8).toFloat()
            }

        navigation.addView(
            createNavigationItem(
                "⌂",
                "Home",
                true
            ) {}
        )

        navigation.addView(
            createNavigationItem(
                "▣",
                "Apps",
                false
            ) {

                startActivity(
                    Intent(
                        this,
                        AppsActivity::class.java
                    )
                )
            }
        )

        navigation.addView(
            createNavigationItem(
                "🛡",
                "Security",
                false
            ) {

                startActivity(
                    Intent(
                        this,
                        SecurityActivity::class.java
                    )
                )
            }
        )

        navigation.addView(
            createNavigationItem(
                "⚙",
                "Settings",
                false
            ) {

                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }
        )

        return navigation
    }

    private fun createNavigationItem(
        icon: String,
        label: String,
        selected: Boolean,
        action: () -> Unit
    ): LinearLayout {

        val item =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                gravity = Gravity.CENTER

                setPadding(
                    dp(12),
                    dp(4),
                    dp(12),
                    dp(4)
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                setOnClickListener {
                    action()
                }
            }

        val iconView =
            TextView(this).apply {

                text = icon

                textSize = 20f

                gravity = Gravity.CENTER

                setTextColor(
                    if (selected) {
                        Color.parseColor("#2563EB")
                    } else {
                        Color.parseColor("#777B84")
                    }
                )
            }

        item.addView(iconView)

        val textView =
            TextView(this).apply {

                text = label

                textSize = 11f

                gravity = Gravity.CENTER

                setTextColor(
                    if (selected) {
                        Color.parseColor("#2563EB")
                    } else {
                        Color.parseColor("#777B84")
                    }
                )
            }

        item.addView(textView)

        return item
    }

    // =====================================================
    // UI HELPERS
    // =====================================================

    private fun createCard(): LinearLayout {

        return LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setBackgroundResource(
                R.drawable.settings_card_bg
            )

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
            )

            elevation = dp(1).toFloat()
        }
    }

    private fun createStatCard(
        icon: String,
        value: String,
        label: String
    ): LinearLayout {

        val card = createCard()

        card.setPadding(
            dp(14),
            dp(16),
            dp(14),
            dp(16)
        )

        val iconView =
            TextView(this).apply {

                text = icon

                textSize = 20f
            }

        card.addView(iconView)

        val valueView =
            TextView(this).apply {

                text = value

                textSize = 23f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#111318")
                )

                setPadding(
                    0,
                    dp(6),
                    0,
                    0
                )
            }

        card.addView(valueView)

        val labelView =
            TextView(this).apply {

                text = label

                textSize = 12f

                setTextColor(
                    Color.parseColor("#737780")
                )

                setPadding(
                    0,
                    dp(2),
                    0,
                    0
                )
            }

        card.addView(labelView)

        return card
    }

    private fun createActionCard(
        icon: String,
        title: String,
        description: String,
        action: () -> Unit
    ): LinearLayout {

        val card = createCard()

        card.orientation = LinearLayout.HORIZONTAL

        card.gravity = Gravity.CENTER_VERTICAL

        card.setOnClickListener {
            action()
        }

        val iconView =
            TextView(this).apply {

                text = icon

                textSize = 27f

                gravity = Gravity.CENTER

                layoutParams =
                    LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                    )
            }

        card.addView(iconView)

        val textContainer =
            LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    0,
                    0,
                    0
                )

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

                textSize = 16f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#111318")
                )
            }

        textContainer.addView(titleView)

        val descriptionView =
            TextView(this).apply {

                text = description

                textSize = 12f

                setTextColor(
                    Color.parseColor("#737780")
                )

                setPadding(
                    0,
                    dp(3),
                    0,
                    0
                )
            }

        textContainer.addView(descriptionView)

        card.addView(textContainer)

        val arrow =
            TextView(this).apply {

                text = "›"

                textSize = 25f

                setTextColor(
                    Color.parseColor("#A0A4AC")
                )
            }

        card.addView(arrow)

        return card
    }

    private fun createSectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = 19f

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            setTextColor(
                Color.parseColor("#111318")
            )

            setPadding(
                0,
                dp(8),
                0,
                dp(12)
            )
        }
    }

    // =====================================================
    // APP INFORMATION
    // =====================================================

    private fun getLaunchableAppsCount(): Int {

        val pm = packageManager

        return pm.getInstalledApplications(
            PackageManager.GET_META_DATA
        ).count { appInfo ->

            appInfo.packageName != packageName &&
                    pm.getLaunchIntentForPackage(
                        appInfo.packageName
                    ) != null
        }
    }

    // =====================================================
    // ACCESSIBILITY
    // =====================================================

    private fun isAccessibilityServiceEnabled(): Boolean {

        val serviceId =
            "$packageName/${AppAccessibilityService::class.java.name}"

        val enabledServicesSetting =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

        val splitter =
            TextUtils.SimpleStringSplitter(':')

        splitter.setString(
            enabledServicesSetting
        )

        while (splitter.hasNext()) {

            val componentName =
                splitter.next()

            if (
                componentName.equals(
                    serviceId,
                    ignoreCase = true
                )
            ) {

                return true
            }
        }

        return false
    }

    override fun onResume() {
        super.onResume()

        if (!isAccessibilityServiceEnabled()) {
            showOneTimePermissionDialog()
        }
    }

    private fun showOneTimePermissionDialog() {

        AlertDialog.Builder(this)
            .setTitle("Enable Protection")
            .setMessage(
                "Farooqui App Lock needs Accessibility permission to detect when a protected app is opened. This permission is processed on your device for App Lock functionality."
            )
            .setPositiveButton("Enable Now") { _, _ ->

                startActivity(
                    Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
                )
            }
            .setNegativeButton("Later", null)
            .show()
    }

    // =====================================================
    // REFRESH
    // =====================================================

    private fun refreshDashboard() {

        if (::rootLayout.isInitialized) {

            setupDashboard()
        }
    }

    // =====================================================
    // DIMENSION HELPERS
    // =====================================================

    private fun dp(value: Int): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
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

    private fun weightParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {

            setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
            )
        }
    }
}
