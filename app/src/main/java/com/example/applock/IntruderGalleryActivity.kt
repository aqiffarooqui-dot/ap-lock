package com.example.applock

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class IntruderGalleryActivity : AppCompatActivity() {

    private lateinit var galleryContainer: LinearLayout
    private val photos = mutableListOf<IntruderPhoto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildScreen()
        loadPhotos()
    }

    override fun onResume() {
        super.onResume()

        if (::galleryContainer.isInitialized) {
            loadPhotos()
        }
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF6F7FB.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(18), dp(20), dp(12))
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(0xFF111318.toInt())

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Intruder Gallery"
            textSize = 24f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111318.toInt())
        }

        val subtitle = TextView(this).apply {
            text = "Failed authentication photos"
            textSize = 14f
            setTextColor(0xFF737780.toInt())
            setPadding(0, dp(3), 0, 0)
        }

        titleBox.addView(title)
        titleBox.addView(subtitle)

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val deleteAll = MaterialButton(this).apply {
            text = "Clear"
            minWidth = dp(70)
            minHeight = dp(42)

            setOnClickListener {
                confirmDeleteAll()
            }
        }

        header.addView(
            deleteAll,
            LinearLayout.LayoutParams(
                dp(82),
                dp(44)
            )
        )

        root.addView(header)

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        galleryContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(30))
        }

        scrollView.addView(galleryContainer)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun loadPhotos() {

        photos.clear()

        val collection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        val selection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
            } else {
                "${MediaStore.Images.Media.DATA} LIKE ?"
            }

        val selectionArgs =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Environment.DIRECTORY_PICTURES +
                            "/Farooqui App Lock/"
                )
            } else {
                arrayOf(
                    "%/Pictures/Farooqui App Lock/%"
                )
            }

        val sortOrder =
            "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {

            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID
                    )

                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DISPLAY_NAME
                    )

                val dateColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DATE_ADDED
                    )

                while (cursor.moveToNext()) {

                    val id =
                        cursor.getLong(idColumn)

                    val name =
                        cursor.getString(nameColumn)

                    val date =
                        cursor.getLong(dateColumn)

                    val uri =
                        ContentUris.withAppendedId(
                            collection,
                            id
                        )

                    photos.add(
                        IntruderPhoto(
                            uri = uri,
                            name = name,
                            timestamp = date * 1000L
                        )
                    )
                }
            }

        } catch (_: Exception) {
            // Gallery remains usable even if MediaStore query fails.
        }

        renderGallery()
    }

    private fun renderGallery() {

        galleryContainer.removeAllViews()

        if (photos.isEmpty()) {
            showEmptyState()
            return
        }

        val countText = TextView(this).apply {
            text = "${photos.size} intruder " +
                    if (photos.size == 1) "photo" else "photos"

            textSize = 14f
            setTypeface(
                typeface,
                android.graphics.Typeface.BOLD
            )
            setTextColor(0xFF555A64.toInt())
            setPadding(4, 0, 0, dp(12))
        }

        galleryContainer.addView(countText)

        photos.forEach { photo ->
            addPhotoCard(photo)
        }
    }

    private fun addPhotoCard(
        photo: IntruderPhoto
    ) {

        val card = MaterialCardView(this).apply {
            radius = dp(20).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(0xFFFFFFFF.toInt())

            setOnClickListener {
                openPhoto(photo)
            }
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFFE5E7EB.toInt())

            try {
                setImageURI(photo.uri)
            } catch (_: Exception) {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@IntruderGalleryActivity,
                        android.R.drawable.ic_menu_gallery
                    )
                )
            }

            setOnClickListener {
                openPhoto(photo)
            }
        }

        row.addView(
            image,
            LinearLayout.LayoutParams(
                dp(82),
                dp(82)
            )
        )

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, dp(8), 0)
        }

        val name = TextView(this).apply {
            text = "Intruder Attempt"
            textSize = 16f
            setTypeface(
                typeface,
                android.graphics.Typeface.BOLD
            )
            setTextColor(0xFF17191D.toInt())
        }

        val time = TextView(this).apply {
            text = formatDate(photo.timestamp)
            textSize = 13f
            setTextColor(0xFF737780.toInt())
            setPadding(0, dp(5), 0, 0)
        }

        val fileName = TextView(this).apply {
            text = photo.name
            textSize = 11f
            setTextColor(0xFF9CA3AF.toInt())
            setPadding(0, dp(4), 0, 0)
        }

        info.addView(name)
        info.addView(time)
        info.addView(fileName)

        row.addView(
            info,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val delete = TextView(this).apply {
            text = "⋮"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(0xFF4B5563.toInt())

            setOnClickListener {
                showPhotoMenu(photo)
            }
        }

        row.addView(
            delete,
            LinearLayout.LayoutParams(
                dp(42),
                dp(60)
            )
        )

        card.addView(row)

        galleryContainer.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )
    }

    private fun showEmptyState() {

        val card = MaterialCardView(this).apply {
            radius = dp(24).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(0xFFFFFFFF.toInt())
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                dp(24),
                dp(45),
                dp(24),
                dp(45)
            )
        }

        val icon = TextView(this).apply {
            text = "📷"
            textSize = 42f
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "No intruder photos"
            textSize = 19f
            setTypeface(
                typeface,
                android.graphics.Typeface.BOLD
            )
            setTextColor(0xFF17191D.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }

        val message = TextView(this).apply {
            text = "Photos captured after failed authentication attempts will appear here."
            textSize = 14f
            setTextColor(0xFF737780.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, 0)
        }

        box.addView(icon)
        box.addView(title)
        box.addView(message)

        card.addView(box)

        galleryContainer.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun openPhoto(
        photo: IntruderPhoto
    ) {

        val dialog = AlertDialog.Builder(this)
            .setView(createFullScreenImage(photo))
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ ->
                deletePhoto(photo)
            }
            .create()

        dialog.show()
    }

    private fun createFullScreenImage(
        photo: IntruderPhoto
    ): ImageView {

        return ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(480)
            )

            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFF101114.toInt())

            try {
                setImageURI(photo.uri)
            } catch (_: Exception) {
                setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }
        }
    }

    private fun showPhotoMenu(
        photo: IntruderPhoto
    ) {

        AlertDialog.Builder(this)
            .setTitle("Intruder Photo")
            .setItems(
                arrayOf(
                    "View Photo",
                    "Delete Photo"
                )
            ) { _, which ->

                when (which) {

                    0 -> openPhoto(photo)

                    1 -> confirmDelete(photo)
                }
            }
            .show()
    }

    private fun confirmDelete(
        photo: IntruderPhoto
    ) {

        AlertDialog.Builder(this)
            .setTitle("Delete photo?")
            .setMessage(
                "This will permanently remove the intruder photo from your Gallery."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deletePhoto(photo)
            }
            .show()
    }

    private fun confirmDeleteAll() {

        if (photos.isEmpty()) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete all photos?")
            .setMessage(
                "All intruder photos from Farooqui App Lock will be permanently deleted."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete All") { _, _ ->
                deleteAllPhotos()
            }
            .show()
    }

    private fun deletePhoto(
        photo: IntruderPhoto
    ) {

        try {

            contentResolver.delete(
                photo.uri,
                null,
                null
            )

        } catch (_: Exception) {
        }

        loadPhotos()
    }

    private fun deleteAllPhotos() {

        photos.toList().forEach { photo ->

            try {
                contentResolver.delete(
                    photo.uri,
                    null,
                    null
                )
            } catch (_: Exception) {
            }
        }

        loadPhotos()
    }

    private fun formatDate(
        timestamp: Long
    ): String {

        val formatter =
            java.text.SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                java.util.Locale.getDefault()
            )

        return formatter.format(
            java.util.Date(timestamp)
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private data class IntruderPhoto(
        val uri: Uri,
        val name: String,
        val timestamp: Long
    )
}
