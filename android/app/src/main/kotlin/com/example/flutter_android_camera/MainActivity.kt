package com.example.flutter_android_camera

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File


class MainActivity : FlutterActivity() {
    val CHANNEL = "camera"
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var _result: MethodChannel.Result
    private var imagePath: String? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            _result = result
            if (call.method == "openCamera") {
                Log.d("MainActivity", "openCamera method called")
                dispatchTakePictureIntent()
            } else {
                result.notImplemented()
            }
        }
    }

    private fun createNewImageFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PHOTO_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        ).apply {
            imagePath = absolutePath
            Log.d("MainActivity", "Image file created at: $absolutePath")
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                createNewImageFile()
            )
            Log.d("MainActivity", "File provider path: $uri")
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Log.e("MainActivity", e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Return image path to flutter
            _result.success(imagePath)
        } else {
            _result.success(null)
        }
    }
}
