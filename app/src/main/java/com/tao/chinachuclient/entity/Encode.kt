package com.tao.chinachuclient.entity

import androidx.room.ColumnInfo

data class Encode(
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "containerFormat") val containerFormat: String,
    @ColumnInfo(name = "videoCodec") val videoCodec: String,
    @ColumnInfo(name = "audioCodec") val audioCodec: String,
    @ColumnInfo(name = "videoBitrate") val videoBitrate: String,
    @ColumnInfo(name = "audioBitrate") val audioBitrate: String,
    @ColumnInfo(name = "videoSize") val videoSize: String,
    @ColumnInfo(name = "frame") val frame: String,
)
