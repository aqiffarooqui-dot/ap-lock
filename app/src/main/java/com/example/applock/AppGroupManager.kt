package com.example.applock

import android.content.Context

object AppGroupManager {

    private const val PREF_NAME =
        "FarooquiAppGroups"

    private const val KEY_GROUP_NAMES =
        "group_names"

    private const val KEY_APPS_PREFIX =
        "group_apps_"

    private const val KEY_LOCKED_PREFIX =
        "group_locked_"

    fun getGroups(
        context: Context
    ): List<String> {

        return getPrefs(context)
            .getStringSet(
                KEY_GROUP_NAMES,
                emptySet()
            )
            ?.toList()
            ?.sorted()
            ?: emptyList()
    }

    fun createGroup(
        context: Context,
        groupName: String
    ): Boolean {

        val name =
            groupName.trim()

        if (name.isEmpty()) {
            return false
        }

        val groups =
            getGroups(context).toMutableSet()

        if (groups.contains(name)) {
            return false
        }

        groups.add(name)

        getPrefs(context)
            .edit()
            .putStringSet(
                KEY_GROUP_NAMES,
                groups
            )
            .putStringSet(
                KEY_APPS_PREFIX + name,
                emptySet()
            )
            .putBoolean(
                KEY_LOCKED_PREFIX + name,
                false
            )
            .apply()

        return true
    }

    fun deleteGroup(
        context: Context,
        groupName: String
    ) {

        val groups =
            getGroups(context).toMutableSet()

        groups.remove(groupName)

        getPrefs(context)
            .edit()
            .putStringSet(
                KEY_GROUP_NAMES,
                groups
            )
            .remove(
                KEY_APPS_PREFIX + groupName
            )
            .remove(
                KEY_LOCKED_PREFIX + groupName
            )
            .apply()
    }

    fun renameGroup(
        context: Context,
        oldName: String,
        newName: String
    ): Boolean {

        val oldGroup =
            oldName.trim()

        val newGroup =
            newName.trim()

        if (
            oldGroup.isEmpty() ||
            newGroup.isEmpty() ||
            oldGroup == newGroup
        ) {
            return false
        }

        val groups =
            getGroups(context).toMutableSet()

        if (
            !groups.contains(oldGroup) ||
            groups.contains(newGroup)
        ) {
            return false
        }

        val apps =
            getGroupApps(
                context,
                oldGroup
            )

        val locked =
            isGroupLocked(
                context,
                oldGroup
            )

        groups.remove(oldGroup)
        groups.add(newGroup)

        getPrefs(context)
            .edit()
            .putStringSet(
                KEY_GROUP_NAMES,
                groups
            )
            .remove(
                KEY_APPS_PREFIX + oldGroup
            )
            .remove(
                KEY_LOCKED_PREFIX + oldGroup
            )
            .putStringSet(
                KEY_APPS_PREFIX + newGroup,
                apps
            )
            .putBoolean(
                KEY_LOCKED_PREFIX + newGroup,
                locked
            )
            .apply()

        return true
    }

    fun getGroupApps(
        context: Context,
        groupName: String
    ): Set<String> {

        return getPrefs(context)
            .getStringSet(
                KEY_APPS_PREFIX + groupName,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }

    fun addAppToGroup(
        context: Context,
        groupName: String,
        packageName: String
    ) {

        val apps =
            getGroupApps(
                context,
                groupName
            ).toMutableSet()

        apps.add(packageName)

        getPrefs(context)
            .edit()
            .putStringSet(
                KEY_APPS_PREFIX + groupName,
                apps
            )
            .apply()
    }

    fun removeAppFromGroup(
        context: Context,
        groupName: String,
        packageName: String
    ) {

        val apps =
            getGroupApps(
                context,
                groupName
            ).toMutableSet()

        apps.remove(packageName)

        getPrefs(context)
            .edit()
            .putStringSet(
                KEY_APPS_PREFIX + groupName,
                apps
            )
            .apply()
    }

    fun isAppInGroup(
        context: Context,
        groupName: String,
        packageName: String
    ): Boolean {

        return getGroupApps(
            context,
            groupName
        ).contains(packageName)
    }

    fun isGroupLocked(
        context: Context,
        groupName: String
    ): Boolean {

        return getPrefs(context)
            .getBoolean(
                KEY_LOCKED_PREFIX + groupName,
                false
            )
    }

    fun setGroupLocked(
        context: Context,
        groupName: String,
        locked: Boolean
    ) {

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_LOCKED_PREFIX + groupName,
                locked
            )
            .apply()
    }

    fun clearGroupApps(
        context: Context,
        groupName: String
    ) {

        getPrefs(context)
            .edit()
            .putStringSet(
                KEY_APPS_PREFIX + groupName,
                emptySet()
            )
            .apply()
    }

    fun getGroupsForApp(
        context: Context,
        packageName: String
    ): List<String> {

        return getGroups(context)
            .filter { groupName ->
                isAppInGroup(
                    context,
                    groupName,
                    packageName
                )
            }
    }

    private fun getPrefs(
        context: Context
    ) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
}
