package com.yt.apps.Utils

import android.os.Build
import android.text.TextUtils
import androidx.annotation.StringDef
import com.blankj.utilcode.util.DeviceUtils
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

object RomUtils {
    const val ROM_MIUI = "MIUI"
    const val ROM_EMUI = "EMUI"
    const val ROM_VIVO = "VIVO"
    const val ROM_OPPO = "OPPO"
    const val ROM_FLYME = "FLYME"
    const val ROM_SMARTISAN = "SMARTISAN"
    const val ROM_QIKU = "QIKU"
    const val ROM_LETV = "LETV"
    const val ROM_LENOVO = "LENOVO"
    const val ROM_NUBIA = "NUBIA"
    const val ROM_ZTE = "ZTE"
    const val ROM_COOLPAD = "COOLPAD"
    const val ROM_UNKNOWN = "UNKNOWN"

    @StringDef(
        ROM_MIUI,
        ROM_EMUI,
        ROM_VIVO,
        ROM_OPPO,
        ROM_FLYME,
        ROM_SMARTISAN,
        ROM_QIKU,
        ROM_LETV,
        ROM_LENOVO,
        ROM_ZTE,
        ROM_COOLPAD,
        ROM_UNKNOWN
    )
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(
        RetentionPolicy.SOURCE
    )
    annotation class RomName

    private const val SYSTEM_VERSION_MIUI = "ro.miui.ui.version.name"
    private const val SYSTEM_VERSION_EMUI = "ro.build.version.emui"
    private const val SYSTEM_VERSION_VIVO = "ro.vivo.os.version"
    private const val SYSTEM_VERSION_OPPO = "ro.build.version.opporom"
    private const val SYSTEM_VERSION_FLYME = "ro.build.display.id"
    private const val SYSTEM_VERSION_SMARTISAN = "ro.smartisan.version"
    private const val SYSTEM_VERSION_LETV = "ro.letv.eui"
    private const val SYSTEM_VERSION_LENOVO = "ro.lenovo.lvp.version"

    private fun getSystemProperty(propName: String): String? {
        return SystemProperties.get(propName, "")
    }

    @RomName
    fun getRomName(): String? {
        if (isMiuiRom()) {
            return ROM_MIUI
        }
        if (isHuaweiRom()) {
            return ROM_EMUI
        }
        if (isVivoRom()) {
            return ROM_VIVO
        }
        if (isOppoRom()) {
            return ROM_OPPO
        }
        if (isMeizuRom()) {
            return ROM_FLYME
        }
        if (isSmartisanRom()) {
            return ROM_SMARTISAN
        }
        if (is360Rom()) {
            return ROM_QIKU
        }
        if (isLetvRom()) {
            return ROM_LETV
        }
        if (isLenovoRom()) {
            return ROM_LENOVO
        }
        if (isZTERom()) {
            return ROM_ZTE
        }
        return if (isCoolPadRom()) {
            ROM_COOLPAD
        } else ROM_UNKNOWN
    }

    fun getDeviceManufacture(): String? {
        if (isMiuiRom()) {
            return "小米"
        }
        if (isHuaweiRom()) {
            return "华为"
        }
        if (isVivoRom()) {
            return ROM_VIVO
        }
        if (isOppoRom()) {
            return ROM_OPPO
        }
        if (isMeizuRom()) {
            return "魅族"
        }
        if (isSmartisanRom()) {
            return "锤子"
        }
        if (is360Rom()) {
            return "奇酷"
        }
        if (isLetvRom()) {
            return "乐视"
        }
        if (isLenovoRom()) {
            return "联想"
        }
        if (isZTERom()) {
            return "中兴"
        }
        return if (isCoolPadRom()) {
            "酷派"
        } else DeviceUtils.getManufacturer()
    }

    fun isMiuiRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_MIUI))
    }

    fun isHuaweiRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_EMUI))
    }

    fun isVivoRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_VIVO))
    }

    fun isOppoRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_OPPO))
    }

    fun isMeizuRom(): Boolean {
        val meizuFlymeOSFlag = getSystemProperty(SYSTEM_VERSION_FLYME)!!
        return !TextUtils.isEmpty(meizuFlymeOSFlag) && meizuFlymeOSFlag.toUpperCase()
            .contains(ROM_FLYME)
    }

    fun isSmartisanRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_SMARTISAN))
    }

    fun is360Rom(): Boolean {
        val manufacturer = Build.MANUFACTURER
        return !TextUtils.isEmpty(manufacturer) && manufacturer.toUpperCase().contains(ROM_QIKU)
    }

    fun isLetvRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_LETV))
    }

    fun isLenovoRom(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_LENOVO))
    }

    fun isCoolPadRom(): Boolean {
        val model = Build.MODEL
        val fingerPrint = Build.FINGERPRINT
        return (!TextUtils.isEmpty(model) && model.toLowerCase().contains(ROM_COOLPAD)
                || !TextUtils.isEmpty(fingerPrint) && fingerPrint.toLowerCase()
            .contains(ROM_COOLPAD))
    }

    fun isZTERom(): Boolean {
        val manufacturer = Build.MANUFACTURER
        val fingerPrint = Build.FINGERPRINT
        return (!TextUtils.isEmpty(manufacturer) && (fingerPrint.toLowerCase().contains(ROM_NUBIA)
                || fingerPrint.toLowerCase().contains(ROM_ZTE))
                || !TextUtils.isEmpty(fingerPrint) && (fingerPrint.toLowerCase().contains(ROM_NUBIA)
                || fingerPrint.toLowerCase().contains(ROM_ZTE)))
    }

    fun isDomesticSpecialRom(): Boolean {
        return (isMiuiRom()
                || isHuaweiRom()
                || isMeizuRom()
                || is360Rom()
                || isOppoRom()
                || isVivoRom()
                || isLetvRom()
                || isZTERom()
                || isLenovoRom()
                || isCoolPadRom())
    }

    fun isSmartisanR1(): Boolean {
        return Build.MODEL.contains("DE106")
    }

    /**
     * Vivo沙雕刘海屏判断
     *
     * @return
     */
    fun isVivoStupidNotch(): Boolean {
        return isVivoX21() || isVivoX21S() || isVivoX23() || isVivoZ1() || isVivoZ3() ||
                isVivoY81s() || isVivoY83() || isVivoY85() || isVivoY93() || isVivoY97()
    }

    fun isVivoX21(): Boolean {
        return Build.MODEL.contains("vivo X21")
    }

    fun isVivoX21S(): Boolean {
        return Build.MODEL.contains("V1814")
    }

    fun isVivoX23(): Boolean {
        //X23普通 幻彩版
        return Build.MODEL.contains("V1809") || Build.MODEL.contains("V1816")
    }

    fun isVivoZ1(): Boolean {
        return Build.MODEL.contains("V1730")
    }

    fun isVivoZ3(): Boolean {
        return Build.MODEL.contains("V1813BA")
    }

    fun isVivoY81s(): Boolean {
        return Build.MODEL.contains("V1732")
    }

    fun isVivoY83(): Boolean {
        return Build.MODEL.contains("Y83")
    }

    fun isVivoY85(): Boolean {
        return Build.MODEL.contains("vivo Y85")
    }

    fun isVivoY93(): Boolean {
        return Build.MODEL.contains("V1818")
    }

    fun isVivoY97(): Boolean {
        return Build.MODEL.contains("V1813A") || Build.MODEL.contains("V1813T")
    }

    fun isHonorV10(): Boolean {
        return Build.MODEL.contains("BKL-AL00")
    }

    fun isHonor10(): Boolean {
        return Build.MODEL.contains("COL-AL10")
    }

    fun isMiPad4(): Boolean {
        return TextUtils.equals(Build.MODEL, "MI PAD 4")
    }
}