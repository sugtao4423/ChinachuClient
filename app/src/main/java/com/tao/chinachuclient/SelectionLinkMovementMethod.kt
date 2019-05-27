package com.tao.chinachuclient

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView


class SelectionLinkMovementMethod(val context: Context) : ArrowKeyMovementMethod() {

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        if (widget == null || buffer == null || event == null) {
            return super.onTouchEvent(widget, buffer, event)
        }

        val action = event.action

        if (action == MotionEvent.ACTION_UP) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(off, off, URLSpan::class.java)
            if (link.isNotEmpty()) {
                val url = link[0].url
                if (url.contains("https") || url.contains("tel") || url.contains("mailto") || url.contains("http") || url.contains("https") || url.contains("www")) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

}