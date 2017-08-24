package com.rexy.widgets.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.rexy.widgetlayout.R

/**
 *
 *
 * <attr name="lineCenterHorizontal" format="boolean"></attr>
 *
 * <attr name="lineCenterVertical" format="boolean"></attr>
 *
 * <attr name="lineMinItemCount" format="integer"></attr>
 *
 * <attr name="lineMaxItemCount" format="integer"></attr>
 *
 * @author: rexy
 * @date: 2015-11-27 17:43
 */
open class WrapLayout : BaseViewGroup {
    //每行内容水平居中
    protected var mEachLineCenterHorizontal = false
    //每行内容垂直居中
    protected var mEachLineCenterVertical = false

    //每一行最少的Item 个数
    protected var mEachLineMinItemCount = 0
    //每一行最多的Item 个数
    protected var mEachLineMaxItemCount = 0

    //是否支持weight 属性。
    protected var mSupportWeight = false

    protected var mWeightSum = 0
    protected var mContentMaxWidthAccess = 0
    protected var mWeightView: SparseArray<View> = SparseArray(2)
    protected var mLineHeight = SparseIntArray(2)
    protected var mLineWidth = SparseIntArray(2)
    protected var mLineItemCount = SparseIntArray(2)
    protected var mLineEndIndex = SparseIntArray(2)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val attr = if (attrs == null) null else context.obtainStyledAttributes(attrs, R.styleable.WrapLayout)
        if (attr != null) {
            mEachLineMinItemCount = attr.getInt(R.styleable.WrapLayout_lineMinItemCount, mEachLineMinItemCount)
            mEachLineMaxItemCount = attr.getInt(R.styleable.WrapLayout_lineMaxItemCount, mEachLineMaxItemCount)
            mEachLineCenterHorizontal = attr.getBoolean(R.styleable.WrapLayout_lineCenterHorizontal, mEachLineCenterHorizontal)
            mEachLineCenterVertical = attr.getBoolean(R.styleable.WrapLayout_lineCenterVertical, mEachLineCenterVertical)
            mSupportWeight = attr.getBoolean(R.styleable.WrapLayout_weightSupport, mSupportWeight)
            attr.recycle()
        }
    }

    private fun ifNeedNewLine(child: View, attemptWidth: Int, countInLine: Int): Boolean {
        var needLine = false
        if (countInLine > 0) {
            if (countInLine >= mEachLineMinItemCount) {
                if (mEachLineMaxItemCount > 0 && countInLine >= mEachLineMaxItemCount) {
                    needLine = true
                } else {
                    if (attemptWidth > mContentMaxWidthAccess) {
                        needLine = !(mSupportWeight && mEachLineMinItemCount <= 0 && mEachLineMaxItemCount != 1)
                    }
                }
            }
        }
        return needLine
    }

    private fun adjustMeasureWithWeight(measureSpec: Int, remain: Int, r: IntArray, vertical: Boolean) {
        val size = mWeightView.size()
        val itemMargin = if (vertical) mBorderDivider!!.contentMarginVertical else mBorderDivider!!.contentMarginHorizontal
        for (i in 0..size - 1) {
            val childIndex = mWeightView.keyAt(i)
            val child = mWeightView.get(childIndex)
            val params = child.layoutParams as LayoutParams
            val oldParamsWidth = params.width
            val oldParamsHeight = params.height
            var childWidthMeasureSpec = measureSpec
            var childHeightMeasureSpec = measureSpec
            if (vertical) {
                r[1] += itemMargin
                childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec((remain * params.weight / mWeightSum).toInt(), View.MeasureSpec.EXACTLY)
                params.height = -1
            } else {
                r[0] += itemMargin
                childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec((remain * params.weight / mWeightSum).toInt(), View.MeasureSpec.EXACTLY)
                params.width = -1
            }
            params.measure(child, params.position(), childWidthMeasureSpec, childHeightMeasureSpec, 0, 0)
            params.width = oldParamsWidth
            params.height = oldParamsHeight
            insertMeasureInfo(params.width(child), params.height(child), childIndex, r, vertical)
        }
    }

    private fun insertMeasureInfo(itemWidth: Int, itemHeight: Int, childIndex: Int, r: IntArray, vertical: Boolean) {
        if (vertical) {
            r[0] = Math.max(r[0], itemWidth)
            r[1] += itemHeight
            var betterLineIndex = -1
            val lineSize = mLineEndIndex.size()
            for (lineIndex in lineSize - 1 downTo 0) {
                val line = mLineEndIndex.keyAt(lineIndex)
                if (childIndex > mLineEndIndex.get(line)) {
                    betterLineIndex = lineIndex
                    break
                }
            }
            betterLineIndex += 1
            val goodLine = if (betterLineIndex < mLineEndIndex.size()) mLineEndIndex.keyAt(betterLineIndex) else betterLineIndex
            for (lineIndex in lineSize - 1 downTo betterLineIndex) {
                val line = mLineEndIndex.keyAt(lineIndex)
                mLineEndIndex.put(line + 1, mLineEndIndex.get(line))
                mLineItemCount.put(line + 1, mLineItemCount.get(line))
                mLineWidth.put(line + 1, mLineWidth.get(line))
                mLineHeight.put(line + 1, mLineHeight.get(line))
            }
            mLineEndIndex.put(goodLine, childIndex)
            mLineItemCount.put(goodLine, 1)
            mLineWidth.put(goodLine, itemWidth)
            mLineHeight.put(goodLine, itemHeight)
        } else {
            r[0] += itemWidth
            r[1] = Math.max(r[1], itemHeight)
            mLineEndIndex.put(0, Math.max(mLineEndIndex.get(0), childIndex))
            mLineItemCount.put(0, mLineItemCount.get(0) + 1)
            mLineHeight.put(0, Math.max(mLineHeight.get(0), itemHeight))
            mLineWidth.put(0, mLineWidth.get(0) + itemWidth)
        }
    }

    override fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int) {
        val ignoreBeyondWidth = true
        val childCount = childCount
        mLineHeight.clear()
        mLineEndIndex.clear()
        mLineItemCount.clear()
        mLineWidth.clear()
        mWeightView.clear()
        mWeightSum = 0
        mContentMaxWidthAccess = View.MeasureSpec.getSize(widthMeasureSpecContent)
        var lastMeasureIndex = 0
        var currentLineIndex = 0
        var currentLineMaxWidth = 0
        var currentLineMaxHeight = 0
        var currentLineItemCount = 0

        var contentWidth = 0
        var contentHeight = 0
        var childState = 0
        var itemPosition = 0
        val middleMarginHorizontal = mBorderDivider!!.contentMarginHorizontal
        val middleMarginVertical = mBorderDivider!!.contentMarginVertical

        val supportWeight = mSupportWeight && (mEachLineMaxItemCount == 1 || mEachLineMinItemCount >= childCount || mEachLineMinItemCount <= 0)
        for (childIndex in 0..childCount - 1) {
            val child = getChildAt(childIndex)
            if (skipChild(child)) continue
            val params = child.layoutParams as LayoutParams
            if (params.weight > 0) {
                if (!mSupportWeight) {
                    throw IllegalArgumentException("use weight feature,should setSupportWeight true ")
                }
                mWeightSum += params.weight.toInt()
                if (supportWeight) {
                    mWeightView.put(childIndex, child)
                    continue
                }
            }
            lastMeasureIndex = childIndex
            params.measure(child, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, 0, contentHeight)
            val childWidthSpace = params.width(child)
            val childHeightSpace = params.height(child)
            childState = childState or child.measuredState
            if (ifNeedNewLine(child, childWidthSpace + currentLineMaxWidth + middleMarginHorizontal, currentLineItemCount)) {
                if (ignoreBeyondWidth || currentLineMaxWidth <= mContentMaxWidthAccess) {
                    contentWidth = Math.max(contentWidth, currentLineMaxWidth)
                }
                if (middleMarginVertical > 0) {
                    contentHeight += middleMarginVertical
                }
                contentHeight += currentLineMaxHeight
                mLineWidth.put(currentLineIndex, currentLineMaxWidth)
                mLineHeight.put(currentLineIndex, currentLineMaxHeight)
                mLineItemCount.put(currentLineIndex, currentLineItemCount)
                mLineEndIndex.put(currentLineIndex, childIndex - 1)
                currentLineIndex += 1
                currentLineItemCount = 1
                currentLineMaxWidth = childWidthSpace
                currentLineMaxHeight = childHeightSpace
            } else {
                if (currentLineItemCount > 0 && middleMarginHorizontal > 0) {
                    currentLineMaxWidth += middleMarginHorizontal
                }
                currentLineItemCount = currentLineItemCount + 1
                currentLineMaxWidth += childWidthSpace
                if (!ignoreBeyondWidth && currentLineMaxWidth <= mContentMaxWidthAccess) {
                    contentWidth = Math.max(contentWidth, currentLineMaxWidth)
                }
                currentLineMaxHeight = Math.max(currentLineMaxHeight, childHeightSpace)
            }
        }
        if (currentLineItemCount > 0) {
            if (ignoreBeyondWidth || currentLineMaxWidth <= mContentMaxWidthAccess) {
                contentWidth = Math.max(contentWidth, currentLineMaxWidth)
            }
            contentHeight += currentLineMaxHeight
            mLineWidth.put(currentLineIndex, currentLineMaxWidth)
            mLineHeight.put(currentLineIndex, currentLineMaxHeight)
            mLineItemCount.put(currentLineIndex, currentLineItemCount)
            mLineEndIndex.put(currentLineIndex, lastMeasureIndex)
        }
        val weightListSize = if (supportWeight) mWeightView.size() else 0
        if (weightListSize > 0) {
            val needAdjustMargin = mLineItemCount.size() == 0
            val vertical = mEachLineMaxItemCount == 1
            val measureSpec: Int
            val remain: Int
            val adjustMargin: Int
            if (vertical) {
                adjustMargin = (if (needAdjustMargin) weightListSize - 1 else weightListSize) * middleMarginVertical
                remain = View.MeasureSpec.getSize(heightMeasureSpecContent) - contentHeight - adjustMargin
                measureSpec = widthMeasureSpecContent
            } else {
                adjustMargin = (if (needAdjustMargin) weightListSize - 1 else weightListSize) * middleMarginHorizontal
                remain = View.MeasureSpec.getSize(widthMeasureSpecContent) - contentWidth - adjustMargin
                measureSpec = heightMeasureSpecContent
            }
            if (remain > mWeightView.size()) {
                val r = IntArray(2)
                adjustMeasureWithWeight(measureSpec, remain, r, vertical)
                if (vertical) {
                    contentHeight += r[1] + adjustMargin
                    contentWidth = Math.max(contentWidth, r[0])
                } else {
                    contentWidth += r[0] + adjustMargin
                    contentHeight = Math.max(contentHeight, r[1])
                    mLineWidth.put(0, mLineWidth.get(0) + adjustMargin)
                }
            }
            mWeightView.clear()
        }
        setContentSize(contentWidth, contentHeight, childState)
    }

    override fun dispatchLayout(contentLeft: Int, contentTop: Int) {
        val lineCount = mLineEndIndex.size()
        val gravity = gravity
        val lineVertical = mEachLineCenterVertical || gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.CENTER_VERTICAL && lineCount == 1
        val lineHorizontal = mEachLineCenterHorizontal || gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.CENTER_HORIZONTAL && lineCount == 1
        val middleMarginHorizontal = mBorderDivider!!.contentMarginHorizontal
        val middleMarginVertical = mBorderDivider!!.contentMarginVertical
        val contentWidthNoMargin = contentPureWidth
        var lineEndIndex: Int
        var lineMaxHeight: Int
        var childIndex = 0
        var lineTop = contentTop
        var childLeft: Int
        var childTop: Int
        var childRight: Int
        var childBottom: Int
        for (lineIndex in 0..lineCount - 1) {
            lineEndIndex = mLineEndIndex.get(lineIndex)
            lineMaxHeight = mLineHeight.get(lineIndex)
            childLeft = contentLeft
            if (lineHorizontal) {
                childLeft += (contentWidthNoMargin - mLineWidth.get(lineIndex)) / 2
            }
            while (childIndex <= lineEndIndex) {
                val child = getChildAt(childIndex)
                if (skipChild(child)) {
                    childIndex++
                    continue
                }
                val params = child.layoutParams as LayoutParams
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                childLeft += params.leftMargin()
                childRight = childLeft + childWidth

                childTop = getContentStartV(lineTop, lineTop + lineMaxHeight, childHeight, params.topMargin(), params.bottomMargin(), if (lineVertical) Gravity.CENTER_VERTICAL else params.gravity)
                childBottom = childTop + childHeight

                child.layout(childLeft, childTop, childRight, childBottom)

                childLeft = childRight + params.rightMargin()
                if (middleMarginHorizontal > 0) {
                    childLeft += middleMarginHorizontal
                }
                childIndex++
            }
            childIndex = lineEndIndex + 1
            lineTop += lineMaxHeight
            if (middleMarginVertical > 0) {
                lineTop += middleMarginVertical
            }
        }
    }

    override fun doAfterDraw(canvas: Canvas, inset: Rect) {
        val dividerHorizontal = mBorderDivider!!.isVisibleDividerHorizontal(true)
        val dividerVertical = mBorderDivider!!.isVisibleDividerVertical(true)
        if (dividerHorizontal || dividerVertical) {
            val lineCount = mLineEndIndex.size()
            val middleMarginHorizontal = mBorderDivider!!.contentMarginHorizontal
            val middleMarginVertical = mBorderDivider!!.contentMarginVertical
            val contentMarginTop = inset.top

            val parentLeft = paddingLeft
            val parentRight = width - paddingRight

            var lineIndex = 0
            var childIndex = 0
            var lineTop = contentTop + contentMarginTop
            var lineBottom: Int
            while (lineIndex < lineCount) {
                val lineEndIndex = mLineEndIndex.get(lineIndex)
                lineBottom = lineTop + mLineHeight.get(lineIndex)
                if (dividerHorizontal && lineIndex != lineCount - 1) {
                    mBorderDivider!!.drawDividerH(canvas, parentLeft.toFloat(), parentRight.toFloat(), (lineBottom + if (middleMarginVertical > 0) middleMarginVertical / 2 else 0).toFloat())
                }
                if (dividerVertical && mLineItemCount.get(lineIndex) > 1) {
                    while (childIndex < lineEndIndex) {
                        val child = getChildAt(childIndex)
                        if (skipChild(child)) {
                            childIndex++
                            continue
                        }
                        val params = child.layoutParams as LayoutParams
                        mBorderDivider!!.drawDividerV(canvas, lineTop.toFloat(), lineBottom.toFloat(), (child.right + params.rightMargin() + if (middleMarginHorizontal > 0) middleMarginHorizontal / 2 else 0).toFloat())
                        childIndex++
                    }
                }
                childIndex = lineEndIndex + 1
                lineTop = lineBottom
                if (middleMarginVertical > 0) {
                    lineTop += middleMarginVertical
                }
                lineIndex++
            }
        }
    }

    var eachLineMinItemCount: Int
        get() = mEachLineMinItemCount
        set(eachLineMinItemCount) {
            if (mEachLineMinItemCount != eachLineMinItemCount) {
                mEachLineMinItemCount = eachLineMinItemCount
                requestLayout()
            }
        }

    var eachLineMaxItemCount: Int
        get() = mEachLineMaxItemCount
        set(eachLineMaxItemCount) {
            if (mEachLineMaxItemCount != eachLineMaxItemCount) {
                mEachLineMaxItemCount = eachLineMaxItemCount
                requestLayout()
            }
        }

    var isEachLineCenterHorizontal: Boolean
        get() = mEachLineCenterHorizontal
        set(eachLineCenterHorizontal) {
            if (mEachLineCenterHorizontal != eachLineCenterHorizontal) {
                mEachLineCenterHorizontal = eachLineCenterHorizontal
                requestLayout()
            }
        }

    var isEachLineCenterVertical: Boolean
        get() = mEachLineCenterVertical
        set(eachLineCenterVertical) {
            if (mEachLineCenterVertical != eachLineCenterVertical) {
                mEachLineCenterVertical = eachLineCenterVertical
                requestLayout()
            }
        }

    var isSupportWeight: Boolean
        get() = mSupportWeight
        set(supportWeight) {
            if (mSupportWeight != supportWeight) {
                mSupportWeight = supportWeight
                if (mWeightSum > 0) {
                    requestLayout()
                }
            }
        }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return if (p is ViewGroup.MarginLayoutParams) {
            LayoutParams(p)
        } else LayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    class LayoutParams : BaseViewGroup.LayoutParams {
        var weight = 0f

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs!!) {
            val a = if (attrs == null) null else c.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.layout_weight))
            if (a != null) {
                weight = a.getFloat(0, weight)
            }
        }

        constructor(width: Int, height: Int) : super(width, height) {}

        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity) {}

        constructor(source: ViewGroup.LayoutParams) : super(source) {}

        constructor(source: ViewGroup.MarginLayoutParams) : super(source) {
            if (source is LayoutParams) {
                weight = source.weight
            }
            if (source is LinearLayout.LayoutParams) {
                weight = source.weight
            }
        }
    }

}
