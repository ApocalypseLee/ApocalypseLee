package com.yt.apps.Widgets

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.yt.apps.Utils.FloatWindowParamManager
import org.greenrobot.eventbus.EventBus

abstract class BaseFloatWindow(var mContext: Context?) {
    val TAG = "FloatWindowBase"

    val FULLSCREEN_TOUCHABLE = 1
    val FULLSCREEN_NOT_TOUCHABLE = 2
    val WRAP_CONTENT_TOUCHABLE = 3
    val WRAP_CONTENT_NOT_TOUCHABLE = 4

    lateinit var mLayoutParams: WindowManager.LayoutParams
    lateinit var mInflate: View
    lateinit var mWindowManager: WindowManager
    private var mAdded = false

    //设置隐藏时是否是INVISIBLE
    private var mInvisibleNeed = false
    private var mRequestFocus = false
    var mGravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
    var mViewMode = WRAP_CONTENT_NOT_TOUCHABLE
    var mHandler: Handler? = Handler(Looper.getMainLooper())

    init {
        create()
        EventBus.getDefault().register(this)
    }

    /**
     * 设置隐藏View的方式是否为Invisible，默认为Gone
     *
     * @param invisibleNeed 是否是Invisible
     */
    open fun setInvisibleNeed(invisibleNeed: Boolean) {
        mInvisibleNeed = invisibleNeed
    }

    /**
     * 悬浮窗是否需要获取焦点，通常获取焦点后，悬浮窗可以和软键盘发生交互，被覆盖的应用失去焦点。
     * 例如：游戏将失去背景音乐
     *
     * @param requestFocus
     */
    open fun requestFocus(requestFocus: Boolean) {
        mRequestFocus = requestFocus
    }

    @CallSuper
    open fun create() {
        mWindowManager =
            mContext!!.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    @CallSuper
    @Synchronized
    open fun show() {
        checkNotNull(mInflate) { "FloatView can not be null" }
        if (mAdded) {
            mInflate.visibility = View.VISIBLE
            return
        }
        getLayoutParam(mViewMode)
        mInflate.visibility = View.VISIBLE
        try {
            mWindowManager.addView(mInflate, mLayoutParams)
            mAdded = true
        } catch (e: Exception) {
            onAddWindowFailed(e)
        }
    }

    @CallSuper
    open fun hide() {
        mInflate.visibility = View.INVISIBLE
    }

    @CallSuper
    open fun gone() {
        mInflate.visibility = View.GONE
    }

    @CallSuper
    open fun remove() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mInflate.isAttachedToWindow) {
                mWindowManager.removeView(mInflate)
            }
        }
        mAdded = false
        if (mHandler != null) {
            mHandler!!.removeCallbacksAndMessages(null)
        }
        EventBus.getDefault().unregister(this)
    }

    @CallSuper
    protected open fun inflate(@LayoutRes layout: Int): View? {
        mInflate = View.inflate(mContext, layout, null)
        return mInflate
    }

    protected open fun <T : View?> findView(@IdRes id: Int): T? {
        return mInflate.findViewById<View>(id) as T
    }


    /**
     * 获取悬浮窗LayoutParam
     *
     * @param mode
     */
    protected open fun getLayoutParam(mode: Int) {
        when (mode) {
            FULLSCREEN_TOUCHABLE -> mLayoutParams =
                FloatWindowParamManager.getFloatLayoutParam(true, true)!!
            FULLSCREEN_NOT_TOUCHABLE -> mLayoutParams =
                FloatWindowParamManager.getFloatLayoutParam(true, false)!!
            WRAP_CONTENT_NOT_TOUCHABLE -> mLayoutParams =
                FloatWindowParamManager.getFloatLayoutParam(false, false)!!
            WRAP_CONTENT_TOUCHABLE -> mLayoutParams =
                FloatWindowParamManager.getFloatLayoutParam(false, true)!!
        }
        if (mRequestFocus) {
            mLayoutParams.flags =
                mLayoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        }
        mLayoutParams.gravity = mGravity
    }

    /**
     * 获取可见性
     *
     * @return
     */
    open fun getVisibility(): Boolean {
        return if (mInflate != null && mInflate!!.visibility == View.VISIBLE) {
            true
        } else {
            false
        }
    }

    /**
     * 改变可见性
     */
    open fun toggleVisibility() {
        if (mInflate != null) {
            if (getVisibility()) {
                if (mInvisibleNeed) {
                    hide()
                } else {
                    gone()
                }
            } else {
                show()
            }
        }
    }

    protected abstract fun onAddWindowFailed(e: Exception?)
}