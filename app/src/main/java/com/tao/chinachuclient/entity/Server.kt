package com.tao.chinachuclient.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class Server(
    @PrimaryKey @ColumnInfo(name = "chinachuAddress") val chinachuAddress: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "streaming") val streaming: Boolean,
    @ColumnInfo(name = "encStreaming") val encStreaming: Boolean,
    @Embedded val encode: Encode,
    @ColumnInfo(name = "channelIds") val channelIds: String,
    @ColumnInfo(name = "channelNames") val channelNames: String,
    @ColumnInfo(name = "oldCategoryColor") val oldCategoryColor: Boolean
)
