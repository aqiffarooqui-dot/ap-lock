package com.example.applock

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val lockedAppsSet =
        mutableSetOf<String>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setupModernUI()
    }

    override fun onResume() {

        super.onResume()

        if (!isAccessibilityServiceEnabled()) {

            showOneTimePermissionDialog()
        }
    }

    private fun setupModernUI() {

        val mainLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setBackgroundColor(
                    Color.parseColor("#F2F2F7")
                )
            }

        val headerLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    32,
                    48,
                    32,
                    24
                )

                gravity =
                    Gravity.CENTER_VERTICAL

                setBackgroundColor(
                    Color.WHITE
                )
            }

        val titleContainer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        val titleView =
            TextView(this).apply {

                text =
                    "Farooqui App Lock"

                textSize = 22f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.BLACK
                )
            }

        titleContainer.addView(
            titleView
        )

        val statusView =
            TextView(this).apply {

                text =
                    "🟢 Protection Active"

                textSize = 12f

                setTextColor(
                    Color.parseColor("#34C759")
                )

                setPadding(
                    0,
                    4,
                    0,
                    0
                )
            }

        titleContainer.addView(
            statusView
        )

        headerLayout.addView(
            titleContainer
        )

        val btnSettings =
            ImageView(this).apply {

                setImageResource(
                    android.R.drawable.ic_menu_manage
                )

                setPadding(
                    12,
                    12,
                    12,
                    12
                )

                setOnClickListener {

                    startActivity(
                        Intent(
                            context,
                            SettingsActivity::class.java
                        )
                    )
                }
            }

        headerLayout.addView(
            btnSettings
        )

        mainLayout.addView(
            headerLayout
        )

        val subTitle =
            TextView(this).apply {

                text =
                    "Select Apps to Lock"

                textSize = 15f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#3A3A3C")
                )

                setPadding(
                    32,
                    24,
                    32,
                    12
                )
            }

        mainLayout.addView(
            subTitle
        )

        val gridView =
            GridView(this).apply {

                numColumns = 2

                horizontalSpacing = 16

                verticalSpacing = 16

                setPadding(
                    24,
                    0,
                    24,
                    24
                )
            }

        mainLayout.addView(
            gridView
        )

        setContentView(
            mainLayout
        )

        loadInstalledApps(
            gridView
        )
    }

    private fun isAccessibilityServiceEnabled():
            Boolean {

        val serviceId =
            "$packageName/${AppAccessibilityService::class.java.name}"

        val enabledServicesSetting =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
                ?: return false

        val splitter =
            TextUtils.SimpleStringSplitter(
                ':'
            )

        splitter.setString(
            enabledServicesSetting
        )

        while (splitter.hasNext()) {

            val componentName =
                splitter.next()

            if (
                componentName.equals(
                    serviceId,
                    ignoreCase = true
                )
            ) {

                return true
            }
        }

        return false
    }

    private fun showOneTimePermissionDialog() {

        AlertDialog.Builder(this)
            .setTitle(
                "Enable Protection"
            )
            .setMessage(
                "To secure your apps with Farooqui App Lock, please enable Accessibility permission in settings."
            )
            .setPositiveButton(
                "Enable Now"
            ) { _, _ ->

                startActivity(
                    Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun loadInstalledApps(
        gridView: GridView
    ) {

        val pm =
            packageManager

        val packages =
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        val appList =
            mutableListOf<AppModel>()

        for (app in packages) {

            // AppLock ko list me mat dikhana
            if (
                app.packageName ==
                packageName
            ) {
                continue
            }

            // System + user apps dono include honge
            val appName =
                pm.getApplicationLabel(
                    app
                ).toString()

            val icon =
                try {
                    pm.getApplicationIcon(
                        app
                    )
                } catch (e: Exception) {
                    continue
                }

            // Sirf actual launchable apps ko list karo.
            // Isse random internal system packages
            // list ko unnecessarily fill nahi karenge.
            val launchIntent =
                pm.getLaunchIntentForPackage(
                    app.packageName
                )

            if (launchIntent == null) {
                continue
            }

            appList.add(
                AppModel(
                    appName,
                    app.packageName,
                    icon,
                    lockedAppsSet.contains(
                        app.packageName
                    )
                )
            )
        }

        appList.sortBy {
            it.appName.lowercase()
        }

        val adapter =
            AppGridAdapter(
                this,
                appList
            ) { app, isLocked ->

                if (isLocked) {

                    lockedAppsSet.add(
                        app.packageName
                    )

                    Toast.makeText(
                        this,
                        "${app.appName} Locked",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    lockedAppsSet.remove(
                        app.packageName
                    )

                    AppAccessibilityService
                        .clearSession(
                            app.packageName
                        )

                    Toast.makeText(
                        this,
                        "${app.appName} Unlocked",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                AppAccessibilityService
                    .lockedAppsList =
                    lockedAppsSet.toSet()
            }

        gridView.adapter =
            adapter
    }
}
