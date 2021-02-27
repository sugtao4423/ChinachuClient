package com.tao.chinachuclient

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.tenthbit.view.ZoomImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ShowImage : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 810
    }

    private lateinit var programId: String
    private var pos: Int = -1
    private lateinit var byteImage: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_image)
        val image = findViewById<ZoomImageView>(R.id.showImageImage)

        val base64 = intent.getStringExtra("base64")
        pos = intent.getIntExtra("pos", -1)
        programId = intent.getStringExtra("programId")!!
        byteImage = Base64.decode(base64, Base64.DEFAULT)

        image.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.size))
    }

    fun clickImageOption(@Suppress("UNUSED_PARAMETER") v: View) {
        AlertDialog.Builder(this)
                .setMessage(R.string.is_save)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok) { _, _ ->
                    saveImage()
                }
                .show()
    }

    private fun saveImage() {
        if (!hasWriteExternalStoragePermission()) {
            requestWriteExternalStoragePermission()
            return
        }

        val fileName = if (pos == -1) {
            programId
        } else {
            "$programId-$pos"
        }
        val type = ".jpg"
        val saveDir = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS
        val imgPath = "$saveDir/$fileName$type"

        if (File(imgPath).exists()) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.error_file_already_exists)
                    .setItems(resources.getStringArray(R.array.file_already_exists_fix_suggestion)) { _, which ->
                        if (which == 0) {
                            save(imgPath)
                        } else if (which == 1) {
                            val edit = EditText(this)
                            edit.setText(fileName)
                            AlertDialog.Builder(this)
                                    .setTitle(R.string.assign_file_name)
                                    .setView(edit)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.ok) { _, _ ->
                                        val newPath = "$saveDir/${edit.text}$type"
                                        if (File(newPath).exists()) {
                                            saveImage()
                                        } else {
                                            save(newPath)
                                        }

                                    }.show()
                        }
                    }
                    .show()
        } else {
            save(imgPath)
        }
    }

    private fun save(imgPath: String) {
        try {
            val fos = FileOutputStream(imgPath, true)
            fos.write(byteImage)
            fos.close()
        } catch (e: IOException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, getString(R.string.saved) + "\n" + imgPath, Toast.LENGTH_LONG).show()
    }

    private fun hasWriteExternalStoragePermission(): Boolean {
        val writeExternalStorage = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return (writeExternalStorage == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            return
        }
        if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage()
        } else {
            Toast.makeText(applicationContext, R.string.fail_permission, Toast.LENGTH_LONG).show()
        }
    }

}