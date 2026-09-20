package com.example.applock

import android.content.ContentValues
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log

class IntruderCaptureHelper(
    private val context: Context
) {

    fun captureIntruderPhoto() {

        if (
            android.content.pm.PackageManager.PERMISSION_GRANTED !=
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            )
        ) {

            Log.w(
                "IntruderCapture",
                "Camera permission is not granted"
            )

            return
        }

        var camera: Camera? = null

        try {

            camera =
                Camera.open(
                    Camera.CameraInfo.CAMERA_FACING_FRONT
                )

            camera.setPreviewTexture(
                SurfaceTexture(0)
            )

            camera.startPreview()

            camera.takePicture(
                null,
                null,
                Camera.PictureCallback { data, _ ->

                    try {

                        if (data != null) {
                            saveImageToGallery(data)
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "IntruderCapture",
                            "Error saving intruder selfie",
                            e
                        )

                    } finally {

                        releaseCamera(camera)
                    }
                }
            )

        } catch (e: Exception) {

            Log.e(
                "IntruderCapture",
                "Camera access failed",
                e
            )

            releaseCamera(camera)
        }
    }

    private fun releaseCamera(
        camera: Camera?
    ) {

        try {
            camera?.stopPreview()
        } catch (_: Exception) {
        }

        try {
            camera?.release()
        } catch (_: Exception) {
        }
    }

    private fun saveImageToGallery(
        imageData: ByteArray
    ) {

        val resolver =
            context.contentResolver

        val fileName =
            "Intruder_${System.currentTimeMillis()}.jpg"

        val contentValues =
            ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {

                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES +
                                "/Farooqui App Lock"
                    )

                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        1
                    )
                }
            }

        val imageUri =
            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

        if (imageUri == null) {

            Log.e(
                "IntruderCapture",
                "Unable to create Gallery image"
            )

            return
        }

        try {

            resolver.openOutputStream(
                imageUri
            )?.use { outputStream ->

                outputStream.write(
                    imageData
                )

                outputStream.flush()
            }

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                val updateValues =
                    ContentValues().apply {

                        put(
                            MediaStore.Images.Media.IS_PENDING,
                            0
                        )
                    }

                resolver.update(
                    imageUri,
                    updateValues,
                    null,
                    null
                )
            }

            Log.d(
                "IntruderCapture",
                "Intruder selfie saved: $fileName"
            )

        } catch (e: Exception) {

            try {

                resolver.delete(
                    imageUri,
                    null,
                    null
                )

            } catch (_: Exception) {
            }

            Log.e(
                "IntruderCapture",
                "Failed to save intruder selfie",
                e
            )
        }
    }
}
