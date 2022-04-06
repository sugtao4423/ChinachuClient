package com.tao.chinachuclient.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tao.chinachuclient.dao.ServerDao
import com.tao.chinachuclient.entity.Server

@Database(entities = [Server::class], version = 4, exportSchema = false)
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
                ).addMigrations(migrate3to4()).build()
                INSTANCE = instance
                instance
            }
        }

        private fun migrate3to4(): Migration {
            val createTableV4 = """
                CREATE TABLE servers (
                    chinachuAddress TEXT NOT NULL PRIMARY KEY, username TEXT NOT NULL, password TEXT NOT NULL,
                    streaming INTEGER NOT NULL, encStreaming INTEGER NOT NULL, type TEXT NOT NULL,
                    containerFormat TEXT NOT NULL, videoCodec TEXT NOT NULL, audioCodec TEXT NOT NULL,
                    videoBitrate TEXT NOT NULL, audioBitrate TEXT NOT NULL, videoSize TEXT NOT NULL,
                    frame TEXT NOT NULL, channelIds TEXT NOT NULL, channelNames TEXT NOT NULL, oldCategoryColor INTEGER NOT NULL)
            """
            val copyTableV4 = """
                INSERT INTO servers SELECT chinachuAddress, username, password,
                CASE WHEN LOWER(streaming) = 'true' THEN 1 ELSE 0 END,
                CASE WHEN LOWER(encStreaming) = 'true' THEN 1 ELSE 0 END,
                type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame, channelIds, channelNames,
                CASE WHEN LOWER(oldCategoryColor) = 'true' THEN 1 ELSE 0 END FROM old_servers
            """

            return object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE servers RENAME TO old_servers")
                    database.execSQL(createTableV4)
                    database.execSQL(copyTableV4)
                }
            }
        }
    }
}
