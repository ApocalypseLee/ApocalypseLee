package com.yt.apps.Widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import com.yt.apps.FullscreenActivity
import com.yt.apps.R
import com.yt.apps.data.MemoryEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

class FollowTouchView(context: Context, private var percent: Int) : BaseFloatWindow(context) {
    private var mScaledTouchSlop = 0
    private lateinit var bubbleView: BubbleView


    @SuppressLint("NewApi")
    override fun create() {
        super.create()
        mViewMode = WRAP_CONTENT_TOUCHABLE
        mGravity = Gravity.START or Gravity.TOP
//        val mAddX = SizeUtils.dp2px(100f)
//        val mAddY = SizeUtils.dp2px(100f)
        inflate(R.layout.main_layout_follow_touch)

        bubbleView = mInflate.findViewById(R.id.bubble_view)
        bubbleView.setTextColor(mContext!!.getColor(R.color.colorAccent))
        bubbleView.percent = percent
        bubbleView.setInnerRadius(90f)
        bubbleView.setOuterStrokeWidth(10f)
        bubbleView.setGreedSize(30f)
        bubbleView.setPercentSize(30f)
        bubbleView.lifeDelegate = BubbleView.RESUME

        mScaledTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop
        mInflate.setOnTouchListener(object : OnTouchListener {
            private var mLastY = 0f
            private var mLastX = 0f
            private var mDownY = 0f
            private var mDownX = 0f

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val x = event.rawX
                val y = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mDownX = x
                        mDownY = y
                        mLastX = x
                        mLastY = y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val moveX = x - mLastX
                        val moveY = y - mLastY
                        Log.e("TAG", "$moveX $moveY")
                        ((mLayoutParams.x + moveX).roundToInt().also { mLayoutParams.x = it })
                        ((mLayoutParams.y + moveY).roundToInt().also { mLayoutParams.y = it })
                        mWindowManager.updateViewLayout(mInflate, mLayoutParams)
                        mLastX = x
                        mLastY = y
                    }
                    MotionEvent.ACTION_UP -> {
                        val disX = x - mDownX
                        val disY = y - mDownY
                        val sqrt = Math.sqrt(
                            Math.pow(disX.toDouble(), 2.0) + Math.pow(
                                disY.toDouble(),
                                2.0
                            )
                        )
                        if (sqrt < mScaledTouchSlop) {
                            jumpAPP()
                        }
                    }
                }
                return false
            }
        })
    }

    private fun jumpAPP() {
        val intent = Intent(mContext, FullscreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        mContext!!.startActivity(intent)
    }

    private fun jumpHome() {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        mContext!!.startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 6)
    fun onGetMessage(message: MemoryEvent) {
        bubbleView.percent = message.getUsedPercent()
    }

    override fun onAddWindowFailed(e: Exception?) {
        TODO("Not yet implemented")
    }
}