package com.yt.apps.Receivers

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.yt.apps.AdActivity
import com.yt.apps.AlarmActivity
import com.yt.apps.R
import com.yt.apps.Services.FloatWindowService
import com.yt.apps.Services.FloatWindowService.Companion.FLAG_RES_ID
import com.yt.apps.Utils.NotificationUtils
import com.yt.apps.Utils.UIUtils

@Suppress("UNREACHABLE_CODE", "UnusedEquals")
class AlarmReceiver : BroadcastReceiver() {

    private val TAG = BroadcastReceiver::class.java.simpleName
    private var timer = 0
    private val PRIORITY = 30 //百分比
    private val INTERVAL = 30 //分钟

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        //拿到锁屏管理者
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val title = context.getString(R.string.app_name)
        if (Intent.ACTION_SCREEN_OFF == action) {
            if (UIUtils.getIntegerRandomBound(1, 100) <= PRIORITY) {
                startAlarm(context, action, title)
            }
        } else if (Intent.ACTION_TIME_TICK == action && !km.isKeyguardLocked) {
            if (!UIUtils.isOnForground(context)) {
                timer++
                if (timer % INTERVAL == 0) {
                    startFloatWindow(context);
//                    startAd(context, action, title)
//                    startAlarm(context, action, title);
                }
            }
        }
    }


    private fun startFloatWindow(context: Context) {
        val fwIntent = Intent(context.applicationContext, FloatWindowService::class.java)
//        context.stopService(fwIntent);
        fwIntent.action = FloatWindowService.ACTION_FULL_SCREEN_ALARM
        val resID: Int = UIUtils.getIntegerRandomBound(1, 2)
        fwIntent.putExtra(FLAG_RES_ID, resID)
        context.startService(fwIntent)
    }

    private fun startAlarm(context: Context, action: String, title: String) {
        var resID = 0
        if (action == Intent.ACTION_SCREEN_OFF) {
            resID = UIUtils.getIntegerRandomBound(1, 3)
        } else if (action == Intent.ACTION_TIME_TICK) {
            resID = UIUtils.getIntegerRandomBound(1, 2)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            startActivity(context, AlarmActivity::class.java, action, resID)
        } else {
            val notificationUtils = NotificationUtils(context)
            notificationUtils.clearAllNotification()
            val content = context.resources.getString(R.string.hint)
            notificationUtils.sendNotificationFullScreen(
                AlarmActivity::class.java,
                title,
                content,
                action,
                resID
            )
        }
    }


    private fun startAd(context: Context, action: String, title: String) {
        val resID: Int = UIUtils.getIntegerRandomBound(1, 2)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            startActivity(context, AdActivity::class.java, action, resID)
        } else {
            val notificationUtils = NotificationUtils(context)
            notificationUtils.clearAllNotification()
            val content = context.resources.getString(R.string.hint)
            notificationUtils.sendNotificationFullScreen(
                AdActivity::class.java,
                title,
                content,
                action,
                resID
            )
        }
    }

    private fun startActivity(context: Context, cls: Class<*>, action: String, resID: Int) {
        Log.d(TAG, "onReceive: 收到广播")
        Log.d(TAG, action)
        //启动Activity
        val alarmIntent = Intent(context, cls)
        //携带数据
        alarmIntent.putExtra("flag", action)
        alarmIntent.putExtra(FLAG_RES_ID, resID)
        //activity需要新的任务栈
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(alarmIntent)
    }
}