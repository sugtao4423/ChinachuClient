package com.tao.chinachuclient.ui.addserver

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.R
import com.tao.chinachuclient.databinding.ActivitySettingBinding
import com.tao.chinachuclient.ui.main.MainActivity
import sugtao4423.support.progressdialog.ProgressDialog

class AddServerActivity : AppCompatActivity() {

    private val viewModel: AddServerActivityViewModelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.isStartMainActivity = intent.getBooleanExtra("startMain", false)
        viewModel.onToast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.onStartMainActivity.observe(this) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        var progressDialog: ProgressDialog? = null
        viewModel.toggleGettingChannelDialog.observe(this) {
            if (progressDialog != null) {
                progressDialog!!.dismiss()
                progressDialog = null
                return@observe
            }

            progressDialog = ProgressDialog(this).apply {
                setMessage(getString(R.string.getting_channel_list))
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                show()
            }
        }

        viewModel.onFinish.observe(this) {
            finish()
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
