package com.tao.chinachuclient.ui.rule

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.databinding.ActivityProgramBinding
import com.tao.chinachuclient.ui.adapter.RuleListAdapter
import com.tao.chinachuclient.ui.ruledetail.RuleDetailActivity
import sugtao4423.library.chinachu4j.Rule

class RuleActivity : AppCompatActivity() {

    private val viewModel: RuleActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProgramBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = RuleListAdapter(this)
        binding.programList.let {
            it.adapter = adapter
            it.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
                startActivity(Intent(this, RuleDetailActivity::class.java).apply {
                    putExtra("position", i)
                    putExtra("rule", adapter.getItem(i) as Rule)
                })
            }
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
        viewModel.programList.observe(this) {
            adapter.clear()
            adapter.addAll(it.map { obj -> obj as Rule })
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
