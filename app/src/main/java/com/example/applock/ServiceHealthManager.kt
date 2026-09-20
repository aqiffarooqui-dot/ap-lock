package com.example.applock

import android.content.Context

data class ServiceHealth(
    val accessibilityRunning: Boolean,
    val notificationListenerRunning: Boolean,
    val cameraReady: Boolean,
    val deviceAdminEnabled: Boolean,
    val overlayReady: Boolean,
    val batteryOptimizationIgnored: Boolean
) {
    val healthyCount: Int
        get() {
            var count = 0

            if (accessibilityRunning) count++
            if (notificationListenerRunning) count++
            if (cameraReady) count++
            if (deviceAdminEnabled) count++
            if (overlayReady) count++
            if (batteryOptimizationIgnored) count++

            return count
        }

    val totalCount: Int
        get() = 6

    val isHealthy: Boolean
        get() = accessibilityRunning
}

object ServiceHealthManager {

    fun getHealth(
        context: Context
    ): ServiceHealth {

        return ServiceHealth(
            accessibilityRunning =
                PermissionStatusManager
                    .isAccessibilityEnabled(
                        context
                    ),

            notificationListenerRunning =
                PermissionStatusManager
                    .isNotificationListenerEnabled(
                        context
                    ),

            cameraReady =
                PermissionStatusManager
                    .isCameraGranted(
                        context
                    ),

            deviceAdminEnabled =
                PermissionStatusManager
                    .isDeviceAdminEnabled(
                        context
                    ),

            overlayReady =
                PermissionStatusManager
                    .isOverlayPermissionGranted(
                        context
                    ),

            batteryOptimizationIgnored =
                PermissionStatusManager
                    .isIgnoringBatteryOptimizations(
                        context
                    )
        )
    }

    fun isProtectionReady(
        context: Context
    ): Boolean {

        return PermissionStatusManager
            .isAccessibilityEnabled(
                context
            )
    }

    fun getHealthSummary(
        context: Context
    ): String {

        val health =
            getHealth(context)

        return "${health.healthyCount}/${health.totalCount} ready"
    }

    fun getProtectionStatus(
        context: Context
    ): String {

        return if (
            isProtectionReady(context)
        ) {
            "Protection Active"
        } else {
            "Action Required"
        }
    }
}
