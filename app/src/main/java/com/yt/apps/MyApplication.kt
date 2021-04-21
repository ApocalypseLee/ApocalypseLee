package com.yt.apps

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.squareup.leakcanary.LeakCanary

class MyApplication : Application() {
    private val TAG = "App"

    private var appActivityLifecycleCallbacks: AppActivityLifecycleCallbacks? = null

    companion object {
        lateinit var myApplication: MyApplication

        fun getPackageName(): String? {
            return myApplication.applicationInfo.packageName
        }
//        private var myApplication: MyApplication? = null
//            get() {
//                if (field == null) {
//                    field = MyApplication()
//                }
//                return field
//            }
//
//        fun get(): MyApplication {
//            return myApplication!!
//        }
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        if (Constants.isDebug) {
            LeakCanary.install(this)
        }

        appActivityLifecycleCallbacks = AppActivityLifecycleCallbacks()
        registerActivityLifecycleCallbacks(appActivityLifecycleCallbacks)
    }

    fun isForceground(): Boolean {
        return appActivityLifecycleCallbacks!!.mIsForceground
    }

    fun isProgressSwitch(): Boolean {
        return appActivityLifecycleCallbacks!!.mIsProgressSwitch
    }

    /**
     * 应用级生命周期回调监听, 通过在onActivityStarted、onActivityStopped中进行统计计数判断应用是否进行了前后台切换
     *
     *
     * 注意: 本监听方案只在单进程场景下有效，不支持多进程场景
     */
    private class AppActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        /**
         * 当前处于前台还是后台
         */
        var mIsForceground = true

        /**
         * 本次生命周期回调是否由应用从后台切回前台触发
         */
        var mIsProgressSwitch = false

        /**
         * 计数器
         */
        private var mCounter = 0
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {
            /**
             * 过滤掉 冷启动开屏 界面从后台切回前台时的统计,具体参见[BaseSplashActivity.needStatistics]
             */
            if (activity is FullscreenActivity && !activity.needStatistics(true)) {
                return
            }
            mCounter++
            if (!mIsForceground && mCounter == 1) {
                mIsForceground = true
                mIsProgressSwitch = true
            } else {
                mIsProgressSwitch = false
            }
        }

        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {
            /**
             * 过滤掉 冷启动开屏 界面从后台切回前台时的统计,具体参见[BaseSplashActivity.needStatistics]
             */
            if (activity is FullscreenActivity && !activity.needStatistics(false)) {
                return
            }
            mCounter--
            if (mIsForceground && mCounter == 0) {
                mIsForceground = false
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}