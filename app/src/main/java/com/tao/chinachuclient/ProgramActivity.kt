package com.tao.chinachuclient

import sugtao4423.library.chinachu4j.ChinachuResponse
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import android.app.AlertDialog
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import sugtao4423.support.progressdialog.ProgressDialog
import java.text.Normalizer
import java.util.*

class ProgramActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var programListAdapter: ProgramListAdapter
    private lateinit var app: App

    private var type = -1
    private lateinit var query: String
    private var searchView: SearchView? = null
    private lateinit var programList: Array<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_program)

        val list = findViewById<ListView>(R.id.programList)
        swipeRefresh = findViewById(R.id.swipe_refresh)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = applicationContext as App
        // type 1: ルール 2: 予約済み 3: 録画中 4: 録画済み 5: 番組検索
        type = intent.getIntExtra("type", -1)
        if (type == -1) {
            finish()
            return
        } else if (type == Type.SEARCH_PROGRAM) {
            query = intent.getStringExtra("query")
        }

        programListAdapter = ProgramListAdapter(this, type)
        list.adapter = programListAdapter

        list.onItemClickListener = ProgramListClickListener(this, type)
        swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"))
        swipeRefresh.setOnRefreshListener(this)

        setActionBarTitle(-1)

        asyncLoad(false)
    }

    private fun setActionBarTitle(programCount: Int) {
        val titleRes = when (type) {
            Type.RESERVES -> R.string.reserved
            Type.RECORDING -> R.string.recording
            Type.RECORDED -> R.string.recorded
            Type.SEARCH_PROGRAM -> R.string.search_result
            else -> {
                finish()
                return
            }
        }
        var title = getString(titleRes)
        if (programCount >= 0) {
            title += " ($programCount)"
        }
        supportActionBar?.title = title
    }

    private fun asyncLoad(isRefresh: Boolean) {
        programListAdapter.clear()
        object : AsyncTask<Unit, Unit, Array<*>?>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                if (!isRefresh) {
                    progressDialog = ProgressDialog(this@ProgramActivity).apply {
                        setMessage(getString(R.string.loading))
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(true)
                        show()
                    }
                }
            }

            override fun doInBackground(vararg params: Unit?): Array<*>? {
                return load()
            }

            override fun onPostExecute(result: Array<*>?) {
                super.onPostExecute(result)
                if (!isRefresh) {
                    progressDialog.dismiss()
                } else {
                    swipeRefresh.isRefreshing = false
                }
                if (result == null) {
                    Toast.makeText(this@ProgramActivity, R.string.error_get_schedule, Toast.LENGTH_SHORT).show()
                    return
                }
                programList = result
                programListAdapter.addAll(result)
                setActionBarTitle(result.size)
            }
        }.execute()
    }

    private fun load(): Array<*>? {
        try {
            return when (type) {
                Type.RESERVES -> app.chinachu.getReserves()
                Type.RECORDING -> app.chinachu.getRecording()
                Type.RECORDED -> {
                    val recorded = app.chinachu.getRecorded()
                    recorded.reverse()
                    return recorded
                }
                Type.SEARCH_PROGRAM -> {
                    val search = app.chinachu.searchProgram(query)
                    Arrays.sort(search) { o1: Program, o2: Program ->
                        when {
                            o1.start > o2.start -> 1
                            o1.start < o2.start -> -1
                            else -> 0
                        }
                    }
                    return search
                }
                else -> null
            }
        } catch (e: Exception) {
        }
        return null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) {
            return false
        }
        if (type == Type.RECORDED) {
            menu.add(0, Menu.FIRST, Menu.NONE, R.string.cleanup)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }
        if (type == Type.SEARCH_PROGRAM) {
            return true
        }

        menuInflater.inflate(R.menu.search, menu)
        searchView = menu.findItem(R.id.search_view).actionView as SearchView
        searchView!!.queryHint = getString(R.string.search_of_list)
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                return onQueryTextChange(query)
            }

            override fun onQueryTextChange(newText: String): Boolean {
                programListAdapter.clear()
                if (newText.isEmpty()) {
                    programListAdapter.addAll(programList)
                    return false
                }

                val filteredPrograms = programList.filter {
                    val item = when (type) {
                        Type.RESERVES -> (it as Reserve).program
                        Type.RECORDED -> (it as Recorded).program
                        else -> (it as Program)
                    }
                    val itemTitle = Normalizer.normalize(item.fullTitle, Normalizer.Form.NFKC).toLowerCase()
                    val searchText = newText.toLowerCase()
                    itemTitle.contains(searchText)
                }
                programListAdapter.addAll(filteredPrograms)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }

        if (item?.itemId == Menu.FIRST) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.cleanup)
                    .setMessage(R.string.is_cleanup_of_recorded_list)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        object : AsyncTask<Unit, Unit, ChinachuResponse?>() {
                            private lateinit var progressDialog: ProgressDialog

                            override fun onPreExecute() {
                                progressDialog = ProgressDialog(this@ProgramActivity).apply {
                                    setMessage(getString(R.string.loading))
                                    isIndeterminate = false
                                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                                    setCancelable(true)
                                    show()
                                }
                            }

                            override fun doInBackground(vararg params: Unit?): ChinachuResponse? {
                                try {
                                    return app.chinachu.recordedCleanUp()
                                } catch (e: Exception) {
                                }
                                return null
                            }

                            override fun onPostExecute(result: ChinachuResponse?) {
                                progressDialog.dismiss()
                                if (result == null) {
                                    Toast.makeText(this@ProgramActivity, R.string.error_access, Toast.LENGTH_SHORT).show()
                                    return
                                }
                                if (!result.result) {
                                    Toast.makeText(this@ProgramActivity, result.message, Toast.LENGTH_LONG).show()
                                    return
                                }

                                AlertDialog.Builder(this@ProgramActivity)
                                        .setTitle(R.string.done)
                                        .setMessage(R.string.cleanup_done_is_refresh)
                                        .setNegativeButton(R.string.cancel, null)
                                        .setPositiveButton(R.string.ok) { _, _ ->
                                            onRefresh()
                                        }
                                        .show()
                            }
                        }.execute()
                    }
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        asyncLoad(true)
    }

    override fun onResume() {
        super.onResume()
        if (app.reloadList) {
            asyncLoad(true)
            app.reloadList = false
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