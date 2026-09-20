package com.example.applock

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class AppsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var searchBox: EditText
    private lateinit var countView: TextView

    private val allApps = mutableListOf<AppModel>()
    private val visibleApps = mutableListOf<AppModel>()

    private var selectedFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadApps()
        setupUI()
        refreshList()
    }

    override fun onResume() {
        super.onResume()

        if (::listView.isInitialized) {
            loadApps()
            refreshList()
        }
    }

    // =====================================================
    // LOAD APPS
    // =====================================================

    private fun loadApps() {

        allApps.clear()

        val pm = packageManager

        val lockedApps =
            AppLockPreferences
                .getLockedApps(this)

        val packages =
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        for (app in packages) {

            if (app.packageName == packageName) {
                continue
            }

            val launchIntent =
                pm.getLaunchIntentForPackage(
                    app.packageName
                ) ?: continue

            val appName =
                try {
                    pm.getApplicationLabel(app).toString()
                } catch (_: Exception) {
                    continue
                }

            val icon =
                try {
                    pm.getApplicationIcon(app)
                } catch (_: Exception) {
                    continue
                }

            allApps.add(
                AppModel(
                    appName = appName,
                    packageName = app.packageName,
                    icon = icon,
                    isLocked =
                        lockedApps.contains(
                            app.packageName
                        )
                )
            )
        }

        allApps.sortBy {
            it.appName.lowercase()
        }

        AppAccessibilityService.lockedAppsList =
            lockedApps.toSet()
    }

    // =====================================================
    // UI
    // =====================================================

    private fun setupUI() {

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setBackgroundColor(
                    Color.parseColor("#F6F7FB")
                )
            }

        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(20),
                    dp(24),
                    dp(20),
                    dp(12)
                )
            }

        val title =
            TextView(this).apply {

                text = "Apps"

                textSize = 28f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#111318")
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        header.addView(title)

        val closeButton =
            TextView(this).apply {

                text = "✕"

                textSize = 20f

                gravity = Gravity.CENTER

                setTextColor(
                    Color.parseColor("#555A64")
                )

                setPadding(
                    dp(12),
                    dp(8),
                    dp(4),
                    dp(8)
                )

                setOnClickListener {
                    finish()
                }
            }

        header.addView(closeButton)

        root.addView(header)

        // -------------------------------------------------
        // SEARCH
        // -------------------------------------------------

        searchBox =
            EditText(this).apply {

                hint = "Search apps"

                textSize = 15f

                singleLine = true

                setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(12)
                )

                setBackgroundResource(
                    R.drawable.settings_card_bg
                )
            }

        root.addView(
            searchBox,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                setMargins(
                    dp(20),
                    dp(4),
                    dp(20),
                    dp(12)
                )
            }
        )

        searchBox.addTextChangedListener(
            object : TextWatcher {

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
                    refreshList()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        // -------------------------------------------------
        // FILTERS
        // -------------------------------------------------

        val filterRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    dp(16),
                    0,
                    dp(16),
                    dp(10)
                )
            }

        val filters =
            listOf(
                "All",
                "Locked",
                "Unlocked",
                "System"
            )

        for (filter in filters) {

            val filterButton =
                TextView(this).apply {

                    text = filter

                    textSize = 13f

                    gravity = Gravity.CENTER

                    setPadding(
                        dp(14),
                        dp(9),
                        dp(14),
                        dp(9)
                    )

                    setOnClickListener {

                        selectedFilter =
                            filter

                        updateFilterButtons(
                            filterRow
                        )

                        refreshList()
                    }
                }

            filterButton.tag = filter

            filterRow.addView(
                filterButton,
                LinearLayout.LayoutParams(
                    0,
                    dp(42),
                    1f
                ).apply {
                    setMargins(
                        dp(3),
                        0,
                        dp(3),
                        0
                    )
                }
            )
        }

        root.addView(filterRow)

        updateFilterButtons(filterRow)

        // -------------------------------------------------
        // COUNT + ACTIONS
        // -------------------------------------------------

        val actionRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(20),
                    dp(2),
                    dp(20),
                    dp(10)
                )
            }

        countView =
            TextView(this).apply {

                textSize = 13f

                setTextColor(
                    Color.parseColor("#737780")
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        actionRow.addView(countView)

        val lockAll =
            TextView(this).apply {

                text = "Lock All"

                textSize = 13f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#2563EB")
                )

                setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    dp(8)
                )

                setOnClickListener {
                    setAllAppsLocked(true)
                }
            }

        actionRow.addView(lockAll)

        val unlockAll =
            TextView(this).apply {

                text = "Unlock All"

                textSize = 13f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor("#555A64")
                )

                setPadding(
                    dp(8),
                    dp(8),
                    0,
                    dp(8)
                )

                setOnClickListener {
                    setAllAppsLocked(false)
                }
            }

        actionRow.addView(unlockAll)

        root.addView(actionRow)

        // -------------------------------------------------
        // LIST
        // -------------------------------------------------

        listView =
            ListView(this).apply {

                divider = null

                dividerHeight = 0

                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    dp(20)
                )

                clipToPadding = false
            }

        root.addView(
            listView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    // =====================================================
    // FILTER BUTTONS
    // =====================================================

    private fun updateFilterButtons(
        container: LinearLayout
    ) {

        for (i in 0 until container.childCount) {

            val button =
                container.getChildAt(i)
                    as TextView

            val selected =
                button.tag == selectedFilter

            if (selected) {

                button.setBackgroundResource(
                    R.drawable.filter_selected_bg
                )

                button.setTextColor(
                    Color.WHITE
                )

            } else {

                button.setBackgroundResource(
                    R.drawable.filter_unselected_bg
                )

                button.setTextColor(
                    Color.parseColor("#555A64")
                )
            }
        }
    }

    // =====================================================
    // REFRESH
    // =====================================================

    private fun refreshList() {

        if (!::listView.isInitialized) {
            return
        }

        val query =
            searchBox.text
                .toString()
                .trim()
                .lowercase()

        visibleApps.clear()

        for (app in allApps) {

            val matchesSearch =
                query.isEmpty() ||
                        app.appName
                            .lowercase()
                            .contains(query)

            if (!matchesSearch) {
                continue
            }

            val matchesFilter =
                when (selectedFilter) {

                    "Locked" ->
                        app.isLocked

                    "Unlocked" ->
                        !app.isLocked

                    "System" ->
                        isSystemApp(
                            app.packageName
                        )

                    else ->
                        true
                }

            if (matchesFilter) {
                visibleApps.add(app)
            }
        }

        countView.text =
            "${visibleApps.size} apps"

        listView.adapter =
            AppsAdapter()
    }

    // =====================================================
    // LOCK / UNLOCK ALL
    // =====================================================

    private fun setAllAppsLocked(
        locked: Boolean
    ) {

        for (app in allApps) {

            AppLockPreferences.setLocked(
                this,
                app.packageName,
                locked
            )

            app.isLocked = locked
        }

        AppAccessibilityService.lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        refreshList()

        Toast.makeText(
            this,
            if (locked) {
                "All available apps locked"
            } else {
                "All apps unlocked"
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    // =====================================================
    // SYSTEM APP
    // =====================================================

    private fun isSystemApp(
        packageName: String
    ): Boolean {

        return try {

            val info =
                packageManager
                    .getApplicationInfo(
                        packageName,
                        0
                    )

            (
                    info.flags and
                            android.content.pm.ApplicationInfo.FLAG_SYSTEM
                    ) != 0

        } catch (_: Exception) {

            false
        }
    }

    // =====================================================
    // APP ADAPTER
    // =====================================================

    private inner class AppsAdapter :
        BaseAdapter() {

        override fun getCount(): Int =
            visibleApps.size

        override fun getItem(
            position: Int
        ): Any =
            visibleApps[position]

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
                visibleApps[position]

            val card =
                LinearLayout(this@AppsActivity).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setBackgroundResource(
                        R.drawable.settings_card_bg
                    )

                    setPadding(
                        dp(16),
                        dp(14),
                        dp(16),
                        dp(14)
                    )
                }

            val icon =
                ImageView(this@AppsActivity).apply {

                    setImageDrawable(
                        app.icon
                    )

                    scaleType =
                        ImageView.ScaleType.CENTER_INSIDE
                }

            card.addView(
                icon,
                LinearLayout.LayoutParams(
                    dp(52),
                    dp(52)
                )
            )

            val textContainer =
                LinearLayout(this@AppsActivity).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    setPadding(
                        dp(14),
                        0,
                        dp(8),
                        0
                    )

                    layoutParams =
                        LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                }

            val name =
                TextView(this@AppsActivity).apply {

                    text = app.appName

                    textSize = 16f

                    setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )

                    setTextColor(
                        Color.parseColor("#111318")
                    )

                    maxLines = 1

                    ellipsize =
                        android.text.TextUtils.TruncateAt.END
                }

            textContainer.addView(name)

            val packageText =
                TextView(this@AppsActivity).apply {

                    text =
                        if (
                            isSystemApp(
                                app.packageName
                            )
                        ) {
                            "System app"
                        } else {
                            "Application"
                        }

                    textSize = 11f

                    setTextColor(
                        Color.parseColor("#858994")
                    )

                    setPadding(
                        0,
                        dp(3),
                        0,
                        0
                    )
                }

            textContainer.addView(
                packageText
            )

            card.addView(textContainer)

            val switch =
                SwitchCompat(
                    this@AppsActivity
                ).apply {

                    setOnCheckedChangeListener(
                        null
                    )

                    isChecked =
                        app.isLocked

                    setOnCheckedChangeListener {
                            _,
                            checked ->

                        if (
                            app.isLocked !=
                            checked
                        ) {

                            app.isLocked =
                                checked

                            AppLockPreferences
                                .setLocked(
                                    this@AppsActivity,
                                    app.packageName,
                                    checked
                                )

                            AppAccessibilityService
                                .lockedAppsList =
                                AppLockPreferences
                                    .getLockedApps(
                                        this@AppsActivity
                                    )
                                    .toSet()

                            Toast.makeText(
                                this@AppsActivity,
                                if (checked) {
                                    "${app.appName} Locked"
                                } else {
                                    "${app.appName} Unlocked"
                                },
                                Toast.LENGTH_SHORT
                            ).show()

                            refreshList()
                        }
                    }
                }

            card.addView(switch)

            val params =
                AbsListViewLayoutParams()

            card.layoutParams = params

            return card
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private class AbsListViewLayoutParams :
        android.widget.AbsListView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun dp(value: Int): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }
}
