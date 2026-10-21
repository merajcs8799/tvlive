package com.example.tvlive

import android.content.Context
import org.json.JSONArray

data class Channel(val name: String, val category: String, val url: String)

fun loadChannels(context: Context): List<Channel> {
    val text = context.assets.open("channels.json").bufferedReader().use { it.readText() }
    val arr = JSONArray(text)
    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        Channel(o.getString("name"), o.optString("category", "All"), o.getString("url"))
    }
}
