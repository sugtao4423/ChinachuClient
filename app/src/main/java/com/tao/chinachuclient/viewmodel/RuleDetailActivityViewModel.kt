package com.tao.chinachuclient.viewmodel

import android.app.Application
import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App
import com.tao.chinachuclient.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Rule

class RuleDetailActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    var rule: Rule? = null
        set(value) {
            if (field != value) {
                field = value
                refreshRule()
            }
        }

    private val thisRule by lazy { rule!! }

    var rulePosition = -1

    private val _onToast = LiveEvent<String>()
    val onToast: LiveData<String> = _onToast

    private val _toggleProgressDialog = LiveEvent<Unit>()
    val toggleProgressDialog: LiveData<Unit> = _toggleProgressDialog

    private val _actionBarTitle = MutableLiveData<String>()
    val actionBarTitle: LiveData<String> = _actionBarTitle

    private val _ruleInfo = MutableLiveData<Spanned>()
    val ruleInfo: LiveData<Spanned> = _ruleInfo

    private val _onRuleDeleted = LiveEvent<Unit>()
    val onRuleDeleted: LiveData<Unit> = _onRuleDeleted

    private fun Array<String>.convertString(emptyString: String): String =
        if (this.isEmpty()) emptyString else this.joinToString()

    private fun refreshRule() {
        setActionBarTitle()
        setRuleInfo()
    }

    private fun setActionBarTitle() {
        val text = thisRule.reserveTitles.let {
            if (it.isEmpty()) "any" else it[0]
        }
        _actionBarTitle.value = text
    }

    private fun setRuleInfo() {
        val type = thisRule.types.convertString("any")
        val category = thisRule.categories.convertString("any")
        val channel = thisRule.channels.convertString("any")
        val ignoreChannel = thisRule.ignoreChannels.convertString("none")
        val reserveFlag = thisRule.reserveFlags.convertString("any")
        val ignoreFlag = thisRule.ignoreFlags.convertString("none")

        val start = if (thisRule.start == -1) 0 else thisRule.start
        val end = if (thisRule.end == -1) 0 else thisRule.end

        val minMax = thisRule.let {
            if (it.min == -1 && it.max == -1) {
                "all"
            } else {
                "${it.min / 60}〜${it.max / 60}"
            }
        }

        val reserveTitle = thisRule.reserveTitles.convertString("any")
        val ignoreTitle = thisRule.ignoreTitles.convertString("none")
        val reserveDescription = thisRule.reserveDescriptions.convertString("any")
        val ignoreDescription = thisRule.ignoreDescriptions.convertString("none")
        val recordedFormat = thisRule.recordedFormat.ifEmpty { "default" }
        val isDisabled = if (thisRule.isDisabled) "無効" else "有効"

        val infoText = """
            タイプ: $type<br><br>
            ジャンル: $category<br><br>
            対象CH: $channel<br><br>
            無視CH: $ignoreChannel<br><br>
            対象フラグ: $reserveFlag<br><br>
            無視フラグ: $ignoreFlag<br><br>
            時間帯: ${start}〜${end}<br><br>
            長さ(分): $minMax<br><br>
            対象タイトル: $reserveTitle<br><br>
            無視タイトル: $ignoreTitle<br><br>
            対象説明文: $reserveDescription<br><br>
            無視説明文: $ignoreDescription<br><br>
            録画ファイル名フォーマット: $recordedFormat<br><br>
            ルールの状態: $isDisabled
        """.trimIndent()
        _ruleInfo.value = HtmlCompat.fromHtml(infoText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    fun getDeleteRuleDialogMessage() =
        app.resources.getString(
            R.string.delete_rule_dialog_message,
            rulePosition,
            thisRule.reserveTitles.convertString("any")
        )

    fun deleteRule() {
        _toggleProgressDialog.value = Unit
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { app.chinachu.delRule(rulePosition.toString()) }.getOrNull()
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

            _onRuleDeleted.value = Unit
        }
    }
}
