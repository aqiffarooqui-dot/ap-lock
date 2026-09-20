package com.example.applock

data class ReleaseItem(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val changes: List<String>
)

object ReleaseManager {
    fun getReleaseHistory(): List<ReleaseItem> {
        return listOf(
            ReleaseItem(
                versionName = "v1.0.2",
                versionCode = 3,
                releaseDate = "September 2026",
                changes = listOf(
                    "Added iOS-Style Onboarding Introduction for First-Time Open",
                    "Added Professional 'About' Screen with Developer Credits",
                    "Integrated Dynamic Version & Release Notes History"
                )
            ),
            ReleaseItem(
                versionName = "v1.0.1",
                versionCode = 2,
                releaseDate = "August 2026",
                changes = listOf(
                    "Added Intruder Selfie on failed authentication",
                    "Implemented Stealth Icon Disguise (Calculator & Notes mode)",
                    "Added Device Admin Uninstall Protection"
                )
            ),
            ReleaseItem(
                versionName = "v1.0.0",
                versionCode = 1,
                releaseDate = "July 2026",
                changes = listOf(
                    "Initial release of Farooqui App Lock",
                    "Lightning-fast Accessibility Service Monitoring",
                    "Zero-Delay Instant Overlay & Anti-Loop Cooldown Engine",
                    "iOS-Style Grid UI and Biometric + PIN Fallback"
                )
            )
        )
    }
}
