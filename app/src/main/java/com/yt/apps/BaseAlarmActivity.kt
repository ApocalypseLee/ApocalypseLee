package com.yt.apps

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

abstract class BaseAlarmActivity : Activity() {
    private var flag = "null"
    open var resId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val win = window
        win.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        onInit()
    }

    protected abstract fun onInit()

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * 这个方法是当这个activity没有销毁的时候，人为的按下锁屏键，然后再启动这个Activity的时候会去调用
     *
     * @param intent
     */
    override fun onNewIntent(intent: Intent?) {
        Log.i(ContentValues.TAG, "onNewIntent: 调用")
        initNewIntent(intent)
    }

    protected abstract fun initNewIntent(intent: Intent?)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    protected open fun getTextRes(id: Int): TextView? {
        return if (id > 0) {
            findViewById(id)
        } else null
    }

    protected open fun getImageRes(id: Int): ImageView? {
        return if (id > 0) {
            findViewById(id)
        } else null
    }

    open fun getFlag(): String? {
        return flag
    }

    open fun setFlag(flag: String?) {
        if (flag != null) {
            this.flag = flag
        }
    }


    /**
     * @param msg
     */
    protected open fun showTip(msg: String?) {
        if (Constants.isDebug) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 当前Activity是否需要对本次 onActivityStarted、onActivityStopped 生命周期进行监听统计
     *
     * @return 默认都需要统计
     */
    open fun needStatistics(isOnStartCall: Boolean): Boolean {
        return true
    }
}