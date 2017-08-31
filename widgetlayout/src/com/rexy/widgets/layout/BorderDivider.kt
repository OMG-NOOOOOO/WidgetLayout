package com.rexy.widgets.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable

import com.rexy.widgetlayout.R

/**
 * 描述 Divider 和 margin 的信息类,可独立画divider。目前只支持纯色divider。
 * 具体属性见
 *
 * <attr name="borderLeft" format="reference"></attr>
 * <attr name="borderLeftColor" format="color"></attr>
 * <attr name="borderLeftWidth" format="dimension"></attr>
 * <attr name="borderLeftMargin" format="dimension"></attr>
 * <attr name="borderLeftMarginStart" format="dimension"></attr>
 * <attr name="borderLeftMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="borderTop" format="reference"></attr>
 * <attr name="borderTopColor" format="color"></attr>
 * <attr name="borderTopWidth" format="dimension"></attr>
 * <attr name="borderTopMargin" format="dimension"></attr>
 * <attr name="borderTopMarginStart" format="dimension"></attr>
 * <attr name="borderTopMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="borderRight" format="reference"></attr>
 * <attr name="borderRightColor" format="color"></attr>
 * <attr name="borderRightWidth" format="dimension"></attr>
 * <attr name="borderRightMargin" format="dimension"></attr>
 * <attr name="borderRightMarginStart" format="dimension"></attr>
 * <attr name="borderRightMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="borderBottom" format="reference"></attr>
 * <attr name="borderBottomColor" format="color"></attr>
 * <attr name="borderBottomWidth" format="dimension"></attr>
 * <attr name="borderBottomMargin" format="dimension"></attr>
 * <attr name="borderBottomMarginStart" format="dimension"></attr>
 * <attr name="borderBottomMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="contentMarginLeft" format="dimension"></attr>
 * <attr name="contentMarginTop" format="dimension"></attr>
 * <attr name="contentMarginRight" format="dimension"></attr>
 * <attr name="contentMarginBottom" format="dimension"></attr>
 *
 * <attr name="contentMarginHorizontal" format="dimension"></attr>
 * <attr name="contentMarginVertical" format="dimension"></attr>
 *
 *
 *
 *
 *
 * <attr name="dividerColorHorizontal" format="color"></attr>
 *
 * <attr name="dividerWidthHorizontal" format="dimension"></attr>
 *
 * <attr name="dividerPaddingHorizontal" format="dimension"></attr>
 * <attr name="dividerPaddingHorizontalStart" format="dimension"></attr>
 * <attr name="dividerPaddingHorizontalEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="dividerColorVertical" format="color"></attr>
 *
 * <attr name="dividerWidthVertical" format="dimension"></attr>
 *
 * <attr name="dividerPaddingVertical" format="dimension"></attr>
 * <attr name="dividerPaddingVerticalStart" format="dimension"></attr>
 * <attr name="dividerPaddingVerticalEnd" format="dimension"></attr>
 *
 * @author: rexy
 * @date: 2017-06-02 10:26
 */
class BorderDivider private constructor(density: Float) {

    private var mPaintHorizontal: Paint? = null
    private var mPaintVertical: Paint? = null
    private var mPaintBorder: Paint? = null

    private var mResetPaintHorizontal = true
    private var mResetPaintVertical = true

    private var mDensity = 1f

    //左边线的drawable,颜色，宽度，和边线padding
    internal var mBorderLeft: Drawable? = null
    internal var mBorderLeftColor: Int = 0
    internal var mBorderLeftWidth: Int = 0
    internal var mBorderLeftMarginStart: Int = 0
    internal var mBorderLeftMarginEnd: Int = 0

    //上边线的drawable,颜色，宽度，和边线padding
    internal var mBorderTop: Drawable? = null
    internal var mBorderTopColor: Int = 0
    internal var mBorderTopWidth: Int = 0
    internal var mBorderTopMarginStart: Int = 0
    internal var mBorderTopMarginEnd: Int = 0

    //右边线的drawable,颜色，宽度，和边线padding
    internal var mBorderRight: Drawable? = null
    internal var mBorderRightColor: Int = 0
    internal var mBorderRightWidth: Int = 0
    internal var mBorderRightMarginStart: Int = 0
    internal var mBorderRightMarginEnd: Int = 0

    //下边线的drawable颜色，宽度，和边线padding
    internal var mBorderBottom: Drawable? = null
    internal var mBorderBottomColor: Int = 0
    internal var mBorderBottomWidth: Int = 0
    internal var mBorderBottomMarginStart: Int = 0
    internal var mBorderBottomMarginEnd: Int = 0

    //内容四个边距的距离。
    private var mContentMarginLeft = 0
    private var mContentMarginTop = 0
    private var mContentMarginRight = 0
    private var mContentMarginBottom = 0

    //水平方向Item 的间距
    private var mContentMarginHorizontal = 0
    //垂直方向Item 的间距
    private var mContentMarginVertical = 0

    //start get divider settings at horizontal direction
    var dividerHorizontal: Drawable? = null
        private set
    //水平分割线颜色
    private var mDividerColorHorizontal = 0
    //水平分割线宽
    private var mDividerWidthHorizontal = 0
    //水平分割线开始padding
    private var mDividerPaddingHorizontalStart = 0
    //水平分割线结束padding
    private var mDividerPaddingHorizontalEnd = 0

    //start get divider settings at vertical direction
    var dividerVertical: Drawable? = null
    //垂直分割线颜色
    private var mDividerColorVertical = 0
    //垂直分割线宽
    private var mDividerWidthVertical = 0
    //垂直分割线开始padding
    private var mDividerPaddingVerticalStart = 0
    //垂直分割线结束padding
    private var mDividerPaddingVerticalEnd = 0

    var mCallback: Callback? = null


    init {
        mDensity = density
        mContentMarginHorizontal *= density.toInt()
        mContentMarginVertical *= density.toInt()
        mDividerWidthHorizontal = (0.5f + density * 0.5f).toInt()
        mDividerWidthVertical = (0.5f + density * 0.5f).toInt()
        mDividerPaddingHorizontalStart *= density.toInt()
        mDividerPaddingVerticalStart *= density.toInt()
        mDividerPaddingHorizontalEnd *= density.toInt()
        mDividerPaddingVerticalEnd *= density.toInt()
        mBorderLeftWidth = (0.5f + density * 0.5f).toInt()
        mBorderLeftMarginStart *= density.toInt()
        mBorderLeftMarginEnd *= density.toInt()
        mBorderTopWidth = (0.5f + density * 0.5f).toInt()
        mBorderTopMarginStart *= density.toInt()
        mBorderTopMarginEnd *= density.toInt()
        mBorderRightWidth = (0.5f + density * 0.5f).toInt()
        mBorderRightMarginStart *= density.toInt()
        mBorderRightMarginEnd *= density.toInt()
        mBorderBottomWidth = (0.5f + density * 0.5f).toInt()
        mBorderBottomMarginStart *= density.toInt()
        mBorderBottomMarginEnd *= density.toInt()
        mContentMarginLeft *= density.toInt()
        mContentMarginTop *= density.toInt()
        mContentMarginRight *= density.toInt()
        mContentMarginBottom *= density.toInt()
    }

    fun dip(dip: Float): Int {
        return (0.5f + dip * mDensity).toInt()
    }


    //start divider setting at vertical direction
    fun setDividerDrawableVertical(drawableVertical: Drawable) {
        if (dividerVertical !== drawableVertical) {
            dividerVertical = drawableVertical
            mCallback?.invalidate()
        }
    }

    //start divider setting at horizontal direction
    fun setDividerDrawableHorizontal(drawableHorizontal: Drawable) {
        if (dividerHorizontal !== drawableHorizontal) {
            dividerHorizontal = drawableHorizontal
            mCallback?.invalidate()
        }
    }

    //start get border left settings
    //start border left settings,include drawable,color,width,margin.
    var borderLeft: Drawable?
        get() = mBorderLeft
        set(borderLeft) {
            if (mBorderLeft !== borderLeft) {
                mBorderLeft = borderLeft
                mCallback?.invalidate()
            }
        }

    var borderLeftColor: Int
        get() = mBorderLeftColor
        set(color) {
            if (mBorderLeftColor != color) {
                mBorderLeftColor = color
                mCallback?.invalidate()
            }
        }

    var borderLeftWidth: Int
        get() = mBorderLeftWidth
        set(width) {
            if (mBorderLeftWidth != width) {
                mBorderLeftWidth = width
                mCallback?.invalidate()
            }
        }

    var borderLeftMarginStart: Int
        get() = mBorderLeftMarginStart
        set(marginStart) {
            if (mBorderLeftMarginStart != marginStart) {
                mBorderLeftMarginStart = marginStart
                mCallback?.invalidate()
            }
        }

    var borderLeftMarginEnd: Int
        get() = mBorderLeftMarginEnd
        set(marginEnd) {
            if (mBorderLeftMarginEnd != marginEnd) {
                mBorderLeftMarginEnd = marginEnd
                mCallback?.invalidate()
            }
        }


    //start get border top settings
    //start border top settings,include drawable,color,width,margin.
    var borderTop: Drawable?
        get() = mBorderTop
        set(borderTop) {
            if (mBorderTop !== borderTop) {
                mBorderTop = borderTop
                mCallback?.invalidate()
            }
        }

    var borderTopColor: Int
        get() = mBorderTopColor
        set(color) {
            if (mBorderTopColor != color) {
                mBorderTopColor = color
                mCallback?.invalidate()
            }
        }

    var borderTopWidth: Int
        get() = mBorderTopWidth
        set(width) {
            if (mBorderTopWidth != width) {
                mBorderTopWidth = width
                mCallback?.invalidate()
            }
        }

    var borderTopMarginStart: Int
        get() = mBorderTopMarginStart
        set(marginStart) {
            if (mBorderTopMarginStart != marginStart) {
                mBorderTopMarginStart = marginStart
                mCallback?.invalidate()
            }
        }

    var borderTopMarginEnd: Int
        get() = mBorderTopMarginEnd
        set(marginEnd) {
            if (mBorderTopMarginEnd != marginEnd) {
                mBorderTopMarginEnd = marginEnd
                mCallback?.invalidate()
            }
        }


    //start get border right settings
    //start border right settings,include drawable,color,width,margin.
    var borderRight: Drawable?
        get() = mBorderRight
        set(borderRight) {
            if (mBorderRight !== borderRight) {
                mBorderRight = borderRight
                mCallback?.invalidate()
            }
        }

    var borderRightColor: Int
        get() = mBorderRightColor
        set(color) {
            if (mBorderRightColor != color) {
                mBorderRightColor = color
                mCallback?.invalidate()
            }
        }

    var borderRightWidth: Int
        get() = mBorderRightWidth
        set(width) {
            if (mBorderRightWidth != width) {
                mBorderRightWidth = width
                mCallback?.invalidate()
            }
        }

    var borderRightMarginStart: Int
        get() = mBorderRightMarginStart
        set(marginStart) {
            if (mBorderRightMarginStart != marginStart) {
                mBorderRightMarginStart = marginStart
                mCallback?.invalidate()
            }
        }

    var borderRightMarginEnd: Int
        get() = mBorderRightMarginEnd
        set(marginEnd) {
            if (mBorderRightMarginEnd != marginEnd) {
                mBorderRightMarginEnd = marginEnd
                mCallback?.invalidate()
            }
        }


    //start get border bottom settings
    //start border bottom settings,include drawable,color,width,margin.
    var borderBottom: Drawable?
        get() = mBorderBottom
        set(borderBottom) {
            if (mBorderBottom !== borderBottom) {
                mBorderBottom = borderBottom
                mCallback?.invalidate()
            }
        }

    var borderBottomColor: Int
        get() = mBorderBottomColor
        set(color) {
            if (mBorderBottomColor != color) {
                mBorderBottomColor = color
                mCallback?.invalidate()
            }
        }

    var borderBottomWidth: Int
        get() = mBorderBottomWidth
        set(width) {
            if (mBorderBottomWidth != width) {
                mBorderBottomWidth = width
                mCallback?.invalidate()
            }
        }

    var borderBottomMarginStart: Int
        get() = mBorderBottomMarginStart
        set(marginStart) {
            if (mBorderBottomMarginStart != marginStart) {
                mBorderBottomMarginStart = marginStart
                mCallback?.invalidate()
            }
        }

    var borderBottomMarginEnd: Int
        get() = mBorderBottomMarginEnd
        set(marginEnd) {
            if (mBorderBottomMarginEnd != marginEnd) {
                mBorderBottomMarginEnd = marginEnd
                mCallback?.invalidate()
            }
        }


    //start get  whole content margin settings
    //start whole content margin settings.
    var contentMarginLeft: Int
        get() = mContentMarginLeft
        set(contentMarginLeft) {
            if (mContentMarginLeft != contentMarginLeft) {
                mContentMarginLeft = contentMarginLeft
                mCallback?.requestLayout()
            }
        }

    var contentMarginTop: Int
        get() = mContentMarginTop
        set(contentMarginTop) {
            if (mContentMarginTop != contentMarginTop) {
                mContentMarginTop = contentMarginTop
                mCallback?.requestLayout()
            }
        }

    var contentMarginRight: Int
        get() = mContentMarginRight
        set(contentMarginRight) {
            if (mContentMarginRight != contentMarginRight) {
                mContentMarginRight = contentMarginRight
                mCallback?.requestLayout()
            }
        }

    var contentMarginBottom: Int
        get() = mContentMarginBottom
        set(contentMarginBottom) {
            if (mContentMarginBottom != contentMarginBottom) {
                mContentMarginBottom = contentMarginBottom
                mCallback?.requestLayout()
            }
        }

    //start get content item margin settings
    //start content item margin settings.
    var contentMarginHorizontal: Int
        get() = mContentMarginHorizontal
        set(contentMarginHorizontal) {
            if (mContentMarginHorizontal != contentMarginHorizontal) {
                mContentMarginHorizontal = contentMarginHorizontal
                mCallback?.requestLayout()
            }
        }

    var contentMarginVertical: Int
        get() = mContentMarginVertical
        set(contentMarginVertical) {
            if (mContentMarginVertical != contentMarginVertical) {
                mContentMarginVertical = contentMarginVertical
                mCallback?.requestLayout()
            }
        }

    var dividerWidthHorizontal: Int
        get() = mDividerWidthHorizontal
        set(width) {
            if (mDividerWidthHorizontal != width) {
                mDividerWidthHorizontal = width
                mResetPaintHorizontal = true
                mCallback?.invalidate()
            }
        }

    var dividerColorHorizontal: Int
        get() = mDividerColorHorizontal
        set(color) {
            if (mDividerColorHorizontal != color) {
                mDividerColorHorizontal = color
                mResetPaintHorizontal = true
                mCallback?.invalidate()
            }
        }

    var dividerPaddingHorizontalStart: Int
        get() = mDividerPaddingHorizontalStart
        set(paddingStart) {
            if (mDividerPaddingHorizontalStart != paddingStart) {
                mDividerPaddingHorizontalStart = paddingStart
                mCallback?.invalidate()
            }
        }

    var dividerPaddingHorizontalEnd: Int
        get() = mDividerPaddingHorizontalEnd
        set(paddingEnd) {
            if (mDividerPaddingHorizontalEnd != paddingEnd) {
                mDividerPaddingHorizontalEnd = paddingEnd
                mCallback?.invalidate()
            }
        }

    var dividerWidthVertical: Int
        get() = mDividerWidthVertical
        set(width) {
            if (mDividerWidthVertical != width) {
                mDividerWidthVertical = width
                mResetPaintVertical = true
                mCallback?.invalidate()
            }
        }

    var dividerColorVertical: Int
        get() = mDividerColorVertical
        set(color) {
            if (mDividerColorVertical != color) {
                mDividerColorVertical = color
                mResetPaintVertical = true
                mCallback?.invalidate()
            }
        }

    var dividerPaddingVerticalStart: Int
        get() = mDividerPaddingVerticalStart
        set(paddingStart) {
            if (mDividerPaddingVerticalStart != paddingStart) {
                mDividerPaddingVerticalStart = paddingStart
                mCallback?.invalidate()
            }
        }

    var dividerPaddingVerticalEnd: Int
        get() = mDividerPaddingVerticalEnd
        set(paddingEnd) {
            if (mDividerPaddingVerticalEnd != paddingEnd) {
                mDividerPaddingVerticalEnd = paddingEnd
                mCallback?.invalidate()
            }
        }

    fun isVisibleDividerHorizontal(initPaintIfNeed: Boolean): Boolean {
        val result = mDividerWidthHorizontal > 0 && (mDividerColorHorizontal != 0 || dividerHorizontal != null)
        if (initPaintIfNeed && result) {
            if (mPaintHorizontal == null) {
                mPaintHorizontal = Paint()
                mPaintHorizontal!!.style = Paint.Style.FILL
            }
            if (mResetPaintHorizontal) {
                mPaintHorizontal!!.strokeWidth = mDividerWidthHorizontal.toFloat()
                mPaintHorizontal!!.color = mDividerColorHorizontal
                mResetPaintHorizontal = false
            }
        }
        return result
    }

    fun isVisibleDividerVertical(initPaintIfNeed: Boolean): Boolean {
        val result = mDividerWidthVertical > 0 || mDividerColorVertical != 0 && dividerVertical != null
        if (initPaintIfNeed && result) {
            if (mPaintVertical == null) {
                mPaintVertical = Paint()
                mPaintVertical!!.style = Paint.Style.FILL
            }
            if (mResetPaintVertical) {
                mPaintVertical!!.strokeWidth = mDividerWidthVertical.toFloat()
                mPaintVertical!!.color = mDividerColorVertical
                mResetPaintVertical = false
            }
        }
        return result
    }

    fun applyContentMargin(outRect: Rect) {
        outRect.left += mContentMarginLeft
        outRect.top += mContentMarginTop
        outRect.right += mContentMarginRight
        outRect.bottom += mContentMarginBottom
    }

    fun drawBorder(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        val drawLeft = mBorderLeftColor != 0 && mBorderLeftWidth > 0
        val drawTop = mBorderTopColor != 0 && mBorderTopWidth > 0
        val drawRight = mBorderRightColor != 0 && mBorderRightWidth > 0
        val drawBottom = mBorderBottomColor != 0 && mBorderBottomWidth > 0
        if (drawLeft || drawTop || drawRight || drawBottom) {
            if (mPaintBorder == null) {
                mPaintBorder = Paint()
                mPaintBorder!!.style = Paint.Style.FILL
            }
            var startX: Float
            var startY: Float
            var endX: Float
            var endY: Float
            if (drawLeft) {
                startY = mBorderLeftMarginStart.toFloat()
                endY = (viewHeight - mBorderLeftMarginEnd).toFloat()
                if (mBorderLeft == null) {
                    mPaintBorder!!.color = mBorderLeftColor
                    mPaintBorder!!.strokeWidth = mBorderLeftWidth.toFloat()
                    endX = (mBorderLeftWidth / 2).toFloat()
                    startX = endX
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder!!)
                } else {
                    startX = 0f
                    endX = mBorderLeftWidth.toFloat()
                    mBorderLeft!!.setBounds(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
                    mBorderLeft!!.draw(canvas)
                }
            }
            if (drawRight) {
                startY = mBorderRightMarginStart.toFloat()
                endY = (viewHeight - mBorderRightMarginEnd).toFloat()
                if (mBorderRight == null) {
                    mPaintBorder!!.color = mBorderRightColor
                    mPaintBorder!!.strokeWidth = mBorderRightWidth.toFloat()
                    endX = (viewWidth - mBorderRightWidth / 2).toFloat()
                    startX = endX
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder!!)
                } else {
                    startX = (viewWidth - mBorderLeftWidth).toFloat()
                    endX = viewWidth.toFloat()
                    mBorderRight!!.setBounds(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
                    mBorderRight!!.draw(canvas)
                }
            }
            if (drawTop) {
                startX = mBorderTopMarginStart.toFloat()
                endX = (viewWidth - mBorderTopMarginEnd).toFloat()
                if (mBorderTop == null) {
                    mPaintBorder!!.color = mBorderTopColor
                    mPaintBorder!!.strokeWidth = mBorderTopWidth.toFloat()
                    endY = (mBorderTopWidth / 2).toFloat()
                    startY = endY
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder!!)
                } else {
                    startY = 0f
                    endY = mBorderTopWidth.toFloat()
                    mBorderTop!!.setBounds(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
                    mBorderTop!!.draw(canvas)
                }
            }
            if (drawBottom) {
                startX = mBorderBottomMarginStart.toFloat()
                endX = (viewWidth - mBorderBottomMarginEnd).toFloat()
                if (mBorderBottom == null) {
                    mPaintBorder!!.color = mBorderBottomColor
                    mPaintBorder!!.strokeWidth = mBorderBottomWidth.toFloat()
                    endY = (viewHeight - mBorderBottomWidth / 2).toFloat()
                    startY = endY
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder!!)
                } else {
                    startY = (viewHeight - mBorderBottomWidth).toFloat()
                    endY = viewHeight.toFloat()
                    mBorderBottom!!.setBounds(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
                    mBorderBottom!!.draw(canvas)
                }
            }
        }
    }

    fun drawDividerH(canvas: Canvas, xStart: Float, xEnd: Float, y: Float) {
        var xStart = xStart
        var xEnd = xEnd
        xStart += mDividerPaddingHorizontalStart.toFloat()
        xEnd -= mDividerPaddingHorizontalEnd.toFloat()
        if (xEnd > xStart && mPaintHorizontal != null) {
            if (dividerHorizontal == null) {
                canvas.drawLine(xStart, y, xEnd, y, mPaintHorizontal!!)
            } else {
                val paintWidth = ((mPaintHorizontal!!.strokeWidth + 0.5f) / 2).toInt().toFloat()
                dividerHorizontal!!.setBounds(xStart.toInt(), (y - paintWidth).toInt(), xEnd.toInt(), (y + paintWidth).toInt())
                dividerHorizontal!!.draw(canvas)
            }

        }
    }

    fun drawDividerV(canvas: Canvas, yStart: Float, yEnd: Float, x: Float) {
        var yStart = yStart
        var yEnd = yEnd
        yStart += mDividerPaddingVerticalStart.toFloat()
        yEnd -= mDividerPaddingVerticalEnd.toFloat()
        if (yEnd > yStart && mPaintVertical != null) {
            if (dividerVertical == null) {
                canvas.drawLine(x, yStart, x, yEnd, mPaintVertical!!)
            } else {
                val paintWidth = ((mPaintHorizontal!!.strokeWidth + 0.5f) / 2).toInt().toFloat()
                dividerVertical!!.setBounds((x - paintWidth).toInt(), yStart.toInt(), (x + paintWidth).toInt(), yEnd.toInt())
                dividerVertical!!.draw(canvas)
            }
        }
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    interface Callback {
        fun requestLayout()

        fun invalidate()
    }

    companion object {

        @JvmStatic fun from(context: Context, attr: TypedArray?): BorderDivider {
            val dm = BorderDivider(context.resources.displayMetrics.density)
            if (attr != null) {
                if (attr.hasValue(R.styleable.BaseViewGroup_borderLeft)) {
                    dm.mBorderLeft = attr.getDrawable(R.styleable.BaseViewGroup_borderLeft)
                }
                dm.mBorderLeftColor = attr.getColor(R.styleable.BaseViewGroup_borderLeftColor, dm.mBorderLeftColor)
                dm.mBorderLeftWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftWidth, dm.mBorderLeftWidth)
                val hasBorderLeftMargin = attr.hasValue(R.styleable.BaseViewGroup_borderLeftMargin)
                val borderLeftMargin = if (hasBorderLeftMargin) attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftMargin, 0) else 0
                dm.mBorderLeftMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftMarginStart, if (hasBorderLeftMargin) borderLeftMargin else dm.mBorderLeftMarginStart)
                dm.mBorderLeftMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftMarginEnd, if (hasBorderLeftMargin) borderLeftMargin else dm.mBorderLeftMarginEnd)

                if (attr.hasValue(R.styleable.BaseViewGroup_borderTop)) {
                    dm.mBorderTop = attr.getDrawable(R.styleable.BaseViewGroup_borderTop)
                }
                dm.mBorderTopColor = attr.getColor(R.styleable.BaseViewGroup_borderTopColor, dm.mBorderTopColor)
                dm.mBorderTopWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopWidth, dm.mBorderTopWidth)
                val hasBorderTopMargin = attr.hasValue(R.styleable.BaseViewGroup_borderTopMargin)
                val borderTopMargin = if (hasBorderTopMargin) attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopMargin, 0) else 0
                dm.mBorderTopMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopMarginStart, if (hasBorderTopMargin) borderTopMargin else dm.mBorderTopMarginStart)
                dm.mBorderTopMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopMarginEnd, if (hasBorderTopMargin) borderTopMargin else dm.mBorderTopMarginEnd)

                if (attr.hasValue(R.styleable.BaseViewGroup_borderRight)) {
                    dm.mBorderRight = attr.getDrawable(R.styleable.BaseViewGroup_borderRight)
                }
                dm.mBorderRightColor = attr.getColor(R.styleable.BaseViewGroup_borderRightColor, dm.mBorderRightColor)
                dm.mBorderRightWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightWidth, dm.mBorderRightWidth)
                val hasBorderRightMargin = attr.hasValue(R.styleable.BaseViewGroup_borderRightMargin)
                val borderRightMargin = if (hasBorderRightMargin) attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightMargin, 0) else 0
                dm.mBorderRightMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightMarginStart, if (hasBorderRightMargin) borderRightMargin else dm.mBorderRightMarginStart)
                dm.mBorderRightMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightMarginEnd, if (hasBorderRightMargin) borderRightMargin else dm.mBorderRightMarginEnd)

                if (attr.hasValue(R.styleable.BaseViewGroup_borderBottom)) {
                    dm.mBorderBottom = attr.getDrawable(R.styleable.BaseViewGroup_borderBottom)
                }
                dm.mBorderBottomColor = attr.getColor(R.styleable.BaseViewGroup_borderBottomColor, dm.mBorderBottomColor)
                dm.mBorderBottomWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomWidth, dm.mBorderBottomWidth)
                val hasBorderBottomMargin = attr.hasValue(R.styleable.BaseViewGroup_borderBottomMargin)
                val borderBottomMargin = if (hasBorderBottomMargin) attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomMargin, 0) else 0
                dm.mBorderBottomMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomMarginStart, if (hasBorderBottomMargin) borderBottomMargin else dm.mBorderBottomMarginStart)
                dm.mBorderBottomMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomMarginEnd, if (hasBorderBottomMargin) borderBottomMargin else dm.mBorderBottomMarginEnd)

                dm.mContentMarginLeft = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginLeft, dm.mContentMarginLeft)
                dm.mContentMarginTop = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginTop, dm.mContentMarginTop)
                dm.mContentMarginRight = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginRight, dm.mContentMarginRight)
                dm.mContentMarginBottom = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginBottom, dm.mContentMarginBottom)

                dm.mContentMarginHorizontal = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginHorizontal, dm.mContentMarginHorizontal)
                dm.mContentMarginVertical = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginVertical, dm.mContentMarginVertical)

                dm.mDividerColorHorizontal = attr.getColor(R.styleable.BaseViewGroup_dividerColorHorizontal, dm.mDividerColorHorizontal)
                dm.mDividerWidthHorizontal = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerWidthHorizontal, dm.mDividerWidthHorizontal)
                val hasDividerPaddingHorizontal = attr.hasValue(R.styleable.BaseViewGroup_dividerPaddingHorizontal)
                val dividerPaddingHorizontal = if (hasDividerPaddingHorizontal) attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingHorizontal, 0) else 0
                dm.mDividerPaddingHorizontalStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingHorizontalStart, if (hasDividerPaddingHorizontal) dividerPaddingHorizontal else dm.mDividerPaddingHorizontalStart)
                dm.mDividerPaddingHorizontalEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingHorizontalEnd, if (hasDividerPaddingHorizontal) dividerPaddingHorizontal else dm.mDividerPaddingHorizontalEnd)

                dm.mDividerColorVertical = attr.getColor(R.styleable.BaseViewGroup_dividerColorVertical, dm.mDividerColorVertical)
                dm.mDividerWidthVertical = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerWidthVertical, dm.mDividerWidthVertical)
                val hasDividerPaddingVertical = attr.hasValue(R.styleable.BaseViewGroup_dividerPaddingVertical)
                val dividerPaddingVertical = if (hasDividerPaddingVertical) attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingVertical, 0) else 0
                dm.mDividerPaddingVerticalStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingVerticalStart, if (hasDividerPaddingVertical) dividerPaddingVertical else dm.mDividerPaddingVerticalStart)
                dm.mDividerPaddingVerticalEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingVerticalEnd, if (hasDividerPaddingVertical) dividerPaddingVertical else dm.mDividerPaddingVerticalEnd)

            }
            return dm
        }
    }
}
