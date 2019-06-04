package com.tao.chinachuclient

import sugtao4423.library.chinachu4j.Rule
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import sugtao4423.support.progressdialog.ProgressDialog

class RuleActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: RuleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_program)

        val list = findViewById<ListView>(R.id.programList)
        adapter = RuleListAdapter(this)
        list.adapter = adapter
        list.onItemClickListener = this

        swipeRefresh = findViewById(R.id.swipe_refresh)
        swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"))
        swipeRefresh.setOnRefreshListener(this)

        setActionBarTitle(-1)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        asyncLoad(false)
    }

    private fun setActionBarTitle(ruleCount: Int) {
        var title = getString(R.string.rule)
        if (ruleCount >= 0) {
            title += " ($ruleCount)"
        }
        supportActionBar?.title = title
    }

    private fun asyncLoad(isRefresh: Boolean) {
        adapter.clear()
        object : AsyncTask<Unit, Unit, Array<Rule>?>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                if (!isRefresh) {
                    progressDialog = ProgressDialog(this@RuleActivity).apply {
                        setMessage(getString(R.string.loading))
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(true)
                        show()
                    }
                }
            }

            override fun doInBackground(vararg params: Unit?): Array<Rule>? {
                try {
                    return (applicationContext as App).chinachu.getRules()
                } catch (e: Exception) {
                }
                return null
            }

            override fun onPostExecute(result: Array<Rule>?) {
                if (!isRefresh) {
                    progressDialog.dismiss()
                } else {
                    swipeRefresh.isRefreshing = false
                }
                if (result == null) {
                    Toast.makeText(this@RuleActivity, R.string.error_get_rule, Toast.LENGTH_SHORT).show()
                    return
                }
                adapter.addAll(result)
                setActionBarTitle(result.size)
            }
        }.execute()
    }

    override fun onRefresh() {
        asyncLoad(true)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null) {
            return
        }

        startActivity(Intent(this, RuleDetail::class.java).apply {
            putExtra("position", position.toString())
            putExtra("rule", parent.getItemAtPosition(position) as Rule)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}