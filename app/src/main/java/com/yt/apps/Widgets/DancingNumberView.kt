package com.yt.apps.Widgets

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*

/**
 * Created by Administrator on 2019/7/30 0030.
 * 功能简介：跳动的数字
 */
@SuppressLint("AppCompatCustomView")
class DancingNumberView(context: Context?) : TextView(context) {

    private var numStart = "0" // 起始值

    private var numEnd // 结束值
            : String? = null

    private val duiation: Long = 2000 // 持续时间


    private var prefixString = "" // 前缀字符串

    private var postfixString = "" // 后缀字符串


    private var isInt // 是否是整数
            = false

    fun setNumberString(number: String) {
        setNumberString("0", number)
    }

    fun setNumberString(numberStart: String, numberEnd: String) {
        numStart = numberStart // 得到设置的起始数字
        numEnd = numberEnd // 得到设置的最终数字
        if (checkNumString(numberStart, numberEnd)) {
            // 数字合法 开始数字动画
            start()
        } else {
            //数字不合法 直接调用 setText 设置最终值
            text = prefixString + numberEnd + postfixString
        }
    }

    /**
     * 设置前字符串符号方法
     */
    fun setPrefixString(prefixString: String) {
        this.prefixString = prefixString
    }

    /**
     * 设置后字符串符号方法
     */
    fun setPostfixString(postfixString: String) {
        this.postfixString = postfixString
    }

    /**
     * 校验数字合法性
     *
     *
     * numberStart  开始的数字
     * numberEnd   开始的数字
     *
     * @return 合法性
     */
    private fun checkNumString(numberStart: String, numberEnd: String): Boolean {
        try {
            BigInteger(numberStart)
            BigInteger(numberEnd)
            isInt = true
        } catch (e: Exception) {
            isInt = false
            e.printStackTrace()
        }
        return try {
            val start = BigDecimal(numberStart) // 起始数字小数的筛选
            val end = BigDecimal(numberEnd) // 最终数字小数的筛选
            end.compareTo(start) >= 0 // 比较小数是否大于等于0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * 设置动画
     */
    private fun start() {

        // 创建数字动画 并设置起始值和最终值
        val animator: ValueAnimator = ValueAnimator.ofObject(
            BigDecimalEvaluator(), BigDecimal(numStart),
            BigDecimal(numEnd)
        )
        //设置动画持续的时间
        animator.setDuration(duiation)
        //设置动画内插器
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        //动画监听器
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            @SuppressLint("SetTextI18n")
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val value: BigDecimal = animation.getAnimatedValue() as BigDecimal
                //设置显示的数值
                text = prefixString + format(value) + postfixString
            }
        })
        animator.start() //启动动画
    }

    /**
     * 格式化 BigDecimal, 小数部分时保留两位小数并四舍五入
     */
    private fun format(bd: BigDecimal): String {
        val pattern: String
        pattern = if (isInt) {  // 如果是整数
            "#,###" // 整数格式
        } else {
            "#,##0.00" //小数格式
        }
        val df = DecimalFormat(pattern) //进行格式化
        return df.format(bd) //返回格式化后的字符串
    }


    /**
     * 计算线性内插的结果
     */
    internal class BigDecimalEvaluator : TypeEvaluator<Any>{
        override fun evaluate(fraction: Float, startValue: Any?, endValue: Any?): Any {
            val start: BigDecimal = startValue as BigDecimal
            val end: BigDecimal = endValue as BigDecimal
            val result: BigDecimal = end.subtract(start)
            return result.multiply(BigDecimal("" + fraction)).add(start)
        }
    }


}