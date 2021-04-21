package com.yt.apps.Services

import android.app.ActivityManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.yt.apps.R
import com.yt.apps.Utils.NotificationUtils


class AudioDaemonService : Service() {
    private val TAG = AudioDaemonService::class.java.simpleName
    val NOTIFICATION_CHANNEL_ID = AudioDaemonService::class.java.simpleName
    private var mMediaPlayer: MediaPlayer? = null
    private var thread: Thread? = null
    val MANAGER_NOTIFICATION_ID = 0x1002

    override fun onBind(intent: Intent?): IBinder? {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "$TAG---->onCreate,启动服务")
        val myThread = MyThread(playMusic = { startPlayMusic() })
        thread = Thread(myThread)
        MediaPlayer.create(applicationContext, R.raw.mute).setLooping(true)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//            notificationUtils.createNotificationChannel("channel_2", "audio", NotificationManager.IMPORTANCE_HIGH);
//        }

        //前台服务通知
        val title = ""
        val content = ""
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.hint_bonus)
        val notification: Notification? = NotificationUtils.getForegroundNotification(
            this@AudioDaemonService,
            NOTIFICATION_CHANNEL_ID,
            title,
            content,
            bitmap
        )
        startForeground(MANAGER_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        thread!!.start()
        return START_STICKY
    }

    fun startPlayMusic() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(applicationContext, R.raw.mute)
            LogUtils.d("音乐启动播放,播放对象为： " + mMediaPlayer.hashCode())
        } else {
            LogUtils.d("音乐启动播放,播放对象为： " + mMediaPlayer.hashCode())
        }
        mMediaPlayer!!.start()
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun stopPlayMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            LogUtils.d("音乐停止播放,播放对象为：" + mMediaPlayer.hashCode())
            LogUtils.d("音乐播放器是否在循环：" + mMediaPlayer!!.isLooping)
            LogUtils.d("音乐播放器是否还在播放：" + mMediaPlayer!!.isPlaying)
            mMediaPlayer!!.release()
            LogUtils.d("播放对象销毁,播放对象为：" + mMediaPlayer.hashCode())
            mMediaPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer!!.pause()
        LogUtils.d("恢复播放 时当前播放器对象：" + mMediaPlayer.hashCode())
        stopPlayMusic()
        LogUtils.d("应用播放服务被杀死，正在重启")
        LogUtils.d("目标播放工作线程是否存活：" + thread!!.isAlive)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, AudioDaemonService::class.java))
        } else {
            startService(Intent(applicationContext, AudioDaemonService::class.java))
        }

//        startService(new Intent(getApplicationContext(), LocalDaemonService.class));
    }

    // 服务是否运行
    fun isServiceRunning(context: Context?, serviceName: String): Boolean {
        var isRunning = false
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val lists = am.runningAppProcesses
        for (info in lists) { // 获取运行服务再启动
            println(info.processName)
            if (info.processName == serviceName) {
                isRunning = true
            }
        }
        return isRunning
    }

    internal class MyThread(private val playMusic: () -> Unit) : Runnable {
        override fun run() {
            playMusic()
        }
    }
}