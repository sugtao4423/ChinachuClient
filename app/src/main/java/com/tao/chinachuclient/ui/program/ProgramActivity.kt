package com.tao.chinachuclient.ui.program

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.App
import com.tao.chinachuclient.ProgramListClickListener
import com.tao.chinachuclient.R
import com.tao.chinachuclient.Type
import com.tao.chinachuclient.databinding.ActivityProgramBinding
import com.tao.chinachuclient.ui.adapter.ProgramListAdapter
import sugtao4423.support.progressdialog.ProgressDialog

class ProgramActivity : AppCompatActivity() {

    private val viewModel: ProgramActivityViewModel by viewModels()
    private var searchView: SearchView? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProgramBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 2: 予約済み, 3: 録画中, 4: 録画済み, 5: 番組検索
        viewModel.listType = intent.getIntExtra("type", -1)
        viewModel.query = intent.getStringExtra("query")
        if (viewModel.listType == -1) {
            finish()
            return
        }

        val programListAdapter = ProgramListAdapter(this, viewModel.listType)
        binding.programList.let {
            it.adapter = programListAdapter
            it.onItemClickListener = ProgramListClickListener(this, viewModel.listType)
        }

        binding.swipeRefresh.let {
            it.setColorSchemeColors(Color.parseColor("#2196F3"))
            it.setOnRefreshListener(viewModel.onRefreshListener)
        }
        viewModel.isShowSwipeRefresh.observe(this) {
            binding.swipeRefresh.isRefreshing = it
        }

        viewModel.onToast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.actionBarTitle.observe(this) {
            supportActionBar?.title = it
        }
        viewModel.filteredProgramList.observe(this) {
            programListAdapter.clear()
            programListAdapter.addAll(it)
        }
        viewModel.toggleLoadingProgressDialog.observe(this) {
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
        viewModel.onRecordedListCleanUpSuccess.observe(this) {
            showRecordedCleanUpSuccessDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (viewModel.listType == Type.RECORDED) {
            menu.add(0, Menu.FIRST, Menu.NONE, R.string.cleanup)
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }
        if (viewModel.listType == Type.SEARCH_PROGRAM) {
            return true
        }

        menuInflater.inflate(R.menu.search, menu)
        searchView = menu.findItem(R.id.searchView).actionView as SearchView
        searchView!!.queryHint = getString(R.string.search_of_list)
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                return onQueryTextChange(query)
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.onSearchQueryChanged(newText)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == Menu.FIRST && viewModel.listType == Type.RECORDED) {
            showRecordedCleanUpDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showRecordedCleanUpDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.cleanup)
            setMessage(R.string.is_cleanup_of_recorded_list)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.onRecordedListCleanUp()
            }
            show()
        }
    }

    private fun showRecordedCleanUpSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.done)
            setMessage(R.string.cleanup_done_is_refresh)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.ok) { _, _ ->
                viewModel.onRefreshListener.onRefresh()
            }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        if ((applicationContext as App).reloadList) {
            viewModel.loadData(true)
            (applicationContext as App).reloadList = false
        }
    }

    override fun onBackPressed() {
        if (searchView == null) {
            super.onBackPressed()
            return
        }
        if (!searchView!!.isIconified) {
            searchView!!.isIconified = true
        } else {
            super.onBackPressed()
        }
    }
}
