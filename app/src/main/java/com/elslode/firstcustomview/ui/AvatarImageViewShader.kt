package com.elslode.firstcustomview.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import com.elslode.firstcustomview.R
import com.elslode.firstcustomview.extensions.dpToPx

class AvatarImageViewShader @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attr, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
    }

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = DEFAULT_BORDER_COLOR
    private var initials: String = DEFAULT_STRING

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private lateinit var resultBm: Bitmap
    private lateinit var maskBm: Bitmap
    private lateinit var srcBm: Bitmap

    init {
        if (attr != null) {
            val ta = context.obtainStyledAttributes(attr, R.styleable.AvatarImageViewMask)

            borderWidth = ta.getDimension(
                R.styleable.AvatarImageViewMask_aiv_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )

            borderColor =
                ta.getColor(R.styleable.AvatarImageViewMask_aiv_borderColor, DEFAULT_BORDER_COLOR)
            initials = ta.getString(R.styleable.AvatarImageViewMask_aiv_initials) ?: DEFAULT_STRING
        }
        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged: ")
        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }
        prepareBitmaps(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw: ")
        canvas.drawBitmap(resultBm, viewRect, viewRect, null)
        //resize rect
        val half = (borderWidth / 2).toInt()
        viewRect.inset(half ,half)
        canvas.drawOval(viewRect.toRectF(), borderPaint)
    }

    private fun setup() {
        with(maskPaint) {
            color = Color.RED
            style = Paint.Style.FILL
        }

        with(borderPaint) {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
    }

    private fun prepareBitmaps(w: Int, h: Int) {
        maskBm = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        resultBm = maskBm.copy(Bitmap.Config.ARGB_8888, true)
        val maskCanvas = Canvas(maskBm)
        maskCanvas.drawOval(viewRect.toRectF(), maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        srcBm = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)

        val resultCanvas = Canvas(resultBm)

        resultCanvas.drawBitmap(maskBm, viewRect, viewRect, null)
        resultCanvas.drawBitmap(srcBm, viewRect, viewRect, maskPaint)
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_SIZE).toInt()
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec)
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            else -> MeasureSpec.getSize(spec)
        }
    }

    companion object {
        private const val TAG = "CUSTOM_VIEW"
        private const val DEFAULT_SIZE = 40
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_STRING = "??"
    }
}