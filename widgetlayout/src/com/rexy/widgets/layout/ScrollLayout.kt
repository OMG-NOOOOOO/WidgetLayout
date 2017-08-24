package com.rexy.widgets.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ScrollingView
import android.support.v4.view.VelocityTrackerCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.EdgeEffectCompat
import android.support.v4.widget.ScrollerCompat
import android.util.AttributeSet
import android.view.*
import android.view.animation.Interpolator

/**
 * 支持容器内容自身的gravity,maxWidth,maxHeight.
 * 支持直接子 View 的 layout_gravity,maxWidth,maxHeight 等。
 * 支持水平和垂直视图的滑动计算 Api 和滑动事件。
 * 随时可监听当前视图可见区域的变法。
 * onMeasure 和 onLayout 内部做了一定的通用处理，不可重载，可打印他们执行的结果和耗费时间。
 *
 *
 *
 *
 * 实现子类需要重写dispatchMeasure和dispatchLayout 两个方法。
 * 其中dispatchMeasure来实现child 的测量，最终需要调用setContentSize 方法。
 *
 * @author: rexy
 * @date: 2017-04-25 09:32
 */
open class ScrollLayout : BaseViewGroup, ScrollingView, NestedScrollingChild {

    internal var mScrollListener: OnScrollChangeListener? = null

    var scrollState = SCROLL_STATE_IDLE
        protected set(newState) {
            if (scrollState != newState) {
                val preState = scrollState
                field = newState
                if (isDevLogAccess) {
                    printDev("state", String.format("from %d to %d", preState, newState))
                }
                onScrollStateChanged(newState, preState)
                if (mScrollListener != null) {
                    mScrollListener!!.onScrollStateChanged(scrollState, preState)
                }
            }
        }
    protected var mMinFlingVelocity: Int = 0
    protected var mMaxFlingVelocity: Int = 0
    private var mVelocityTracker: VelocityTracker? = null
    private val mFlingScroller = FlingScroller()
    private var mLastTouchX: Int = 0
    private var mLastTouchY: Int = 0
    private var mInitialTouchX: Int = 0
    private var mInitialTouchY: Int = 0
    private var mScrollPointerId = INVALID_POINTER
    private var mInterpolator: Interpolator? = null
    private val mScrollInfo = Rect()

    private val mCanTouchScroll = BooleanArray(2)
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private val mNestedOffsets = IntArray(2)

    private var mScrollingChildHelper: NestedScrollingChildHelper? = null

    private var mLeftGlow: EdgeEffectCompat? = null
    private var mTopGlow: EdgeEffectCompat? = null
    private var mRightGlow: EdgeEffectCompat? = null
    private var mBottomGlow: EdgeEffectCompat? = null

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
        val vc = ViewConfiguration.get(context)
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        setTouchScrollEnable(isTouchScrollEnable())
    }

    override fun setTouchScrollEnable(touchScrollEnable: Boolean) {
        super.setTouchScrollEnable(touchScrollEnable)
        if (isOrientationHorizontal) {
            setTouchScrollHorizontalEnable(touchScrollEnable)
        }
        if (isOrientationVertical) {
            setTouchScrollVerticalEnable(touchScrollEnable)
        }
    }

    private var isEdgeEffectEnable: Boolean=false
        set(enable) {
            if (field !== enable) {
                field=enable;
                if (!enable) {
                    invalidateGlows()
                }
                invalidate()
            }
        }

    override fun onOrientationChanged(orientation: Int, oldOrientation: Int) {
        setTouchScrollEnable(isTouchScrollEnable(false))
        scrollTo(0, 0)
    }

    override fun setClipToPadding(clipToPadding: Boolean) {
        if (clipToPadding != getClipToPadding()) {
            invalidateGlows()
        }
        super.setClipToPadding(clipToPadding)
    }

    val isOrientationHorizontal: Boolean
        get() = orientationMatch(BaseViewGroup.Companion.HORIZONTAL, false)

    val isOrientationVertical: Boolean
        get() = orientationMatch(BaseViewGroup.Companion.VERTICAL, true)

    fun orientationMatch(orientation: Int, defaultIfUnset: Boolean): Boolean {
        return if (orientation == 0) {
            defaultIfUnset
        } else verifyFlag(mOrientation, orientation)
    }

    fun setOnScrollChangeListener(l: OnScrollChangeListener) {
        mScrollListener = l
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        invalidateGlows()
        mScrollInfo.setEmpty()
        mScrollingChildHelper = null
    }

    override fun removeAllViewsInLayout() {
        super.removeAllViewsInLayout()
        invalidateGlows()
        mScrollInfo.setEmpty()
        mScrollingChildHelper = null
    }

    //start:measure&layout&draw
    override fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int) {
        var widthMeasureSpecContent = widthMeasureSpecContent
        var heightMeasureSpecContent = heightMeasureSpecContent
        val childCount = childCount
        var contentWidth = 0
        var contentHeight = 0
        var childState = 0
        var itemPosition = 0
        val itemMargin: Int
        if (isOrientationHorizontal) {
            itemMargin = mBorderDivider!!.contentMarginHorizontal
            widthMeasureSpecContent = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(widthMeasureSpecContent), View.MeasureSpec.UNSPECIFIED)
            for (i in 0..childCount - 1) {
                val child = getChildAt(i)
                if (skipChild(child)) continue
                if (itemPosition != 0) contentWidth += itemMargin
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                params.measure(child, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, 0, 0)
                contentWidth += params.width(child)
                val itemHeight = params.height(child)
                if (contentHeight < itemHeight) {
                    contentHeight = itemHeight
                }
                childState = childState or child.measuredState
            }
        } else {
            itemMargin = mBorderDivider!!.contentMarginVertical
            heightMeasureSpecContent = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpecContent), View.MeasureSpec.UNSPECIFIED)
            for (i in 0..childCount - 1) {
                val child = getChildAt(i)
                if (skipChild(child)) continue
                if (itemPosition != 0) contentHeight += itemMargin
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                params.measure(child, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, 0, 0)
                contentHeight += params.height(child)
                val itemWidth = params.width(child)
                if (contentWidth < itemWidth) {
                    contentWidth = itemWidth
                }
                childState = childState or child.measuredState
            }
        }
        setContentSize(contentWidth, contentHeight, childState)
    }

    override fun dispatchLayout(contentLeft: Int, contentTop: Int) {
        val count = childCount
        var childLeft = contentLeft
        var childTop = contentTop
        var childRight: Int
        var childBottom: Int
        val itemMargin: Int
        if (isOrientationHorizontal) {
            itemMargin = mBorderDivider!!.contentMarginHorizontal
            val baseBottom = contentTop + contentPureHeight
            for (i in 0..count - 1) {
                val child = getChildAt(i)
                if (skipChild(child)) continue
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                childTop = getContentStartH(contentTop, baseBottom, child.measuredHeight, params.topMargin(), params.bottomMargin(), params.gravity)
                childBottom = childTop + child.measuredHeight
                childLeft += params.leftMargin()
                childRight = childLeft + child.measuredWidth
                child.layout(childLeft, childTop, childRight, childBottom)
                childLeft = childRight + params.rightMargin + itemMargin
            }
        } else {
            itemMargin = mBorderDivider!!.contentMarginVertical
            val baseRight = contentLeft + contentPureWidth
            for (i in 0..count - 1) {
                val child = getChildAt(i)
                if (skipChild(child)) continue
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                childTop += params.topMargin()
                childBottom = childTop + child.measuredHeight
                childLeft = getContentStartH(contentLeft, baseRight, child.measuredWidth, params.leftMargin(), params.rightMargin(), params.gravity)
                childRight = childLeft + child.measuredWidth
                child.layout(childLeft, childTop, childRight, childBottom)
                childTop = childBottom + params.bottomMargin() + itemMargin
            }
        }
    }

    override fun doAfterLayout(firstAttachLayout: Boolean) {
        if (0x80000000.toInt() == 0x80000000.toInt() and mScrollInfo.left) {
            scrollToItem(mScrollInfo, true)
        }
    }

    override fun doAfterDraw(c: Canvas, inset: Rect) {
        if (isEdgeEffectEnable) {
            // TODO If padding is not 0 and clipChildrenToPadding is false, to draw glows properly, we
            // need find children closest to edges. Not sure if it is worth the effort.
            val clipToPadding = clipToPadding
            var needsInvalidate = false
            if (mLeftGlow != null && !mLeftGlow!!.isFinished) {
                val restore = c.save()
                val padding = if (clipToPadding) paddingBottom else 0
                c.rotate(270f)
                c.translate((-height + padding).toFloat(), 0f)
                needsInvalidate = mLeftGlow != null && mLeftGlow!!.draw(c)
                c.restoreToCount(restore)
            }
            if (mTopGlow != null && !mTopGlow!!.isFinished) {
                val restore = c.save()
                if (clipToPadding) {
                    c.translate(paddingLeft.toFloat(), paddingTop.toFloat())
                }
                needsInvalidate = needsInvalidate or (mTopGlow != null && mTopGlow!!.draw(c))
                c.restoreToCount(restore)
            }
            if (mRightGlow != null && !mRightGlow!!.isFinished) {
                val restore = c.save()
                val width = width
                val padding = if (clipToPadding) paddingTop else 0
                c.rotate(90f)
                c.translate((-padding).toFloat(), (-width - scrollX).toFloat())
                needsInvalidate = needsInvalidate or (mRightGlow != null && mRightGlow!!.draw(c))
                c.restoreToCount(restore)
            }
            if (mBottomGlow != null && !mBottomGlow!!.isFinished) {
                val restore = c.save()
                c.rotate(180f)
                if (clipToPadding) {
                    c.translate((-width + paddingRight).toFloat(), (-height + paddingBottom - scrollY).toFloat())
                } else {
                    c.translate((-width).toFloat(), (-height).toFloat())
                }
                needsInvalidate = needsInvalidate or (mBottomGlow != null && mBottomGlow!!.draw(c))
                c.restoreToCount(restore)
            }
            if (needsInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }
    //end:measure&layout&draw

    //start: touch gesture

    protected open fun ignoreSelfTouch(fromIntercept: Boolean, e: MotionEvent): Boolean {
        return !isTouchScrollEnable(false)
    }

    protected fun ignoreSelfFling(velocityX: Int, velocityY: Int): Boolean {
        return (velocityX == 0 || !isTouchScrollHorizontalEnable(false)) && (velocityY == 0 || !isTouchScrollVerticalEnable(false))
    }

    protected fun willDragging(lastMoved: Int, canScrollContent: Boolean, horizontal: Boolean): Boolean {
        return canScrollContent && Math.abs(lastMoved) > mTouchSlop
    }

    protected fun willScrollHorizontal(dx: Int): Int {
        val scroll = scrollX
        val willScroll = Math.min(Math.max(scroll + dx, 0), horizontalScrollRange)
        return willScroll - scroll
    }

    protected fun willScrollVertical(dy: Int): Int {
        val scroll = scrollY
        val willScroll = Math.min(Math.max(scroll + dy, 0), verticalScrollRange)
        return willScroll - scroll
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (dispatchOnItemTouchIntercept(e)) {
            cancelTouch(true)
            return true
        }
        if (ignoreSelfTouch(true, e)) {
            cancelTouch(true)
            return super.onInterceptTouchEvent(e)
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(e)
        val action = MotionEventCompat.getActionMasked(e)
        val actionIndex = MotionEventCompat.getActionIndex(e)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mCanTouchScroll[0] = isTouchScrollHorizontalEnable(true)
                mCanTouchScroll[1] = isTouchScrollVerticalEnable(true)
                mScrollPointerId = e.getPointerId(0)
                mLastTouchX = (e.x + 0.5f).toInt()
                mInitialTouchX = mLastTouchX
                mLastTouchY = (e.y + 0.5f).toInt()
                mInitialTouchY = mLastTouchY
                if (scrollState == SCROLL_STATE_SETTLING) {
                    mFlingScroller.stop()
                    parent.requestDisallowInterceptTouchEvent(true)
                    scrollState = SCROLL_STATE_DRAGGING
                }
                mNestedOffsets[1] = 0
                mNestedOffsets[0] = mNestedOffsets[1]
                var nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE
                if (mCanTouchScroll[0]) {
                    nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_HORIZONTAL
                }
                if (mCanTouchScroll[1]) {
                    nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_VERTICAL
                }
                startNestedScroll(nestedScrollAxis)
            }
            MotionEventCompat.ACTION_POINTER_DOWN -> {
                mScrollPointerId = e.getPointerId(actionIndex)
                mLastTouchX = (e.getX(actionIndex) + 0.5f).toInt()
                mInitialTouchX = mLastTouchX
                mLastTouchY = (e.getY(actionIndex) + 0.5f).toInt()
                mInitialTouchY = mLastTouchY
            }
            MotionEvent.ACTION_MOVE -> {
                val index = e.findPointerIndex(mScrollPointerId)
                if (index < 0) {
                    if (isLogAccess) {
                        print("error", "processing scroll; pointer index for id $mScrollPointerId not found. Did any MotionEvents get skipped?")
                    }
                    return false
                }
                val x = (e.getX(index) + 0.5f).toInt()
                val y = (e.getY(index) + 0.5f).toInt()
                if (scrollState != SCROLL_STATE_DRAGGING) {
                    val dx = x - mInitialTouchX
                    val dy = y - mInitialTouchY
                    var startScroll = false
                    if (willDragging(dx, mCanTouchScroll[0], true)) {
                        mLastTouchX = mInitialTouchX + mTouchSlop * if (dx < 0) -1 else 1
                        startScroll = true
                    }
                    if (willDragging(dy, mCanTouchScroll[1], false)) {
                        mLastTouchY = mInitialTouchY + mTouchSlop * if (dy < 0) -1 else 1
                        startScroll = true
                    }
                    if (startScroll) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        scrollState = SCROLL_STATE_DRAGGING
                    }
                }
            }
            MotionEventCompat.ACTION_POINTER_UP -> {
                onPointerUp(e)
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker!!.clear()
                stopNestedScroll()
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelTouch(true)
            }
        }
        return scrollState == SCROLL_STATE_DRAGGING
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (dispatchOnItemTouch(e)) {
            cancelTouch(true)
            return true
        }
        if (ignoreSelfTouch(false, e)) {
            cancelTouch(true)
            return super.onTouchEvent(e)
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        var eventAddedToVelocityTracker = false
        val vtev = MotionEvent.obtain(e)
        val action = MotionEventCompat.getActionMasked(e)
        val actionIndex = MotionEventCompat.getActionIndex(e)
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[1] = 0
            mNestedOffsets[0] = mNestedOffsets[1]
            mCanTouchScroll[0] = isTouchScrollHorizontalEnable(true)
            mCanTouchScroll[1] = isTouchScrollVerticalEnable(true)
            mScrollPointerId = e.getPointerId(0)
            mLastTouchX = (e.x + 0.5f).toInt()
            mInitialTouchX = mLastTouchX
            mLastTouchY = (e.y + 0.5f).toInt()
            mInitialTouchY = mLastTouchY
            var nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE
            if (mCanTouchScroll[0]) {
                nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_HORIZONTAL
            }
            if (mCanTouchScroll[1]) {
                nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_VERTICAL
            }
            startNestedScroll(nestedScrollAxis)
        }
        vtev.offsetLocation(mNestedOffsets[0].toFloat(), mNestedOffsets[1].toFloat())
        when (action) {
            MotionEventCompat.ACTION_POINTER_DOWN -> {
                mScrollPointerId = e.getPointerId(actionIndex)
                mLastTouchX = (e.getX(actionIndex) + 0.5f).toInt()
                mInitialTouchX = mLastTouchX
                mLastTouchY = (e.getY(actionIndex) + 0.5f).toInt()
                mInitialTouchY = mLastTouchY
            }
            MotionEvent.ACTION_MOVE -> {
                val index = e.findPointerIndex(mScrollPointerId)
                if (index < 0) {
                    if (isLogAccess) {
                        print("error", "processing scroll; pointer index for id $mScrollPointerId not found. Did any MotionEvents get skipped?")
                    }
                    return false
                }
                val x = (e.getX(index) + 0.5f).toInt()
                val y = (e.getY(index) + 0.5f).toInt()
                var dx = mLastTouchX - x
                var dy = mLastTouchY - y
                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                    dx -= mScrollConsumed[0]
                    dy -= mScrollConsumed[1]
                    vtev.offsetLocation(mScrollOffset[0].toFloat(), mScrollOffset[1].toFloat())
                    mNestedOffsets[0] += mScrollOffset[0]
                    mNestedOffsets[1] += mScrollOffset[1]
                }
                if (scrollState != SCROLL_STATE_DRAGGING) {
                    var startScroll = false
                    if (willDragging(dx, mCanTouchScroll[0], true)) {
                        if (dx > 0) {
                            dx -= mTouchSlop
                        } else {
                            dx += mTouchSlop
                        }
                        startScroll = true
                    }
                    if (willDragging(dy, mCanTouchScroll[1], false)) {
                        if (dy > 0) {
                            dy -= mTouchSlop
                        } else {
                            dy += mTouchSlop
                        }
                        startScroll = true
                    }
                    if (startScroll) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        scrollState = SCROLL_STATE_DRAGGING
                    }
                }
                if (scrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x - mScrollOffset[0]
                    mLastTouchY = y - mScrollOffset[1]
                    if (scrollByInternal(
                            if (mCanTouchScroll[0]) dx else 0,
                            if (mCanTouchScroll[1]) dy else 0,
                            vtev)) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
            MotionEventCompat.ACTION_POINTER_UP -> {
                onPointerUp(e)
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker!!.addMovement(vtev)
                eventAddedToVelocityTracker = true
                mVelocityTracker!!.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
                val xvel :Float= if (mCanTouchScroll[0])
                    -VelocityTrackerCompat.getXVelocity(mVelocityTracker, mScrollPointerId)
                else
                    0f
                val yvel :Float= if (mCanTouchScroll[1])
                    -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId)
                else
                    0f
                if (!fling(mLastTouchX - mInitialTouchX, mLastTouchY - mInitialTouchY, xvel.toInt(), yvel.toInt())) {
                    scrollState = SCROLL_STATE_IDLE
                }
                resetTouch()
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelTouch(true)
            }
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker!!.addMovement(vtev)
        }
        vtev.recycle()
        return true
    }

    private fun onPointerUp(e: MotionEvent) {
        val actionIndex = MotionEventCompat.getActionIndex(e)
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            val newIndex = if (actionIndex == 0) 1 else 0
            mScrollPointerId = e.getPointerId(newIndex)
            mLastTouchX = (e.getX(newIndex) + 0.5f).toInt()
            mInitialTouchX = mLastTouchX
            mLastTouchY = (e.getY(newIndex) + 0.5f).toInt()
            mInitialTouchY = mLastTouchY
        }
    }

    protected open fun cancelTouch(resetToIdle: Boolean) {
        resetTouch()
        if (resetToIdle) {
            scrollState = SCROLL_STATE_IDLE
        }
    }

    private fun resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.clear()
        }
        stopNestedScroll()
        releaseGlows()
    }

    protected open fun formatDuration(duration: Int): Int {
        return Math.max(0, Math.min(duration, MAX_SCROLL_DURATION))
    }

    protected val defaultInterpolator: Interpolator
        get() = DefaultInterpolator

    protected fun awakenScrollBarsIfNeed(): Boolean {
        var awaken = false
        val horizontal = isHorizontalScrollBarEnabled && isTouchScrollHorizontalEnable(true)
        val vertical = isVerticalScrollBarEnabled && isTouchScrollVerticalEnable(true)
        if (horizontal || vertical) {
            awaken = awakenScrollBars()
        }
        return awaken
    }

    /**
     * Does not perform bounds checking. Used by internal methods that have already validated input.
     * It also reports any unused scroll request to the related EdgeEffect.
     *
     * @param x  The amount of horizontal scroll request
     * @param y  The amount of vertical scroll request
     * @param ev The originating MotionEvent, or null if not from a touch event.
     * @return Whether any scroll was consumed in either direction.
     */
    internal fun scrollByInternal(x: Int, y: Int, ev: MotionEvent?): Boolean {
        var unconsumedX = 0
        var unconsumedY = 0
        var consumedX = 0
        var consumedY = 0
        if (x != 0) {
            consumedX = willScrollHorizontal(x)
            unconsumedX = x - consumedX
        }
        if (y != 0) {
            consumedY = willScrollVertical(y)
            unconsumedY = y - consumedY
        }
        if (consumedX != 0 || consumedY != 0) {
            scrollBy(consumedX, consumedY)
        }
        if (dispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, mScrollOffset)) {
            mLastTouchX -= mScrollOffset[0]
            mLastTouchY -= mScrollOffset[1]
            ev?.offsetLocation(mScrollOffset[0].toFloat(), mScrollOffset[1].toFloat())
            mNestedOffsets[0] += mScrollOffset[0]
            mNestedOffsets[1] += mScrollOffset[1]
        } else if (overScrollMode != View.OVER_SCROLL_NEVER) {
            if (ev != null) {
                pullGlows(ev.x, unconsumedX.toFloat(), ev.y, unconsumedY.toFloat())
            }
            considerReleasingGlowsOnScroll(x, y)
        }
        awakenScrollBarsIfNeed()
        return consumedX != 0 || consumedY != 0
    }

    fun fling(velocityX: Int, velocityY: Int): Boolean {
        return fling(0, 0, velocityX, velocityY)
    }

    /**
     * Begin a standard fling with an initial velocity along each axis in pixels per second.
     * If the velocity given is below the system-defined minimum this method will return false
     * and no fling will occur.
     *
     * @param velocityX Initial horizontal velocity in pixels per second
     * @param velocityY Initial vertical velocity in pixels per second
     * @return true if the fling was started, false if the velocity was too low to fling or
     */
    protected open fun fling(movedX: Int, movedY: Int, velocityX: Int, velocityY: Int): Boolean {
        var velocityX = velocityX
        var velocityY = velocityY
        if (!ignoreSelfFling(velocityX, velocityY)) {
            val canScrollHorizontal = isTouchScrollHorizontalEnable(true)
            val canScrollVertical = isTouchScrollVerticalEnable(true)
            if (!canScrollHorizontal || Math.abs(velocityX) < mMinFlingVelocity) {
                velocityX = 0
            }
            if (!canScrollVertical || Math.abs(velocityY) < mMinFlingVelocity) {
                velocityY = 0
            }
            if ((velocityX != 0 || velocityY != 0) && !dispatchNestedPreFling(velocityX.toFloat(), velocityY.toFloat())) {
                val canScroll = canScrollHorizontal || canScrollVertical
                dispatchNestedFling(velocityX.toFloat(), velocityY.toFloat(), canScroll)
                if (canScroll) {
                    velocityX = Math.max(-mMaxFlingVelocity, Math.min(velocityX, mMaxFlingVelocity))
                    velocityY = Math.max(-mMaxFlingVelocity, Math.min(velocityY, mMaxFlingVelocity))
                    if (isDevLogAccess) {
                        printDev("fling", String.format("velocityX=%d,velocityY=%d,scrollX,scrollY=%d,rangeX=%d,rangeY=%d", velocityX, velocityY, scrollX, scrollY, horizontalScrollRange, verticalScrollRange))
                    }
                    mFlingScroller.fling(velocityX, velocityY)
                    return true
                }
            }
        }
        return false
    }

    override fun onScrollChanged(l: Int, t: Int, ol: Int, ot: Int) {
        super.onScrollChanged(l, t, ol, ot)
        if (mScrollListener != null) {
            mScrollListener!!.onScrollChanged(l, t, ol, ot)
        }
    }

    protected open fun onScrollStateChanged(newState: Int, prevState: Int) {}

    open fun scrollTo(x: Int, y: Int, duration: Int) {
        scrollTo(x, y, duration, defaultInterpolator)
    }

    fun scrollTo(x: Int, y: Int, duration: Int, interpolator: Interpolator?) {
        var x = x
        var y = y
        if (isAttachLayoutFinished) {
            mScrollInfo.left = 0 shl 31
            x = Math.max(0, Math.min(horizontalScrollRange, x))
            y = Math.max(0, Math.min(verticalScrollRange, y))
            val dx = x - scrollX
            val dy = y - scrollY
            if (dx != 0 || dy != 0) {
                if (duration >= 0) {
                    mFlingScroller.smoothScrollBy(dx, dy, if (duration < 0) 0 else duration, interpolator)
                } else {
                    mFlingScroller.smoothScrollBy(dx, dy, interpolator)
                }
            }
        } else {
            mScrollInfo.left = 1 shl 31 or ((if (duration < 0) 1 else 0) shl 30) or ((if (duration < 0) -duration else duration) and 0x3FFFFFFF)
            mScrollInfo.top = 0
            mScrollInfo.right = 1 shl 31 or ((if (x < 0) 1 else 0) shl 30) or ((if (x < 0) -x else x) and 0x3FFFFFFF)
            mScrollInfo.bottom = 1 shl 31 or ((if (y < 0) 1 else 0) shl 30) or ((if (y < 0) -y else y) and 0x3FFFFFFF)
        }
        mInterpolator = interpolator
    }

    fun scrollToItem(index: Int, duration: Int, centerInParent: Boolean) {
        scrollToItem(index, duration, 0, 0, centerInParent)
    }

    protected fun scrollToItem(index: Int, duration: Int, x: Int, y: Int, centerInParent: Boolean) {
        scrollToItem(index, duration, x, y, isTouchScrollHorizontalEnable(false), isTouchScrollVerticalEnable(false), centerInParent)
    }

    protected fun scrollToItem(index: Int, duration: Int, x: Int, y: Int, okX: Boolean, okY: Boolean, centerInParent: Boolean) {
        mScrollInfo.left = 1 shl 31 or ((if (duration < 0) 1 else 0) shl 30) or ((if (duration < 0) -duration else duration) and 0x3FFFFFFF)
        mScrollInfo.top = (if (centerInParent) 1 else 0) shl 31 or ((if (index < 0) 1 else 0) shl 30) or ((if (index < 0) -index else index) and 0x3FFFFFFF)
        mScrollInfo.right = (if (okX) 1 else 0) shl 31 or ((if (x < 0) 1 else 0) shl 30) or ((if (x < 0) -x else x) and 0x3FFFFFFF)
        mScrollInfo.bottom = (if (okY) 1 else 0) shl 31 or ((if (y < 0) 1 else 0) shl 30) or ((if (y < 0) -y else y) and 0x3FFFFFFF)
        if (isAttachLayoutFinished) {
            val child = if (index >= 0) getItemView(index) else null
            if (!(child != null && child.isLayoutRequested && isLayoutRequested)) {
                scrollToItem(mScrollInfo, false)
            }
        }
    }

    private fun scrollToItem(scrollInfo: Rect, afterLayout: Boolean) {
        if (0x80000000.toInt() == 0x80000000.toInt() and mScrollInfo.left) {
            val duration = (mScrollInfo.left and 0x3FFFFFFF) * if (0x40000000 == 0x40000000 and mScrollInfo.left) -1 else 1
            val index = (mScrollInfo.top and 0x3FFFFFFF) * if (0x40000000 == 0x40000000 and mScrollInfo.top) -1 else 1
            val offsetX = (mScrollInfo.right and 0x3FFFFFFF) * if (0x40000000 == 0x40000000 and mScrollInfo.right) -1 else 1
            val offsetY = (mScrollInfo.bottom and 0x3FFFFFFF) * if (0x40000000 == 0x40000000 and mScrollInfo.bottom) -1 else 1
            if (index < 0) {
                scrollTo(offsetX, offsetY, duration, mInterpolator)
            } else {
                val child = getItemView(index)
                if (child != null) {
                    var x = scrollX
                    var y = scrollY
                    val centerInParent = 0x80000000.toInt() == 0x80000000.toInt() and mScrollInfo.top
                    if (0x80000000.toInt() == 0x80000000.toInt() and mScrollInfo.right) {
                        x = offsetX(child, centerInParent, true) + offsetX
                    }
                    if (0x80000000.toInt() == 0x80000000.toInt() and mScrollInfo.bottom) {
                        y = offsetY(child, centerInParent, true) + offsetY
                    }
                    scrollTo(x, y, duration, mInterpolator)
                }
            }
        }
        mScrollInfo.left = 0 shl 31
    }

    override fun computeHorizontalScrollExtent(): Int {
        return super.computeHorizontalScrollExtent()
    }

    override fun computeVerticalScrollExtent(): Int {
        return super.computeVerticalScrollExtent()
    }
    //end:compute scroll information.

    //start:EdgeEffect
    private fun ensureLeftGlow() {
        if (mLeftGlow == null) {
            mLeftGlow = EdgeEffectCompat(context)
            if (clipToPadding) {
                mLeftGlow!!.setSize(heightWithoutPadding, widthWithoutPadding)
            } else {
                mLeftGlow!!.setSize(measuredHeight, measuredWidth)
            }
        }
    }

    private fun ensureRightGlow() {
        if (mRightGlow == null) {
            mRightGlow = EdgeEffectCompat(context)
            if (clipToPadding) {
                mRightGlow!!.setSize(heightWithoutPadding, widthWithoutPadding)
            } else {
                mRightGlow!!.setSize(measuredHeight, measuredWidth)
            }
        }
    }

    private fun ensureTopGlow() {
        if (mTopGlow == null) {
            mTopGlow = EdgeEffectCompat(context)
            if (clipToPadding) {
                mTopGlow!!.setSize(widthWithoutPadding, heightWithoutPadding)
            } else {
                mTopGlow!!.setSize(measuredWidth, measuredHeight)
            }
        }
    }

    private fun ensureBottomGlow() {
        if (mBottomGlow == null) {
            mBottomGlow = EdgeEffectCompat(context)
            if (clipToPadding) {
                mBottomGlow!!.setSize(widthWithoutPadding, heightWithoutPadding)
            } else {
                mBottomGlow!!.setSize(measuredWidth, measuredHeight)
            }
        }
    }

    protected fun invalidateGlows() {
        mBottomGlow = null
        mTopGlow = mBottomGlow
        mRightGlow = mTopGlow
        mLeftGlow = mRightGlow
    }

    protected fun pullGlows(x: Float, overscrollX: Float, y: Float, overscrollY: Float) {
        if (isEdgeEffectEnable) {
            var invalidate = false
            if (overscrollX < 0) {
                ensureLeftGlow()
                if (mLeftGlow!!.onPull(-overscrollX / width, 1f - y / height)) {
                    invalidate = true
                }
            } else if (overscrollX > 0) {
                ensureRightGlow()
                if (mRightGlow!!.onPull(overscrollX / width, y / height)) {
                    invalidate = true
                }
            }
            if (overscrollY < 0) {
                ensureTopGlow()
                if (mTopGlow!!.onPull(-overscrollY / height, x / width)) {
                    invalidate = true
                }
            } else if (overscrollY > 0) {
                ensureBottomGlow()
                if (mBottomGlow!!.onPull(overscrollY / height, 1f - x / width)) {
                    invalidate = true
                }
            }
            if (invalidate || overscrollX != 0f || overscrollY != 0f) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    protected fun considerReleasingGlowsOnScroll(dx: Int, dy: Int) {
        if (isEdgeEffectEnable) {
            var needsInvalidate = false
            if (mLeftGlow != null && !mLeftGlow!!.isFinished && dx > 0) {
                needsInvalidate = mLeftGlow!!.onRelease()
            }
            if (mRightGlow != null && !mRightGlow!!.isFinished && dx < 0) {
                needsInvalidate = needsInvalidate or mRightGlow!!.onRelease()
            }
            if (mTopGlow != null && !mTopGlow!!.isFinished && dy > 0) {
                needsInvalidate = needsInvalidate or mTopGlow!!.onRelease()
            }
            if (mBottomGlow != null && !mBottomGlow!!.isFinished && dy < 0) {
                needsInvalidate = needsInvalidate or mBottomGlow!!.onRelease()
            }
            if (needsInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    protected fun releaseGlows() {
        if (isEdgeEffectEnable) {
            var needsInvalidate = false
            if (mLeftGlow != null) needsInvalidate = mLeftGlow!!.onRelease()
            if (mTopGlow != null) needsInvalidate = needsInvalidate or mTopGlow!!.onRelease()
            if (mRightGlow != null) needsInvalidate = needsInvalidate or mRightGlow!!.onRelease()
            if (mBottomGlow != null) needsInvalidate = needsInvalidate or mBottomGlow!!.onRelease()
            if (needsInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    protected fun absorbGlows(velocityX: Int, velocityY: Int) {
        if (isEdgeEffectEnable) {
            if (velocityX < 0) {
                ensureLeftGlow()
                mLeftGlow!!.onAbsorb(-velocityX)
            } else if (velocityX > 0) {
                ensureRightGlow()
                mRightGlow!!.onAbsorb(velocityX)
            }
            if (velocityY < 0) {
                ensureTopGlow()
                mTopGlow!!.onAbsorb(-velocityY)
            } else if (velocityY > 0) {
                ensureBottomGlow()
                mBottomGlow!!.onAbsorb(velocityY)
            }
            if (velocityX != 0 || velocityY != 0) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }
    //end:EdgeEffect

    //start:NestedScrollingChild
    private val scrollingChildHelper: NestedScrollingChildHelper
        get() {
            if (mScrollingChildHelper == null) {
                mScrollingChildHelper = NestedScrollingChildHelper(this)
            }
            return mScrollingChildHelper as NestedScrollingChildHelper
        }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        scrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return scrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return scrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        scrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return scrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
                                      dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return scrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return scrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return scrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return scrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    internal inner class FlingScroller : Runnable {
        private var mLastFlingX: Int = 0
        private var mLastFlingY: Int = 0
        private var mScroller: ScrollerCompat? = null
        var mInterpolator: Interpolator? = defaultInterpolator
        // When set to true, postOnAnimation callbacks are delayed until the run method completes
        private var mEatRunOnAnimationRequest = false
        // Tracks if postAnimationCallback should be re-attached when it is done
        private var mReSchedulePostAnimationCallback = false

        init {
            mScroller = ScrollerCompat.create(context, defaultInterpolator)
        }

        override fun run() {
            disableRunOnAnimationRequests()
            // keep a local reference so that if it is changed during onAnimation method, it won't
            // cause unexpected behaviors
            val scroller = mScroller
            if (scroller!!.computeScrollOffset()) {
                val x = scroller.currX
                val y = scroller.currY
                val dx = x - mLastFlingX
                val dy = y - mLastFlingY
                var hresult = 0
                var vresult = 0
                mLastFlingX = x
                mLastFlingY = y
                var overscrollX = 0
                var overscrollY = 0
                if (dx != 0) {
                    hresult = willScrollHorizontal(dx)
                    overscrollX = dx - hresult
                }
                if (dy != 0) {
                    vresult = willScrollVertical(dy)
                    overscrollY = dy - vresult
                }
                if (hresult != 0 || vresult != 0) {
                    scrollBy(hresult, vresult)
                }
                if (overScrollMode != View.OVER_SCROLL_NEVER) {
                    considerReleasingGlowsOnScroll(dx, dy)
                }
                if (overscrollX != 0 || overscrollY != 0) {
                    val vel = scroller.currVelocity.toInt()
                    var velX = 0
                    if (overscrollX != x) {
                        velX = if (overscrollX < 0) -vel else if (overscrollX > 0) vel else 0
                    }
                    var velY = 0
                    if (overscrollY != y) {
                        velY = if (overscrollY < 0) -vel else if (overscrollY > 0) vel else 0
                    }
                    if (overScrollMode != View.OVER_SCROLL_NEVER) {
                        absorbGlows(velX, velY)
                    }
                    if ((velX != 0 || overscrollX == x || scroller.finalX == 0) && (velY != 0 || overscrollY == y || scroller.finalY == 0)) {
                        scroller.abortAnimation()
                    }
                }
                awakenScrollBarsIfNeed()
                val fullyConsumedVertical = dy != 0 && isTouchScrollVerticalEnable(true)
                        && vresult == dy
                val fullyConsumedHorizontal = dx != 0 && isTouchScrollHorizontalEnable(true)
                        && hresult == dx
                val fullyConsumedAny = dx == 0 && dy == 0 || fullyConsumedHorizontal
                        || fullyConsumedVertical
                if (scroller.isFinished || !fullyConsumedAny) {
                    if (isDevLogAccess) {
                        printDev("fling", "scroller finished or full consumed")
                    }
                    scrollState = SCROLL_STATE_IDLE // setting state to idle will stop this.
                } else {
                    postOnAnimation()
                }
            }
            enableRunOnAnimationRequests()
        }

        private fun disableRunOnAnimationRequests() {
            mReSchedulePostAnimationCallback = false
            mEatRunOnAnimationRequest = true
        }

        private fun enableRunOnAnimationRequests() {
            mEatRunOnAnimationRequest = false
            if (mReSchedulePostAnimationCallback) {
                postOnAnimation()
            }
        }

        fun postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true
            } else {
                removeCallbacks(this)
                ViewCompat.postOnAnimation(this@ScrollLayout, this)
            }
        }

        fun fling(velocityX: Int, velocityY: Int) {
            scrollState = SCROLL_STATE_SETTLING
            mLastFlingY = 0
            mLastFlingX = mLastFlingY
            mScroller!!.fling(0, 0, velocityX, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
            postOnAnimation()
        }

        fun fling(velocityX: Int, velocityY: Int, interpolator: Interpolator) {
            if (mInterpolator !== interpolator) {
                mInterpolator = interpolator
                mScroller = ScrollerCompat.create(context, interpolator)
            }
            fling(velocityX, velocityY)
        }


        fun smoothScrollBy(dx: Int, dy: Int, vx: Int = 0, vy: Int = 0) {
            smoothScrollBy(dx, dy, computeScrollDuration(dx, dy, vx, vy),defaultInterpolator)
        }

        private fun distanceInfluenceForSnapDuration(f: Float): Float {
            var f = f
            f -= 0.5f // center the values about 0.
            f *= (0.3f * Math.PI / 2.0f).toFloat()
            return Math.sin(f.toDouble()).toFloat()
        }

        private fun computeScrollDuration(dx: Int, dy: Int, vx: Int, vy: Int): Int {
            val absDx = Math.abs(dx)
            val absDy = Math.abs(dy)
            val horizontal = absDx > absDy
            val velocity = Math.sqrt((vx * vx + vy * vy).toDouble()).toInt()
            val delta = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
            val containerSize = if (horizontal) width else height
            val halfContainerSize = containerSize / 2
            val distanceRatio = Math.min(1f, 1f * delta / containerSize)
            val distance = halfContainerSize + halfContainerSize * distanceInfluenceForSnapDuration(distanceRatio)

            val duration: Int
            if (velocity > 0) {
                duration = 4 * Math.round(1000 * Math.abs(distance / velocity))
            } else {
                val absDelta = (if (horizontal) absDx else absDy).toFloat()
                duration = ((absDelta / containerSize + 1) * 300).toInt()
            }
            return formatDuration(duration)
        }

        fun smoothScrollBy(dx: Int, dy: Int, interpolator: Interpolator?) {
            smoothScrollBy(dx, dy, computeScrollDuration(dx, dy, 0, 0),
                    interpolator ?: defaultInterpolator)
        }

        fun smoothScrollBy(dx: Int, dy: Int, duration: Int, interpolator: Interpolator? = defaultInterpolator) {
            if (mInterpolator !== interpolator) {
                mInterpolator = interpolator
                mScroller = ScrollerCompat.create(context, interpolator)
            }
            scrollState = SCROLL_STATE_SETTLING
            mLastFlingY = 0
            mLastFlingX = mLastFlingY
            mScroller!!.startScroll(0, 0, dx, dy, duration)
            postOnAnimation()
        }

        fun stop() {
            removeCallbacks(this)
            mScroller!!.abortAnimation()
        }
    }

    interface OnScrollChangeListener {
        fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)

        fun onScrollStateChanged(state: Int, oldState: Int)
    }

    companion object {
        private val INVALID_POINTER = -1
        private val MAX_SCROLL_DURATION = 2000
        val SCROLL_STATE_IDLE = 0
        val SCROLL_STATE_DRAGGING = 1
        val SCROLL_STATE_SETTLING = 2

        private val DefaultInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }
        //end:NestedScrollingChild

        protected fun clamp(n: Int, my: Int, child: Int): Int {
            if (my >= child || n < 0) {
                /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- mScrollX --|
             */
                return 0
            }
            return if (my + n > child) {
                /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- mScrollX --|
             */
                child - my
            } else n
        }
    }
}