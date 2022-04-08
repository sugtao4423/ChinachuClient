package com.tao.chinachuclient.model

import android.content.Context
import androidx.preference.PreferenceManager

class PrefRepository(context: Context) {
    private val keyChinachuAddress = "chinachuAddress"

    private val pref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun clear() = pref.edit().clear().apply()

    fun putChinachuAddress(address: String) =
        pref.edit().putString(keyChinachuAddress, address).apply()

    fun getChinachuAddress(): String = pref.getString(keyChinachuAddress, "")!!
}
