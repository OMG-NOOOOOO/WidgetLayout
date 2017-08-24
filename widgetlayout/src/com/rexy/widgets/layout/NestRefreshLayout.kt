package com.rexy.widgets.layout

import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.support.v4.util.Pair
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.ScrollingView
import android.support.v4.view.ViewCompat
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.Transformation
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.ScrollView
import java.util.*

/**
 * TODO:功能说明
 * 每次下接刷新仅仅会影响二次或四次measure 和 layout (状态变法设置visible 引起)
 *
 * @author: rexy
 * @date: 2017-06-19 16:23
 */
class NestRefreshLayout<INDICATOR : View> : BaseViewGroup, NestedScrollingParent where INDICATOR : NestRefreshLayout.OnRefreshListener {
    internal var mHeaderView: View? = null//固定头部。
    internal var mFooterView: View? = null//固定尾部。
    internal var mRefreshHeader: INDICATOR? = null//刷新指示头。
    internal var mRefreshFooter: INDICATOR? = null//刷新指示尾。
    internal var mContentView: View? = null//中间内容，仅支持一个。
    var maskView: View? = null
        internal set

    internal var mScrollChild: View? = null

    internal var isHeaderViewFloat: Boolean = false
    internal var isFooterViewFloat: Boolean = false
    internal var isMaskContent = true
    var isRefreshPullEnable = true
        get() {
            return field && mRefreshHeader != null && hasRefreshListener(mRefreshHeader)
        }
    var isRefreshPushEnable = false
        get() {
            return field && mRefreshFooter != null && hasRefreshListener(mRefreshFooter)
        }
    internal var isRefreshNestEnable = true
    internal var mInterceptTouchRefresh = true

    internal var mMaskViewVisible = -1
    internal var mMaxPullDistance = -1
    internal var mMaxPushDistance = -1

    internal var mRefreshListener: OnRefreshListener? = null
    internal var mRefreshMoveFactor = 0.33f
    internal var mRefreshReadyFactor = 1.3f

    private var isRefreshing = false
    private var isOptHeader: Boolean = false
    private var mIsBeingDragged = false
    private var mCancelDragged = false
    private val mPointDown = PointF()
    private val mPointLast = PointF()
    private var mNeedCheckAddInLayout = false
    private val mMeasureResult = IntArray(3)

    private val mRefreshOffsetWay = false
    private var mNestInitDistance = 0
    var refreshState = OnRefreshListener.STATE_IDLE
        private set
    private var mLastRefreshDistance = 0
    private var mInnerMaxPullDistance = 0
    private var mInnerMaxPushDistance = 0
    private var mInnerPullReadyDistance = 0
    private var mInnerPushReadyDistance = 0

    internal var mDurationMin = 0
    internal var mDurationMax = 0
    internal var mInterpolator: Interpolator? = null
    private var mRefreshAnimation: RefreshAnimation? = null

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

    private fun init(context: Context, attr: AttributeSet?) {
        setTouchScrollEnable(true)
    }

    override fun setTouchScrollEnable(touchScrollEnable: Boolean) {
        super.setTouchScrollEnable(touchScrollEnable)
        setTouchScrollHorizontalEnable(false)
        setTouchScrollVerticalEnable(touchScrollEnable)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, checkContentView(child, index), params)
    }

    private fun checkContentView(child: View, index: Int): Int {
        if (child !== mRefreshHeader && child !== mRefreshFooter && child !== mHeaderView && child !== mFooterView) {
            if (mContentView != null && mContentView!!.parent === this) {
                throw IllegalStateException("RefreshLayout can host only one content child")
            }
            mContentView = child
            var optIndex = 0
            if (mRefreshFooter != null && mRefreshFooter!!.parent === this) {
                optIndex++
            }
            if (mRefreshHeader != null && mRefreshHeader!!.parent === this) {
                optIndex++
            }
            return optIndex
        }
        return index
    }

    @JvmOverloads
    fun setHeaderView(child: View, headerViewFloat: Boolean = isHeaderViewFloat): Boolean {
        if (mHeaderView !== child) {
            val oldView = mHeaderView
            mHeaderView = child
            val floatChanged = this.isHeaderViewFloat != headerViewFloat
            if (floatChanged) {
                this.isHeaderViewFloat = headerViewFloat
            }
            if (!updateBuildInView(child, oldView) && floatChanged) {
                requestLayout()
            }
            return true
        }
        return false
    }

    fun setHeaderViewFloat(floatView: Boolean) {
        if (isHeaderViewFloat != floatView) {
            isHeaderViewFloat = floatView
            if (!skipChild(mHeaderView)) {
                requestLayout()
            }
        }
    }

    @JvmOverloads
    fun setFooterView(child: View, footerViewFloat: Boolean = isFooterViewFloat): Boolean {
        if (mFooterView !== child) {
            val oldView = mFooterView
            mFooterView = child
            val floatChanged = this.isFooterViewFloat != footerViewFloat
            if (floatChanged) {
                this.isFooterViewFloat = footerViewFloat
            }
            if (!updateBuildInView(child, oldView) && floatChanged) {
                requestLayout()
            }
            return true
        }
        return false
    }

    fun setFooterViewFloat(floatView: Boolean) {
        if (isFooterViewFloat != floatView) {
            isFooterViewFloat = floatView
            if (!skipChild(mFooterView)) {
                requestLayout()
            }
        }
    }

    fun setMaskView(child: View, justMaskContent: Boolean): Boolean {
        if (maskView !== child) {
            val oldView = maskView
            maskView = child
            val maskChanged = this.isMaskContent != justMaskContent
            if (maskChanged) {
                this.isMaskContent = justMaskContent
            }
            if (!updateBuildInView(child, oldView) && maskChanged) {
                requestLayout()
            }
        }
        return false
    }

    fun setMaskViewVisibility(visibility: Int) {
        if (mMaskViewVisible != visibility) {
            mMaskViewVisible = visibility
            if (!skipChild(maskView)) {
                maskView!!.visibility = View.VISIBLE
                mMaskViewVisible = -1
            }
        }
    }

    fun setMaskViewJustOverContent(justOverContent: Boolean) {
        if (isMaskContent != justOverContent) {
            isMaskContent = justOverContent
            if (!skipChild(maskView) && (!isMaskContent || skipChild(mContentView))) {
                requestLayout()
            }
        }
    }

    fun setRefreshPullIndicator(child: INDICATOR?): Boolean {
        if (mRefreshHeader !== child) {
            if (child != null && child !is OnRefreshListener) {
                throw IllegalArgumentException("refresh indicator must implements " + OnRefreshListener::class.java.name)
            }
            val oldView = mRefreshHeader
            mRefreshHeader = child
            updateBuildInView(child, oldView)
        }
        return child != null
    }

    fun setRefreshPushIndicator(child: INDICATOR?): Boolean {
        if (mRefreshFooter !== child) {
            if (child != null && child !is OnRefreshListener) {
                throw IllegalArgumentException("refresh indicator must implements " + OnRefreshListener::class.java.name)
            }
            val oldView = mRefreshFooter
            mRefreshFooter = child
            updateBuildInView(child, oldView)
        }
        return child != null
    }

    fun setRefresNestEnable(enable: Boolean) {
        isRefreshNestEnable = enable
    }

    fun setRefreshInterceptTouch(interceptTouchWhileRefresh: Boolean) {
        mInterceptTouchRefresh = interceptTouchWhileRefresh
    }

    /**
     * @param moveFactor value in [0.25f,1f]
     */
    fun setRefreshMoveFactor(moveFactor: Float) {
        if (moveFactor > 0.25f && moveFactor <= 1) {
            mRefreshMoveFactor = moveFactor
        }
    }

    /**
     * @param refreshReadyFactor value in (1,..)
     */
    fun setRefreshReadyFactor(refreshReadyFactor: Float) {
        if (refreshReadyFactor > 1) {
            mRefreshReadyFactor = refreshReadyFactor
        }
    }

    fun setOnRefreshListener(l: OnRefreshListener) {
        mRefreshListener = l
    }

    fun setRefreshing(refreshHeader: Boolean) {
        var criticalDistance = 0
        if (refreshHeader && isRefreshPullEnable) {
            criticalDistance = (pullReadyDistance / mRefreshReadyFactor).toInt()
        }
        if (!refreshHeader && isRefreshPushEnable) {
            criticalDistance = (pushReadyDistance / mRefreshReadyFactor).toInt()
        }
        if (criticalDistance > 0) {
            setRefreshComplete(false)
            isOptHeader = refreshHeader
            animateRefresh(0, criticalDistance, -1)
        }
    }

    @JvmOverloads
    fun setRefreshComplete(animation: Boolean = true) {
        if (refreshState != OnRefreshListener.STATE_IDLE || mLastRefreshDistance != 0) {
            animateRefresh(mLastRefreshDistance, 0, if (animation) -1 else 0)
        }
    }

    fun setAnimationInterpolator(l: Interpolator) {
        mInterpolator = l
    }

    fun setAnimationDuration(minDuration: Int, maxDuration: Int) {
        mDurationMin = minDuration
        mDurationMax = maxDuration
    }

    fun getHeaderView(): View? {
        return mHeaderView
    }

    fun getFooterView(): View? {
        return mFooterView
    }

    fun getRefreshPullIndicator(): INDICATOR? {
        return mRefreshHeader
    }

    fun getRefreshPushIndicator(): INDICATOR? {
        return mRefreshFooter
    }


    var contentView: View?
        get() = mContentView
        set(child) {
            if (mContentView !== child) {
                val optView = mContentView
                mContentView = child
                if (optView != null && optView.parent === this) {
                    removeView(optView)
                }
                if (child != null) {
                    addView(child)
                }
            }
        }

    fun setScrollChild(child: View?) {
        mScrollChild = child
    }

    fun isRefreshing(): Boolean {
        return isRefreshing
    }

    private fun hasRefreshListener(indicator: View?): Boolean {
        return mRefreshListener != null || indicator is OnRefreshListener
    }

    fun canScrollToChildTop(child: View?): Boolean {
        if (child == null) {
            return false
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (child is AbsListView) {
                val absListView = child as AbsListView?
                return absListView!!.childCount > 0 && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(0)
                        .top < absListView.paddingTop)
            } else {
                return ViewCompat.canScrollVertically(child, -1) || child.scrollY > 0
            }
        } else {
            return ViewCompat.canScrollVertically(child, -1)|| child.scrollY > 0
        }
    }

    fun canScrollToChildBottom(child: View?): Boolean {
        if (child == null) {
            return false
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (child is AbsListView) {
                val absListView = child as AbsListView?
                if (absListView!!.childCount > 0) {
                    val lastChildBottom = absListView.getChildAt(absListView.childCount - 1)
                            .bottom
                    return absListView.lastVisiblePosition == absListView.adapter.count - 1 && lastChildBottom <= absListView.measuredHeight
                } else {
                    return false
                }
            } else {
                return ViewCompat.canScrollVertically(child, 1) || child.scrollY > 0
            }
        } else {
            return ViewCompat.canScrollVertically(child, 1)|| child.scrollY > 0
        }
    }

    protected fun isScrollAbleView(view: View?): Boolean {
        return view is ScrollingView
                || view is AbsListView
                || view is ScrollView
                || view is WebView
                || view is NestedScrollView
                || view is PageScrollView
                || view is NestFloatLayout
    }

    protected val scrollAbleView: View?
        get() {
            if (mScrollChild == null && mContentView != null) {
                if (isScrollAbleView(mContentView)) {
                    mScrollChild = mContentView
                } else if (mContentView is ViewGroup) {
                    val maxRound = 3
                    val queue = LinkedList<Pair<Int, ViewGroup>>()
                    queue.offer(Pair.create(0, mContentView as ViewGroup?))
                    var pair = queue.poll();
                    while (pair != null) {
                        val parent = pair.second
                        val round = pair.first
                        val size = parent?.childCount ?: 0
                        for (i in 0..size - 1) {
                            val v = parent!!.getChildAt(i)
                            if (isScrollAbleView(v)) {
                                mScrollChild = v
                                queue.clear()
                                return v
                            } else if (v is ViewGroup) {
                                if (round < maxRound) {
                                    queue.offer(Pair.create(round + 1, v))
                                }
                            }
                        }
                        pair = queue.poll();
                    }
                    queue.clear()
                }
            }
            return mScrollChild
        }

    private fun updateBuildInView(view: View?, oldView: View?): Boolean {
        var requestLayout = false
        if (oldView != null && oldView.parent === this) {
            removeView(oldView)
            requestLayout = true
        }
        if (view != null) {
            mNeedCheckAddInLayout = true
            requestLayout()
            requestLayout = true
        }
        return requestLayout
    }

    private fun addBuildInView(child: View?, index: Int, defaultWidth: Int) {
        if (child == null) {
            throw IllegalArgumentException("Cannot add a null child view to a ViewGroup")
        }
        var params: ViewGroup.LayoutParams? = child.layoutParams
        if (params == null) {
            params = generateDefaultLayoutParams()
            params.width = defaultWidth
        } else if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params)
        }
        addViewInLayout(child, index, params, true)
    }

    private fun addBuildInViewIfNeed(view: View?, addIndex: Int, defaultWidth: Int): Int {
        var addIndex = addIndex
        if (view != null) {
            if (view.parent !== this) {
                addBuildInView(view, addIndex, defaultWidth)
            }
            addIndex++
        }
        return addIndex
    }

    private fun makeSureBuildInViewAdded() {
        if (refreshState == OnRefreshListener.STATE_IDLE) {
            checkRefreshVisible(false, false)
        }
        var optIndex = addBuildInViewIfNeed(mRefreshFooter, 0, -1)
        optIndex = addBuildInViewIfNeed(mRefreshHeader, optIndex, -1)
        if (mContentView != null && mContentView!!.parent === this) {
            optIndex++
        }
        optIndex = addBuildInViewIfNeed(mFooterView, optIndex, -1)
        optIndex = addBuildInViewIfNeed(mHeaderView, optIndex, -1)
        val resetMaskVisible = mMaskViewVisible != -1 && maskView != null && maskView!!.parent !== this
        addBuildInViewIfNeed(maskView, optIndex, -1)
        if (resetMaskVisible) {
            maskView!!.visibility = mMaskViewVisible
            mMaskViewVisible = -1
        }
    }

    override fun skipChild(child: View?): Boolean {
        return super.skipChild(child) || child!!.parent !== this
    }

    private fun measureChild(child: View?, itemPosition: Int, parentSpecWidth: Int, parentSpecHeight: Int, heightUsed: Int) {
        Arrays.fill(mMeasureResult, 0)
        val params = child!!.layoutParams as BaseViewGroup.LayoutParams
        params.measure(child, itemPosition, parentSpecWidth, parentSpecHeight, 0, heightUsed)
        mMeasureResult[0] = params.width(child)
        mMeasureResult[1] = params.height(child)
        mMeasureResult[2] = child.measuredState
    }

    override fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int) {
        if (mNeedCheckAddInLayout) {
            makeSureBuildInViewAdded()
            mNeedCheckAddInLayout = false
        }
        var contentHeight = 0
        var contentWidth = 0
        var childState = 0
        var floatHeight = 0
        var refreshHeight = 0
        var itemPosition = 0
        val contentVisible = !skipChild(mContentView)
        if (!skipChild(mHeaderView)) {
            measureChild(mHeaderView, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, contentHeight)
            contentWidth = Math.max(contentWidth, mMeasureResult[0])
            childState = childState or mMeasureResult[2]
            if (isHeaderViewFloat) {
                floatHeight += mMeasureResult[1]
            } else {
                contentHeight += mMeasureResult[1]
            }
        }
        if (!skipChild(mFooterView)) {
            measureChild(mFooterView, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, contentHeight)
            contentWidth = Math.max(contentWidth, mMeasureResult[0])
            childState = childState or mMeasureResult[2]
            if (isFooterViewFloat) {
                floatHeight += mMeasureResult[1]
            } else {
                contentHeight += mMeasureResult[1]
            }
        }
        val usedHeight = contentHeight
        if (!skipChild(mRefreshHeader)) {
            measureChild(mRefreshHeader, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, usedHeight)
            contentWidth = Math.max(contentWidth, mMeasureResult[0])
            childState = childState or mMeasureResult[2]
            refreshHeight = Math.max(refreshHeight, mMeasureResult[1])
        }
        if (contentVisible && !skipChild(mRefreshFooter)) {
            measureChild(mRefreshFooter, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, usedHeight)
            contentWidth = Math.max(contentWidth, mMeasureResult[0])
            childState = childState or mMeasureResult[2]
            refreshHeight = Math.max(refreshHeight, mMeasureResult[1])
        }

        if (contentVisible) {
            measureChild(mContentView, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, usedHeight)
            contentWidth = Math.max(contentWidth, mMeasureResult[0])
            childState = childState or mMeasureResult[2]
            contentHeight += mMeasureResult[1]
        }
        var maxContentHeight = Math.max(contentHeight, Math.max(refreshHeight, floatHeight))
        if (!skipChild(maskView) && (contentVisible || !isMaskContent)) {
            val params = maskView!!.layoutParams as BaseViewGroup.LayoutParams
            if (isMaskContent) {
                val cp = mContentView!!.layoutParams as BaseViewGroup.LayoutParams
                val cWidth = cp.width(mContentView!!)
                val cHeight = cp.height(mContentView!!)
                val widthMode = View.MeasureSpec.getMode(widthMeasureSpecContent)
                val heightMode = View.MeasureSpec.getMode(heightMeasureSpecContent)
                params.measure(maskView!!, itemPosition++, View.MeasureSpec.makeMeasureSpec(cWidth, widthMode), View.MeasureSpec.makeMeasureSpec(cHeight, heightMode), 0, 0)
            } else {
                params.measure(maskView!!, itemPosition++, widthMeasureSpecContent, heightMeasureSpecContent, 0, 0)
            }
            maxContentHeight = Math.max(maxContentHeight, params.height(maskView!!))
        }
        setContentSize(contentWidth, maxContentHeight, childState)
    }

    override fun dispatchLayout(contentLeft: Int, contentTop: Int) {
        val contentRight = contentLeft + contentWidth
        var childLeft: Int
        var childTop = contentTop
        var childRight: Int
        var childBottom: Int
        val contentVisible = !skipChild(mContentView)
        val offsetTopAndBottom = if (mRefreshOffsetWay && refreshState != OnRefreshListener.STATE_IDLE) if (isOptHeader) mLastRefreshDistance else -mLastRefreshDistance else 0
        if (!skipChild(mHeaderView)) {
            val params = mHeaderView!!.layoutParams as BaseViewGroup.LayoutParams
            val childWidth = mHeaderView!!.measuredWidth
            val childHeight = mHeaderView!!.measuredHeight
            childLeft = getContentStartH(contentLeft, contentRight, childWidth, params.leftMargin, params.rightMargin, params.gravity)
            childRight = childLeft + childWidth
            childTop += params.topMargin
            childBottom = childTop + childHeight
            if (!isOptHeader && offsetTopAndBottom != 0) {
                mHeaderView!!.layout(childLeft, childTop + offsetTopAndBottom, childRight, childBottom + offsetTopAndBottom)
            } else {
                mHeaderView!!.layout(childLeft, childTop, childRight, childBottom)
            }
            if (isHeaderViewFloat) {
                childTop = contentTop
            } else {
                childTop = childBottom + params.bottomMargin
            }
        }
        var biggestBottom = childTop
        var tempTop = contentTop
        if (contentVisible) {
            tempTop = childTop
            val params = mContentView!!.layoutParams as BaseViewGroup.LayoutParams
            val childWidth = mContentView!!.measuredWidth
            val childHeight = mContentView!!.measuredHeight
            childLeft = getContentStartH(contentLeft, contentRight, childWidth, params.leftMargin, params.rightMargin, params.gravity)
            childRight = childLeft + childWidth
            childTop += params.topMargin
            childBottom = childTop + childHeight
            mContentView!!.layout(childLeft, childTop + offsetTopAndBottom, childRight, childBottom + offsetTopAndBottom)
            childTop = biggestBottom
            biggestBottom = childBottom + params.bottomMargin
        }
        if (!skipChild(mRefreshHeader)) {
            val params = mRefreshHeader!!.layoutParams as BaseViewGroup.LayoutParams
            val childWidth = mRefreshHeader!!.measuredWidth
            val childHeight = mRefreshHeader!!.measuredHeight
            childLeft = getContentStartH(contentLeft, contentRight, childWidth, params.leftMargin, params.rightMargin, params.gravity)
            childRight = childLeft + childWidth
            childTop += params.topMargin
            childBottom = childTop + childHeight
            mRefreshHeader!!.layout(childLeft, childTop, childRight, childBottom)
            biggestBottom = Math.max(biggestBottom, childBottom + params.bottomMargin)
        }

        if (contentVisible && !skipChild(mRefreshFooter)) {
            val params = mRefreshFooter!!.layoutParams as BaseViewGroup.LayoutParams
            val childWidth = mRefreshFooter!!.measuredWidth
            val childHeight = mRefreshFooter!!.measuredHeight
            childLeft = getContentStartH(contentLeft, contentRight, childWidth, params.leftMargin, params.rightMargin, params.gravity)
            childRight = childLeft + childWidth
            childBottom = biggestBottom - params.bottomMargin
            childTop = childBottom - childHeight
            mRefreshFooter!!.layout(childLeft, childTop, childRight, childBottom)
        }

        if (!skipChild(mFooterView)) {
            val params = mFooterView!!.layoutParams as BaseViewGroup.LayoutParams
            val childWidth = mFooterView!!.measuredWidth
            val childHeight = mFooterView!!.measuredHeight
            childLeft = getContentStartH(contentLeft, contentRight, childWidth, params.leftMargin, params.rightMargin, params.gravity)
            childRight = childLeft + childWidth
            if (isFooterViewFloat) {
                childBottom = biggestBottom - params.bottomMargin
                childTop = childBottom - childHeight
            } else {
                childTop = biggestBottom + params.topMargin
                childBottom = childTop + childHeight
            }
            if (isOptHeader && offsetTopAndBottom != 0) {
                mFooterView!!.layout(childLeft, childTop + offsetTopAndBottom, childRight, childBottom + offsetTopAndBottom)
            } else {
                mFooterView!!.layout(childLeft, childTop, childRight, childBottom)
            }
        }

        if (!skipChild(maskView) && (contentVisible || !isMaskContent)) {
            val params = maskView!!.layoutParams as BaseViewGroup.LayoutParams
            val childWidth = maskView!!.measuredWidth
            val childHeight = maskView!!.measuredHeight
            childTop = (if (isMaskContent) tempTop else contentTop) + params.topMargin
            childBottom = childTop + childHeight
            childLeft = getContentStartH(contentLeft, contentRight, childWidth, params.leftMargin, params.rightMargin, params.gravity)
            childRight = childLeft + childWidth
            maskView!!.layout(childLeft, childTop + offsetTopAndBottom, childRight, childBottom + offsetTopAndBottom)
        }
    }

    private fun ifNeedInterceptTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mCancelDragged = mContentView == null || mContentView!!.parent !== this || refreshState != OnRefreshListener.STATE_IDLE
            if (!mCancelDragged) {
                val refreshHeaderEnable = isRefreshPullEnable
                val refreshFooterEnable = isRefreshPushEnable
                mCancelDragged = !refreshHeaderEnable && !refreshFooterEnable
            }
        }
        return mCancelDragged
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ifNeedInterceptTouch(ev) || mCancelDragged) {
            mIsBeingDragged = false
        } else {
            val action = ev.action and MotionEventCompat.ACTION_MASK
            if (action == MotionEvent.ACTION_MOVE) {
                handleTouchActionMove(ev, true)
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    handleTouchActionDown(ev)
                }
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    handleTouchActionUp(ev)
                }
            }
        }
        return mIsBeingDragged || isRefreshing && (mInterceptTouchRefresh || skipChild(mHeaderView) && skipChild(mFooterView) && skipChild(maskView))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (ifNeedInterceptTouch(event) || mCancelDragged) {
            return super.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_DOWN && event.edgeFlags != 0) {
            return false
        }
        val action = event.action and MotionEventCompat.ACTION_MASK
        if (action == MotionEvent.ACTION_MOVE) {
            handleTouchActionMove(event, false)
        } else {
            if (action == MotionEvent.ACTION_DOWN) {
                handleTouchActionDown(event)
            }
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                handleTouchActionUp(event)
            }
        }
        return true
    }

    private fun handleTouchActionDown(ev: MotionEvent) {
        mPointDown.set(ev.x, ev.y)
        mPointLast.set(mPointDown)
        mIsBeingDragged = false
        if (mLastRefreshDistance != 0) {
            setRefreshComplete(false)
        }
    }

    private fun handleTouchActionMove(ev: MotionEvent, fromIntercept: Boolean) {
        val x = ev.x
        val y = ev.y
        if (mIsBeingDragged) {
            mPointLast.set(x, y)
            updateRefreshByMoveDistance(mPointLast.y - mPointDown.y, false)
        } else {
            var dx = (mPointDown.x - x).toInt()
            var dy = (mPointDown.y - y).toInt()
            var dxAbs = Math.abs(dx)
            var dyAbs = Math.abs(dy)
            var dragged = (dyAbs > mTouchSlop && dyAbs * 0.6f > dxAbs)
            if (dragged) {
                dy = if (dy > 0) mTouchSlop else -mTouchSlop shr 1
                dx = 0
            }
            if (!dragged) {
                dx = (mPointLast.x - x).toInt()
                dy = (mPointLast.y - y).toInt()
                dxAbs = Math.abs(dx)
                dyAbs = Math.abs(dy)
                dragged = dyAbs > mTouchSlop && dyAbs * 0.6f > dxAbs
                if (dragged) {
                    dy = (if (dy > 0) mTouchSlop else -mTouchSlop) shr 1
                    dx = 0
                }
            }
            mPointLast.set(x, y)
            if (dragged) {
                var refreshState = this.refreshState
                if (dy < 0 && isRefreshPullEnable && !canScrollToChildTop(scrollAbleView)) {
                    refreshState = OnRefreshListener.STATE_PULL_TO_READY
                    isOptHeader = true
                }
                if (dy > 0 && isRefreshPushEnable && !canScrollToChildBottom(scrollAbleView)) {
                    refreshState = OnRefreshListener.STATE_PUSH_TO_READY
                    isOptHeader = false
                }
                if (refreshState != this.refreshState) {
                    mIsBeingDragged = true
                    mInnerMaxPushDistance = 0
                    mInnerMaxPullDistance = mInnerMaxPushDistance
                    mInnerPushReadyDistance = 0
                    mInnerPullReadyDistance = mInnerPushReadyDistance
                    mPointDown.set(mPointLast)
                    mPointDown.offset(dx.toFloat(), dy.toFloat())
                    updateRefreshState(refreshState, (dy * mRefreshMoveFactor).toInt())
                    updateRefreshByMoveDistance(mPointLast.y - mPointDown.y, false)
                }
            }
        }
    }

    private fun handleTouchActionUp(ev: MotionEvent) {
        if (mIsBeingDragged) {
            mIsBeingDragged = false
            mPointLast.set(ev.x, ev.y)
            updateRefreshByMoveDistance(mPointLast.y - mPointDown.y, true)
        }
    }

    private fun checkRefreshVisible(headerVisible: Boolean, footerVisible: Boolean) {
        if (mRefreshHeader != null) {
            val visible = if (headerVisible) View.VISIBLE else View.GONE
            if (mRefreshHeader!!.visibility != visible) {
                mRefreshHeader!!.visibility = visible
            }
        }
        if (mRefreshFooter != null) {
            val visible = if (footerVisible) View.VISIBLE else View.GONE
            if (mRefreshFooter!!.visibility != visible) {
                mRefreshFooter!!.visibility = visible
            }
        }
    }

    private fun updateRefreshState(refreshState: Int, formatDistance: Int) {
        var formatDistance = formatDistance
        if (this.refreshState != refreshState) {
            if (formatDistance < 0) {
                formatDistance = -formatDistance
            }
            val oldRefreshState = this.refreshState
            this.refreshState = refreshState
            if (refreshState == OnRefreshListener.STATE_IDLE) {
                isRefreshing = false
                checkRefreshVisible(false, false)
            } else {
                checkRefreshVisible(isOptHeader, !isOptHeader)
            }
            notify(refreshState, oldRefreshState, formatDistance)
        }
    }

    protected var maxPullDistance: Int
        get() {
            if (mInnerMaxPullDistance == 0) {
                val maxDistance = (height * mRefreshMoveFactor).toInt()
                mInnerMaxPullDistance = if (mMaxPullDistance <= 0) maxDistance else Math.min(mMaxPullDistance, maxDistance)
            }
            return mInnerMaxPullDistance
        }
        set(maxPullDistance) {
            mMaxPullDistance = maxPullDistance
        }

    protected var maxPushDistance: Int
        get() {
            if (mInnerMaxPushDistance == 0) {
                val maxDistance = (height * mRefreshMoveFactor).toInt()
                mInnerMaxPushDistance = if (mMaxPushDistance <= 0) maxDistance else Math.min(mMaxPushDistance, maxDistance)
            }
            return mInnerMaxPushDistance
        }
        set(maxPUshDistance) {
            mMaxPushDistance = maxPUshDistance
        }

    protected val pullReadyDistance: Int
        get() {
            if (mInnerPullReadyDistance == 0 && !skipChild(mRefreshHeader) && mRefreshHeader!!.measuredHeight > 0) {
                mInnerPullReadyDistance = (mRefreshHeader!!.measuredHeight * mRefreshReadyFactor).toInt()
            }
            return mInnerPullReadyDistance
        }

    protected val pushReadyDistance: Int
        get() {
            if (mInnerPushReadyDistance == 0 && !skipChild(mRefreshFooter) && mRefreshFooter!!.measuredHeight > 0) {
                mInnerPushReadyDistance = (mRefreshFooter!!.measuredHeight * mRefreshReadyFactor).toInt()
            }
            return mInnerPushReadyDistance
        }

    private fun calculateRefreshState(formatDistance: Int, viewHeight: Int, criticalDistance: Int, optRefreshHeader: Boolean): Int {
        if (criticalDistance == 0) {
            return refreshState
        } else {
            if (formatDistance < viewHeight) {
                return if (optRefreshHeader) OnRefreshListener.STATE_PULL_TO_READY else OnRefreshListener.STATE_PUSH_TO_READY
            }
            if (formatDistance < criticalDistance) {
                return if (optRefreshHeader) OnRefreshListener.STATE_PULL_READY else OnRefreshListener.STATE_PUSH_READY
            }
            return if (optRefreshHeader) OnRefreshListener.STATE_PULL_BEYOND_READY else OnRefreshListener.STATE_PUSH_BEYOND_READY
        }
    }

    protected fun interceptRefreshDirectionChanged(willOptHeader: Boolean, distance: Float, cancelUp: Boolean): Boolean {
        return true
    }

    private fun animateRefresh(from: Int, to: Int, duration: Int) {
        var from = from
        if (mRefreshAnimation != null) {
            if (!mRefreshAnimation!!.hasEnded()) {
                from = if (mRefreshAnimation!!.hasStarted()) mRefreshAnimation!!.value else from
                mRefreshAnimation!!.cancel()
            }
        }
        mRefreshAnimation = RefreshAnimation()
        val finalDuration = if (duration < 0) calculateDuration(Math.abs(to - from), isOptHeader) else duration
        if (finalDuration == 0) {
            updateRefreshDistance(to, false)
        } else {
            mRefreshAnimation!!.reset(from, to, true, finalDuration)
            if (mInterpolator != null) {
                mRefreshAnimation!!.interpolator = mInterpolator
            }
            startAnimation(mRefreshAnimation)
        }
    }

    private fun calculateMaxAnimationRefreshDistance(optHeader: Boolean): Int {
        val left: Int
        val right: Int
        if (optHeader) {
            left = if (mRefreshHeader == null) 0 else mRefreshHeader!!.measuredHeight
            right = maxPullDistance - left
        } else {
            left = if (mRefreshFooter == null) 0 else mRefreshFooter!!.measuredHeight
            right = maxPushDistance - left
        }
        return Math.max(left, right)
    }

    private fun calculateDuration(distance: Int, optHeader: Boolean): Int {
        if (distance == 0) return 0
        var defaultMin = 100
        var defaultMax = 250
        val maxDistance = calculateMaxAnimationRefreshDistance(optHeader)
        val minDuration = if (mDurationMin <= 0) defaultMin else mDurationMin
        val maxDuration = if (mDurationMax <= 0) defaultMax else mDurationMax
        if (minDuration == maxDuration || maxDistance == 0) {
            return minDuration
        } else {
            defaultMin = Math.min(minDuration, maxDuration)
            defaultMax = Math.max(minDuration, maxDuration)
            return if (distance > maxDistance) {
                defaultMax
            } else (defaultMin + distance * (defaultMax - defaultMin) / maxDistance.toFloat()).toInt()
        }
    }

    private fun updateRefreshByMoveDistance(moveDistance: Float, cancelUp: Boolean) {
        var formatDistance = Math.round(moveDistance * mRefreshMoveFactor)
        if (formatDistance == 0 && refreshState == OnRefreshListener.STATE_IDLE) {
            return
        }
        val willOptHeader = if (formatDistance == 0) refreshState % 2 == 1 else formatDistance > 0
        if (willOptHeader != isOptHeader) {
            if (interceptRefreshDirectionChanged(willOptHeader, moveDistance, cancelUp)) {
                if (cancelUp) {
                    formatDistance = 0
                } else {
                    return
                }
            }
        }
        if (formatDistance < 0) {
            formatDistance = -formatDistance
        }
        updateRefreshDistance(formatDistance, true)
        if (cancelUp && refreshState != OnRefreshListener.STATE_IDLE) {
            flingAfterMove(formatDistance)
        }
    }

    private fun flingAfterMove(formatDistance: Int) {
        val optView = if (isOptHeader) mRefreshHeader else mRefreshFooter
        val criticalDistance = optView?.height ?: formatDistance
        isRefreshing = formatDistance > criticalDistance
        if (isRefreshing && mRefreshListener != null) {
            animateRefresh(formatDistance, criticalDistance, -1)
            notifyRefresh(isOptHeader)
        } else {
            isRefreshing = false
            animateRefresh(formatDistance, 0, -1)
        }
    }

    private fun updateRefreshByNestMove(nestMoved: Float, nestStop: Boolean) {
        var nestMoved = nestMoved
        nestMoved = nestMoved * mRefreshMoveFactor
        val formatDistance = if (nestMoved > 0.5f && nestMoved < 1) 1 else nestMoved.toInt()
        updateRefreshDistance(formatDistance, true)
        if (nestStop && refreshState != OnRefreshListener.STATE_IDLE) {
            flingAfterMove(formatDistance)
        }
    }

    private fun updateRefreshDistance(refreshAbsDistance: Int, fromUserTouch: Boolean) {
        val oldState = refreshState
        var newState = OnRefreshListener.STATE_IDLE
        val viewHeight: Int
        val criticalDistance: Int
        var finalPosition = refreshAbsDistance
        if (refreshAbsDistance > 0) {
            if (isOptHeader) {
                finalPosition = Math.min(finalPosition, maxPullDistance)
                criticalDistance = pullReadyDistance
                viewHeight = if (mRefreshHeader == null) 0 else mRefreshHeader!!.height
            } else {
                finalPosition = Math.min(finalPosition, maxPushDistance)
                criticalDistance = pushReadyDistance
                viewHeight = if (mRefreshFooter == null) 0 else mRefreshFooter!!.height
            }
            mPointDown.y += (if (isOptHeader) refreshAbsDistance - finalPosition else finalPosition - refreshAbsDistance).toFloat()
            newState = calculateRefreshState(finalPosition, viewHeight, criticalDistance, isOptHeader)
        }
        if (oldState != newState) {
            updateRefreshState(newState, refreshAbsDistance)
        }
        if (mLastRefreshDistance != finalPosition) {
            updateRefreshView(finalPosition, isOptHeader)
            notify(refreshState, refreshState, finalPosition)
        }
    }

    private fun updateRefreshView(absOffset: Int, refreshHeader: Boolean) {
        var offset = if (refreshHeader) absOffset - mLastRefreshDistance else mLastRefreshDistance - absOffset
        mLastRefreshDistance = absOffset
        if (mRefreshOffsetWay) {
            mContentView!!.offsetTopAndBottom(offset)
            if (maskView != null && isMaskContent && maskView!!.visibility == View.VISIBLE) {
                maskView!!.offsetTopAndBottom(offset)
            }
            if (refreshHeader) {
                if (mFooterView != null && !isFooterViewFloat) {
                    mFooterView!!.offsetTopAndBottom(offset)
                }
            } else {
                if (mHeaderView != null && !isHeaderViewFloat) {
                    mHeaderView!!.offsetTopAndBottom(offset)
                }
            }
        } else {
            offset = if (refreshHeader) absOffset else -absOffset
            mContentView!!.translationY = offset.toFloat()
            if (maskView != null && isMaskContent && maskView!!.visibility == View.VISIBLE) {
                maskView!!.translationY = offset.toFloat()
            }
            if (refreshHeader) {
                if (mFooterView != null && !isFooterViewFloat) {
                    mFooterView!!.translationY = offset.toFloat()
                }
            } else {
                if (mHeaderView != null && !isHeaderViewFloat) {
                    mHeaderView!!.translationY = offset.toFloat()
                }
            }
        }
    }

    protected fun notify(state: Int, oldState: Int, absDistance: Int) {
        val indicator = if (isOptHeader) mRefreshHeader else mRefreshFooter
        indicator?.onRefreshStateChanged(this@NestRefreshLayout, state, oldState, absDistance)
        if (mRefreshListener != null) {
            mRefreshListener!!.onRefreshStateChanged(this@NestRefreshLayout, state, oldState, absDistance)
        }
    }

    protected fun notifyRefresh(refresh: Boolean) {
        val indicator = if (refresh) mRefreshHeader else mRefreshFooter
        indicator?.onRefresh(this@NestRefreshLayout, refresh)
        if (mRefreshListener != null) {
            mRefreshListener!!.onRefresh(this@NestRefreshLayout, refresh)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        var acceptedVertical = isRefreshNestEnable && !isRefreshing && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        if (acceptedVertical) {
            acceptedVertical = isRefreshPullEnable || isRefreshPushEnable
        }
        return acceptedVertical
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        mNestInitDistance = 0
    }

    override fun onStopNestedScroll(target: View) {
        if (mNestInitDistance != 0) {
            updateRefreshByNestMove(mNestInitDistance.toFloat(), true)
            mNestInitDistance = 0
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (dyConsumed == 0 && dyUnconsumed != 0) {
            if (mNestInitDistance == 0) {
                var refreshState = this.refreshState
                if (dyUnconsumed > 0 && isRefreshPushEnable && !canScrollToChildBottom(scrollAbleView)) {
                    refreshState = OnRefreshListener.STATE_PUSH_TO_READY
                    isOptHeader = false
                    mNestInitDistance = dyUnconsumed
                }
                if (dyUnconsumed < 0 && isRefreshPullEnable && !canScrollToChildTop(scrollAbleView)) {
                    refreshState = OnRefreshListener.STATE_PULL_TO_READY
                    isOptHeader = true
                    mNestInitDistance = -dyUnconsumed
                }
                if (refreshState != this.refreshState) {
                    updateRefreshState(refreshState, mNestInitDistance)
                }
            } else {
                if (dyUnconsumed > 0) {
                    mNestInitDistance += dyUnconsumed
                }
                if (dyUnconsumed < 0) {
                    mNestInitDistance -= dyUnconsumed
                }
            }
            if (mNestInitDistance != 0) {
                updateRefreshByNestMove(mNestInitDistance.toFloat(), false)
            }
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (mNestInitDistance != 0 && dy != 0) {
            if (isOptHeader) {
                if (dy > 0) {
                    val willConsumed = Math.min(dy, mNestInitDistance)
                    consumed[1] = willConsumed
                    mNestInitDistance -= willConsumed
                    updateRefreshByNestMove(mNestInitDistance.toFloat(), false)
                }
            } else {
                if (dy < 0) {
                    val willConsumed = Math.min(-dy, mNestInitDistance)
                    consumed[1] = -willConsumed
                    mNestInitDistance -= willConsumed
                    updateRefreshByNestMove(mNestInitDistance.toFloat(), false)
                }
            }
        }
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return false
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun getNestedScrollAxes(): Int {
        return ViewCompat.SCROLL_AXIS_VERTICAL
    }

    interface OnRefreshListener {

        fun onRefreshStateChanged(parent: NestRefreshLayout<*>, state: Int, preState: Int, moveAbsDistance: Int)

        fun onRefresh(parent: NestRefreshLayout<*>, refresh: Boolean)

        companion object {
            val STATE_IDLE = 0
            val STATE_PULL_TO_READY = 1
            val STATE_PULL_READY = 3
            val STATE_PULL_BEYOND_READY = 5

            val STATE_PUSH_TO_READY = 2
            val STATE_PUSH_READY = 4
            val STATE_PUSH_BEYOND_READY = 6
        }
    }

    inner class RefreshAnimation : Animation(), Animation.AnimationListener {
        private var mValueFrom = 0
        private var mValueTo = 0
        var value = 0
            private set
        private var mCancel: Boolean = false

        public override fun applyTransformation(t: Float, trans: Transformation) {
            if (!mCancel && t < 1) {
                val current = mValueFrom + ((mValueTo - mValueFrom) * t).toInt()
                if (value != current) {
                    value = current
                }
                updateRefreshDistance(value, false)
            }
        }

        fun reset(from: Int, to: Int, listener: Boolean, duration: Int) {
            reset()
            mValueFrom = from
            value = mValueFrom
            mValueTo = to
            setDuration(duration.toLong())
            setAnimationListener(if (listener) this else null)
        }

        override fun cancel() {
            mCancel = true
            super.cancel()
            setAnimationListener(null)
        }

        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            if (!mCancel) {
                updateRefreshDistance(mValueTo, false)
            }
            setAnimationListener(null)
        }

        override fun onAnimationRepeat(animation: Animation) {}
    }
}
