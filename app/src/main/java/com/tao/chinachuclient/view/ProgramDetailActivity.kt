package com.tao.chinachuclient.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.R
import com.tao.chinachuclient.SelectionLinkMovementMethod
import com.tao.chinachuclient.ShowImage
import com.tao.chinachuclient.databinding.ActivityProgramDetailBinding
import com.tao.chinachuclient.databinding.CaptureDialogBinding
import com.tao.chinachuclient.viewmodel.ProgramDetailActivityViewModel
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import sugtao4423.support.progressdialog.ProgressDialog

class ProgramDetailActivity : AppCompatActivity() {

    private val viewModel: ProgramDetailActivityViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProgramDetailBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.programType = intent.getIntExtra("type", -1)
        if (viewModel.programType == -1) {
            finish()
            return
        }

        intent.getSerializableExtra("reserve")?.let { viewModel.reserveProgram = it as Reserve }
        intent.getSerializableExtra("recorded")?.let { viewModel.recordedProgram = it as Recorded }
        intent.getSerializableExtra("program")?.let { viewModel.program = it as Program }

        binding.programDetailDetail.movementMethod = SelectionLinkMovementMethod(this)

        viewModel.onToast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.toggleProgressDialog.observe(this) {
            if (progressDialog != null) {
                progressDialog!!.dismiss()
                progressDialog = null
                return@observe
            }
            progressDialog = ProgressDialog(this).apply {
                setMessage(getString(R.string.loading))
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                show()
            }
        }
        viewModel.actionBarTitle.observe(this) {
            supportActionBar?.title = it
        }
        viewModel.thumbnailBase64.observe(this) {
            val decodedString = Base64.decode(it, Base64.DEFAULT)
            val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            binding.programDetailImage.setImageBitmap(img)
        }
        viewModel.showOpenThumbnailDialog.observe(this) {
            showOpenThumbnailDialog(it)
        }
        viewModel.startShowImageActivity.observe(this) {
            startActivity(Intent(this, ShowImage::class.java).apply {
                putExtra("base64", it.base64)
                putExtra("programId", it.programId)
                putExtra("pos", it.pos)
            })
        }
        viewModel.startActionViewActivity.observe(this) {
            startActivity(Intent(Intent.ACTION_VIEW, it))
        }
        viewModel.operationResultDialog.observe(this) {
            AlertDialog.Builder(this).setTitle(it.title).setMessage(it.message).show()
        }
    }

    private fun showOpenThumbnailDialog(data: ProgramDetailActivityViewModel.OpenThumbnailDialogData) {
        val binding = CaptureDialogBinding.inflate(layoutInflater).also {
            it.data = data
            it.capSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    it.capPos.setText(progress.toString())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        AlertDialog.Builder(this).also {
            it.setView(binding.root)
            it.setNegativeButton(R.string.cancel, null)
            it.setPositiveButton(R.string.ok) { _, _ ->
                viewModel.openCapture(
                    binding.capPos.text.toString().toInt(),
                    binding.capResolution.text.toString()
                )
            }
            it.setNeutralButton(R.string.zoom_this_state) { _, _ ->
                viewModel.openCaptureThisState()
            }
            it.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        viewModel.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun confirm(@StringRes titleRes: Int) {
            AlertDialog.Builder(this).apply {
                setTitle(titleRes)
                setMessage(viewModel.programFullTitle)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.ok) { _, _ -> viewModel.doOptionOperations(item.itemId) }
                show()
            }
        }

        when (item.itemId) {
            android.R.id.home -> finish()
            ProgramDetailActivityViewModel.MenuId.RESERVE -> {
                confirm(R.string.is_reserve)
            }
            ProgramDetailActivityViewModel.MenuId.DELETE_RESERVE -> {
                confirm(R.string.is_delete_reserve)
            }
            ProgramDetailActivityViewModel.MenuId.SKIP_RESERVE_RELEASE -> {
                confirm(R.string.is_skip_reserve_release)
            }
            ProgramDetailActivityViewModel.MenuId.SKIP_RESERVE -> {
                confirm(R.string.is_skip_reserve)
            }
            ProgramDetailActivityViewModel.MenuId.PLAY_STREAMING -> {
                viewModel.startPlayStreamingActivity()
            }
            ProgramDetailActivityViewModel.MenuId.PLAY_ENCODE_STREAMING -> {
                viewModel.startPlayEncodeStreamingActivity()
            }
            ProgramDetailActivityViewModel.MenuId.DELETE_RECORDED_FILE -> {
                confirm(R.string.is_delete_recorded_file)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
