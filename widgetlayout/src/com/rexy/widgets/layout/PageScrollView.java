package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.rexy.widgetlayout.R;
import com.rexy.widgets.divider.BorderDivider;
import com.rexy.widgets.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A customized scroll container support both Horizontal and Vertical layout and gesture.
 * support both scroll style of ScrollView and ViewPager and also their interfaces .
 * support float any view to its start and end position .
 *
 * @author: rexy
 * @date: 2017-04-25 09:32
 */
public class PageScrollView extends ScrollLayout {
    private static final int FLOAT_VIEW_SCROLL = 1;

    protected float mSizeFixedPercent = 0;
    protected boolean isViewPagerStyle = false;

    protected int mFloatViewStart = -1;
    protected int mFloatViewEnd = -1;
    protected boolean isChildCenter = false;
    protected boolean isChildFillParent = false;

    protected int mSwapViewIndex = -1;
    protected int mFloatViewStartIndex = -1;
    protected int mFloatViewEndIndex = -1;

    protected int mFloatViewStartMode = 0;
    protected int mFloatViewEndMode = 0;

    //目前只保证 pageHeader pageFooter 在item View 添加完后再设置。
    protected View mPageHeaderView;
    protected View mPageFooterView;

    int mCurrItem = 0;
    int mPrevItem = -1;
    int mFirstVisiblePosition = -1;
    int mLastVisiblePosition = -1;

    boolean mNeedResolveFloatOffset = false;

    PageTransformer mPageTransformer;
    OnPageChangeListener mPageListener = null;
    OnVisibleRangeChangeListener mOnVisibleRangeChangeListener = null;

    private Comparator<PointF> mComparator;
    private List<PointF> mPairList;
    private static final Pools.SimplePool<PointF> sPairPools = new Pools.SimplePool(8);
    private int[] mMeasureTemp = new int[3], mMeasureSum = new int[5];

    public PageScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public PageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        TypedArray attr = attributeSet == null ? null : context.obtainStyledAttributes(attributeSet, R.styleable.PageScrollView);
        if (attr != null) {
            mSizeFixedPercent = attr.getFloat(R.styleable.PageScrollView_sizeFixedPercent, mSizeFixedPercent);
            isViewPagerStyle = attr.getBoolean(R.styleable.PageScrollView_viewPagerStyle, isViewPagerStyle);
            mFloatViewStart = attr.getInt(R.styleable.PageScrollView_floatViewStartIndex, mFloatViewStart);
            mFloatViewEnd = attr.getInt(R.styleable.PageScrollView_floatViewEndIndex, mFloatViewEnd);
            isChildCenter = attr.getBoolean(R.styleable.PageScrollView_childCenter, isChildCenter);
            isChildFillParent = attr.getBoolean(R.styleable.PageScrollView_childFillParent, isChildFillParent);
        }
    }


    @Override
    protected void onOrientationChanged(int orientation, int oldOrientation) {
        if (!isViewPagerStyle) {
            boolean oldHorizontal = isOrientationHorizontal();
            mCurrItem = mFirstVisiblePosition >= 0 ? mFirstVisiblePosition : 0;
            resetPositionForFloatView(mFloatViewStartIndex, oldHorizontal);
            resetPositionForFloatView(mFloatViewEndIndex, oldHorizontal);
            mFloatViewStartIndex = -1;
            mSwapViewIndex = -1;
            mFloatViewStartMode = 0;
            mFloatViewEndIndex = -1;
            mFloatViewEndMode = 0;
        }
        if (!isAttachLayoutFinished()) {
            scrollToItem(mCurrItem, 0, 0, isViewPagerStyle);
        }
    }

    public int getFloatViewStartIndex() {
        return mFloatViewStart;
    }

    public void setFloatViewStartIndex(int floatStartIndex) {
        if (mFloatViewStart != floatStartIndex) {
            resetPositionForFloatView(mFloatViewStartIndex, isOrientationHorizontal());
            mFloatViewStart = floatStartIndex;
            if (mFloatViewStart >= 0) {
                mNeedResolveFloatOffset = true;
            }
            mSwapViewIndex = -1;
            mFloatViewStartIndex = -1;
            mFloatViewStartMode = 0;
            requestLayoutIfNeed();
        }
    }

    public int getFloatViewEndIndex() {
        return mFloatViewEnd;
    }

    public void setFloatViewEndIndex(int floatEndIndex) {
        if (mFloatViewEnd != floatEndIndex) {
            resetPositionForFloatView(mFloatViewEndIndex, isOrientationHorizontal());
            mFloatViewEnd = floatEndIndex;
            if (mFloatViewEnd >= 0) {
                mNeedResolveFloatOffset = true;
            }
            mFloatViewEndIndex = -1;
            mFloatViewEndMode = 0;
            requestLayoutIfNeed();
        }
    }

    public float getSizeFixedPercent() {
        return mSizeFixedPercent;
    }

    public void setSizeFixedPercent(float percent) {
        if (mSizeFixedPercent != percent && percent >= 0 && percent <= 0) {
            mSizeFixedPercent = percent;
            requestLayoutIfNeed();
        }
    }

    public boolean isViewPagerStyle() {
        return isViewPagerStyle;
    }

    public void setViewPagerStyle(boolean viewPagerStyle) {
        if (isViewPagerStyle != viewPagerStyle) {
            isViewPagerStyle = viewPagerStyle;
        }
    }

    public boolean isChildCenter() {
        return isChildCenter;
    }

    public void setChildCenter(boolean centerChild) {
        if (this.isChildCenter != centerChild) {
            this.isChildCenter = centerChild;
            if (isAttachLayoutFinished()) {
                requestLayoutIfNeed();
            }
        }
    }

    public boolean isChildFillParent() {
        return isChildFillParent;
    }

    public void setChildFillParent(boolean childFillParent) {
        if (isChildFillParent != childFillParent) {
            this.isChildFillParent = childFillParent;
            if (isAttachLayoutFinished()) {
                requestLayoutIfNeed();
            }
        }
    }

    public View getPageHeaderView() {
        return mPageHeaderView;
    }

    public void setPageHeaderView(View headView) {
        if (mPageHeaderView != headView) {
            if (mPageHeaderView != null) {
                removeViewInLayout(mPageHeaderView);
            }
            mPageHeaderView = headView;
            if (mPageHeaderView != null) {
                addView(mPageHeaderView);
                mNeedResolveFloatOffset = true;
            }
            requestLayoutIfNeed();
        }
    }

    public View getPageFooterView() {
        return mPageFooterView;
    }

    public void setPageFooterView(View pageFooterView) {
        if (mPageFooterView != pageFooterView) {
            if (mPageFooterView != null) {
                removeViewInLayout(mPageFooterView);
            }
            mPageFooterView = pageFooterView;
            if (mPageFooterView != null) {
                addView(mPageFooterView);
                mNeedResolveFloatOffset = true;
            }
            requestLayoutIfNeed();

        }
    }

    public PageTransformer getPageTransformer() {
        return mPageTransformer;
    }

    public void setPageTransformer(PageTransformer transformer) {
        if (mPageTransformer != transformer) {
            PageTransformer oldTransformer = mPageTransformer;
            mPageTransformer = transformer;
            if (isAttachLayoutFinished()) {
                boolean horizontal = isOrientationHorizontal();
                if (oldTransformer != null && mPageTransformer == null) {
                    int childCount = getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = getChildAt(i);
                        if (!skipVirtualChild(child, true)) {
                            oldTransformer.recoverTransformPage(child, horizontal);
                        }
                    }
                }
                if (mPageTransformer != null) {
                    resolvePageOffset(horizontal ? getScrollX() : getScrollY(), horizontal);
                }
            }
        }
    }

    public OnPageChangeListener getPageChangeListener() {
        return mPageListener;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageListener = listener;
    }

    public OnVisibleRangeChangeListener getVisibleRangeChangeListener() {
        return mOnVisibleRangeChangeListener;
    }

    public void setOnVisibleRangeChangeListener(OnVisibleRangeChangeListener l) {
        mOnVisibleRangeChangeListener = l;
    }

    public boolean hasPageHeaderView() {
        return mPageHeaderView != null && mPageHeaderView.getVisibility() != View.GONE && mPageHeaderView.getParent() == this;
    }

    public boolean hasPageFooterView() {
        return mPageFooterView != null && mPageFooterView.getVisibility() != View.GONE && mPageFooterView.getParent() == this;
    }

    public int getCurrentItem() {
        return mCurrItem;
    }

    public int getPrevItem() {
        return mPrevItem;
    }

    //TODO 判断是否可悬停，可能需要在onLayout 中进行。
    protected boolean floatViewScrollNeeded(View view, boolean horizontal, boolean floatStartPosition) {
        int scrollRange = horizontal ? getHorizontalScrollRange() : getVerticalScrollRange();
        return view != null && scrollRange > 0;
    }

    protected void computeFloatViewIndexIfNeed(int virtualCount, boolean horizontal) {
        mFloatViewStartIndex = -1;
        mFloatViewEndIndex = -1;
        mFloatViewStartMode=0;
        mFloatViewEndMode=0;
        if (virtualCount >= 2) {
            if (mFloatViewStart >= 0 && mFloatViewStart < virtualCount) {
                computeFloatViewIndex(mFloatViewStart, horizontal, true);
            }
            if (mFloatViewEnd >= 0 && mFloatViewEnd < virtualCount) {
                computeFloatViewIndex(mFloatViewEnd, horizontal, false);
            }
        }
    }

    protected void computeFloatViewIndex(int itemIndex, boolean horizontal, boolean floatStart) {
        View view = getVirtualChildAt(itemIndex, true);
        if (getVirtualChildAt(itemIndex, false) == view && floatViewScrollNeeded(view, horizontal, floatStart)) {
            if (floatStart) {
                mFloatViewStartIndex = indexOfChild(view);
                mFloatViewStartMode = FLOAT_VIEW_SCROLL;
            } else {
                mFloatViewEndIndex = indexOfChild(view);
                mFloatViewEndMode = FLOAT_VIEW_SCROLL;
            }
        }
    }

    private void mergeMeasureResult(boolean horizontal) {
        if (horizontal) {
            mMeasureSum[0] = Math.max(mMeasureSum[0], mMeasureTemp[0]);
            mMeasureSum[1] += mMeasureTemp[1];
            mMeasureSum[3] += mMeasureTemp[1];
        } else {
            mMeasureSum[0] += mMeasureTemp[0];
            mMeasureSum[1] = Math.max(mMeasureSum[1], mMeasureTemp[1]);
            mMeasureSum[2] += mMeasureTemp[0];
        }
        mMeasureSum[4] |= mMeasureTemp[2];
    }

    @Override
    protected void dispatchMeasure(int widthMeasureSpecContent, int heightMeasureSpecContent) {
        boolean horizontal = isOrientationHorizontal();
        int itemCount = getItemViewCount();
        Arrays.fill(mMeasureSum, 0);
        if (hasPageHeaderView()) {
            Arrays.fill(mMeasureTemp, 0);
            measureHeaderFooter(mPageHeaderView, widthMeasureSpecContent, heightMeasureSpecContent, mMeasureSum[2], mMeasureSum[3], horizontal);
            mergeMeasureResult(horizontal);
        }
        if (itemCount > 0) {
            int middleWidthSpec = widthMeasureSpecContent, middleHeightSpec = heightMeasureSpecContent;
            if (horizontal) {
                middleWidthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(middleWidthSpec), MeasureSpec.UNSPECIFIED);
            } else {
                middleHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(middleHeightSpec), MeasureSpec.UNSPECIFIED);
            }
            Arrays.fill(mMeasureTemp, 0);
            measureItems(middleWidthSpec, middleHeightSpec, mMeasureSum[2], mMeasureSum[3], horizontal);
            mergeMeasureResult(horizontal);
        }
        if (hasPageFooterView()) {
            Arrays.fill(mMeasureTemp, 0);
            measureHeaderFooter(mPageFooterView, widthMeasureSpecContent, heightMeasureSpecContent, mMeasureSum[2], mMeasureSum[3], horizontal);
            mergeMeasureResult(horizontal);
        }
        setContentSize(mMeasureSum[0], mMeasureSum[1], mMeasureSum[4]);
    }

    protected void measureHeaderFooter(View view, int widthMeasureSpec, int heightMeasureSpec, int widthUsed, int heightUsed, boolean horizontal) {
        LayoutParams params = measure(view, -1, widthMeasureSpec, heightMeasureSpec, widthUsed, heightUsed);
        mMeasureTemp[0] = params.width(view);
        mMeasureTemp[1] = params.height(view);
        mMeasureTemp[2] = view.getMeasuredState();
    }

    protected void measureItems(int widthMeasureSpec, int heightMeasureSpec, int widthUsed, int heightUsed, boolean horizontal) {
        final BorderDivider borderDivider=getBorderDivider();
        int itemWidthMeasureSpec, itemHeightMeasureSpec;
        int accessWidth = MeasureSpec.getSize(widthMeasureSpec) - widthUsed;
        int accessHeight = MeasureSpec.getSize(heightMeasureSpec) - heightUsed;
        int itemMargin = horizontal ?borderDivider.getItemMarginHorizontal() : borderDivider.getItemMarginVertical();
        boolean fixedOrientationSize = mSizeFixedPercent > 0 && mSizeFixedPercent <= 1;
        if (fixedOrientationSize) {
            if (horizontal) {
                accessWidth *= mSizeFixedPercent;
            } else {
                accessHeight *= mSizeFixedPercent;
            }
        }
        itemWidthMeasureSpec = MeasureSpec.makeMeasureSpec(accessWidth, MeasureSpec.getMode(widthMeasureSpec));
        itemHeightMeasureSpec = MeasureSpec.makeMeasureSpec(accessHeight, MeasureSpec.getMode(heightMeasureSpec));
        final int childCount = getChildCount();
        int contentWidth = 0, contentHeight = 0, childState = 0, itemPosition = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, true)) continue;
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int oldParamsWidth = params.width, oldParamsHeight = params.height;
            if (fixedOrientationSize) {
                if (horizontal) {
                    params.width = -1;
                } else {
                    params.height = -1;
                }
            }
            measure(child, itemPosition++, itemWidthMeasureSpec, itemHeightMeasureSpec, 0, 0);
            params.width = oldParamsWidth;
            params.height = oldParamsHeight;
            int itemWidth = params.width(child), itemHeight = params.height(child);
            if (horizontal) {
                contentWidth += (itemWidth + itemMargin);
                contentHeight = Math.max(contentHeight, itemHeight);
            } else {
                contentWidth = Math.max(contentWidth, itemWidth);
                contentHeight += (itemHeight + itemMargin);
            }
            childState |= child.getMeasuredState();
        }
        if (horizontal) {
            mMeasureTemp[0] = contentWidth - itemMargin;
            mMeasureTemp[1] = contentHeight;
        } else {
            mMeasureTemp[0] = contentWidth;
            mMeasureTemp[1] = contentHeight - itemMargin;
        }
        mMeasureTemp[2] = childState;
    }

    @Override
    protected void doAfterMeasure(int measuredWidth, int measuredHeight, int contentWidth, int contentHeight) {
        int itemCount = getItemViewCount();
        if (itemCount > 0) {
            boolean horizontal = isOrientationHorizontal();
            if (isChildFillParent && itemCount > 0) {
                int adjustTotal;
                if (horizontal) {
                    adjustTotal = measuredWidth - (contentWidth + getPaddingLeft() + getPaddingRight());
                } else {
                    adjustTotal = measuredHeight - (contentHeight + getPaddingTop() + getPaddingBottom());
                }
                if (adjustTotal > itemCount && adjustMatchParentMeasure(adjustTotal, horizontal)) {
                    if (horizontal) {
                        setContentSize(getContentWidth() + adjustTotal, getContentHeight(), getMeasureState());
                    } else {
                        setContentSize(getContentWidth(), getContentHeight() + adjustTotal, getMeasureState());
                    }
                }
            }
            computeFloatViewIndexIfNeed(itemCount, horizontal);
        }
    }

    @Override
    protected void doAfterLayout(int baseLeft, int baseTop, int contentWidth, int contentHeight, boolean firstAttachLayout) {
        super.doAfterLayout(baseLeft, baseTop, contentWidth, contentHeight, firstAttachLayout);
        if (mNeedResolveFloatOffset && !firstAttachLayout) {
            mNeedResolveFloatOffset = false;
            boolean horizontal = isOrientationHorizontal();
            int scrolled = horizontal ? getScrollX() : getScrollY();
            if (mPageHeaderView != null || mPageFooterView != null) {
                updatePositionForHeaderAndFooter(scrolled, horizontal);
            }
            if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
                updatePositionForFloatView(scrolled, horizontal);
            }
        }
    }

    private void destroyCacheMeasureSize() {
        Iterator<PointF> its = mPairList.iterator();
        while (its.hasNext()) {
            PointF it = its.next();
            sPairPools.release(it);
            its.remove();
        }
    }

    private List<PointF> buildCacheMeasureSize(int childCount, boolean horizontal) {
        if (mPairList == null) {
            mPairList = new ArrayList(8);
            mComparator = new Comparator<PointF>() {
                @Override
                public int compare(PointF l, PointF r) {
                    return Float.compare(l.y, r.y);
                }
            };
        } else {
            destroyCacheMeasureSize();
        }
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, true)) continue;
            PointF pair = sPairPools.acquire();
            if (pair == null) {
                pair = new PointF();
            }
            if (horizontal) {
                pair.set(i, ((LayoutParams) child.getLayoutParams()).width(child));
            } else {
                pair.set(i, ((LayoutParams) child.getLayoutParams()).height(child));
            }
            mPairList.add(pair);
        }
        return mPairList;
    }

    private void adjustMatchMeasureSize(int matchSize, float space) {
        Collections.sort(mPairList, mComparator);
        int startIndex = 0;
        while (space > 1) {
            int diffIndex = -1;
            PointF start = mPairList.get(startIndex), current = null;
            for (int i = startIndex + 1; i < matchSize; i++) {
                current = mPairList.get(i);
                if (current.y > start.y) {
                    diffIndex = i;
                    break;
                }
            }
            if (diffIndex == -1) {
                float addedSize = space / matchSize;
                for (PointF point : mPairList) {
                    point.y = point.y + addedSize;
                }
                space = 0;
            } else {
                float addedSize = Math.min(current.y - start.y, space / diffIndex);
                for (int i = 0; i < diffIndex; i++) {
                    start = mPairList.get(i);
                    start.y = start.y + addedSize;
                }
                space = space - (addedSize * diffIndex);
                startIndex = diffIndex;
            }
        }
    }

    private boolean adjustMatchParentMeasure(float space, boolean horizontal) {
        List<PointF> list = buildCacheMeasureSize(getChildCount(), horizontal);
        int matchSize = list.size();
        if (matchSize > 0) {
            adjustMatchMeasureSize(matchSize, space);
            for (PointF point : mPairList) {
                View child = getChildAt((int) point.x);
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int goodSize = (int) point.y;
                if (horizontal) {
                    if (goodSize != params.width(child)) {
                        child.measure(MeasureSpec.makeMeasureSpec(goodSize - params.leftMargin() - params.rightMargin(), MeasureSpec.EXACTLY)
                                , MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
                    }
                } else {
                    if (goodSize != params.height(child)) {
                        child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY)
                                , MeasureSpec.makeMeasureSpec(goodSize - params.topMargin() - params.bottomMargin(), MeasureSpec.EXACTLY));
                    }
                }
            }
            destroyCacheMeasureSize();
            return true;
        }
        return false;
    }

    @Override
    protected void dispatchLayout(int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        int contentRight = contentLeft + contentWidth;
        int contentBottom = contentTop + contentHeight;
        if (isOrientationHorizontal()) {
            onLayoutHorizontal(contentLeft, contentTop, contentRight, contentBottom);
        } else {
            onLayoutVertical(contentLeft, contentTop, contentRight, contentBottom);
        }
    }

    protected void onLayoutVertical(int baseLeft, int baseTop, int baseRight, int baseBottom) {
        int childLeft, childTop, childRight, childBottom;
        Rect inset = mContentInset;
        if (hasPageHeaderView()) {
            LayoutParams params = (LayoutParams) mPageHeaderView.getLayoutParams();
            childTop = ViewUtils.getContentStartV(Math.max(baseTop, getPaddingTop() + inset.top), Math.min(baseBottom, getHeight() - (getPaddingBottom() + inset.bottom))
                    , mPageHeaderView.getMeasuredHeight(), params.topMargin(), params.bottomMargin(), params.gravity);
            childBottom = childTop + mPageHeaderView.getMeasuredHeight();
            childLeft = baseLeft + params.leftMargin();
            childRight = childLeft + mPageHeaderView.getMeasuredWidth();
            baseLeft = childRight + params.rightMargin();
            mPageHeaderView.layout(childLeft, childTop, childRight, childBottom);
        }

        if (hasPageFooterView()) {
            LayoutParams params = (LayoutParams) mPageFooterView.getLayoutParams();
            childTop = ViewUtils.getContentStartV(Math.max(baseTop, getPaddingTop() + inset.top), Math.min(baseBottom, getHeight() - (getPaddingBottom() + inset.bottom))
                    , mPageFooterView.getMeasuredHeight(), params.topMargin(), params.bottomMargin(), params.gravity);
            childBottom = childTop + mPageFooterView.getMeasuredHeight();
            childRight = baseRight - params.rightMargin();
            childLeft = childRight - mPageFooterView.getMeasuredWidth();
            baseRight = childLeft - params.leftMargin();
            mPageFooterView.layout(childLeft, childTop, childRight, childBottom);
        }

        final int count = getChildCount(), mMiddleMargin = getBorderDivider().getItemMarginVertical();
        childTop = baseTop;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, true)) continue;
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            childTop += params.topMargin();
            childBottom = childTop + child.getMeasuredHeight();
            childLeft = ViewUtils.getContentStartH(baseLeft, baseRight, child.getMeasuredWidth(), params.leftMargin(), params.rightMargin(), isChildCenter ? Gravity.CENTER : params.gravity);
            childRight = childLeft + child.getMeasuredWidth();
            child.layout(childLeft, childTop, childRight, childBottom);
            childTop = childBottom + params.bottomMargin() + mMiddleMargin;
        }
    }

    protected void onLayoutHorizontal(int baseLeft, int baseTop, int baseRight, int baseBottom) {
        int childLeft, childTop, childRight, childBottom;
        Rect inset = mContentInset;
        if (hasPageHeaderView()) {
            LayoutParams params = (LayoutParams) mPageHeaderView.getLayoutParams();
            childLeft = ViewUtils.getContentStartH(Math.max(baseLeft, getPaddingLeft() + inset.left), Math.min(baseRight, getWidth() - (getPaddingRight() + inset.right))
                    , mPageHeaderView.getMeasuredWidth(), params.leftMargin(), params.rightMargin(), params.gravity);
            childRight = childLeft + mPageHeaderView.getMeasuredWidth();
            childTop = baseTop + params.topMargin();
            childBottom = childTop + mPageHeaderView.getMeasuredHeight();
            baseTop = childBottom + params.bottomMargin();
            mPageHeaderView.layout(childLeft, childTop, childRight, childBottom);
        }

        if (hasPageFooterView()) {
            LayoutParams params = (LayoutParams) mPageFooterView.getLayoutParams();
            childLeft = ViewUtils.getContentStartH(Math.max(baseLeft, getPaddingLeft() + inset.left), Math.min(baseRight, getWidth() - (getPaddingRight() + inset.right))
                    , mPageFooterView.getMeasuredWidth(), params.leftMargin(), params.rightMargin(), params.gravity);
            childRight = childLeft + mPageFooterView.getMeasuredWidth();
            childBottom = baseBottom - params.bottomMargin();
            childTop = childBottom - mPageFooterView.getMeasuredHeight();
            baseBottom = childTop - params.topMargin();
            mPageFooterView.layout(childLeft, childTop, childRight, childBottom);
        }

        final int count = getChildCount(), mMiddleMargin = getBorderDivider().getItemMarginHorizontal();
        childLeft = baseLeft;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, true)) continue;
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            childLeft += params.leftMargin();
            childRight = childLeft + child.getMeasuredWidth();
            childTop = ViewUtils.getContentStartV(baseTop, baseBottom, child.getMeasuredHeight(), params.topMargin(), params.bottomMargin(), isChildCenter ? Gravity.CENTER : params.gravity);
            childBottom = childTop + child.getMeasuredHeight();
            child.layout(childLeft, childTop, childRight, childBottom);
            childLeft = childRight + params.bottomMargin() + mMiddleMargin;
        }
    }

    @Override
    protected void doBeforeDraw(Canvas canvas, int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        boolean swapIndexEnable = mFloatViewStartIndex >= 0 && mSwapViewIndex >= 0;
        if (swapIndexEnable && isChildrenDrawingOrderEnabled() == false) {
            setChildrenDrawingOrderEnabled(true);
        } else {
            if (swapIndexEnable == false) {
                setChildrenDrawingOrderEnabled(false);
            }
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int order = i;
        if (mFloatViewStartIndex >= 0 && mSwapViewIndex >= 0) {
            if (mFloatViewStartIndex == i) {
                return mSwapViewIndex;
            }
            if (i == mSwapViewIndex) {
                return mFloatViewStartIndex;
            }
        }
        return order;
    }

    @Override
    protected void cancelTouch(boolean resetToIdle) {
        int fling = 0;
        if (isViewPagerStyle && OnScrollChangeListener.SCROLL_STATE_IDLE != getScrollState()) {
            fling = flingToWhere(0, 0, isOrientationHorizontal());
        }
        super.cancelTouch(resetToIdle && fling == 0);
    }

    @Override
    public boolean fling(int movedX, int movedY, int velocityX, int velocityY) {
        if (isViewPagerStyle && OnScrollChangeListener.SCROLL_STATE_IDLE != getScrollState()) {
            boolean horizontal = isOrientationHorizontal();
            int moved = horizontal ? movedX : movedY;
            int velocity = horizontal ? velocityX : velocityY;
            return flingToWhere(moved, velocity, horizontal) > 0;
        }
        return super.fling(movedX, movedY, velocityX, velocityY);
    }

    private int flingToWhere(int moved, int velocity, boolean horizontal) {
        int scrolled = horizontal ? getScrollX() : getScrollY(), willScroll;
        if (velocity == 0) {
            velocity = -(int) Math.signum(moved);
        }
        int targetIndex = mCurrItem;
        int itemSize = horizontal ? getChildAt(mCurrItem).getWidth() : getChildAt(mCurrItem).getHeight();
        int absVelocity = velocity > 0 ? velocity : -velocity;
        int pageItemCount = getItemViewCount();
        if (Math.abs(moved) > mTouchSlop) {
            int halfItemSize = itemSize / 2;
            if (absVelocity > mMinFlingVelocity) {
                if (velocity > 0 && mCurrItem < pageItemCount - 1 && (velocity / 10 - moved) > halfItemSize) {
                    targetIndex++;
                }
                if (velocity < 0 && mCurrItem > 0 && (moved - velocity / 10) > halfItemSize) {
                    targetIndex--;
                }
            } else {
                if (moved > halfItemSize && mCurrItem > 0) {
                    targetIndex--;
                }
                if (moved < -halfItemSize && mCurrItem < pageItemCount - 1) {
                    targetIndex++;
                }
            }
        }
        int targetScroll = computeScrollOffset(targetIndex, 0, true, horizontal);
        if ((willScroll = targetScroll - scrolled) != 0) {
            scrollToCentre(targetIndex, 0, -1);
        }
        return willScroll;
    }


    public void scrollTo(int index, int offset, int duration) {
        scrollToItem(index, offset, duration, false);
    }

    public void scrollToCentre(int index, int offset, int duration) {
        scrollToItem(index, offset, duration, true);
    }

    public void scrollTo(View child, int offset, int duration, boolean centerInParent) {
        int pageIndex = indexOfItemView(child);
        if (pageIndex != -1) {
            scrollToItem(pageIndex, offset, duration, centerInParent);
        }
    }

    private void scrollToItem(int index, int offset, int duration, boolean centerInParent) {
        boolean okX = isOrientationHorizontal();
        boolean okY = isOrientationVertical();
        int x = okX ? offset : getScrollX(), y = okY ? offset : getScrollY();
        setCurrentItem(index);
        scrollToItem(index, duration, x, y, okX, okY, centerInParent);
    }

    protected int computeScrollOffset(View child, int offset, boolean centreWithParent, boolean horizontal) {
        int scrollRange, targetScroll;
        if (horizontal) {
            targetScroll = offsetX(child, centreWithParent, true) + offset;
            scrollRange = getHorizontalScrollRange();
        } else {
            targetScroll = offsetY(child, centreWithParent, true) + offset;
            scrollRange = getVerticalScrollRange();
        }
        return Math.max(0, Math.min(scrollRange, targetScroll));
    }

    protected int computeScrollOffset(int childPosition, int offset, boolean centreWithParent, boolean horizontal) {
        View child = getVirtualChildAt(childPosition, true);
        return child == null ? 0 : computeScrollOffset(child, offset, centreWithParent, horizontal);
    }

    @Override
    protected int formatDuration(int duration) {
        return isViewPagerStyle ? Math.max(0, Math.min(duration, 800)) : super.formatDuration(duration);
    }

    private void enableLayers(boolean enable) {
        final int childCount = getChildCount();
        final int layerType = enable ? ViewCompat.LAYER_TYPE_HARDWARE : ViewCompat.LAYER_TYPE_NONE;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != mPageHeaderView && child != mPageFooterView) {
                ViewCompat.setLayerType(child, layerType, null);
            }
        }
    }

    private boolean setCurrentItem(int willItem) {
        if (mCurrItem != willItem || mPrevItem == -1) {
            int preItem = mCurrItem == willItem ? mPrevItem : mCurrItem;
            mPrevItem = mCurrItem;
            mCurrItem = willItem;
            if (isLogAccess()) {
                print("select", String.format("selectChange  $$$$:%d >>>>>>>>> %d", preItem, mCurrItem));
            }
            if (mPageListener != null) {
                mPageListener.onPageSelected(willItem, preItem);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onScrollStateChanged(int newState, int prevState) {
        if (mPageListener != null) {
            mPageListener.onScrollStateChanged(newState, prevState);
        }
        if (mPageTransformer != null) {
            // PageTransformers can do complex things that benefit from hardware layers.
            enableLayers(newState != OnScrollChangeListener.SCROLL_STATE_IDLE);
        }
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, Rect visibleBounds, boolean fromScrollChanged) {
        mNeedResolveFloatOffset = false;
        boolean horizontal = isOrientationHorizontal();
        int scrolled = horizontal ? scrollX : scrollY;
        resolveVisiblePosition(scrolled, horizontal);
        if (mFloatViewStartIndex >= 0) {
            mSwapViewIndex = computeSwapViewIndex(scrolled, horizontal);
        }
        if (mPageHeaderView != null || mPageFooterView != null) {
            updatePositionForHeaderAndFooter(scrolled, horizontal);
        }
        if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
            updatePositionForFloatView(scrolled, horizontal);
        }
        if (mPageListener != null || mPageTransformer != null) {
            resolvePageOffset(scrolled, horizontal);
        }
        if (isLogAccess()) {
            StringBuilder sb = new StringBuilder(32);
            sb.append("scrollX=").append(scrollX);
            sb.append(",scrollY=").append(scrollY).append(",visibleBounds=").append(visibleBounds);
            sb.append(",scrollChanged=").append(fromScrollChanged);
            print("scroll", sb);
        }
    }

    protected int computeSwapViewIndex(int scrolled, boolean horizontal) {
        if (mFloatViewStartIndex >= 0) {
            int count = getChildCount(), baseLine;
            View view = getChildAt(mFloatViewStartIndex);
            baseLine = (horizontal ? view.getRight() : view.getBottom()) + scrolled;
            for (int i = mFloatViewStartIndex + 1; i < count; i++) {
                final View child = getChildAt(i);
                if (skipVirtualChild(child, true))
                    continue;
                if (horizontal) {
                    if (child.getRight() >= baseLine) {
                        return i;
                    }
                } else {
                    if (child.getBottom() >= baseLine) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    protected void resetPositionForFloatView(int realChildIndex, boolean horizontal) {
        View child = realChildIndex >= 0 ? getChildAt(realChildIndex) : null;
        if (child != null) {
            child.setTranslationX(0);
            child.setTranslationY(0);
        }
    }

    private void updatePositionForHeaderAndFooter(int scrolled, boolean horizontal) {
        if (mPageHeaderView != null && mPageHeaderView.getParent() == this) {
            if (horizontal) {
                mPageHeaderView.setTranslationX(scrolled);
            } else {
                mPageHeaderView.setTranslationY(scrolled);
            }
        }
        if (mPageFooterView != null && mPageFooterView.getParent() == this) {
            if (horizontal) {
                mPageFooterView.setTranslationX(scrolled);
            } else {
                mPageFooterView.setTranslationY(scrolled);
            }
        }
    }

    private void updatePositionForFloatView(int scrolled, boolean horizontal) {
        float viewTranslated;
        int wantTranslated;
        //TODO FLOAT MARGIN SHOULD MAKE AS A ATTRIBUTION.
        if (mFloatViewStartMode == FLOAT_VIEW_SCROLL) {
            View view = getItemView(mFloatViewStartIndex);
            if (horizontal) {
                wantTranslated = scrolled - view.getLeft();
                viewTranslated = view.getTranslationX();
            } else {
                wantTranslated = scrolled - view.getTop();
                viewTranslated = view.getTranslationY();
            }
            wantTranslated = Math.max(0, wantTranslated);
            if (wantTranslated != viewTranslated) {
                if (horizontal) {
                    view.setTranslationX(wantTranslated);
                } else {
                    view.setTranslationY(wantTranslated);
                }
            }
        }
        if (mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
            View view = getItemView(mFloatViewEndIndex);
            int scrollRange;
            if (horizontal) {
                scrollRange = getHorizontalScrollRange();
                wantTranslated = scrolled - scrollRange + (getContentWidth() - view.getRight());
                viewTranslated = view.getTranslationX();
            } else {
                scrollRange = getVerticalScrollRange();
                wantTranslated = scrolled - scrollRange + (getContentHeight() - view.getBottom());
                viewTranslated = view.getTranslationY();
            }
            wantTranslated = Math.min(0, wantTranslated);
            if (wantTranslated != viewTranslated) {
                if (horizontal) {
                    view.setTranslationX(wantTranslated);
                } else {
                    view.setTranslationY(wantTranslated);
                }
            }
        }
    }

    private void resolveVisiblePosition(int scrolled, boolean horizontal) {
        int visibleStart, visibleEnd;
        Rect visibleBounds = getVisibleContentBounds();
        if (horizontal) {
            visibleStart = visibleBounds.left;
            visibleEnd = visibleBounds.right;
        } else {
            visibleStart = visibleBounds.top;
            visibleEnd = visibleBounds.bottom;
        }
        int childCount = getChildCount(), counted = 0;
        int firstVisible = -1, lastVisible = -1;
        boolean visible;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, true)) continue;
            if (horizontal) {
                visible = !((child.getRight() <= visibleStart) || child.getLeft() >= visibleEnd);
            } else {
                visible = !((child.getBottom() <= visibleStart) || child.getTop() >= visibleEnd);
            }
            if (visible) {
                if (firstVisible == -1) {
                    firstVisible = counted;
                }
                lastVisible = counted;
            } else {
                if (firstVisible >= 0) {
                    break;
                }
            }
            counted++;
        }
        if (firstVisible != -1) {
            if (firstVisible != mFirstVisiblePosition || lastVisible != mLastVisiblePosition) {
                int oldFirstVisible = mFirstVisiblePosition;
                int oldLastVisible = mLastVisiblePosition;
                mFirstVisiblePosition = firstVisible;
                mLastVisiblePosition = lastVisible;
                if (isLogAccess()) {
                    print("range", String.format("visibleRangeChanged  ****:[%d , %d]", firstVisible, lastVisible));
                }
                if (mOnVisibleRangeChangeListener != null) {
                    mOnVisibleRangeChangeListener.onVisibleRangeChanged(firstVisible, lastVisible, oldFirstVisible, oldLastVisible);
                }
            }
        }
    }

    private void resolvePageOffset(int scrolled, boolean horizontal) {
        int targetOffset = computeScrollOffset(mCurrItem, 0, true, horizontal);
        int prevIndex = mCurrItem, itemCount = getItemViewCount();
        if (scrolled > targetOffset && prevIndex < itemCount - 1) {
            prevIndex++;
        }
        if (scrolled < targetOffset && prevIndex > 0) {
            prevIndex--;
        }
        int minIndex, maxIndex, minOffset, maxOffset;
        if (prevIndex > mCurrItem) {
            minIndex = mCurrItem;
            minOffset = targetOffset;
            maxIndex = prevIndex;
            maxOffset = maxIndex == minIndex ? minOffset : computeScrollOffset(maxIndex, 0, true, horizontal);
        } else {
            maxIndex = mCurrItem;
            maxOffset = targetOffset;
            minIndex = prevIndex;
            minOffset = minIndex == maxIndex ? maxOffset : computeScrollOffset(minIndex, 0, true, horizontal);
        }
        int distance = maxOffset - minOffset;
        int positionOffsetPixels = 0;
        float positionOffset = 0;
        if (distance > 0) {
            positionOffsetPixels = scrolled - minOffset;
            positionOffset = positionOffsetPixels / (float) distance;
        }
        if (mPageListener != null) {
            mPageListener.onPageScrolled(minIndex, positionOffset, positionOffsetPixels);
        }
        if (mPageTransformer != null) {
            dispatchTransformPosition(scrolled, itemCount, horizontal);
        }
    }

    private void dispatchTransformPosition(int scrolled, int itemCount, boolean horizontal) {
        final BorderDivider borderDivider=getBorderDivider();
        int childCount = getChildCount(), pageItemIndex = 0;
        int mMiddleMargin = horizontal ? borderDivider.getItemMarginHorizontal() : borderDivider.getItemMarginVertical();
        int pageItemStart = Math.max(0, mFirstVisiblePosition - 1);
        int pageItemEnd = Math.min(itemCount - 1, mLastVisiblePosition + 1);
        for (int i = 0; i < childCount && pageItemIndex <= pageItemEnd; i++) {
            View child = getChildAt(i);
            if (skipVirtualChild(child, true))
                continue;
            if (pageItemIndex >= pageItemStart) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int contentLength = horizontal ? params.width(child) : params.height(child);
                if (mMiddleMargin > 0) {
                    if (pageItemIndex == 0 || pageItemIndex == itemCount - 1) {
                        contentLength += (mMiddleMargin / 2);
                    } else {
                        contentLength += mMiddleMargin;
                    }
                }
                float transformerPosition = (scrolled - computeScrollOffset(child, 0, true, horizontal)) / (float) contentLength;
                mPageTransformer.transformPage(child, transformerPosition, horizontal);
            }
            pageItemIndex++;
        }
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
        mFirstVisiblePosition = -1;
        mLastVisiblePosition = -1;
        mCurrItem = 0;
        mPrevItem = -1;
    }

    @Override
    protected boolean skipVirtualChild(View child, boolean withoutGone) {
        return super.skipVirtualChild(child, withoutGone) || (child == mPageHeaderView || child == mPageFooterView);
    }

    public interface PageTransformer {
        void transformPage(View view, float position, boolean horizontal);

        void recoverTransformPage(View view, boolean horizontal);
    }

    public interface OnPageChangeListener extends OnScrollChangeListener {

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position, int oldPosition);

    }

    public interface OnVisibleRangeChangeListener {
        void onVisibleRangeChanged(int firstVisible, int lastVisible, int oldFirstVisible, int oldLastVisible);
    }
}