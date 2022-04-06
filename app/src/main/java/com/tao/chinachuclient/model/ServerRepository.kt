package com.tao.chinachuclient.model

import com.tao.chinachuclient.dao.ServerDao
import com.tao.chinachuclient.entity.Server

class ServerRepository(private val serverDao: ServerDao) {

    suspend fun getAll(): List<Server> = serverDao.getServers()

    suspend fun findByAddress(chinachuAddress: String): Server? =
        serverDao.findServerByAddress(chinachuAddress)

    suspend fun isExists(chinachuAddress: String): Boolean = findByAddress(chinachuAddress) != null

    suspend fun insert(server: Server) = serverDao.insert(server)

    suspend fun update(server: Server) = serverDao.update(server)

    suspend fun delete(chinachuAddress: String) = serverDao.delete(chinachuAddress)

    private suspend fun updateColumn(
        chinachuAddress: String,
        streaming: Boolean? = null,
        encStreaming: Boolean? = null,
        oldCategoryColor: Boolean? = null
    ) {
        findByAddress(chinachuAddress)?.let {
            val newServer = it.copy(
                streaming = streaming ?: it.streaming,
                encStreaming = encStreaming ?: it.encStreaming,
                oldCategoryColor = oldCategoryColor ?: it.oldCategoryColor
            )
            update(newServer)
        }
    }

    suspend fun updateStreaming(newValue: Boolean, chinachuAddress: String) =
        updateColumn(chinachuAddress, streaming = newValue)

    suspend fun updateEncStreaming(newValue: Boolean, chinachuAddress: String) =
        updateColumn(chinachuAddress, encStreaming = newValue)

    suspend fun updateOldCategoryColor(newValue: Boolean, chinachuAddress: String) =
        updateColumn(chinachuAddress, oldCategoryColor = newValue)

}