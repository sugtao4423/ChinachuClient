package com.tao.chinachuclient.view

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tao.chinachuclient.R
import com.tao.chinachuclient.databinding.ShowImageBinding
import com.tao.chinachuclient.viewmodel.ShowImageActivityViewModel

class ShowImageActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 114514
    }

    private val viewModel: ShowImageActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ShowImageBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        viewModel.apply {
            base64Image = intent.getStringExtra("base64")!!
            imagePosition = intent.getIntExtra("pos", -1)
            programId = intent.getStringExtra("programId")!!
        }

        viewModel.onToast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.imageBitmap.observe(this) {
            binding.image.setImageBitmap(it)
        }
        viewModel.onShowSaveDialog.observe(this) {
            showImageDialog()
        }
        viewModel.onPermissionRequest.observe(this) {
            requestPermission()
        }
    }

    private fun showImageDialog() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.is_save)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ -> viewModel.saveImage() }
            show()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, grantResults)
    }
}
