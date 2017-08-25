package com.rexy.widgets.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.view.Gravity
import android.view.View

import com.rexy.widgetlayout.R

import java.util.regex.Pattern

/**
 *
 * <attr name="columnNumber" format="integer"></attr>
 *
 * <attr name="columnCenterVertical" format="boolean"></attr>
 *
 *
 *
 * <attr name="stretchColumns" format="string"></attr>
 *
 * <attr name="alignCenterColumns" format="string"></attr>
 *
 * <attr name="alignRightColumns" format="string"></attr>
 *
 *
 *
 * <attr name="columnMinWidth" format="dimension"></attr>
 * <attr name="columnMaxWidth" format="dimension"></attr>
 * <attr name="columnMinHeight" format="dimension"></attr>
 * <attr name="columnMaxHeight" format="dimension"></attr>
 *
 *
 *
 * <attr name="columnDividerColor" format="color"></attr>
 *
 * <attr name="columnDividerWidth" format="dimension"></attr>
 *
 * <attr name="columnDividerPadding" format="dimension"></attr>
 * <attr name="columnDividerPaddingStart" format="dimension"></attr>
 * <attr name="columnDividerPaddingEnd" format="dimension"></attr>
 *
 * @author: rexy
 * @date: 2015-11-27 17:43
 */
class ColumnLayout : BaseViewGroup {
    //列个数-
    private var mColumnNumber = 1
    //列内内容全展开的索引 * 或 1,3,5 类似列索引0 开始
    private var mStretchColumns: SparseBooleanArray? = null
    //列内内容全靠中间 * 或 1,3,5 类似列索引0 开始
    private var mAlignCenterColumns: SparseBooleanArray? = null
    //列内内容全靠右 * 或 1,3,5 类似列索引0 开始
    private var mAlignRightColumns: SparseBooleanArray? = null

    //列的最小宽和高限定。
    private var mColumnMinWidth = -1
    private var mColumnMaxWidth = -1
    private var mColumnMinHeight = -1
    private var mColumnMaxHeight = -1
    private var mColumnCenterVertical = true

    private var mStretchAllColumns: Boolean = false
    private var mAlignCenterAllColumns: Boolean = false
    private var mAlignRightAllColumns: Boolean = false
    private var mColumnWidth: Int = 0
    private val mLineHeight = SparseIntArray(2)
    private val mLineLastIndex = SparseIntArray(2)

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
        val attr = if (attrs == null) null else context.obtainStyledAttributes(attrs, R.styleable.ColumnLayout)
        if (attr != null) {
            mColumnNumber = attr.getInt(R.styleable.ColumnLayout_columnNumber, mColumnNumber)
            mColumnMinWidth = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMinWidth, mColumnMinWidth)
            mColumnMaxWidth = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMaxWidth, mColumnMaxWidth)
            mColumnMinHeight = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMinHeight, mColumnMinHeight)
            mColumnMaxHeight = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMaxHeight, mColumnMaxHeight)
            mColumnCenterVertical = attr.getBoolean(R.styleable.ColumnLayout_columnCenterVertical, mColumnCenterVertical)

            val stretchableColumns = attr.getString(R.styleable.ColumnLayout_stretchColumns)
            if (stretchableColumns != null) {
                if (stretchableColumns.contains("*")) {
                    mStretchAllColumns = true
                } else {
                    mStretchColumns = parseColumns(stretchableColumns)
                }
            }

            val alignCenterColumns = attr.getString(R.styleable.ColumnLayout_alignCenterColumns)
            if (alignCenterColumns != null) {
                if (alignCenterColumns.contains("*")) {
                    mAlignCenterAllColumns = true
                } else {
                    mAlignCenterColumns = parseColumns(alignCenterColumns)
                }
            }

            val alignRightColumns = attr.getString(R.styleable.ColumnLayout_alignRightColumns)
            if (alignRightColumns != null) {
                if (alignRightColumns.contains("*")) {
                    mAlignRightAllColumns = true
                } else {
                    mAlignRightColumns = parseColumns(alignRightColumns)
                }
            }
            attr.recycle()
        }
    }

    private fun parseColumns(sequence: String): SparseBooleanArray {
        val columns = SparseBooleanArray()
        val pattern = Pattern.compile("\\s*,\\s*")
        val columnDefs = pattern.split(sequence)
        for (columnIdentifier in columnDefs) {
            try {
                val columnIndex = Integer.parseInt(columnIdentifier)
                if (columnIndex >= 0) {
                    columns.put(columnIndex, true)
                }
            } catch (e: NumberFormatException) {
            }

        }
        return columns
    }

    private fun computeColumnWidth(selfWidthNoPadding: Int, middleMarginHorizontal: Int, columnCount: Int): Int {
        var selfWidthNoPadding = selfWidthNoPadding
        if (middleMarginHorizontal > 0) {
            selfWidthNoPadding -= middleMarginHorizontal * (columnCount - 1)
        }
        selfWidthNoPadding = selfWidthNoPadding / columnCount
        if (mColumnMaxWidth > 0 && selfWidthNoPadding > mColumnMaxWidth) {
            selfWidthNoPadding = mColumnMaxWidth
        }
        if (selfWidthNoPadding < mColumnMinWidth) {
            selfWidthNoPadding = mColumnMinWidth
        }
        return Math.max(selfWidthNoPadding, 0)
    }

    private fun computeColumnHeight(measureHeight: Int): Int {
        var measureHeight = measureHeight
        if (mColumnMaxHeight > 0 && measureHeight > mColumnMaxHeight) {
            measureHeight = mColumnMaxHeight
        }
        if (measureHeight < mColumnMinHeight) {
            measureHeight = mColumnMinHeight
        }
        return measureHeight
    }

    private fun adjustMeasureAndSave(lineIndex: Int, endIndex: Int, columnHeight: Int, columnCount: Int) {
        var endIndex = endIndex
        mLineHeight.put(lineIndex, columnHeight)
        mLineLastIndex.put(lineIndex, endIndex)
        var columnIndex = columnCount - 1
        while (columnIndex >= 0 && endIndex >= 0) {
            val child = getChildAt(endIndex)
            if (skipChild(child)) {
                endIndex--
                continue
            }
            if (isColumnStretch(columnIndex)) {
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                val childHeightWithMargin = params.height(child)
                if (childHeightWithMargin != columnHeight) {
                    params.measure(child, params.position(), View.MeasureSpec.makeMeasureSpec(params.width(child), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(columnHeight, View.MeasureSpec.EXACTLY))
                }
            }
            columnIndex--
            endIndex--
        }
    }

    override fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int) {
        val childCount = childCount
        val columnCount = Math.max(1, mColumnNumber)
        val middleMarginHorizontal = mBorderDivider!!.contentMarginHorizontal
        val middleMarginVertical = mBorderDivider!!.contentMarginVertical

        mLineHeight.clear()
        mColumnWidth = computeColumnWidth(View.MeasureSpec.getSize(widthMeasureSpecContent), middleMarginHorizontal, columnCount)
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mColumnWidth, View.MeasureSpec.getMode(widthMeasureSpecContent))
        var currentLineMaxHeight = 0
        var contentHeight = 0
        var childState = 0
        var measuredCount = 0
        var lineIndex: Int
        var preLineIndex = 0
        var columnIndex = 0
        var itemPosition = 0
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (skipChild(child)) continue
            lineIndex = measuredCount / columnCount
            if (lineIndex != preLineIndex) {
                currentLineMaxHeight = computeColumnHeight(currentLineMaxHeight)
                contentHeight += currentLineMaxHeight
                adjustMeasureAndSave(preLineIndex, i - 1, currentLineMaxHeight, columnCount)
                preLineIndex = lineIndex
                columnIndex = 0
                currentLineMaxHeight = 0
                if (middleMarginVertical > 0) {
                    contentHeight += middleMarginVertical
                }
            }
            val stretchMeasure = isColumnStretch(columnIndex)
            val params = child.layoutParams as BaseViewGroup.LayoutParams
            val oldParamsWidth = params.width
            val tempParamsWidth = if (stretchMeasure) -1 else params.width
            params.width = tempParamsWidth
            params.measure(child, itemPosition++, widthMeasureSpec, heightMeasureSpecContent, 0, contentHeight)
            params.width = oldParamsWidth
            childState = childCount or child.measuredState
            val childHeightWithMargin = params.height(child)
            if (currentLineMaxHeight < childHeightWithMargin) {
                currentLineMaxHeight = childHeightWithMargin
            }
            measuredCount++
            columnIndex++
        }
        if (childCount > 0 && currentLineMaxHeight > 0) {
            currentLineMaxHeight = computeColumnHeight(currentLineMaxHeight)
            contentHeight += currentLineMaxHeight
            adjustMeasureAndSave(preLineIndex, childCount - 1, currentLineMaxHeight, columnIndex)
        }
        val contentWidth = mColumnWidth * columnCount + if (middleMarginHorizontal <= 0) 0 else middleMarginHorizontal * (columnCount - 1)
        setContentSize(contentWidth, contentHeight, childState)
    }

    private fun getAlignHorizontalGravity(columnIndex: Int, defaultGravity: Int): Int {
        var defaultGravity = defaultGravity
        if (isColumnAlignCenter(columnIndex)) {
            defaultGravity = Gravity.CENTER_HORIZONTAL
        } else if (isColumnAlignRight(columnIndex)) {
            defaultGravity = Gravity.RIGHT
        }
        return defaultGravity
    }

    override fun dispatchLayout(contentLeft: Int, contentTop: Int) {
        val lineCount = mLineHeight.size()
        val columnWidth = mColumnWidth
        val middleMarginHorizontal = mBorderDivider!!.contentMarginHorizontal
        val middleMarginVertical = mBorderDivider!!.contentMarginVertical
        var childIndex = 0
        var childLastIndex: Int
        var columnIndex: Int
        var columnLeft: Int
        var columnTop = contentTop
        var columnRight: Int
        var columnBottom: Int
        for (lineIndex in 0..lineCount - 1) {
            columnIndex = 0
            childLastIndex = mLineLastIndex.get(lineIndex)
            columnLeft = contentLeft
            columnBottom = columnTop + mLineHeight.get(lineIndex)
            while (childIndex <= childLastIndex) {
                val child = getChildAt(childIndex)
                if (skipChild(child)) {
                    childIndex++
                    continue
                }
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                columnRight = columnLeft + columnWidth
                val gravityHorizontal = getAlignHorizontalGravity(columnIndex, params.gravity)
                val gravityVertical = if (mColumnCenterVertical) Gravity.CENTER_VERTICAL else params.gravity
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val childLeft = getContentStartH(columnLeft, columnRight, childWidth, params.leftMargin(), params.rightMargin(), gravityHorizontal)
                val childTop = getContentStartV(columnTop, columnBottom, childHeight, params.topMargin(), params.bottomMargin(), gravityVertical)
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
                columnLeft = columnRight
                if (middleMarginHorizontal > 0) {
                    columnLeft += middleMarginHorizontal
                }
                columnIndex++
                childIndex++
            }
            childIndex = childLastIndex + 1
            columnTop = columnBottom
            if (middleMarginVertical > 0) {
                columnTop += middleMarginVertical
            }
        }
    }

    override fun doAfterDraw(canvas: Canvas, inset: Rect) {
        val lineCount = mLineHeight.size()
        val dividerHorizontal = mBorderDivider!!.isVisibleDividerHorizontal(true) && lineCount > 1
        val dividerVertical = mBorderDivider!!.isVisibleDividerVertical(true) && mColumnNumber > 1
        if (dividerHorizontal || dividerVertical) {
            val columnWidth = mColumnWidth
            val middleMarginHorizontal = mBorderDivider!!.contentMarginHorizontal
            val middleMarginVertical = mBorderDivider!!.contentMarginVertical
            val contentMarginLeft = inset.left
            val contentMarginTop = inset.top
            val contentMarginBottom = inset.bottom
            val parentLeft = paddingLeft
            val parentRight = width - paddingRight
            val parentBottom = height - paddingBottom
            val contentLeft = contentLeft
            val maxColumnIndex = Math.max(mColumnNumber - 1, 0)
            var mColumnIndex: Int
            var childIndex = 0
            var childLastIndex: Int
            var columnLeft: Int
            var columnTop = contentTop + contentMarginTop
            var columnRight: Int
            var columnBottom: Int
            val halfMiddleVertical = if (middleMarginVertical > 0) middleMarginVertical / 2 else 0
            var bottomCoincide = false
            for (lineIndex in 0 until lineCount) {
                childLastIndex = mLineLastIndex.get(lineIndex)
                columnLeft = contentLeft + contentMarginLeft
                columnBottom = columnTop + mLineHeight.get(lineIndex) + halfMiddleVertical
                if (dividerHorizontal) {
                    if(lineIndex < lineCount - 1 ){
                        mBorderDivider!!.drawDividerH(canvas, parentLeft.toFloat(), parentRight.toFloat(), columnBottom.toFloat())
                    }else{
                        bottomCoincide = columnBottom + contentMarginBottom < parentBottom
                        if(bottomCoincide){
                            mBorderDivider!!.drawDividerH(canvas, parentLeft.toFloat(), parentRight.toFloat(), columnBottom.toFloat())
                        }
                    }
                }
                if (dividerVertical) {
                    mColumnIndex = 0
                    val dividerTop = columnTop - if (lineIndex == 0) contentMarginTop else halfMiddleVertical
                    val dividerBottom = columnBottom + if (lineIndex == lineCount - 1 && !bottomCoincide) contentMarginBottom else 0
                    while (childIndex <= childLastIndex) {
                        val child = getChildAt(lineIndex)
                        if (mColumnIndex == maxColumnIndex || skipChild(child)) {
                            childIndex++
                            continue
                        }
                        columnRight = columnLeft + columnWidth
                        mBorderDivider!!.drawDividerV(canvas, dividerTop.toFloat(), dividerBottom.toFloat(), (columnRight + if (middleMarginHorizontal > 0) middleMarginHorizontal / 2 else 0).toFloat())
                        columnLeft = columnRight
                        if (middleMarginHorizontal > 0) {
                            columnLeft += middleMarginHorizontal
                        }
                        mColumnIndex++
                        childIndex++
                    }
                }
                childIndex = childLastIndex + 1
                columnTop = columnBottom + halfMiddleVertical
            }
        }
    }

    fun isColumnAlignCenter(columnIndex: Int): Boolean {
        return mAlignCenterAllColumns || mAlignCenterColumns != null && mAlignCenterColumns!!.get(columnIndex, false)
    }

    fun isColumnAlignRight(columnIndex: Int): Boolean {
        return mAlignRightAllColumns || mAlignRightColumns != null && mAlignRightColumns!!.get(columnIndex, false)
    }

    fun isColumnAlignLeft(columnIndex: Int): Boolean {
        return !(isColumnAlignCenter(columnIndex) || isColumnAlignRight(columnIndex))
    }

    fun isColumnStretch(columnIndex: Int): Boolean {
        return mStretchAllColumns || mStretchColumns != null && mStretchColumns!!.get(columnIndex, false)
    }

    var isColumnCenterVertical: Boolean
        get() = mColumnCenterVertical
        set(columnCenterVertical) {
            if (mColumnCenterVertical != columnCenterVertical) {
                mColumnCenterVertical = columnCenterVertical
                requestLayout()
            }
        }

    var columnMinWidth: Int
        get() = mColumnMinWidth
        set(columnMinWidth) {
            if (mColumnMinWidth != columnMinWidth) {
                mColumnMinWidth = columnMinWidth
                requestLayout()
            }
        }

    var columnMaxWidth: Int
        get() = mColumnMaxWidth
        set(columnMaxWidth) {
            if (mColumnMaxWidth != columnMaxWidth) {
                mColumnMaxWidth = columnMaxWidth
                requestLayout()
            }
        }

    var columnMinHeight: Int
        get() = mColumnMinHeight
        set(columnMinHeight) {
            if (mColumnMinHeight != columnMinHeight) {
                mColumnMinHeight = columnMinHeight
                requestLayout()
            }
        }

    var columnMaxHeight: Int
        get() = mColumnMaxHeight
        set(columnMaxHeight) {
            if (mColumnMaxHeight != columnMaxHeight) {
                mColumnMaxHeight = columnMaxHeight
                requestLayout()
            }
        }

    fun setColumnNumber(columnNumber: Int) {
        if (mColumnNumber != columnNumber) {
            mColumnNumber = columnNumber
            requestLayout()
        }
    }
}
