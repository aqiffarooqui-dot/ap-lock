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

    private lateinit var containerLayout: LinearLayout

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

    private fun triggerBiometric() {

        val biometricManager =
            BiometricManager.from(
                this
            )

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
                        onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        if (!isUnlocked) {

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
                        BiometricPrompt.AuthenticationResult
                    ) {

                        super.onAuthenticationSucceeded(
                            result
                        )

                        unlockSuccess()
                    }

                    override fun
                        onAuthenticationFailed() {

                        super.onAuthenticationFailed()

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

    private fun unlockSuccess() {

        isUnlocked = true

        val packageName =
            targetPackageName

        if (packageName != null) {

            /*
             * IMPORTANT:
             *
             * Current launch ko authenticated mark
             * karna zaroori hai even when mode = 0.
             *
             * Isse successful biometric ke turant baad
             * same app ke repeated accessibility events
             * dobara biometric screen nahi kholenge.
             *
             * Mode 0 me service app foreground se bahar
             * jaate hi session clear karegi.
             *
             * Mode 1 me phone lock hone tak session rahega.
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

    override fun onDestroy() {

        AppAccessibilityService
            .clearLockScreenActive(
                targetPackageName
            )

        super.onDestroy()
    }
}
