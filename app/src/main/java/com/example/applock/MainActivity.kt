package com.example.applock

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridView

    private var authenticated = false

    private var authenticating = false

    private var shouldAuthenticate = true

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setupModernUI()
    }

    override fun onStart() {

        super.onStart()

        if (shouldAuthenticate && !authenticated) {
            authenticateForAppLock()
        }
    }

    override fun onStop() {

        super.onStop()

        // Jab AppLock foreground se bahar chala jaye,
        // next time open hone par authentication maango.
        if (!isChangingConfigurations) {
            authenticated = false
            shouldAuthenticate = true
        }
    }

    private fun authenticateForAppLock() {

        if (authenticating) {
            return
        }

        authenticating = true

        val biometricManager =
            BiometricManager.from(this)

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate =
            biometricManager.canAuthenticate(
                authenticators
            )

        if (
            canAuthenticate !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {

            authenticating = false

            showSecuritySetupDialog()

            return
        }

        val executor: Executor =
            ContextCompat.getMainExecutor(
                this
            )

        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun
                        onAuthenticationSucceeded(
                        result:
                        BiometricPrompt.AuthenticationResult
                    ) {

                        super
                            .onAuthenticationSucceeded(
                                result
                            )

                        authenticating = false
                        authenticated = true
                        shouldAuthenticate = false
                    }

                    override fun
                        onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super
                            .onAuthenticationError(
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

                    override fun
                        onAuthenticationFailed() {

                        super
                            .onAuthenticationFailed()

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
                .setTitle(
                    "Farooqui App Lock"
                )
                .setSubtitle(
                    "Unlock App Lock"
                )
                .setAllowedAuthenticators(
                    authenticators
                )
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }

    private fun showSecuritySetupDialog() {

        AlertDialog.Builder(this)
            .setTitle(
                "Screen Lock Required"
            )
            .setMessage(
                "Please set a fingerprint, face unlock, PIN, password or pattern on your phone before using Farooqui App Lock."
            )
            .setPositiveButton(
                "Open Security Settings"
            ) { _, _ ->

                try {

                    startActivity(
                        Intent(
                            Settings.ACTION_SECURITY_SETTINGS
                        )
                    )

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        "Unable to open security settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                "Close"
            ) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupModernUI() {

        val mainLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setBackgroundColor(
                    Color.parseColor("#F2F2F7")
                )
            }

        val headerLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    32,
                    48,
                    32,
                    24
                )

                gravity =
                    Gravity.CENTER_VERTICAL

                setBackgroundColor(
                    Color.WHITE
                )
            }

        val titleContainer =
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

                text =
                    "Farooqui App Lock"

                textSize =
                    22f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.BLACK
                )
            }

        titleContainer.addView(
            titleView
        )

        val statusView =
            TextView(this).apply {

                text =
                    "🛡 Protection Active"

                textSize =
                    12f

                setTextColor(
                    Color.parseColor("#34C759")
                )

                setPadding(
                    0,
                    4,
                    0,
                    0
                )
            }

        titleContainer.addView(
            statusView
        )

        headerLayout.addView(
            titleContainer
        )

        val btnSettings =
            ImageView(this).apply {

                setImageResource(
                    R.drawable.ic_settings_lock
                )

                setPadding(
                    12,
                    12,
                    12,
                    12
                )

                setOnClickListener {

                    startActivity(
                        Intent(
                            context,
                            SettingsActivity::class.java
                        )
                    )
                }
            }

        headerLayout.addView(
            btnSettings
        )

        mainLayout.addView(
            headerLayout
        )

        val subTitle =
            TextView(this).apply {

                text =
                    "Select Apps to Lock"

                textSize =
                    15f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#3A3A3C")
                )

                setPadding(
                    32,
                    24,
                    32,
                    12
                )
            }

        mainLayout.addView(
            subTitle
        )

        gridView =
            GridView(this).apply {

                numColumns =
                    2

                horizontalSpacing =
                    16

                verticalSpacing =
                    16

                setPadding(
                    24,
                    0,
                    24,
                    24
                )
            }

        mainLayout.addView(
            gridView
        )

        setContentView(
            mainLayout
        )

        loadInstalledApps()
    }

    private fun isAccessibilityServiceEnabled():
            Boolean {

        val serviceId =
            "$packageName/${AppAccessibilityService::class.java.name}"

        val enabledServicesSetting =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
                ?: return false

        val splitter =
            TextUtils.SimpleStringSplitter(
                ':'
            )

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

        if (
            !isAccessibilityServiceEnabled()
        ) {

            showOneTimePermissionDialog()
        }
    }

    private fun showOneTimePermissionDialog() {

        AlertDialog.Builder(this)
            .setTitle(
                "Enable Protection"
            )
            .setMessage(
                "To secure your apps with Farooqui App Lock, please enable Accessibility permission in settings."
            )
            .setPositiveButton(
                "Enable Now"
            ) { _, _ ->

                startActivity(
                    Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun loadInstalledApps() {

        val pm =
            packageManager

        val packages =
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        val appList =
            mutableListOf<AppModel>()

        val lockedApps =
            AppLockPreferences
                .getLockedApps(this)

        for (app in packages) {

            if (
                app.packageName ==
                packageName
            ) {
                continue
            }

            val launchIntent =
                pm.getLaunchIntentForPackage(
                    app.packageName
                )

            if (launchIntent == null) {
                continue
            }

            val appName =
                try {

                    pm.getApplicationLabel(
                        app
                    ).toString()

                } catch (e: Exception) {

                    continue
                }

            val icon =
                try {

                    pm.getApplicationIcon(
                        app
                    )

                } catch (e: Exception) {

                    continue
                }

            appList.add(
                AppModel(
                    appName,
                    app.packageName,
                    icon,
                    lockedApps.contains(
                        app.packageName
                    )
                )
            )
        }

        appList.sortBy {
            it.appName.lowercase()
        }

        // Service ko persistent list do
        AppAccessibilityService
            .lockedAppsList =
            lockedApps.toSet()

        val adapter =
            AppGridAdapter(
                this,
                appList
            ) { app, isLocked ->

                // PERMANENTLY save toggle
                AppLockPreferences.setLocked(
                    this,
                    app.packageName,
                    isLocked
                )

                if (isLocked) {

                    Toast.makeText(
                        this,
                        "${app.appName} Locked",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    AppAccessibilityService
                        .clearSession(
                            app.packageName
                        )

                    Toast.makeText(
                        this,
                        "${app.appName} Unlocked",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                AppAccessibilityService
                    .lockedAppsList =
                    AppLockPreferences
                        .getLockedApps(this)
                        .toSet()
            }

        gridView.adapter =
            adapter
    }
}
