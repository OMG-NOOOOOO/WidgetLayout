package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;

import com.rexy.widgetlayout.R;
import com.rexy.widgets.divider.BorderDivider;
import com.rexy.widgets.utils.ViewUtils;

/**
 * <!--水平方向Item 的间距-->
 * <!--每行内容水平居中-->
 * <attr name="lineCenterHorizontal" format="boolean"/>
 * <!--每行内容垂直居中-->
 * <attr name="lineCenterVertical" format="boolean"/>
 * <!--每一行最少的Item 个数-->
 * <attr name="lineMinItemCount" format="integer"/>
 * <!--每一行最多的Item 个数-->
 * <attr name="lineMaxItemCount" format="integer"/>
 *
 * @author: rexy
 * @date: 2015-11-27 17:43
 */
public class WrapLayout extends WidgetLayout {
    //每行内容水平居中
    protected boolean mEachLineCenterHorizontal = false;
    //每行内容垂直居中
    protected boolean mEachLineCenterVertical = false;

    //每一行最少的Item 个数
    protected int mEachLineMinItemCount = 0;
    //每一行最多的Item 个数
    protected int mEachLineMaxItemCount = 0;

    //是否支持weight 属性。
    protected boolean mSupportWeight = false;

    protected int mWeightSum = 0;
    protected int mContentMaxWidthAccess = 0;
    protected SparseArray<View> mWeightView = new SparseArray(2);
    protected SparseIntArray mLineHeight = new SparseIntArray(2);
    protected SparseIntArray mLineWidth = new SparseIntArray(2);
    protected SparseIntArray mLineItemCount = new SparseIntArray(2);
    protected SparseIntArray mLineEndIndex = new SparseIntArray(2);


    public WrapLayout(Context context) {
        super(context);
        init(context, null);
    }

    public WrapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WrapLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public WrapLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attr = attrs == null ? null : context.obtainStyledAttributes(attrs, R.styleable.WrapLayout);
        if (attr != null) {
            mEachLineMinItemCount = attr.getInt(R.styleable.WrapLayout_lineMinItemCount, mEachLineMinItemCount);
            mEachLineMaxItemCount = attr.getInt(R.styleable.WrapLayout_lineMaxItemCount, mEachLineMaxItemCount);
            mEachLineCenterHorizontal = attr.getBoolean(R.styleable.WrapLayout_lineCenterHorizontal, mEachLineCenterHorizontal);
            mEachLineCenterVertical = attr.getBoolean(R.styleable.WrapLayout_lineCenterVertical, mEachLineCenterVertical);
            mSupportWeight = attr.getBoolean(R.styleable.WrapLayout_weightSupport, mSupportWeight);
            attr.recycle();
        }
    }


    private boolean ifNeedNewLine(View child, int attemptWidth, int countInLine, boolean supportWeight) {
        boolean needLine = false;
        if (countInLine > 0) {
            if (countInLine >= mEachLineMinItemCount) {
                if (mEachLineMaxItemCount > 0 && countInLine >= mEachLineMaxItemCount) {
                    needLine = true;
                } else {
                    if (attemptWidth > mContentMaxWidthAccess) {
                        needLine = !supportWeight;
                    }
                }
            }
        }
        return needLine;
    }

    private void adjustMeasureWithWeight(int measureSpec, int remain, int[] r, boolean vertical) {
        final int size = mWeightView.size();
        final BorderDivider borderDivider = getBorderDivider();
        int itemMargin = vertical ? borderDivider.getItemMarginVertical() : borderDivider.getItemMarginHorizontal();
        for (int i = 0; i < size; i++) {
            int childIndex = mWeightView.keyAt(i);
            View child = mWeightView.get(childIndex);
            WidgetLayout.LayoutParams params = (WidgetLayout.LayoutParams) child.getLayoutParams();
            int oldW = params.width, oldH = params.height;
            int parentWidthSpec = measureSpec, parentHeightSpec = measureSpec;
            if (vertical) {
                r[1] += itemMargin;
                parentHeightSpec = View.MeasureSpec.makeMeasureSpec((int) ((remain * params.weight) / mWeightSum), View.MeasureSpec.EXACTLY);
                if (oldH == 0) {
                    params.height = -1;
                }
            } else {
                r[0] += itemMargin;
                parentWidthSpec = View.MeasureSpec.makeMeasureSpec((int) ((remain * params.weight) / mWeightSum), View.MeasureSpec.EXACTLY);
                if (oldW == 0) {
                    params.width = -1;
                }
            }
            measure(child, params.position(), parentWidthSpec, parentHeightSpec, 0, 0);
            insertMeasureInfo(params.width(child), params.height(child), childIndex, r, vertical);
            params.width = oldW;
            params.height = oldH;
        }
    }

    private void insertMeasureInfo(int itemWidth, int itemHeight, int childIndex, int[] r, boolean vertical) {
        if (vertical) {
            r[0] = Math.max(r[0], itemWidth);
            r[1] += itemHeight;
            int betterLineIndex = -1;
            int lineSize = mLineEndIndex.size();
            for (int lineIndex = lineSize - 1; lineIndex >= 0; lineIndex--) {
                int line = mLineEndIndex.keyAt(lineIndex);
                if (childIndex > mLineEndIndex.get(line)) {
                    betterLineIndex = lineIndex;
                    break;
                }
            }
            betterLineIndex += 1;
            int goodLine = betterLineIndex < mLineEndIndex.size() ? mLineEndIndex.keyAt(betterLineIndex) : betterLineIndex;
            for (int lineIndex = lineSize - 1; lineIndex >= betterLineIndex; lineIndex--) {
                int line = mLineEndIndex.keyAt(lineIndex);
                mLineEndIndex.put(line + 1, mLineEndIndex.get(line));
                mLineItemCount.put(line + 1, mLineItemCount.get(line));
                mLineWidth.put(line + 1, mLineWidth.get(line));
                mLineHeight.put(line + 1, mLineHeight.get(line));
            }
            mLineEndIndex.put(goodLine, childIndex);
            mLineItemCount.put(goodLine, 1);
            mLineWidth.put(goodLine, itemWidth);
            mLineHeight.put(goodLine, itemHeight);
        } else {
            r[0] += itemWidth;
            r[1] = Math.max(r[1], itemHeight);
            mLineEndIndex.put(0, Math.max(mLineEndIndex.get(0), childIndex));
            mLineItemCount.put(0, mLineItemCount.get(0) + 1);
            mLineHeight.put(0, Math.max(mLineHeight.get(0), itemHeight));
            mLineWidth.put(0, mLineWidth.get(0) + itemWidth);
        }
    }

    @Override
    protected void dispatchMeasure(int widthExcludeUnusedSpec, int heightExcludeUnusedSpec) {
        final boolean ignoreBeyondWidth = true;
        final int childCount = getChildCount();
        final BorderDivider borderDivider = getBorderDivider();
        mLineHeight.clear();
        mLineEndIndex.clear();
        mLineItemCount.clear();
        mLineWidth.clear();
        mWeightView.clear();
        mWeightSum = 0;
        mContentMaxWidthAccess = View.MeasureSpec.getSize(widthExcludeUnusedSpec);
        int contentMaxHeightAccess = View.MeasureSpec.getSize(heightExcludeUnusedSpec);
        int lastMeasureIndex = 0;
        int currentLineIndex = 0;
        int currentLineMaxWidth = 0;
        int currentLineMaxHeight = 0;
        int currentLineItemCount = 0;
        int contentWidth = 0, contentHeight = 0, childState = 0, itemPosition = 0;
        int middleMarginHorizontal = borderDivider.getItemMarginHorizontal();
        int middleMarginVertical = borderDivider.getItemMarginVertical();
        final boolean supportWeight = mSupportWeight && ((mEachLineMaxItemCount == 1) || (mEachLineMinItemCount <= 0 || mEachLineMinItemCount >= childCount));
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            final View child = getChildAt(childIndex);
            if (skipChild(child)) continue;
            WidgetLayout.LayoutParams params = (WidgetLayout.LayoutParams) child.getLayoutParams();
            params.setPosition(itemPosition++);
            if (supportWeight && params.weight > 0) {
                mWeightSum += params.weight;
                mWeightView.put(childIndex, child);
                continue;
            }
            lastMeasureIndex = childIndex;
            measure(child, params.position(), widthExcludeUnusedSpec, heightExcludeUnusedSpec, 0, contentHeight);
            int childWidthSpace = params.width(child);
            int childHeightSpace = params.height(child);
            childState |= child.getMeasuredState();
            if (ifNeedNewLine(child, childWidthSpace + currentLineMaxWidth + middleMarginHorizontal, currentLineItemCount, supportWeight)) {
                if (contentMaxHeightAccess < (contentHeight + childHeightSpace + currentLineMaxHeight)) {
                    measure(child, params.position(), widthExcludeUnusedSpec, heightExcludeUnusedSpec, 0, contentHeight + currentLineMaxHeight);
                    childWidthSpace = params.width(child);
                    childHeightSpace = params.height(child);
                }
                if (currentLineMaxWidth <= mContentMaxWidthAccess) {
                    contentWidth = Math.max(contentWidth, currentLineMaxWidth);
                } else {
                    contentWidth = ignoreBeyondWidth ? mContentMaxWidthAccess : currentLineMaxWidth;
                }
                if (middleMarginVertical > 0) {
                    contentHeight += middleMarginVertical;
                }
                contentHeight += currentLineMaxHeight;
                mLineWidth.put(currentLineIndex, currentLineMaxWidth);
                mLineHeight.put(currentLineIndex, currentLineMaxHeight);
                mLineItemCount.put(currentLineIndex, currentLineItemCount);
                mLineEndIndex.put(currentLineIndex, childIndex - 1);
                currentLineIndex++;
                currentLineItemCount = 1;
                currentLineMaxWidth = childWidthSpace;
                currentLineMaxHeight = childHeightSpace;
            } else {
                if (currentLineItemCount > 0 && middleMarginHorizontal > 0) {
                    currentLineMaxWidth += middleMarginHorizontal;
                }
                currentLineItemCount++;
                currentLineMaxWidth += childWidthSpace;
                currentLineMaxHeight = Math.max(currentLineMaxHeight, childHeightSpace);
            }
        }
        if (currentLineItemCount > 0) {
            if (currentLineMaxWidth <= mContentMaxWidthAccess) {
                contentWidth = Math.max(contentWidth, currentLineMaxWidth);
            } else {
                contentWidth = ignoreBeyondWidth ? mContentMaxWidthAccess : currentLineMaxWidth;
            }
            contentHeight += currentLineMaxHeight;
            mLineWidth.put(currentLineIndex, currentLineMaxWidth);
            mLineHeight.put(currentLineIndex, currentLineMaxHeight);
            mLineItemCount.put(currentLineIndex, currentLineItemCount);
            mLineEndIndex.put(currentLineIndex, lastMeasureIndex);
        }
        int weightListSize = supportWeight ? mWeightView.size() : 0;
        if (weightListSize > 0) {
            boolean allSupportWeight = mLineItemCount.size() == 0;
            boolean vertical = mEachLineMaxItemCount == 1;
            int measureSpec, remain, adjustMargin;
            if (vertical) {
                adjustMargin = (allSupportWeight ? weightListSize - 1 : weightListSize) * middleMarginVertical;
                remain = contentMaxHeightAccess - contentHeight - adjustMargin;
                measureSpec = widthExcludeUnusedSpec;
            } else {
                adjustMargin = (allSupportWeight ? weightListSize - 1 : weightListSize) * middleMarginHorizontal;
                remain = mContentMaxWidthAccess - contentWidth - adjustMargin;
                measureSpec = heightExcludeUnusedSpec;
            }
            if (remain > mWeightView.size()) {
                int[] r = new int[2];
                adjustMeasureWithWeight(measureSpec, remain, r, vertical);
                if (vertical) {
                    contentHeight += (r[1] + adjustMargin);
                    contentWidth = Math.max(contentWidth, r[0]);
                } else {
                    contentWidth += (r[0] + adjustMargin);
                    contentHeight = Math.max(contentHeight, r[1]);
                    mLineWidth.put(0, mLineWidth.get(0) + adjustMargin);
                }
            }
            mWeightView.clear();
        }
        setContentSize(contentWidth, contentHeight, childState);
    }

    @Override
    protected void dispatchLayout(int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        final BorderDivider borderDivider = getBorderDivider();
        final int lineCount = mLineEndIndex.size(), gravity = getGravity();
        final boolean lineVertical = mEachLineCenterVertical || ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.CENTER_VERTICAL && lineCount == 1);
        final int lineGravity = mEachLineCenterHorizontal ? Gravity.CENTER_HORIZONTAL : gravity;
        final int middleMarginHorizontal = borderDivider.getItemMarginHorizontal();
        final int middleMarginVertical = borderDivider.getItemMarginVertical();
        int lineEndIndex, lineMaxHeight, childIndex = 0, lineTop = contentTop, lineBottom;
        int childLeft, childTop, childRight, childBottom;
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            lineEndIndex = mLineEndIndex.get(lineIndex);
            lineMaxHeight = mLineHeight.get(lineIndex);
            childLeft = ViewUtils.getContentStartH(contentLeft, contentLeft + contentWidth, mLineWidth.get(lineIndex), 0, 0, lineGravity);
            lineBottom = lineTop + lineMaxHeight;
            for (; childIndex <= lineEndIndex; childIndex++) {
                final View child = getChildAt(childIndex);
                if (skipChild(child)) continue;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                WidgetLayout.LayoutParams params = (WidgetLayout.LayoutParams) child.getLayoutParams();
                if(mEachLineMaxItemCount==1&&!mEachLineCenterHorizontal){
                    childLeft = ViewUtils.getContentStartH(contentLeft, contentLeft + contentWidth, mLineWidth.get(lineIndex), 0, 0, params.gravity);
                }
                childLeft += params.leftMargin();
                childRight = childLeft + childWidth;
                childTop = ViewUtils.getContentStartV(lineTop, lineBottom, childHeight, params.topMargin(), params.bottomMargin(), lineVertical ? Gravity.CENTER_VERTICAL : params.gravity);
                childBottom = childTop + childHeight;
                child.layout(childLeft, childTop, childRight, childBottom);
                childLeft = childRight + params.rightMargin();
                if (middleMarginHorizontal > 0) {
                    childLeft += middleMarginHorizontal;
                }
            }
            childIndex = lineEndIndex + 1;
            lineTop = lineBottom + middleMarginVertical;
        }
    }

    @Override
    protected void doAfterDraw(Canvas canvas, int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        final BorderDivider borderDivider = getBorderDivider();
        boolean dividerHorizontal = borderDivider.isVisibleDividerHorizontal();
        boolean dividerVertical = borderDivider.isVisibleDividerVertical();
        if (dividerHorizontal || dividerVertical) {
            final int lineCount = mLineEndIndex.size();
            final int halfMiddleMarginHorizontal = borderDivider.getItemMarginHorizontal() / 2;
            final int halfMiddleMarginVertical = borderDivider.getItemMarginVertical() / 2;
            int parentLeft = getPaddingLeft();
            int parentRight = getWidth() - getPaddingRight();
            int parentBottom = getHeight() - getPaddingBottom();
            int contentBottomMargin = mContentInset.bottom;
            int lineIndex = 0, childIndex = 0;
            int lineTop = contentTop, lineBottom;
            for (; lineIndex < lineCount; lineIndex++) {
                int lineEndIndex = mLineEndIndex.get(lineIndex);
                lineBottom = lineTop + mLineHeight.get(lineIndex) + halfMiddleMarginVertical;
                if (dividerHorizontal && (lineBottom + contentBottomMargin < parentBottom)) {
                    borderDivider.drawDivider(canvas, parentLeft, parentRight, lineBottom, true);
                }
                if (dividerVertical && mLineItemCount.get(lineIndex) > 1) {
                    int dividerTop = lineTop - halfMiddleMarginVertical;
                    int dividerBottom = lineBottom;
                    for (; childIndex < lineEndIndex; childIndex++) {
                        final View child = getChildAt(childIndex);
                        if (skipChild(child)) continue;
                        WidgetLayout.LayoutParams params = (WidgetLayout.LayoutParams) child.getLayoutParams();
                        borderDivider.drawDivider(canvas, dividerTop, dividerBottom, child.getRight() + params.rightMargin() + halfMiddleMarginHorizontal, false);
                    }
                }
                childIndex = lineEndIndex + 1;
                lineTop = lineBottom + halfMiddleMarginVertical;
            }
        }
    }

    public int getEachLineMinItemCount() {
        return mEachLineMinItemCount;
    }

    public int getEachLineMaxItemCount() {
        return mEachLineMaxItemCount;
    }

    public boolean isEachLineCenterHorizontal() {
        return mEachLineCenterHorizontal;
    }

    public boolean isEachLineCenterVertical() {
        return mEachLineCenterVertical;
    }

    public boolean isSupportWeight() {
        return mSupportWeight;
    }

    public void setSupportWeight(boolean supportWeight) {
        if (mSupportWeight != supportWeight) {
            mSupportWeight = supportWeight;
            if (mWeightSum > 0) {
                requestLayoutIfNeed();
            }
        }
    }

    public void setEachLineMinItemCount(int eachLineMinItemCount) {
        if (mEachLineMinItemCount != eachLineMinItemCount) {
            mEachLineMinItemCount = eachLineMinItemCount;
            requestLayoutIfNeed();
        }
    }

    public void setEachLineMaxItemCount(int eachLineMaxItemCount) {
        if (mEachLineMaxItemCount != eachLineMaxItemCount) {
            mEachLineMaxItemCount = eachLineMaxItemCount;
            requestLayoutIfNeed();
        }
    }

    public void setEachLineCenterHorizontal(boolean eachLineCenterHorizontal) {
        if (mEachLineCenterHorizontal != eachLineCenterHorizontal) {
            mEachLineCenterHorizontal = eachLineCenterHorizontal;
            requestLayoutIfNeed();
        }
    }

    public void setEachLineCenterVertical(boolean eachLineCenterVertical) {
        if (mEachLineCenterVertical != eachLineCenterVertical) {
            mEachLineCenterVertical = eachLineCenterVertical;
            requestLayoutIfNeed();
        }
    }
}
