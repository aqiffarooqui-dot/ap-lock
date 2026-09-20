package com.example.applock

import android.graphics.drawable.Drawable

data class AppModel(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val isSystem: Boolean,
    val category: String,
    var isLocked: Boolean
)
