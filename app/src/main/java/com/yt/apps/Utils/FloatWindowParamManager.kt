package com.yt.apps.Utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import com.blankj.utilcode.util.AppUtils

object FloatWindowParamManager {
    const val TAG = "FloatWindowParamManager"

    fun tryJumpToPermissionPage(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            when (RomUtils.getRomName()) {
                RomUtils.ROM_MIUI -> applyMiuiPermission(context)
                RomUtils.ROM_EMUI -> applyHuaweiPermission(context)
                RomUtils.ROM_VIVO -> applyVivoPermission(context)
                RomUtils.ROM_OPPO -> applyOppoPermission(context)
                RomUtils.ROM_QIKU -> apply360Permission(context)
                RomUtils.ROM_SMARTISAN -> applySmartisanPermission(context)
                RomUtils.ROM_COOLPAD -> applyCoolpadPermission(context)
                RomUtils.ROM_ZTE -> applyZTEPermission(context)
                RomUtils.ROM_LENOVO -> applyLenovoPermission(context)
                RomUtils.ROM_LETV -> applyLetvPermission(context)
                else -> true
            }
        } else {
            if (RomUtils.isMeizuRom()) {
                getAppDetailSettingIntent(context)
            } else if (RomUtils.isVivoRom()) {
                applyVivoPermission(context)
            } else if (RomUtils.isMiuiRom()) {
                applyMiuiPermission(context) || getAppDetailSettingIntent(context)
            } else {
                applyCommonPermission(context)
            }
        }
    }

    private fun startActivitySafely(intent: Intent, context: Context): Boolean {
        return try {
            if (isIntentAvailable(intent, context)) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "启动Activity失败！！！！！！")
            false
        }
    }

    fun isIntentAvailable(intent: Intent?, context: Context): Boolean {
        return intent != null && context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).size > 0
    }

    private fun applyCommonPermission(context: Context): Boolean {
        return try {
            val clazz: Class<*> = Settings::class.java
            val field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")
            val intent = Intent(field[null].toString())
            intent.data = Uri.parse("package:" + context.packageName)
            startActivitySafely(intent, context)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun applyCoolpadPermission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.yulong.android.seccenter",
            "com.yulong.android.seccenter.dataprotection.ui.AppListActivity"
        )
        return startActivitySafely(intent, context)
    }

    private fun applyLenovoPermission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.lenovo.safecenter",
            "com.lenovo.safecenter.MainTab.LeSafeMainActivity"
        )
        return startActivitySafely(intent, context)
    }

    private fun applyZTEPermission(context: Context): Boolean {
        val intent = Intent()
        intent.action = "com.zte.heartyservice.intent.action.startActivity.PERMISSION_SCANNER"
        return startActivitySafely(intent, context)
    }

    private fun applyLetvPermission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.letv.android.letvsafe",
            "com.letv.android.letvsafe.AppActivity"
        )
        return startActivitySafely(intent, context)
    }

    private fun applyVivoPermission(context: Context): Boolean {
        val intent = Intent()
        intent.putExtra("packagename", context.packageName)
        intent.action = "com.vivo.permissionmanager"
        intent.setClassName(
            "com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
        )
        val componentName1 = intent.resolveActivity(context.packageManager)
        if (componentName1 != null) {
            return startActivitySafely(intent, context)
        }
        intent.action = "com.iqoo.secure"
        intent.setClassName(
            "com.iqoo.secure",
            "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"
        )
        val componentName2 = intent.resolveActivity(context.packageManager)
        if (componentName2 != null) {
            return startActivitySafely(intent, context)
        }
        intent.action = "com.iqoo.secure"
        intent.setClassName("com.iqoo.secure", "com.iqoo.secure.MainActivity")
        val componentName3 = intent.resolveActivity(context.packageManager)
        return if (componentName3 != null) {
            startActivitySafely(intent, context)
        } else startActivitySafely(intent, context)
    }

    private fun applyOppoPermission(context: Context): Boolean {
        val intent = Intent()
        intent.putExtra("packageName", context.packageName)
        intent.action = "com.oppo.safe"
        intent.setClassName(
            "com.oppo.safe",
            "com.oppo.safe.permission.PermissionAppListActivity"
        )
        return if (!startActivitySafely(intent, context)) {
            intent.action = "com.color.safecenter"
            intent.setClassName(
                "com.color.safecenter",
                "com.color.safecenter.permission.floatwindow.FloatWindowListActivity"
            )
            if (!startActivitySafely(intent, context)) {
                intent.action = "com.coloros.safecenter"
                intent.setClassName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"
                )
                startActivitySafely(intent, context)
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun apply360Permission(context: Context): Boolean {
        val intent = Intent()
        intent.setClassName(
            "com.android.settings",
            "com.android.settings.Settings\$OverlaySettingsActivity"
        )
        return if (!startActivitySafely(intent, context)) {
            intent.setClassName(
                "com.qihoo360.mobilesafe",
                "com.qihoo360.mobilesafe.ui.index.AppEnterActivity"
            )
            startActivitySafely(intent, context)
        } else {
            true
        }
    }

    private fun applyMiuiPermission(context: Context): Boolean {
        val intent = Intent()
        intent.action = "miui.intent.action.APP_PERM_EDITOR"
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.putExtra("extra_pkgname", context.packageName)
        return startActivitySafely(intent, context)
    }

    fun getAppDetailSettingIntent(context: Context): Boolean {
        val localIntent = Intent()
        localIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        localIntent.data = Uri.fromParts("package", context.packageName, null)
        return startActivitySafely(localIntent, context)
    }

    private fun applyMeizuPermission(context: Context): Boolean {
        val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
        intent.setClassName(
            "com.meizu.safe",
            "com.meizu.safe.security.AppSecActivity"
        )
        intent.putExtra("packageName", context.packageName)
        return startActivitySafely(intent, context)
    }

    private fun applyHuaweiPermission(context: Context): Boolean {
        return try {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            var comp = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"
            )
            intent.component = comp
            if (!startActivitySafely(intent, context)) {
                comp = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.notificationmanager.ui.NotificationManagmentActivity"
                )
                intent.component = comp
                context.startActivity(intent)
                true
            } else {
                true
            }
        } catch (e: SecurityException) {
            try {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val comp = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity"
                )
                intent.component = comp
                context.startActivity(intent)
                true
            } catch (e1: Exception) {
                Log.e(TAG, "Huawei跳转失败1$e1")
                getAppDetailSettingIntent(context)
            }
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val comp = ComponentName(
                    "com.Android.settings",
                    "com.android.settings.permission.TabItem"
                )
                intent.component = comp
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Huawei跳转失败2$e")
                getAppDetailSettingIntent(context)
            }
        } catch (e: Exception) {
            getAppDetailSettingIntent(context)
        }
    }

    private fun applySmartisanPermission(context: Context): Boolean {
        var intent = Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS_NEW")
        intent.setClassName(
            "com.smartisanos.security",
            "com.smartisanos.security.SwitchedPermissions"
        )
        intent.putExtra("index", 17) //有版本差异,不一定定位正确
        return if (startActivitySafely(intent, context)) {
            true
        } else {
            intent = Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS")
            intent.setClassName(
                "com.smartisanos.security",
                "com.smartisanos.security.SwitchedPermissions"
            )
            intent.putExtra("permission", arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW))
            startActivitySafely(intent, context)
        }
    }

    fun getFloatLayoutParam(fullScreen: Boolean, touchAble: Boolean): WindowManager.LayoutParams? {
        val layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            //刘海屏延伸到刘海里面
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.M
        ) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams.packageName = AppUtils.getAppPackageName()
        layoutParams.flags =
            layoutParams.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

        //Focus会占用屏幕焦点，导致游戏无声
        if (touchAble) {
            layoutParams.flags =
                layoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        } else {
            layoutParams.flags =
                layoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        if (fullScreen) {
            layoutParams.flags = layoutParams.flags or (WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        } else {
            layoutParams.flags =
                layoutParams.flags or (WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        layoutParams.format = PixelFormat.TRANSPARENT
        return layoutParams
    }
}