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

    override fun getItem(position: Int): AppModel =
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

                    gravity =
                        Gravity.CENTER_HORIZONTAL

                    setBackgroundResource(
                        R.drawable.settings_card_bg
                    )

                    setPadding(
                        dp(16),
                        dp(16),
                        dp(16),
                        dp(16)
                    )

                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                }

        } else {

            cardLayout =
                convertView as LinearLayout
        }

        cardLayout.removeAllViews()

        val app =
            appList[position]

        /*
         * App icon
         */
        val iconView =
            ImageView(context).apply {

                setImageDrawable(
                    app.icon
                )

                scaleType =
                    ImageView.ScaleType.CENTER_INSIDE

                layoutParams =
                    LinearLayout.LayoutParams(
                        dp(64),
                        dp(64)
                    ).apply {

                        bottomMargin =
                            dp(10)
                    }
            }

        cardLayout.addView(
            iconView
        )

        /*
         * App name
         */
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
                        "#111111"
                    )
                )

                gravity =
                    Gravity.CENTER

                maxLines =
                    1

                ellipsize =
                    android.text.TextUtils.TruncateAt.END

                layoutParams =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {

                        bottomMargin =
                            dp(8)
                    }
            }

        cardLayout.addView(
            nameView
        )

        /*
         * Lock switch
         *
         * Listener MUST be removed before
         * changing isChecked because GridView
         * recycles its child views.
         */
        val switchView =
            SwitchCompat(context).apply {

                setOnCheckedChangeListener(
                    null
                )

                isChecked =
                    app.isLocked

                contentDescription =
                    if (app.isLocked) {
                        "${app.appName} is locked"
                    } else {
                        "${app.appName} is unlocked"
                    }

                setOnCheckedChangeListener {
                        _,
                        isChecked ->

                    if (
                        app.isLocked !=
                        isChecked
                    ) {

                        app.isLocked =
                            isChecked

                        contentDescription =
                            if (isChecked) {
                                "${app.appName} is locked"
                            } else {
                                "${app.appName} is unlocked"
                            }

                        onLockChanged(
                            app,
                            isChecked
                        )
                    }
                }

                layoutParams =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            }

        cardLayout.addView(
            switchView
        )

        return cardLayout
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                context.resources.displayMetrics.density
            ).toInt()
    }
}
