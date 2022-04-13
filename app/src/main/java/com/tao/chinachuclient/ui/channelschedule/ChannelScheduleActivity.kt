package com.tao.chinachuclient.ui.channelschedule

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.App
import com.tao.chinachuclient.ProgramListClickListener
import com.tao.chinachuclient.R
import com.tao.chinachuclient.Type
import com.tao.chinachuclient.databinding.ActivityChannelScheduleBinding
import com.tao.chinachuclient.ui.adapter.ProgramListAdapter
import com.tao.chinachuclient.ui.program.ProgramActivity

class ChannelScheduleActivity : AppCompatActivity() {

    private val viewModel: ChannelScheduleActivityViewModel by viewModels()
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChannelScheduleBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.getChannelSpinnerItems()
        )

        val programListAdapter = ProgramListAdapter(this, Type.CHANNEL_SCHEDULE_ACTIVITY)
        binding.programList.apply {
            adapter = programListAdapter
            onItemClickListener = ProgramListClickListener(
                this@ChannelScheduleActivity,
                Type.CHANNEL_SCHEDULE_ACTIVITY
            )
        }

        viewModel.onToast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.programList.observe(this) {
            programListAdapter.clear()
            programListAdapter.addAll(it)
        }
        viewModel.programListSelection.observe(this) {
            binding.programList.setSelection(it)
        }
        viewModel.channelSpinnerSelectedItemPosition.observe(this) {
            viewModel.onChangeChannelSpinner()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val app = applicationContext as App
        if (app.currentServer.streaming) {
            menu.add(0, Menu.FIRST, Menu.NONE, R.string.live_play)
        }
        if (app.currentServer.encStreaming) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == Menu.FIRST || item.itemId == Menu.FIRST + 1) {
            val nowProgramTitle = viewModel.getBroadcastingProgramTitle()
            val titleRes = if (item.itemId == Menu.FIRST) {
                R.string.is_live_play
            } else {
                R.string.is_live_play_encode
            }
            AlertDialog.Builder(this).apply {
                setTitle(titleRes)
                setMessage(getString(R.string.broadcasting_to) + nowProgramTitle)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.ok) { _, _ ->
                    val uri = if (item.itemId == Menu.FIRST) {
                        viewModel.getCurrentChannelStreamingUri()
                    } else {
                        viewModel.getCurrentChannelEncodeStreamingUri()
                    }
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                show()
            }
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
