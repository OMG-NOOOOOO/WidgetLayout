package com.rexy.widgets.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.support.v4.util.Pools
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.rexy.widgetlayout.R
import java.util.*

/**
 * A customized scroll container support both Horizontal and Vertical layout and gesture.
 * support both scroll style of ScrollView and ViewPager and also their interfaces .
 * support float any view to its start and end position .
 *
 * @author: rexy
 * @date: 2017-04-25 09:32
 */
open class PageScrollView : ScrollLayout {

    protected var mSizeFixedPercent = 0f
    var isViewPagerStyle = false
    protected var mFloatViewStart = -1
    protected var mFloatViewEnd = -1
    var isChildCenter = false
        set(value) {
            if (field != value) {
                field = value
                if (isAttachLayoutFinished) {
                    requestLayout()
                }
            }
        }
    var isChildFillParent = false
        set(value) {
            if (field != value) {
                field = value
                if (isAttachLayoutFinished) {
                    requestLayout()
                }
            }
        }

    protected var mSwapViewIndex = -1
    protected var mFloatViewStartIndex = -1
    protected var mFloatViewEndIndex = -1

    protected var mFloatViewStartMode = 0
    protected var mFloatViewEndMode = 0

    //目前只保证 pageHeader pageFooter 在item View 添加完后再设置。
    protected var mPageHeaderView: View? = null
    protected var mPageFooterView: View? = null

    internal var mCurrItem = 0
    var prevItem = -1
        internal set
    internal var mFirstVisiblePosition = -1
    internal var mLastVisiblePosition = -1

    internal var mNeedResolveFloatOffset = false

    internal var mPageTransformer: PageTransformer? = null
    var pageChangeListener: OnPageChangeListener? = null
        internal set
    var visibleRangeChangeListener: OnVisibleRangeChangeListener? = null
        internal set

    private var mComparator: Comparator<PointF>? = null
    private var mPairList: MutableList<PointF>? = null
    private val mMeasureTemp = IntArray(3)
    private val mMeasureSum = IntArray(5)

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

    private fun init(context: Context, attributeSet: AttributeSet?) {
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        val attr = if (attributeSet == null) null else context.obtainStyledAttributes(attributeSet, R.styleable.PageScrollView)
        if (attr != null) {
            mSizeFixedPercent = attr.getFloat(R.styleable.PageScrollView_sizeFixedPercent, mSizeFixedPercent)
            isViewPagerStyle = attr.getBoolean(R.styleable.PageScrollView_viewPagerStyle, isViewPagerStyle)
            mFloatViewStart = attr.getInt(R.styleable.PageScrollView_floatViewStartIndex, mFloatViewStart)
            mFloatViewEnd = attr.getInt(R.styleable.PageScrollView_floatViewEndIndex, mFloatViewEnd)
            isChildCenter = attr.getBoolean(R.styleable.PageScrollView_childCenter, isChildCenter)
            isChildFillParent = attr.getBoolean(R.styleable.PageScrollView_childFillParent, isChildFillParent)
        }
    }


    override fun onOrientationChanged(orientation: Int, oldOrientation: Int) {
        if (!isViewPagerStyle) {
            val oldHorizontal = isOrientationHorizontal
            mCurrItem = if (mFirstVisiblePosition >= 0) mFirstVisiblePosition else 0
            resetPositionForFloatView(mFloatViewStartIndex, oldHorizontal)
            resetPositionForFloatView(mFloatViewEndIndex, oldHorizontal)
            mFloatViewStartIndex = -1
            mSwapViewIndex = -1
            mFloatViewStartMode = 0
            mFloatViewEndIndex = -1
            mFloatViewEndMode = 0
        }
        setTouchScrollEnable(isTouchScrollEnable(false))
        if (!isAttachLayoutFinished) {
            scrollToItem(mCurrItem, 0, 0, isViewPagerStyle)
        }
    }

    var floatViewStartIndex: Int
        get() = mFloatViewStart
        set(floatStartIndex) {
            if (mFloatViewStart != floatStartIndex) {
                resetPositionForFloatView(mFloatViewStartIndex, isOrientationHorizontal)
                mFloatViewStart = floatStartIndex
                if (mFloatViewStart >= 0) {
                    mNeedResolveFloatOffset = true
                }
                mSwapViewIndex = -1
                mFloatViewStartIndex = -1
                mFloatViewStartMode = 0
                requestLayout()
            }
        }

    var floatViewEndIndex: Int
        get() = mFloatViewEnd
        set(floatEndIndex) {
            if (mFloatViewEnd != floatEndIndex) {
                resetPositionForFloatView(mFloatViewEndIndex, isOrientationHorizontal)
                mFloatViewEnd = floatEndIndex
                if (mFloatViewEnd >= 0) {
                    mNeedResolveFloatOffset = true
                }
                mFloatViewEndIndex = -1
                mFloatViewEndMode = 0
                requestLayout()
            }
        }

    var sizeFixedPercent: Float
        get() = mSizeFixedPercent
        set(percent) {
            if (mSizeFixedPercent != percent && percent >= 0 && percent <= 0) {
                mSizeFixedPercent = percent
                requestLayout()
            }
        }

    var pageHeaderView: View?
        get() = mPageHeaderView
        set(headView) {
            if (mPageHeaderView !== headView) {
                if (mPageHeaderView != null) {
                    removeViewInLayout(mPageHeaderView)
                }
                mPageHeaderView = headView
                if (mPageHeaderView != null) {
                    addView(mPageHeaderView)
                    mNeedResolveFloatOffset = true
                }
                requestLayout()
            }
        }

    var pageFooterView: View?
        get() = mPageFooterView
        set(pageFooterView) {
            if (mPageFooterView !== pageFooterView) {
                if (mPageFooterView != null) {
                    removeViewInLayout(mPageFooterView)
                }
                mPageFooterView = pageFooterView
                if (mPageFooterView != null) {
                    addView(mPageFooterView)
                    mNeedResolveFloatOffset = true
                }
                requestLayout()
            }
        }

    var pageTransformer: PageTransformer?
        get() = mPageTransformer
        set(transformer) {
            if (mPageTransformer !== transformer) {
                val oldTransformer = mPageTransformer
                mPageTransformer = transformer
                if (isAttachLayoutFinished) {
                    val horizontal = isOrientationHorizontal
                    if (oldTransformer != null && mPageTransformer == null) {
                        val childCount = childCount
                        for (i in 0..childCount - 1) {
                            val child = getChildAt(i)
                            if (!skipVirtualChild(child, true)) {
                                oldTransformer.recoverTransformPage(child, horizontal)
                            }
                        }
                    }
                    if (mPageTransformer != null) {
                        resolvePageOffset(if (horizontal) scrollX else scrollY, horizontal)
                    }
                }
            }
        }

    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        pageChangeListener = listener
    }

    fun setOnVisibleRangeChangeListener(l: OnVisibleRangeChangeListener) {
        visibleRangeChangeListener = l
    }

    fun hasPageHeaderView(): Boolean {
        return mPageHeaderView != null && mPageHeaderView!!.visibility != View.GONE && mPageHeaderView!!.parent === this
    }

    fun hasPageFooterView(): Boolean {
        return mPageFooterView != null && mPageFooterView!!.visibility != View.GONE && mPageFooterView!!.parent === this
    }

    fun getCurrentItem(): Int {
        return mCurrItem
    }

    //TODO 判断是否可悬停，可能需要在onLayout 中进行。
    protected fun floatViewScrollNeeded(view: View?, horizontal: Boolean, floatStartPosition: Boolean): Boolean {
        val scrollRange = if (horizontal) horizontalScrollRange else verticalScrollRange
        return view != null && scrollRange > 0
    }

    protected fun computeFloatViewIndexIfNeed(virtualCount: Int, horizontal: Boolean) {
        mFloatViewStartIndex = -1
        mFloatViewEndIndex = -1
        if (virtualCount >= 2) {
            if (mFloatViewStart >= 0 && mFloatViewStart < virtualCount) {
                computeFloatViewIndex(mFloatViewStart, horizontal, true)
            }
            if (mFloatViewEnd >= 0 && mFloatViewEnd < virtualCount) {
                computeFloatViewIndex(mFloatViewEnd, horizontal, false)
            }
        }
    }

    protected fun computeFloatViewIndex(itemIndex: Int, horizontal: Boolean, floatStart: Boolean) {
        val view = getVirtualChildAt(itemIndex, true)
        if (getVirtualChildAt(itemIndex, false) === view && floatViewScrollNeeded(view, horizontal, floatStart)) {
            if (floatStart) {
                mFloatViewStartIndex = indexOfChild(view)
                mFloatViewStartMode = FLOAT_VIEW_SCROLL
            } else {
                mFloatViewEndIndex = indexOfChild(view)
                mFloatViewEndMode = FLOAT_VIEW_SCROLL
            }
        }
    }

    private fun mergeMeasureResult(horizontal: Boolean) {
        if (horizontal) {
            mMeasureSum[0] = Math.max(mMeasureSum[0], mMeasureTemp[0])
            mMeasureSum[1] += mMeasureTemp[1]
            mMeasureSum[3] += mMeasureTemp[1]
        } else {
            mMeasureSum[0] += mMeasureTemp[0]
            mMeasureSum[1] = Math.max(mMeasureSum[1], mMeasureTemp[1])
            mMeasureSum[2] += mMeasureTemp[0]
        }
        mMeasureSum[4] = mMeasureSum[4] or mMeasureTemp[2]
    }

    override fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int) {
        val horizontal = isOrientationHorizontal
        val itemCount = itemViewCount
        Arrays.fill(mMeasureSum, 0)
        if (hasPageHeaderView()) {
            Arrays.fill(mMeasureTemp, 0)
            measureHeaderFooter(mPageHeaderView, widthMeasureSpecContent, heightMeasureSpecContent, mMeasureSum[2], mMeasureSum[3], horizontal)
            mergeMeasureResult(horizontal)
        }
        if (itemCount > 0) {
            var middleWidthSpec = widthMeasureSpecContent
            var middleHeightSpec = heightMeasureSpecContent
            if (horizontal) {
                middleWidthSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(middleWidthSpec), View.MeasureSpec.UNSPECIFIED)
            } else {
                middleHeightSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(middleHeightSpec), View.MeasureSpec.UNSPECIFIED)
            }
            Arrays.fill(mMeasureTemp, 0)
            measureItems(middleWidthSpec, middleHeightSpec, mMeasureSum[2], mMeasureSum[3], horizontal)
            mergeMeasureResult(horizontal)
        }
        if (hasPageFooterView()) {
            Arrays.fill(mMeasureTemp, 0)
            measureHeaderFooter(mPageFooterView, widthMeasureSpecContent, heightMeasureSpecContent, mMeasureSum[2], mMeasureSum[3], horizontal)
            mergeMeasureResult(horizontal)
        }
        setContentSize(mMeasureSum[0], mMeasureSum[1], mMeasureSum[4])
    }

    protected fun measureHeaderFooter(view: View?, widthMeasureSpec: Int, heightMeasureSpec: Int, widthUsed: Int, heightUsed: Int, horizontal: Boolean) {
        val params = view!!.layoutParams as BaseViewGroup.LayoutParams
        params.measure(view, -1, widthMeasureSpec, heightMeasureSpec, widthUsed, heightUsed)
        mMeasureTemp[0] = params.width(view)
        mMeasureTemp[1] = params.height(view)
        mMeasureTemp[2] = view.measuredState
    }

    protected fun measureItems(widthMeasureSpec: Int, heightMeasureSpec: Int, widthUsed: Int, heightUsed: Int, horizontal: Boolean) {
        val itemWidthMeasureSpec: Int
        val itemHeightMeasureSpec: Int
        var accessWidth = View.MeasureSpec.getSize(widthMeasureSpec) - widthUsed
        var accessHeight = View.MeasureSpec.getSize(heightMeasureSpec) - heightUsed
        val itemMargin = if (horizontal) this.mBorderDivider!!.contentMarginHorizontal else this.mBorderDivider!!.contentMarginVertical
        val fixedOrientationSize = mSizeFixedPercent > 0 && mSizeFixedPercent <= 1
        if (fixedOrientationSize) {
            if (horizontal) {
                accessWidth *= mSizeFixedPercent.toInt()
            } else {
                accessHeight *= mSizeFixedPercent.toInt()
            }
        }
        itemWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(accessWidth, View.MeasureSpec.getMode(widthMeasureSpec))
        itemHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(accessHeight, View.MeasureSpec.getMode(heightMeasureSpec))
        val childCount = childCount
        var contentWidth = 0
        var contentHeight = 0
        var childState = 0
        var itemPosition = 0
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, true)) continue
            val params = child.layoutParams as BaseViewGroup.LayoutParams
            val oldParamsWidth = params.width
            val oldParamsHeight = params.height
            if (fixedOrientationSize) {
                if (horizontal) {
                    params.width = -1
                } else {
                    params.height = -1
                }
            }
            params.measure(child, itemPosition++, itemWidthMeasureSpec, itemHeightMeasureSpec, 0, 0)
            params.width = oldParamsWidth
            params.height = oldParamsHeight
            val itemWidth = params.width(child)
            val itemHeight = params.height(child)
            if (horizontal) {
                contentWidth += itemWidth + itemMargin
                contentHeight = Math.max(contentHeight, itemHeight)
            } else {
                contentWidth = Math.max(contentWidth, itemWidth)
                contentHeight += itemHeight + itemMargin
            }
            childState = childState or child.measuredState
        }
        if (horizontal) {
            mMeasureTemp[0] = contentWidth - itemMargin
            mMeasureTemp[1] = contentHeight
        } else {
            mMeasureTemp[0] = contentWidth
            mMeasureTemp[1] = contentHeight - itemMargin
        }
        mMeasureTemp[2] = childState
    }

    override fun doAfterMeasure(measuredWidth: Int, measuredHeight: Int, contentWidth: Int, contentHeight: Int) {
        val itemCount = itemViewCount
        if (itemCount > 0) {
            val horizontal = isOrientationHorizontal
            if (isChildFillParent && itemCount > 0) {
                val adjustTotal: Int
                if (horizontal) {
                    adjustTotal = measuredWidth - (contentWidth + paddingLeft + paddingRight)
                } else {
                    adjustTotal = measuredHeight - (contentHeight + paddingTop + paddingBottom)
                }
                if (adjustTotal > itemCount && adjustMatchParentMeasure(adjustTotal.toFloat(), horizontal)) {
                    if (horizontal) {
                        setContentSize(contentWidth + adjustTotal, contentHeight, measureState)
                    } else {
                        setContentSize(contentWidth, contentHeight + adjustTotal, measureState)
                    }
                }
            }
            computeFloatViewIndexIfNeed(itemCount, horizontal)
        }
    }

    override fun doAfterLayout(firstAttachLayout: Boolean) {
        super.doAfterLayout(firstAttachLayout)
        if (mNeedResolveFloatOffset && !firstAttachLayout) {
            mNeedResolveFloatOffset = false
            val horizontal = isOrientationHorizontal
            val scrolled = if (horizontal) scrollX else scrollY
            if (mPageHeaderView != null || mPageFooterView != null) {
                updatePositionForHeaderAndFooter(scrolled, horizontal)
            }
            if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
                updatePositionForFloatView(scrolled, horizontal)
            }
        }
    }

    private fun destroyCacheMeasureSize() {
        val its = mPairList!!.iterator()
        while (its.hasNext()) {
            val it = its.next()
            sPairPools.release(it)
            its.remove()
        }
    }

    private fun buildCacheMeasureSize(childCount: Int, horizontal: Boolean): List<PointF> {
        if (mPairList == null) {
            mPairList = ArrayList(8)
            mComparator = Comparator { l, r -> java.lang.Float.compare(l.y, r.y) }
        } else {
            destroyCacheMeasureSize()
        }
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, true)) continue
            var pair: PointF? = sPairPools.acquire()
            if (pair == null) {
                pair = PointF()
            }
            if (horizontal) {
                pair.set(i.toFloat(), (child.layoutParams as BaseViewGroup.LayoutParams).width(child).toFloat())
            } else {
                pair.set(i.toFloat(), (child.layoutParams as BaseViewGroup.LayoutParams).height(child).toFloat())
            }
            mPairList!!.add(pair)
        }
        return mPairList!!
    }

    private fun adjustMatchMeasureSize(matchSize: Int, space: Float) {
        var space = space
        Collections.sort(mPairList!!, mComparator)
        var startIndex = 0
        while (space > 1) {
            var diffIndex = -1
            var start = mPairList!![startIndex]
            var current: PointF? = null
            for (i in startIndex + 1..matchSize - 1) {
                current = mPairList!![i]
                if (current.y > start.y) {
                    diffIndex = i
                    break
                }
            }
            if (diffIndex == -1) {
                val addedSize = space / matchSize
                for (point in mPairList!!) {
                    point.y = point.y + addedSize
                }
                space = 0f
            } else {
                val addedSize = Math.min(current!!.y - start.y, space / diffIndex)
                for (i in 0..diffIndex - 1) {
                    start = mPairList!![i]
                    start.y = start.y + addedSize
                }
                space = space - addedSize * diffIndex
                startIndex = diffIndex
            }
        }
    }

    private fun adjustMatchParentMeasure(space: Float, horizontal: Boolean): Boolean {
        val list = buildCacheMeasureSize(childCount, horizontal)
        val matchSize = list.size
        if (matchSize > 0) {
            adjustMatchMeasureSize(matchSize, space)
            for (point in mPairList!!) {
                val child = getChildAt(point.x.toInt())
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                val goodSize = point.y.toInt()
                if (horizontal) {
                    if (goodSize != params.width(child)) {
                        child.measure(View.MeasureSpec.makeMeasureSpec(goodSize - params.leftMargin() - params.rightMargin(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(child.measuredHeight, View.MeasureSpec.EXACTLY))
                    }
                } else {
                    if (goodSize != params.height(child)) {
                        child.measure(View.MeasureSpec.makeMeasureSpec(child.measuredWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(goodSize - params.topMargin() - params.bottomMargin(), View.MeasureSpec.EXACTLY))
                    }
                }
            }
            destroyCacheMeasureSize()
            return true
        }
        return false
    }

    override fun dispatchLayout(contentLeft: Int, contentTop: Int) {
        val contentRight = contentLeft + contentPureWidth
        val contentBottom = contentTop + contentPureHeight
        if (isOrientationHorizontal) {
            onLayoutHorizontal(contentLeft, contentTop, contentRight, contentBottom)
        } else {
            onLayoutVertical(contentLeft, contentTop, contentRight, contentBottom)
        }
    }

    protected fun onLayoutVertical(baseLeft: Int, baseTop: Int, baseRight: Int, baseBottom: Int) {
        var baseLeft = baseLeft
        var baseRight = baseRight
        var childLeft: Int
        var childTop: Int
        var childRight: Int
        var childBottom: Int
        if (hasPageHeaderView()) {
            val params = mPageHeaderView!!.layoutParams as BaseViewGroup.LayoutParams
            childTop = getContentStartV(Math.max(baseTop, paddingTopWithInset), Math.min(baseBottom, height - paddingBottomWithInset), mPageHeaderView!!.measuredHeight, params.topMargin(), params.bottomMargin(), params.gravity)
            childBottom = childTop + mPageHeaderView!!.measuredHeight
            childLeft = baseLeft + params.leftMargin()
            childRight = childLeft + mPageHeaderView!!.measuredWidth
            baseLeft = childRight + params.rightMargin()
            mPageHeaderView!!.layout(childLeft, childTop, childRight, childBottom)
        }

        if (hasPageFooterView()) {
            val params = mPageFooterView!!.layoutParams as BaseViewGroup.LayoutParams
            childTop = getContentStartV(Math.max(baseTop, paddingTopWithInset), Math.min(baseBottom, height - paddingBottomWithInset), mPageFooterView!!.measuredHeight, params.topMargin(), params.bottomMargin(), params.gravity)
            childBottom = childTop + mPageFooterView!!.measuredHeight
            childRight = baseRight - params.rightMargin()
            childLeft = childRight - mPageFooterView!!.measuredWidth
            baseRight = childLeft - params.leftMargin()
            mPageFooterView!!.layout(childLeft, childTop, childRight, childBottom)
        }

        val count = childCount
        val mMiddleMargin = mBorderDivider!!.contentMarginVertical
        childTop = baseTop
        for (i in 0..count - 1) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, true)) continue
            val params = child.layoutParams as BaseViewGroup.LayoutParams
            childTop += params.topMargin()
            childBottom = childTop + child.measuredHeight
            childLeft = getContentStartH(baseLeft, baseRight, child.measuredWidth, params.leftMargin(), params.rightMargin(), if (isChildCenter) Gravity.CENTER else params.gravity)
            childRight = childLeft + child.measuredWidth
            child.layout(childLeft, childTop, childRight, childBottom)
            childTop = childBottom + params.bottomMargin() + mMiddleMargin
        }
    }

    protected fun onLayoutHorizontal(baseLeft: Int, baseTop: Int, baseRight: Int, baseBottom: Int) {
        var baseTop = baseTop
        var baseBottom = baseBottom
        var childLeft: Int
        var childTop: Int
        var childRight: Int
        var childBottom: Int
        if (hasPageHeaderView()) {
            val params = mPageHeaderView!!.layoutParams as BaseViewGroup.LayoutParams
            childLeft = getContentStartH(Math.max(baseLeft, paddingLeftWithInset), Math.min(baseRight, width - paddingRightWithInset), mPageHeaderView!!.measuredWidth, params.leftMargin(), params.rightMargin(), params.gravity)
            childRight = childLeft + mPageHeaderView!!.measuredWidth
            childTop = baseTop + params.topMargin()
            childBottom = childTop + mPageHeaderView!!.measuredHeight
            baseTop = childBottom + params.bottomMargin()
            mPageHeaderView!!.layout(childLeft, childTop, childRight, childBottom)
        }

        if (hasPageFooterView()) {
            val params = mPageFooterView!!.layoutParams as BaseViewGroup.LayoutParams
            childLeft = getContentStartH(Math.max(baseLeft, paddingLeftWithInset), Math.min(baseRight, width - paddingRightWithInset), mPageFooterView!!.measuredWidth, params.leftMargin(), params.rightMargin(), params.gravity)
            childRight = childLeft + mPageFooterView!!.measuredWidth
            childBottom = baseBottom - params.bottomMargin()
            childTop = childBottom - mPageFooterView!!.measuredHeight
            baseBottom = childTop - params.topMargin()
            mPageFooterView!!.layout(childLeft, childTop, childRight, childBottom)
        }

        val count = childCount
        val mMiddleMargin = mBorderDivider!!.contentMarginHorizontal
        childLeft = baseLeft
        for (i in 0..count - 1) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, true)) continue
            val params = child.layoutParams as BaseViewGroup.LayoutParams
            childLeft += params.leftMargin()
            childRight = childLeft + child.measuredWidth
            childTop = getContentStartV(baseTop, baseBottom, child.measuredHeight, params.topMargin(), params.bottomMargin(), if (isChildCenter) Gravity.CENTER else params.gravity)
            childBottom = childTop + child.measuredHeight
            child.layout(childLeft, childTop, childRight, childBottom)
            childLeft = childRight + params.bottomMargin() + mMiddleMargin
        }
    }

    override fun doBeforeDraw(canvas: Canvas, inset: Rect) {
        val swapIndexEnable = mFloatViewStartIndex >= 0 && mSwapViewIndex >= 0
        if (swapIndexEnable && isChildrenDrawingOrderEnabled == false) {
            isChildrenDrawingOrderEnabled = true
        } else {
            if (swapIndexEnable == false) {
                isChildrenDrawingOrderEnabled = false
            }
        }
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        if (mFloatViewStartIndex >= 0 && mSwapViewIndex >= 0) {
            if (mFloatViewStartIndex == i) {
                return mSwapViewIndex
            }
            if (i == mSwapViewIndex) {
                return mFloatViewStartIndex
            }
        }
        return i
    }

    override fun cancelTouch(resetToIdle: Boolean) {
        var fling = 0
        if (isViewPagerStyle && ScrollLayout.SCROLL_STATE_IDLE != scrollState) {
            fling = flingToWhere(0, 0, isOrientationHorizontal)
        }
        super.cancelTouch(resetToIdle && fling == 0)
    }

    public override fun fling(movedX: Int, movedY: Int, velocityX: Int, velocityY: Int): Boolean {
        if (isViewPagerStyle && ScrollLayout.SCROLL_STATE_IDLE != scrollState) {
            val horizontal = isOrientationHorizontal
            val moved = if (horizontal) movedX else movedY
            val velocity = if (horizontal) velocityX else velocityY
            return flingToWhere(moved, velocity, horizontal) > 0
        }
        return super.fling(movedX, movedY, velocityX, velocityY)
    }

    private fun flingToWhere(moved: Int, velocity: Int, horizontal: Boolean): Int {
        var velocity = velocity
        val scrolled = if (horizontal) scrollX else scrollY
        val willScroll: Int
        if (velocity == 0) {
            velocity = -Math.signum(moved.toFloat()).toInt()
        }
        var targetIndex = mCurrItem
        val itemSize = if (horizontal) getChildAt(mCurrItem).width else getChildAt(mCurrItem).height
        val absVelocity = if (velocity > 0) velocity else -velocity
        val pageItemCount = itemViewCount
        if (Math.abs(moved) > mTouchSlop) {
            val halfItemSize = itemSize / 2
            if (absVelocity > mMinFlingVelocity) {
                if (velocity > 0 && mCurrItem < pageItemCount - 1 && velocity / 10 - moved > halfItemSize) {
                    targetIndex++
                }
                if (velocity < 0 && mCurrItem > 0 && moved - velocity / 10 > halfItemSize) {
                    targetIndex--
                }
            } else {
                if (moved > halfItemSize && mCurrItem > 0) {
                    targetIndex--
                }
                if (moved < -halfItemSize && mCurrItem < pageItemCount - 1) {
                    targetIndex++
                }
            }
        }
        val targetScroll = computeScrollOffset(targetIndex, 0, true, horizontal)
        willScroll = targetScroll - scrolled
        if (willScroll != 0) {
            scrollToCentre(targetIndex, 0, -1)
        }
        return willScroll
    }


    override fun scrollTo(index: Int, offset: Int, duration: Int) {
        scrollToItem(index, offset, duration, false)
    }

    fun scrollToCentre(index: Int, offset: Int, duration: Int) {
        scrollToItem(index, offset, duration, true)
    }

    fun scrollTo(child: View, offset: Int, duration: Int, centerInParent: Boolean) {
        val pageIndex = indexOfItemView(child)
        if (pageIndex != -1) {
            scrollToItem(pageIndex, offset, duration, centerInParent)
        }
    }

    private fun scrollToItem(index: Int, offset: Int, duration: Int, centerInParent: Boolean) {
        val okX = isOrientationHorizontal
        val okY = isOrientationVertical
        val x = if (okX) offset else scrollX
        val y = if (okY) offset else scrollY
        setCurrentItem(index)
        scrollToItem(index, duration, x, y, okX, okY, centerInParent)
    }

    protected fun computeScrollOffset(child: View?, offset: Int, centreWithParent: Boolean, horizontal: Boolean): Int {
        val scrollRange: Int
        val targetScroll: Int
        if (horizontal) {
            targetScroll = offsetX(child!!, centreWithParent, true) + offset
            scrollRange = horizontalScrollRange
        } else {
            targetScroll = offsetY(child!!, centreWithParent, true) + offset
            scrollRange = verticalScrollRange
        }
        return Math.max(0, Math.min(scrollRange, targetScroll))
    }

    protected fun computeScrollOffset(childPosition: Int, offset: Int, centreWithParent: Boolean, horizontal: Boolean): Int {
        val child = getVirtualChildAt(childPosition, true)
        return if (child == null) 0 else computeScrollOffset(child, offset, centreWithParent, horizontal)
    }

    override fun formatDuration(duration: Int): Int {
        return if (isViewPagerStyle) Math.max(0, Math.min(duration, 800)) else super.formatDuration(duration)
    }

    private fun enableLayers(enable: Boolean) {
        val childCount = childCount
        val layerType = if (enable) ViewCompat.LAYER_TYPE_HARDWARE else ViewCompat.LAYER_TYPE_NONE
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (child !== mPageHeaderView && child !== mPageFooterView) {
                ViewCompat.setLayerType(child, layerType, null)
            }
        }
    }

    private fun setCurrentItem(willItem: Int): Boolean {
        if (mCurrItem != willItem || prevItem == -1) {
            val preItem = if (mCurrItem == willItem) prevItem else mCurrItem
            prevItem = mCurrItem
            mCurrItem = willItem
            if (isLogAccess) {
                print("select", String.format("selectChange  $$$$:%d >>>>>>>>> %d", preItem, mCurrItem))
            }
            if (pageChangeListener != null) {
                pageChangeListener!!.onPageSelected(willItem, preItem)
            }
            return true
        }
        return false
    }

    override fun onScrollStateChanged(newState: Int, prevState: Int) {
        if (pageChangeListener != null) {
            pageChangeListener!!.onScrollStateChanged(newState, prevState)
        }
        if (mPageTransformer != null) {
            // PageTransformers can do complex things that benefit from hardware layers.
            enableLayers(newState != ScrollLayout.SCROLL_STATE_IDLE)
        }
    }

    override fun onScrollChanged(scrollX: Int, scrollY: Int, visibleBounds: Rect, fromScrollChanged: Boolean) {
        mNeedResolveFloatOffset = false
        val horizontal = isOrientationHorizontal
        val scrolled = if (horizontal) scrollX else scrollY
        resolveVisiblePosition(scrolled, horizontal)
        if (mFloatViewStartIndex >= 0) {
            mSwapViewIndex = computeSwapViewIndex(scrolled, horizontal)
        }
        if (mPageHeaderView != null || mPageFooterView != null) {
            updatePositionForHeaderAndFooter(scrolled, horizontal)
        }
        if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
            updatePositionForFloatView(scrolled, horizontal)
        }
        if (pageChangeListener != null || mPageTransformer != null) {
            resolvePageOffset(scrolled, horizontal)
        }
        if (isLogAccess) {
            val sb = StringBuilder(32)
            sb.append("scrollX=").append(scrollX)
            sb.append(",scrollY=").append(scrollY).append(",visibleBounds=").append(visibleBounds)
            sb.append(",scrollChanged=").append(fromScrollChanged)
            print("scroll", sb)
        }
    }

    protected fun computeSwapViewIndex(scrolled: Int, horizontal: Boolean): Int {
        if (mFloatViewStartIndex >= 0) {
            val count = childCount
            val baseLine: Int
            val view = getChildAt(mFloatViewStartIndex)
            baseLine = (if (horizontal) view.right else view.bottom) + scrolled
            for (i in mFloatViewStartIndex + 1..count - 1) {
                val child = getChildAt(i)
                if (skipVirtualChild(child, true))
                    continue
                if (horizontal) {
                    if (child.right >= baseLine) {
                        return i
                    }
                } else {
                    if (child.bottom >= baseLine) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    protected fun resetPositionForFloatView(realChildIndex: Int, horizontal: Boolean) {
        val child = if (realChildIndex >= 0) getChildAt(realChildIndex) else null
        if (child != null) {
            child.translationX = 0f
            child.translationY = 0f
        }
    }

    private fun updatePositionForHeaderAndFooter(scrolled: Int, horizontal: Boolean) {
        if (mPageHeaderView != null && mPageHeaderView!!.parent === this) {
            if (horizontal) {
                mPageHeaderView!!.translationX = scrolled.toFloat()
            } else {
                mPageHeaderView!!.translationY = scrolled.toFloat()
            }
        }
        if (mPageFooterView != null && mPageFooterView!!.parent === this) {
            if (horizontal) {
                mPageFooterView!!.translationX = scrolled.toFloat()
            } else {
                mPageFooterView!!.translationY = scrolled.toFloat()
            }
        }
    }

    private fun updatePositionForFloatView(scrolled: Int, horizontal: Boolean) {
        var viewTranslated: Float
        var wantTranslated: Int
        //TODO FLOAT MARGIN SHOULD MAKE AS A ATTRIBUTION.
        if (mFloatViewStartMode == FLOAT_VIEW_SCROLL) {
            val view = getItemView(mFloatViewStartIndex)
            if (horizontal) {
                wantTranslated = scrolled - view!!.left
                viewTranslated = view.translationX
            } else {
                wantTranslated = scrolled - view!!.top
                viewTranslated = view.translationY
            }
            wantTranslated = Math.max(0, wantTranslated)
            if (wantTranslated.toFloat() != viewTranslated) {
                if (horizontal) {
                    view.translationX = wantTranslated.toFloat()
                } else {
                    view.translationY = wantTranslated.toFloat()
                }
            }
        }
        if (mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
            val view = getItemView(mFloatViewEndIndex)
            val scrollRange: Int
            if (horizontal) {
                scrollRange = horizontalScrollRange
                wantTranslated = scrolled - scrollRange + (contentWidth - view!!.right)
                viewTranslated = view.translationX
            } else {
                scrollRange = verticalScrollRange
                wantTranslated = scrolled - scrollRange + (contentHeight - view!!.bottom)
                viewTranslated = view.translationY
            }
            wantTranslated = Math.min(0, wantTranslated)
            if (wantTranslated.toFloat() != viewTranslated) {
                if (horizontal) {
                    view.translationX = wantTranslated.toFloat()
                } else {
                    view.translationY = wantTranslated.toFloat()
                }
            }
        }
    }

    private fun resolveVisiblePosition(scrolled: Int, horizontal: Boolean) {
        val visibleStart: Int
        val visibleEnd: Int
        val visibleBounds = visibleContentBounds
        if (horizontal) {
            visibleStart = visibleBounds.left
            visibleEnd = visibleBounds.right
        } else {
            visibleStart = visibleBounds.top
            visibleEnd = visibleBounds.bottom
        }
        val childCount = childCount
        var counted = 0
        var firstVisible = -1
        var lastVisible = -1
        var visible: Boolean
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, true)) continue
            if (horizontal) {
                visible = !(child.right <= visibleStart || child.left >= visibleEnd)
            } else {
                visible = !(child.bottom <= visibleStart || child.top >= visibleEnd)
            }
            if (visible) {
                if (firstVisible == -1) {
                    firstVisible = counted
                }
                lastVisible = counted
            } else {
                if (firstVisible >= 0) {
                    break
                }
            }
            counted++
        }
        if (firstVisible != -1) {
            if (firstVisible != mFirstVisiblePosition || lastVisible != mLastVisiblePosition) {
                val oldFirstVisible = mFirstVisiblePosition
                val oldLastVisible = mLastVisiblePosition
                mFirstVisiblePosition = firstVisible
                mLastVisiblePosition = lastVisible
                if (isLogAccess) {
                    print("range", String.format("visibleRangeChanged  ****:[%d , %d]", firstVisible, lastVisible))
                }
                if (visibleRangeChangeListener != null) {
                    visibleRangeChangeListener!!.onVisibleRangeChanged(firstVisible, lastVisible, oldFirstVisible, oldLastVisible)
                }
            }
        }
    }

    private fun resolvePageOffset(scrolled: Int, horizontal: Boolean) {
        val targetOffset = computeScrollOffset(mCurrItem, 0, true, horizontal)
        var prevIndex = mCurrItem
        val itemCount = itemViewCount
        if (scrolled > targetOffset && prevIndex < itemCount - 1) {
            prevIndex++
        }
        if (scrolled < targetOffset && prevIndex > 0) {
            prevIndex--
        }
        val minIndex: Int
        val maxIndex: Int
        val minOffset: Int
        val maxOffset: Int
        if (prevIndex > mCurrItem) {
            minIndex = mCurrItem
            minOffset = targetOffset
            maxIndex = prevIndex
            maxOffset = if (maxIndex == minIndex) minOffset else computeScrollOffset(maxIndex, 0, true, horizontal)
        } else {
            maxIndex = mCurrItem
            maxOffset = targetOffset
            minIndex = prevIndex
            minOffset = if (minIndex == maxIndex) maxOffset else computeScrollOffset(minIndex, 0, true, horizontal)
        }
        val distance = maxOffset - minOffset
        var positionOffsetPixels = 0
        var positionOffset = 0f
        if (distance > 0) {
            positionOffsetPixels = scrolled - minOffset
            positionOffset = positionOffsetPixels / distance.toFloat()
        }
        if (pageChangeListener != null) {
            pageChangeListener!!.onPageScrolled(minIndex, positionOffset, positionOffsetPixels)
        }
        if (mPageTransformer != null) {
            dispatchTransformPosition(scrolled, itemCount, horizontal)
        }
    }

    private fun dispatchTransformPosition(scrolled: Int, itemCount: Int, horizontal: Boolean) {
        val childCount = childCount
        var pageItemIndex = 0
        val mMiddleMargin = if (horizontal) mBorderDivider!!.contentMarginHorizontal else mBorderDivider!!.contentMarginVertical
        val pageItemStart = Math.max(0, mFirstVisiblePosition - 1)
        val pageItemEnd = Math.min(itemCount - 1, mLastVisiblePosition + 1)
        var i = 0
        while (i < childCount && pageItemIndex <= pageItemEnd) {
            val child = getChildAt(i)
            if (skipVirtualChild(child, true)) {
                i++
                continue
            }
            if (pageItemIndex >= pageItemStart) {
                val params = child.layoutParams as BaseViewGroup.LayoutParams
                var contentLength = if (horizontal) params.width(child) else params.height(child)
                if (mMiddleMargin > 0) {
                    if (pageItemIndex == 0 || pageItemIndex == itemCount - 1) {
                        contentLength += mMiddleMargin / 2
                    } else {
                        contentLength += mMiddleMargin
                    }
                }
                val transformerPosition = (scrolled - computeScrollOffset(child, 0, true, horizontal)) / contentLength.toFloat()
                mPageTransformer!!.transformPage(child, transformerPosition, horizontal)
            }
            pageItemIndex++
            i++
        }
    }

    override fun removeAllViewsInLayout() {
        super.removeAllViewsInLayout()
        mFirstVisiblePosition = -1
        mLastVisiblePosition = -1
        mCurrItem = 0
        prevItem = -1
    }

    override fun skipVirtualChild(child: View?, withoutGone: Boolean): Boolean {
        return super.skipVirtualChild(child, withoutGone) || child === mPageHeaderView || child === mPageFooterView
    }

    interface PageTransformer {
        fun transformPage(view: View, position: Float, horizontal: Boolean)

        fun recoverTransformPage(view: View, horizontal: Boolean)
    }

    interface OnPageChangeListener : ScrollLayout.OnScrollChangeListener {

        fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)

        fun onPageSelected(position: Int, oldPosition: Int)

    }

    interface OnVisibleRangeChangeListener {
        fun onVisibleRangeChanged(firstVisible: Int, lastVisible: Int, oldFirstVisible: Int, oldLastVisible: Int)
    }

    companion object {
        private val FLOAT_VIEW_SCROLL = 1
        private val sPairPools = Pools.SimplePool<PointF>(8)
    }
}
