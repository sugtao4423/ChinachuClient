package com.tao.chinachuclient.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tao.chinachuclient.dao.ServerDao
import com.tao.chinachuclient.entity.Server

@Database(entities = [Server::class], version = 3, exportSchema = false)
abstract class ServerRoomDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerDao

    companion object {
        @Volatile
        private var INSTANCE: ServerRoomDatabase? = null

        fun getDatabase(context: Context): ServerRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ServerRoomDatabase::class.java,
                    "servers"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
