package com.example.applock

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class AppGridAdapter(
    private val context: Context,
    private val appList: List<AppModel>,
    private val onLockChanged:
        (AppModel, Boolean) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int =
        appList.size

    override fun getItem(position: Int): Any =
        appList[position]

    override fun getItemId(position: Int): Long =
        position.toLong()

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val cardLayout: LinearLayout

        if (convertView == null) {

            cardLayout =
                LinearLayout(context).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    setBackgroundResource(
                        R.drawable.settings_card_bg
                    )

                    setPadding(
                        24,
                        24,
                        24,
                        24
                    )

                    gravity =
                        Gravity.CENTER_HORIZONTAL
                }

        } else {

            cardLayout =
                convertView as LinearLayout
        }

        cardLayout.removeAllViews()

        val app =
            appList[position]

        val iconView =
            ImageView(context).apply {

                setImageDrawable(
                    app.icon
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        120,
                        120
                    )
            }

        cardLayout.addView(
            iconView
        )

        val nameView =
            TextView(context).apply {

                text =
                    app.appName

                textSize =
                    14f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    Color.parseColor(
                        "#000000"
                    )
                )

                setPadding(
                    0,
                    16,
                    0,
                    16
                )

                gravity =
                    Gravity.CENTER

                maxLines =
                    1

                ellipsize =
                    android.text.TextUtils.TruncateAt.END
            }

        cardLayout.addView(
            nameView
        )

        val switchView =
            SwitchCompat(context)

        // IMPORTANT:
        // listener pehle remove karo
        // taki recycled GridView item unwanted
        // callback trigger na kare.
        switchView.setOnCheckedChangeListener(
            null
        )

        switchView.isChecked =
            app.isLocked

        switchView.setOnCheckedChangeListener {
                _,
                isChecked ->

            if (
                app.isLocked !=
                isChecked
            ) {

                app.isLocked =
                    isChecked

                onLockChanged(
                    app,
                    isChecked
                )
            }
        }

        cardLayout.addView(
            switchView
        )

        return cardLayout
    }
}
