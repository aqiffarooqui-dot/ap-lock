package com.example.applock

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object DisguiseHelper {

    fun switchIcon(context: Context, mode: String) {
        val pm = context.packageManager
        val packageName = context.packageName

        val mainComponent =
            ComponentName(
                packageName,
                "$packageName.MainActivity"
            )

        val calculatorComponent =
            ComponentName(
                packageName,
                "$packageName.CalculatorAlias"
            )

        // Normal launcher ko pehle disable karo
        pm.setComponentEnabledSetting(
            mainComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        // Calculator alias ko disable karo
        pm.setComponentEnabledSetting(
            calculatorComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        // Selected mode enable karo
        if (mode == "calculator") {
            pm.setComponentEnabledSetting(
                calculatorComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            pm.setComponentEnabledSetting(
                mainComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
