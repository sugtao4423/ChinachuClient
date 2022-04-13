package com.tao.chinachuclient.ui.setting

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.R
import com.tao.chinachuclient.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private val viewModel: SettingActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.onShowConfirmDialog.observe(this) {
            showConfirmDialog()
        }
        viewModel.onFinish.observe(this) {
            finish()
        }
    }

    private fun showConfirmDialog() {
        val currentAddress = viewModel.currentServerAddress
        AlertDialog.Builder(this).apply {
            setTitle(R.string.change_settings)
            setMessage(getString(R.string.change_current_server_settings, currentAddress))
            setCancelable(false)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel) { _, _ ->
                finish()
            }
            show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
