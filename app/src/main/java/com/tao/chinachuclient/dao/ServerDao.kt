package com.tao.chinachuclient.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tao.chinachuclient.entity.Server

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers")
    fun getServers(): List<Server>

    @Query("SELECT * FROM servers WHERE chinachuAddress = :chinachuAddress")
    fun findServerByAddress(chinachuAddress: String): Server?

    @Insert
    fun insert(server: Server)

    @Update
    fun update(server: Server)

    @Query("DELETE FROM servers WHERE chinachuAddress = :chinachuAddress")
    fun delete(chinachuAddress: String)

}
