package com.yt.apps.data

data class MemoryEvent(private var memUsed: Double, private var memTotal: Double) {

    fun getUsedPercent(): Int {
        return (memUsed / memTotal * 100).toInt()
    }


}