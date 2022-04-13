package com.tao.chinachuclient.ui.preference

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tao.chinachuclient.R
import com.tao.chinachuclient.ui.addserver.AddServerActivity
import com.tao.chinachuclient.ui.setting.SettingActivity

class PreferenceFragment : PreferenceFragmentCompat() {

    val viewModel: PreferenceFragmentViewModel by viewModels()

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
        if (activity == null) {
            return
        }

        viewModel.showCheckSettingDialog.observe(this) {
            showCheckSettingDialog()
        }
        viewModel.showDeleteServerDialog.observe(this) {
            showDeleteServerDialog(it)
        }
        viewModel.onToast.observe(this) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        val initData = viewModel.getInitData()

        findPreference<CheckBoxPreference>("streaming")?.also {
            it.isChecked = initData.isStreaming
            it.setOnPreferenceChangeListener { _, newValue ->
                viewModel.updateStreaming(newValue)
                true
            }
        }
        findPreference<CheckBoxPreference>("encStreaming")?.also {
            it.isChecked = initData.isEncStreaming
            it.setOnPreferenceChangeListener { _, newValue ->
                viewModel.updateEncStreaming(newValue)
                true
            }
        }
        findPreference<CheckBoxPreference>("oldCategoryColor")?.also {
            it.isChecked = initData.isOldCategoryColor
            it.setOnPreferenceChangeListener { _, newValue ->
                viewModel.updateOldCategoryColor(newValue)
                true
            }
        }

        findPreference<Preference>("addServer")?.setOnPreferenceClickListener {
            startActivity(Intent(activity, AddServerActivity::class.java))
            false
        }
        findPreference<Preference>("settingActivity")?.setOnPreferenceClickListener {
            startActivity(Intent(activity, SettingActivity::class.java))
            false
        }
        findPreference<Preference>("delServer")?.setOnPreferenceClickListener {
            viewModel.showDeleteServerDialog()
            false
        }
    }

    private fun showCheckSettingDialog() {
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

    private fun showDeleteServerDialog(serverAddresses: List<String>) {
        AlertDialog.Builder(activity).also { d1 ->
            d1.setTitle(R.string.choose_delete_server)
            d1.setItems(serverAddresses.toTypedArray()) { _, which ->
                val selectedServerAddress = serverAddresses[which]
                AlertDialog.Builder(activity).also { d2 ->
                    d2.setTitle(R.string.confirm_delete)
                    d2.setMessage(getString(R.string.is_delete_server_below, selectedServerAddress))
                    d2.setNegativeButton(R.string.cancel, null)
                    d2.setPositiveButton(R.string.ok) { _, _ ->
                        viewModel.deleteServer(selectedServerAddress)
                    }
                    d2.show()
                }
            }
            d1.show()
        }
    }
}
