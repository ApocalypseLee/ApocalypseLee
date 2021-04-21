package com.yt.apps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.PowerManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.yt.apps.Services.FloatWindowService.Companion.FLAG_RES_ID
import com.yt.apps.Utils.UIUtils

class AlarmActivity : BaseAlarmActivity() {
    var imageView: ImageView? = null
    var textView: TextView? = null
    override var resId = 0

    fun onAlarmClick(v: View?) {
        val intent = Intent(this, FullscreenActivity::class.java)
        startActivity(intent)
    }

    fun onAlarmCloseClick(v: View?) {
        finish()
    }

    override fun onInit() {
        setContentView(R.layout.alarm)

        //下面就是根据自己的跟你需求来写，跟写一个Activity一样的
        //拿到传过来的数据
        setFlag(intent.getStringExtra("flag")!!)
        if (intent.hasExtra(FLAG_RES_ID)) {
            resId = intent.getIntExtra(FLAG_RES_ID, 0)
            initRes(resId)
        } else {
            if (getFlag() == Intent.ACTION_SCREEN_OFF) {
                resId = UIUtils.getIntegerRandomBound(1, 3)
            } else if (getFlag() == Intent.ACTION_TIME_TICK) {
                resId = UIUtils.getIntegerRandomBound(1, 2)
            }
            initRes(resId)
        }
    }

    private fun initRes(resId: Int) {
        textView = getTextRes(R.id.close)
        if (getFlag() == Intent.ACTION_SCREEN_OFF) {
            textView!!.visibility = View.GONE
            imageView = getImageRes(R.id.imageView)
            when (resId) {
                1 -> imageView!!.setImageDrawable(resources.getDrawable(R.drawable.hint_iphone))
                2 -> imageView!!.setImageDrawable(resources.getDrawable(R.drawable.hint_bonus))
                3 -> imageView!!.setImageDrawable(resources.getDrawable(R.drawable.hint_money2))
            }
        } else if (getFlag() == Intent.ACTION_TIME_TICK) {
            textView!!.visibility = View.VISIBLE
            imageView = getImageRes(R.id.imageView)
            when (resId) {
                1 -> imageView!!.setImageDrawable(resources.getDrawable(R.drawable.alarm_iphone))
                2 -> imageView!!.setImageDrawable(resources.getDrawable(R.drawable.alarm_money))
            }
        }
    }

    @SuppressLint("WakelockTimeout")
    override fun initNewIntent(intent: Intent?) {
        setFlag(intent?.getStringExtra("flag"))
        resId = intent?.getIntExtra("resId", 0)!!
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        initRes(resId)
        if (!pm.isScreenOn && getFlag() == Intent.ACTION_TIME_TICK) {
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