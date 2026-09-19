package com.example.applock

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LockScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.activity_list_item)

        showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Cancel karne par phone ki home screen par bhej dega taaki app khul na sake
                    val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                        addCategory(android.content.Intent.CATEGORY_HOME)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    finish() // Unlock successful, lock screen gayab aur WhatsApp khul jayega
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Farooqui App Lock")
            .setSubtitle("Verify Fingerprint or Face to Open")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onBackPressed() {
        // Back button dabane par bhi bypass nahi hoga, seedha home screen jayega
        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
