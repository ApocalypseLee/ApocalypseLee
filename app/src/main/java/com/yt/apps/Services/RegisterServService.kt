package com.yt.apps.Services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.yt.apps.Receivers.AlarmReceiver

class RegisterServService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        registerAlarm()
    }

    protected fun registerAlarm() {
        val alarmReceiver = AlarmReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF) //熄屏
        filter.addAction(Intent.ACTION_TIME_TICK) //系统时间过去一分钟
        registerReceiver(alarmReceiver, filter)
    }
}