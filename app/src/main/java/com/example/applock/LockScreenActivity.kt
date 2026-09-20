package com.example.applock

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LockScreenActivity : AppCompatActivity() {

    private var isUnlocked = false
    private var failedAttempts = 0

    private var targetPackageName: String? = null
    private var targetAppName: String = "App"

    private lateinit var containerLayout: LinearLayout
    private lateinit var unlockButton: TextView
    private lateinit var statusText: TextView
    private lateinit var intruderCaptureHelper: IntruderCaptureHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenSecurityManager.apply(this)

        targetPackageName =
            intent.getStringExtra("PACKAGE_NAME")

        targetAppName =
            intent.getStringExtra("APP_NAME")
                ?: "App"

        intruderCaptureHelper =
            IntruderCaptureHelper(this)

        buildLockScreen()
        setupBackHandling()

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        triggerBiometric()
    }

    private fun buildLockScreen() {

        containerLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setBackgroundColor(
                    Color.parseColor("#0B0D12")
                )

                setPadding(
                    dp(28),
                    dp(48),
                    dp(28),
                    dp(48)
                )
            }

        setContentView(containerLayout)

        val contentCard =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    dp(28),
                    dp(30),
                    dp(28),
                    dp(28)
                )

                background =
                    GradientDrawable().apply {

                        setColor(
                            Color.parseColor("#151821")
                        )

                        cornerRadius =
                            dp(28).toFloat()

                        setStroke(
                            dp(1),
                            Color.parseColor("#252A36")
                        )
                    }
            }

        containerLayout.addView(
            contentCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val appIcon =
            ImageView(this).apply {

                val drawable =
                    try {
                        packageManager.getApplicationIcon(
                            targetPackageName
                                ?: packageName
                        )
                    } catch (_: Exception) {
                        null
                    }

                if (drawable != null) {
                    setImageDrawable(drawable)
                } else {
                    setImageResource(
                        android.R.drawable.ic_lock_lock
                    )
                }

                contentDescription =
                    "$targetAppName icon"

                scaleType =
                    ImageView.ScaleType.CENTER_INSIDE
            }

        contentCard.addView(
            appIcon,
            LinearLayout.LayoutParams(
                dp(76),
                dp(76)
            ).apply {
                bottomMargin = dp(20)
            }
        )

        val shieldText =
            TextView(this).apply {

                text = "🔒"

                textSize = 22f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    0,
                    0,
                    0,
                    dp(10)
                )
            }

        contentCard.addView(
            shieldText
        )

        val title =
            TextView(this).apply {

                text =
                    "$targetAppName is locked"

                textSize = 24f

                setTextColor(
                    Color.WHITE
                )

                setTypeface(
                    null,
                    Typeface.BOLD
                )

                gravity =
                    Gravity.CENTER

                maxLines = 2

                setPadding(
                    0,
                    0,
                    0,
                    dp(10)
                )
            }

        contentCard.addView(
            title
        )

        val subtitle =
            TextView(this).apply {

                text =
                    "Authenticate to continue"

                textSize = 15f

                setTextColor(
                    Color.parseColor("#A7ACB8")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(8)
                )
            }

        contentCard.addView(
            subtitle
        )

        statusText =
            TextView(this).apply {

                text =
                    "Use fingerprint, face, PIN, pattern or password"

                textSize = 13f

                setTextColor(
                    Color.parseColor("#7F8797")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(24)
                )
            }

        contentCard.addView(
            statusText
        )

        unlockButton =
            TextView(this).apply {

                text =
                    "Unlock $targetAppName"

                textSize = 16f

                setTypeface(
                    null,
                    Typeface.BOLD
                )

                setTextColor(
                    Color.WHITE
                )

                gravity =
                    Gravity.CENTER

                isClickable = true
                isFocusable = true

                background =
                    GradientDrawable().apply {

                        setColor(
                            Color.parseColor("#2563EB")
                        )

                        cornerRadius =
                            dp(16).toFloat()
                    }

                setPadding(
                    dp(16),
                    dp(15),
                    dp(16),
                    dp(15)
                )

                setOnClickListener {
                    triggerBiometric()
                }
            }

        contentCard.addView(
            unlockButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        val protectedText =
            TextView(this).apply {

                text =
                    "Protected by Farooqui App Lock"

                textSize = 12f

                setTextColor(
                    Color.parseColor("#666D7A")
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(22),
                    0,
                    0
                )
            }

        contentCard.addView(
            protectedText
        )
    }

    private fun triggerBiometric() {

        if (isUnlocked) {
            return
        }

        unlockButton.isEnabled = false

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

            unlockButton.isEnabled = true

            statusText.text =
                "Set a screen lock or biometric on your phone first"

            Toast.makeText(
                this,
                "Please set a phone screen lock or biometric first.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        statusText.text =
            "Waiting for authentication…"

        val executor: Executor =
            ContextCompat.getMainExecutor(this)

        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result:
                            BiometricPrompt.AuthenticationResult
                    ) {

                        super
                            .onAuthenticationSucceeded(result)

                        unlockSuccess()
                    }

                    override fun onAuthenticationFailed() {

                        super
                            .onAuthenticationFailed()

                        failedAttempts++

                        StatsManager.recordFailedUnlock(
                            this@LockScreenActivity
                        )

                        SecurityActivityLogManager.addLog(
                            this@LockScreenActivity,
                            SecurityActivityLogManager.TYPE_FAILED_UNLOCK,
                            "Authentication failed",
                            targetAppName
                        )

                        handleFailedAuthentication()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super
                            .onAuthenticationError(
                                errorCode,
                                errString
                            )

                        if (isUnlocked) {
                            return
                        }

                        unlockButton.isEnabled = true

                        statusText.text =
                            "Authenticate to continue"

                        when (errorCode) {

                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {

                                Toast.makeText(
                                    this@LockScreenActivity,
                                    "Authentication cancelled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {

                                Toast.makeText(
                                    this@LockScreenActivity,
                                    errString,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(
                    "$targetAppName is locked"
                )
                .setSubtitle(
                    "Verify to open $targetAppName"
                )
                .setAllowedAuthenticators(
                    authenticators
                )
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }

    private fun handleFailedAuthentication() {

        unlockButton.isEnabled = true

        statusText.text =
            "Authentication failed • Try again"

        val threshold =
            AppLockRulesManager
                .getFailedAttemptLimit(this)
                .coerceIn(1, 10)

        Toast.makeText(
            this,
            "Authentication failed",
            Toast.LENGTH_SHORT
        ).show()

        if (failedAttempts < threshold) {
            return
        }

        failedAttempts = 0

        IntruderAlertManager.triggerAlert(
            this
        )

        SecurityActivityLogManager.addLog(
            this,
            SecurityActivityLogManager.TYPE_INTRUSION,
            "Intruder attempt detected",
            "$targetAppName • $threshold failed attempt(s)"
        )

        val packageName =
            targetPackageName ?: ""

        /*
         * Record exactly ONE intrusion event.
         *
         * If selfie capture is enabled, the captured
         * image is attached to that event.
         *
         * If selfie capture is disabled or capture fails,
         * the event is still recorded without a photo.
         */
        if (
            SettingsManager.isIntruderSelfieEnabled(
                this
            )
        ) {

            intruderCaptureHelper
                .captureIntruderPhoto { imageUri ->

                    runOnUiThread {

                        IntrusionManager.addIntrusion(
                            context = this,
                            packageName = packageName,
                            appName = targetAppName,
                            imageUri = imageUri?.toString()
                        )
                    }
                }

        } else {

            IntrusionManager.addIntrusion(
                context = this,
                packageName = packageName,
                appName = targetAppName,
                imageUri = null
            )
        }
    }

    private fun unlockSuccess() {

        if (isUnlocked) {
            return
        }

        isUnlocked = true

        StatsManager.recordSuccessfulUnlock(
            this
        )

        SecurityActivityLogManager.addLog(
            this,
            SecurityActivityLogManager.TYPE_UNLOCK,
            "App unlocked",
            targetAppName
        )

        val packageName =
            targetPackageName

        if (packageName != null) {

            AppAccessibilityService
                .setSessionUnlocked(
                    packageName
                )
        }

        AppAccessibilityService
            .clearLockScreenActive(
                packageName
            )

        statusText.text =
            "Access granted"

        unlockButton.isEnabled = false

        Toast.makeText(
            applicationContext,
            "$targetAppName unlocked",
            Toast.LENGTH_SHORT
        ).show()

        launchTargetApp()
    }

    private fun launchTargetApp() {

        val packageName =
            targetPackageName

        if (packageName == null) {
            finish()
            return
        }

        try {

            val launchIntent =
                packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )

            if (launchIntent != null) {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(
                    launchIntent
                )

            } else {

                Toast.makeText(
                    this,
                    "Unable to open $targetAppName",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Unable to open $targetAppName",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    private fun setupBackHandling() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (!isUnlocked) {

                        AppAccessibilityService
                            .clearLockScreenActive(
                                targetPackageName
                            )

                        goToHome()

                    } else {

                        isEnabled = false

                        onBackPressedDispatcher
                            .onBackPressed()
                    }
                }
            }
        )
    }

    private fun goToHome() {

        val homeIntent =
            Intent(Intent.ACTION_MAIN).apply {

                addCategory(
                    Intent.CATEGORY_HOME
                )

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK
            }

        startActivity(
            homeIntent
        )

        finish()
    }

    override fun onDestroy() {

        if (!isUnlocked) {

            AppAccessibilityService
                .clearLockScreenActive(
                    targetPackageName
                )
        }

        super.onDestroy()
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
