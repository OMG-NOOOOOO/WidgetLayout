package com.rexy.widgets.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.rexy.widgetlayout.R
import com.rexy.widgets.drawable.FloatDrawable

/**
 *
 *
 * 1.support gravity,maxWidth,maxHeight for itself
 * 2.all its directly child View can use layout_gravity,maxWidth,maxHeight to limit its size and layout position。
 * 3.can draw container border and child divider
 * 7.provide a chance to resize all of its child View margin and draw any thing below or over the child
 * 4.provide a chance to take over its measure and layout process
 * 5.support to take over its [.onInterceptTouchEvent] and [.onTouchEvent]
 * 6.support hover drawable animation when press like ios
 *
 *
 *
 *
 *
 * subclass extends this base class should at least to implement [.dispatchMeasure] and [.dispatchLayout]
 * to measure and layout all its children
 *
 *
 *
 *
 *
 * <declare-styleable name="BaseViewGroup">
 *
 * <attr name="ignoreForegroundStateWhenTouchOut" format="boolean"></attr>
 *
 * <attr name="foregroundColor" format="color"></attr>
 *
 * <attr name="foregroundRadius" format="dimension"></attr>
 *
 * <attr name="foregroundDuration" format="integer"></attr>
 *
 * <attr name="foregroundAlphaMin" format="integer"></attr>
 *
 * <attr name="foregroundAlphaMax" format="integer"></attr>
</declare-styleable> *
 *
 *
 * <attr name="edgeEffectEnable" format="boolean"></attr>
 *
 *
 *
 * <attr name="borderLeftColor" format="color"></attr>
 * <attr name="borderLeftWidth" format="dimension"></attr>
 * <attr name="borderLeftMargin" format="dimension"></attr>
 * <attr name="borderLeftMarginStart" format="dimension"></attr>
 * <attr name="borderLeftMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="borderTopColor" format="color"></attr>
 * <attr name="borderTopWidth" format="dimension"></attr>
 * <attr name="borderTopMargin" format="dimension"></attr>
 * <attr name="borderTopMarginStart" format="dimension"></attr>
 * <attr name="borderTopMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="borderRightColor" format="color"></attr>
 * <attr name="borderRightWidth" format="dimension"></attr>
 * <attr name="borderRightMargin" format="dimension"></attr>
 * <attr name="borderRightMarginStart" format="dimension"></attr>
 * <attr name="borderRightMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="borderBottomColor" format="color"></attr>
 * <attr name="borderBottomWidth" format="dimension"></attr>
 * <attr name="borderBottomMargin" format="dimension"></attr>
 * <attr name="borderBottomMarginStart" format="dimension"></attr>
 * <attr name="borderBottomMarginEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="contentMarginLeft" format="dimension"></attr>
 * <attr name="contentMarginTop" format="dimension"></attr>
 * <attr name="contentMarginRight" format="dimension"></attr>
 * <attr name="contentMarginBottom" format="dimension"></attr>
 *
 * <attr name="contentMarginHorizontal" format="dimension"></attr>
 * <attr name="contentMarginVertical" format="dimension"></attr>
 *
 *
 *
 * <attr name="dividerColorHorizontal" format="color"></attr>
 *
 * <attr name="dividerWidthHorizontal" format="dimension"></attr>
 *
 * <attr name="dividerPaddingHorizontal" format="dimension"></attr>
 * <attr name="dividerPaddingHorizontalStart" format="dimension"></attr>
 * <attr name="dividerPaddingHorizontalEnd" format="dimension"></attr>
 *
 *
 *
 * <attr name="dividerColorVertical" format="color"></attr>
 *
 * <attr name="dividerWidthVertical" format="dimension"></attr>
 *
 * <attr name="dividerPaddingVertical" format="dimension"></attr>
 * <attr name="dividerPaddingVerticalStart" format="dimension"></attr>
 * <attr name="dividerPaddingVerticalEnd" format="dimension"></attr>
 *
 *
 * @author: rexy
 * @date: 2017-04-25 09:32
 */
abstract class BaseViewGroup : ViewGroup, BorderDivider.Callback {

    /**
     * content gravity [android.view.Gravity]
     */
    private var mGravity: Int = 0
    private var mMaxWidth = -1
    private var mMaxHeight = -1
    private var mWidthPercent = 0f
    private var mHeightPercent = 0f
    /**
     * @see HORIZONTAL
     *
     * @see VERTICAL
     */
    internal var mOrientation: Int = VERTICAL
    private var mClipToPadding: Boolean = false
    private var mEdgeEffectEnable: Boolean = false

    var isIgnoreForegroundStateWhenTouchOut = false
    /**
     * hove drawable that will draw over the content
     */
    private var mForegroundDrawable: FloatDrawable? = null


    /**
     * whether it support touch scroll action .
     */
    private var mTouchScrollEnable = true
    private var mCanTouchScrollHorizontal = false
    private var mCanTouchScrollVertical = false
    /**
     * provide a chance let the user to take over touch event.
     */
    private var mItemTouchListener: OnItemTouchListener? = null

    /**
     * a decoration interface to adjust child margin and draw some over or under the child
     */
    private var mDrawerDecoration: DrawerDecoration? = null


    protected var mTouchSlop = 0
    /**
     * control content margin and item divider also it's margin padding
     */
    protected var mBorderDivider: BorderDivider? = null

    private var mVirtualCount = 0
    /**
     * get content width with inset margin
     */
    var contentWidth = 0
        private set
    /**
     * get content height with inset margin
     */
    var contentHeight = 0
        private set
    protected var measureState = 0
        private set
    private val mContentInset = Rect()
    /**
     * get visible area rect ,scrollX and scrollY are taken into account with a offset
     *
     * @see .computeVisibleBounds
     */
    val visibleContentBounds = Rect()

    /**
     * true if a layout process happened
     */
    var isAttachLayoutFinished = false
        private set
    private var mItemTouchInvoked = false

    private var mLogTag: String? = null
    private var mDevLog = true
    private var mTimeMeasureStart: Long = 0
    private var mTimeLayoutStart: Long = 0
    private var mTimeDrawStart: Long = 0
    var mLastMeasureCost: Long = 0
    var mLastLayoutCost: Long = 0
    var mLastDrawCost: Long = 0


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
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        var a: TypedArray? = if (attrs == null) null else context.obtainStyledAttributes(attrs, R.styleable.BaseViewGroup)
        mBorderDivider = BorderDivider.from(context, a)
        if (a != null) {
            mEdgeEffectEnable = a.getBoolean(R.styleable.BaseViewGroup_edgeEffectEnable, mEdgeEffectEnable)
            val floatColor = a.getColor(R.styleable.BaseViewGroup_foregroundColor, 0)
            if (floatColor != 0) {
                val floatRadius = a.getDimensionPixelSize(R.styleable.BaseViewGroup_foregroundRadius, 0)
                val floatDuration = a.getInt(R.styleable.BaseViewGroup_foregroundDuration, 120)
                val floatMinAlpha = a.getInt(R.styleable.BaseViewGroup_foregroundAlphaMin, 0)
                val floatMaxAlpha = a.getInt(R.styleable.BaseViewGroup_foregroundAlphaMax, 50)
                val floatDrawable = FloatDrawable(floatColor, floatMinAlpha, floatMaxAlpha).duration(floatDuration).radius(floatRadius)
                foregroundDrawable = floatDrawable
                isClickable = true
            }
            a.recycle()
        }
        a = if (attrs == null) null else context.obtainStyledAttributes(attrs, ATTRS_PROPERTIES)
        if (a != null) {
            mGravity = a.getInt(0, mGravity)
            mMaxWidth = a.getDimensionPixelSize(1, mMaxWidth)
            mMaxHeight = a.getDimensionPixelSize(2, mMaxHeight)
            mOrientation = a.getInt(3, mOrientation - 1) + 1
            mClipToPadding = a.getBoolean(4, true)
            mWidthPercent = a.getFraction(5, 1, 1, mWidthPercent)
            mHeightPercent = a.getFraction(6, 1, 1, mHeightPercent)
            a.recycle()
        }
    }

    //start:log
    protected val isLogAccess: Boolean
        get() = mLogTag != null

    protected val isDevLogAccess: Boolean
        get() = mLogTag != null && mDevLog

    fun setLogTag(logTag: String, devMode: Boolean) {
        mLogTag = logTag
        mDevLog = devMode
    }

    protected fun print(category: CharSequence, msg: CharSequence) {
        print(category, msg, false)
    }

    internal fun printDev(category: CharSequence, msg: CharSequence) {
        print(category, msg, true)
    }

    private fun print(category: CharSequence?, msg: CharSequence, dev: Boolean) {
        if (isLogAccess) {
            var msg = msg
            var tag = mLogTag + if (dev) "@" else "#"
            if (category != null) {
                tag += category
            }
            Log.d(tag, msg.toString())
        }
    }
    //end:log

    protected fun requestLayoutIfNeed() {
        if (!isLayoutRequested) {
            requestLayout()
        }
    }

    override fun setClipToPadding(clipToPadding: Boolean) {
        if (clipToPadding != mClipToPadding) {
            super.setClipToPadding(clipToPadding)
            mClipToPadding = clipToPadding
            if (isAttachLayoutFinished) {
                invalidate()
            }
        }
    }

    override fun getClipToPadding(): Boolean {
        return mClipToPadding
    }

    /**
     * set content gravity
     *
     * @param gravity
     */
    var gravity: Int
        get() = mGravity
        set(gravity) {
            if (mGravity != gravity) {
                mGravity = gravity
                requestLayoutIfNeed()
            }
        }

    /**
     * set self max width to  measure
     */
    var maxWidth: Int
        get() = mMaxWidth
        set(maxWidth) {
            if (mMaxWidth != maxWidth) {
                mMaxWidth = maxWidth
                requestLayoutIfNeed()
            }
        }

    /**
     * set self max height to  measure
     */
    var maxHeight: Int
        get() = mMaxHeight
        set(maxHeight) {
            if (mMaxHeight != maxHeight) {
                mMaxHeight = maxHeight
                requestLayoutIfNeed()
            }
        }

    /**
     * set layout and gesture direction
     *
     * @param orientation [HORIZONTAL] and [VERTICAL]
     */
    var orientation: Int
        get() = mOrientation
        set(orientation) {
            var orientation = orientation
            orientation = orientation and (HORIZONTAL or VERTICAL)
            if (mOrientation != orientation) {
                val oldOrientation = mOrientation
                mOrientation = orientation
                isAttachLayoutFinished = false
                onOrientationChanged(orientation, oldOrientation)
                requestLayoutIfNeed()
            }
        }

    /**
     * set whether to support scroll when touch move
     *
     * @param touchScrollEnable true to support touch scroll
     */
    open fun setTouchScrollEnable(touchScrollEnable: Boolean) {
        if (mTouchScrollEnable != touchScrollEnable) {
            mTouchScrollEnable = touchScrollEnable
        }
    }

    fun isTouchScrollEnable() = mTouchScrollEnable

    fun isTouchScrollEnable(contentConsidered: Boolean): Boolean {
        return isTouchScrollVerticalEnable(contentConsidered) || isTouchScrollHorizontalEnable(contentConsidered)
    }

    /**
     * set whether to support horizontal scroll when touch
     *
     * @see .setTouchScrollEnable
     * @see .isTouchScrollHorizontalEnable
     */
    fun setTouchScrollHorizontalEnable(canTouchScrollHorizontal: Boolean) {
        if (mCanTouchScrollHorizontal != canTouchScrollHorizontal) {
            mCanTouchScrollHorizontal = canTouchScrollHorizontal
        }
    }

    fun isTouchScrollHorizontalEnable(contentConsidered: Boolean): Boolean {
        val scrollOk = mTouchScrollEnable && mCanTouchScrollHorizontal
        return if (contentConsidered && scrollOk) {
            horizontalScrollRange > 0
        } else scrollOk
    }

    /**
     * set whether to support vertical scroll when touch
     *
     * @see .setTouchScrollEnable
     * @see .isTouchScrollHorizontalEnable
     */
    fun setTouchScrollVerticalEnable(canTouchScrollVertical: Boolean) {
        if (mCanTouchScrollVertical != canTouchScrollVertical) {
            mCanTouchScrollVertical = canTouchScrollVertical
        }
    }

    fun isTouchScrollVerticalEnable(contentConsidered: Boolean): Boolean {
        val scrollOk = mTouchScrollEnable && mCanTouchScrollVertical
        return if (contentConsidered && scrollOk) {
            verticalScrollRange > 0
        } else scrollOk
    }

    /**
     * set content size after measure,so we can decide the final measure dimension
     *
     * @param contentWidth just content width without margin and padding
     * @param contentHeight just content height without margin and padding
     * @param measureState measure state [View.getMeasuredState]
     * @see .dispatchMeasure
     */
    protected fun setContentSize(contentWidth: Int, contentHeight: Int, measureState: Int) {
        this.contentWidth = contentWidth
        this.contentHeight = contentHeight
        this.measureState = this.measureState or measureState
    }

    /**
     * get content width without inset margin
     */
    val contentPureWidth: Int
        get() = contentWidth - (mContentInset.left + mContentInset.right)

    /**
     * get content height without inset margin
     */
    val contentPureHeight: Int
        get() = contentHeight - (mContentInset.top + mContentInset.bottom)

    val paddingLeftWithInset: Int
        get() = paddingLeft + mContentInset.left

    val paddingRightWithInset: Int
        get() = paddingRight + mContentInset.right

    val paddingTopWithInset: Int
        get() = paddingTop + mContentInset.top

    val paddingBottomWithInset: Int
        get() = paddingBottom + mContentInset.bottom

    val widthWithoutPadding: Int
        get() = visibleContentBounds.width()

    val widthWithoutPaddingInset: Int
        get() = visibleContentBounds.width() - mContentInset.left - mContentInset.right

    val heightWithoutPadding: Int
        get() = visibleContentBounds.height()

    val heightWithoutPaddingInset: Int
        get() = visibleContentBounds.height() - mContentInset.top - mContentInset.bottom

    fun setOnItemTouchListener(itemTouchListener: OnItemTouchListener) {
        this.mItemTouchListener = itemTouchListener
    }

    fun setDrawerDecoration(drawerDecoration: DrawerDecoration) {
        if (mDrawerDecoration !== drawerDecoration) {
            mDrawerDecoration = drawerDecoration
            requestLayoutIfNeed()
        }
    }

    fun setForegroundDrawable(color: Int, minAlpha: Int, maxAlpha: Int): FloatDrawable {
        val drawable = FloatDrawable(color, minAlpha, maxAlpha)
        foregroundDrawable = drawable
        return drawable
    }

    fun verifyFlag(value: Int, flag: Int): Boolean {
        return flag == value and flag
    }

    /**
     * set hover drawable [FloatDrawable]
     *
     * @param foregroundDrawable
     */
    var foregroundDrawable: FloatDrawable?
        get() = mForegroundDrawable
        set(foregroundDrawable) {
            if (mForegroundDrawable !== foregroundDrawable) {
                if (mForegroundDrawable != null) {
                    mForegroundDrawable!!.callback = null
                    unscheduleDrawable(mForegroundDrawable)
                }
                if (foregroundDrawable == null) {
                    mForegroundDrawable = null
                } else {
                    mForegroundDrawable = foregroundDrawable
                    foregroundDrawable.callback = this
                    foregroundDrawable.setVisible(visibility == View.VISIBLE, false)
                }
            }
        }

    override fun removeAllViewsInLayout() {
        super.removeAllViewsInLayout()
        isAttachLayoutFinished = false
        mVirtualCount = 0
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachLayoutFinished = false
        mVirtualCount = 0
        if (mBorderDivider != null) {
            mBorderDivider!!.setCallback(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttachLayoutFinished = false
        mVirtualCount = 0
        if (mBorderDivider != null) {
            mBorderDivider!!.mCallback = null
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return if (p is ViewGroup.MarginLayoutParams) {
            LayoutParams(p)
        } else LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    //start:measure&layout&draw
    private fun getMeasureSize(minSize: Int, maxSize: Int, contentSize: Int, padding: Int): Int {
        var finalSize = Math.max(minSize, contentSize + padding)
        if (maxSize > minSize && maxSize > 0 && finalSize > maxSize) {
            finalSize = maxSize
        }
        return finalSize
    }

    private fun getMeasureSizeWithoutPadding(minSize: Int, maxSize: Int, measureSpec: Int, padding: Int): Int {
        var finalSize = View.MeasureSpec.getSize(measureSpec)
        if (minSize > finalSize) {
            finalSize = minSize
        }
        if (maxSize > 0 && finalSize > maxSize && maxSize > minSize) {
            finalSize = maxSize
        }
        return Math.max(finalSize - padding, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        mTimeMeasureStart = System.currentTimeMillis()
        mVirtualCount = 0
        measureState = 0
        contentHeight = measureState
        contentWidth = contentHeight
        if (mWidthPercent > 0) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((mWidthPercent * View.MeasureSpec.getSize(widthMeasureSpec)).toInt(), View.MeasureSpec.EXACTLY)
        }
        if (mHeightPercent > 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((mHeightPercent * View.MeasureSpec.getSize(heightMeasureSpec)).toInt(), View.MeasureSpec.EXACTLY)
        }
        val minWidth = suggestedMinimumWidth
        val minHeight = suggestedMinimumHeight
        val paddingHorizontal = paddingLeft + paddingRight
        val paddingVertical = paddingTop + paddingBottom
        val mostWidthNoPadding = getMeasureSizeWithoutPadding(minWidth, mMaxWidth, widthMeasureSpec, paddingHorizontal)
        val mostHeightNoPadding = getMeasureSizeWithoutPadding(minHeight, mMaxHeight, heightMeasureSpec, paddingVertical)
        mContentInset.setEmpty()
        if (mDrawerDecoration != null) {
            mDrawerDecoration!!.getContentOffsets(mContentInset, this, mostWidthNoPadding, mostHeightNoPadding)
        }
        mBorderDivider!!.applyContentMargin(mContentInset)
        val contentMarginH = mContentInset.left + mContentInset.right
        val contentMarginV = mContentInset.top + mContentInset.bottom
        dispatchMeasure(
                View.MeasureSpec.makeMeasureSpec(Math.max(0, mostWidthNoPadding - contentMarginH), View.MeasureSpec.getMode(widthMeasureSpec)),
                View.MeasureSpec.makeMeasureSpec(Math.max(0, mostHeightNoPadding - contentMarginV), View.MeasureSpec.getMode(heightMeasureSpec))
        )
        val contentWidth = this.contentWidth + contentMarginH
        val contentHeight = this.contentHeight + contentMarginV
        val childState = measureState
        setContentSize(contentWidth, contentHeight, childState)
        val finalWidth = getMeasureSize(minWidth, mMaxWidth, contentWidth, paddingHorizontal)
        val finalHeight = getMeasureSize(minHeight, mMaxHeight, contentHeight, paddingVertical)
        setMeasuredDimension(View.resolveSizeAndState(finalWidth, widthMeasureSpec, childState),
                View.resolveSizeAndState(finalHeight, heightMeasureSpec, childState shl View.MEASURED_HEIGHT_STATE_SHIFT))
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
        computeVisibleBounds(scrollX, scrollY, false, false)
        visibleContentBounds.offset(1, 1)
        doAfterMeasure(measuredWidth, measuredHeight, contentWidth, contentHeight)
        mLastMeasureCost = System.currentTimeMillis() - mTimeMeasureStart
        if (isDevLogAccess) {
            printDev("MLD", String.format("measure cost %d ms: [width=%d,height=%d],[contentW=%d,contentH=%d]", mLastMeasureCost, measuredWidth, measuredHeight, contentWidth, contentHeight))
        }
    }

    private fun updateItemInset(child: View, outRect: Rect, position: Int) {
        if (mDrawerDecoration != null) {
            mDrawerDecoration!!.getItemOffsets(outRect, child, position, this)
        }
    }

    /**
     * tips:do your measure no need to take content margin into account since we have handled.
     * after all child measure must call [.setContentSize];
     *
     * @param widthMeasureSpecContent widthMeasureSpec with out padding and content margin
     * @param heightMeasureSpecContent heightMeasureSpec with out padding and content margin.
     */
    protected abstract fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int)

    /**
     * @param measuredWidth self measure width
     * @param measuredHeight self measure height
     * @param contentWidth real content width and content margin horizontal sum
     * @param contentHeight real content height and content margin vertical sum
     */
    protected open fun doAfterMeasure(measuredWidth: Int, measuredHeight: Int, contentWidth: Int, contentHeight: Int) {}

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mTimeLayoutStart = System.currentTimeMillis()
        var firstAttachLayout = false
        if (!isAttachLayoutFinished) {
            isAttachLayoutFinished = true
            firstAttachLayout = isAttachLayoutFinished
        }
        dispatchLayout(contentLeft + mContentInset.left, contentTop + mContentInset.top)
        if (firstAttachLayout) {
            computeVisibleBounds(scrollX, scrollY, false, true)
        }
        doAfterLayout(firstAttachLayout)
        mLastLayoutCost = System.currentTimeMillis() - mTimeLayoutStart
        if (isDevLogAccess) {
            printDev("MLD", String.format("layout cost %d ms: firstAttachLayout=%s", mLastLayoutCost, firstAttachLayout))
        }
    }

    /**
     * tips:should take content margin into account when layout child.
     *
     * @param baseLeft format content's left no need to consider margin and padding of content.
     * @param baseTop format content's top no need to consider margin and padding of content.
     */
    protected abstract fun dispatchLayout(baseLeft: Int, baseTop: Int)

    protected open fun doAfterLayout(firstAttachLayout: Boolean) {}

    public override fun dispatchDraw(canvas: Canvas) {
        mTimeDrawStart = System.currentTimeMillis()
        doBeforeDraw(canvas, mContentInset)
        super.dispatchDraw(canvas)
        doAfterDraw(canvas, mContentInset)
        mBorderDivider!!.drawBorder(canvas, width, height)
        if (mForegroundDrawable != null) {
            mForegroundDrawable!!.setBounds(0, 0, width, height)
            mForegroundDrawable!!.draw(canvas)
        }
        mLastDrawCost = System.currentTimeMillis() - mTimeDrawStart
        if (isDevLogAccess) {
            printDev("MLD", String.format("draw cost %d ms", mLastDrawCost))
        }
    }

    protected open fun doBeforeDraw(canvas: Canvas, inset: Rect) {}

    protected open fun doAfterDraw(canvas: Canvas, inset: Rect) {}
    //end:measure&layout&draw

    //start: touch gesture
    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        mItemTouchListener?.onRequestDisallowInterceptTouchEvent(disallowIntercept)
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    protected fun dispatchOnItemTouchIntercept(e: MotionEvent): Boolean {
        val action = e.action
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_DOWN) {
            mItemTouchInvoked = false
        }
        if (mItemTouchListener != null) {
            if (mItemTouchListener!!.onInterceptTouchEvent(this, e) && action != MotionEvent.ACTION_CANCEL) {
                mItemTouchInvoked = true
                return true
            }
        }
        return false
    }

    protected fun dispatchOnItemTouch(e: MotionEvent): Boolean {
        val action = e.action
        if (mItemTouchInvoked) {
            if (action == MotionEvent.ACTION_DOWN) {
                mItemTouchInvoked = false
            } else {
                mItemTouchListener?.onTouchEvent(this, e)
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    // Clean up for the next gesture.
                    mItemTouchInvoked = false
                }
                return true
            }
        }
        if (action != MotionEvent.ACTION_DOWN && mItemTouchListener != null) {
            if (mItemTouchListener!!.onInterceptTouchEvent(this, e)) {
                mItemTouchInvoked = true
                return true
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return if (dispatchOnItemTouchIntercept(e)) {
            true
        } else super.onInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (dispatchOnItemTouch(e)) {
            dispatchDrawableTouch(null)
            return true
        }
        dispatchDrawableTouch(e)
        return super.onTouchEvent(e)
    }

    private fun dispatchDrawableTouch(e: MotionEvent?) {
        if (mForegroundDrawable != null && isClickable) {
            if (e == null) {
                mForegroundDrawable!!.start(false)
                return
            }
            val action = e.action
            if (action == MotionEvent.ACTION_DOWN) {
                mForegroundDrawable!!.start(true)
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mForegroundDrawable!!.start(false)
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (!isIgnoreForegroundStateWhenTouchOut && !pointInView(e.x, e.y, mTouchSlop.toFloat())) {
                    mForegroundDrawable!!.start(false)
                }
            }
        }
    }

    protected open fun onOrientationChanged(orientation: Int, oldOrientation: Int) {}

    override fun onScrollChanged(l: Int, t: Int, ol: Int, ot: Int) {
        super.onScrollChanged(l, t, ol, ot)
        computeVisibleBounds(l, t, true, true)
    }

    protected open fun onScrollChanged(scrollX: Int, scrollY: Int, visibleBounds: Rect, fromScrollChanged: Boolean) {

    }

    private  fun computeVisibleBounds(scrollX: Int, scrollY: Int, scrollChanged: Boolean, apply: Boolean) {
        val beforeHash = visibleContentBounds.hashCode()
        var width = if (apply) width else 0
        var height = if (apply) height else 0
        if (width <= 0) width = measuredWidth
        if (height <= 0) height = measuredHeight
        visibleContentBounds.left = paddingLeft + scrollX
        visibleContentBounds.top = paddingTop + scrollY
        visibleContentBounds.right = visibleContentBounds.left + width - paddingLeft - paddingRight
        visibleContentBounds.bottom = visibleContentBounds.top + height - paddingTop - paddingBottom
        if (apply && beforeHash != visibleContentBounds.hashCode()) {
            if (isDevLogAccess) {
                val sb = StringBuilder(32)
                sb.append("scrollX=").append(scrollX)
                sb.append(",scrollY=").append(scrollY).append(",visibleBounds=").append(visibleContentBounds)
                sb.append(",scrollChanged=").append(scrollChanged)
                printDev("scroll", sb)
            }
            onScrollChanged(scrollX, scrollY, visibleContentBounds, scrollChanged)
        }
    }

    private fun pointInView(localX: Float, localY: Float, slop: Float): Boolean {
        return localX >= -slop && localY >= -slop && localX < right - left + slop &&
                localY < bottom - top + slop
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        var result = super.verifyDrawable(who)
        if (!result && mForegroundDrawable === who) {
            result = true
        }
        return result
    }
    //end: touch gesture

    // start: tool function
    protected fun getVirtualChildAt(virtualIndex: Int, withoutGone: Boolean): View? {
        var virtualCount = 0
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, withoutGone)) continue
            if (virtualCount == virtualIndex) {
                return child
            }
            virtualCount++
        }
        return null
    }

    private fun getVirtualChildCount(withoutGone: Boolean): Int {
        var virtualCount = 0
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, withoutGone)) continue
            virtualCount++
        }
        return virtualCount
    }

    val itemViewCount: Int
        get() {
            var itemCount = mVirtualCount
            if (itemCount == 0) {
                mVirtualCount = getVirtualChildCount(true)
                itemCount = mVirtualCount
            }
            return itemCount
        }

    fun getItemView(itemIndex: Int): View? {
        var result: View? = null
        val itemCount = itemViewCount
        if (itemIndex in 0..(itemCount - 1)) {
            result = getVirtualChildAt(itemIndex, true)
        }
        return result
    }

    fun indexOfItemView(view: View?): Int {
        if (view != null) {
            var virtualIndex = 0
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (skipVirtualChild(child, true)) continue
                if (view === child) {
                    return virtualIndex
                }
                virtualIndex++
            }
        }
        return -1
    }

    protected open fun skipChild(child: View?): Boolean {
        return child == null || child.visibility == View.GONE
    }

    protected open fun skipVirtualChild(child: View?, withoutGone: Boolean): Boolean {
        return child == null || withoutGone && child.visibility == View.GONE
    }

    protected val contentLeft: Int
        get() {
            val paddingLeft = paddingLeft
            var result = getContentStartH(paddingLeft, width - paddingRight, contentWidth, mGravity)
            if (result < paddingLeft && isTouchScrollHorizontalEnable(false)) {
                result = paddingLeft
            }
            return result
        }

    protected val contentTop: Int
        get() {
            val paddingTop = paddingTop
            var result = getContentStartV(paddingTop, height - paddingBottom, contentHeight, mGravity)
            if (result < paddingTop && isTouchScrollVerticalEnable(false)) {
                result = paddingTop
            }
            return result
        }

    private   fun getContentStartH(containerLeft: Int, containerRight: Int, contentWillSize: Int, gravity: Int): Int {
        return getContentStartH(containerLeft, containerRight, contentWillSize, 0, 0, gravity)
    }

    private fun getContentStartV(containerTop: Int, containerBottom: Int, contentWillSize: Int, gravity: Int): Int {
        return getContentStartV(containerTop, containerBottom, contentWillSize, 0, 0, gravity)
    }

    protected fun getContentStartH(containerLeft: Int, containerRight: Int, contentWillSize: Int, contentMarginLeft: Int, contentMarginRight: Int, gravity: Int): Int {
        if (gravity != -1 || gravity != 0) {
            val start: Int
            val mask = Gravity.HORIZONTAL_GRAVITY_MASK
            val maskCenter = Gravity.CENTER_HORIZONTAL
            val maskEnd = Gravity.RIGHT
            val okGravity = gravity and mask
            if (maskCenter == okGravity) {//center
                start = containerLeft + (containerRight - containerLeft - (contentWillSize + contentMarginRight - contentMarginLeft)) / 2
            } else if (maskEnd == okGravity) {//end
                start = containerRight - contentWillSize - contentMarginRight
            } else {//start
                start = containerLeft + contentMarginLeft
            }
            return start
        }
        return containerLeft + contentMarginLeft
    }

    protected fun getContentStartV(containerTop: Int, containerBottom: Int, contentWillSize: Int, contentMarginTop: Int, contentMarginBottom: Int, gravity: Int): Int {
        if (gravity != -1 || gravity != 0) {
            val start: Int
            val mask = Gravity.VERTICAL_GRAVITY_MASK
            val maskCenter = Gravity.CENTER_VERTICAL
            val maskEnd = Gravity.BOTTOM
            val okGravity = gravity and mask
            if (maskCenter == okGravity) {//center
                start = containerTop + (containerBottom - containerTop - (contentWillSize + contentMarginBottom - contentMarginTop)) / 2
            } else if (maskEnd == okGravity) {//end
                start = containerBottom - contentWillSize - contentMarginBottom
            } else {//start
                start = containerTop + contentMarginTop
            }
            return start
        }
        return containerTop + contentMarginTop
    }

    /**
     * get offset from first left item
     *
     * @param child
     * @param centreInVisibleBounds if true ,refer to parent view centre to get the offset .
     * @param marginInclude take margin into view space.
     */
    protected fun offsetX(child: View, centreInVisibleBounds: Boolean, marginInclude: Boolean): Int {
        var current: Int
        val marginLp = if (marginInclude) child.layoutParams as ViewGroup.MarginLayoutParams else null
        if (centreInVisibleBounds) {
            current = child.left + child.right shr 1
            if (marginLp != null) {
                current += (marginLp.rightMargin - marginLp.leftMargin) / 2
            }
            return current - visibleContentBounds.centerX() + visibleContentBounds.left - paddingLeft
        } else {
            current = child.left
            if (marginLp != null) {
                current -= marginLp.leftMargin
            }
            return current - paddingLeft
        }
    }

    /**
     * get offset from first top item
     *
     * @param child
     * @param centreInVisibleBounds if true ,refer to parent view centre to get the offset .
     * @param marginInclude take margin into view space.
     */
    protected fun offsetY(child: View, centreInVisibleBounds: Boolean, marginInclude: Boolean): Int {
        var current: Int
        val marginLp = if (marginInclude) child.layoutParams as ViewGroup.MarginLayoutParams else null
        if (centreInVisibleBounds) {
            current = child.top + child.bottom shr 1
            if (marginLp != null) {
                current += (marginLp.bottomMargin - marginLp.topMargin) / 2
            }
            return current - visibleContentBounds.centerY() + visibleContentBounds.top - paddingTop
        } else {
            current = child.top
            if (marginLp != null) {
                current -= marginLp.topMargin
            }
            return current - paddingTop
        }
    }
    // end: tool function


    //start:compute scroll information.

    /**
     * get max scroll range at direction vertical
     */
    protected val verticalScrollRange: Int
        get() {
            var scrollRange = 0
            val contentSize = contentHeight
            if (contentSize > 0) {
                scrollRange = contentSize - visibleContentBounds.height()
                if (scrollRange < 0) {
                    scrollRange = 0
                }
            }
            return scrollRange
        }

    public override fun computeVerticalScrollRange(): Int {
        val count = childCount
        val paddingTop = paddingTop
        val contentHeight = visibleContentBounds.height()
        if (count == 0) {
            return contentHeight
        }
        var scrollRange = paddingTop + contentHeight
        val scrollY = scrollY
        val overScrollBottom = Math.max(0, scrollRange - contentHeight)
        if (scrollY < 0) {
            scrollRange -= scrollY
        } else if (scrollY > overScrollBottom) {
            scrollRange += scrollY - overScrollBottom
        }
        return scrollRange
    }

    public override fun computeVerticalScrollOffset(): Int {
        return Math.max(0, super.computeVerticalScrollOffset())
    }

    public override fun computeHorizontalScrollRange(): Int {
        val count = childCount
        val paddingLeft = paddingLeft
        val contentWidth = visibleContentBounds.width()
        if (count == 0) {
            return contentWidth
        }
        var scrollRange = paddingLeft + contentWidth
        val scrollX = scrollX
        val overScrollRight = Math.max(0, scrollRange - contentWidth)
        if (scrollX < 0) {
            scrollRange -= scrollX
        } else if (scrollX > overScrollRight) {
            scrollRange += scrollX - overScrollRight
        }
        return scrollRange
    }

    /**
     * get max scroll range at direction horizontal
     */
    protected val horizontalScrollRange: Int
        get() {
            var scrollRange = 0
            val contentSize = contentWidth
            if (contentSize > 0) {
                scrollRange = contentSize - visibleContentBounds.width()
                if (scrollRange < 0) {
                    scrollRange = 0
                }
            }
            return scrollRange
        }

    public override fun computeHorizontalScrollOffset(): Int {
        return Math.max(0, super.computeHorizontalScrollOffset())
    }

    val borderDivider: BorderDivider
        get() {
            if (mBorderDivider == null) {
                mBorderDivider = BorderDivider.from(context, null)
            }
            return mBorderDivider as BorderDivider
        }

    /**
     * custom LayoutParams which support layout_gravity,maxWidth and maxHeight in xml attr
     * what's more,it supports inset to resize margin of child view .
     */
    open class LayoutParams : ViewGroup.MarginLayoutParams {
        var gravity = -1
        var maxWidth = -1
        var maxHeight = -1
        private var mPosition = -1
        var mWidthPercent = 0f
        var mHeightPercent = 0f

        private val mInsets = Rect()

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, ATTRS_PARAMS)
            gravity = a.getInt(0, gravity)
            maxWidth = a.getDimensionPixelSize(1, maxWidth)
            maxHeight = a.getDimensionPixelSize(2, maxHeight)
            mWidthPercent = a.getFraction(3, 1, 1, mWidthPercent)
            mHeightPercent = a.getFraction(4, 1, 1, mHeightPercent)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height) {}

        constructor(width: Int, height: Int, gravity: Int) : super(width, height) {
            this.gravity = gravity
        }

        constructor(source: ViewGroup.LayoutParams) : super(source) {}

        constructor(source: ViewGroup.MarginLayoutParams) : super(source) {
            if (source is LayoutParams) {
                gravity = source.gravity
                maxWidth = source.maxWidth
                maxHeight = source.maxHeight
            } else {
                if (source is LinearLayout.LayoutParams) {
                    gravity = source.gravity
                }
                if (source is FrameLayout.LayoutParams) {
                    gravity = source.gravity
                }
            }
        }

        fun position(): Int {
            return mPosition
        }

        /**
         * get view width include its margin and inset width
         */
        fun width(view: View): Int {
            return view.measuredWidth + leftMargin + rightMargin + mInsets.left + mInsets.right
        }

        /**
         * get view height include its margin and inset width
         */
        fun height(view: View): Int {
            return view.measuredHeight + topMargin + bottomMargin + mInsets.top + mInsets.bottom
        }

        fun leftMargin(): Int {
            return leftMargin + mInsets.left
        }

        fun topMargin(): Int {
            return topMargin + mInsets.top
        }

        fun rightMargin(): Int {
            return rightMargin + mInsets.right
        }

        fun bottomMargin(): Int {
            return bottomMargin + mInsets.bottom
        }

        /**
         * measure child from a calculated child MeasureSpec,we recommend to use [.measure]
         * subclass of BaseViewGroup should aways use this measure function to apply extra property such as maxWidth,maxHeight,layout_gravity
         */
        fun measure(child: View, itemPosition: Int, childWidthMeasureSpec: Int, childHeightMeasureSpec: Int) {
            var childWidthMeasureSpec = childWidthMeasureSpec
            var childHeightMeasureSpec = childHeightMeasureSpec
            mPosition = itemPosition
            mInsets.setEmpty()
            if (child.parent is BaseViewGroup) {
                (child.parent as BaseViewGroup).updateItemInset(child, mInsets, mPosition)
            }
            val marginInsetH = mInsets.left + mInsets.right + leftMargin + rightMargin
            val marginInsetV = mInsets.top + mInsets.bottom + topMargin + bottomMargin
            childWidthMeasureSpec = limitMeasureSpec(childWidthMeasureSpec, maxWidth, marginInsetH, width == -1)
            childHeightMeasureSpec = limitMeasureSpec(childHeightMeasureSpec, maxHeight, marginInsetV, height == -1)
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }

        /**
         * measure child from parent MeasureSpec
         * subclass of BaseViewGroup should aways use this measure function to apply extra property such as maxWidth,maxHeight,layout_gravity
         */
        fun measure(view: View, itemPosition: Int, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int, widthUsed: Int, heightUsed: Int) {
            var widthDimension = width
            var heightDimension = height
            if (view !is BaseViewGroup) {
                if (mWidthPercent > 0) {
                    widthDimension = (View.MeasureSpec.getSize(parentWidthMeasureSpec) * mWidthPercent).toInt()
                }
                if (mHeightPercent > 0) {
                    heightDimension = (View.MeasureSpec.getSize(parentHeightMeasureSpec) * mHeightPercent).toInt()
                }
            }
            measure(view, itemPosition, ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, widthUsed, widthDimension), ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, heightUsed, heightDimension))
        }

        private fun limitMeasureSpec(measureSpec: Int, maxSize: Int, used: Int, mostToExactly: Boolean): Int {
            var size = View.MeasureSpec.getSize(measureSpec) - used
            var mode = View.MeasureSpec.getMode(measureSpec)
            if (size < 0) size = 0
            if (maxSize > 0 && size > maxSize) size = maxSize
            if (mostToExactly) {
                if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.UNSPECIFIED) {
                    mode = View.MeasureSpec.EXACTLY
                }
            }
            if (maxSize > 0 && mode == View.MeasureSpec.UNSPECIFIED) {
                mode = View.MeasureSpec.AT_MOST
            }
            return View.MeasureSpec.makeMeasureSpec(if (size < 0) 0 else size, mode)
        }
    }

    /**
     * this interface provided a chance to handle touch event
     */
    interface OnItemTouchListener {

        fun onInterceptTouchEvent(parent: BaseViewGroup, e: MotionEvent): Boolean

        fun onTouchEvent(parent: BaseViewGroup, e: MotionEvent)

        fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean)
    }

    /**
     * this interface give a chance to resize content margin and resize all its children's margin.
     * beyond that it can draw something on the canvas in this canvas coordinate
     */
    abstract class DrawerDecoration {
        fun onDraw(c: Canvas, parent: BaseViewGroup) {}

        fun onDrawOver(c: Canvas, parent: BaseViewGroup) {}

        fun getItemOffsets(outRect: Rect, child: View, itemPosition: Int, parent: BaseViewGroup) {
            outRect.set(0, 0, 0, 0)
        }

        fun getContentOffsets(outRect: Rect, parent: BaseViewGroup, widthNoPadding: Int, heightNoPadding: Int) {
            outRect.set(0, 0, 0, 0)
        }
    }

    companion object {

        /**
         * Horizontal layout gesture direction
         *
         * @see .setOrientation
         */
        val HORIZONTAL = 1
        /**
         * Vertical layout or gesture direction
         *
         * @see .setOrientation
         */
        val VERTICAL = 2

        private val ATTRS_PROPERTIES = intArrayOf(android.R.attr.gravity, android.R.attr.maxWidth, android.R.attr.maxHeight, android.R.attr.orientation, android.R.attr.clipToPadding, R.attr.widthPercent, R.attr.heightPercent)
        private val ATTRS_PARAMS = intArrayOf(android.R.attr.layout_gravity, android.R.attr.maxWidth, android.R.attr.maxHeight, R.attr.widthPercent, R.attr.heightPercent)
    }
}
