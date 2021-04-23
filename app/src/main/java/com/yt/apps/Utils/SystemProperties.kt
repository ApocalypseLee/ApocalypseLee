package com.yt.apps.Utils

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import java.lang.reflect.Method


object SystemProperties {
    private val getStringProperty = getMethod(getClass("android.os.SystemProperties"))

    private fun getClass(name: String): Class<*>? {
        return try {
            val cls = Class.forName(name) ?: throw ClassNotFoundException()
            cls
        } catch (e: ClassNotFoundException) {
            try {
                ClassLoader.getSystemClassLoader().loadClass(name)
            } catch (e1: ClassNotFoundException) {
                null
            }
        }
    }

    private fun getMethod(clz: Class<*>?): Method? {
        return if (clz == null) null else try {
            clz.getMethod("get", String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    operator fun get(key: String?): String? {
        if (getStringProperty != null) {
            try {
                val value = getStringProperty.invoke(null, key) ?: return ""
                return trimToEmpty(value.toString())
            } catch (ignored: Exception) {
            }
        }
        return ""
    }

    operator fun get(key: String?, def: String): String? {
        if (getStringProperty != null) {
            try {
                val value = getStringProperty.invoke(null, key) as String
                return defaultString(trimToNull(value), def)
            } catch (ignored: Exception) {
            }
        }
        return def
    }

    private fun defaultString(str: String?, defaultStr: String): String? {
        return str ?: defaultStr
    }

    private fun trimToNull(str: String): String? {
        val ts = trim(str)
        return if (TextUtils.isEmpty(ts)) null else ts
    }

    private fun trimToEmpty(str: String?): String? {
        return str?.trim { it <= ' ' } ?: ""
    }

    private fun trim(str: String?): String? {
        return str?.trim { it <= ' ' }
    }

    private const val TAG = "Clean"
    fun clean(activity: Activity) {
        //To change body of implemented methods use File | Settings | File Templates.
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val infoList = am!!.runningAppProcesses
        val serviceInfos = am.getRunningServices(100)
        val beforeMem = getAvailMemory(activity)
        Log.d(TAG, "-----------before memory info : $beforeMem")
        var count = 0
        val pm: PackageManager = activity.packageManager
        if (infoList != null) {
            for (i in infoList.indices) {
                val appProcessInfo = infoList[i]
                Log.d(TAG, "process name : " + appProcessInfo.processName)
                //importance 该进程的重要程度  分为几个级别，数值越低就越重要。
                Log.d(TAG, "importance : " + appProcessInfo.importance)


                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
                if (appProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    val pkgList = appProcessInfo.pkgList
                    for (j in pkgList.indices) { //pkgList 得到该进程下运行的包名
                        var appName: String? = null
                        try {
                            appName = pm.getApplicationLabel(pm.getApplicationInfo(pkgList[j], 0)) as String
                        } catch (e: PackageManager.NameNotFoundException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                        Log.d(TAG, "It will be killed, package name : " + pkgList[j] + " -- " + appName)
                        am.killBackgroundProcesses(pkgList[j])
                        count++
                    }
                }
            }
        }
        val afterMem = getAvailMemory(activity)
        Log.d(TAG, "----------- after memory info : $afterMem")
        Toast.makeText(activity, "clear " + count + " process, "
                + (afterMem - beforeMem) + "M", Toast.LENGTH_LONG).show()
    }

    private fun getAvailMemory(activity: Activity): Long {
        // 获取android当前可用内存大小
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        //mi.availMem; 当前系统的可用内存
        //return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
        Log.d(TAG, "可用内存---->>>" + mi.availMem / (1024 * 1024))
        return mi.availMem / (1024 * 1024)
    }


}