package com.example.applock

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LockScreenActivity : AppCompatActivity() {

    private var isUnlocked = false
    private var targetPackageName: String? = null
    private var failedAttempts = 0

    private lateinit var containerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetPackageName = intent.getStringExtra("PACKAGE_NAME")

        containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1C1C1E"))
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        setContentView(containerLayout)
        showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        containerLayout.removeAllViews()

        val title = TextView(this).apply {
            text = "Farooqui App Lock"
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        containerLayout.addView(title)

        val subtitle = TextView(this).apply {
            text = "Verify identity to open app"
            textSize = 14f
            setTextColor(Color.parseColor("#8E8E93"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        containerLayout.addView(subtitle)

        val btnRetry = Button(this).apply {
            text = "Tap to Unlock"
            setBackgroundColor(Color.parseColor("#007AFF"))
            setTextColor(Color.WHITE)
            setOnClickListener { triggerBiometric() }
        }
        containerLayout.addView(btnRetry)

        triggerBiometric()
    }

    private fun triggerBiometric() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (!isUnlocked) {
                        failedAttempts++
                        checkFallbackTrigger()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    failedAttempts++
                    Toast.makeText(applicationContext, "Verification Failed ($failedAttempts/3)", Toast.LENGTH_SHORT).show()
                    checkFallbackTrigger()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Farooqui App Lock")
            .setSubtitle("Use your configured Biometric")
            .setNegativeButtonText("Use PIN Fallback")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkFallbackTrigger() {
        // Agar 3 baar fail ho jaye ya user negative button dabaye, toh PIN window dikhao
        if (failedAttempts >= 3) {
            showPinFallbackScreen()
        }
    }

    private fun showPinFallbackScreen() {
        containerLayout.removeAllViews()

        val title = TextView(this).apply {
            text = "Enter PIN Fallback"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        containerLayout.addView(title)

        val pinInput = EditText(this).apply {
            hint = "Enter 4-digit PIN (default: 1234)"
            setHintTextColor(Color.parseColor("#8E8E93"))
            setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#2C2C2E"))
            setPadding(32, 32, 32, 32)
        }
        containerLayout.addView(pinInput)

        val btnSubmit = Button(this).apply {
            text = "Unlock"
            setBackgroundColor(Color.parseColor("#34C759"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (pinInput.text.toString() == "1234") {
                    unlockSuccess()
                } else {
                    Toast.makeText(context, "Incorrect PIN!", Toast.LENGTH_SHORT).show()
                }
            }
            setPadding(0, 24, 0, 24)
        }
        containerLayout.addView(btnSubmit)
    }

    private fun unlockSuccess() {
        isUnlocked = true
        AppAccessibilityService.isCurrentlyLocked = false
        targetPackageName?.let { pkg ->
            AppAccessibilityService.setSessionUnlocked(pkg)
        }
        Toast.makeText(applicationContext, "Unlocked Successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun goToHome() {
        AppAccessibilityService.isCurrentlyLocked = false
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    override fun onBackPressed() {
        if (!isUnlocked) {
            goToHome()
        } else {
            super.onBackPressed()
        }
    }
}
