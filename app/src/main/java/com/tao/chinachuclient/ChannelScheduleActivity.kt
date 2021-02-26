package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Program
import sugtao4423.support.progressdialog.ProgressDialog
import java.util.*

class ChannelScheduleActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var spinnerAdapter: ArrayAdapter<String>

    private lateinit var channelIdList: List<String>
    private lateinit var selectingChannelId: String

    private lateinit var programList: ListView
    private lateinit var programListAdapter: ProgramListAdapter

    private lateinit var app: App

    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_schedule)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = applicationContext as App
        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        channelIdList = app.currentServer.channelIds.split(Regex("\\s*,\\s*"))
        app.currentServer.channelNames.split(Regex("\\s*,\\s*")).map {
            spinnerAdapter.add(it)
        }

        val spinner = toolbar.findViewById<Spinner>(R.id.toolbarSpinner)
        spinner.onItemSelectedListener = this
        spinner.adapter = spinnerAdapter

        programList = findViewById(R.id.programList)
        programListAdapter = ProgramListAdapter(this, Type.CHANNEL_SCHEDULE_ACTIVITY)
        programList.adapter = programListAdapter
        programList.onItemClickListener = ProgramListClickListener(this, Type.CHANNEL_SCHEDULE_ACTIVITY)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectingChannelId = channelIdList[position]
        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = ProgressDialog(this@ChannelScheduleActivity).apply {
                setMessage(getString(R.string.loading))
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                show()
            }
            val result = withContext(Dispatchers.IO) {
                try {
                    app.chinachu.getChannelSchedule(selectingChannelId)
                } catch (e: Exception) {
                    null
                }
            }
            progressDialog.dismiss()
            programListAdapter.clear()
            if (result == null) {
                Toast.makeText(this@ChannelScheduleActivity, R.string.error_get_schedule, Toast.LENGTH_SHORT).show()
                return@launch
            }
            Arrays.sort(result) { o1: Program, o2: Program ->
                when {
                    o1.start > o2.start -> 1
                    o1.start < o2.start -> -1
                    else -> 0
                }
            }
            programListAdapter.addAll(result)
            result.mapIndexed { i, it ->
                val now = Date().time
                if (it.start < now && it.end > now) {
                    programList.setSelection(i)
                    return@mapIndexed
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) {
            return false
        }

        if (app.streaming) {
            menu.add(0, Menu.FIRST, Menu.NONE, R.string.live_play)
        }
        if (app.encStreaming) {
            menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.live_play_encode)
        }

        menuInflater.inflate(R.menu.search, menu)
        searchView = menu.findItem(R.id.searchView).actionView as SearchView
        searchView!!.queryHint = getString(R.string.search_of_all_channel)
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                Intent(this@ChannelScheduleActivity, ProgramActivity::class.java).apply {
                    putExtra("type", Type.SEARCH_PROGRAM)
                    putExtra("query", query)
                    startActivity(this)
                    return false
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) {
            return false
        }

        if (item.itemId == Menu.FIRST || item.itemId == Menu.FIRST + 1) {
            val nowProgramTitle: String = run {
                var title = ""
                for (i in 0 until programListAdapter.count) {
                    val program = programListAdapter.getItem(i) as Program
                    val now = Date().time
                    if (program.start < now && program.end > now) {
                        title = program.title
                        break
                    }
                }
                title
            }
            val titleRes = if (item.itemId == Menu.FIRST) R.string.is_live_play else R.string.is_live_play_encode
            AlertDialog.Builder(this).apply {
                setTitle(titleRes)
                setMessage(getString(R.string.broadcasting_to) + nowProgramTitle)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.ok) { _, _ ->
                    if (item.itemId == Menu.FIRST) {
                        val uri = Uri.parse(app.chinachu.getNonEncLiveMovieURL(selectingChannelId))
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } else {
                        app.currentServer.encode.let {
                            val type = it.type
                            val params = arrayOf(
                                    it.containerFormat,
                                    it.videoCodec,
                                    it.audioCodec,
                                    it.videoBitrate,
                                    it.audioBitrate,
                                    it.videoSize,
                                    it.frame
                            )
                            val uri = Uri.parse(app.chinachu.getEncLiveMovieURL(selectingChannelId, type, params))
                            startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    }
                }
            }.show()
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
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