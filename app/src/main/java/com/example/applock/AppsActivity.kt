package com.example.applock

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
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

    private var currentFilter = "all"

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        ThemeManager.applySavedTheme(this)

        buildScreen()
        loadApps()
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            loadApps()
        }
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
                    dp(24),
                    dp(32),
                    dp(24),
                    dp(24)
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
            LinearLayout.LayoutParams(-1, -2)
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
                    dp(6),
                    0,
                    dp(20)
                )
            }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(-1, -2)
        )

        searchEditText =
            EditText(this).apply {

                hint = "Search apps"
                textSize = 15f
                setSingleLine(true)

                setPadding(
                    dp(20),
                    0,
                    dp(20),
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
                dp(52)
            ).apply {
                bottomMargin = dp(16)
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

        allFilter = createFilter("All")
        lockedFilter = createFilter("Locked")
        unlockedFilter = createFilter("Unlocked")
        systemFilter = createFilter("System")

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
                dp(44)
            ).apply {
                bottomMargin = dp(14)
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

        val groupsButton =
            createActionButton(
                "Groups",
                false
            )

        groupsButton.setOnClickListener {
            showGroupsDialog()
        }

        actionRow.addView(
            groupsButton,
            LinearLayout.LayoutParams(
                -2,
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )

        val lockAll =
            createActionButton(
                "Lock All",
                true
            )

        lockAll.setOnClickListener {
            setAllAppsLocked(true)
        }

        actionRow.addView(
            lockAll,
            LinearLayout.LayoutParams(
                -2,
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )

        val unlockAll =
            createActionButton(
                "Unlock All",
                false
            )

        unlockAll.setOnClickListener {
            setAllAppsLocked(false)
        }

        actionRow.addView(
            unlockAll,
            LinearLayout.LayoutParams(
                -2,
                dp(40)
            )
        )

        root.addView(
            actionRow,
            LinearLayout.LayoutParams(
                -1,
                dp(48)
            ).apply {
                bottomMargin = dp(10)
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
                    dp(4),
                    0,
                    dp(24)
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

        adapter = AppsAdapter()

        listView.adapter = adapter

        updateFilterAppearance()

        setContentView(root)
    }

    private fun createActionButton(
        text: String,
        filled: Boolean
    ): TextView {

        return TextView(this).apply {

            this.text = text
            textSize = 12f
            gravity = Gravity.CENTER

            setPadding(
                dp(14),
                0,
                dp(14),
                0
            )

            if (filled) {

                setTextColor(Color.WHITE)

                background =
                    createRoundedBackground(
                        "#2563EB",
                        18f
                    )

            } else {

                setTextColor(
                    Color.parseColor("#2563EB")
                )

                background =
                    createRoundedBackground(
                        "#E8F0FE",
                        18f
                    )
            }
        }
    }

    private fun loadApps() {

        allApps.clear()

        val pm = packageManager

        val installedApps =
            pm.getInstalledApplications(0)

        for (appInfo in installedApps) {

            val currentPackage =
                appInfo.packageName

            if (currentPackage == packageName) {
                continue
            }

            pm.getLaunchIntentForPackage(
                currentPackage
            ) ?: continue

            val appName =
                try {
                    pm.getApplicationLabel(
                        appInfo
                    ).toString()
                } catch (_: Exception) {
                    currentPackage
                }

            val isSystem =
                isSystemApplication(appInfo)

            val category =
                getAppCategory(
                    currentPackage,
                    appName,
                    isSystem
                )

            allApps.add(
                AppModel(
                    appName = appName,
                    packageName = currentPackage,
                    icon = appInfo.loadIcon(pm),
                    isSystem = isSystem,
                    category = category,
                    isLocked =
                        isEffectivelyLocked(
                            currentPackage
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

    private fun isSystemApplication(
        appInfo: ApplicationInfo
    ): Boolean {

        return (
            appInfo.flags and
                ApplicationInfo.FLAG_SYSTEM
        ) != 0 ||
            (
                appInfo.flags and
                    ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
            ) != 0
    }

    private fun isEffectivelyLocked(
        packageName: String
    ): Boolean {

        if (
            AppLockPreferences.isLocked(
                this,
                packageName
            )
        ) {
            return true
        }

        return AppGroupManager
            .getGroupsForApp(
                this,
                packageName
            )
            .any { groupName ->

                AppGroupManager.isGroupLocked(
                    this,
                    groupName
                )
            }
    }

    private fun isLockedByGroup(
        packageName: String
    ): Boolean {

        return AppGroupManager
            .getGroupsForApp(
                this,
                packageName
            )
            .any { groupName ->

                AppGroupManager.isGroupLocked(
                    this,
                    groupName
                )
            }
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
                packageName +
                    " " +
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

            value.contains("game") ->
                "Games"

            else ->
                "Other"
        }
    }

    private fun refreshList() {

        if (!::adapter.isInitialized) {
            return
        }

        syncAppLockStates()

        adapter.notifyDataSetChanged()

        val visibleCount =
            getFilteredApps().size

        val lockedCount =
            allApps.count {
                isEffectivelyLocked(
                    it.packageName
                )
            }

        countText.text =
            "$visibleCount apps • $lockedCount locked"
    }

    private fun syncAppLockStates() {

        for (app in allApps) {

            app.isLocked =
                isEffectivelyLocked(
                    app.packageName
                )
        }
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

            val locked =
                isEffectivelyLocked(
                    app.packageName
                )

            val matchesFilter =
                when (currentFilter) {

                    "locked" ->
                        locked

                    "unlocked" ->
                        !locked

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

        var changedCount = 0

        for (app in allApps) {

            if (
                app.isSystem &&
                !AppLockRulesManager
                    .isSystemAppLockEnabled(
                        this
                    )
            ) {
                continue
            }

            val currentlyLocked =
                isEffectivelyLocked(
                    app.packageName
                )

            if (
                locked &&
                currentlyLocked
            ) {
                continue
            }

            if (
                !locked &&
                !currentlyLocked
            ) {
                continue
            }

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

            app.isLocked = locked

            changedCount++
        }

        /*
         * Bulk Unlock also turns off group protection.
         * This keeps group state and individual app state
         * consistent.
         */
        if (!locked) {

            val groups =
                AppGroupManager.getGroups(
                    this
                )

            for (groupName in groups) {

                if (
                    AppGroupManager.isGroupLocked(
                        this,
                        groupName
                    )
                ) {

                    AppGroupManager.setGroupLocked(
                        this,
                        groupName,
                        false
                    )
                }
            }
        }

        AppAccessibilityService.lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        AppAccessibilityService.clearAllSessions()

        if (changedCount > 0) {

            SecurityActivityLogManager.addLog(
                this,
                SecurityActivityLogManager
                    .TYPE_SETTING_CHANGED,
                if (locked)
                    "Apps locked in bulk"
                else
                    "Apps unlocked in bulk",
                "$changedCount apps"
            )
        }

        loadApps()

        Toast.makeText(
            this,
            if (locked)
                "$changedCount apps locked"
            else
                "$changedCount apps unlocked",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showGroupsDialog() {

        val groups =
            AppGroupManager.getGroups(this)

        if (groups.isEmpty()) {

            MaterialAlertDialogBuilder(this)
                .setTitle("App Groups")
                .setMessage(
                    "Create groups such as Social, Finance or Private to manage multiple apps together."
                )
                .setPositiveButton(
                    "Create Group"
                ) { _, _ ->
                    showCreateGroupDialog()
                }
                .setNegativeButton(
                    "Close",
                    null
                )
                .show()

            return
        }

        val items =
            groups.map { groupName ->

                val appCount =
                    AppGroupManager
                        .getGroupApps(
                            this,
                            groupName
                        )
                        .size

                val locked =
                    AppGroupManager
                        .isGroupLocked(
                            this,
                            groupName
                        )

                "$groupName • $appCount apps • " +
                    if (locked)
                        "Locked"
                    else
                        "Unlocked"

            }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("App Groups")
            .setItems(items) { _, which ->

                showGroupActionsDialog(
                    groups[which]
                )
            }
            .setPositiveButton(
                "Create Group"
            ) { _, _ ->
                showCreateGroupDialog()
            }
            .setNegativeButton(
                "Close",
                null
            )
            .show()
    }

    private fun showCreateGroupDialog() {

        val input =
            EditText(this).apply {

                hint = "Group name"
                setSingleLine(true)

                setPadding(
                    dp(24),
                    dp(16),
                    dp(24),
                    dp(8)
                )
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Create App Group")
            .setView(input)
            .setPositiveButton(
                "Create"
            ) { _, _ ->

                val name =
                    input.text
                        .toString()
                        .trim()

                if (
                    AppGroupManager.createGroup(
                        this,
                        name
                    )
                ) {

                    SecurityActivityLogManager.addLog(
                        this,
                        SecurityActivityLogManager
                            .TYPE_SETTING_CHANGED,
                        "App group created",
                        name
                    )

                    Toast.makeText(
                        this,
                        "$name created",
                        Toast.LENGTH_SHORT
                    ).show()

                    showGroupActionsDialog(
                        name
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Group name is empty or already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun showGroupActionsDialog(
        groupName: String
    ) {

        val appCount =
            AppGroupManager
                .getGroupApps(
                    this,
                    groupName
                )
                .size

        val locked =
            AppGroupManager
                .isGroupLocked(
                    this,
                    groupName
                )

        MaterialAlertDialogBuilder(this)
            .setTitle(groupName)
            .setMessage(
                "$appCount apps in this group\n\n" +
                    if (locked)
                        "Group protection is ON"
                    else
                        "Group protection is OFF"
            )
            .setPositiveButton(
                if (locked)
                    "Unlock Group"
                else
                    "Lock Group"
            ) { _, _ ->

                setGroupLocked(
                    groupName,
                    !locked
                )
            }
            .setNeutralButton(
                "Manage Apps"
            ) { _, _ ->

                showManageGroupAppsDialog(
                    groupName
                )
            }
            .setNegativeButton(
                "Delete"
            ) { _, _ ->

                MaterialAlertDialogBuilder(this)
                    .setTitle(
                        "Delete group?"
                    )
                    .setMessage(
                        "Apps will remain installed. Their individual lock settings will not be changed."
                    )
                    .setPositiveButton(
                        "Delete"
                    ) { _, _ ->

                        AppGroupManager.deleteGroup(
                            this,
                            groupName
                        )

                        SecurityActivityLogManager.addLog(
                            this,
                            SecurityActivityLogManager
                                .TYPE_SETTING_CHANGED,
                            "App group deleted",
                            groupName
                        )

                        Toast.makeText(
                            this,
                            "$groupName deleted",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadApps()
                    }
                    .setNegativeButton(
                        "Cancel",
                        null
                    )
                    .show()
            }
            .show()
    }

    private fun showManageGroupAppsDialog(
        groupName: String
    ) {

        if (allApps.isEmpty()) {

            Toast.makeText(
                this,
                "No launchable apps found",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val names =
            allApps
                .map {
                    it.appName
                }
                .toTypedArray()

        val selected =
            BooleanArray(
                allApps.size
            ) { index ->

                AppGroupManager
                    .isAppInGroup(
                        this,
                        groupName,
                        allApps[index].packageName
                    )
            }

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Apps in $groupName"
            )
            .setMultiChoiceItems(
                names,
                selected
            ) { _, which, checked ->

                val app =
                    allApps[which]

                if (checked) {

                    AppGroupManager.addAppToGroup(
                        this,
                        groupName,
                        app.packageName
                    )

                } else {

                    AppGroupManager.removeAppFromGroup(
                        this,
                        groupName,
                        app.packageName
                    )
                }
            }
            .setPositiveButton(
                "Done"
            ) { _, _ ->

                if (
                    AppGroupManager.isGroupLocked(
                        this,
                        groupName
                    )
                ) {

                    applyGroupProtection(
                        groupName,
                        true
                    )
                }

                loadApps()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun setGroupLocked(
        groupName: String,
        locked: Boolean
    ) {

        val apps =
            AppGroupManager
                .getGroupApps(
                    this,
                    groupName
                )

        if (apps.isEmpty()) {

            Toast.makeText(
                this,
                "Add apps to the group first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        AppGroupManager.setGroupLocked(
            this,
            groupName,
            locked
        )

        applyGroupProtection(
            groupName,
            locked
        )

        SecurityActivityLogManager.addLog(
            this,
            SecurityActivityLogManager
                .TYPE_SETTING_CHANGED,
            if (locked)
                "App group locked"
            else
                "App group unlocked",
            groupName
        )

        AppAccessibilityService.clearAllSessions()

        loadApps()

        Toast.makeText(
            this,
            if (locked)
                "$groupName locked"
            else
                "$groupName unlocked",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun applyGroupProtection(
        groupName: String,
        locked: Boolean
    ) {

        val apps =
            AppGroupManager
                .getGroupApps(
                    this,
                    groupName
                )

        for (packageName in apps) {

            val app =
                allApps.firstOrNull {
                    it.packageName == packageName
                }

            if (
                app?.isSystem == true &&
                !AppLockRulesManager
                    .isSystemAppLockEnabled(
                        this
                    )
            ) {
                continue
            }

            AppLockPreferences.setLocked(
                this,
                packageName,
                locked
            )

            if (!locked) {

                TemporaryUnlockManager
                    .clearTemporaryUnlock(
                        this,
                        packageName
                    )
            }
        }

        AppAccessibilityService.lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()
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
                dp(12),
                0,
                dp(12),
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
            marginEnd = dp(6)
        }
    }

    private fun createRoundedBackground(
        color: String,
        radiusDp: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                Color.parseColor(color)
            )

            cornerRadius =
                radiusDp *
                    resources.displayMetrics.density
        }
    }

    private fun showTemporaryUnlockDialog(
        app: AppModel
    ) {

        if (
            !isEffectivelyLocked(
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

        SecurityActivityLogManager.addLog(
            this,
            SecurityActivityLogManager
                .TYPE_UNLOCK,
            "Temporary unlock",
            if (minutes == 0)
                "${app.appName} until phone is locked"
            else
                "${app.appName} for $minutes minutes"
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

        } catch (_: Exception) {

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

            val effectiveLocked =
                isEffectivelyLocked(
                    app.packageName
                )

            val groupLocked =
                isLockedByGroup(
                    app.packageName
                )

            val card =
                LinearLayout(
                    this@AppsActivity
                ).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        dp(18),
                        dp(14),
                        dp(14),
                        dp(14)
                    )

                    background =
                        createRoundedBackground(
                            "#FFFFFF",
                            20f
                        )

                    layoutParams =
                        AbsListView.LayoutParams(
                            -1,
                            dp(88)
                        )
                }

            val icon =
                ImageView(
                    this@AppsActivity
                ).apply {

                    setImageDrawable(
                        app.icon
                    )

                    contentDescription =
                        app.appName
                }

            card.addView(
                icon,
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
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
                        dp(14),
                        0,
                        dp(8),
                        0
                    )
                }

            val name =
                TextView(
                    this@AppsActivity
                ).apply {

                    text =
                        app.appName

                    textSize = 15f
                    maxLines = 1

                    ellipsize =
                        android.text.TextUtils
                            .TruncateAt.END

                    setTextColor(
                        Color.parseColor(
                            "#111827"
                        )
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
                        when {

                            groupLocked ->
                                "Group protected"

                            app.isSystem ->
                                "System"

                            else ->
                                app.category
                        }

                    textSize = 11f

                    setTextColor(
                        Color.parseColor(
                            "#8A94A6"
                        )
                    )

                    setPadding(
                        0,
                        dp(3),
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

            if (effectiveLocked) {

                val temporaryButton =
                    TextView(
                        this@AppsActivity
                    ).apply {

                        text = "⏱"
                        textSize = 20f
                        gravity = Gravity.CENTER

                        setTextColor(
                            Color.parseColor(
                                "#2563EB"
                            )
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
                        dp(44),
                        dp(44)
                    ).apply {
                        marginEnd = dp(8)
                    }
                )
            }

            val switch =
                SwitchCompat(
                    this@AppsActivity
                ).apply {

                    setOnCheckedChangeListener(
                        null
                    )

                    isChecked =
                        effectiveLocked

                    contentDescription =
                        if (effectiveLocked)
                            "${app.appName} locked"
                        else
                            "${app.appName} unlocked"

                    setOnCheckedChangeListener {
                            _,
                            checked ->

                        if (
                            !checked &&
                            groupLocked
                        ) {

                            setOnCheckedChangeListener(
                                null
                            )

                            isChecked = true

                            setOnCheckedChangeListener {
                                    _,
                                    newChecked ->

                                handleAppLockChange(
                                    app,
                                    newChecked
                                )
                            }

                            Toast.makeText(
                                this@AppsActivity,
                                "Unlock the group first",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@setOnCheckedChangeListener
                        }

                        handleAppLockChange(
                            app,
                            checked
                        )
                    }
                }

            card.addView(
                switch,
                LinearLayout.LayoutParams(
                    dp(52),
                    dp(48)
                )
            )

            card.setOnLongClickListener {

                if (effectiveLocked) {

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

    private fun handleAppLockChange(
        app: AppModel,
        checked: Boolean
    ) {

        if (
            app.isSystem &&
            !AppLockRulesManager
                .isSystemAppLockEnabled(
                    this
                )
        ) {

            Toast.makeText(
                this,
                "System app locking is disabled",
                Toast.LENGTH_SHORT
            ).show()

            refreshList()
            return
        }

        val previousState =
            isEffectivelyLocked(
                app.packageName
            )

        AppLockPreferences.setLocked(
            this,
            app.packageName,
            checked
        )

        if (!checked) {

            TemporaryUnlockManager
                .clearTemporaryUnlock(
                    this,
                    app.packageName
                )

            AppAccessibilityService
                .clearSession(
                    app.packageName
                )
        }

        if (checked) {

            AppAccessibilityService
                .clearSession(
                    app.packageName
                )
        }

        app.isLocked = checked

        AppAccessibilityService.lockedAppsList =
            AppLockPreferences
                .getLockedApps(this)
                .toSet()

        if (previousState != checked) {

            if (checked) {

                StatsManager.recordAppLocked(
                    this
                )

                SecurityActivityLogManager.addLog(
                    this,
                    SecurityActivityLogManager
                        .TYPE_APP_LOCKED,
                    "App locked",
                    app.appName
                )

            } else {

                SecurityActivityLogManager.addLog(
                    this,
                    SecurityActivityLogManager
                        .TYPE_APP_UNLOCKED,
                    "App unlocked",
                    app.appName
                )
            }
        }

        refreshList()
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
    ) : android.text.TextWatcher {

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

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
