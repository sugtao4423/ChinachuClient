package com.tao.chinachuclient.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri

class StreamingUtil(private val activity: Activity) {

    private fun startVlc(uri: Uri) = activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
        setPackage("org.videolan.vlc")
        setDataAndTypeAndNormalize(uri, "video/*")
    })

    private fun startMxPro(uri: Uri) = activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
        setPackage("com.mxtech.videoplayer.pro")
        setDataAndTypeAndNormalize(uri, "video/*")
    })

    private fun startMxFree(uri: Uri) = activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
        setPackage("com.mxtech.videoplayer.ad")
        setDataAndTypeAndNormalize(uri, "video/*")
    })

    fun startStreamingApp(uri: Uri) {
        runCatching {
            startVlc(uri)
        }.getOrNull() ?: runCatching {
            startMxPro(uri)
        }.getOrNull() ?: runCatching {
            startMxFree(uri)
        }.getOrNull() ?: activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
