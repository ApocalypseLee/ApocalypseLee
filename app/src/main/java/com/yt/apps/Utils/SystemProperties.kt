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
//                    // ??????????????????
//                    for (j in pkgList.indices) {
//                        val pkg = pkgList[j]
//                        if (pkg.matches("com.android.*".toRegex())) {
//                            Log.d(TAG, "not clean is system android pid pkg= $pkg")
//                        } else {
//                            Log.d(TAG, "auto clean apk pkg= $pkg")
//                            method.invoke(mActivityManager, pkgList[j]) //packageName??????????????????????????????????????????
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
                //importance ????????????????????????  ????????????????????????????????????????????????
                Log.d(TAG, "importance : " + appProcessInfo.importance)

                // ??????????????????RunningAppProcessInfo.IMPORTANCE_SERVICE?????????????????????????????????????????????
                // ??????????????????RunningAppProcessInfo.IMPORTANCE_VISIBLE????????????????????????????????????????????????????????????
                if (appProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    val pkgList = appProcessInfo.pkgList
                    for (j in pkgList.indices) { //pkgList ?????????????????????????????????
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
        // ??????android????????????????????????
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        //mi.availMem; ???????????????????????????
        //return Formatter.formatFileSize(context, mi.availMem);// ?????????????????????????????????
        Log.d(TAG, "????????????---->>>" + mi.availMem / (1024 * 1024))
        return mi.availMem / (1024 * 1024)
    }

    fun getTotalMemory(activity: Activity): Long {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        Log.d(TAG, "?????????---->>>" + mi.totalMem / (1024 * 1024))
        return mi.totalMem / (1024 * 1024)
    }

    fun getUsedMemory(activity: Activity): Long {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val mi = ActivityManager.MemoryInfo()
        am!!.getMemoryInfo(mi)
        Log.d(TAG, "????????????---->>>" + (mi.totalMem - mi.availMem) / (1024 * 1024))
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
     * ????????????????????????
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
     * ????????????
     * Context.getExternalFilesDir() --> SDCard/Android/data/?????????????????????/files/ ????????????????????????????????????????????????
     * Context.getExternalCacheDir() --> SDCard/Android/data/??????????????????/cache/???????????????????????????????????????
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
                // ????????????????????????
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
     * ???????????????
     * @param size
     */
    fun getFormatSize(size: Long): String {
        val kb = size / 1024
        val m = (kb / 1024).toInt()
        val kbs = (kb % 1024).toInt()
        return m.toString() + "." + kbs + "M"
    }

    /**
     * ????????????
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