package com.tao.chinachuclient.model

import android.content.Context
import com.tao.chinachuclient.R
import com.tao.chinachuclient.entity.Encode

class EncodeUtil {
    companion object {

        private fun unitBitrate2noUnit(bitrate: String, unitPos: Int): String {
            if (bitrate.isEmpty()) {
                return ""
            }

            var bit = bitrate.toInt()
            bit *= if (unitPos == 0) 1000 else 1000000
            return bit.toString()
        }

        fun getEncodeSetting(
            context: Context,
            typePos: Int,
            containerFormat: String,
            videoCodec: String,
            audioCodec: String,
            videoBitrate: String,
            videoBitrateUnitPos: Int,
            audioBitrate: String,
            audioBitrateUnitPos: Int,
            videoSize: String,
            frame: String
        ): Encode {
            val typeItemArray =
                context.applicationContext.resources.getStringArray(R.array.enc_setting_typeSpinner_item)
            val type = typeItemArray[typePos]!!

            return Encode(
                type,
                containerFormat,
                videoCodec,
                audioCodec,
                unitBitrate2noUnit(videoBitrate, videoBitrateUnitPos),
                unitBitrate2noUnit(audioBitrate, audioBitrateUnitPos),
                videoSize,
                frame
            )
        }
    }
}