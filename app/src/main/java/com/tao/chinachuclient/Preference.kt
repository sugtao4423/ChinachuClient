package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.tao.chinachuclient.view.AddServerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Preference : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, PreferencesFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.Main).launch {
            (applicationContext as App).reloadCurrentServer()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            if (context == null) {
                activity?.finish()
                return
            }
            setPreferencesFromResource(R.xml.preference, rootKey)

            val serverRepository = (requireContext().applicationContext as App).serverRepository
            val currentServer = (requireContext().applicationContext as App).currentServer

            val checkStreaming = findPreference<CheckBoxPreference>("streaming")!!
            val checkEncode = findPreference<CheckBoxPreference>("encStreaming")!!
            val oldCateColor = findPreference<CheckBoxPreference>("oldCategoryColor")!!

            val addServer = findPreference<Preference>("addServer")!!
            val settingActivity = findPreference<Preference>("settingActivity")!!
            val delServer = findPreference<Preference>("delServer")!!

            checkStreaming.isChecked = currentServer.streaming
            checkStreaming.setOnPreferenceChangeListener { _, newValue ->
                CoroutineScope(Dispatchers.Main).launch {
                    serverRepository.updateStreaming(newValue as Boolean, currentServer.chinachuAddress)
                }
                true
            }

            checkEncode.isChecked = currentServer.encStreaming
            checkEncode.setOnPreferenceChangeListener { _, newValue ->
                CoroutineScope(Dispatchers.Main).launch {
                    serverRepository.updateEncStreaming(newValue as Boolean, currentServer.chinachuAddress)
                }

                if (newValue as Boolean) {
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
            oldCateColor.setOnPreferenceChangeListener { _, newValue ->
                CoroutineScope(Dispatchers.Main).launch {
                    serverRepository.updateOldCategoryColor(newValue as Boolean, currentServer.chinachuAddress)
                }
                true
            }

            addServer.setOnPreferenceClickListener {
                startActivity(Intent(activity, AddServerActivity::class.java))
                false
            }

            settingActivity.setOnPreferenceClickListener {
                startActivity(Intent(activity, SettingActivity::class.java))
                false
            }

            delServer.setOnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val address = withContext(Dispatchers.IO) { serverRepository.getAll() } .map { it.chinachuAddress }
                    AlertDialog.Builder(activity)
                        .setTitle(R.string.choose_delete_server)
                        .setItems(address.toTypedArray()) { _, which ->
                            val selectedServerAddress = address[which]
                            AlertDialog.Builder(activity)
                                    .setTitle(R.string.confirm_delete)
                                    .setMessage(getString(R.string.is_delete_server_below) + "\n" + selectedServerAddress)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.ok) { _, _ ->
                                        CoroutineScope(Dispatchers.Main).launch {
                                            serverRepository.delete(selectedServerAddress)
                                            val servers = withContext(Dispatchers.IO) { serverRepository.getAll() }
                                            if (servers.isEmpty()) {
                                                PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit().clear().apply()
                                            } else {
                                                (requireContext().applicationContext as App).changeCurrentServer(servers[0])
                                            }
                                            Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .show()
                        }
                        .show()
                }
                false
            }
        }
    }

}