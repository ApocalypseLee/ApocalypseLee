package com.yt.apps.Utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.AppUtils
import com.yt.apps.Constants
import com.yt.apps.MyApplication
import com.yt.apps.R
import com.yt.apps.Services.FloatWindowService


object PermissionUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        var isIgnoring = false
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(MyApplication.getPackageName())
        return isIgnoring
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            @SuppressLint("BatteryLife") val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + MyApplication.getPackageName())
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showOpenPermissionDialog(context: Context, handler: Handler) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.no_float_permission)
        builder.setMessage(R.string.go_t0_open_float_ask)
        builder.setPositiveButton(
            android.R.string.ok
        ) { dialog, which ->
            dialog.dismiss()
            FloatWindowParamManager.tryJumpToPermissionPage(context.applicationContext)
            val intent = Intent(context.applicationContext, FloatWindowService::class.java)
            intent.action = FloatWindowService.ACTION_CHECK_PERMISSION_AND_TRY_ADD
            context.startService(intent)
            handler.sendEmptyMessage(Constants.checkPermission)
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which ->
            dialog.dismiss()
            handler.sendEmptyMessage(Constants.checkPermission)
        }
        builder.show()
    }

    fun checkOPsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return checkOps(context)
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun checkOverlayPermission(context: Context): Boolean {
        val appOpsMgr = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        try {
            val mode = appOpsMgr.checkOpNoThrow(
                "android:system_alert_window",
                Process.myUid(), context.packageName
            )
            if (mode == 0) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun checkOps(context: Context): Boolean {
        try {
            val `object` = context.getSystemService(Context.APP_OPS_SERVICE) ?: return false
            val localClass: Class<*> = `object`.javaClass
            val arrayOfClass: Array<Class<*>?> = arrayOfNulls(3)
            arrayOfClass[0] = Integer.TYPE
            arrayOfClass[1] = Integer.TYPE
            arrayOfClass[2] = String::class.java
            val method = localClass.getMethod("checkOp", *arrayOfClass) ?: return false
            val arrayOfObject1 = arrayOfNulls<Any>(3)
            arrayOfObject1[0] = 24
            arrayOfObject1[1] = Binder.getCallingUid()
            arrayOfObject1[2] = AppUtils.getAppPackageName()
            val m = method.invoke(`object`, *arrayOfObject1) as Int
            return m == AppOpsManager.MODE_ALLOWED || !RomUtils.isDomesticSpecialRom()
        } catch (ignore: Exception) {
        }
        return false
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun needPermissionForBlocking(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode != AppOpsManager.MODE_ALLOWED
        } catch (e: NameNotFoundException) {
            true
        }
    }


    fun startActivitySafely(intent: Intent, context: Context): Boolean {
        return try {
            if (isIntentAvailable(intent, context)) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(FloatWindowParamManager.TAG, "启动Activity失败！！！！！！")
            false
        }
    }

    fun isIntentAvailable(intent: Intent?, context: Context): Boolean {
        return intent != null && context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).size > 0
    }
}