package com.yt.apps.Services

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.yt.apps.Constants
import com.yt.apps.IMyAidlInterface
import java.util.*

class LocalDaemonService : Service() {
    private val TAG = LocalDaemonService::class.java.simpleName
    private var count = 0
    private var timer: Timer? = null
    private var mBinder: MyBinder? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val iMyAidlInterface: IMyAidlInterface = IMyAidlInterface.Stub.asInterface(service)
            try {
                Log.i("LocalService", "connected with " + iMyAidlInterface.getServiceName())
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (Constants.isOpenServiceDefend == 1) {
                if (Constants.isDebug) {
                    Toast.makeText(
                        this@LocalDaemonService,
                        "链接断开，重新启动 RemoteService",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.i(TAG, "链接断开，重新启动 RemoteService......")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(
                        Intent(
                            this@LocalDaemonService,
                            RemoteDaemonService::class.java
                        )
                    )
                } else {
                    startService(
                        Intent(
                            this@LocalDaemonService,
                            RemoteDaemonService::class.java
                        )
                    )
                }
                bindService(
                    Intent(this@LocalDaemonService, RemoteDaemonService::class.java),
                    this, BIND_IMPORTANT
                )

//                Intent intent = new Intent(LocalDaemonService.this, AudioDaemonService.class);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(intent);
//                } else startService(intent);
            }
        }
    }

    fun LocalDaemonService() {}

    override fun onCreate() {
        super.onCreate()
        getLock(this@LocalDaemonService)
        if (timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    Log.i(TAG, "--" + count++)
                }
            }, 0, 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Constants.isOpenServiceDefend == 1) {
            Log.i(TAG, "本地服务启动......")
            if (Constants.isDebug) {
                Toast.makeText(this@LocalDaemonService, "本地服务启动", Toast.LENGTH_LONG).show()
            }
            startService(Intent(this@LocalDaemonService, RemoteDaemonService::class.java))
            bindService(
                Intent(this@LocalDaemonService, RemoteDaemonService::class.java),
                connection,
                BIND_IMPORTANT
            )
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
            return LocalDaemonService::class.java.name
        }

    }

    override fun onDestroy() {
        // TODO 自动生成的sss方法存根
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
            mWakeLock =
                mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LocalDaemonService::class.java.name)
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