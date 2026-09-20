package com.example.applock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var componentName: ComponentName

    private lateinit var devicePolicyManager:
        DevicePolicyManager

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        devicePolicyManager =
            getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        componentName =
            ComponentName(
                this,
                MyDeviceAdminReceiver::class.java
            )

        val scrollView =
            ScrollView(this).apply {

                setBackgroundColor(
                    Color.parseColor(
                        "#F2F2F7"
                    )
                )
            }

        val mainLayout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    32,
                    48,
                    32,
                    48
                )
            }

        // -------------------------------------------------
        // TITLE
        // -------------------------------------------------

        val titleView =
            TextView(this).apply {

                text = "Settings"

                textSize = 28f

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
                    0,
                    0,
                    32
                )
            }

        mainLayout.addView(
            titleView
        )

        // -------------------------------------------------
        // SECURITY & AUTHENTICATION
        // -------------------------------------------------

        val securitySectionTitle =
            createSectionTitle(
                "SECURITY & AUTHENTICATION"
            )

        mainLayout.addView(
            securitySectionTitle
        )

        val securityCard =
            createCard()

        val phoneAuthInfo =
            TextView(this).apply {

                text =
                    "App Lock uses your phone's biometric or screen-lock credential. No separate App Lock PIN is required."

                textSize = 15f

                setTextColor(
                    Color.parseColor(
                        "#3A3A3C"
                    )
                )

                setPadding(
                    0,
                    16,
                    0,
                    16
                )
            }

        securityCard.addView(
            phoneAuthInfo
        )

        mainLayout.addView(
            securityCard
        )

        // -------------------------------------------------
        // PRIVACY & PROTECTION
        // -------------------------------------------------

        val privacySectionTitle =
            createSectionTitle(
                "PRIVACY & PROTECTION"
            )

        mainLayout.addView(
            privacySectionTitle
        )

        val privacyCard =
            createCard()

        // -------------------------------------------------
        // INTRUDER SELFIE
        // -------------------------------------------------

        val intruderSelfieRow =
            createSettingRow(
                "Intruder Selfie (Capture on Fail)",
                SettingsManager
                    .isIntruderSelfieEnabled(
                        this
                    )
            ) { isChecked ->

                SettingsManager
                    .setIntruderSelfieEnabled(
                        this,
                        isChecked
                    )
            }

        privacyCard.addView(
            intruderSelfieRow
        )

        privacyCard.addView(
            createDivider()
        )

        // -------------------------------------------------
        // DISGUISE ICON
        // -------------------------------------------------

        val stealthRow =
            createSettingRow(
                "Disguise Icon as Calculator",
                false
            ) { isChecked ->

                if (isChecked) {

                    DisguiseHelper.switchIcon(
                        this,
                        "calculator"
                    )

                } else {

                    DisguiseHelper.switchIcon(
                        this,
                        "normal"
                    )
                }
            }

        privacyCard.addView(
            stealthRow
        )

        privacyCard.addView(
            createDivider()
        )

        // -------------------------------------------------
        // UNINSTALL PROTECTION
        // -------------------------------------------------

        val isAdminActive =
            devicePolicyManager.isAdminActive(
                componentName
            )

        val uninstallProtectionRow =
            createSettingRow(
                "Uninstall Protection",
                isAdminActive
            ) { isChecked ->

                if (isChecked) {

                    val intent =
                        Intent(
                            DevicePolicyManager
                                .ACTION_ADD_DEVICE_ADMIN
                        ).apply {

                            putExtra(
                                DevicePolicyManager
                                    .EXTRA_DEVICE_ADMIN,
                                componentName
                            )

                            putExtra(
                                DevicePolicyManager
                                    .EXTRA_ADD_EXPLANATION,
                                "Enable Uninstall Protection to prevent unauthorized uninstallation of Farooqui App Lock."
                            )
                        }

                    startActivity(
                        intent
                    )

                } else {

                    devicePolicyManager
                        .removeActiveAdmin(
                            componentName
                        )
                }
            }

        privacyCard.addView(
            uninstallProtectionRow
        )

        mainLayout.addView(
            privacyCard
        )

        // -------------------------------------------------
        // UNLOCK BEHAVIOR
        // -------------------------------------------------

        val unlockBehaviorTitle =
            createSectionTitle(
                "UNLOCK BEHAVIOR"
            )

        mainLayout.addView(
            unlockBehaviorTitle
        )

        val unlockBehaviorCard =
            createCard()

        val currentBehavior =
            SettingsManager
                .getUnlockBehavior(
                    this
                )

        // Ask biometric every time
        val everyTimeRow =
            createSettingRow(
                "Ask biometric every time",
                currentBehavior == 0
            ) { checked ->

                if (checked) {

                    SettingsManager
                        .setUnlockBehavior(
                            this,
                            0
                        )
                }
            }

        unlockBehaviorCard.addView(
            everyTimeRow
        )

        unlockBehaviorCard.addView(
            createDivider()
        )

        // Stay unlocked until phone is locked
        val untilPhoneLockedRow =
            createSettingRow(
                "Stay unlocked until phone is locked",
                currentBehavior == 1
            ) { checked ->

                if (checked) {

                    SettingsManager
                        .setUnlockBehavior(
                            this,
                            1
                        )
                }
            }

        unlockBehaviorCard.addView(
            untilPhoneLockedRow
        )

        mainLayout.addView(
            unlockBehaviorCard
        )

        // -------------------------------------------------
        // INFORMATION
        // -------------------------------------------------

        val aboutSectionTitle =
            createSectionTitle(
                "INFORMATION"
            )

        mainLayout.addView(
            aboutSectionTitle
        )

        val aboutCard =
            createCard()

        val aboutRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    0,
                    16,
                    0,
                    16
                )

                gravity =
                    Gravity.CENTER_VERTICAL

                setOnClickListener {

                    startActivity(
                        Intent(
                            this@SettingsActivity,
                            AboutActivity::class.java
                        )
                    )
                }
            }

        val aboutLabel =
            TextView(this).apply {

                text =
                    "About & Version History"

                textSize = 16f

                setTextColor(
                    Color.parseColor(
                        "#000000"
                    )
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        aboutRow.addView(
            aboutLabel
        )

        val arrowView =
            TextView(this).apply {

                text = "›"

                textSize = 22f

                setTextColor(
                    Color.parseColor(
                        "#C6C6C8"
                    )
                )
            }

        aboutRow.addView(
            arrowView
        )

        aboutCard.addView(
            aboutRow
        )

        mainLayout.addView(
            aboutCard
        )

        // -------------------------------------------------
        // FINAL LAYOUT
        // -------------------------------------------------

        scrollView.addView(
            mainLayout
        )

        setContentView(
            scrollView
        )
    }

    // -----------------------------------------------------
    // SECTION TITLE
    // -----------------------------------------------------

    private fun createSectionTitle(
        title: String
    ): TextView {

        return TextView(this).apply {

            text = title

            textSize = 13f

            setTextColor(
                Color.parseColor(
                    "#6D6D72"
                )
            )

            setPadding(
                16,
                32,
                16,
                8
            )
        }
    }

    // -----------------------------------------------------
    // CARD
    // -----------------------------------------------------

    private fun createCard():
            LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setBackgroundResource(
                R.drawable.settings_card_bg
            )

            setPadding(
                24,
                16,
                24,
                16
            )
        }
    }

    // -----------------------------------------------------
    // SETTING ROW
    // -----------------------------------------------------

    private fun createSettingRow(
        title: String,
        initialChecked: Boolean,
        onToggle: (Boolean) -> Unit
    ): LinearLayout {

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    0,
                    16,
                    0,
                    16
                )

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val label =
            TextView(this).apply {

                text = title

                textSize = 16f

                setTextColor(
                    Color.parseColor(
                        "#000000"
                    )
                )

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        row.addView(
            label
        )

        val switchView =
            SwitchCompat(this).apply {

                isChecked =
                    initialChecked

                setOnCheckedChangeListener {
                    _,
                    isChecked ->

                    onToggle(
                        isChecked
                    )
                }
            }

        row.addView(
            switchView
        )

        return row
    }

    // -----------------------------------------------------
    // DIVIDER
    // -----------------------------------------------------

    private fun createDivider():
            View {

        return View(this).apply {

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply {

                    setMargins(
                        0,
                        8,
                        0,
                        8
                    )
                }

            setBackgroundColor(
                Color.parseColor(
                    "#C6C6C8"
                )
            )
        }
    }
}
