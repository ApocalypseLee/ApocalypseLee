package com.yt.apps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AbsListView.OnScrollListener
import android.widget.AbsListView.OnScrollListener.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.yt.apps.Services.*
import com.yt.apps.Utils.NotificationUtils
import com.yt.apps.Utils.PermissionUtils
import com.yt.apps.Utils.SystemProperties
import com.yt.apps.Widgets.DetailAdapter
import com.yt.apps.data.MemoryEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: ListView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()
    private lateinit var detailAdapter: DetailAdapter
    private lateinit var mActivity: FullscreenActivity
    private var run = false

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mActivity = this
        EventBus.getDefault().register(this)
        // 启动双进程保活机制
        startBackService()
        initTask()
        initContent()

        checkFWPermission()
//        val intent = Intent(this, SplashActivity::class.java)
//        startActivityForResult(intent, 1)
        initFloatBubble()

        run = true
        memHandler.postDelayed(task, 1000)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initContent() {
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content)
        fullscreenContent.setOnScrollListener(scrollListener)
        detailAdapter = DetailAdapter(this, applicationContext)
        detailAdapter.setContent()
        fullscreenContent.adapter = detailAdapter

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById<Button>(R.id.dummy_button).setOnTouchListener(delayHideTouchListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode > 0) {
            checkFWPermission()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        endAllService() // 结束双进程守护
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot) {
            return
        }
    }

    val scrollListener: OnScrollListener = object : OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            when (scrollState) {
                SCROLL_STATE_FLING -> {
                    toggle()
                }
                SCROLL_STATE_IDLE -> {
                    toggle()
                }
                SCROLL_STATE_TOUCH_SCROLL -> {
                    toggle()
                }
            }
        }

        override fun onScroll(
            view: AbsListView,
            firstVisibleItem: Int,
            visibleItemCount: Int,
            totalItemCount: Int
        ) {
        }
    }

    private fun refreshList() {
        var contentView: MutableList<Int> = ArrayList()
        contentView.add(0)
        contentView.add(1)
        contentView.add(2)
        detailAdapter.refreshAdapter(contentView)
    }

    private fun toggle() {
        if (isFullscreen) {
            refreshList()
            hide()
        } else {
            refreshList()
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    /**
     * 当前Activity是否需要对本次 onActivityStarted、onActivityStopped 生命周期进行监听统计
     *
     * @return 默认都需要统计
     */
    fun needStatistics(isOnStartCall: Boolean): Boolean {
        return true
    }

    private fun initFloatBubble() {
        val fwIntent = Intent(this, FloatWindowService::class.java)
        fwIntent.action = FloatWindowService.ACTION_FOLLOW_TOUCH
        fwIntent.putExtra(
            FloatWindowService.FLAG_RES_ID,
            SystemProperties.getUsedPercentValueInt(this)
        )
        startService(fwIntent)
    }

    protected fun initTask() {
        val register = Intent(this, RegisterServService::class.java)
        startService(register)
        val audio = Intent(this, AudioDaemonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(audio)
        } else startService(audio)


        val notificationUtils = NotificationUtils(this)
        notificationUtils.clearAllNotification()
        val content = ""
        notificationUtils.sendNotificationFullScreen(
            FullscreenActivity::class.java,
            getString(R.string.app_name), content, Intent.ACTION_BOOT_COMPLETED, 0
        )
    }

    private fun checkFWPermission() {
        val permission = PermissionUtils.checkOPsPermission(applicationContext)
        mHandler.sendEmptyMessage(if (permission) Constants.checkFWSucc else Constants.checkFWFail)
    }


    private fun checkBatteryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val batteryPermission = PermissionUtils.isIgnoringBatteryOptimizations(
                applicationContext
            )
            mHandler.sendEmptyMessage(if (batteryPermission) Constants.checkBatteryFail else Constants.checkBatterySucc)
        }
    }

    private fun checkNotification() {
        val permission: Boolean =
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        mHandler.sendEmptyMessage(if (permission) Constants.checkNotifySucc else Constants.checkNotifyFail)
    }

    //********************************************************************************************************************************************
    // 双进程保活机制 相关函数
    //********************************************************************************************************************************************
    private fun startBackService() {
        startService(Intent(this, LocalDaemonService::class.java))
    }

    private fun stopBackService() {
        stopService(Intent(this, LocalDaemonService::class.java))
    }

    private fun stopRemoteService() {
        stopService(Intent(this, RemoteDaemonService::class.java))
    }

    private fun endAllService() {
        Constants.isOpenServiceDefend = Constants.stopServiceDefend //结束进程守护
        stopRemoteService()
        stopBackService()
    }


    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("NewApi")
        override fun handleMessage(msg: Message) {
            val what = msg.what
            when (what) {
                Constants.checkFWSucc -> checkBatteryPermission()
                Constants.checkFWFail -> {
                    if (Constants.isDebug) {
                        Toast.makeText(
                            mActivity,
                            R.string.no_float_permission,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    PermissionUtils.showOpenPermissionDialog(mActivity, this)
                }
                Constants.checkBatterySucc -> checkNotification()
                Constants.checkBatteryFail -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionUtils.requestIgnoreBatteryOptimizations(mActivity)
                    }
                    checkNotification()
                }
                Constants.checkNotifySucc -> {
                }
                Constants.checkNotifyFail -> NotificationUtils.notificationGuide(mActivity)
                Constants.checkPermission -> checkBatteryPermission()
            }
        }
    }

    private val memHandler = Handler(Looper.getMainLooper())
    private val task: Runnable = object : Runnable {
        override fun run() {
            if (run) {
                val memUsed = SystemProperties.getUsedMemory(mActivity)
                val memTotal = SystemProperties.getTotalMemory(mActivity)
                val event = MemoryEvent(memUsed.toDouble(), memTotal.toDouble())
                EventBus.getDefault().postSticky(event)
                memHandler.postDelayed(this, 1000)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 6)
    fun onGetMessage(message: MemoryEvent) {
        refreshList()
    }
}