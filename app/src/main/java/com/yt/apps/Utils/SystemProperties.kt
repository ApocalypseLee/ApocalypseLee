package com.yt.apps.Utils

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.Method
import java.util.*


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
    fun clean(activity: Activity, customCallback: Unit) {
        //To change body of implemented methods use File | Settings | File Templates.
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val infoList = am!!.runningAppProcesses
        val serviceInfos = am.getRunningServices(100)
        val beforeMem = getAvailableMemory(activity)
        Log.d(TAG, "-----------before memory info : $beforeMem")
        var count = 0
        val pm: PackageManager = activity.packageManager

//        val mActivityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val method = Class.forName("android.app.ActivityManager").getMethod(
//            "forceStopPackage",
//            String::class.java
//        )
//
//        val list: List<RunningAppProcessInfo> = am.getRunningAppProcesses()
//        if (list != null) {
//            for (i in list.indices) {
//                val apinfo = list[i]
//                val pkgList = apinfo.pkgList
//                if (apinfo.importance > RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    // 清理后台进程
//                    for (j in pkgList.indices) {
//                        val pkg = pkgList[j]
//                        if (pkg.matches("com.android.*".toRegex())) {
//                            Log.d(TAG, "not clean is system android pid pkg= $pkg")
//                        } else {
//                            Log.d(TAG, "auto clean apk pkg= $pkg")
//                            method.invoke(mActivityManager, pkgList[j]) //packageName是需要强制停止的应用程序包名
//                            count++
//                        }
//                    }
//                }
//            }
//        }

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
                            appName = pm.getApplicationLabel(
                                pm.getApplicationInfo(
                                    pkgList[j],
                                    0
                                )
                            ) as String
                        } catch (e: PackageManager.NameNotFoundException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                        Log.d(
                            TAG,
                            "It will be killed, package name : " + pkgList[j] + " -- " + appName
                        )
                        am.killBackgroundProcesses(pkgList[j])
                        count++
                    }
                }
            }
        }
        val afterMem = getAvailableMemory(activity)
        Log.d(TAG, "----------- after memory info : $afterMem")
        Toast.makeText(
            activity, "clear " + count + " process, "
                    + (afterMem - beforeMem) + "M", Toast.LENGTH_LONG
        ).show()
    }

    fun getAvailableMemory(activity: Activity): Long {
        // 获取android当前可用内存大小
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        //mi.availMem; 当前系统的可用内存
        //return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
        Log.d(TAG, "可用内存---->>>" + mi.availMem / (1024 * 1024))
        return mi.availMem / (1024 * 1024)
    }

    fun getTotalMemory(activity: Activity): Long {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        Log.d(TAG, "总内存---->>>" + mi.totalMem / (1024 * 1024))
        return mi.totalMem / (1024 * 1024)
    }

    fun getUsedMemory(activity: Activity): Long {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        Log.d(TAG, "已用内存---->>>" + (mi.totalMem - mi.availMem) / (1024 * 1024))
        return (mi.totalMem - mi.availMem) / (1024 * 1024)
    }

    fun isLowMemory(activity: Activity): Boolean {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        return mi.lowMemory
    }

    fun getUsedPercentValueInt(activity: Activity): Int {
        val used = getUsedMemory(activity)
        val total = getTotalMemory(activity)
        return (used.toDouble() / total.toDouble() * 100).toInt()
    }

    fun getUsedPercentValue(activity: Activity): String {
        val dir = "/proc/meminfo"
        try {
            val fr = FileReader(dir)
            val br = BufferedReader(fr, 2048)
            val memoryLine = br.readLine()
            val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
            br.close()
            val totalMemorySize = subMemoryLine.replace("\\D+".toRegex(), "").toInt().toLong()
            val availableSize: Long = getAvailableMemory(activity)
            val percent =
                ((totalMemorySize - availableSize) / totalMemorySize.toFloat() * 100).toInt()
            return "$percent%"
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "none"
    }

    /**
     * 获取整体缓存大小
     * @param context
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(context.getCacheDir())
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir()!!)
        }
        return getFormatSize(cacheSize)
    }

    fun getInnerCacheSize(context: Context): String {
        val cacheSize = getFolderSize(context.getCacheDir())
        return getFormatSize(cacheSize)
    }

    fun getExternalCacheSize(context: Context): String {
        var cacheSize = 0L
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir()!!)
        }
        return getFormatSize(cacheSize)
    }

    /**
     * 获取文件
     * Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
     * Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
     * @param file
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            for (i in fileList.indices) {
                // 如果下面还有文件
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     * @param size
     */
    fun getFormatSize(size: Long): String {
        val kb = size / 1024
        val m = (kb / 1024).toInt()
        val kbs = (kb % 1024).toInt()
        return m.toString() + "." + kbs + "M"
    }

    /**
     * 清空方法
     * @param context
     */
    fun clearAllCache(context: Context) {
        deleteDir(context.getCacheDir())
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir())
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun printForegroundTask(activity: Activity): String {
        var currentApp = "NULL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usm = activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val appList =
                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time)
            if (appList != null && appList.size > 0) {
                val mySortedMap: SortedMap<Long, UsageStats> = TreeMap()
                for (usageStats in appList) {
                    mySortedMap[usageStats.lastTimeUsed] = usageStats
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp = mySortedMap[mySortedMap.lastKey()]!!.packageName
                }
            }
        } else {
            val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = am.runningAppProcesses
            currentApp = tasks[0].processName
        }
        Log.e("adapter", "Current App in foreground is: $currentApp")
        return currentApp
    }
}