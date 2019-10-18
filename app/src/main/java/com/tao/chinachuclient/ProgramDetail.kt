package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import sugtao4423.library.chinachu4j.ChinachuResponse
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import sugtao4423.support.progressdialog.ProgressDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

class ProgramDetail : AppCompatActivity() {

    private lateinit var program: Program
    private var type: Int = -1
    private lateinit var app: App
    private lateinit var capture: String
    private var randomSecond: Int = 7
    private var reserveIsManualReserved: Boolean = false
    private var reserveIsSkip: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_program_detail)

        app = applicationContext as App
        type = intent.getIntExtra("type", -1)

        program = when (type) {
            Type.RESERVES -> {
                (intent.getSerializableExtra("reserve") as Reserve).let {
                    reserveIsManualReserved = it.isManualReserved
                    reserveIsSkip = it.isSkip
                    it.program
                }
            }
            Type.RECORDED -> {
                (intent.getSerializableExtra("recorded") as Recorded).program
            }
            else -> {
                intent.getSerializableExtra("program") as Program
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = program.title

        var detail = program.detail
        val start = program.start
        val end = program.end
        val category = program.category
        val flags = program.flags
        val channelType = program.channel.type
        val channelName = program.channel.name

        val image = findViewById<ImageView>(R.id.programs_detail_image)
        image.visibility = if (type == Type.RECORDING || type == Type.RECORDED) {
            View.VISIBLE
        } else {
            View.GONE
        }

        detail = detail.replace("\n", "<br>")
        val m = Pattern.compile("http(s)?://[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+").matcher(detail)
        while (m.find()) {
            detail = detail.replace(m.group(), "<a href=\"${m.group()}\">${m.group()}</a>")
        }
        val detailText = "<p><strong>フルタイトル</strong><br>${program.fullTitle}<br></p><p><strong>詳細</strong><br>$detail</p>"

        val detailView = findViewById<TextView>(R.id.program_detail_detail)
        detailView.text = app.fromHtml(detailText)
        detailView.movementMethod = SelectionLinkMovementMethod(this)

        val otherView = findViewById<TextView>(R.id.program_detail_other)

        val startStr = SimpleDateFormat("yyyy/MM/dd (E) HH:mm", Locale.JAPANESE).format(Date(start))
        val endStr = SimpleDateFormat("HH:mm", Locale.JAPANESE).format(Date(end))
        val minute = program.seconds / 60
        val flag = if (flags.isEmpty()) {
            "なし"
        } else {
            flags.joinToString()
        }
        val otherText = "<p>$startStr 〜 $endStr (${minute}分間)<br><br>$category / $channelType: $channelName<br><br>フラグ: $flag<br><br>id: ${program.id}</p>"
        otherView.text = app.fromHtml(otherText)

        if (type == Type.RECORDING || type == Type.RECORDED) {
            object : AsyncTask<Unit, Unit, String?>() {
                override fun doInBackground(vararg params: Unit?): String? {
                    try {
                        if (type == Type.RECORDING)
                            return app.chinachu.getRecordingImage(program.id, "1280x720")
                        if (type == Type.RECORDED) {
                            randomSecond = Random.nextInt(program.seconds) + 1
                            return app.chinachu.getRecordedImage(program.id, randomSecond, "1280x720")
                        }
                    } catch (e: Exception) {
                    }
                    return null
                }

                override fun onPostExecute(result: String?) {
                    if (result == null) {
                        Toast.makeText(this@ProgramDetail, R.string.error_get_image, Toast.LENGTH_SHORT).show()
                        return
                    }
                    val image64 = if (result.startsWith("data:image/jpeg;base64,")) {
                        result.substring(23)
                    } else {
                        result
                    }
                    val decodedString = Base64.decode(image64, Base64.DEFAULT)
                    val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    image.setImageBitmap(img)
                    capture = image64
                }
            }.execute()
        }
    }

    fun imageClick(@Suppress("UNUSED_PARAMETER") v: View) {
        val view = layoutInflater.inflate(R.layout.capture_dialog, null)
        val capPos = view.findViewById<EditText>(R.id.cap_pos)
        val capSize = view.findViewById<EditText>(R.id.cap_size)
        val capSeek = view.findViewById<SeekBar>(R.id.cap_seek)

        if (type == Type.RECORDING) {
            capPos.visibility = View.GONE
            capSeek.visibility = View.GONE
        } else if (type == Type.RECORDED) {
            capPos.setText(randomSecond.toString())
            val textSize = capPos.textSize
            capPos.width = ((program.seconds.toString().count() + 1) * textSize).toInt()
            capSeek.max = program.seconds - 10
            capSeek.progress = randomSecond
            capSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

                override fun onStopTrackingTouch(seekBar: SeekBar) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    capPos.setText(progress.toString())
                }
            })
        }

        AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok) { _, _ ->

                    object : AsyncTask<Unit, Unit, String?>() {
                        private lateinit var progressDialog: ProgressDialog

                        override fun onPreExecute() {
                            progressDialog = ProgressDialog(this@ProgramDetail).apply {
                                setMessage(getString(R.string.loading))
                                isIndeterminate = false
                                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                                setCancelable(true)
                                show()
                            }
                        }

                        override fun doInBackground(vararg params: Unit?): String? {
                            try {
                                return when (type) {
                                    Type.RECORDING -> app.chinachu.getRecordingImage(program.id, capSize.text.toString())
                                    Type.RECORDED -> app.chinachu.getRecordedImage(program.id, capPos.text.toString().toInt(), capSize.text.toString())
                                    else -> null
                                }
                            } catch (e: Exception) {
                            }
                            return null
                        }

                        override fun onPostExecute(result: String?) {
                            progressDialog.dismiss()
                            if (result == null) {
                                Toast.makeText(this@ProgramDetail, R.string.error_get_image, Toast.LENGTH_SHORT).show()
                                return
                            }
                            val image64 = if (result.startsWith("data:image/jpeg;base64,")) {
                                result.substring(23)
                            } else {
                                result
                            }
                            startActivity(Intent(this@ProgramDetail, ShowImage::class.java).also {
                                it.putExtra("base64", image64)
                                it.putExtra("programId", program.id)
                                if (type == Type.RECORDED) {
                                    it.putExtra("pos", capPos.text.toString().toInt())
                                }
                            })

                        }
                    }.execute()
                }
                .setNeutralButton(R.string.zoom_this_state) { _, _ ->
                    startActivity(Intent(this@ProgramDetail, ShowImage::class.java).also {
                        it.putExtra("base64", capture)
                        it.putExtra("programId", program.id)
                        if (type == Type.RECORDED) {
                            it.putExtra("pos", capPos.text.toString().toInt())
                        }
                    })
                }
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) {
            return false
        }

        when (type) {
            Type.CHANNEL_SCHEDULE_ACTIVITY, Type.SEARCH_PROGRAM -> {
                menu.add(0, Menu.FIRST, Menu.NONE, R.string.reserve)
            }
            Type.RESERVES -> {
                if (reserveIsManualReserved) {
                    menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.delete_reserve)
                } else {
                    if (reserveIsSkip) {
                        menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.skip_reserve_release)
                    } else {
                        menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.skip_reserve)
                    }
                }
            }
            Type.RECORDING, Type.RECORDED -> {
                if (app.streaming) {
                    menu.add(0, Menu.FIRST + 2, Menu.NONE, R.string.streaming_play)
                }
                if (app.encStreaming) {
                    menu.add(0, Menu.FIRST + 3, Menu.NONE, R.string.streaming_play_encode)
                }
            }
        }

        if (type == Type.RECORDED) {
            menu.add(0, Menu.FIRST + 4, Menu.NONE, R.string.delete_recorded_file)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) {
            return false
        }

        when (item.itemId) {
            android.R.id.home -> finish()
            Menu.FIRST + 2 -> {
                startActivity(Intent(Intent.ACTION_VIEW, when (type) {
                    Type.RECORDING -> Uri.parse(app.chinachu.getNonEncRecordingMovieURL(program.id))
                    Type.RECORDED -> Uri.parse(app.chinachu.getNonEncRecordedMovieURL(program.id))
                    else -> null
                }))
            }
            Menu.FIRST + 3 -> {
                app.currentServer.encode.let {
                    val t = it.type
                    val params = arrayOf(
                            it.containerFormat,
                            it.videoCodec,
                            it.audioCodec,
                            it.videoBitrate,
                            it.audioBitrate,
                            it.videoSize,
                            it.frame
                    )
                    startActivity(Intent(Intent.ACTION_VIEW, when (type) {
                        Type.RECORDING -> Uri.parse(app.chinachu.getEncRecordingMovieURL(program.id, t, params))
                        Type.RECORDED -> Uri.parse(app.chinachu.getEncRecordedMovieURL(program.id, t, params))
                        else -> null
                    }))
                }
            }
            else -> {
                if (type == Type.CHANNEL_SCHEDULE_ACTIVITY || type == Type.RESERVES || type == Type.RECORDED || type == Type.SEARCH_PROGRAM) {
                    confirm()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirm() {
        val chinachu = app.chinachu

        val before = AlertDialog.Builder(this)
        before.setTitle(when (type) {
            Type.CHANNEL_SCHEDULE_ACTIVITY, Type.SEARCH_PROGRAM -> R.string.is_reserve
            Type.RESERVES -> {
                if (reserveIsManualReserved) {
                    R.string.is_delete_reserve
                } else {
                    if (reserveIsSkip) {
                        R.string.is_skip_reserve_release
                    } else {
                        R.string.is_skip_reserve
                    }
                }
            }
            Type.RECORDED -> R.string.is_delete_recorded_file
            else -> -1
        })
        before.setMessage(program.fullTitle)
        before.setNegativeButton(R.string.cancel, null)
        before.setPositiveButton(R.string.ok) { _, _ ->
            object : AsyncTask<Unit, Unit, ChinachuResponse?>() {
                private lateinit var progressDialog: ProgressDialog

                override fun onPreExecute() {
                    progressDialog = ProgressDialog(this@ProgramDetail).apply {
                        setMessage(getString(R.string.sending))
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(true)
                        show()
                    }
                }

                override fun doInBackground(vararg params: Unit?): ChinachuResponse? {
                    try {
                        return when (type) {
                            Type.CHANNEL_SCHEDULE_ACTIVITY, Type.SEARCH_PROGRAM -> chinachu.putReserve(program.id)
                            Type.RESERVES -> {
                                if (reserveIsManualReserved) {
                                    chinachu.delReserve(program.id)
                                } else {
                                    if (reserveIsSkip) {
                                        chinachu.reserveUnskip(program.id)
                                    } else {
                                        chinachu.reserveSkip(program.id)
                                    }
                                }
                            }
                            Type.RECORDED -> chinachu.delRecordedFile(program.id)
                            else -> null
                        }
                    } catch (e: Exception) {
                    }
                    return null
                }

                override fun onPostExecute(result: ChinachuResponse?) {
                    progressDialog.dismiss()
                    if (result == null) {
                        Toast.makeText(this@ProgramDetail, R.string.error_access, Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (!result.result) {
                        Toast.makeText(this@ProgramDetail, result.message, Toast.LENGTH_LONG).show()
                        return
                    }

                    val after = AlertDialog.Builder(this@ProgramDetail)
                    when (type) {
                        Type.CHANNEL_SCHEDULE_ACTIVITY, Type.SEARCH_PROGRAM -> {
                            after.setTitle(R.string.done_reserve)
                            after.setMessage(program.fullTitle)
                        }
                        Type.RESERVES -> {
                            if (reserveIsManualReserved) {
                                after.setTitle(R.string.done_delete_reverse)
                            } else {
                                if (reserveIsSkip) {
                                    after.setTitle(R.string.done_skip_reserve_release)
                                } else {
                                    after.setTitle(R.string.done_skip_reserve)
                                }
                            }
                            after.setMessage(program.fullTitle)
                            app.reloadList = true
                        }
                        Type.RECORDED -> {
                            after.setTitle(R.string.done_delete_recorded_file)
                            after.setMessage(program.fullTitle + "\n\n" + getString(R.string.reflect_recorded_list_need_cleanup))
                        }
                    }
                    after.show()
                }
            }.execute()
        }
        before.show()
    }

}
