package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.NestedScrollingChildHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.rexy.widgetlayout.R;
import com.rexy.widgets.ViewHelper;
import com.rexy.widgets.divider.BorderDivider;
import com.rexy.widgets.drawable.FloatDrawable;

/**
 * <p>
 * 1.support gravity,maxWidth,maxHeight,widthPercent,heightPercent for itself
 * 2.all its directly child can use layout_gravity,maxWidth,maxHeight,widthPercent,heightPercent to limit its size and layout position。
 * 3.support all sides of container border and layout divider
 * 4.provide interface to resize all of its child View margin and draw any thing below or over the child
 * 5.support to take over its {@link #onInterceptTouchEvent(MotionEvent)} and {@link #onTouchEvent(MotionEvent)}
 * 6.support hover drawable animation when press just like ios
 * </p>
 * <p>
 * <p>
 * <p>
 * subclass extends this base class  can implement {@link #dispatchMeasure(int, int)} and {@link #dispatchLayout(int, int, int, int)}
 * to measure and layout all its children self
 * <p>
 * most time just use this layout class and provide a LayoutManager
 * </p>
 * <!--hover drawable ignore touch out side to give up hover -->
 * <attr name="ignoreForegroundStateWhenTouchOut" format="boolean" />
 * <!--hover drawable color-->
 * <attr name="foregroundColor" format="color" />
 * <!--hover drawable round rectangle radius -->
 * <attr name="foregroundRadius" format="dimension" />
 * <!--hover drawable animation duration -->
 * <attr name="foregroundDuration" format="integer" />
 * <!--hover drawable min alpha to support -->
 * <attr name="foregroundAlphaMin" format="integer" />
 * <!--hover drawable max alpha to support -->
 * <attr name="foregroundAlphaMax" format="integer" />
 * <p>
 * <!--shade edge effect-->
 * <attr name="edgeEffectEnable" format="boolean" />
 * <p>
 * <!-- layout orientation and gesture direction-->
 * <attr name="android:orientation" />
 * <p>
 * <!--width and height support percent of parent-->
 * <attr name="widthPercent" format="fraction" />
 * <attr name="heightPercent" format="fraction" />
 * <p>
 * <!--edge effect when touch move to the end-->
 * <attr name="defaultBorderDividerWidth" format="dimension" />
 * * <!--左边线的颜色，宽度，和边线padding-->
 * <attr name="borderLeft" format="reference"/>
 * <attr name="borderLeftColor" format="color"/>
 * <attr name="borderLeftWidth" format="dimension"/>
 * <attr name="borderLeftMargin" format="dimension"/>
 * <attr name="borderLeftMarginStart" format="dimension"/>
 * <attr name="borderLeftMarginEnd" format="dimension"/>
 * <p>
 * <!--上边线的颜色，宽度，和边线padding-->
 * <attr name="borderTop" format="reference"/>
 * <attr name="borderTopColor" format="color"/>
 * <attr name="borderTopWidth" format="dimension"/>
 * <attr name="borderTopMargin" format="dimension"/>
 * <attr name="borderTopMarginStart" format="dimension"/>
 * <attr name="borderTopMarginEnd" format="dimension"/>
 * <p>
 * <!--右边线的颜色，宽度，和边线padding-->
 * <attr name="borderRight" format="reference"/>
 * <attr name="borderRightColor" format="color"/>
 * <attr name="borderRightWidth" format="dimension"/>
 * <attr name="borderRightMargin" format="dimension"/>
 * <attr name="borderRightMarginStart" format="dimension"/>
 * <attr name="borderRightMarginEnd" format="dimension"/>
 * <p>
 * <!--下边线的颜色，宽度，和边线padding-->
 * <attr name="borderBottom" format="reference"/>
 * <attr name="borderBottomColor" format="color"/>
 * <attr name="borderBottomWidth" format="dimension"/>
 * <attr name="borderBottomMargin" format="dimension"/>
 * <attr name="borderBottomMarginStart" format="dimension"/>
 * <attr name="borderBottomMarginEnd" format="dimension"/>
 * <p>
 * <!-- content margin just like margin but different-->
 * <attr name="contentMarginHorizontal" format="dimension" />
 * <attr name="contentMarginVertical" format="dimension" />
 * <attr name="contentMargin" format="dimension" />
 * <attr name="contentMarginLeft" format="dimension"/>
 * <attr name="contentMarginTop" format="dimension"/>
 * <attr name="contentMarginRight" format="dimension"/>
 * <attr name="contentMarginBottom" format="dimension"/>
 * <!--item margin between at orientation of horizontal and vertical-->
 * <attr name="itemMarginBetween" format="dimension"/>
 * <attr name="itemMarginHorizontal" format="dimension"/>
 * <attr name="itemMarginVertical" format="dimension"/>
 * <p>
 * <p>
 * <!--水平分割线Drawable-->
 * <attr name="dividerHorizontal" format="reference"/>
 * <!--水平分割线颜色-->
 * <attr name="dividerColorHorizontal" format="color"/>
 * <!--水平分割线宽-->
 * <attr name="dividerWidthHorizontal" format="dimension"/>
 * <!--水平分割线开始和结束padding-->
 * <attr name="dividerPaddingHorizontal" format="dimension"/>
 * <attr name="dividerPaddingHorizontalStart" format="dimension"/>
 * <attr name="dividerPaddingHorizontalEnd" format="dimension"/>
 * <p>
 * <!--垂直分割线Drawable-->
 * <attr name="dividerVertical" format="reference"/>
 * <!--垂直分割线颜色-->
 * <attr name="dividerColorVertical" format="color"/>
 * <!--垂直分割线宽-->
 * <attr name="dividerWidthVertical" format="dimension"/>
 * <!--垂直分割线开始 和结束padding-->
 * <attr name="dividerPaddingVertical" format="dimension"/>
 * <attr name="dividerPaddingVerticalStart" format="dimension"/>
 * <attr name="dividerPaddingVerticalEnd" format="dimension"/>
 *
 * @author: rexy
 * @date: 2017-04-25 09:32
 */
public class WidgetLayout extends ViewGroup {

    /**
     * Horizontal layout gesture and direction
     *
     * @see #setOrientation(int)
     */
    public static final int HORIZONTAL = 1;

    /**
     * Vertical layout or gesture and direction
     *
     * @see #setOrientation(int)
     */
    public static final int VERTICAL = 2;

    private static final int[] ATTRS_PROPERTIES = new int[]{
            android.R.attr.gravity,
            android.R.attr.maxWidth,
            android.R.attr.maxHeight
    };

    private static final int[] ATTRS_PARAMS = new int[]{
            android.R.attr.layout_gravity,
            android.R.attr.maxWidth,
            android.R.attr.maxHeight,
            android.R.attr.layout_weight,
            R.attr.heightPercent,
            R.attr.widthPercent
    };

    /**
     * content gravity {@link Gravity},self maxWidth maxHeight,widthPercent,heightPercent
     */

    private int mGravity;  // content gravity

    private int mMaxWidth = -1; // view max width

    private int mMaxHeight = -1; // view max height

    private float mWidthPercent = 0; // width percent of its parent limit

    private float mHeightPercent = 0; // height percent of its parent limit

    /**
     * @see #HORIZONTAL
     * @see #VISIBLE
     * @see #setOrientation(int)
     * @see #onOrientationChanged(int, int)
     */
    private int mOrientation;

    /**
     * whether it support touch scroll action .
     */
    protected boolean mTouchScrollEnable = false;

    /**
     * EdgeEffect enable in scroll layout
     */
    protected boolean mEdgeEffectEnable;

    private boolean mIgnoreForegroundStateWhenTouchOut = false;


    protected int mTouchSlop = 0; // touch scale slop
    /**
     * control content margin and item divider also it's margin padding
     */
    private int mVirtualCount = 0; // virtual child count
    private int mContentLeft = 0; // content start left since the layout will be constrained by gravity
    private int mContentTop = 0; // content start top since the layout will be constrained by gravity
    private int mContentWidth = 0; // content width include content inset and content margin
    private int mContentHeight = 0; // content height include content inset and content margin
    private int mMeasureState = 0; // measure state of all children
    protected Rect mContentInset = new Rect(); // content inset
    protected Rect mVisibleContentBounds = new Rect(); // visible bounds exclude padding
    private boolean mAttachLayout = false; // attach after layout if true
    private boolean mItemTouchInvoked = false; // flag for a processing with a series of touch event that consumed by custom handler
    private NestedScrollingChildHelper mScrollingChildHelper; // child nested scroll helper

    //start-dev
    private String mLogTag;
    private boolean mDevLog = false;
    //end-dev

    /**
     * provide a chance let the user to take over touch event.
     */
    private OnItemTouchListener mItemTouchListener;

    /**
     * this border and divider provider which can custom and draw border and divider
     */
    private BorderDivider mBorderDivider = null;

    /**
     * a decoration interface to adjust child margin and draw some over or under the child
     */
    private DrawerDecoration mDrawerDecoration;

    /**
     * hove drawable that will draw over the content
     */
    private FloatDrawable mForegroundDrawable = null;

    public WidgetLayout(Context context) {
        super(context);
        init(context, null);
    }

    public WidgetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WidgetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public WidgetLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final TypedArray typed1 = attrs == null ? null : context.obtainStyledAttributes(attrs, ATTRS_PROPERTIES);
        if (typed1 != null) {
            mGravity = typed1.getInteger(0, mGravity);
            mMaxWidth = typed1.getDimensionPixelSize(1, mMaxWidth);
            mMaxHeight = typed1.getDimensionPixelSize(2, mMaxHeight);
            typed1.recycle();
        }
        final TypedArray typed2 = attrs == null ? null : context.obtainStyledAttributes(attrs, R.styleable.WidgetLayout);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mBorderDivider = BorderDivider.from(typed2, (int) (0.5f + context.getResources().getDisplayMetrics().density * 0.4f));
        if (typed2 != null) {
            mEdgeEffectEnable = typed2.getBoolean(R.styleable.WidgetLayout_edgeEffectEnable, mEdgeEffectEnable);
            mWidthPercent = typed2.getFraction(R.styleable.WidgetLayout_widthPercent, 1, 1, mWidthPercent);
            mHeightPercent = typed2.getFraction(R.styleable.WidgetLayout_heightPercent, 1, 1, mHeightPercent);
            mOrientation = typed2.getInteger(R.styleable.WidgetLayout_android_orientation, -1) + 1;
            int floatColor = typed2.getColor(R.styleable.WidgetLayout_foregroundColor, 0);
            if (floatColor != 0) {
                final int floatRadius = typed2.getDimensionPixelSize(R.styleable.WidgetLayout_foregroundRadius, 0);
                final int floatDuration = typed2.getInt(R.styleable.WidgetLayout_foregroundDuration, 120);
                final int floatMinAlpha = typed2.getInt(R.styleable.WidgetLayout_foregroundAlphaMin, 0);
                final int floatMaxAlpha = typed2.getInt(R.styleable.WidgetLayout_foregroundAlphaMax, 50);
                final FloatDrawable floatDrawable = new FloatDrawable(floatColor, floatMinAlpha, floatMaxAlpha).duration(floatDuration).radius(floatRadius);
                setForegroundDrawable(floatDrawable);
                setClickable(true);
            }
            typed2.recycle();
        }
    }

    //start:log
    protected boolean isLogAccess() {
        return mLogTag != null;
    }

    protected boolean isDevLogAccess() {
        return mLogTag != null && mDevLog;
    }

    public void setLogTag(String logTag, boolean devMode) {
        mLogTag = logTag;
        mDevLog = devMode;
    }

    protected void print(CharSequence category, CharSequence msg) {
        print(category, msg, false);
    }

    void printDev(CharSequence category, CharSequence msg) {
        print(category, msg, true);
    }

    private void print(CharSequence category, CharSequence msg, boolean dev) {
        String tag = mLogTag + (dev ? "@" : "#");
        if (category == null || msg == null) {
            msg = category == null ? msg : category;
        } else {
            tag = tag + category;
        }
        Log.d(tag, String.valueOf(msg));
    }
    //end:log

    /**
     * set content gravity
     *
     * @param gravity
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayoutIfNeed();
        }
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * set self max width to  measure
     */
    public void setMaxWidth(int maxWidth) {
        if (mMaxWidth != maxWidth) {
            mMaxWidth = maxWidth;
            requestLayoutIfNeed();
        }
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    /**
     * set self max height to  measure
     */
    public void setMaxHeight(int maxHeight) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight;
            requestLayoutIfNeed();
        }
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    /**
     * set width percent of its parent could give, this will overwrite layout_width property
     */
    public void setWidthPercent(float percent) {
        if (this.mWidthPercent != percent) {
            this.mWidthPercent = percent;
            requestLayoutIfNeed();
        }
    }

    public float getWidthPercent() {
        return this.mWidthPercent;
    }

    /**
     * set height percent of its parent could give, this will overwrite layout_width property
     */
    public void setHeightPercent(float percent) {
        if (this.mHeightPercent != percent) {
            this.mHeightPercent = percent;
            requestLayoutIfNeed();
        }
    }

    public float getHeightPercent() {
        return this.mHeightPercent;
    }

    /**
     * set layout and gesture direction
     *
     * @param orientation {@link #HORIZONTAL} and {@link #VERTICAL}
     * @see #onOrientationChanged(int, int)
     */
    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            final int oldOrientation = mOrientation;
            mOrientation = orientation;
            mAttachLayout = false;
            scrollTo(0, 0);
            onOrientationChanged(orientation, oldOrientation);
            requestLayout();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public boolean orientation(int orientation) {
        return (mOrientation & orientation) == orientation;
    }

    public boolean isOrientationHorizontal() {
        return orientation(HORIZONTAL);
    }

    public boolean isOrientationVertical() {
        return orientation(VERTICAL);
    }

    protected void requestLayoutIfNeed() {
        if (mAttachLayout && !isLayoutRequested()) {
            requestLayout();
        }
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
        mContentWidth = 0;
        mMeasureState = 0;
        mVirtualCount = 0;
        mContentHeight = 0;
        mContentLeft = 0;
        mContentTop = 0;
        mAttachLayout = false;
        mItemTouchInvoked = false;
        mContentInset.setEmpty();
        mVisibleContentBounds.setEmpty();
        mScrollingChildHelper = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mItemTouchInvoked = false;
        mScrollingChildHelper = null;
    }

    /**
     * set itemTouchListener , the user can handle the touch event first .
     */
    public void setOnItemTouchListener(OnItemTouchListener itemTouchListener) {
        this.mItemTouchListener = itemTouchListener;
    }

    /**
     * set implement to control drawer decoration such as onDraw onDrawOver getItemOffset .
     */
    public void setDrawerDecoration(DrawerDecoration drawerDecoration) {
        if (mDrawerDecoration != drawerDecoration) {
            mDrawerDecoration = drawerDecoration;
            requestLayoutIfNeed();
        }
    }

    public BorderDivider getBorderDivider() {
        return mBorderDivider;
    }

    /**
     * set content size after dispatch measure,so we can decide the final measure dimension
     *
     * @param contentWidth  just content width without margin and padding
     * @param contentHeight just content height without margin and padding
     * @param measureState  measure state {@link View#getMeasuredState()}
     * @see #dispatchMeasure(int, int)
     */
    protected void setContentSize(int contentWidth, int contentHeight, int measureState) {
        mContentWidth = contentWidth;
        mContentHeight = contentHeight;
        mMeasureState |= measureState;
    }

    /**
     * get content width with inset margin
     */
    public int getContentWidth() {
        return mContentWidth;
    }

    /**
     * get content height with inset margin
     */
    public int getContentHeight() {
        return mContentHeight;
    }

    public int getMeasureState() {
        return mMeasureState;
    }

    /**
     * get visible area rect exclude padding ,scrollX and scrollY are taken into account with a offset
     *
     * @see #computeVisibleBounds(int, int, boolean, boolean)
     */
    public Rect getVisibleContentBounds() {
        return mVisibleContentBounds;
    }

    /**
     * true if a layout process happened
     */
    public boolean isAttachLayoutFinished() {
        return mAttachLayout;
    }

    public FloatDrawable setForegroundDrawable(int color, int minAlpha, int maxAlpha) {
        FloatDrawable drawable = new FloatDrawable(color, minAlpha, maxAlpha);
        setForegroundDrawable(drawable);
        return drawable;
    }

    /**
     * set hover drawable {@link FloatDrawable}
     *
     * @param foregroundDrawable
     */
    public void setForegroundDrawable(FloatDrawable foregroundDrawable) {
        if (mForegroundDrawable != foregroundDrawable) {
            if (mForegroundDrawable != null) {
                mForegroundDrawable.setCallback(null);
                unscheduleDrawable(mForegroundDrawable);
            }
            if (foregroundDrawable == null) {
                mForegroundDrawable = null;
            } else {
                mForegroundDrawable = foregroundDrawable;
                foregroundDrawable.setCallback(this);
                foregroundDrawable.setVisible(getVisibility() == VISIBLE, false);
            }
        }
    }

    public FloatDrawable getForegroundDrawable() {
        return mForegroundDrawable;
    }

    public void setIgnoreForegroundStateWhenTouchOut(boolean ignoreForegroundStateWhenTouchOut) {
        mIgnoreForegroundStateWhenTouchOut = ignoreForegroundStateWhenTouchOut;
    }

    public boolean isIgnoreForegroundStateWhenTouchOut() {
        return mIgnoreForegroundStateWhenTouchOut;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        boolean result = super.verifyDrawable(who);
        if (!result && mForegroundDrawable == who) {
            result = true;
        }
        return result;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }
    //start:measure&layout&draw

    /**
     * measure child from parent MeasureSpec
     * subclass of Layout should aways use this measure function to apply extra property such as maxWidth,maxHeight,WidgetLayout_gravity
     */
    protected LayoutParams measure(View view, int itemPosition, int parentWidthMeasureSpec, int parentHeightMeasureSpec, int widthUsed, int heightUsed) {
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final int parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec);
        final int parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
        int childWidthDimension = lp.width, childHeightDimension = lp.height;
        if (!(view instanceof WidgetLayout)) {
            if (lp.widthPercent > 0) {
                childWidthDimension = (int) (parentWidth * lp.widthPercent);
            }
            if (lp.heightPercent > 0) {
                childHeightDimension = (int) (parentHeight * lp.heightPercent);
            }
        }
        lp.mPosition = itemPosition;
        lp.mInsets.setEmpty();
        if (mDrawerDecoration != null) {
            mDrawerDecoration.getItemOffsets(this, view, itemPosition, lp.mInsets);
        }
        view.measure(
                getChildMeasureSpec(Math.max(0, parentWidth - lp.horizontalMargin() - widthUsed), MeasureSpec.getMode(parentWidthMeasureSpec), lp.maxWidth, childWidthDimension),
                getChildMeasureSpec(Math.max(0, parentHeight - lp.verticalMargin() - heightUsed), MeasureSpec.getMode(parentHeightMeasureSpec), lp.maxHeight, childHeightDimension)
        );
        return lp;
    }

    private int size(int minSize, int maxSize, int contentSize, int unused, boolean include) {
        int finalSize = contentSize + (include ? unused : 0);
        finalSize = Math.max(finalSize, minSize);
        if (maxSize > 0 && maxSize < finalSize) {
            finalSize = maxSize;
        }
        if (!include) {
            finalSize = Math.max(finalSize - unused, 0);
        }
        return finalSize;
    }

    private int getChildMeasureSpec(int size, int specMode, int maxSize, int childDimension) {
        int resultSize = size;
        int resultMode = MeasureSpec.EXACTLY;
        if (childDimension >= 0) {
            resultSize = childDimension;
        } else if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
            resultMode = MeasureSpec.AT_MOST;
            if (specMode == MeasureSpec.UNSPECIFIED && maxSize <= 0) {
                resultMode = MeasureSpec.UNSPECIFIED;//MeasureSpec.AT_MOST ? MeasureSpec.UNSPECIFIED ?
            }
        }
        if (maxSize > 0 && resultSize > maxSize) {
            resultSize = maxSize;
        }
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mVirtualCount = mContentWidth = mContentHeight = mMeasureState = 0;
        mContentInset.setEmpty();
        if (mDrawerDecoration != null) {
            mDrawerDecoration.getContentOffsets(this, mContentInset);
        }
        mBorderDivider.applyContentMargin(mContentInset);
        final int minWidth = getSuggestedMinimumWidth();
        final int minHeight = getSuggestedMinimumHeight();
        final int marginH = mContentInset.left + mContentInset.right;
        final int marginV = mContentInset.top + mContentInset.bottom;
        final int paddingH = getPaddingLeft() + getPaddingRight();
        final int paddingV = getPaddingTop() + getPaddingBottom();
        final int width = mWidthPercent > 0 ? (int) (mWidthPercent * MeasureSpec.getSize(widthMeasureSpec)) : MeasureSpec.getSize(widthMeasureSpec);
        final int height = mHeightPercent > 0 ? (int) (mHeightPercent * MeasureSpec.getSize(heightMeasureSpec)) : MeasureSpec.getSize(heightMeasureSpec);
        final int oldWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int oldHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthMode = (mTouchScrollEnable && orientation(HORIZONTAL)) ? MeasureSpec.UNSPECIFIED : oldWidthMode;
        final int heightMode = (mTouchScrollEnable && orientation(VERTICAL)) ? MeasureSpec.UNSPECIFIED : oldHeightMode;
        final int adjustWidthSpec = MeasureSpec.makeMeasureSpec(width, oldWidthMode);
        final int adjustHeightSpec = MeasureSpec.makeMeasureSpec(height, oldHeightMode);
        final int visibleWidthSpec = MeasureSpec.makeMeasureSpec(size(minWidth, mMaxWidth, width, paddingH + marginH, false), widthMode);
        final int visibleHeightSpec = MeasureSpec.makeMeasureSpec(size(minHeight, mMaxHeight, height, paddingV + marginV, false), heightMode);
        dispatchMeasure(visibleWidthSpec, visibleHeightSpec);
        final int status = mMeasureState;
        final int contentWidth = mContentWidth;
        final int contentHeight = mContentHeight;
        setContentSize(contentWidth + marginH, contentHeight + marginV, status);
        setMeasuredDimension(
                resolveSizeAndState(size(minWidth, mMaxWidth, mContentWidth, paddingH, true), adjustWidthSpec, status),
                resolveSizeAndState(size(minHeight, mMaxHeight, mContentHeight, paddingV, true), adjustHeightSpec, status << MEASURED_HEIGHT_STATE_SHIFT)
        );
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();
        computeVisibleBounds(scrollX, scrollY, false, false);
        doAfterMeasure(measuredWidth, measuredHeight, contentWidth, contentHeight);
        if (isDevLogAccess()) {
            printDev("MLD", String.format("measure: [width=%d,height=%d],[contentWidth=%d,contentHeight=%d]", measuredWidth, measuredHeight, contentWidth, contentHeight));
        }
    }

    /**
     * tips:do your measure no need to take content margin into account since we have handled.
     * after all child measure must call {@link #setContentSize(int, int, int)};
     *
     * @param widthExcludeUnusedSpec  widthMeasureSpec without padding and content margin
     * @param heightExcludeUnusedSpec heightMeasureSpec without padding and content margin.
     */
    protected void dispatchMeasure(int widthExcludeUnusedSpec, int heightExcludeUnusedSpec) {
        final int childCount = getChildCount();
        int contentWidth = 0, contentHeight = 0, childState = 0;
        int itemPosition = 0, itemMargin;
        if (isOrientationHorizontal()) {
            itemMargin = mBorderDivider.getItemMarginHorizontal();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if (skipChild(child)) continue;
                if (itemPosition != 0) contentWidth += itemMargin;
                LayoutParams params = measure(child, itemPosition++, widthExcludeUnusedSpec, heightExcludeUnusedSpec, 0, 0);
                contentWidth += params.width(child);
                int itemHeight = params.height(child);
                if (contentHeight < itemHeight) {
                    contentHeight = itemHeight;
                }
                childState |= child.getMeasuredState();
            }
        } else {
            itemMargin = mBorderDivider.getItemMarginVertical();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if (skipChild(child)) continue;
                if (itemPosition != 0) contentWidth += itemMargin;
                LayoutParams params = measure(child, itemPosition++, widthExcludeUnusedSpec, heightExcludeUnusedSpec, 0, 0);
                contentHeight += params.height(child);
                int itemWidth = params.width(child);
                if (contentWidth < itemWidth) {
                    contentWidth = itemWidth;
                }
                childState |= child.getMeasuredState();
            }
        }
        setContentSize(contentWidth, contentHeight, childState);
    }

    /**
     * @param measuredWidth  self measure width
     * @param measuredHeight self measure height
     * @param contentWidth   real content width
     * @param contentHeight  real content height
     */
    protected void doAfterMeasure(int measuredWidth, int measuredHeight, int contentWidth, int contentHeight) {
    }

    @Override
    protected final void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean firstAttachLayout = !mAttachLayout;
        mAttachLayout = true;
        final Rect inset = mContentInset;
        final int contentWidth = mContentWidth - (inset.left + inset.right);
        final int contentHeight = mContentHeight - (inset.top + inset.bottom);
        final int unusedLeft = getPaddingLeft() + inset.left;
        final int unusedRight = getPaddingRight() + inset.right;
        final int unusedTop = getPaddingTop() + inset.top;
        final int unusedBottom = getPaddingBottom() + inset.bottom;
        int contentLeft = ViewHelper.getContentStartH(unusedLeft, getWidth() - unusedRight, contentWidth, 0, 0, mGravity);
        if (contentLeft < unusedLeft && mTouchScrollEnable && orientation(HORIZONTAL)) {
            contentLeft = unusedLeft;
        }
        int contentTop = ViewHelper.getContentStartV(unusedTop, getHeight() - unusedBottom, contentHeight, 0, 0, mGravity);
        if (contentTop < unusedTop && mTouchScrollEnable && orientation(VERTICAL)) {
            contentTop = unusedTop;
        }
        mContentLeft = contentLeft;
        mContentTop = contentTop;
        dispatchLayout(contentLeft, contentTop, contentWidth, contentHeight);
        if (firstAttachLayout) {
            mVisibleContentBounds.offset(1, 1);
            computeVisibleBounds(getScrollX(), getScrollY(), false, true);
        }
        doAfterLayout(contentLeft, contentTop, contentWidth, contentHeight, firstAttachLayout);
        if (isDevLogAccess()) {
            printDev("MLD", String.format("layout: contentLeft=%d,contentRight=%d,contentWidth=%d,contentHeight=%d, firstAttachLayout=%s", contentLeft, contentTop, contentWidth, contentHeight, firstAttachLayout));
        }
    }

    /**
     * @param contentLeft   format content's left no need to consider margin and padding of content.
     * @param contentTop    format content's top no need to consider margin and padding of content.
     * @param contentWidth  real content width exclude padding and content margin
     * @param contentHeight real content height exclude padding and content margin
     */
    protected void dispatchLayout(int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        final int count = getChildCount();
        int childLeft = contentLeft, childTop = contentTop, childRight, childBottom, itemMargin;
        if (isOrientationHorizontal()) {
            itemMargin = mBorderDivider.getItemMarginHorizontal();
            final int baseTop = contentTop, baseBottom = contentTop + contentHeight;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (skipChild(child)) continue;
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                childTop = ViewHelper.getContentStartH(baseTop, baseBottom, child.getMeasuredHeight(), params.topMargin(), params.bottomMargin(), params.gravity);
                childBottom = childTop + child.getMeasuredHeight();
                childLeft += params.leftMargin();
                childRight = childLeft + child.getMeasuredWidth();
                child.layout(childLeft, childTop, childRight, childBottom);
                childLeft = childRight + params.rightMargin + itemMargin;
            }
        } else {
            itemMargin = mBorderDivider.getItemMarginVertical();
            final int baseLeft = contentLeft, baseRight = contentLeft + contentWidth;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (skipChild(child)) continue;
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                childTop += params.topMargin();
                childBottom = childTop + child.getMeasuredHeight();
                childLeft = ViewHelper.getContentStartH(baseLeft, baseRight, child.getMeasuredWidth(), params.leftMargin(), params.rightMargin(), params.gravity);
                childRight = childLeft + child.getMeasuredWidth();
                child.layout(childLeft, childTop, childRight, childBottom);
                childTop = childBottom + params.bottomMargin() + itemMargin;
            }
        }
    }

    /**
     * @param contentLeft   format content's left no need to consider margin and padding of content.
     * @param contentTop    format content's top no need to consider margin and padding of content.
     * @param contentWidth  real content width exclude padding and content margin
     * @param contentHeight real content height exclude padding and content margin
     */
    protected void doAfterLayout(int contentLeft, int contentTop, int contentWidth, int contentHeight, boolean firstAttachLayout) {
    }

    @Override
    public final void dispatchDraw(Canvas canvas) {
        if (mDrawerDecoration != null) {
            mDrawerDecoration.onDraw(this, canvas);
        }
        final Rect inset = mContentInset;
        final int width = getWidth();
        final int height = getHeight();
        final int contentLeft = mContentLeft;
        final int contentTop = mContentTop;
        final int contentWidth = mContentWidth - (inset.left + inset.right);
        final int contentHeight = mContentHeight - (inset.top + inset.bottom);
        doBeforeDraw(canvas, contentLeft, contentTop, contentWidth, contentHeight);
        super.dispatchDraw(canvas);
        doAfterDraw(canvas, contentLeft, contentTop, contentWidth, contentHeight);
        mBorderDivider.drawBorder(canvas, width, height, getScrollX(), getScrollY());
        if (mDrawerDecoration != null) {
            mDrawerDecoration.onDrawOver(this, canvas);
        }
        if (mForegroundDrawable != null) {
            mForegroundDrawable.setBounds(0, 0, width, height);
            mForegroundDrawable.draw(canvas);
        }
    }

    /**
     * @param contentLeft   format content's left no need to consider margin and padding of content.
     * @param contentTop    format content's top no need to consider margin and padding of content.
     * @param contentWidth  real content width exclude padding and content margin
     * @param contentHeight real content height exclude padding and content margin
     */
    protected void doBeforeDraw(Canvas canvas, int contentLeft, int contentTop, int contentWidth, int contentHeight) {
    }

    /**
     * @param contentLeft   format content's left no need to consider margin and padding of content.
     * @param contentTop    format content's top no need to consider margin and padding of content.
     * @param contentWidth  real content width exclude padding and content margin
     * @param contentHeight real content height exclude padding and content margin
     */
    protected void doAfterDraw(Canvas canvas, int contentLeft, int contentTop, int contentWidth, int contentHeight) {
        boolean horizontal = isOrientationHorizontal();
        int count = getChildCount();
        if (horizontal && mBorderDivider.isVisibleDividerVertical()) {
            final int halfMargin = mBorderDivider.getItemMarginHorizontal() / 2;
            int start = getPaddingTop();
            int end = getHeight() - getPaddingBottom();
            int position;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (skipChild(child)) continue;
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                position = child.getRight() + params.rightMargin() + halfMargin;
                mBorderDivider.drawDivider(canvas, start, end, position, false);
            }
        }
        if (!horizontal && mBorderDivider.isVisibleDividerHorizontal()) {
            final int halfMargin = mBorderDivider.getItemMarginVertical() / 2;
            int start = getPaddingLeft();
            int end = getWidth() - getPaddingRight();
            int position;
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (skipChild(child)) continue;
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                position = child.getBottom() + params.bottomMargin() + halfMargin;
                mBorderDivider.drawDivider(canvas, start, end, position, true);
            }
        }
    }

    //end:measure&layout&draw


    // start: tool function

    /**
     * get max scroll range at direction vertical
     */
    public int getHorizontalScrollRange() {
        return Math.max(0, getContentWidth() - mVisibleContentBounds.width());
    }

    /**
     * get max scroll range at direction vertical
     */
    public int getVerticalScrollRange() {
        return Math.max(0, getContentHeight() - mVisibleContentBounds.height());
    }

    /**
     * compute visible bounds exclude padding and considerate scroll  x and y .
     *
     * @param scrollX       {@link #getScrollX()}
     * @param scrollY       {@link #getScrollY()}
     * @param scrollChanged true indicate it was called by scroll change.
     * @param apply         true to notify listener.
     */
    protected void computeVisibleBounds(int scrollX, int scrollY, boolean scrollChanged, boolean apply) {
        final int beforeHash = mVisibleContentBounds.hashCode();
        int width = apply ? getWidth() : 0, height = apply ? getHeight() : 0;
        if (width <= 0) width = getMeasuredWidth();
        if (height <= 0) height = getMeasuredHeight();
        mVisibleContentBounds.left = getPaddingLeft() + scrollX;
        mVisibleContentBounds.top = getPaddingTop() + scrollY;
        mVisibleContentBounds.right = mVisibleContentBounds.left + width - getPaddingLeft() - getPaddingRight();
        mVisibleContentBounds.bottom = mVisibleContentBounds.top + height - getPaddingTop() - getPaddingBottom();
        if (beforeHash != mVisibleContentBounds.hashCode()) {
            if (!scrollChanged && !apply) {
                final int adjustScrollX = Math.min(scrollX, getHorizontalScrollRange());
                final int adjustScrollY = Math.min(scrollY, getVerticalScrollRange());
                if (adjustScrollX != scrollX || adjustScrollY != scrollY) {
                    scrollTo(adjustScrollX, adjustScrollY);
                }
            }
            if (apply) {
                onScrollChanged(scrollX, scrollY, mVisibleContentBounds, scrollChanged);
            }
            if (isDevLogAccess()) {
                StringBuilder sb = new StringBuilder(32);
                sb.append("scrollX=").append(scrollX);
                sb.append(",scrollY=").append(scrollY).append(",visibleBounds=").append(mVisibleContentBounds);
                sb.append(",scrollChanged=").append(scrollChanged);
                printDev("scroll", sb);
            }
        }
    }

    private boolean pointInView(float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((getRight() - getLeft()) + slop) &&
                localY < ((getBottom() - getTop()) + slop);
    }

    public final int indexOfItemView(View view) {
        if (view != null) {
            int virtualIndex = 0;
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (skipVirtualChild(child, true)) continue;
                if (view == child) {
                    return virtualIndex;
                }
                virtualIndex++;
            }
        }
        return -1;
    }

    public final View getItemView(int itemIndex) {
        View result = null;
        int itemCount = getItemViewCount();
        if (itemIndex >= 0 && itemIndex < itemCount) {
            result = getVirtualChildAt(itemIndex, true);
        }
        return result;
    }

    public final int getItemViewCount() {
        int itemCount = mVirtualCount;
        if (itemCount == 0) {
            itemCount = mVirtualCount = getVirtualChildCount(true);
        }
        return itemCount;
    }

    protected final int getVirtualChildCount(boolean withoutGone) {
        int virtualCount = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, withoutGone)) continue;
            virtualCount++;
        }
        return virtualCount;
    }

    public final View getVirtualChildAt(int virtualIndex, boolean withoutGone) {
        int virtualCount = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (skipVirtualChild(child, withoutGone)) continue;
            if (virtualCount == virtualIndex) {
                return child;
            }
            virtualCount++;
        }
        return null;
    }

    protected boolean skipChild(View child) {
        return child == null || child.getVisibility() == View.GONE;
    }

    protected boolean skipVirtualChild(View child, boolean withoutGone) {
        return child == null || (withoutGone && child.getVisibility() == View.GONE);
    }

    //start: touch gesture

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (mItemTouchListener != null) {
            mItemTouchListener.onRequestDisallowInterceptTouchEvent(disallowIntercept);
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    /**
     * intercept touch event ,first handle by ItemTouchListener then layout manager and super will be last.
     *
     * @return true to intercept event and all event will handle by self {{@link #onTouchEvent(MotionEvent)}}
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return dispatchInterceptTouchEvent(e, dispatchOnItemTouchIntercept(e)) || super.onInterceptTouchEvent(e);
    }

    protected boolean dispatchInterceptTouchEvent(MotionEvent e, boolean consumed) {
        return consumed;
    }

    protected boolean dispatchOnItemTouchIntercept(MotionEvent e) {
        final int action = e.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_DOWN) {
            mItemTouchInvoked = false;
        }
        if (mItemTouchListener != null) {
            if (mItemTouchListener.onInterceptTouchEvent(this, e) && action != MotionEvent.ACTION_CANCEL) {
                mItemTouchInvoked = true;
                return true;
            }
        }
        return false;
    }

    /**
     * handle touch event ,first handle by ItemTouchListener then layout manager and super will be last.
     *
     * @return true to consume theme,and all event will come here again later.
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return dispatchTouchEvent(e, dispatchOnItemTouch(e)) || super.onTouchEvent(e);
    }

    protected boolean dispatchTouchEvent(MotionEvent e, boolean consumed) {
        return consumed;
    }

    protected boolean dispatchOnItemTouch(MotionEvent e) {
        final int action = e.getAction();
        boolean handled = false;
        if (mItemTouchInvoked) {
            if (action == MotionEvent.ACTION_DOWN) {
                mItemTouchInvoked = false;
            } else {
                mItemTouchListener.onTouchEvent(this, e);
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    // Clean up for the next gesture.
                    mItemTouchInvoked = false;
                }
                handled = true;
            }
        }
        if (action != MotionEvent.ACTION_DOWN && mItemTouchListener != null) {
            if (mItemTouchListener.onInterceptTouchEvent(this, e)) {
                mItemTouchInvoked = true;
                handled = true;
            }
        }
        if (mForegroundDrawable != null && isClickable()) {
            if (handled) {
                mForegroundDrawable.start(false);
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    mForegroundDrawable.start(true);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    mForegroundDrawable.start(false);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (!mIgnoreForegroundStateWhenTouchOut && !pointInView(e.getX(), e.getY(), mTouchSlop)) {
                        mForegroundDrawable.start(false);
                    }
                }
            }
        }
        return handled;
    }

    //end: touch gesture

    @Override
    protected void onScrollChanged(int l, int t, int ol, int ot) {
        super.onScrollChanged(l, t, ol, ot);
        computeVisibleBounds(l, t, true, true);
    }

    protected void onScrollChanged(int scrollX, int scrollY, Rect visibleBounds, boolean fromScrollChanged) {
    }

    protected void onOrientationChanged(int orientation, int oldOrientation) {
    }

    /**
     * custom LayoutParams which support WidgetLayout_gravity,maxWidth and maxHeight in xml attr
     * what's more,it supports inset to resize margin of child view .
     */
    public static class LayoutParams extends MarginLayoutParams {
        public int gravity = -1;
        public int maxWidth = -1;
        public int maxHeight = -1;
        public float weight = 0;
        public float widthPercent = 0;
        public float heightPercent = 0;

        private Rect mInsets = new Rect();
        private Object mExtras;
        private int mPosition = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, ATTRS_PARAMS);
            gravity = a.getInteger(0, gravity);
            maxWidth = a.getDimensionPixelSize(1, maxWidth);
            maxHeight = a.getDimensionPixelSize(2, maxHeight);
            weight = a.getFloat(3, weight);
            widthPercent = a.getFraction(5, 1, 1, widthPercent);
            heightPercent = a.getFraction(4, 1, 1, heightPercent);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            if (source instanceof LayoutParams) {
                LayoutParams lp = (LayoutParams) source;
                gravity = lp.gravity;
                weight = lp.weight;
                maxWidth = lp.maxWidth;
                maxHeight = lp.maxHeight;
                widthPercent = lp.widthPercent;
                heightPercent = lp.heightPercent;
                mExtras = lp.mExtras;
                mPosition = lp.mPosition;
                mInsets.set(lp.mInsets);
            } else {
                if (source instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) source;
                    gravity = lp.gravity;
                    weight = lp.weight;
                }
                if (source instanceof FrameLayout.LayoutParams) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) source;
                    gravity = lp.gravity;
                }
            }
        }

        public int position() {
            return mPosition;
        }

        public void setPosition(int pos) {
            mPosition = pos;
        }

        /**
         * get view width include its margin and inset width
         */
        public int width(View view) {
            return view.getMeasuredWidth() + leftMargin + mInsets.left + rightMargin + mInsets.right;
        }

        /**
         * get view height include its margin and inset width
         */
        public int height(View view) {
            return view.getMeasuredHeight() + topMargin + mInsets.top + bottomMargin + mInsets.bottom;
        }

        public int leftMargin() {
            return leftMargin + mInsets.left;
        }

        public int topMargin() {
            return topMargin + mInsets.top;
        }

        public int rightMargin() {
            return rightMargin + mInsets.right;
        }

        public int bottomMargin() {
            return bottomMargin + mInsets.bottom;
        }

        public int horizontalMargin() {
            return leftMargin + mInsets.left + rightMargin + mInsets.right;
        }

        public int verticalMargin() {
            return topMargin + mInsets.top + bottomMargin + mInsets.bottom;
        }

        public void setExtras(Object extras) {
            mExtras = extras;
        }

        public Object getExtras() {
            return mExtras;
        }
    }

    /**
     * this interface give a chance to resize content margin and resize all its children's margin.
     * beyond that it can draw something on the canvas in this canvas coordinate
     */
    public static abstract class DrawerDecoration {
        public void onDraw(WidgetLayout parent, Canvas c) {
        }

        public void onDrawOver(WidgetLayout parent, Canvas c) {
        }

        public void getItemOffsets(WidgetLayout parent, View child, int itemPosition, Rect outRect) {
            outRect.set(0, 0, 0, 0);
        }

        public void getContentOffsets(WidgetLayout parent, Rect outRect) {
            outRect.set(0, 0, 0, 0);
        }
    }

    /**
     * this interface provided a chance to handle touch event
     */
    public interface OnItemTouchListener {
        void onTouchEvent(WidgetLayout parent, MotionEvent e);

        boolean onInterceptTouchEvent(WidgetLayout parent, MotionEvent e);

        void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept);
    }
}