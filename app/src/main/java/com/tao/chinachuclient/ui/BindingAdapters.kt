package com.tao.chinachuclient.ui

import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

@BindingAdapter("colorSchemeResources")
fun bindRefreshColor(swipeRefreshLayout: SwipeRefreshLayout, colorResIds: IntArray) {
    swipeRefreshLayout.setColorSchemeColors(*colorResIds)
}
