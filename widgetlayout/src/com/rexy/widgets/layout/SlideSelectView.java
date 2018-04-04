package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.rexy.widgetlayout.R;
import com.rexy.widgets.adpter.ItemProvider;

import java.lang.ref.WeakReference;

/**
 * Created by rexy on 2018/3/28.
 */

public class SlideSelectView extends ScrollLayout {
    private static int TAB_INDEX = com.rexy.widgetlayout.R.id.widgetLayoutViewIndexType;
    private static int mDefaultTextColor = 0xFF666666;
    private static int mDefaultSelectedTextColor = 0xFFFF8010;
    /**
     * 以下是设置tab item 的最小padding 值。
     */
    private int mItemMinPaddingHorizontal = 0;
    private int mItemMinPaddingTop = 0;
    private int mItemMinPaddingBottom = 0;

    private int mTextSize = 16;

    private int mIndicatorColor = 0xFFFF8010;
    private int mIndicatorWidth = 100;
    private int mIndicatorHeight = 2;
    private int mIndicatorTriangleHeight = 4;
    private int mIndicatorTriangleWidth = 6;

    private float mSelectScale = 1.625f;

    private ColorStateList mColorStateList;
    private ColorStateList mSelectedColorStateList;

    private int mLeftMakeUp;
    private int mRightMakeUp;
    private int mCurrentPosition = 0; //当前选中的tab 索引。
    protected ItemProvider mItemProvider = null;
    private Path mPath = new Path();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LayoutParams mItemLayoutParams;
    WeakReference<TextView> mPreviousView;

    private OnClickListener mItemClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Object tag = view.getTag(TAB_INDEX);
            int cur;
            if (tag instanceof Integer) {
                cur = (Integer) tag;
            } else {
                cur = indexOfItemView(view);
            }
            int pre = mCurrentPosition;
            handItemClick(cur, pre);
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener mTreeObserver = null;

    private void removeTreeObserver() {
        if (mTreeObserver != null && getViewTreeObserver() != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                getViewTreeObserver().removeGlobalOnLayoutListener(mTreeObserver);
            } else {
                getViewTreeObserver().removeOnGlobalLayoutListener(mTreeObserver);
            }
            mTreeObserver = null;
        }
    }

    private void listenTreeObserver() {
        removeTreeObserver();
        if (getViewTreeObserver() != null) {
            mTreeObserver = new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    removeTreeObserver();
                    int itemCount = getItemViewCount();
                    int selectedIndex = Math.min(Math.max(0, mCurrentPosition), itemCount - 1);
                    setSelectedItem(selectedIndex, false);
                }
            };
            getViewTreeObserver().addOnGlobalLayoutListener(mTreeObserver);
        }
    }


    protected void handItemClick(int cur, int pre) {
        if (cur != pre) {
            setSelectedItem(cur, true);
        }
    }

    public SlideSelectView(Context context) {
        super(context);
        init(context, null);
    }

    public SlideSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SlideSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SlideSelectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        float density = context.getResources().getDisplayMetrics().density;
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics());
        mIndicatorWidth = (int) (mIndicatorWidth * density + 0.5f);
        mIndicatorHeight = (int) (mIndicatorHeight * density + 0.5f);
        mIndicatorTriangleHeight = (int) (mIndicatorTriangleHeight * density + 0.5f);
        mIndicatorTriangleWidth = (int) (mIndicatorTriangleWidth * density + 0.5f);
        setOrientation(HORIZONTAL);
        TypedArray a = attrs == null ? null : context.obtainStyledAttributes(attrs, R.styleable.SlideSelectView);
        if (a != null) {
            mTextSize = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_android_textSize, mTextSize);
            mItemMinPaddingTop = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_itemMinPaddingTop, mItemMinPaddingTop);
            mItemMinPaddingBottom = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_itemMinPaddingBottom, mItemMinPaddingBottom);
            mItemMinPaddingHorizontal = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_itemMinPaddingHorizontal, mItemMinPaddingHorizontal);
            mIndicatorWidth = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_indicatorWidth, mIndicatorWidth);
            mIndicatorHeight = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_indicatorHeight, mIndicatorHeight);
            mIndicatorTriangleWidth = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_indicatorTriangleWidth, mIndicatorTriangleWidth);
            mIndicatorTriangleHeight = a.getDimensionPixelSize(
                    R.styleable.SlideSelectView_indicatorTriangleHeight, mIndicatorTriangleHeight);
            mSelectScale = a.getFloat(R.styleable.SlideSelectView_selectedTextScale, mSelectScale);
            mIndicatorColor = a.getColor(R.styleable.SlideSelectView_indicatorColor, mIndicatorColor);
            mColorStateList = a.getColorStateList(R.styleable.SlideSelectView_android_textColor);
            mSelectedColorStateList = a.getColorStateList(R.styleable.SlideSelectView_selectedTextColor);
            a.recycle();
        }
        if (mColorStateList == null) {
            mColorStateList = ColorStateList.valueOf(mDefaultTextColor);
        }
        if (mSelectedColorStateList == null) {
            mSelectedColorStateList = ColorStateList.valueOf(mDefaultSelectedTextColor);
        }
    }

    public void setTextSize(int textSize) {
        this.mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics());
        updateItemStyles();
    }

    public void setSelectedColor(int textColor) {
        if (mSelectedColorStateList.getDefaultColor() != textColor) {
            mSelectedColorStateList = ColorStateList.valueOf(textColor);
            invalidate();
        }
    }

    public void setSelectedScale(float scale) {
        if (scale > 0 && mSelectScale != scale) {
            mSelectScale = scale;
        }
    }

    public void setIndicatorColor(int color) {
        if (mPaint.getColor() != color) {
            mPaint.setColor(color);
            invalidate();
        }
    }

    public void setIndicatorWidth(int width) {
        if (mIndicatorWidth != width) {
            mIndicatorWidth = width;
            invalidate();
        }
    }

    public void setIndicatorHeight(int height) {
        if (mIndicatorHeight != height) {
            mIndicatorHeight = height;
            invalidate();
        }
    }

    public void setIndicatorTriangleWidth(int width) {
        if (mIndicatorTriangleWidth != width) {
            mIndicatorTriangleWidth = width;
            invalidate();
        }
    }

    public void setIndicatorTriangleHeight(int height) {
        if (mIndicatorTriangleHeight != height) {
            mIndicatorTriangleHeight = height;
            invalidate();
        }
    }

    public void setTextColor(int textColor) {
        if (mColorStateList.getDefaultColor() != textColor) {
            mColorStateList = ColorStateList.valueOf(textColor);
            invalidate();
        }
        updateItemStyles();
    }

    public void setItemPaddingHorizonal(int paddingHorizonalPixel) {
        this.mItemMinPaddingHorizontal = paddingHorizonalPixel;
    }

    public void setItemPaddingTop(int paddingTopPixel) {
        this.mItemMinPaddingTop = paddingTopPixel;
    }

    public void setItemPaddingBottom(int paddingBottomPixel) {
        this.mItemMinPaddingBottom = paddingBottomPixel;
    }

    public int getSelectedIndex() {
        return mCurrentPosition;
    }

    public void setItemProvider(ItemProvider provider) {
        if (mItemProvider != provider) {
            removeAllViewsInLayout();
            mItemProvider = provider;
            if (provider != null) {
                buildItemWithProvider(provider, provider.getCount());
            }
        }
    }

    public void setLayoutParams(LayoutParams lp) {
        mItemLayoutParams = lp;
    }

    private void buildItemWithProvider(ItemProvider provider, int count) {
        if (count <= 0) return;
        boolean isViewTab = (provider instanceof ItemProvider.ViewProvider);
        for (int i = 0; i < count; i++) {
            if (isViewTab) {
                addItem(i, ((ItemProvider.ViewProvider) provider).getView(i, null, SlideSelectView.this));
            } else {
                CharSequence label = provider.getTitle(i);
                addItem(i, makeItem(label));
            }
        }
        updateItemStyles();
        listenTreeObserver();
    }

    private View makeItem(CharSequence title) {
        TextView tab = new TextView(getContext());
        tab.setEnabled(true);
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        tab.setIncludeFontPadding(false);
        return tab;
    }

    private void addItem(final int position, View tab) {
        tab.setFocusable(true);
        tab.setTag(TAB_INDEX, position);
        tab.setOnClickListener(mItemClick);
        int left = Math.max(mItemMinPaddingHorizontal, tab.getPaddingLeft());
        int top = Math.max(mItemMinPaddingTop, tab.getPaddingTop());
        int right = Math.max(mItemMinPaddingHorizontal, tab.getPaddingRight());
        int bottom = Math.max(mItemMinPaddingBottom, tab.getPaddingBottom());
        tab.setPadding(left, top, right, bottom);
        if (mItemLayoutParams != null && tab.getLayoutParams() == null) {
            addView(tab, position, mItemLayoutParams);
        } else {
            addView(tab, position);
        }
    }

    private void updateItemStyles() {
        int itemCount = getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View v = getChildAt(i);
            if (v instanceof TextView) {
                updateItemText((TextView) v, 1);
            } else {
                break;
            }
        }
    }

    private void updateItemText(TextView textView, float textScale) {
        if (textScale != 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize * textScale);
        }
        if (mColorStateList != null) {
            textView.setTextColor(mColorStateList);
        }
    }

    public void setSelectedItem(int position, boolean animator) {
        if (position >= 0 && position < getItemViewCount()) {
            if (mCurrentPosition != position) {
                int previous = mCurrentPosition;
                mCurrentPosition = position;
                if (mListener != null) {
                    mListener.onItemSelected(position, previous);
                }
            }
            scrollToItem(position, animator ? -1 : 0, 0, 0, true, false, true);
        }
    }

    private int getViewWidthWithInsetMargin(View view) {
        LayoutParams lp = view == null ? null : (LayoutParams) view.getLayoutParams();
        return lp == null ? 0 : lp.width(view);
    }

    @Override
    protected void dispatchMeasure(int widthExcludeUnusedSpec, int heightExcludeUnusedSpec) {
        super.dispatchMeasure(widthExcludeUnusedSpec, heightExcludeUnusedSpec);
        int itemCount = getItemViewCount();
        int widthAvailable = MeasureSpec.getSize(widthExcludeUnusedSpec);
        mLeftMakeUp = mRightMakeUp = 0;
        if (itemCount > 0 && widthAvailable > 0) {
            mLeftMakeUp = mRightMakeUp = widthAvailable >> 1;
            mLeftMakeUp -= getViewWidthWithInsetMargin(getItemView(0)) >> 1;
            mRightMakeUp -= getViewWidthWithInsetMargin(getItemView(itemCount - 1)) >> 1;
            setContentSize(getContentWidth() + mLeftMakeUp + mRightMakeUp, getContentHeight(), getMeasureState());
        }
    }

    @Override
    protected void dispatchLayout(int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        super.dispatchLayout(contentLeft + mLeftMakeUp, contentTop, contentWidth, contentHeight);
    }

    @Override
    protected void doDrawOver(Canvas canvas, int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        super.doDrawOver(canvas, contentLeft, contentTop, contentWidth, contentHeight);
        if (mIndicatorColor != 0) {
            boolean drawLine = (mIndicatorHeight > 0 && mIndicatorWidth > 0);
            boolean drawTriangle = (mIndicatorTriangleHeight > 0 && mIndicatorTriangleWidth > 0);
            if (drawLine || drawTriangle) {
                mPath.reset();
                Rect visibleContentBounds = getVisibleContentBounds();
                int bottom = getHeight();
                int middle = visibleContentBounds.centerX();
                if (drawLine) {
                    int start = middle - (mIndicatorWidth >> 1);
                    int end = start + mIndicatorWidth;
                    mPath.moveTo(start, bottom);
                    mPath.lineTo(end, bottom);
                    bottom -= mIndicatorHeight;
                    mPath.lineTo(end, bottom);
                    mPath.lineTo(start, bottom);
                    mPath.close();
                }
                if (drawTriangle) {
                    int start = middle - (mIndicatorTriangleWidth >> 1);
                    int end = start + mIndicatorTriangleWidth;
                    mPath.moveTo(start, bottom);
                    mPath.lineTo(middle, bottom - mIndicatorTriangleHeight);
                    mPath.lineTo(end, bottom);
                }
                mPaint.setColor(mIndicatorColor);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawPath(mPath, mPaint);
            }
        }
    }

    @Override
    protected void cancelTouch(boolean resetToIdle) {
        int fling = 0;
        if (OnScrollChangeListener.SCROLL_STATE_IDLE != getScrollState()) {
            fling = flingToWhere(0, 0, false);
        }
        super.cancelTouch(resetToIdle && fling == 0);
    }

    @Override
    protected int formatDuration(int duration) {
        return Math.max(0, Math.min(Math.max((int) (duration * 1.5f), 250), 800));
    }

    @Override
    protected boolean fling(FlingScroller scroller, int velocityX, int velocityY) {
        boolean handled = super.fling(scroller, velocityX, velocityY);
        if (handled) {
            scroller.stop();
            int scrollX = getScrollX();
            int finalX = scroller.getFinalX();
            int deltaX = getAdjustFlingDelta(scrollX, finalX);
            if (deltaX == 0) {
                handled = false;
                setScrollState(OnScrollChangeListener.SCROLL_STATE_IDLE);
            } else {
                if (deltaX > 0) {
                    scroller.setMaxFling(deltaX, 0);
                } else {
                    scroller.setMinFling(deltaX, 0);
                }
                scroller.fling(velocityX, velocityY);
                scroller.resetMinMaxFling();
            }
        }
        return handled;
    }

    private int getAdjustFlingDelta(int scrollX, int finalX) {
        int adjust = 0;
        int adjustIndex = getAdjustSelectItem(scrollX + finalX);
        int deltaX = offsetX(getItemView(adjustIndex), true, true) - scrollX;
        if (finalX > 0 && deltaX > 0) {
            if (deltaX < finalX) {
                adjust = deltaX;
            } else if (adjustIndex > 0) {
                adjust = offsetX(getItemView(adjustIndex - 1), true, true) - scrollX;
            }
        }
        if (finalX < 0 && deltaX < 0) {
            if (deltaX > finalX) {
                adjust = deltaX;
            } else if (adjust < getItemViewCount() - 1) {
                adjust = offsetX(getItemView(adjustIndex + 1), true, true) - scrollX;
            }
        }
        return adjust;
    }

    @Override
    protected void onScrollStateChanged(int newState, int prevState) {
        super.onScrollStateChanged(newState, prevState);
        if (newState == OnScrollChangeListener.SCROLL_STATE_IDLE) {
            setSelectedItem(getAdjustSelectItem(getScrollX()), true);
        }
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, Rect visibleBounds, boolean fromScrollChanged) {
        super.onScrollChanged(scrollX, scrollY, visibleBounds, fromScrollChanged);
        int itemCount = getItemViewCount();
        if (itemCount > 0) {
            int position = getAdjustSelectItem(scrollX);
            View view = getItemView(position);
            if (view != null) {
                int offsetX = offsetX(view, true, true) - scrollX;
                float offsetPercent = offsetX / ((float) ((LayoutParams) view.getLayoutParams()).width(view) / 2);
                if (mListener != null) {
                    mListener.onItemFling(position, offsetPercent);
                }
                updateViewWhileFling(view, position, offsetPercent, offsetPercent == 0);
            }
        }
    }

    private void updateViewWhileFling(View view, int index, float percent, boolean selected) {
        if ((mSelectedColorStateList != null || mSelectScale > 0) && (view instanceof TextView)) {
            TextView textView = (TextView) view;
            TextView textViewPrevious = mPreviousView == null ? null : mPreviousView.get();
            if (selected) {
                if (mSelectedColorStateList != null) {
                    textView.setTextColor(mSelectedColorStateList);
                }
                if (mSelectScale != 0) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize * mSelectScale);
                }
            } else {
                if (mSelectScale != 0) {
                    updateItemText(textView, 1 + Math.max(0, 1 - Math.abs(percent)) * (mSelectScale - 1));
                }
                if (mCurrentPosition == index && mSelectedColorStateList != null) {
                    textView.setTextColor(mSelectedColorStateList);
                }
            }
            if (textViewPrevious == null || textViewPrevious != textView) {
                if (textViewPrevious != null) {
                    updateItemText(textViewPrevious, 1);
                }
                mPreviousView = new WeakReference(textView);
            }
        }
    }

    private int getAdjustSelectItem(int scrollX) {
        int adjustIndex = -1;
        int maxItemIndex = getItemViewCount() - 1;
        if (maxItemIndex >= 0) {
            int itemIndex = -1;
            int count = getChildCount();
            int finalCentre = scrollX;
            int minIndex = -1;
            int maxIndex = -1;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (skipVirtualChild(child, true)) continue;
                if (minIndex != -1 && maxIndex != -1 && minIndex <= maxIndex) break;
                if (++itemIndex == 0) {
                    finalCentre += (child.getLeft() + child.getWidth() / 2);
                }
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int viewLeft = child.getLeft() - lp.leftMargin();
                int viewRight = child.getRight() + lp.rightMargin();
                if (finalCentre < viewLeft) {
                    maxIndex = itemIndex;
                    if (minIndex == -1) {
                        minIndex = Math.max(0, maxIndex - 1);
                    }
                } else if (finalCentre < viewRight) {
                    minIndex = maxIndex = itemIndex;
                } else {
                    minIndex = itemIndex;
                }
            }
            if (minIndex == maxIndex || maxIndex == -1) {
                adjustIndex = minIndex;
            } else {
                View minView = getItemView(minIndex);
                View maxView = getItemView(maxIndex);
                int toMinRight = finalCentre - minView.getRight();
                int toMaxLeft = maxView.getLeft() - finalCentre;
                adjustIndex = toMinRight < toMaxLeft ? minIndex : maxIndex;
            }
        }
        return adjustIndex;
    }

    private int flingToWhere(int moved, int velocity, boolean fling) {
        int scrolled = getScrollX(), willScroll;
        if (velocity == 0) {
            velocity = -(int) Math.signum(moved);
        }
        int targetIndex = mCurrentPosition;
        int itemSize = getViewWidthWithInsetMargin(getItemView(mCurrentPosition));
        int absVelocity = velocity > 0 ? velocity : -velocity;
        int pageItemCount = getItemViewCount();
        if (Math.abs(moved) > mTouchSlop) {
            int halfItemSize = itemSize / 2;
            if (absVelocity > mMinFlingVelocity) {
                if (velocity > 0 && mCurrentPosition < pageItemCount - 1 && (velocity / 10 - moved) > halfItemSize) {
                    targetIndex++;
                }
                if (velocity < 0 && mCurrentPosition > 0 && (moved - velocity / 10) > halfItemSize) {
                    targetIndex--;
                }
            } else {
                if (moved > halfItemSize && mCurrentPosition > 0) {
                    targetIndex--;
                }
                if (moved < -halfItemSize && mCurrentPosition < pageItemCount - 1) {
                    targetIndex++;
                }
            }
        }
        int targetScroll = computeScrollOffset(getItemView(targetIndex), 0, true);
        if ((willScroll = targetScroll - scrolled) != 0) {
            setScrollState(OnScrollChangeListener.SCROLL_STATE_IDLE);
            setSelectedItem(targetIndex, true);
        }
        return willScroll;
    }

    protected int computeScrollOffset(View child, int offset, boolean centreWithParent) {
        int scrollRange, targetScroll;
        targetScroll = offsetX(child, centreWithParent, true) + offset;
        scrollRange = getHorizontalScrollRange();
        return Math.max(0, Math.min(scrollRange, targetScroll));
    }

    private SlideSelectListener mListener = null;

    public void setSlideSelectListener(SlideSelectListener l) {
        mListener = l;
    }

    public interface SlideSelectListener {
        void onItemSelected(int selectedIndex, int previousIndex);

        void onItemFling(int index, float offsetPercent);
    }

}