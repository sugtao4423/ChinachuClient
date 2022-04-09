package com.tao.chinachuclient.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.ProgramActivity
import com.tao.chinachuclient.R
import com.tao.chinachuclient.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainList = ListView(this)
        setContentView(mainList)

        if (viewModel.isStartAddServerActivity) {
            replaceToAddServerActivity()
            return
        }

        viewModel.onStartActivityWoExtra.observe(this) {
            startActivityWoExtra(it)
        }
        viewModel.onStartProgramActivity.observe(this) {
            startProgramActivity(it)
        }
        viewModel.changeServerDialogData.observe(this) {
            showChangeServerDialog(it)
        }

        val listItem = resources.getStringArray(R.array.main_list_names)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItem)
        mainList.adapter = adapter
        mainList.onItemClickListener = viewModel.mainOnItemClickListener
    }

    private fun replaceToAddServerActivity() {
        val intent = Intent(this, AddServerActivity::class.java).apply {
            putExtra("startMain", true)
        }
        startActivity(intent)
        finish()
    }

    private fun startActivityWoExtra(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }

    private fun startProgramActivity(adapterPosition: Int) {
        val intent = Intent(this, ProgramActivity::class.java).apply {
            putExtra("type", adapterPosition)
        }
        startActivity(intent)
    }

    private fun showChangeServerDialog(data: MainActivityViewModel.ChangeServerDialogData) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.select_server)
            setSingleChoiceItems(
                data.serverAddresses.toTypedArray(),
                data.currentServerPosition
            ) { dialog, which ->
                val selectedAddress = data.serverAddresses[which]
                viewModel.changeServer(selectedAddress)
                dialog.dismiss()
            }
            setPositiveButton(R.string.cancel, null)
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, Menu.FIRST, Menu.NONE, R.string.change_server)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.settings)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }
}
