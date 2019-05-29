package com.tao.chinachuclient

import Chinachu4j.ChinachuResponse
import Chinachu4j.Rule
import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import sugtao4423.support.progressdialog.ProgressDialog

class RuleDetail : AppCompatActivity() {

    private lateinit var reserveTitle: String
    private lateinit var position: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_detail)

        position = intent.getStringExtra("position")
        val rule = intent.getSerializableExtra("rule") as Rule

        supportActionBar?.title = if (rule.reserveTitles.isEmpty()) {
            "any"
        } else {
            rule.reserveTitles[0]
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val textView = findViewById<TextView>(R.id.rule_list_text)
        textView.text = Html.fromHtml(rule.run {
            val type = if (types.isEmpty()) "any" else types.joinToString()
            val category = if (categories.isEmpty()) "any" else categories.joinToString()
            val channel = if (channels.isEmpty()) "any" else channels.joinToString()
            val ignoreChannel = if (ignoreChannels.isEmpty()) "none" else ignoreChannels.joinToString()
            val reserveFlag = if (reserveFlags.isEmpty()) "any" else reserveFlags.joinToString()
            val ignoreFlag = if (ignoreFlags.isEmpty()) "none" else ignoreFlags.joinToString()

            val startEnd = let {
                val start = if (it.start == -1) 0 else it.start
                val end = if (it.end == -1) 0 else it.end
                "$start〜$end"
            }
            val minMax = let {
                if (it.min == -1 && it.max == -1) {
                    "all"
                } else {
                    "${min / 60}〜${max / 60}"
                }
            }

            reserveTitle = if (reserveTitles.isEmpty()) "any" else reserveTitles.joinToString()
            val ignoreTitle = if (ignoreTitles.isEmpty()) "none" else ignoreTitles.joinToString()
            val reserveDescription = if (reserveDescriptions.isEmpty()) "any" else reserveDescriptions.joinToString()
            val ignoreDescription = if (ignoreDescriptions.isEmpty()) "none" else ignoreDescriptions.joinToString()
            val recordedFormat = if (recordedFormat.isEmpty()) "default" else recordedFormat
            val isDisabled = if (isDisabled) "無効" else "有効"

            "タイプ: $type<br><br>ジャンル: $category<br><br>対象CH: $channel" +
                    "<br><br>無視CH: $ignoreChannel<br><br>対象フラグ: $reserveFlag" +
                    "<br><br>無視フラグ: $ignoreFlag<br><br>時間帯: $startEnd" +
                    "<br><br>長さ(分): $minMax<br><br>対象タイトル: $reserveTitle" +
                    "<br><br>無視タイトル: $ignoreTitle<br><br>対象説明文: $reserveDescription" +
                    "<br><br>無視説明文: $ignoreDescription<br><br>録画ファイル名フォーマット: $recordedFormat<br><br>ルールの状態: $isDisabled"
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, Menu.NONE, R.string.delete_rule)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            Menu.FIRST -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.is_delete)
                        .setMessage(getString(R.string.rule_number) + position + "\n" + getString(R.string.target_title_to) + reserveTitle)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            object : AsyncTask<Unit, Unit, ChinachuResponse?>() {
                                private lateinit var progressDialog: ProgressDialog

                                override fun onPreExecute() {
                                    progressDialog = ProgressDialog(this@RuleDetail).apply {
                                        setMessage(getString(R.string.sending))
                                        isIndeterminate = false
                                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                                        setCancelable(true)
                                        show()
                                    }
                                }

                                override fun doInBackground(vararg params: Unit?): ChinachuResponse? {
                                    try {
                                        return (applicationContext as ApplicationClass).chinachu.delRule(position)
                                    } catch (e: Exception) {
                                    }
                                    return null
                                }

                                override fun onPostExecute(result: ChinachuResponse?) {
                                    progressDialog.dismiss()
                                    if (result == null) {
                                        Toast.makeText(this@RuleDetail, R.string.error_access, Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    if (!result.result) {
                                        Toast.makeText(this@RuleDetail, result.message, Toast.LENGTH_LONG).show()
                                        return
                                    }

                                    AlertDialog.Builder(this@RuleDetail)
                                            .setTitle(R.string.done_delete)
                                            .setMessage(R.string.back_activity_must_list_refresh)
                                            .setNegativeButton(R.string.cancel, null)
                                            .setPositiveButton(R.string.ok) { _, _ ->
                                                finish()
                                            }
                                            .show()
                                }
                            }.execute()
                        }
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}