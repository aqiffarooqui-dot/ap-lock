package com.example.applock

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var countText: TextView
    private lateinit var listView: ListView

    private lateinit var allFilter: TextView
    private lateinit var lockedFilter: TextView
    private lateinit var unlockedFilter: TextView
    private lateinit var systemFilter: TextView

    private lateinit var adapter: AppsAdapter

    private val allApps =
        mutableListOf<AppModel>()

    private var currentFilter =
        "all"

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        buildScreen()
        loadApps()
    }

    private fun buildScreen() {

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setBackgroundColor(
                    Color.parseColor("#F5F7FB")
                )

                setPadding(
                    24,
                    32,
                    24,
                    24
                )
            }

        val title =
            TextView(this).apply {

                text = "Protected Apps"

                textSize = 28f

                setTextColor(
                    Color.parseColor("#111827")
                )

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        val subtitle =
            TextView(this).apply {

                text =
                    "Choose which apps should require authentication."

                textSize = 14f

                setTextColor(
                    Color.parseColor("#6B7280")
                )

                setPadding(
                    0,
                    6,
                    0,
                    20
                )
            }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        searchEditText =
            EditText(this).apply {

                hint = "Search apps"

                textSize = 15f

                singleLine = true

                setPadding(
                    20,
                    0,
                    20,
                    0
                )

                background =
                    createRoundedBackground(
                        "#FFFFFF",
                        22f
                    )
            }

        root.addView(
            searchEditText,
            LinearLayout.LayoutParams(
                -1,
                52
            ).apply {
                bottomMargin = 16
            }
        )

        searchEditText.addTextChangedListener(
            SimpleTextWatcher {
                refreshList()
            }
        )

        val filters =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        allFilter =
            createFilter("All")

        lockedFilter =
            createFilter("Locked")

        unlockedFilter =
            createFilter("Unlocked")

        systemFilter =
            createFilter("System")

        filters.addView(
            allFilter,
            filterParams()
        )

        filters.addView(
            lockedFilter,
            filterParams()
        )

        filters.addView(
            unlockedFilter,
            filterParams()
        )

        filters.addView(
            systemFilter,
            filterParams()
        )

        allFilter.setOnClickListener {
            currentFilter = "all"
            updateFilterAppearance()
            refreshList()
        }

        lockedFilter.setOnClickListener {
            currentFilter = "locked"
            updateFilterAppearance()
            refreshList()
        }

        unlockedFilter.setOnClickListener {
            currentFilter = "unlocked"
            updateFilterAppearance()
            refreshList()
        }

        systemFilter.setOnClickListener {
            currentFilter = "system"
            updateFilterAppearance()
            refreshList()
        }

        root.addView(
            filters,
            LinearLayout.LayoutParams(
                -1,
                44
            ).apply {
                bottomMargin = 14
            }
        )

        val actionRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        countText =
            TextView(this).apply {

                textSize = 13f

                setTextColor(
                    Color.parseColor("#6B7280")
                )
            }

        actionRow.addView(
            countText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        val lockAll =
            TextView(this).apply {

                text = "Lock All"

                textSize = 13f

                gravity = Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    18,
                    0,
                    18,
                    0
                )

                background =
                    createRoundedBackground(
                        "#2563EB",
                        18f
                    )

                setOnClickListener {
                    setAllAppsLocked(true)
                }
            }

        actionRow.addView(
            lockAll,
            LinearLayout.LayoutParams(
                -2,
                40
            ).apply {
                marginEnd = 8
            }
        )

        val unlockAll =
            TextView(this).apply {

                text = "Unlock All"

                textSize = 13f

                gravity = Gravity.CENTER

                setTextColor(
                    Color.parseColor("#2563EB")
                )

                setPadding(
                    18,
                    0,
                    18,
                    0
                )

                background =
                    createRoundedBackground(
                        "#E8F0FE",
                        18f
                    )

                setOnClickListener {
                    setAllAppsLocked(false)
                }
            }

        actionRow.addView(
            unlockAll,
            LinearLayout.LayoutParams(
                -2,
                40
            )
        )

        root.addView(
            actionRow,
            LinearLayout.LayoutParams(
                -1,
                48
            ).apply {
                bottomMargin = 10
            }
        )

        listView =
            ListView(this).apply {

                divider = null

                dividerHeight = 0

                overScrollMode =
                    View.OVER_SCROLL_NEVER

                clipToPadding = false

                setPadding(
                    0,
                    4,
                    0,
                    24
                )
            }

        root.addView(
            listView,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        adapter =
            AppsAdapter()

        listView.adapter =
            adapter

        updateFilterAppearance()

        setContentView(root)
    }

    private fun loadApps() {

        allApps.clear()

        val pm =
            packageManager

        val installedApps =
            pm.getInstalledApplications(
                0
            )

        for (appInfo in installedApps) {

            val packageName =
                appInfo.packageName

            if (
                packageName ==
                packageNameOfThisApp()
            ) {
                continue
            }

            val launchIntent =
                pm.getLaunchIntentForPackage(
                    packageName
                )
                    ?: continue

            val appName =
                try {
                    pm.getApplicationLabel(
                        appInfo
                    ).toString()
                } catch (_: Exception) {
                    packageName
                }

            val isSystem =
                (
                    appInfo.flags and
                            android.content.pm.ApplicationInfo.FLAG_SYSTEM
                    ) != 0
                        ||
                    (
                        appInfo.flags and
                                android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                        ) != 0

            val category =
                getAppCategory(
                    packageName,
                    appName,
                    isSystem
                )

            allApps.add(
                AppModel(
                    packageName = packageName,
                    appName = appName,
                    icon = appInfo.loadIcon(pm),
                    isSystem = isSystem,
                    category = category,
                    isLocked =
                        AppLockPreferences.isLocked(
                            this,
                            packageName
                        )
                )
            )
        }

        allApps.sortBy {
            it.appName.lowercase(
                Locale.getDefault()
            )
        }

        refreshList()
    }

    private fun packageNameOfThisApp(): String {
        return packageName
    }

    private fun getAppCategory(
        packageName: String,
        appName: String,
        isSystem: Boolean
    ): String {

        if (isSystem) {
            return "System"
        }

        val value =
            (
                packageName + " " +
                        appName
                ).lowercase(
                    Locale.getDefault()
                )

        return when {

            value.contains("whatsapp") ||
                    value.contains("instagram") ||
                    value.contains("facebook") ||
                    value.contains("telegram") ||
                    value.contains("messenger") ||
                    value.contains("snapchat") ||
                    value.contains("twitter") ||
                    value.contains("x.com") ||
                    value.contains("linkedin") ->
                "Social"

            value.contains("bank") ||
                    value.contains("pay") ||
                    value.contains("finance") ||
                    value.contains("upi") ||
                    value.contains("wallet") ->
                "Finance"

            value.contains("gallery") ||
                    value.contains("photos") ||
                    value.contains("camera") ->
                "Photos"

            value.contains("game") ||
                    value.contains("play") ->
                "Games"

            else ->
                "Other"
        }
    }

    private fun refreshList() {

        if (!::adapter.isInitialized) {
            return
        }

        adapter.notifyDataSetChanged()

        val visibleCount =
            getFilteredApps().size

        val lockedCount =
            allApps.count {
                AppLockPreferences.isLocked(
                    this,
                    it.packageName
                )
            }

        countText.text =
            "$visibleCount apps • $lockedCount locked"
    }

    private fun getFilteredApps():
            List<AppModel> {

        val query =
            searchEditText.text
                ?.toString()
                ?.trim()
                ?.lowercase(
                    Locale.getDefault()
                )
                ?: ""

        return allApps.filter { app ->

            val matchesSearch =
                query.isEmpty() ||
                        app.appName
                            .lowercase(
                                Locale.getDefault()
                            )
                            .contains(query) ||
                        app.packageName
                            .lowercase(
                                Locale.getDefault()
                            )
                            .contains(query)

            val matchesFilter =
                when (currentFilter) {

                    "locked" ->
                        AppLockPreferences.isLocked(
                            this,
                            app.packageName
                        )

                    "unlocked" ->
                        !AppLockPreferences.isLocked(
                            this,
                            app.packageName
                        )

                    "system" ->
                        app.isSystem

                    else ->
                        true
                }

            matchesSearch &&
                    matchesFilter
        }
    }

    private fun setAllAppsLocked(
        locked: Boolean
    ) {

        for (app in allApps) {

            AppLockPreferences.setLocked(
                this,
                app.packageName,
                locked
            )

            if (!locked) {
                TemporaryUnlockManager
                    .clearTemporaryUnlock(
                        this,
                        app.packageName
                    )
            }
        }

        AppAccessibilityService.lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        for (app in allApps) {
            app.isLocked = locked
        }

        refreshList()

        Toast.makeText(
            this,
            if (locked)
                "All available apps locked"
            else
                "All available apps unlocked",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateFilterAppearance() {

        updateFilter(
            allFilter,
            currentFilter == "all"
        )

        updateFilter(
            lockedFilter,
            currentFilter == "locked"
        )

        updateFilter(
            unlockedFilter,
            currentFilter == "unlocked"
        )

        updateFilter(
            systemFilter,
            currentFilter == "system"
        )
    }

    private fun updateFilter(
        view: TextView,
        selected: Boolean
    ) {

        if (selected) {

            view.setTextColor(
                Color.WHITE
            )

            view.background =
                createRoundedBackground(
                    "#2563EB",
                    20f
                )

        } else {

            view.setTextColor(
                Color.parseColor("#4B5563")
            )

            view.background =
                createRoundedBackground(
                    "#E9EDF4",
                    20f
                )
        }
    }

    private fun createFilter(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = 12f

            gravity = Gravity.CENTER

            setPadding(
                12,
                0,
                12,
                0
            )
        }
    }

    private fun filterParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            -1,
            1f
        ).apply {
            marginEnd = 6
        }
    }

    private fun createRoundedBackground(
        color: String,
        radiusDp: Float
    ): GradientDrawable {

        val drawable =
            GradientDrawable()

        drawable.setColor(
            Color.parseColor(color)
        )

        drawable.cornerRadius =
            radiusDp *
                    resources.displayMetrics.density

        return drawable
    }

    private fun showTemporaryUnlockDialog(
        app: AppModel
    ) {

        if (
            !AppLockPreferences.isLocked(
                this,
                app.packageName
            )
        ) {
            Toast.makeText(
                this,
                "Lock the app first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Temporary Unlock"
            )
            .setMessage(
                "Temporarily allow ${app.appName} without authentication."
            )
            .setItems(
                arrayOf(
                    "5 minutes",
                    "15 minutes",
                    "Until phone is locked"
                )
            ) { _, which ->

                when (which) {

                    0 ->
                        applyTemporaryUnlock(
                            app,
                            5
                        )

                    1 ->
                        applyTemporaryUnlock(
                            app,
                            15
                        )

                    2 ->
                        applyTemporaryUnlock(
                            app,
                            0
                        )
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun applyTemporaryUnlock(
        app: AppModel,
        minutes: Int
    ) {

        TemporaryUnlockManager
            .setTemporaryUnlock(
                this,
                app.packageName,
                minutes
            )

        val message =
            if (minutes == 0) {
                "${app.appName} unlocked until phone is locked"
            } else {
                "${app.appName} unlocked for $minutes minutes"
            }

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()

        try {

            val intent =
                packageManager
                    .getLaunchIntentForPackage(
                        app.packageName
                    )

            if (intent != null) {

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(intent)
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open ${app.appName}",
                Toast.LENGTH_SHORT
            ).show()
        }

        refreshList()
    }

    inner class AppsAdapter :
        BaseAdapter() {

        override fun getCount(): Int =
            getFilteredApps().size

        override fun getItem(
            position: Int
        ): AppModel =
            getFilteredApps()[position]

        override fun getItemId(
            position: Int
        ): Long =
            position.toLong()

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup?
        ): View {

            val app =
                getItem(position)

            val card =
                LinearLayout(this@AppsActivity).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        18,
                        14,
                        14,
                        14
                    )

                    background =
                        createRoundedBackground(
                            "#FFFFFF",
                            20f
                        )

                    layoutParams =
                        AbsListViewParams(
                            -1,
                            78
                        ).apply {
                            setMargins(
                                0,
                                0,
                                0,
                                10
                            )
                        }
                }

            val icon =
                ImageView(
                    this@AppsActivity
                ).apply {

                    setImageDrawable(
                        app.icon
                    )
                }

            card.addView(
                icon,
                LinearLayout.LayoutParams(
                    48,
                    48
                )
            )

            val info =
                LinearLayout(
                    this@AppsActivity
                ).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        14,
                        0,
                        8,
                        0
                    )
                }

            val name =
                TextView(
                    this@AppsActivity
                ).apply {

                    text = app.appName

                    textSize = 15f

                    maxLines = 1

                    ellipsize =
                        android.text.TextUtils.TruncateAt.END

                    setTextColor(
                        Color.parseColor("#111827")
                    )

                    setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )
                }

            info.addView(
                name,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                )
            )

            val category =
                TextView(
                    this@AppsActivity
                ).apply {

                    text =
                        if (app.isSystem) {
                            "System"
                        } else {
                            app.category
                        }

                    textSize = 11f

                    setTextColor(
                        Color.parseColor("#8A94A6")
                    )

                    setPadding(
                        0,
                        3,
                        0,
                        0
                    )
                }

            info.addView(
                category,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                )
            )

            card.addView(
                info,
                LinearLayout.LayoutParams(
                    0,
                    -1,
                    1f
                )
            )

            /*
             * Temporary unlock button.
             *
             * Only visible for locked apps.
             */
            if (app.isLocked) {

                val temporaryButton =
                    TextView(
                        this@AppsActivity
                    ).apply {

                        text = "⏱"

                        textSize = 20f

                        gravity =
                            Gravity.CENTER

                        setTextColor(
                            Color.parseColor("#2563EB")
                        )

                        background =
                            createRoundedBackground(
                                "#E8F0FE",
                                18f
                            )

                        setOnClickListener {
                            showTemporaryUnlockDialog(
                                app
                            )
                        }

                        contentDescription =
                            "Temporary unlock ${app.appName}"
                    }

                card.addView(
                    temporaryButton,
                    LinearLayout.LayoutParams(
                        44,
                        44
                    ).apply {
                        marginEnd = 8
                    }
                )
            }

            val switch =
                SwitchCompat(
                    this@AppsActivity
                ).apply {

                    isChecked =
                        app.isLocked

                    setOnCheckedChangeListener(
                        null
                    )

                    setOnCheckedChangeListener {
                            _,
                            checked ->

                        AppLockPreferences
                            .setLocked(
                                this@AppsActivity,
                                app.packageName,
                                checked
                            )

                        app.isLocked =
                            checked

                        if (!checked) {

                            TemporaryUnlockManager
                                .clearTemporaryUnlock(
                                    this@AppsActivity,
                                    app.packageName
                                )
                        }

                        AppAccessibilityService
                            .lockedAppsList =
                            AppLockPreferences
                                .getLockedApps(
                                    this@AppsActivity
                                )
                                .toSet()

                        refreshList()
                    }
                }

            card.addView(
                switch,
                LinearLayout.LayoutParams(
                    52,
                    48
                )
            )

            /*
             * Long press is also supported as a
             * secondary way to access temporary unlock.
             */
            card.setOnLongClickListener {

                if (app.isLocked) {

                    showTemporaryUnlockDialog(
                        app
                    )

                    true

                } else {
                    false
                }
            }

            return card
        }
    }

    private class AbsListViewParams(
        width: Int,
        height: Int
    ) : AbsListView.LayoutParams(
        width,
        height
    )

    private class SimpleTextWatcher(
        private val action: () -> Unit
    ) :
        android.text.TextWatcher {

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            action()
        }

        override fun afterTextChanged(
            s: android.text.Editable?
        ) {
        }
    }
}
