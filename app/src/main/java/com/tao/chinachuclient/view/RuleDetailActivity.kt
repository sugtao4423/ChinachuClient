package com.tao.chinachuclient.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.R
import com.tao.chinachuclient.databinding.ActivityRuleDetailBinding
import com.tao.chinachuclient.viewmodel.RuleDetailActivityViewModel
import sugtao4423.library.chinachu4j.Rule
import sugtao4423.support.progressdialog.ProgressDialog

class RuleDetailActivity : AppCompatActivity() {

    private val viewModel: RuleDetailActivityViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRuleDetailBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.rulePosition = intent.getIntExtra("position", -1)
        viewModel.rule = intent.getSerializableExtra("rule") as Rule

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
                setMessage(getString(R.string.sending))
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                show()
            }
        }
        viewModel.actionBarTitle.observe(this) {
            supportActionBar?.title = it
        }
        viewModel.onRuleDeleted.observe(this) {
            showRuleDeletedDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, Menu.FIRST, Menu.NONE, R.string.delete_rule)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            Menu.FIRST -> deleteRuleDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteRuleDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.is_delete)
            setMessage(viewModel.getDeleteRuleDialogMessage())
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ -> viewModel.deleteRule() }
            show()
        }
    }

    private fun showRuleDeletedDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.done_delete)
            setMessage(R.string.back_activity_must_list_refresh)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ -> finish() }
            show()
        }
    }
}
