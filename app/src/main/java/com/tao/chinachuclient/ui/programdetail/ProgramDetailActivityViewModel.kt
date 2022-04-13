package com.tao.chinachuclient.ui.programdetail

import android.app.Application
import android.net.Uri
import android.text.Spanned
import android.view.Menu
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App
import com.tao.chinachuclient.R
import com.tao.chinachuclient.ui.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

class ProgramDetailActivityViewModel(application: Application) : AndroidViewModel(application) {

    val app = getApplication<App>()

    var programType = -1
        set(value) {
            field = value
            _isShowThumbnail.value = when (value) {
                Type.RECORDING, Type.RECORDED -> View.VISIBLE
                else -> View.GONE
            }
        }

    var reserveProgram: Reserve? = null
        set(value) {
            if (field != value) {
                field = value
                refreshProgram()
            }
        }
    var recordedProgram: Recorded? = null
        set(value) {
            if (field != value) {
                field = value
                refreshProgram()
            }
        }
    var program: Program? = null
        set(value) {
            if (field != value) {
                field = value
                refreshProgram()
            }
        }

    private val thisProgram by lazy {
        when (programType) {
            Type.RESERVES -> reserveProgram!!.program
            Type.RECORDED -> recordedProgram!!.program
            else -> program!!
        }
    }

    val programFullTitle by lazy { thisProgram.fullTitle }

    private val _onToast = LiveEvent<String>()
    val onToast: LiveData<String> = _onToast

    private val _toggleProgressDialog = LiveEvent<Unit>()
    val toggleProgressDialog: LiveData<Unit> = _toggleProgressDialog

    private val _actionBarTitle = MutableLiveData<String>()
    val actionBarTitle: LiveData<String> = _actionBarTitle

    private val _programDetail = MutableLiveData<Spanned>()
    val programDetail: LiveData<Spanned> = _programDetail

    private val _programInfo = MutableLiveData<Spanned>()
    val programInfo: LiveData<Spanned> = _programInfo

    private val _isShowThumbnail = MutableLiveData(View.VISIBLE)
    val isShowThumbnail: LiveData<Int> = _isShowThumbnail

    private val thumbnailSize = "1280x720"
    private val _thumbnailBase64 = MutableLiveData<String>()
    val thumbnailBase64: LiveData<String> = _thumbnailBase64
    private var thumbnailPosition = 7

    data class OpenThumbnailDialogData(
        val visibilityPosition: Int,
        val programLength: Int,
        val selectedSecond: Int,
        val resolution: String
    )

    private val _showOpenThumbnailDialog = LiveEvent<OpenThumbnailDialogData>()
    val showOpenThumbnailDialog: LiveData<OpenThumbnailDialogData> = _showOpenThumbnailDialog

    data class ShowImageActivityData(
        val base64: String,
        val programId: String,
        val pos: Int
    )

    private val _startShowImageActivity = LiveEvent<ShowImageActivityData>()
    val startShowImageActivity: LiveData<ShowImageActivityData> = _startShowImageActivity

    private val _startStreamingApp = LiveEvent<Uri>()
    val startStreamingApp: LiveData<Uri> = _startStreamingApp

    data class OperationResultDialogData(
        val title: Int,
        val message: String,
    )

    private val _operationResultDialog = LiveEvent<OperationResultDialogData>()
    val operationResultDialog: LiveData<OperationResultDialogData> = _operationResultDialog

    private fun refreshProgram() {
        setActionBarTitle()
        setProgramDetail()
        setProgramInfo()
        when (programType) {
            Type.RECORDING, Type.RECORDED -> setThumbnailData()
        }
    }

    private fun setActionBarTitle() {
        _actionBarTitle.value = thisProgram.title
    }

    private fun setProgramDetail() {
        var detail = thisProgram.detail.replace("\n", "<br>")
        val m = Pattern.compile("https?://[\\w.\\-/:#?=&;%~+]+").matcher(detail)
        while (m.find()) {
            detail = detail.replace(m.group(), "<a href=\"${m.group()}\">${m.group()}</a>")
        }
        val detailText = """
            <p>
                <strong>フルタイトル</strong><br>
                ${thisProgram.fullTitle}
            </p>
            <p>
                <strong>詳細</strong><br>
                $detail
            </p>
        """.trimIndent()
        _programDetail.value = HtmlCompat.fromHtml(detailText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setProgramInfo() {
        val startDateFormat = SimpleDateFormat("yyyy/MM/dd (E) HH:mm", Locale.JAPANESE)
        val endDateFormat = SimpleDateFormat("HH:mm", Locale.JAPANESE)
        val startDate = startDateFormat.format(Date(thisProgram.start))
        val endDate = endDateFormat.format(Date(thisProgram.end))

        val minute = thisProgram.seconds / 60
        val flag = if (thisProgram.flags.isEmpty()) "なし" else thisProgram.flags.joinToString()
        val infoText = """
            <p>
                $startDate 〜 $endDate (${minute}分間)<br>
                <br>
                ${thisProgram.category} / ${thisProgram.channel.type}: ${thisProgram.channel.name}<br>
                <br>
                フラグ: $flag<br>
                <br>
                id: ${thisProgram.id}
            </p>
        """.trimIndent()
        _programInfo.value = HtmlCompat.fromHtml(infoText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private fun getThumbnailBase64(): String {
        val rawBase64 = when (programType) {
            Type.RECORDING -> app.chinachu.getRecordingImage(thisProgram.id, thumbnailSize)
            Type.RECORDED -> {
                thumbnailPosition = Random.nextInt(thisProgram.seconds) + 1
                app.chinachu.getRecordedImage(thisProgram.id, thumbnailPosition, thumbnailSize)
            }
            else -> throw UnsupportedOperationException()
        }
        return if (rawBase64.startsWith("data:image/jpeg;base64,")) {
            rawBase64.substring(23)
        } else {
            rawBase64
        }
    }

    private fun setThumbnailData() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { getThumbnailBase64() }.getOrNull()
            }
            if (result == null) {
                _onToast.value = app.resources.getString(R.string.error_get_image)
                return@launch
            }
            result.let { _thumbnailBase64.value = it }
        }
    }

    fun onTapImage() {
        val data = OpenThumbnailDialogData(
            if (programType == Type.RECORDING) View.GONE else View.VISIBLE,
            thisProgram.seconds,
            thumbnailPosition,
            thumbnailSize
        )
        _showOpenThumbnailDialog.value = data
    }

    fun openCapture(position: Int, resolution: String) {
        _toggleProgressDialog.value = Unit
        viewModelScope.launch {
            val rawBase64 = withContext(Dispatchers.IO) {
                runCatching {
                    when (programType) {
                        Type.RECORDING -> {
                            app.chinachu.getRecordingImage(thisProgram.id, resolution)
                        }
                        Type.RECORDED -> {
                            app.chinachu.getRecordedImage(thisProgram.id, position, resolution)
                        }
                        else -> throw UnsupportedOperationException()
                    }
                }.getOrNull()
            }
            _toggleProgressDialog.value = Unit
            if (rawBase64 == null) {
                _onToast.value = app.resources.getString(R.string.error_get_image)
                return@launch
            }

            val image64 = if (rawBase64.startsWith("data:image/jpeg;base64,")) {
                rawBase64.substring(23)
            } else {
                rawBase64
            }
            _startShowImageActivity.value = ShowImageActivityData(image64, thisProgram.id, position)
        }
    }

    fun openCaptureThisState() {
        _startShowImageActivity.value =
            ShowImageActivityData(_thumbnailBase64.value!!, thisProgram.id, thumbnailPosition)
    }

    object MenuId {
        const val RESERVE = 1
        const val DELETE_RESERVE = 2
        const val SKIP_RESERVE_RELEASE = 3
        const val SKIP_RESERVE = 4
        const val PLAY_STREAMING = 5
        const val PLAY_ENCODE_STREAMING = 6
        const val DELETE_RECORDED_FILE = 7
    }

    fun onCreateOptionsMenu(menu: Menu) {
        fun addMenu(id: Int, @StringRes title: Int) = menu.add(0, id, Menu.NONE, title)

        when (programType) {
            Type.CHANNEL_SCHEDULE_ACTIVITY, Type.SEARCH_PROGRAM -> {
                addMenu(MenuId.RESERVE, R.string.reserve)
            }
            Type.RESERVES -> {
                if (reserveProgram!!.isManualReserved) {
                    addMenu(MenuId.DELETE_RESERVE, R.string.delete_reserve)
                } else {
                    if (reserveProgram!!.isSkip) {
                        addMenu(MenuId.SKIP_RESERVE_RELEASE, R.string.skip_reserve_release)
                    } else {
                        addMenu(MenuId.SKIP_RESERVE, R.string.skip_reserve)
                    }
                }
            }
            Type.RECORDING, Type.RECORDED -> {
                if (app.currentServer.streaming) {
                    addMenu(MenuId.PLAY_STREAMING, R.string.streaming_play)
                }
                if (app.currentServer.encStreaming) {
                    addMenu(MenuId.PLAY_ENCODE_STREAMING, R.string.streaming_play_encode)
                }
            }
        }
        if (programType == Type.RECORDED) {
            addMenu(MenuId.DELETE_RECORDED_FILE, R.string.delete_recorded_file)
        }
    }

    fun doOptionOperations(menuItemId: Int) {
        val ch = app.chinachu
        _toggleProgressDialog.value = Unit
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    when (menuItemId) {
                        MenuId.RESERVE -> {
                            ch.putReserve(thisProgram.id)
                        }
                        MenuId.DELETE_RESERVE -> {
                            ch.delReserve(thisProgram.id)
                        }
                        MenuId.SKIP_RESERVE_RELEASE -> {
                            ch.reserveUnskip(thisProgram.id)
                        }
                        MenuId.SKIP_RESERVE -> {
                            ch.reserveSkip(thisProgram.id)
                        }
                        MenuId.DELETE_RECORDED_FILE -> {
                            ch.delRecordedFile((thisProgram.id))
                        }
                        else -> throw UnsupportedOperationException()
                    }
                }.getOrNull()
            }
            _toggleProgressDialog.value = Unit

            if (result == null) {
                _onToast.value = app.resources.getString(R.string.error_access)
                return@launch
            }
            if (!result.result) {
                _onToast.value = result.message
                return@launch
            }

            var resultTitle = 0
            var resultMessage = thisProgram.fullTitle
            when (menuItemId) {
                MenuId.RESERVE -> {
                    resultTitle = R.string.done_reserve
                }
                MenuId.DELETE_RESERVE -> {
                    resultTitle = R.string.done_delete_reverse
                    app.reloadList = true
                }
                MenuId.SKIP_RESERVE_RELEASE -> {
                    resultTitle = R.string.done_skip_reserve_release
                    app.reloadList = true
                }
                MenuId.SKIP_RESERVE -> {
                    resultTitle = R.string.done_skip_reserve
                    app.reloadList = true
                }
                MenuId.DELETE_RECORDED_FILE -> {
                    resultTitle = R.string.done_delete_recorded_file
                    resultMessage = thisProgram.fullTitle + "\n\n" +
                            app.resources.getString(R.string.reflect_recorded_list_need_cleanup)
                }
            }
            _operationResultDialog.value = OperationResultDialogData(resultTitle, resultMessage)
        }
    }

    fun startPlayStreamingApp() {
        val uri = when (programType) {
            Type.RECORDING -> Uri.parse(app.chinachu.getNonEncRecordingMovieURL(thisProgram.id))
            Type.RECORDED -> Uri.parse(app.chinachu.getNonEncRecordedMovieURL(thisProgram.id))
            else -> throw UnsupportedOperationException()
        }
        _startStreamingApp.value = uri
    }

    fun startPlayEncodeStreamingActivity() {
        val t = app.currentServer.encode.type
        val params = app.currentServer.encode.let {
            arrayOf(
                it.containerFormat,
                it.videoCodec,
                it.audioCodec,
                it.videoBitrate,
                it.audioBitrate,
                it.videoSize,
                it.frame
            )
        }
        val uri = when (programType) {
            Type.RECORDING -> {
                Uri.parse(app.chinachu.getEncRecordingMovieURL(thisProgram.id, t, params))
            }
            Type.RECORDED -> {
                Uri.parse(app.chinachu.getEncRecordedMovieURL(thisProgram.id, t, params))
            }
            else -> throw UnsupportedOperationException()
        }
        _startStreamingApp.value = uri
    }
}
