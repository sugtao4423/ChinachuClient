package com.tao.chinachuclient

import Chinachu4j.Chinachu4j
import android.app.Application
import android.widget.EditText
import android.widget.Spinner

class ApplicationClass : Application() {

    lateinit var chinachu: Chinachu4j
    var streaming: Boolean = false
    var encStreaming: Boolean = false
    var reloadList: Boolean = false

    fun getEncodeSetting(type: Spinner,
                         containerFormat: EditText, videoCodec: EditText, audioCodec: EditText,
                         videoBitrate: EditText, videoBitrateFormat: Spinner,
                         audioBitrate: EditText, audioBitrateFormat: Spinner,
                         videoSize: EditText, frame: EditText): Encode {
        var vb = ""
        var ab = ""
        if (videoBitrate.text.toString().isNotEmpty()) {
            var videoBit = videoBitrate.text.toString().toInt()
            videoBit *= if (videoBitrateFormat.selectedItemPosition == 0) 1000 else 1000000
            vb = videoBit.toString()
        }

        if (audioBitrate.text.toString().isNotEmpty()) {
            var audioBit = audioBitrate.text.toString().toInt()
            audioBit *= if (audioBitrateFormat.selectedItemPosition == 0) 1000 else 1000000
            ab = audioBit.toString()
        }

        return Encode(
                type.selectedItem as String,
                containerFormat.text.toString(),
                videoCodec.text.toString(),
                audioCodec.text.toString(),
                vb,
                ab,
                videoSize.text.toString(),
                frame.text.toString())
    }

}