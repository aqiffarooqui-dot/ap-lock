package com.example.applock

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class AppAdapter(context: Context, private val appList: List<AppModel>, private val onLockChanged: (AppModel, Boolean) -> Unit) :
    ArrayAdapter<AppModel>(context, 0, appList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_app_row, parent, false)
        val app = appList[position]

        val iconView = view.findViewById<ImageView>(R.id.imgAppIcon)
        val nameView = view.findViewById<TextView>(R.id.txtAppName)
        val switchView = view.findViewById<SwitchCompat>(R.id.switchLock)

        iconView.setImageDrawable(app.appIcon)
        nameView.text = app.appName
        switchView.isChecked = app.isLocked

        view.setOnClickListener {
            app.isLocked = !app.isLocked
            switchView.isChecked = app.isLocked
            onLockChanged(app, app.isLocked)
        }

        return view
    }
}
