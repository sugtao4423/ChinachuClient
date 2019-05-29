package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast

class Preference : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, MyPreferencesFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        (applicationContext as ApplicationClass).reloadCurrentServer()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        class MyPreferencesFragment : PreferenceFragment() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                addPreferencesFromResource(R.xml.preference)

                val currentServer = (activity.applicationContext as ApplicationClass).currentServer

                val checkStreaming = findPreference("streaming") as CheckBoxPreference
                val checkEncode = findPreference("encStreaming") as CheckBoxPreference
                val oldCateColor = findPreference("oldCategoryColor") as CheckBoxPreference

                val addServer = findPreference("addServer")
                val settingActivity = findPreference("settingActivity")
                val delServer = findPreference("delServer")

                checkStreaming.isChecked = currentServer.streaming
                checkStreaming.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                    val sql = "UPDATE servers SET streaming = ? WHERE chinachuAddress = ?"
                    val bindArgs = arrayOf(
                            (newValue as Boolean).toString(),
                            currentServer.chinachuAddress
                    )
                    ServerSQLHelper(activity).writableDatabase.compileStatement(sql).apply {
                        bindAllArgsAsStrings(bindArgs)
                        execute()
                        close()
                    }
                    true
                }

                checkEncode.isChecked = currentServer.encStreaming
                checkEncode.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                    val sql = "UPDATE servers SET encStreaming = ? WHERE chinachuAddress = ?"
                    val bindArgs = arrayOf(
                            (newValue as Boolean).toString(),
                            currentServer.chinachuAddress
                    )
                    ServerSQLHelper(activity).writableDatabase.compileStatement(sql).apply {
                        bindAllArgsAsStrings(bindArgs)
                        execute()
                        close()
                    }

                    if (newValue.toString().toBoolean()) {
                        AlertDialog.Builder(activity).apply {
                            setTitle(R.string.confirm_settings)
                            setMessage(R.string.plz_use_after_confirm_settings)
                            setNegativeButton(R.string.cancel, null)
                            setPositiveButton(R.string.ok) { _, _ ->
                                startActivity(Intent(activity, SettingActivity::class.java))
                            }
                            show()
                        }
                    }
                    true
                }

                oldCateColor.isChecked = currentServer.oldCategoryColor
                oldCateColor.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                    val sql = "UPDATE servers SET oldCategoryColor = ? WHERE chinachuAddress = ?"
                    val bindArgs = arrayOf(
                            (newValue as Boolean).toString(),
                            currentServer.chinachuAddress
                    )
                    ServerSQLHelper(activity).writableDatabase.compileStatement(sql).apply {
                        bindAllArgsAsStrings(bindArgs)
                        execute()
                        close()
                    }
                    true
                }

                addServer.onPreferenceClickListener = OnPreferenceClickListener {
                    startActivity(Intent(activity, AddServer::class.java))
                    false
                }

                settingActivity.onPreferenceClickListener = OnPreferenceClickListener {
                    startActivity(Intent(activity, SettingActivity::class.java))
                    false
                }

                delServer.onPreferenceClickListener = OnPreferenceClickListener {
                    val db = ServerSQLHelper(activity).writableDatabase
                    val address = arrayListOf<String>()
                    val rowid = arrayListOf<String>()
                    val result = db.rawQuery("SELECT chinachuAddress, ROWID FROM servers", null)
                    var mov = result.moveToFirst()
                    while (mov) {
                        address.add(result.getString(0))
                        rowid.add(result.getString(1))
                        mov = result.moveToNext()
                    }
                    result.close()
                    AlertDialog.Builder(activity)
                            .setTitle(R.string.choose_delete_server)
                            .setItems(address.toTypedArray()) { _, which ->
                                AlertDialog.Builder(activity)
                                        .setTitle(R.string.confirm_delete)
                                        .setMessage(getString(R.string.is_delete_server_below) + "\n" + address[which])
                                        .setNegativeButton(R.string.cancel, null)
                                        .setPositiveButton(R.string.ok) { _, _ ->
                                            val delRowid = rowid[which]
                                            db.execSQL("DELETE from servers WHERE ROWID=$delRowid")
                                            val dbUtils = DBUtils(activity)
                                            val servers = dbUtils.getServers()
                                            dbUtils.close()
                                            if (servers.isEmpty()) {
                                                PreferenceManager.getDefaultSharedPreferences(activity).edit().clear().commit()
                                            } else {
                                                (activity.applicationContext as ApplicationClass).changeCurrentServer(servers[0])
                                            }
                                            Toast.makeText(activity, R.string.deleted, Toast.LENGTH_SHORT).show()
                                        }
                                        .show()
                            }
                            .show()
                    false
                }
            }
        }

    }

}