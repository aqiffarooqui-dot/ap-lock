package com.example.applock

content.Context
graphics.Bitmap
graphics.BitmapFactory
hardware.camera2.CameraCharacteristics
hardware.camera2.CameraManager
os.Environment
os.Handler
os.Looper
provider.MediaStore
view.Surface
view.WindowManager
widget.Toast
hardware.Camera
io.File
io.FileOutputStream
util.Log

class IntruderCaptureHelper(private val context: Context) {

    fun captureIntruderPhoto() {
        try {
            // Android Camera API (Legacy/Compat support for background silent capture)
            val camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            val parameters = camera.parameters
            
            // Set optimal picture size
            camera.parameters = parameters
            
            camera.setPreviewTexture(android.graphics.SurfaceTexture(0))
            camera.startPreview()
            
            camera.takePicture(null, null, Camera.PictureCallback { data, _ ->
                try {
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    saveImageToInternalStorage(bitmap)
                } catch (e: Exception) {
                    Log.e("IntruderCapture", "Error saving photo: ${e.message}")
                } finally {
                    try {
                        camera.stopPreview()
                        camera.release()
                    } catch (ex: Exception) {
                        // ignore
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("IntruderCapture", "Camera access failed: ${e.message}")
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val directory = File(context.filesDir, "IntruderSelfies")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val filename = "Intruder_${System.currentTimeMillis()}.jpg"
        val file = File(directory, filename)

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            Log.d("IntruderCapture", "Intruder selfie saved successfully at: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
