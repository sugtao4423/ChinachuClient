package com.tao.chinachuclient.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tao.chinachuclient.entity.Server

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers")
    suspend fun getServers(): List<Server>

    @Query("SELECT * FROM servers WHERE chinachuAddress = :chinachuAddress")
    suspend fun findServerByAddress(chinachuAddress: String): Server?

    @Insert
    suspend fun insert(server: Server)

    @Update
    suspend fun update(server: Server)

    @Query("DELETE FROM servers WHERE chinachuAddress = :chinachuAddress")
    suspend fun delete(chinachuAddress: String)

}
