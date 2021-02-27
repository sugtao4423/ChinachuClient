package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tenthbit.view.ZoomImageView
import java.io.FileOutputStream

class ShowImage : AppCompatActivity() {

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
        val fileName = if (pos == -1) {
            programId
        } else {
            "$programId-$pos"
        }
        val type = ".jpg"
        val imageName = fileName + type

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, imageName)
            put(MediaStore.Downloads.MIME_TYPE, "image/jpg")
            put(MediaStore.Downloads.IS_PENDING, true)
        }

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val item = contentResolver.insert(collection, values)!!
        contentResolver.openFileDescriptor(item, "w").use {
            FileOutputStream(it!!.fileDescriptor).use { fos ->
                fos.write(byteImage)
                fos.close()
            }
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, false)
        contentResolver.update(item, values, null, null)

        Toast.makeText(this, getString(R.string.saved) + "\n" + imageName, Toast.LENGTH_LONG).show()
    }

}