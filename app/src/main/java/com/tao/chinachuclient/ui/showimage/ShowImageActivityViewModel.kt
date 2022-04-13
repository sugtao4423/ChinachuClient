package com.tao.chinachuclient.ui.showimage

import android.Manifest
import android.app.Application
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App
import com.tao.chinachuclient.R
import java.io.File
import java.io.FileOutputStream

class ShowImageActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    var base64Image = ""
        set(value) {
            if (field != value) {
                field = value
                setImage()
            }
        }
    var imagePosition = -1
    var programId = ""

    private val _onToast = LiveEvent<String>()
    val onToast: LiveData<String> = _onToast

    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> = _imageBitmap

    private val _onShowSaveDialog = LiveEvent<Unit>()
    val onShowSaveDialog: LiveData<Unit> = _onShowSaveDialog

    private val _onPermissionRequest = LiveEvent<Unit>()
    val onPermissionRequest: LiveData<Unit> = _onPermissionRequest

    private fun setImage() {
        val byteImage = Base64.decode(base64Image, Base64.DEFAULT)
        _imageBitmap.value = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.size)
    }

    fun clickImageOption() {
        _onShowSaveDialog.value = Unit
    }

    fun saveImage() {
        val imageName = if (imagePosition == -1) programId else "$programId-$imagePosition"
        val extension = ".jpg"
        val fileName = imageName + extension
        val imageByte = Base64.decode(base64Image, Base64.DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageApiMore29(fileName, imageByte)
        } else {
            val checkPermission =
                ContextCompat.checkSelfPermission(app, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkPermission == PackageManager.PERMISSION_GRANTED) {
                saveImageApiLess29(fileName, imageByte)
            } else {
                _onPermissionRequest.value = Unit
                return
            }
        }

        _onToast.value = app.resources.getString(R.string.saved, fileName)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == ShowImageActivity.PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults.first() == PackageManager.PERMISSION_GRANTED
        ) {
            saveImage()
        }
    }

    private fun saveImageApiLess29(fileName: String, imageByte: ByteArray) {
        @Suppress("DEPRECATION")
        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        FileOutputStream(target).use {
            it.write(imageByte)
            it.close()
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageApiMore29(fileName: String, imageByte: ByteArray) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = app.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)!!
        resolver.openFileDescriptor(uri, "w").use {
            FileOutputStream(it!!.fileDescriptor).use { fos ->
                fos.write(imageByte)
                fos.close()
            }
        }
        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, false)
        resolver.update(uri, contentValues, null, null)
    }
}
