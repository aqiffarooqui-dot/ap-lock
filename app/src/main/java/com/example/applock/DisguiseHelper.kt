package com.example.applock

content.ComponentName
content.Context
content.pm.PackageManager

object DisguiseHelper {

    fun switchIcon(context: Context, mode: String) {
        val pm = context.packageManager
        val packageName = context.packageName

        val mainComponent = ComponentName(packageName, "$packageName.MainActivity")
        val calculatorComponent = ComponentName(packageName, "$packageName.CalculatorAlias")
        val notesComponent = ComponentName(packageName, "$packageName.NotesAlias")

        // Sabhi ko disable karo pehle
        pm.setComponentEnabledSetting(mainComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(calculatorComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(notesComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        // Selected mode ko enable karo
        when (mode) {
            "calculator" -> {
                pm.setComponentEnabledSetting(calculatorComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
            "notes" -> {
                pm.setComponentEnabledSetting(notesComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
            else -> {
                pm.setComponentEnabledSetting(mainComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
        }
    }
}
