package com.example.applock

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionStatusManager {

    fun isCameraGranted(
        context: Context
    ): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isAccessibilityEnabled(
        context: Context
    ): Boolean {

        val expectedService =
            "${context.packageName}/" +
                    "${AppAccessibilityService::class.java.name}"

        return try {

            val enabledServices =
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )

            enabledServices
                ?.split(":")
                ?.any {
                    it.equals(
                        expectedService,
                        ignoreCase = true
                    )
                } == true

        } catch (_: Exception) {

            false
        }
    }

    fun isNotificationListenerEnabled(
        context: Context
    ): Boolean {

        val expectedComponent =
            ComponentName(
                context,
                NotificationPrivacyService::class.java
            )

        return try {

            val enabledPackages =
                Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                )

            enabledPackages
                ?.split(":")
                ?.mapNotNull {
                    ComponentName.unflattenFromString(it)
                }
                ?.contains(expectedComponent) == true

        } catch (_: Exception) {

            false
        }
    }

    fun isDeviceAdminEnabled(
        context: Context
    ): Boolean {

        val devicePolicyManager =
            context.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as? DevicePolicyManager
                ?: return false

        val adminComponent =
            ComponentName(
                context,
                MyDeviceAdminReceiver::class.java
            )

        return devicePolicyManager
            .isAdminActive(adminComponent)
    }

    fun isOverlayPermissionGranted(
        context: Context
    ): Boolean {

        return if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.M
        ) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(
        context: Context
    ): Boolean {

        if (
            android.os.Build.VERSION.SDK_INT <
            android.os.Build.VERSION_CODES.M
        ) {
            return true
        }

        val powerManager =
            context.getSystemService(
                Context.POWER_SERVICE
            ) as? PowerManager
                ?: return false

        return powerManager
            .isIgnoringBatteryOptimizations(
                context.packageName
            )
    }

    fun getPermissionSummary(
        context: Context
    ): String {

        val total =
            6

        var granted =
            0

        if (isCameraGranted(context)) {
            granted++
        }

        if (isAccessibilityEnabled(context)) {
            granted++
        }

        if (
            isNotificationListenerEnabled(context)
        ) {
            granted++
        }

        if (isDeviceAdminEnabled(context)) {
            granted++
        }

        if (isOverlayPermissionGranted(context)) {
            granted++
        }

        if (
            isIgnoringBatteryOptimizations(context)
        ) {
            granted++
        }

        return "$granted/$total ready"
    }

    fun openAccessibilitySettings(
        context: Context
    ) {

        try {

            context.startActivity(
                Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                ).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            )

        } catch (_: Exception) {
        }
    }

    fun openNotificationListenerSettings(
        context: Context
    ) {

        try {

            context.startActivity(
                Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                ).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            )

        } catch (_: Exception) {
        }
    }

    fun openDeviceAdminSettings(
        context: Context
    ) {

        try {

            val component =
                ComponentName(
                    context,
                    MyDeviceAdminReceiver::class.java
                )

            context.startActivity(
                Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                ).apply {

                    putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        component
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            )

        } catch (_: Exception) {
        }
    }

    fun openOverlaySettings(
        context: Context
    ) {

        try {

            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse(
                        "package:${context.packageName}"
                    )
                ).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            )

        } catch (_: Exception) {
        }
    }

    fun openBatteryOptimizationSettings(
        context: Context
    ) {

        try {

            val intent =
                if (
                    android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.M
                ) {
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse(
                            "package:${context.packageName}"
                        )
                    )
                } else {
                    Intent(
                        Settings.ACTION_BATTERY_SAVER_SETTINGS
                    )
                }

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(intent)

        } catch (_: Exception) {

            try {

                context.startActivity(
                    Intent(
                        Settings.ACTION_BATTERY_SAVER_SETTINGS
                    ).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                )

            } catch (_: Exception) {
            }
        }
    }
}
