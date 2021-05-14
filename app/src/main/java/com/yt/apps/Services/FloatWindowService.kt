package com.yt.apps.Services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.common.base.Strings
import com.yt.apps.R
import com.yt.apps.Utils.NotificationUtils
import com.yt.apps.Utils.PermissionUtils
import com.yt.apps.Utils.RomUtils
import com.yt.apps.Widgets.*
import java.util.*

class FloatWindowService : Service() {

    val TAG = FloatWindowService::class.java.name
    val NOTIFICATION_CHANNEL_ID: String = FloatWindowService::class.java.getSimpleName()

    companion object {
        const val MANAGER_NOTIFICATION_ID = 0x1001
        const val HANDLER_DETECT_PERMISSION = 0x2001

        const val ACTION_CHECK_PERMISSION_AND_TRY_ADD = "action_check_permission_and_try_add"
        const val ACTION_FULL_SCREEN_TOUCH_ABLE = "action_full_screen_touch_able"
        const val ACTION_FULL_SCREEN_TOUCH_DISABLE = "action_full_screen_touch_disable"
        const val ACTION_NOT_FULL_SCREEN_TOUCH_ABLE = "action_not_full_screen_touch_able"
        const val ACTION_NOT_FULL_SCREEN_TOUCH_DISABLE = "action_not_full_screen_touch_disable"
        const val ACTION_INPUT = "action_input"
        const val ACTION_ANIM = "action_anim"
        const val ACTION_FOLLOW_TOUCH = "action_follow_touch"
        const val ACTION_KILL = "action_kill"
        const val ACTION_FULL_SCREEN_ALARM = "action_full_screen_alarm"

        const val FLAG_RES_ID = "resID"
    }

    private var resId = 0

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val what = msg.what
            when (what) {
                HANDLER_DETECT_PERMISSION -> if (PermissionUtils.checkOPsPermission(
                        applicationContext
                    )
                ) {
                    //对沙雕VIVO机型特殊处理,应用处于后台检查悬浮窗权限成功才能确认真的获取了悬浮窗权限
//                        if (RomUtils.isVivoRom() && AppUtils.isAppForeground()) {
//                            Log.e(TAG, "悬浮窗权限检查成功，但App处于前台状态，特殊机型会允许App获取权限，特殊机型就是指Vivo这个沙雕");
//                            mHandler.sendEmptyMessageDelayed(HANDLER_DETECT_PERMISSION, 500);
//                            return;
//                        }
                    this.removeMessages(HANDLER_DETECT_PERMISSION)
                    Log.e(TAG, "悬浮窗权限检查成功")
                    //                        showFloatPermissionWindow();
                } else {
                    Log.e(TAG, "悬浮窗权限检查失败")
                    sendEmptyMessageDelayed(HANDLER_DETECT_PERMISSION, 500)
                }
            }
        }
    }
    private var mFloatPermissionDetectView: FloatPermissionDetectView? = null
    private var mFullScreenTouchAbleFloatWindow: FullScreenTouchAbleFloatWindow? = null
    private var mFullScreenTouchDisableFloatWindow: FullScreenTouchDisableFloatWindow? = null
    private var mNotFullScreenTouchDisableFloatWindow: NotFullScreenTouchDisableFloatWindow? = null
    private var mInputWindow: InputWindow? = null
    private var animRevealFloatView: AnimRevealFloatView? = null
    private var mFollowTouchView: FollowTouchView? = null
    private var alarmFloatWindow: AlarmFloatWindow? = null


    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        return null
    }

    override fun onCreate() {
        super.onCreate()

        //前台服务通知
        val title = ""
        val content = ""
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.hint_money2)
        val notification: Notification? = NotificationUtils.getForegroundNotification(
            this@FloatWindowService,
            NOTIFICATION_CHANNEL_ID,
            title,
            content,
            bitmap
        )
        startForeground(MANAGER_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = Strings.nullToEmpty(intent.action)
        if (intent.hasExtra(FLAG_RES_ID)) {
            resId = intent.getIntExtra(FLAG_RES_ID, 0)
        }
        when (action) {
            ACTION_CHECK_PERMISSION_AND_TRY_ADD ->                 //对沙雕Vivo做特殊处理
                if (RomUtils.isVivoRom()) {
                    mHandler.sendEmptyMessageDelayed(HANDLER_DETECT_PERMISSION, 1000)
                } else {
                    mHandler.sendEmptyMessage(HANDLER_DETECT_PERMISSION)
                }
            ACTION_FULL_SCREEN_TOUCH_ABLE -> showFullTouchWindow()
            ACTION_FULL_SCREEN_TOUCH_DISABLE -> showFullTouchDisableWindow()
            ACTION_NOT_FULL_SCREEN_TOUCH_ABLE -> showNotFullTouchWindow()
            ACTION_NOT_FULL_SCREEN_TOUCH_DISABLE -> showNotFullTouchDisableWindow()
            ACTION_INPUT -> showInputWindow()
            ACTION_ANIM -> showAnimWindow()
            ACTION_FOLLOW_TOUCH -> showFollowTouch()
            ACTION_KILL -> stopSelf()
            ACTION_FULL_SCREEN_ALARM -> showAlarmFloatWindow()
        }
        return START_REDELIVER_INTENT
    }

    private fun showFollowTouch() {
        dismissFollowTouch()
        mFollowTouchView = FollowTouchView(this@FloatWindowService, percent = resId)
        mFollowTouchView!!.show()
    }

    private fun dismissFollowTouch() {
        if (mFollowTouchView != null) {
            mFollowTouchView!!.remove()
            mFollowTouchView = null
        }
    }

    private fun showAnimWindow() {
        dismissAnimRevealFloatView()
        animRevealFloatView = AnimRevealFloatView(this@FloatWindowService)
        animRevealFloatView!!.show()
    }

    override fun onDestroy() {
        if (mFloatPermissionDetectView != null) {
            mFloatPermissionDetectView!!.remove()
            mFloatPermissionDetectView = null
        }
        if (mFullScreenTouchAbleFloatWindow != null) {
            mFullScreenTouchAbleFloatWindow!!.remove()
            mFullScreenTouchAbleFloatWindow = null
        }
        if (alarmFloatWindow != null) {
            alarmFloatWindow!!.remove()
            alarmFloatWindow = null
        }
        if (mFullScreenTouchDisableFloatWindow != null) {
            mFullScreenTouchDisableFloatWindow!!.remove()
            mFullScreenTouchDisableFloatWindow = null
        }
        if (mNotFullScreenTouchDisableFloatWindow != null) {
            mNotFullScreenTouchDisableFloatWindow!!.remove()
            mNotFullScreenTouchDisableFloatWindow = null
        }
        if (mInputWindow != null) {
            mInputWindow!!.remove()
            mInputWindow = null
        }
        dismissAnimRevealFloatView()
        dismissFollowTouch()
        super.onDestroy()
    }

    private fun dismissAnimRevealFloatView() {
        if (animRevealFloatView != null) {
            animRevealFloatView!!.remove()
            animRevealFloatView = null
        }
    }

    @Synchronized
    private fun showFloatPermissionWindow() {
        if (mFloatPermissionDetectView != null) {
            mFloatPermissionDetectView!!.remove()
            mFloatPermissionDetectView = null
        }
        mFloatPermissionDetectView = FloatPermissionDetectView(applicationContext)
        mFloatPermissionDetectView!!.show()
    }

    private fun showFullTouchWindow() {
        if (mFullScreenTouchAbleFloatWindow != null) {
            mFullScreenTouchAbleFloatWindow!!.remove()
            mFullScreenTouchAbleFloatWindow = null
        }
        mFullScreenTouchAbleFloatWindow = FullScreenTouchAbleFloatWindow(applicationContext)
        mFullScreenTouchAbleFloatWindow!!.show()
    }


    private fun showAlarmFloatWindow() {
        if (alarmFloatWindow != null) {
            alarmFloatWindow!!.remove()
            alarmFloatWindow = null
        }
        alarmFloatWindow = AlarmFloatWindow(applicationContext)
        alarmFloatWindow!!.setBG(if (resId == 1) R.drawable.alarm_iphone else R.drawable.alarm_money)
        alarmFloatWindow!!.show()
    }

    private fun showFullTouchDisableWindow() {
        if (mFullScreenTouchDisableFloatWindow != null) {
            mFullScreenTouchDisableFloatWindow!!.remove()
            mFullScreenTouchDisableFloatWindow = null
        }
        mFullScreenTouchDisableFloatWindow = FullScreenTouchDisableFloatWindow(applicationContext)
        mFullScreenTouchDisableFloatWindow!!.show()
    }

    private fun showNotFullTouchWindow() {
        showFloatPermissionWindow()
    }

    private fun showNotFullTouchDisableWindow() {
        if (mNotFullScreenTouchDisableFloatWindow != null) {
            mNotFullScreenTouchDisableFloatWindow!!.remove()
            mNotFullScreenTouchDisableFloatWindow = null
        }
        mNotFullScreenTouchDisableFloatWindow =
            NotFullScreenTouchDisableFloatWindow(applicationContext)
        mNotFullScreenTouchDisableFloatWindow!!.show()
    }

    private fun showInputWindow() {
        if (mInputWindow != null) {
            mInputWindow!!.remove()
            mInputWindow = null
        }
        mInputWindow = InputWindow(applicationContext)
        mInputWindow!!.show()
    }
}