package com.rexy.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.rexy.widgets.adpter.ItemProvider;
import com.rexy.widgets.layout.ScrollLayout;
import com.rexy.widgets.layout.WidgetLayout;

import java.lang.ref.WeakReference;

/**
 * Created by rexy on 2018/3/28.
 */

public class SlideSelectScrollView extends ScrollLayout {
    private static int TAB_INDEX = com.rexy.widgetlayout.R.id.widgetLayoutViewIndexType;
    /**
     * 以下是设置tab item 的最小padding 值。
     */
    private int mItemMinPaddingHorizontal = 0;
    private int mItemMinPaddingTop = 0;
    private int mItemMinPaddingBottom = 0;

    private int mTextSize = 16;
    private int mTextColor = 0xFF666666;
    private int mTextColorResId = 0;

    private int mIndicatorWidth = 250;
    private int mIndicatorHeight = 5;
    private int mIndicatorColor = 0xFFFF8010;
    private int mIndicatorTriangleHeight = 16;
    private int mIndicatorTriangleWidth = 24;

    private float mSelectScale = 1.625f;
    private int mSelectColor = 0xFFFF8010;

    private int mLeftMakeUp;
    private int mRightMakeUp;
    private int mCurrentPosition = 0; //当前选中的tab 索引。
    protected ItemProvider mItemProvider = null;
    private Path mPath = new Path();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private WidgetLayout.LayoutParams mItemLayoutParams;
    WeakReference<TextView> mPreviousView;

    private OnClickListener mItemClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Object tag = view.getTag(TAB_INDEX);
            int cur = (tag instanceof Integer) ? (Integer) tag : mCurrentPosition;
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

    public SlideSelectScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public SlideSelectScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SlideSelectScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SlideSelectScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics());
        setOrientation(HORIZONTAL);
    }

    public void setTextSize(int textSize) {
        this.mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics());
        updateItemStyles();
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        updateItemStyles();
    }

    public void setTextColorId(int resId) {
        this.mTextColorResId = resId;
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

    public void setLayoutParams(WidgetLayout.LayoutParams lp) {
        mItemLayoutParams = lp;
    }

    private void buildItemWithProvider(ItemProvider provider, int count) {
        if (count <= 0) return;
        boolean isViewTab = (provider instanceof ItemProvider.ViewProvider);
        for (int i = 0; i < count; i++) {
            if (isViewTab) {
                addItem(i, ((ItemProvider.ViewProvider) provider).getView(i, null, SlideSelectScrollView.this));
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
        if (mTextColorResId != 0) {
            textView.setTextColor(getContext().getResources().getColorStateList(mTextColorResId));
        } else if (mTextColor != 0) {
            textView.setTextColor(mTextColor);
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
        WidgetLayout.LayoutParams lp = view == null ? null : (LayoutParams) view.getLayoutParams();
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
        return Math.max(0, Math.min(Math.max(duration, 250), 800));
    }

    @Override
    protected boolean fling(ScrollLayout.FlingScroller scroller, int velocityX, int velocityY) {
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

    private void print(CharSequence msg) {
        Log.d("selectView", msg.toString());
    }

    @Override
    protected void onScrollStateChanged(int newState, int prevState) {
        super.onScrollStateChanged(newState, prevState);
        print(String.format("state changed from %d to %d", prevState, newState));
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
                float offsetPercent = offsetX / ((float) ((WidgetLayout.LayoutParams) view.getLayoutParams()).width(view) / 2);
                if (mListener != null) {
                    mListener.onItemFling(position, offsetPercent);
                }
                print(String.format("scroll changed selected=%d,index=%d,offset=.2f", mCurrentPosition, position, offsetPercent));
                updateViewWhileFling(view, position, offsetPercent, offsetPercent == 0);
            }
        }
    }

    private void updateViewWhileFling(View view, int index, float percent, boolean selected) {
        if ((mSelectColor != 0 || mSelectScale > 0) && (view instanceof TextView)) {
            TextView textView = (TextView) view;
            TextView textViewPrevious = mPreviousView == null ? null : mPreviousView.get();
            if (selected) {
                if (mSelectColor != 0) {
                    textView.setTextColor(mSelectColor);
                }
                if (mSelectScale != 0) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize * mSelectScale);
                }
            } else {
                if (mSelectScale != 0) {
                    updateItemText(textView, 1 + Math.max(0, 1 - Math.abs(percent)) * (mSelectScale - 1));
                }
                if (mCurrentPosition == index && mSelectColor != 0) {
                    textView.setTextColor(mSelectColor);
                }
            }
            if (textViewPrevious == null || textViewPrevious != textView) {
                if(textViewPrevious!=null){
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
                WidgetLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
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