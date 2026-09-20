package com.example.applock

import android.content.ContentValues
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat

class IntruderCaptureHelper(
    private val context: Context
) {

    fun captureIntruderPhoto(
        onComplete: (Uri?) -> Unit
    ) {

        if (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            Log.w(
                "IntruderCapture",
                "Camera permission is not granted"
            )

            onComplete(null)
            return
        }

        var camera: Camera? = null
        var surfaceTexture: SurfaceTexture? = null

        try {

            camera =
                Camera.open(
                    Camera.CameraInfo.CAMERA_FACING_FRONT
                )

            surfaceTexture =
                SurfaceTexture(0)

            camera.setPreviewTexture(
                surfaceTexture
            )

            camera.startPreview()

            val activeCamera = camera

            activeCamera.takePicture(
                null,
                null,
                Camera.PictureCallback { data, _ ->

                    var imageUri: Uri? = null

                    try {

                        if (data != null) {

                            imageUri =
                                saveImageToGallery(
                                    data
                                )
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "IntruderCapture",
                            "Error saving intruder selfie",
                            e
                        )

                    } finally {

                        releaseCamera(
                            activeCamera
                        )

                        try {
                            surfaceTexture?.release()
                        } catch (_: Exception) {
                        }

                        onComplete(imageUri)
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

            try {
                surfaceTexture?.release()
            } catch (_: Exception) {
            }

            onComplete(null)
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
    ): Uri? {

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

            return null
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
                ?: throw Exception(
                    "Unable to open Gallery output stream"
                )

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

            return imageUri

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

            return null
        }
    }
}
