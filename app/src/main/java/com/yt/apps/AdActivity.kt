package com.yt.apps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.PowerManager

class AdActivity :BaseAlarmActivity() {
    override fun onInit() {
        setContentView(R.layout.ad)
        setFlag(intent.getStringExtra("flag"))
        resId = intent.getIntExtra("resId", 0)
    }

    @SuppressLint("WakelockTimeout")
    override fun initNewIntent(intent: Intent?) {
        setFlag(intent?.getStringExtra("flag"))
        resId = intent?.getIntExtra("resId", 0)!!
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isScreenOn && getFlag().equals(Intent.ACTION_TIME_TICK)) {
            //点亮屏幕
            @SuppressLint("InvalidWakeLockTag") val wl = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                        or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright"
            )
            wl.acquire()
            wl.release()
        }
    }
}