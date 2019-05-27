package com.tao.chinachuclient

data class Encode(
        val type: String,
        val containerFormat: String,
        val videoCodec: String,
        val audioCodec: String,
        val videoBitrate: String,
        val audioBitrate: String,
        val videoSize: String,
        val frame: String
)

data class Server(
        val chinachuAddress: String,
        val username: String,
        val password: String,
        val streaming: Boolean,
        val encStreaming: Boolean,
        val encode: Encode,
        val channelIds: String,
        val channelNames: String,
        val oldCategoryColor: Boolean
)
