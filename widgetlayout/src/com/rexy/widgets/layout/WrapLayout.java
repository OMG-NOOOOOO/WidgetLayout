package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rexy.widgetlayout.R;

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
public class WrapLayout extends BaseViewGroup {
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

    private boolean ifNeedNewLine(View child, int attemptWidth, int countInLine) {
        boolean needLine = false;
        if (countInLine > 0) {
            if (countInLine >= mEachLineMinItemCount) {
                if (mEachLineMaxItemCount > 0 && countInLine >= mEachLineMaxItemCount) {
                    needLine = true;
                } else {
                    if (attemptWidth > mContentMaxWidthAccess) {
                        needLine = !(mSupportWeight && mEachLineMinItemCount <= 0 && mEachLineMaxItemCount != 1);
                    }
                }
            }
        }
        return needLine;
    }

    private void adjustMeasureWithWeight(int measureSpec, int remain, int[] r, boolean vertical) {
        int size = mWeightView.size();
        int itemMargin = vertical ? mBorderDivider.getContentMarginVertical() : mBorderDivider.getContentMarginHorizontal();
        for (int i = 0; i < size; i++) {
            int childIndex = mWeightView.keyAt(i);
            View child = mWeightView.get(childIndex);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int oldParamsWidth = params.width, oldParamsHeight = params.height;
            int childWidthMeasureSpec = measureSpec, childHeightMeasureSpec = measureSpec;
            if (vertical) {
                r[1] += itemMargin;
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) ((remain * params.weight) / mWeightSum), MeasureSpec.EXACTLY);
                params.height = -1;
            } else {
                r[0] += itemMargin;
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) ((remain * params.weight) / mWeightSum), MeasureSpec.EXACTLY);
                params.width = -1;
            }
            params.measure(child, params.position(), childWidthMeasureSpec, childHeightMeasureSpec, 0, 0);
            params.width = oldParamsWidth;
            params.height = oldParamsHeight;
            insertMeasureInfo(params.width(child), params.height(child), childIndex, r, vertical);
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
    protected void dispatchMeasure(int widthMeasureSpecContent, int heightMeasureSpecContent) {
        final boolean ignoreBeyondWidth = true;
        final int childCount = getChildCount();
        mLineHeight.clear();
        mLineEndIndex.clear();
        mLineItemCount.clear();
        mLineWidth.clear();
        mWeightView.clear();
        mWeightSum = 0;
        mContentMaxWidthAccess = MeasureSpec.getSize(widthMeasureSpecContent);
        int contentMaxHeightAccess = MeasureSpec.getSize(heightMeasureSpecContent);
        int lastMeasureIndex = 0;
        int currentLineIndex = 0;
        int currentLineMaxWidth = 0;
        int currentLineMaxHeight = 0;
        int currentLineItemCount = 0;

        int contentWidth = 0, contentHeight = 0, childState = 0, itemPosition = 0;
        int middleMarginHorizontal = mBorderDivider.getContentMarginHorizontal();
        int middleMarginVertical = mBorderDivider.getContentMarginVertical();

        final boolean supportWeight = mSupportWeight && ((mEachLineMaxItemCount == 1) || (mEachLineMinItemCount >= childCount || mEachLineMinItemCount <= 0));
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            final View child = getChildAt(childIndex);
            if (skipChild(child)) continue;
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.weight > 0) {
                if (!mSupportWeight) {
                    throw new IllegalArgumentException("use weight feature,should setSupportWeight true ");
                }
                mWeightSum += params.weight;
                if (supportWeight) {
                    mWeightView.put(childIndex, child);
                    continue;
                }
            }
            lastMeasureIndex = childIndex;
            params.measure(child, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, 0, contentHeight);
            int childWidthSpace = params.width(child);
            int childHeightSpace = params.height(child);
            childState |= child.getMeasuredState();
            if (ifNeedNewLine(child, childWidthSpace + currentLineMaxWidth + middleMarginHorizontal, currentLineItemCount)) {
                if (contentMaxHeightAccess < (contentHeight + childHeightSpace + currentLineMaxHeight)) {
                    params.measure(child, itemPosition, widthMeasureSpecContent, heightMeasureSpecContent, 0, contentHeight+currentLineMaxHeight);
                    childWidthSpace = params.width(child);
                    childHeightSpace = params.height(child);
                }
                if (ignoreBeyondWidth || currentLineMaxWidth <= mContentMaxWidthAccess) {
                    contentWidth = Math.max(contentWidth, currentLineMaxWidth);
                }
                if (middleMarginVertical > 0) {
                    contentHeight += middleMarginVertical;
                }
                contentHeight += currentLineMaxHeight;
                mLineWidth.put(currentLineIndex, currentLineMaxWidth);
                mLineHeight.put(currentLineIndex, currentLineMaxHeight);
                mLineItemCount.put(currentLineIndex, currentLineItemCount);
                mLineEndIndex.put(currentLineIndex, childIndex - 1);
                currentLineIndex += 1;
                currentLineItemCount = 1;
                currentLineMaxWidth = childWidthSpace;
                currentLineMaxHeight = childHeightSpace;
            } else {
                if (currentLineItemCount > 0 && middleMarginHorizontal > 0) {
                    currentLineMaxWidth += middleMarginHorizontal;
                }
                currentLineItemCount = currentLineItemCount + 1;
                currentLineMaxWidth += childWidthSpace;
                if (!ignoreBeyondWidth && currentLineMaxWidth <= mContentMaxWidthAccess) {
                    contentWidth = Math.max(contentWidth, currentLineMaxWidth);
                }
                currentLineMaxHeight = Math.max(currentLineMaxHeight, childHeightSpace);
            }
        }
        if (currentLineItemCount > 0) {
            if (ignoreBeyondWidth || currentLineMaxWidth <= mContentMaxWidthAccess) {
                contentWidth = Math.max(contentWidth, currentLineMaxWidth);
            }
            contentHeight += currentLineMaxHeight;
            mLineWidth.put(currentLineIndex, currentLineMaxWidth);
            mLineHeight.put(currentLineIndex, currentLineMaxHeight);
            mLineItemCount.put(currentLineIndex, currentLineItemCount);
            mLineEndIndex.put(currentLineIndex, lastMeasureIndex);
        }
        int weightListSize = supportWeight ? mWeightView.size() : 0;
        if (weightListSize > 0) {
            boolean needAdjustMargin = mLineItemCount.size() == 0;
            boolean vertical = mEachLineMaxItemCount == 1;
            int measureSpec, remain, adjustMargin;
            if (vertical) {
                adjustMargin = (needAdjustMargin ? weightListSize - 1 : weightListSize) * middleMarginVertical;
                remain = MeasureSpec.getSize(heightMeasureSpecContent) - contentHeight - adjustMargin;
                measureSpec = widthMeasureSpecContent;
            } else {
                adjustMargin = (needAdjustMargin ? weightListSize - 1 : weightListSize) * middleMarginHorizontal;
                remain = MeasureSpec.getSize(widthMeasureSpecContent) - contentWidth - adjustMargin;
                measureSpec = heightMeasureSpecContent;
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
    protected void dispatchLayout(int contentLeft, int contentTop) {
        final int lineCount = mLineEndIndex.size(), gravity = getGravity();
        final boolean lineVertical = mEachLineCenterVertical || ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.CENTER_VERTICAL && lineCount == 1);
        final boolean lineHorizontal = mEachLineCenterHorizontal || ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL && lineCount == 1);
        final int middleMarginHorizontal = mBorderDivider.getContentMarginHorizontal();
        final int middleMarginVertical = mBorderDivider.getContentMarginVertical();
        final int contentWidthNoMargin = getContentPureWidth();
        int lineEndIndex, lineMaxHeight, childIndex = 0, lineTop = contentTop;
        int childLeft, childTop, childRight, childBottom;
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            lineEndIndex = mLineEndIndex.get(lineIndex);
            lineMaxHeight = mLineHeight.get(lineIndex);
            childLeft = contentLeft;
            if (lineHorizontal) {
                childLeft += (contentWidthNoMargin - mLineWidth.get(lineIndex)) / 2;
            }
            for (; childIndex <= lineEndIndex; childIndex++) {
                final View child = getChildAt(childIndex);
                if (skipChild(child)) continue;
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                childLeft += params.leftMargin();
                childRight = childLeft + childWidth;

                childTop = getContentStartV(lineTop, lineTop + lineMaxHeight, childHeight, params.topMargin(), params.bottomMargin(), lineVertical ? Gravity.CENTER_VERTICAL : params.gravity);
                childBottom = childTop + childHeight;

                child.layout(childLeft, childTop, childRight, childBottom);

                childLeft = childRight + params.rightMargin();
                if (middleMarginHorizontal > 0) {
                    childLeft += middleMarginHorizontal;
                }
            }
            childIndex = lineEndIndex + 1;
            lineTop += lineMaxHeight;
            if (middleMarginVertical > 0) {
                lineTop += middleMarginVertical;
            }
        }
    }

    @Override
    protected void doAfterDraw(Canvas canvas, Rect inset) {
        boolean dividerHorizontal = mBorderDivider.isVisibleDividerHorizontal(true);
        boolean dividerVertical = mBorderDivider.isVisibleDividerVertical(true);
        if (dividerHorizontal || dividerVertical) {
            final int lineCount = mLineEndIndex.size();
            final int middleMarginHorizontal = mBorderDivider.getContentMarginHorizontal();
            final int middleMarginVertical = mBorderDivider.getContentMarginVertical();
            final int contentMarginTop = inset.top;

            int parentLeft = getPaddingLeft();
            int parentRight = getWidth() - getPaddingRight();

            int lineIndex = 0, childIndex = 0;
            int lineTop = getContentTop() + contentMarginTop, lineBottom;
            for (; lineIndex < lineCount; lineIndex++) {
                int lineEndIndex = mLineEndIndex.get(lineIndex);
                lineBottom = lineTop + mLineHeight.get(lineIndex);
                if (dividerHorizontal && lineIndex != lineCount - 1) {
                    mBorderDivider.drawDividerH(canvas, parentLeft, parentRight, lineBottom + (middleMarginVertical > 0 ? middleMarginVertical / 2 : 0));
                }
                if (dividerVertical && mLineItemCount.get(lineIndex) > 1) {
                    for (; childIndex < lineEndIndex; childIndex++) {
                        final View child = getChildAt(childIndex);
                        if (skipChild(child)) continue;
                        LayoutParams params = (LayoutParams) child.getLayoutParams();
                        mBorderDivider.drawDividerV(canvas, lineTop, lineBottom, child.getRight() + params.rightMargin() + (middleMarginHorizontal > 0 ? middleMarginHorizontal / 2 : 0));
                    }
                }
                childIndex = lineEndIndex + 1;
                lineTop = lineBottom;
                if (middleMarginVertical > 0) {
                    lineTop += middleMarginVertical;
                }
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
                requestLayout();
            }
        }
    }

    public void setEachLineMinItemCount(int eachLineMinItemCount) {
        if (mEachLineMinItemCount != eachLineMinItemCount) {
            mEachLineMinItemCount = eachLineMinItemCount;
            requestLayout();
        }
    }

    public void setEachLineMaxItemCount(int eachLineMaxItemCount) {
        if (mEachLineMaxItemCount != eachLineMaxItemCount) {
            mEachLineMaxItemCount = eachLineMaxItemCount;
            requestLayout();
        }
    }

    public void setEachLineCenterHorizontal(boolean eachLineCenterHorizontal) {
        if (mEachLineCenterHorizontal != eachLineCenterHorizontal) {
            mEachLineCenterHorizontal = eachLineCenterHorizontal;
            requestLayout();
        }
    }

    public void setEachLineCenterVertical(boolean eachLineCenterVertical) {
        if (mEachLineCenterVertical != eachLineCenterVertical) {
            mEachLineCenterVertical = eachLineCenterVertical;
            requestLayout();
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static class LayoutParams extends BaseViewGroup.LayoutParams {
        public float weight = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = attrs == null ? null : c.obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_weight});
            if (a != null) {
                weight = a.getFloat(0, weight);
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            if (source instanceof LayoutParams) {
                weight = ((LayoutParams) source).weight;
            }
            if (source instanceof LinearLayout.LayoutParams) {
                weight = ((LinearLayout.LayoutParams) source).weight;
            }
        }
    }

}
