package com.tao.chinachuclient.model

import com.tao.chinachuclient.dao.ServerDao
import com.tao.chinachuclient.entity.Server

class ServerRepository(private val serverDao: ServerDao) {

    fun getAll(): List<Server> = serverDao.getServers()

    fun findByAddress(chinachuAddress: String): Server? =
        serverDao.findServerByAddress(chinachuAddress)

    fun isExists(chinachuAddress: String): Boolean = findByAddress(chinachuAddress) != null

    fun insert(server: Server) = serverDao.insert(server)

    fun update(server: Server) = serverDao.update(server)

    fun delete(chinachuAddress: String) = serverDao.delete(chinachuAddress)

    private fun updateColumn(
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

    fun updateStreaming(newValue: Boolean, chinachuAddress: String) =
        updateColumn(chinachuAddress, streaming = newValue)

    fun updateEncStreaming(newValue: Boolean, chinachuAddress: String) =
        updateColumn(chinachuAddress, encStreaming = newValue)

    fun updateOldCategoryColor(newValue: Boolean, chinachuAddress: String) =
        updateColumn(chinachuAddress, oldCategoryColor = newValue)

}