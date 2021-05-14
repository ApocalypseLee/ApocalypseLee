package com.yt.apps.Widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import com.yt.apps.R
import kotlin.properties.Delegates


@RequiresApi(Build.VERSION_CODES.KITKAT)
class BubbleView(context: Context?, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    companion object {
        const val RESUME = 0x1
        const val STOP = 0x2
        const val DESTROY = 0x3
    }

    private var mWidth = 0 //控件整体宽度
    private var mHeight = 0 //控件整体高度

    //控件中心位置,x,y坐标
    private var centerX = 0
    private var centerY = 0
    private var outerRadius = 0//外圈圆环的半径
    private var innerRadius = 90f//内部圆圈的半径
    private var radiusDist = 10f//内外圆圈的半径差距
    private var fWaveShader: LinearGradient? = null
    private var sWaveShader: LinearGradient? = null
    private var wavePath = Path()
    private var waveCirclePath = Path()
    private val waveNum = 2

    //波浪的渐变颜色数组
    private val waveColors by lazy {
        arrayListOf(
//深红色
                intArrayOf(Color.parseColor("#E8E6421A"), Color.parseColor("#E2E96827")),
                intArrayOf(Color.parseColor("#E8E6421A"), Color.parseColor("#E2F19A7F")),
//橙色
                intArrayOf(Color.parseColor("#E8FDA085"), Color.parseColor("#E2F6D365")),
                intArrayOf(Color.parseColor("#E8FDA085"), Color.parseColor("#E2F5E198")),
//绿色
                intArrayOf(Color.parseColor("#E8009EFD"), Color.parseColor("#E22AF598")),
                intArrayOf(Color.parseColor("#E8009EFD"), Color.parseColor("#E28EF0C6"))
        )
    }

    //外围圆环的渐变色
    private val circleColors by lazy {
        arrayListOf(
//深红色
                intArrayOf(Color.parseColor("#FFF83600"), Color.parseColor("#FFF9D423")),
//橙色
                intArrayOf(Color.parseColor("#FFFDA085"), Color.parseColor("#FFF6D365")),
//绿色
                intArrayOf(Color.parseColor("#FF2AF598"), Color.parseColor("#FF009EFD"))
        )
    }
    private val wavePaint by lazy {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.strokeWidth = 1f
        paint
    }

    //波浪高度比例
    private var waveWaterLevelRatio = 0f

    //波浪的振幅
    private var waveAmplitude = 0f

    //波浪最大振幅高度
    private var maxWaveAmplitude = 0f

    //外围圆圈的画笔
    private val outerCirclePaint by lazy {
        val paint = Paint()
        paint.strokeWidth = 20f
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint
    }
    private val outerNormalCirclePaint by lazy {
        val paint = Paint()
        paint.strokeWidth = 20f
        paint.color = Color.parseColor("#FFF2F3F3")
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint
    }
    private val bgCirclePaint by lazy {
        val paint = Paint()
        paint.color = Color.parseColor("#FFF6FAFF")
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint
    }
    private val textPaint by lazy {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.isAntiAlias = true
        paint
    }
    private val ringPaint by lazy {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint
    }

    //外围圆圈所在的矩形
    private val outerCircleRectf by lazy {
        val rectF = RectF()
        rectF.set(
                centerX - outerRadius + outerCirclePaint.strokeWidth,
                centerY - outerRadius + outerCirclePaint.strokeWidth,
                centerX + outerRadius - outerCirclePaint.strokeWidth,
                centerY + outerRadius - outerCirclePaint.strokeWidth
        )
        rectF
    }

    //外围圆圈的颜色渐变器矩阵，用于从90度开启渐变，由于线条头部有个小圆圈会导致显示差异，因此从88度开始绘制
    private val sweepMatrix by lazy {
        val matrix = Matrix()
        matrix.setRotate(88f, centerX.toFloat(), centerY.toFloat())
        matrix
    }

    //进度 0-100
    var percent = 0
        set(value) {
            field = value
            waveWaterLevelRatio = value / 100f
//y = -4 * x2 + 4x抛物线计算振幅，水波纹振幅规律更加真实
            waveAmplitude =
                    (-4 * (waveWaterLevelRatio * waveWaterLevelRatio) + 4 * waveWaterLevelRatio) * maxWaveAmplitude
//   waveAmplitude = if (value < 50) 2f * waveWaterLevelRatio * maxWaveAmplitude else (-2 * waveWaterLevelRatio + 2) * maxWaveAmplitude
            val shader = when (value) {
                in 0..46 -> {
                    fWaveShader = LinearGradient(
                            0f, mHeight.toFloat(), 0f, mHeight * (1 - waveWaterLevelRatio),
                            waveColors[4],
                            null, Shader.TileMode.CLAMP
                    )
                    sWaveShader = LinearGradient(
                            0f, mHeight.toFloat(), 0f, mHeight * (1 - waveWaterLevelRatio),
                            waveColors[5],
                            null, Shader.TileMode.CLAMP
                    )
                    SweepGradient(
                            centerX.toFloat(),
                            centerY.toFloat(),
                            circleColors[2],
                            floatArrayOf(0f, value / 100f)
                    )
                }
                in 47..54 -> {
                    fWaveShader = LinearGradient(
                            0f, mHeight.toFloat(), 0f, mHeight * (1 - waveWaterLevelRatio),
                            waveColors[2],
                            null, Shader.TileMode.CLAMP
                    )
                    sWaveShader = LinearGradient(
                            0f, mHeight.toFloat(), 0f, mHeight * (1 - waveWaterLevelRatio),
                            waveColors[3],
                            null, Shader.TileMode.CLAMP
                    )
                    SweepGradient(
                            centerX.toFloat(),
                            centerY.toFloat(),
                            circleColors[1],
                            floatArrayOf(0f, value / 100f)
                    )
                }
                else -> {
                    fWaveShader = LinearGradient(
                            0f, mHeight.toFloat(), 0f, mHeight * (1 - waveWaterLevelRatio),
                            waveColors[0],
                            null, Shader.TileMode.CLAMP
                    )
                    sWaveShader = LinearGradient(
                            0f, mHeight.toFloat(), 0f, mHeight * (1 - waveWaterLevelRatio),
                            waveColors[1],
                            null, Shader.TileMode.CLAMP
                    )
                    SweepGradient(
                            centerX.toFloat(),
                            centerY.toFloat(),
                            circleColors[0],
                            floatArrayOf(0f, value / 100f)
                    )
                }
            }
            shader.setLocalMatrix(sweepMatrix)
            outerCirclePaint.shader = shader
            invalidate()
        }
    private val greedTip = resources.getString(R.string.mem_usage)

    //文本的字体大小
    private var percentSize = 80f
    private var greedSize = 30f
    private var textColor = Color.BLACK

    //外围圆圈的画笔大小
    private var outerStrokeWidth = 10f
    private var fAnimatedValue = 0f
    private var sAnimatedValue = 0f

    //动画
    private val fValueAnimator by lazy {
        val valueAnimator = ValueAnimator()
        valueAnimator.duration = 1500
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.setFloatValues(0f, waveWidth)
        valueAnimator.addUpdateListener { animation ->
            fAnimatedValue = animation.animatedValue as Float
            invalidate()
        }
        valueAnimator
    }
    private val sValueAnimator by lazy {
        val valueAnimator = ValueAnimator()
        valueAnimator.duration = 2000
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.setFloatValues(waveWidth, 0f)
        valueAnimator.addUpdateListener { animation ->
            sAnimatedValue = animation.animatedValue as Float
            invalidate()
        }
        valueAnimator
    }

    //一小段完整波浪的宽度
    private var waveWidth = 200f
    var lifeDelegate by Delegates.observable(0) { _, old, new ->
        when (new) {
            RESUME -> onResume()
            STOP -> onPause()
            DESTROY -> onDestroy()
        }
    }

    //设置中间进度文本的字体大小
    fun setPercentSize(size: Float) {
        percentSize = size
        invalidate()
    }

    //设置中间提示文本的字体大小
    fun setGreedSize(size: Float) {
        greedSize = size
        invalidate()
    }

    //设置文本颜色
    fun setTextColor(color: Int) {
        textColor = color
        textPaint.color = textColor
        invalidate()
    }

    //设置外围圆圈的宽度
    fun setOuterStrokeWidth(width: Float) {
        outerStrokeWidth = width
        outerCirclePaint.strokeWidth = outerStrokeWidth
        outerNormalCirclePaint.strokeWidth = outerStrokeWidth
        invalidate()
    }

    //设置内圆半径
    fun setInnerRadius(radius: Float) {
        innerRadius = radius
        invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = width - paddingStart - paddingEnd
        mHeight = height - paddingTop - paddingBottom
        centerX = mWidth / 2
        centerY = mHeight / 2
        outerRadius = mWidth.coerceAtMost(mHeight) / 2
        radiusDist = outerRadius - innerRadius
        waveWidth = mWidth * 1.8f
        maxWaveAmplitude = mHeight * 0.15f
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun onResume() {
        if (fValueAnimator.isStarted) {
            animatorResume()
        } else {
            fValueAnimator.start()
            sValueAnimator.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun animatorResume() {
        if (fValueAnimator.isPaused || !fValueAnimator.isRunning) {
            fValueAnimator.resume()
        }
        if (sValueAnimator.isPaused || !sValueAnimator.isRunning) {
            sValueAnimator.resume()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun onPause() {
        if (fValueAnimator.isRunning) {
            fValueAnimator.pause()
        }
        if (sValueAnimator.isRunning) {
            sValueAnimator.pause()
        }
    }

    private fun onDestroy() {
        fValueAnimator.cancel()
        sValueAnimator.cancel()
    }

    //当前窗口销毁时，回收动画资源
    override fun onDetachedFromWindow() {
        onDestroy()
        super.onDetachedFromWindow()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onDraw(canvas: Canvas) {
        drawCircle(canvas)
        drawWave(canvas)
        drawText(canvas)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun drawWave(canvas: Canvas) {
//波浪当前高度
        val level = (1 - waveWaterLevelRatio) * innerRadius * 2 + radiusDist
//绘制所有波浪
        for (num in 0 until waveNum) {
//重置path
            wavePath.reset()
            waveCirclePath.reset()
            var startX = if (num == 0) {//第一条波浪的起始位置
                wavePath.moveTo(-waveWidth + fAnimatedValue, level)
                -waveWidth + fAnimatedValue
            } else {//第二条波浪的起始位置
                wavePath.moveTo(-waveWidth + sAnimatedValue, level)
                -waveWidth + sAnimatedValue
            }
            while (startX < mWidth + waveWidth) {
                wavePath.quadTo(
                        startX + waveWidth / 4,
                        level + waveAmplitude,
                        startX + waveWidth / 2,
                        level
                )
                wavePath.quadTo(
                        startX + waveWidth / 4 * 3,
                        level - waveAmplitude,
                        startX + waveWidth,
                        level
                )
                startX += waveWidth
            }
            wavePath.lineTo(startX, mHeight.toFloat())
            wavePath.lineTo(0f, mHeight.toFloat())
            wavePath.close()
            waveCirclePath.addCircle(
                    centerX.toFloat(),
                    centerY.toFloat(),
                    innerRadius,
                    Path.Direction.CCW
            )
            waveCirclePath.op(wavePath, Path.Op.INTERSECT)
//绘制波浪渐变色
            wavePaint.shader = if (num == 0) {
                sWaveShader
            } else {
                fWaveShader
            }
            canvas.drawPath(waveCirclePath, wavePaint)
        }
//Fixme android6设置Path.op存在明显抖动，因此多画一圈圆环
        val ringWidth = outerRadius - outerStrokeWidth - innerRadius
        ringPaint.strokeWidth = ringWidth / 2
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), innerRadius + ringWidth / 4, ringPaint)
    }

    private fun drawText(canvas: Canvas) {
//绘制进度文字
        textPaint.isFakeBoldText = true
        textPaint.textSize = percentSize
        canvas.drawText(
            "$percent%",
                centerX.toFloat(),
                centerY.toFloat() + textPaint.textSize / 2,
                textPaint
        )
        textPaint.isFakeBoldText = false
        textPaint.textSize = greedSize
        canvas.drawText(
                greedTip,
                centerX.toFloat(),
                centerY.toFloat() - textPaint.textSize * 2,
                textPaint
        )
    }

    private fun drawCircle(canvas: Canvas) {
//绘制外围进度圆圈
        canvas.drawArc(outerCircleRectf, 0f, 360f, false, outerNormalCirclePaint)
        canvas.drawArc(outerCircleRectf, 90f, percent * 3.6f, false, outerCirclePaint)
        canvas.drawCircle(
                centerX.toFloat(),
                centerY.toFloat(),
                innerRadius,
                bgCirclePaint
        )
    }
}