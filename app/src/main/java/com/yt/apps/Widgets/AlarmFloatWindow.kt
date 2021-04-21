package com.yt.apps.Widgets

import android.content.Context
import android.content.Intent
import com.yt.apps.FullscreenActivity
import com.yt.apps.Services.FloatWindowService

class AlarmFloatWindow(context: Context) : FullScreenTouchAbleFloatWindow(context) {


    override fun create()  {
        super.create()
        binding.fwImageView.setOnClickListener {
            val intent = Intent(mContext, FullscreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mContext!!.startActivity(intent)
            remove()
            val fwIntent = Intent(mContext, FloatWindowService::class.java)
            mContext!!.stopService(fwIntent)
        }
    }

    fun setBG(resId: Int) {
        binding.fwImageView.setBackground(mContext!!.resources.getDrawable(resId))
    }

}