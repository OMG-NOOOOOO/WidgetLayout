package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import com.rexy.widgets.drawable.FloatDrawable;

/**
 * <p>
 * 1.support gravity,maxWidth,maxHeight for itself
 * 2.all its directly child View can use layout_gravity,maxWidth,maxHeight to limit its size and layout position。
 * 3.can draw container border and child divider
 * 7.provide a chance to resize all of its child View margin and draw any thing below or over the child
 * 4.provide a chance to take over its measure and layout process
 * 5.support to take over its {@link #onInterceptTouchEvent(MotionEvent)} and {@link #onTouchEvent(MotionEvent)}
 * 6.support hover drawable animation when press like ios
 * </p>
 * <p>
 * <p>
 * subclass extends this base class should at least to implement {@link #dispatchMeasure(int, int)} and {@link #dispatchLayout(int, int)}
 * to measure and layout all its children
 * </p>
 * <p>
 * <p>
 * <declare-styleable name="BaseViewGroup">
 * <!--hover drawable ignore touch out side to give up hover -->
 * <attr name="ignoreForegroundStateWhenTouchOut" format="boolean"/>
 * <!--hover drawable color-->
 * <attr name="foregroundColor" format="color"/>
 * <!--hover drawable round rectangle radius -->
 * <attr name="foregroundRadius" format="dimension"/>
 * <!--hover drawable animation duration -->
 * <attr name="foregroundDuration" format="integer"/>
 * <!--hover drawable min alpha to support -->
 * <attr name="foregroundAlphaMin" format="integer"/>
 * <!--hover drawable max alpha to support -->
 * <attr name="foregroundAlphaMax" format="integer"/>
 * <p>
 * <!--edge effect when touch move to the end-->
 * <attr name="edgeEffectEnable" format="boolean"/>
 * <p>
 * <!--左边线的颜色，宽度，和边线padding-->
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
 * <attr name="contentMarginHorizontal" format="dimension"/>
 * <attr name="contentMarginVertical" format="dimension"/>
 * <attr name="contentMarginLeft" format="dimension"/>
 * <attr name="contentMarginTop" format="dimension"/>
 * <attr name="contentMarginRight" format="dimension"/>
 * <attr name="contentMarginBottom" format="dimension"/>
 * <!--item margin between at orientation of horizontal and vertical-->
 * <attr name="itemMarginBetween" format="dimension"/>
 * <attr name="itemMarginHorizontal" format="dimension"/>
 * <attr name="itemMarginVertical" format="dimension"/>
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
 * </declare-styleable>
 * </p>
 *
 * @author: rexy
 * @date: 2017-04-25 09:32
 */
public abstract class BaseViewGroup extends ViewGroup {

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
     * content gravity {@link android.view.Gravity}
     */
    private int mGravity;
    private int mMaxWidth = -1;
    private int mMaxHeight = -1;
    private float mWidthPercent = 0;
    private float mHeightPercent = 0;
    /**
     * @see #HORIZONTAL
     * @see #VISIBLE
     */
    private int mOrientation;
    protected boolean mEdgeEffectEnable;
    private boolean mIgnoreForegroundStateWhenTouchOut = false;
    /**
     * hove drawable that will draw over the content
     */
    private FloatDrawable mForegroundDrawable = null;

    /**
     * whether it support touch scroll action .
     */
    private boolean mTouchScrollEnable = false;
    /**
     * provide a chance let the user to take over touch event.
     */
    private OnItemTouchListener mItemTouchListener;
    /**
     * a decoration interface to adjust child margin and draw some over or under the child
     */
    private DrawerDecoration mDrawerDecoration;

    protected BorderDivider mBorderDivider = null;

    protected int mTouchSlop = 0;
    /**
     * control content margin and item divider also it's margin padding
     */
    private int mVirtualCount = 0;
    private int mContentLeft = 0;
    private int mContentTop = 0;
    private int mContentWidth = 0;
    private int mContentHeight = 0;
    private int mMeasureState = 0;
    protected Rect mContentInset = new Rect();
    private Rect mVisibleContentBounds = new Rect();
    private boolean mAttachLayout = false;
    private boolean mItemTouchInvoked = false;
    //start-dev
    private String mLogTag;
    private boolean mDevLog = true;
    private long mTimeMeasureStart, mTimeLayoutStart, mTimeDrawStart;
    long mLastMeasureCost, mLastLayoutCost, mLastDrawCost;
    //end-dev

    public BaseViewGroup(Context context) {
        super(context);
        init(context, null);
    }

    public BaseViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BaseViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BaseViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typed1 = attrs == null ? null : context.obtainStyledAttributes(attrs, ATTRS_PROPERTIES);
        if (typed1 != null) {
            mGravity = typed1.getInteger(0, mGravity);
            mMaxWidth = typed1.getDimensionPixelSize(1, mMaxWidth);
            mMaxHeight = typed1.getDimensionPixelSize(2, mMaxHeight);
            typed1.recycle();
        }
        TypedArray typed2 = attrs == null ? null : context.obtainStyledAttributes(attrs, R.styleable.BaseViewGroup);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mBorderDivider = BorderDivider.from(typed2, (int) (0.5f + context.getResources().getDisplayMetrics().density * 0.4f));
        if (typed2 != null) {
            mEdgeEffectEnable = typed2.getBoolean(R.styleable.BaseViewGroup_edgeEffectEnable, mEdgeEffectEnable);
            mWidthPercent = typed2.getFraction(R.styleable.BaseViewGroup_widthPercent, 1, 1, mWidthPercent);
            mHeightPercent = typed2.getFraction(R.styleable.BaseViewGroup_heightPercent, 1, 1, mHeightPercent);
            mOrientation = typed2.getInteger(R.styleable.BaseViewGroup_android_orientation, -1) + 1;
            int floatColor = typed2.getColor(R.styleable.BaseViewGroup_foregroundColor, 0);
            if (floatColor != 0) {
                int floatRadius = typed2.getDimensionPixelSize(R.styleable.BaseViewGroup_foregroundRadius, 0);
                int floatDuration = typed2.getInt(R.styleable.BaseViewGroup_foregroundDuration, 120);
                int floatMinAlpha = typed2.getInt(R.styleable.BaseViewGroup_foregroundAlphaMin, 0);
                int floatMaxAlpha = typed2.getInt(R.styleable.BaseViewGroup_foregroundAlphaMax, 50);
                FloatDrawable floatDrawable = new FloatDrawable(floatColor, floatMinAlpha, floatMaxAlpha).duration(floatDuration).radius(floatRadius);
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

    protected void requestLayoutIfNeed() {
        if (mAttachLayout && !isLayoutRequested()) {
            requestLayout();
        }
    }

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

    /**
     * set layout and gesture direction
     *
     * @param orientation {@link #HORIZONTAL} and {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            int oldOrientation = mOrientation;
            mOrientation = orientation;
            mAttachLayout = false;
            scrollTo(0, 0);
            onOrientationChanged(orientation, oldOrientation);
            requestLayout();
        }
    }

    /**
     * set whether to support scroll when touch move
     *
     * @param touchScrollEnable true to support touch scroll
     */
    public void setTouchScrollEnable(boolean touchScrollEnable) {
        mTouchScrollEnable = touchScrollEnable;
    }

    public boolean isTouchScrollEnable() {
        return mTouchScrollEnable;
    }

    /**
     * set content size after measure,so we can decide the final measure dimension
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

    protected int getMeasureState() {
        return mMeasureState;
    }

    /**
     * get visible area rect ,scrollX and scrollY are taken into account with a offset
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

    public void setOnItemTouchListener(OnItemTouchListener itemTouchListener) {
        this.mItemTouchListener = itemTouchListener;
    }

    public void setDrawerDecoration(DrawerDecoration drawerDecoration) {
        if (mDrawerDecoration != drawerDecoration) {
            mDrawerDecoration = drawerDecoration;
            requestLayoutIfNeed();
        }
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

    public void setIgnoreForegroundStateWhenTouchOut(boolean ignoreForegroundStateWhenTouchOut) {
        mIgnoreForegroundStateWhenTouchOut = ignoreForegroundStateWhenTouchOut;
    }

    public FloatDrawable setForegroundDrawable(int color, int minAlpha, int maxAlpha) {
        FloatDrawable drawable = new FloatDrawable(color, minAlpha, maxAlpha);
        setForegroundDrawable(drawable);
        return drawable;
    }

    public boolean isIgnoreForegroundStateWhenTouchOut() {
        return mIgnoreForegroundStateWhenTouchOut;
    }

    public FloatDrawable getForegroundDrawable() {
        return mForegroundDrawable;
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
        reset(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset(false);
    }

    protected void reset(boolean attached) {
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

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mTimeMeasureStart = System.currentTimeMillis();
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
        dispatchMeasure(
                MeasureSpec.makeMeasureSpec(size(minWidth, mMaxWidth, width, paddingH + marginH, false), widthMode),
                MeasureSpec.makeMeasureSpec(size(minHeight, mMaxHeight, height, paddingV + marginV, false), heightMode)
        );
        final int status = mMeasureState;
        final int contentWidth = mContentWidth;
        final int contentHeight = mContentHeight;
        setContentSize(contentWidth + marginH, contentHeight + marginV, status);
        setMeasuredDimension(
                resolveSizeAndState(size(minWidth, mMaxWidth, mContentWidth, paddingH, true), adjustWidthSpec, status),
                resolveSizeAndState(size(minHeight, mMaxHeight, mContentHeight, paddingV, true), adjustHeightSpec, status << MEASURED_HEIGHT_STATE_SHIFT)
        );
        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();
        computeVisibleBounds(getScrollX(), getScrollY(), false, false);
        doAfterMeasure(measuredWidth, measuredHeight, contentWidth, contentHeight);
        mLastMeasureCost = System.currentTimeMillis() - mTimeMeasureStart;
        if (isDevLogAccess()) {
            printDev("MLD", String.format("measure cost %d ms: [width=%d,height=%d],[contentWidth=%d,contentHeight=%d]", mLastMeasureCost, measuredWidth, measuredHeight, contentWidth, contentHeight));
        }
    }

    /**
     * tips:do your measure no need to take content margin into account since we have handled.
     * after all child measure must call {@link #setContentSize(int, int, int)};
     *
     * @param widthExcludeUnusedSpec  widthMeasureSpec without padding and content margin
     * @param heightExcludeUnusedSpec heightMeasureSpec without padding and content margin.
     */
    protected abstract void dispatchMeasure(int widthExcludeUnusedSpec, int heightExcludeUnusedSpec);

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
        mTimeLayoutStart = System.currentTimeMillis();
        final Rect inset = mContentInset;
        final int contentWidth = mContentWidth - (inset.left + inset.right);
        final int contentHeight = mContentHeight - (inset.top + inset.bottom);
        int unusedLeft = getPaddingLeft() + inset.left;
        int unusedRight = getPaddingRight() + inset.right;
        int unusedTop = getPaddingTop() + inset.top;
        int unusedBottom = getPaddingBottom() + inset.bottom;
        int contentLeft = getContentStartH(unusedLeft, getWidth() - unusedRight, contentWidth, mGravity);
        if (contentLeft < unusedLeft && mTouchScrollEnable && orientation(HORIZONTAL)) {
            contentLeft = unusedLeft;
        }
        int contentTop = getContentStartV(unusedTop, getHeight() - unusedBottom, contentHeight, mGravity);
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
        mLastLayoutCost = System.currentTimeMillis() - mTimeLayoutStart;
        if (isDevLogAccess()) {
            printDev("MLD", String.format("layout cost %d ms: contentLeft=%d,contentRight=%d,contentWidth=%d,contentHeight=%d, firstAttachLayout=%s", mLastLayoutCost, contentLeft, contentTop, contentWidth, contentHeight, firstAttachLayout));
        }
    }

    /**
     * @param contentLeft   format content's left no need to consider margin and padding of content.
     * @param contentTop    format content's top no need to consider margin and padding of content.
     * @param contentWidth  real content width exclude padding and content margin
     * @param contentHeight real content height exclude padding and content margin
     */
    protected abstract void dispatchLayout(int contentLeft, int contentTop, int contentWidth, int contentHeight);

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
        mTimeDrawStart = System.currentTimeMillis();
        if (mDrawerDecoration != null) {
            mDrawerDecoration.onDraw(this, canvas);
        }
        final Rect inset = mContentInset;
        final int contentWidth = mContentWidth - (inset.left + inset.right);
        final int contentHeight = mContentHeight - (inset.top + inset.bottom);
        final int contentLeft = mContentLeft;
        final int contentTop = mContentTop;
        mBorderDivider.drawBorder(canvas, getWidth(), getHeight());
        doBeforeDraw(canvas, contentLeft, contentTop, contentWidth, contentHeight);
        super.dispatchDraw(canvas);
        doAfterDraw(canvas, contentLeft, contentTop, contentWidth, contentHeight);
        if (mDrawerDecoration != null) {
            mDrawerDecoration.onDrawOver(this, canvas);
        }
        if (mForegroundDrawable != null) {
            mForegroundDrawable.setBounds(0, 0, getWidth(), getHeight());
            mForegroundDrawable.draw(canvas);
        }
        mLastDrawCost = System.currentTimeMillis() - mTimeDrawStart;
        if (isDevLogAccess()) {
            printDev("MLD", String.format("draw cost %d ms", mLastDrawCost));
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
    }
    //end:measure&layout&draw

    //start: touch gesture
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (mItemTouchListener != null) {
            mItemTouchListener.onRequestDisallowInterceptTouchEvent(disallowIntercept);
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return dispatchOnItemTouchIntercept(e) || superOnInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return dispatchOnItemTouch(e) || superOnTouchEvent(e);
    }

    protected boolean superOnTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }

    protected boolean superOnInterceptTouchEvent(MotionEvent e) {
        return super.onInterceptTouchEvent(e);
    }

    protected void onOrientationChanged(int orientation, int oldOrientation) {
    }

    @Override
    protected void onScrollChanged(int l, int t, int ol, int ot) {
        super.onScrollChanged(l, t, ol, ot);
        computeVisibleBounds(l, t, true, true);
    }

    protected void onScrollChanged(int scrollX, int scrollY, Rect visibleBounds, boolean fromScrollChanged) {
    }

    protected void computeVisibleBounds(int scrollX, int scrollY, boolean scrollChanged, boolean apply) {
        int beforeHash = mVisibleContentBounds.hashCode(), width = apply ? getWidth() : 0, height = apply ? getHeight() : 0;
        if (width <= 0) width = getMeasuredWidth();
        if (height <= 0) height = getMeasuredHeight();
        mVisibleContentBounds.left = getPaddingLeft() + scrollX;
        mVisibleContentBounds.top = getPaddingTop() + scrollY;
        mVisibleContentBounds.right = mVisibleContentBounds.left + width - getPaddingLeft() - getPaddingRight();
        mVisibleContentBounds.bottom = mVisibleContentBounds.top + height - getPaddingTop() - getPaddingBottom();
        if (apply && beforeHash != mVisibleContentBounds.hashCode()) {
            if (isDevLogAccess()) {
                StringBuilder sb = new StringBuilder(32);
                sb.append("scrollX=").append(scrollX);
                sb.append(",scrollY=").append(scrollY).append(",visibleBounds=").append(mVisibleContentBounds);
                sb.append(",scrollChanged=").append(scrollChanged);
                printDev("scroll", sb);
            }
            onScrollChanged(scrollX, scrollY, mVisibleContentBounds, scrollChanged);
        }
    }

    private boolean pointInView(float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((getRight() - getLeft()) + slop) &&
                localY < ((getBottom() - getTop()) + slop);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        boolean result = super.verifyDrawable(who);
        if (!result && mForegroundDrawable == who) {
            result = true;
        }
        return result;
    }
    //end: touch gesture

    // start: tool function
    protected final View getVirtualChildAt(int virtualIndex, boolean withoutGone) {
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

    public final int getItemViewCount() {
        int itemCount = mVirtualCount;
        if (itemCount == 0) {
            itemCount = mVirtualCount = getVirtualChildCount(true);
        }
        return itemCount;
    }

    public final View getItemView(int itemIndex) {
        View result = null;
        int itemCount = getItemViewCount();
        if (itemIndex >= 0 && itemIndex < itemCount) {
            result = getVirtualChildAt(itemIndex, true);
        }
        return result;
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

    protected boolean skipChild(View child) {
        return child == null || child.getVisibility() == View.GONE;
    }

    protected boolean skipVirtualChild(View child, boolean withoutGone) {
        return child == null || (withoutGone && child.getVisibility() == View.GONE);
    }

    protected int getContentStartH(int containerLeft, int containerRight, int contentWillSize, int gravity) {
        return getContentStartH(containerLeft, containerRight, contentWillSize, 0, 0, gravity);
    }

    protected int getContentStartV(int containerTop, int containerBottom, int contentWillSize, int gravity) {
        return getContentStartV(containerTop, containerBottom, contentWillSize, 0, 0, gravity);
    }

    protected int getContentStartH(int containerLeft, int containerRight, int contentWillSize, int contentMarginLeft, int contentMarginRight, int gravity) {
        if (gravity != -1 || gravity != 0) {
            int start;
            final int mask = Gravity.HORIZONTAL_GRAVITY_MASK;
            final int maskCenter = Gravity.CENTER_HORIZONTAL;
            final int maskEnd = Gravity.RIGHT;
            final int okGravity = gravity & mask;
            if (maskCenter == okGravity) {//center
                start = containerLeft + (containerRight - containerLeft - (contentWillSize + contentMarginRight - contentMarginLeft)) / 2;
            } else if (maskEnd == okGravity) {//end
                start = containerRight - contentWillSize - contentMarginRight;
            } else {//start
                start = containerLeft + contentMarginLeft;
            }
            return start;
        }
        return containerLeft + contentMarginLeft;
    }

    protected int getContentStartV(int containerTop, int containerBottom, int contentWillSize, int contentMarginTop, int contentMarginBottom, int gravity) {
        if (gravity != -1 || gravity != 0) {
            int start;
            final int mask = Gravity.VERTICAL_GRAVITY_MASK;
            final int maskCenter = Gravity.CENTER_VERTICAL;
            final int maskEnd = Gravity.BOTTOM;
            final int okGravity = gravity & mask;
            if (maskCenter == okGravity) {//center
                start = containerTop + (containerBottom - containerTop - (contentWillSize + contentMarginBottom - contentMarginTop)) / 2;
            } else if (maskEnd == okGravity) {//end
                start = containerBottom - contentWillSize - contentMarginBottom;
            } else {//start
                start = containerTop + contentMarginTop;
            }
            return start;
        }
        return containerTop + contentMarginTop;
    }

    /**
     * get offset from first left item
     *
     * @param child
     * @param centreInVisibleBounds if true ,refer to parent view centre to get the offset .
     * @param marginInclude         take margin into view space.
     */
    protected int offsetX(View child, boolean centreInVisibleBounds, boolean marginInclude) {
        int current;
        MarginLayoutParams marginLp = marginInclude ? (MarginLayoutParams) child.getLayoutParams() : null;
        if (centreInVisibleBounds) {
            current = (child.getLeft() + child.getRight()) >> 1;
            if (marginLp != null) {
                current = current + (marginLp.rightMargin + marginLp.leftMargin) / 2;
            }
            return current - getPaddingLeft() + mVisibleContentBounds.left - mVisibleContentBounds.centerX();
        } else {
            current = child.getLeft();
            if (marginLp != null) {
                current = current + marginLp.leftMargin;
            }
            return current - getPaddingLeft();
        }
    }

    /**
     * get offset from first top item
     *
     * @param child
     * @param centreInVisibleBounds if true ,refer to parent view centre to get the offset .
     * @param marginInclude         take margin into view space.
     */
    protected int offsetY(View child, boolean centreInVisibleBounds, boolean marginInclude) {
        int current;
        MarginLayoutParams marginLp = marginInclude ? (MarginLayoutParams) child.getLayoutParams() : null;
        if (centreInVisibleBounds) {
            current = (child.getTop() + child.getBottom()) >> 1;
            if (marginLp != null) {
                current = current + (marginLp.bottomMargin + marginLp.topMargin) / 2;
            }
            return current - getPaddingTop() + mVisibleContentBounds.top - mVisibleContentBounds.centerY();
        } else {
            current = child.getTop();
            if (marginLp != null) {
                current = current + marginLp.topMargin;
            }
            return current - getPaddingTop();
        }
    }
    // end: tool function


    //start:compute scroll information.

    /**
     * get max scroll range at direction vertical
     */
    protected int getVerticalScrollRange() {
        return Math.max(0, getContentHeight() - mVisibleContentBounds.height());
    }

    @Override
    public int computeVerticalScrollRange() {
        return Math.max(mVisibleContentBounds.height(), getContentHeight());
    }

    @Override
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    public int computeVerticalScrollExtent() {
        return mVisibleContentBounds.height();
    }

    /**
     * get max scroll range at direction vertical
     */
    protected int getHorizontalScrollRange() {
        return Math.max(0, getContentWidth() - mVisibleContentBounds.width());
    }

    @Override
    public int computeHorizontalScrollRange() {
        return Math.max(mVisibleContentBounds.width(), getContentWidth());
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return Math.max(0, super.computeHorizontalScrollOffset());
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return mVisibleContentBounds.width();
    }

    public BorderDivider getBorderDivider() {
        return mBorderDivider;
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

    /**
     * measure child from parent MeasureSpec
     * subclass of BaseViewGroup should aways use this measure function to apply extra property such as maxWidth,maxHeight,layout_gravity
     */
    protected BaseViewGroup.LayoutParams measure(View view, int itemPosition, int parentWidthMeasureSpec, int parentHeightMeasureSpec, int widthUsed, int heightUsed) {
        BaseViewGroup.LayoutParams lp = (LayoutParams) view.getLayoutParams();
        int parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
        int childWidthDimension = lp.width, childHeightDimension = lp.height;
        if (!(view instanceof BaseViewGroup)) {
            if (lp.mWidthPercent > 0) {
                childWidthDimension = (int) (parentWidth * lp.mWidthPercent);
            }
            if (lp.mHeightPercent > 0) {
                childHeightDimension = (int) (parentHeight * lp.mHeightPercent);
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

    /**
     * custom LayoutParams which support layout_gravity,maxWidth and maxHeight in xml attr
     * what's more,it supports inset to resize margin of child view .
     */
    public static class LayoutParams extends MarginLayoutParams {
        public int gravity = -1;
        public int maxWidth = -1;
        public int maxHeight = -1;
        public float weight = 0;
        float mWidthPercent = 0;
        float mHeightPercent = 0;
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
            mWidthPercent = a.getFraction(5, 1, 1, mWidthPercent);
            mHeightPercent = a.getFraction(4, 1, 1, mHeightPercent);
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

        protected void setPosition(int pos) {
            mPosition = pos;
        }

        /**
         * get view width include its margin and inset width
         */
        public int width(View view) {
            return view.getMeasuredWidth() + horizontalMargin();
        }

        /**
         * get view height include its margin and inset width
         */
        public int height(View view) {
            return view.getMeasuredHeight() + verticalMargin();
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
            return leftMargin() + rightMargin();
        }

        public int verticalMargin() {
            return topMargin() + bottomMargin();
        }

        public void setExtras(Object extras) {
            mExtras = extras;
        }

        public Object getExtras() {
            return mExtras;
        }
    }

    /**
     * this interface provided a chance to handle touch event
     */
    public interface OnItemTouchListener {
        void onTouchEvent(BaseViewGroup parent, MotionEvent e);

        boolean onInterceptTouchEvent(BaseViewGroup parent, MotionEvent e);

        void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept);
    }

    /**
     * this interface give a chance to resize content margin and resize all its children's margin.
     * beyond that it can draw something on the canvas in this canvas coordinate
     */
    public static abstract class DrawerDecoration {
        public void onDraw(BaseViewGroup parent, Canvas c) {
        }

        public void onDrawOver(BaseViewGroup parent, Canvas c) {
        }

        public void getItemOffsets(BaseViewGroup parent, View child, int itemPosition, Rect outRect) {
            outRect.set(0, 0, 0, 0);
        }

        public void getContentOffsets(BaseViewGroup parent, Rect outRect) {
            outRect.set(0, 0, 0, 0);
        }
    }
}