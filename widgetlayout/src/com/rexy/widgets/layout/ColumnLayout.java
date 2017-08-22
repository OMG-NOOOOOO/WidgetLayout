package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;

import com.rexy.widgetlayout.R;

import java.util.regex.Pattern;

/**
 * <!--列个数-->
 * <attr name="columnNumber" format="integer" />
 * <!--每行内容垂直居中-->
 * <attr name="columnCenterVertical" format="boolean"/>
 * <p>
 * <!--列内内容全展开的索引 * 或 1,3,5 类似列索引0 开始-->
 * <attr name="stretchColumns" format="string" />
 * <!--列内内容全靠中间 * 或 1,3,5 类似列索引0 开始-->
 * <attr name="alignCenterColumns" format="string" />
 * <!--列内内容全靠右 * 或 1,3,5 类似列索引0 开始-->
 * <attr name="alignRightColumns" format="string" />
 * <p>
 * <!--列宽和高的最大最小值限定-->
 * <attr name="columnMinWidth" format="dimension" />
 * <attr name="columnMaxWidth" format="dimension" />
 * <attr name="columnMinHeight" format="dimension" />
 * <attr name="columnMaxHeight" format="dimension" />
 * <p>
 * <!-- 列分割线颜色-->
 * <attr name="columnDividerColor" format="color"/>
 * <!--列分割线宽-->
 * <attr name="columnDividerWidth" format="dimension"/>
 * <!--列分割线开始 和结束padding-->
 * <attr name="columnDividerPadding" format="dimension"/>
 * <attr name="columnDividerPaddingStart" format="dimension"/>
 * <attr name="columnDividerPaddingEnd" format="dimension"/>
 *
 * @author: rexy
 * @date: 2015-11-27 17:43
 */
public class ColumnLayout extends BaseViewGroup {
    //列个数-
    int mColumnNumber = 1;
    //列内内容全展开的索引 * 或 1,3,5 类似列索引0 开始
    SparseBooleanArray mStretchColumns;
    //列内内容全靠中间 * 或 1,3,5 类似列索引0 开始
    SparseBooleanArray mAlignCenterColumns;
    //列内内容全靠右 * 或 1,3,5 类似列索引0 开始
    SparseBooleanArray mAlignRightColumns;

    //列的最小宽和高限定。
    int mColumnMinWidth = -1;
    int mColumnMaxWidth = -1;
    int mColumnMinHeight = -1;
    int mColumnMaxHeight = -1;
    boolean mColumnCenterVertical = true;

    private boolean mStretchAllColumns;
    private boolean mAlignCenterAllColumns;
    private boolean mAlignRightAllColumns;
    private int mColumnWidth;
    private SparseIntArray mLineHeight = new SparseIntArray(2);
    private SparseIntArray mLineLastIndex = new SparseIntArray(2);

    public ColumnLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ColumnLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColumnLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ColumnLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attr = attrs == null ? null : context.obtainStyledAttributes(attrs, R.styleable.ColumnLayout);
        if (attr != null) {
            mColumnNumber = attr.getInt(R.styleable.ColumnLayout_columnNumber, mColumnNumber);
            mColumnMinWidth = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMinWidth, mColumnMinWidth);
            mColumnMaxWidth = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMaxWidth, mColumnMaxWidth);
            mColumnMinHeight = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMinHeight, mColumnMinHeight);
            mColumnMaxHeight = attr.getDimensionPixelSize(R.styleable.ColumnLayout_columnMaxHeight, mColumnMaxHeight);
            mColumnCenterVertical = attr.getBoolean(R.styleable.ColumnLayout_columnCenterVertical, mColumnCenterVertical);

            String stretchableColumns = attr.getString(R.styleable.ColumnLayout_stretchColumns);
            if (stretchableColumns != null) {
                if (stretchableColumns.contains("*")) {
                    mStretchAllColumns = true;
                } else {
                    mStretchColumns = parseColumns(stretchableColumns);
                }
            }

            String alignCenterColumns = attr.getString(R.styleable.ColumnLayout_alignCenterColumns);
            if (alignCenterColumns != null) {
                if (alignCenterColumns.contains("*")) {
                    mAlignCenterAllColumns = true;
                } else {
                    mAlignCenterColumns = parseColumns(alignCenterColumns);
                }
            }

            String alignRightColumns = attr.getString(R.styleable.ColumnLayout_alignRightColumns);
            if (alignRightColumns != null) {
                if (alignRightColumns.contains("*")) {
                    mAlignRightAllColumns = true;
                } else {
                    mAlignRightColumns = parseColumns(alignRightColumns);
                }
            }
            attr.recycle();
        }
    }

    private static SparseBooleanArray parseColumns(String sequence) {
        SparseBooleanArray columns = new SparseBooleanArray();
        Pattern pattern = Pattern.compile("\\s*,\\s*");
        String[] columnDefs = pattern.split(sequence);
        for (String columnIdentifier : columnDefs) {
            try {
                int columnIndex = Integer.parseInt(columnIdentifier);
                if (columnIndex >= 0) {
                    columns.put(columnIndex, true);
                }
            } catch (NumberFormatException e) {
            }
        }
        return columns;
    }

    private int computeColumnWidth(int selfWidthNoPadding, int middleMarginHorizontal, int columnCount) {
        if (middleMarginHorizontal > 0) {
            selfWidthNoPadding -= (middleMarginHorizontal * (columnCount - 1));
        }
        selfWidthNoPadding = selfWidthNoPadding / columnCount;
        if (mColumnMaxWidth > 0 && selfWidthNoPadding > mColumnMaxWidth) {
            selfWidthNoPadding = mColumnMaxWidth;
        }
        if (selfWidthNoPadding < mColumnMinWidth) {
            selfWidthNoPadding = mColumnMinWidth;
        }
        return Math.max(selfWidthNoPadding, 0);
    }

    private int computeColumnHeight(int measureHeight) {
        if (mColumnMaxHeight > 0 && measureHeight > mColumnMaxHeight) {
            measureHeight = mColumnMaxHeight;
        }
        if (measureHeight < mColumnMinHeight) {
            measureHeight = mColumnMinHeight;
        }
        return measureHeight;
    }

    private void adjustMeasureAndSave(int lineIndex, int endIndex, int columnHeight, int columnCount) {
        mLineHeight.put(lineIndex, columnHeight);
        mLineLastIndex.put(lineIndex, endIndex);
        for (int columnIndex = columnCount - 1; columnIndex >= 0 && endIndex >= 0; endIndex--) {
            final View child = getChildAt(endIndex);
            if (skipChild(child)) continue;
            if (isColumnStretch(columnIndex)) {
                ColumnLayout.LayoutParams params = (LayoutParams) child.getLayoutParams();
                int childHeightWithMargin = params.height(child);
                if (childHeightWithMargin != columnHeight) {
                    params.measure(child, params.position()
                            , MeasureSpec.makeMeasureSpec(params.width(child), MeasureSpec.EXACTLY)
                            , MeasureSpec.makeMeasureSpec(columnHeight, MeasureSpec.EXACTLY));
                }
            }
            columnIndex--;
        }
    }

    @Override
    protected void dispatchMeasure(int widthMeasureSpecContent, int heightMeasureSpecContent) {
        final int childCount = getChildCount();
        final int columnCount = Math.max(1, mColumnNumber);
        final int middleMarginHorizontal = mBorderDivider.getContentMarginHorizontal();
        final int middleMarginVertical = mBorderDivider.getContentMarginVertical();

        mLineHeight.clear();
        mColumnWidth = computeColumnWidth(MeasureSpec.getSize(widthMeasureSpecContent), middleMarginHorizontal, columnCount);
        int heightMeasureSpec = heightMeasureSpecContent;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(mColumnWidth, MeasureSpec.getMode(widthMeasureSpecContent));
        int currentLineMaxHeight = 0;
        int contentHeight = 0, childState = 0, measuredCount = 0;
        int lineIndex, preLineIndex = 0, columnIndex = 0, itemPosition = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (skipChild(child)) continue;
            lineIndex = measuredCount / columnCount;
            if (lineIndex != preLineIndex) {
                currentLineMaxHeight = computeColumnHeight(currentLineMaxHeight);
                contentHeight += currentLineMaxHeight;
                adjustMeasureAndSave(preLineIndex, i - 1, currentLineMaxHeight, columnCount);
                preLineIndex = lineIndex;
                columnIndex = 0;
                currentLineMaxHeight = 0;
                if (middleMarginVertical > 0) {
                    contentHeight += middleMarginVertical;
                }
            }
            boolean stretchMeasure = isColumnStretch(columnIndex);
            ColumnLayout.LayoutParams params = (LayoutParams) child.getLayoutParams();
            int oldParamsWidth = params.width, tempParamsWidth = stretchMeasure ? -1 : params.width;
            params.width = tempParamsWidth;
            params.measure(child, itemPosition++, widthMeasureSpec, heightMeasureSpec, 0, contentHeight);
            params.width = oldParamsWidth;
            childState = childCount | child.getMeasuredState();
            int childHeightWithMargin = params.height(child);
            if (currentLineMaxHeight < childHeightWithMargin) {
                currentLineMaxHeight = childHeightWithMargin;
            }
            measuredCount++;
            columnIndex++;
        }
        if (childCount > 0 && currentLineMaxHeight > 0) {
            currentLineMaxHeight = computeColumnHeight(currentLineMaxHeight);
            contentHeight += currentLineMaxHeight;
            adjustMeasureAndSave(preLineIndex, childCount - 1, currentLineMaxHeight, columnIndex);
        }
        int contentWidth = mColumnWidth * columnCount + (middleMarginHorizontal <= 0 ? 0 : (middleMarginHorizontal * (columnCount - 1)));
        setContentSize(contentWidth, contentHeight, childState);
    }

    private int getAlignHorizontalGravity(int columnIndex, int defaultGravity) {
        if (isColumnAlignCenter(columnIndex)) {
            defaultGravity = Gravity.CENTER_HORIZONTAL;
        } else if (isColumnAlignRight(columnIndex)) {
            defaultGravity = Gravity.RIGHT;
        }
        return defaultGravity;
    }

    @Override
    protected void dispatchLayout(int contentLeft, int contentTop) {
        final int lineCount = mLineHeight.size();
        final int columnWidth = mColumnWidth;
        final int middleMarginHorizontal = mBorderDivider.getContentMarginHorizontal();
        final int middleMarginVertical = mBorderDivider.getContentMarginVertical();
        int childIndex = 0, childLastIndex, columnIndex;
        int columnLeft, columnTop = contentTop, columnRight, columnBottom;
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            columnIndex = 0;
            childLastIndex = mLineLastIndex.get(lineIndex);
            columnLeft = contentLeft;
            columnBottom = columnTop + mLineHeight.get(lineIndex);
            for (; childIndex <= childLastIndex; childIndex++) {
                final View child = getChildAt(childIndex);
                if (skipChild(child)) continue;
                ColumnLayout.LayoutParams params = (LayoutParams) child.getLayoutParams();
                columnRight = columnLeft + columnWidth;
                int gravityHorizontal = getAlignHorizontalGravity(columnIndex, params.gravity);
                int gravityVertical = mColumnCenterVertical ? Gravity.CENTER_VERTICAL : params.gravity;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int childLeft = getContentStartH(columnLeft, columnRight, childWidth, params.leftMargin(), params.rightMargin(), gravityHorizontal);
                int childTop = getContentStartV(columnTop, columnBottom, childHeight, params.topMargin(), params.bottomMargin(), gravityVertical);
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                columnLeft = columnRight;
                if (middleMarginHorizontal > 0) {
                    columnLeft += middleMarginHorizontal;
                }
                columnIndex++;
            }
            childIndex = childLastIndex + 1;
            columnTop = columnBottom;
            if (middleMarginVertical > 0) {
                columnTop += middleMarginVertical;
            }
        }
    }

    @Override
    protected void doAfterDraw(Canvas canvas, Rect inset) {
        final int lineCount = mLineHeight.size();
        boolean dividerHorizontal = mBorderDivider.isVisibleDividerHorizontal(true) && lineCount > 1;
        boolean dividerVertical = mBorderDivider.isVisibleDividerVertical(true) && mColumnNumber > 1;
        if (dividerHorizontal || dividerVertical) {
            final int columnWidth = mColumnWidth;
            final int middleMarginHorizontal = mBorderDivider.getContentMarginHorizontal();
            final int middleMarginVertical = mBorderDivider.getContentMarginVertical();
            final int contentMarginLeft = inset.left, contentMarginTop = inset.top, contentMarginBottom = inset.bottom;
            int parentLeft = getPaddingLeft(), parentRight = getWidth() - getPaddingRight(), parentBottom = getHeight() - getPaddingBottom();
            int contentLeft = getContentLeft();
            int maxColumnIndex = Math.max(mColumnNumber - 1, 0), mColumnIndex;
            int childIndex = 0, childLastIndex;
            int columnLeft, columnTop = getContentTop() + contentMarginTop, columnRight, columnBottom;
            int halfMiddleVertical = middleMarginVertical > 0 ? (middleMarginVertical / 2) : 0;
            boolean bottomCoincide = false;
            for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
                childLastIndex = mLineLastIndex.get(lineIndex);
                columnLeft = contentLeft + contentMarginLeft;
                columnBottom = columnTop + mLineHeight.get(lineIndex) + halfMiddleVertical;
                if (dividerHorizontal) {
                    if (lineIndex < lineCount - 1 || (bottomCoincide = (columnBottom + contentMarginBottom) < parentBottom)) {
                        mBorderDivider.drawDividerH(canvas, parentLeft, parentRight, columnBottom);
                    }
                }
                if (dividerVertical) {
                    mColumnIndex = 0;
                    int dividerTop = columnTop - (lineIndex == 0 ? contentMarginTop : halfMiddleVertical);
                    int dividerBottom = columnBottom + (lineIndex == lineCount - 1 && !bottomCoincide ? contentMarginBottom : 0);
                    for (; childIndex <= childLastIndex; childIndex++) {
                        final View child = getChildAt(lineIndex);
                        if (mColumnIndex == maxColumnIndex || skipChild(child)) continue;
                        columnRight = columnLeft + columnWidth;
                        mBorderDivider.drawDividerV(canvas, dividerTop, dividerBottom, columnRight + (middleMarginHorizontal > 0 ? middleMarginHorizontal / 2 : 0));
                        columnLeft = columnRight;
                        if (middleMarginHorizontal > 0) {
                            columnLeft += middleMarginHorizontal;
                        }
                        mColumnIndex++;
                    }
                }
                childIndex = childLastIndex + 1;
                columnTop = columnBottom + halfMiddleVertical;
            }
        }
    }

    public boolean isColumnAlignCenter(int columnIndex) {
        return mAlignCenterAllColumns || (mAlignCenterColumns != null && mAlignCenterColumns.get(columnIndex, false));
    }

    public boolean isColumnAlignRight(int columnIndex) {
        return mAlignRightAllColumns || (mAlignRightColumns != null && mAlignRightColumns.get(columnIndex, false));
    }

    public boolean isColumnAlignLeft(int columnIndex) {
        return !(isColumnAlignCenter(columnIndex) || isColumnAlignRight(columnIndex));
    }

    public boolean isColumnStretch(int columnIndex) {
        return mStretchAllColumns || (mStretchColumns != null && mStretchColumns.get(columnIndex, false));
    }

    public boolean isColumnCenterVertical() {
        return mColumnCenterVertical;
    }

    public int getColumnMinWidth() {
        return mColumnMinWidth;
    }

    public int getColumnMaxWidth() {
        return mColumnMaxWidth;
    }

    public int getColumnMinHeight() {
        return mColumnMinHeight;
    }

    public int getColumnMaxHeight() {
        return mColumnMaxHeight;
    }

    public void setColumnNumber(int columnNumber) {
        if (mColumnNumber != columnNumber) {
            mColumnNumber = columnNumber;
            requestLayout();
        }
    }

    public void setColumnCenterVertical(boolean columnCenterVertical) {
        if (mColumnCenterVertical != columnCenterVertical) {
            mColumnCenterVertical = columnCenterVertical;
            requestLayout();
        }
    }

    public void setColumnMinWidth(int columnMinWidth) {
        if (mColumnMinWidth != columnMinWidth) {
            mColumnMinWidth = columnMinWidth;
            requestLayout();
        }
    }

    public void setColumnMaxWidth(int columnMaxWidth) {
        if (mColumnMaxWidth != columnMaxWidth) {
            mColumnMaxWidth = columnMaxWidth;
            requestLayout();
        }
    }

    public void setColumnMinHeight(int columnMinHeight) {
        if (mColumnMinHeight != columnMinHeight) {
            mColumnMinHeight = columnMinHeight;
            requestLayout();
        }
    }

    public void setColumnMaxHeight(int columnMaxHeight) {
        if (mColumnMaxHeight != columnMaxHeight) {
            mColumnMaxHeight = columnMaxHeight;
            requestLayout();
        }
    }
}
