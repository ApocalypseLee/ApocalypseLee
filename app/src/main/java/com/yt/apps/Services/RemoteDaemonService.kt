package com.yt.apps.Services

import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.yt.apps.Constants
import com.yt.apps.IMyAidlInterface
import com.yt.apps.R
import com.yt.apps.Utils.NotificationUtils
import java.util.*

class RemoteDaemonService : Service() {
    private val TAG = RemoteDaemonService::class.java.simpleName
    val NOTIFICATION_CHANNEL_ID = RemoteDaemonService::class.java.simpleName
    private var mBinder: MyBinder? = null
    private var timer: Timer? = null
    private var count = 0

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val iMyAidlInterface: IMyAidlInterface = IMyAidlInterface.Stub.asInterface(service)
            try {
                Log.i("RemoteService", "connected with " + iMyAidlInterface.getServiceName())
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (Constants.isOpenServiceDefend == 1) {
                if (Constants.isDebug) {
                    Toast.makeText(
                        this@RemoteDaemonService,
                        "链接断开，重新启动 LocalService",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.i(TAG, "链接断开，重新启动 LocalService")
                startService(Intent(this@RemoteDaemonService, LocalDaemonService::class.java))
                bindService(
                    Intent(this@RemoteDaemonService, LocalDaemonService::class.java),
                    this, BIND_IMPORTANT
                )

//                Intent intent = new Intent(RemoteDaemonService.this, AudioDaemonService.class);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(intent);
//                } else startService(intent);
            }
        }
    }

    fun RemoteDaemonService() {}

    override fun onCreate() {
        super.onCreate()
        getLock(this@RemoteDaemonService)
        if (timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    Log.i("RemoteService", "==" + count++)
                }
            }, 0, 1000)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //前台服务通知
            val title = ""
            val content = ""
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.hint_money2)
            val notification: Notification? = NotificationUtils.getForegroundNotification(
                this@RemoteDaemonService,
                NOTIFICATION_CHANNEL_ID,
                title,
                content,
                bitmap
            )
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Constants.isOpenServiceDefend == 1) {
            if (Constants.isDebug) {
                Toast.makeText(this@RemoteDaemonService, "RemoteService 启动...", Toast.LENGTH_LONG)
                    .show()
            }
            Log.i("RemoteService", "RemoteService 启动...")
            bindService(Intent(this, LocalDaemonService::class.java), connection, BIND_IMPORTANT)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        mBinder = MyBinder()
        return mBinder
    }

    private class MyBinder : IMyAidlInterface.Stub() {

        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {
            TODO("Not yet implemented")
        }

        override fun getServiceName(): String {
            return RemoteDaemonService::class.java.name
        }
    }

    override fun onDestroy() {
        // TODO 自动生成的方法存根
        super.onDestroy()
        releaseLock()
        try {
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
            unbindService(connection)
        } catch (e: Exception) {
            System.exit(0)
        }
    }

    private var mWakeLock: WakeLock? = null

    /**
     *      * 同步方法   得到休眠锁
     *      * @param context
     *      * @return
     *     
     */
    @Synchronized
    private fun getLock(context: Context) {
        if (mWakeLock == null) {
            val mgr = context.getSystemService(POWER_SERVICE) as PowerManager
            mWakeLock = mgr.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                RemoteDaemonService::class.java.name
            )
            mWakeLock!!.setReferenceCounted(true)
            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()
            val hour = c[Calendar.HOUR_OF_DAY]
            if (hour >= 23 || hour <= 6) {
                mWakeLock!!.acquire(5000)
            } else {
                mWakeLock!!.acquire(300000)
            }
        }
        Log.v(TAG, "get lock")
    }

    @Synchronized
    private fun releaseLock() {
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) {
                mWakeLock!!.release()
                Log.v(TAG, "release lock")
            }
            mWakeLock = null
        }
    }
}