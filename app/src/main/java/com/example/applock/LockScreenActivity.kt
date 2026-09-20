package com.example.applock

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
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

    private var targetPackageName: String? = null

    private var targetAppName: String = "App"

    private lateinit var containerLayout: LinearLayout

    private lateinit var intruderCaptureHelper:
        IntruderCaptureHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots/screen recording of the lock screen.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        targetPackageName =
            intent.getStringExtra("PACKAGE_NAME")

        targetAppName =
            intent.getStringExtra("APP_NAME")
                ?: "App"

        intruderCaptureHelper =
            IntruderCaptureHelper(this)

        buildLockScreen()

        setupBackHandling()

        showBiometricPrompt()
    }

    // =====================================================
    // LOCK SCREEN UI
    // =====================================================

    private fun buildLockScreen() {

        containerLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setBackgroundColor(
                    Color.parseColor("#111318")
                )

                setPadding(
                    dp(32),
                    dp(48),
                    dp(32),
                    dp(48)
                )
            }

        setContentView(containerLayout)

        showBiometricPrompt()
    }

    private fun showBiometricPrompt() {

        containerLayout.removeAllViews()

        val shield =
            TextView(this).apply {

                text = "🛡️"

                textSize = 52f

                gravity = Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(20)
                )
            }

        containerLayout.addView(shield)

        val title =
            TextView(this).apply {

                text =
                    "$targetAppName is locked"

                textSize = 25f

                setTextColor(Color.WHITE)

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                gravity = Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(12)
                )
            }

        containerLayout.addView(title)

        val subtitle =
            TextView(this).apply {

                text =
                    "Authenticate to continue"

                textSize = 15f

                setTextColor(
                    Color.parseColor("#A1A5AE")
                )

                gravity = Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    dp(28)
                )
            }

        containerLayout.addView(subtitle)

        val btnRetry =
            Button(this).apply {

                text =
                    "Unlock $targetAppName"

                setTextColor(Color.WHITE)

                setBackgroundColor(
                    Color.parseColor("#2563EB")
                )

                setOnClickListener {
                    triggerBiometric()
                }
            }

        containerLayout.addView(
            btnRetry,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )
    }

    // =====================================================
    // BIOMETRIC
    // =====================================================

    private fun showBiometricPromptAgain() {
        triggerBiometric()
    }

    private fun triggerBiometric() {

        if (isUnlocked) {
            return
        }

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

            Toast.makeText(
                this,
                "Please set a phone screen lock or biometric first.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

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

                        super.onAuthenticationSucceeded(
                            result
                        )

                        unlockSuccess()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        /*
                         * IMPORTANT:
                         *
                         * Cancellation is NOT an intrusion.
                         *
                         * We only capture an intruder selfie
                         * from onAuthenticationFailed(), which
                         * represents an actual failed biometric
                         * authentication attempt.
                         */

                        if (!isUnlocked) {

                            when (errorCode) {

                                BiometricPrompt
                                    .ERROR_USER_CANCELED,

                                BiometricPrompt
                                    .ERROR_CANCELED,

                                BiometricPrompt
                                    .ERROR_NEGATIVE_BUTTON -> {

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

                    override fun onAuthenticationFailed() {

                        super.onAuthenticationFailed()

                        /*
                         * This callback is reached when biometric
                         * verification actually fails.
                         *
                         * This is where the optional intruder
                         * selfie is captured.
                         */

                        captureIntruderPhoto()

                        Toast.makeText(
                            this@LockScreenActivity,
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
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

    // =====================================================
    // INTRUDER SELFIE
    // =====================================================

    private fun captureIntruderPhoto() {

        if (
            !SettingsManager.isIntruderSelfieEnabled(
                this
            )
        ) {
            return
        }

        try {

            intruderCaptureHelper
                .captureIntruderPhoto()

        } catch (_: Exception) {

            /*
             * Intruder capture must never prevent
             * the normal App Lock flow.
             */
        }
    }

    // =====================================================
    // UNLOCK SUCCESS
    // =====================================================

    private fun unlockSuccess() {

        if (isUnlocked) {
            return
        }

        isUnlocked = true

        val packageName =
            targetPackageName

        if (packageName != null) {

            /*
             * Mode 0:
             * Session is cleared when the protected app
             * leaves the foreground.
             *
             * Mode 1:
             * Session remains unlocked until screen-off.
             */

            AppAccessibilityService
                .setSessionUnlocked(
                    packageName
                )
        }

        AppAccessibilityService
            .clearLockScreenActive(
                packageName
            )

        Toast.makeText(
            applicationContext,
            "$targetAppName Unlocked",
            Toast.LENGTH_SHORT
        ).show()

        launchTargetApp()
    }

    // =====================================================
    // TARGET APP
    // =====================================================

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

    // =====================================================
    // BACK HANDLING
    // =====================================================

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

        startActivity(homeIntent)

        finish()
    }

    // =====================================================
    // DESTROY
    // =====================================================

    override fun onDestroy() {

        if (!isUnlocked) {

            AppAccessibilityService
                .clearLockScreenActive(
                    targetPackageName
                )
        }

        super.onDestroy()
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
        ).toInt()
    }
}
