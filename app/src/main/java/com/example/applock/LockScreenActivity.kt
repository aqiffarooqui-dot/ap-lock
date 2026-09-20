package com.example.applock

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LockScreenActivity : AppCompatActivity() {

    private var isUnlocked = false

    private var targetPackageName: String? = null

    private var targetAppName: String = "App"

    private lateinit var containerLayout:
        LinearLayout

    private lateinit var intruderCaptureHelper:
        IntruderCaptureHelper

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        targetPackageName =
            intent.getStringExtra(
                "PACKAGE_NAME"
            )

        targetAppName =
            intent.getStringExtra(
                "APP_NAME"
            ) ?: "App"

        intruderCaptureHelper =
            IntruderCaptureHelper(
                this
            )

        containerLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setBackgroundColor(
                    Color.parseColor(
                        "#1C1C1E"
                    )
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    48,
                    48,
                    48,
                    48
                )
            }

        setContentView(
            containerLayout
        )

        showBiometricPrompt()
    }

    // -----------------------------------------------------
    // LOCK SCREEN UI
    // -----------------------------------------------------

    private fun showBiometricPrompt() {

        containerLayout.removeAllViews()

        val title =
            TextView(this).apply {

                text =
                    "$targetAppName is locked"

                textSize =
                    24f

                setTextColor(
                    Color.WHITE
                )

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    16
                )
            }

        containerLayout.addView(
            title
        )

        val subtitle =
            TextView(this).apply {

                text =
                    "Use your phone's biometric or screen-lock credential"

                textSize =
                    14f

                setTextColor(
                    Color.parseColor(
                        "#8E8E93"
                    )
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    32
                )
            }

        containerLayout.addView(
            subtitle
        )

        val btnRetry =
            Button(this).apply {

                text =
                    "Unlock $targetAppName"

                setBackgroundColor(
                    Color.parseColor(
                        "#007AFF"
                    )
                )

                setTextColor(
                    Color.WHITE
                )

                setOnClickListener {

                    triggerBiometric()
                }
            }

        containerLayout.addView(
            btnRetry
        )

        triggerBiometric()
    }

    // -----------------------------------------------------
    // BIOMETRIC
    // -----------------------------------------------------

    private fun triggerBiometric() {

        val biometricManager =
            BiometricManager.from(
                this
            )

        val authenticators =
            BiometricManager
                .Authenticators
                .BIOMETRIC_STRONG or
                    BiometricManager
                        .Authenticators
                        .DEVICE_CREDENTIAL

        val canAuthenticate =
            biometricManager
                .canAuthenticate(
                    authenticators
                )

        if (
            canAuthenticate !=
            BiometricManager
                .BIOMETRIC_SUCCESS
        ) {

            Toast.makeText(
                this,
                "Please set a phone screen lock or biometric first.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val executor: Executor =
            ContextCompat
                .getMainExecutor(
                    this
                )

        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt
                        .AuthenticationCallback() {

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

                        if (!isUnlocked) {

                            captureIntruderPhoto()

                            Toast.makeText(
                                this@LockScreenActivity,
                                "Authentication cancelled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun
                        onAuthenticationSucceeded(
                        result:
                        BiometricPrompt
                            .AuthenticationResult
                    ) {

                        super
                            .onAuthenticationSucceeded(
                                result
                            )

                        unlockSuccess()
                    }

                    override fun
                        onAuthenticationFailed() {

                        super
                            .onAuthenticationFailed()

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
            BiometricPrompt.PromptInfo
                .Builder()
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

    // -----------------------------------------------------
    // INTRUDER SELFIE
    // -----------------------------------------------------

    private fun captureIntruderPhoto() {

        if (
            !SettingsManager
                .isIntruderSelfieEnabled(
                    this
                )
        ) {

            return
        }

        try {

            intruderCaptureHelper
                .captureIntruderPhoto()

        } catch (e: Exception) {

            // Selfie failure should never
            // block the normal App Lock flow.
        }
    }

    // -----------------------------------------------------
    // UNLOCK SUCCESS
    // -----------------------------------------------------

    private fun unlockSuccess() {

        isUnlocked = true

        val packageName =
            targetPackageName

        if (packageName != null) {

            /*
             * Successful authentication ke baad
             * current app ko temporarily unlocked mark karo.
             *
             * Mode 0:
             * App foreground se bahar jaate hi
             * session clear hoga.
             *
             * Mode 1:
             * Phone lock hone tak session rahega.
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

    // -----------------------------------------------------
    // LAUNCH TARGET APP
    // -----------------------------------------------------

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
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open $targetAppName",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    // -----------------------------------------------------
    // GO HOME
    // -----------------------------------------------------

    private fun goToHome() {

        val homeIntent =
            Intent(
                Intent.ACTION_MAIN
            ).apply {

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

    // -----------------------------------------------------
    // BACK BUTTON
    // -----------------------------------------------------

    override fun onBackPressed() {

        if (!isUnlocked) {

            AppAccessibilityService
                .clearLockScreenActive(
                    targetPackageName
                )

            goToHome()

        } else {

            super.onBackPressed()
        }
    }

    // -----------------------------------------------------
    // DESTROY
    // -----------------------------------------------------

    override fun onDestroy() {

        AppAccessibilityService
            .clearLockScreenActive(
                targetPackageName
            )

        super.onDestroy()
    }
}
