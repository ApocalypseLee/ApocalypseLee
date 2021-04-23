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
import com.blankj.utilcode.util.SizeUtils
import com.yt.apps.R
import kotlin.math.roundToInt

class FollowTouchView(private val context: Context) : BaseFloatWindow(context) {
    private var mScaledTouchSlop = 0


    override fun create() {
        super.create()
        mViewMode = WRAP_CONTENT_TOUCHABLE
        mGravity = Gravity.START or Gravity.TOP
        val mAddX = SizeUtils.dp2px(100f)
        val mAddY = SizeUtils.dp2px(100f)
        inflate(R.layout.main_layout_follow_touch)
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
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
                        val sqrt = Math.sqrt(Math.pow(disX.toDouble(), 2.0) + Math.pow(disY.toDouble(), 2.0))
                        if (sqrt < mScaledTouchSlop) {
                            jumpHome()
                        }
                    }
                }
                return false
            }
        })
    }

    private fun jumpHome() {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        mContext!!.startActivity(intent)
    }

    override fun onAddWindowFailed(e: Exception?) {
        TODO("Not yet implemented")
    }
}