package com.yt.apps.Widgets

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.yt.apps.R
import com.yt.apps.Services.FloatWindowService
import com.yt.apps.databinding.FullscreenFloatWindowBinding

open class FullScreenTouchAbleFloatWindow(context: Context) : BaseFloatWindow(context) {

    lateinit var binding: FullscreenFloatWindowBinding

    override fun create() {
        super.create()
        inflate(R.layout.fullscreen_float_window)
        mViewMode = FULLSCREEN_TOUCHABLE
        binding = FullscreenFloatWindowBinding.inflate(LayoutInflater.from(mContext))
        binding.fwClose.setOnClickListener {
            Toast.makeText(mContext, "已关闭", Toast.LENGTH_SHORT).show()
            remove()
            val fwIntent = Intent(mContext, FloatWindowService::class.java)
            mContext!!.stopService(fwIntent)
        }
    }

    override fun onAddWindowFailed(e: Exception?) {
        Log.e(TAG, "添加悬浮窗失败")
        Toast.makeText(mContext, "添加悬浮窗失败,请检查悬浮窗权限!", Toast.LENGTH_SHORT).show()
    }
}