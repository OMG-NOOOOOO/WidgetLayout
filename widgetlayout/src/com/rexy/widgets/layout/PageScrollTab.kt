package com.rexy.widgets.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Typeface
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import android.widget.Checkable
import android.widget.TextView
import com.rexy.widgetlayout.R
import com.rexy.widgets.adapter.ItemProvider
import com.rexy.widgets.view.CheckText
import java.util.*


class PageScrollTab constructor(context: Context, attrs: AttributeSet) : PageScrollView(context, attrs) {

    /**
     * 以下和
     */
    var selectedPosition = 0
        private set //当前选中的tab 索引。
    private var mCurrentPositionOffset = 0f//选中tab 的偏移量子。


    /**
     * 以下是设置tab item 的最小padding 值。
     */
    private var mItemMinPaddingHorizontal = 10
    private var mItemMinPaddingTop = 0
    private var mItemMinPaddingBottom = 0

    /**
     * tab item 的背景
     */
    private var mItemBackgroundFirst = 0
    private var mItemBackground = 0
    private var mItemBackgroundLast = 0
    private var mItemBackgroundFull = 0

    /**
     * 如果item 是 TextView 会应用以下属性。
     */

    private var mTextAllCaps = false
    private var mTextTypeFace: Typeface? = null
    private var mTextTypefaceStyle = Typeface.NORMAL
    private var mTextSize = 14
    private var mTextColor = 0xFF666666.toInt()
    private var mTextColorResId = 0

    private val mRectPaint: Paint
    private val mDividerPaint: Paint

    /**
     * item 之间垂直分割线。
     */
    private var mDividerWidth = 1
    private var mDividerPadding = 6
    private var mDividerColor = 0x1A000000


    /**
     * 选中item 底部指示线。
     */
    private var mIndicatorHeight = 2
    private var mIndicatorOffset = 0
    private var mIndicatorColor = 0xffff9500.toInt()
    private var mIndicatorWidthPercent = 1f

    /**
     * 顶部水平分界线。
     */
    private var mTopLineHeight = 0
    private var mTopLineColor = 0xffd8e2e9.toInt()

    /**
     * 底部水平分界线
     */
    private var mBottomLineHeight = 0
    private var mBottomLineColor = 0x1A000000

    private var mLocalInfo: Locale? = null
    var isAutoCheckState = true
    private var mPreCheckView: View? = null
    private val mItemLayoutParams: LayoutParams

    //c
    private var mViewPager: ViewPager? = null
    private val mViewPageListener = PageListener()
    var mDelegatePageListener: ViewPager.OnPageChangeListener? = null

    private var mITabProvider: ItemProvider? = null
    private var mTabClick: ITabClickEvent? = null
    private val mTabItemClick = View.OnClickListener { view ->
        val tag = view?.getTag(TAB_INDEX)
        val cur = tag as? Int ?: selectedPosition
        val pre = selectedPosition
        val handled = if (mTabClick == null) false else mTabClick!!.onTabClicked(this@PageScrollTab, view!!, cur, mPreCheckView, pre)
        handTabClick(cur, pre, handled)
    }

    private fun handTabClick(cur: Int, pre: Int, handled: Boolean) {
        if (!handled) {
            if (cur != pre) {
                if (mViewPager != null) {
                    mViewPager!!.currentItem = cur
                } else {
                    setSelectedTab(cur, false, false)
                }
            }
        } else {
            selectedPosition = cur
        }
    }

    init {
        setWillNotDraw(false)
        orientation = BaseViewGroup.Companion.HORIZONTAL
        isChildFillParent=true
        val dm = resources.displayMetrics
        mIndicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mIndicatorHeight.toFloat(), dm).toInt()
        mTopLineHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mTopLineHeight.toFloat(), dm).toInt()
        mBottomLineHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mBottomLineHeight.toFloat(), dm).toInt()
        mDividerPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mDividerPadding.toFloat(), dm).toInt()
        mItemMinPaddingHorizontal = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemMinPaddingHorizontal.toFloat(), dm).toInt()
        mItemMinPaddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemMinPaddingTop.toFloat(), dm).toInt()
        mItemMinPaddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemMinPaddingBottom.toFloat(), dm).toInt()
        mDividerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerWidth.toFloat(),
                dm).toInt()
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize.toFloat(), dm).toInt()
        // get system attrs (android:textSize and android:textColor)
        var a = context.obtainStyledAttributes(attrs, ATTRS)

        mTextSize = a.getDimensionPixelSize(0, mTextSize)
        mTextColor = a.getColor(1, mTextColor)

        a.recycle()

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.PageScrollTab)
        mItemBackground = a.getResourceId(R.styleable.PageScrollTab_tabItemBackground,
                mItemBackground)
        mItemBackgroundFirst = a.getResourceId(R.styleable.PageScrollTab_tabItemBackgroundFirst,
                mItemBackgroundFirst)
        mItemBackgroundLast = a.getResourceId(R.styleable.PageScrollTab_tabItemBackgroundLast,
                mItemBackgroundLast)
        mItemBackgroundFull = a.getResourceId(R.styleable.PageScrollTab_tabItemBackgroundFull,
                mItemBackgroundFull)


        mIndicatorColor = a.getColor(R.styleable.PageScrollTab_tabIndicatorColor,
                mIndicatorColor)
        mIndicatorHeight = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabIndicatorHeight, mIndicatorHeight)
        mIndicatorOffset = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabIndicatorOffset, mIndicatorOffset)
        val atrIndicatorWidthPercent = a.getFloat(R.styleable.PageScrollTab_tabIndicatorWidthPercent, mIndicatorWidthPercent)


        mTopLineColor = a.getColor(R.styleable.PageScrollTab_tabTopLineColor,
                mTopLineColor)
        mTopLineHeight = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabTopLineHeight, mTopLineHeight)

        mBottomLineColor = a.getColor(R.styleable.PageScrollTab_tabBottomLineColor,
                mBottomLineColor)
        mBottomLineHeight = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabBottomLineHeight, mBottomLineHeight)


        mDividerColor = a.getColor(R.styleable.PageScrollTab_tabItemDividerColor, mDividerColor)
        mDividerWidth = a.getDimensionPixelSize(R.styleable.PageScrollTab_tabItemDividerWidth, mDividerWidth)
        mDividerPadding = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabItemDividerPadding, mDividerPadding)


        mItemMinPaddingHorizontal = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabItemMinPaddingHorizontal, mItemMinPaddingHorizontal)
        mItemMinPaddingTop = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabItemMinPaddingTop, mItemMinPaddingTop)
        mItemMinPaddingBottom = a.getDimensionPixelSize(
                R.styleable.PageScrollTab_tabItemMinPaddingBottom, mItemMinPaddingBottom)

        mTextAllCaps = a.getBoolean(R.styleable.PageScrollTab_tabItemTextCaps, mTextAllCaps)
        mTextColorResId = a.getResourceId(R.styleable.PageScrollTab_tabItemTextColor,
                mTextColorResId)
        a.recycle()

        mRectPaint = Paint()
        mRectPaint.isAntiAlias = true
        mRectPaint.style = Style.FILL

        mDividerPaint = Paint()
        mDividerPaint.isAntiAlias = true
        mDividerPaint.strokeWidth = mDividerWidth.toFloat()

        if (mLocalInfo == null) {
            mLocalInfo = resources.configuration.locale
        }
        mItemLayoutParams = LayoutParams(-2, -2, Gravity.CENTER_VERTICAL)

        gravity = Gravity.CENTER_VERTICAL or gravity
        setIndicatorWidthPercent(atrIndicatorWidthPercent)
    }

    val tabItemCount: Int
        get() {
            if (mITabProvider != null) {
                return mITabProvider!!.count
            }
            return if (mViewPager != null && mViewPager!!.adapter != null) {
                mViewPager!!.adapter.count
            } else itemViewCount
        }

    val tabProvider: ItemProvider?
        get() {
            if (mITabProvider != null) {
                return mITabProvider
            }
            return if (mViewPager != null && mViewPager!!.adapter is ItemProvider) {
                mViewPager!!.adapter as ItemProvider
            } else null
        }

    fun setViewPager(pager: ViewPager?) {
        mViewPager = pager
        val adp = pager?.adapter
        if (adp != null) {
            if (adp is ItemProvider) {
                mITabProvider = adp
            }
            pager.setOnPageChangeListener(mViewPageListener)
        }
        notifyDataSetChanged()
    }

    fun setTabProvider(provider: ItemProvider, currentPosition: Int) {
        mITabProvider = provider
        this.selectedPosition = currentPosition
        notifyDataSetChanged()
    }

    fun setTabClickListener(l: ITabClickEvent) {
        mTabClick = l
    }

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        this.mDelegatePageListener = listener
    }

    fun notifyDataSetChanged() {
        removeAllViews()
        val tabItemCount = tabItemCount
        val accessToTabProvider = mITabProvider != null
        val accessToViewPage = mViewPager != null
        val isViewTab: Boolean
        if (!accessToTabProvider && !accessToViewPage) {
            return
        } else {
            isViewTab = accessToTabProvider && mITabProvider is ItemProvider.ViewProvider
        }
        for (i in 0..tabItemCount - 1) {
            if (isViewTab) {
                addTab(i, (mITabProvider as ItemProvider.ViewProvider).getView(i, null, this@PageScrollTab))
            } else {
                val label = if (accessToTabProvider) mITabProvider!!.getTitle(i) else mViewPager!!.adapter.getPageTitle(i)
                addTextTab(i, label)
            }
        }
        updateTabStyles()
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                if (mViewPager != null) {
                    selectedPosition = mViewPager!!.currentItem
                }
                val n = itemViewCount
                if (selectedPosition >= 0 && selectedPosition < n) {
                    mPreCheckView = getVirtualChildAt(selectedPosition, true)
                    if (mPreCheckView is Checkable && mPreCheckView!!.isEnabled) {
                        (mPreCheckView as Checkable).isChecked = true
                    }
                    scrollToChild(selectedPosition, 0, false)
                } else {
                    scrollToChild(0, 0, false)
                }
            }
        })
    }

    private fun addTextTab(position: Int, title: CharSequence) {
        val tab = CheckText(context)
        tab.isEnabled = true
        tab.text = title
        tab.gravity = Gravity.CENTER
        tab.setSingleLine()
        tab.includeFontPadding = false
        addTab(position, tab)
    }

    private fun addTab(position: Int, tab: View) {
        tab.isFocusable = true
        tab.setTag(TAB_INDEX, position)
        tab.setOnClickListener(mTabItemClick)
        val left = Math.max(mItemMinPaddingHorizontal, tab.paddingLeft)
        val top = Math.max(mItemMinPaddingTop, tab.paddingTop)
        val right = Math.max(mItemMinPaddingHorizontal, tab.paddingRight)
        val bottom = Math.max(mItemMinPaddingBottom, tab.paddingBottom)
        tab.setPadding(left, top, right, bottom)
        if (tab.layoutParams == null) {
            addView(tab, position, mItemLayoutParams)
        } else {
            addView(tab, position)
        }
    }

    fun addTabItem(title: CharSequence, updateStyle: Boolean) {
        addTextTab(childCount, title)
        if (updateStyle) {
            updateTabStyles()
        }
    }

    private fun updateTabStyles() {
        val itemCount = childCount
        val hasMutiBackground = mItemBackgroundFirst != 0 && mItemBackgroundLast != 0
        for (i in 0 until itemCount) {
            var backgroundRes = mItemBackground
            val v = getChildAt(i)
            if (hasMutiBackground) {
                if (i == 0) {
                    if (itemCount == 1) {
                        if (mItemBackgroundFull != 0) {
                            backgroundRes = mItemBackgroundFull
                        }
                    } else {
                        backgroundRes = mItemBackgroundFirst
                    }
                } else if (i == itemCount - 1) {
                    backgroundRes = mItemBackgroundLast
                }
            }
            if (backgroundRes != 0) {
                v.setBackgroundResource(backgroundRes)
            }
            if (v is TextView) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
                v.setTypeface(mTextTypeFace, mTextTypefaceStyle)
                if (mTextColorResId != 0) {
                    v.setTextColor(context.resources.getColorStateList(mTextColorResId))
                } else {
                    v.setTextColor(mTextColor)
                }
                // setAllCaps() is only available from API 14, so the upper case
                // is made manually if we are on a
                // pre-ICS-build
                if (mTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        v.setAllCaps(true)
                    } else {
                        v.text = v.text.toString().toUpperCase(mLocalInfo!!)
                    }
                }
            }
        }
    }

    private fun scrollToChild(position: Int, offset: Int, anim: Boolean) {
        scrollToCentre(position, offset, if (anim) -1 else 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val itemCount = tabItemCount
        if (isInEditMode || itemCount == 0) {
            return
        }
        val width = width
        var height = height
        val scrollX = scrollX

        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.color = mDividerColor
            val dividerXOffset = mDividerPaint.strokeWidth / 2
            for (i in 0 until itemCount - 1) {
                val tab = getVirtualChildAt(i, true)
                val startX = tab!!.right + dividerXOffset
                canvas.drawLine(startX, mDividerPadding.toFloat(), startX, (height - mDividerPadding).toFloat(), mDividerPaint)
            }
        }

        // draw top or bottom line.
        if (mBottomLineHeight > 0) {
            mRectPaint.color = mBottomLineColor
            canvas.drawRect(scrollX.toFloat(), (height - mBottomLineHeight).toFloat(), (width + scrollX).toFloat(), height.toFloat(), mRectPaint)
        }
        if (mTopLineHeight > 0) {
            mRectPaint.color = mTopLineColor
            canvas.drawRect(scrollX.toFloat(), 0f, (width + scrollX).toFloat(), mTopLineHeight.toFloat(), mRectPaint)
        }

        // draw indicator line
        if (mIndicatorHeight > 0 && mIndicatorWidthPercent > 0) {
            mRectPaint.color = mIndicatorColor
            // default: line below current tab
            val currentTab = getVirtualChildAt(selectedPosition, true)
            var lineLeft = currentTab!!.left.toFloat()
            var lineRight = currentTab.right.toFloat()
            // if there is an offset, start interpolating left and right coordinates   between current and next tab
            if (mCurrentPositionOffset > 0f && selectedPosition < itemCount - 1) {
                val nextTab = getVirtualChildAt(selectedPosition + 1, true)
                val nextTabLeft = nextTab!!.left.toFloat()
                val nextTabRight = nextTab.right.toFloat()
                lineLeft = mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft
                lineRight = mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight
            }
            if (mIndicatorOffset != 0) {
                height -= mIndicatorOffset
            }
            if (mIndicatorWidthPercent >= 1) {
                canvas.drawRect(lineLeft, (height - mIndicatorHeight).toFloat(), lineRight, height.toFloat(), mRectPaint)
            } else {
                val offsetSize = (lineRight - lineLeft) * (1 - mIndicatorWidthPercent)
                canvas.drawRect(lineLeft + offsetSize, (height - mIndicatorHeight).toFloat(), lineRight - offsetSize, height.toFloat(), mRectPaint)
            }
            // draw underline
        }
    }

    private inner class PageListener : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            callPageScrolled(position, positionOffset)
            if (mDelegatePageListener != null) {
                mDelegatePageListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            callPageScrollStateChanged(state, mViewPager!!.currentItem)
            if (mDelegatePageListener != null) {
                mDelegatePageListener!!.onPageScrollStateChanged(state)
            }
        }

        override fun onPageSelected(position: Int) {
            callPageSelected(position)
            if (mDelegatePageListener != null) {
                mDelegatePageListener!!.onPageSelected(position)
            }
        }
    }


    fun callPageScrolled(position: Int, positionOffset: Float) {
        selectedPosition = position
        mCurrentPositionOffset = positionOffset
        scrollToChild(position, (positionOffset * getVirtualChildAt(position, true)!!.width).toInt(), false)
        invalidate()
    }

    fun callPageSelected(position: Int) {
        setSelectedTab(position, true)
    }

    fun callPageScrollStateChanged(state: Int, viewPageItem: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (viewPageItem != selectedPosition) {
                selectedPosition = viewPageItem
                mCurrentPositionOffset = 0f
            }
            scrollToChild(viewPageItem, 0, false)
        }
    }

    fun setSelectedTab(position: Int, fromViewPageListener: Boolean, animToCur: Boolean) {
        setSelectedTab(position, fromViewPageListener)
        scrollToChild(selectedPosition, 0, animToCur)
    }

    protected fun setSelectedTab(position: Int, fromViewPageListener: Boolean) {
        if (!fromViewPageListener) {
            selectedPosition = position
            mCurrentPositionOffset = 0f
        }
        val v = getVirtualChildAt(position, true)
        if (mPreCheckView == null || mPreCheckView !== v) {
            if (isAutoCheckState) {
                if (mPreCheckView is Checkable) {
                    (mPreCheckView as Checkable).isChecked = false
                }
            }
            mPreCheckView = v
            if (v is Checkable) {
                (v as Checkable).isChecked = true
            }
        }
        invalidate()
    }

    fun setCheckedAtPosition(pos: Int, checked: Boolean): Boolean {
        var pos = pos
        if (pos < 0) {
            pos = selectedPosition
        }
        val itemCount = tabItemCount
        if (pos in 0..(itemCount - 1)) {
            val v = getVirtualChildAt(pos, true)
            if (v is Checkable) {
                val cv = v as Checkable
                if (cv.isChecked != checked) {
                    cv.isChecked = checked
                    return true
                }
            }
        }
        return false
    }

    val selectedView: View?
        get() = if (selectedPosition in 0..(tabItemCount - 1)) {
            getVirtualChildAt(selectedPosition, true)
        } else null

    fun <T : View> findTabViewByClass(cls: Class<T>, from: Int, endExclude: Int): SparseArray<T> {
        val size = childCount
        var start = if (from > 0) from else 0
        val end = if (endExclude <= 0 || endExclude > size) size else endExclude
        val result = SparseArray<T>(size + 1)
        while (start < end) {
            val itemView = getChildAt(start)
            if (cls.isAssignableFrom(itemView.javaClass)) {
                result.put(start, itemView as T)
            }
            start++
        }
        return result
    }

    fun setIndicatorWidthPercent(widthPercent: Float) {
        var widthPercent = widthPercent
        if (widthPercent != mIndicatorWidthPercent) {
            if (widthPercent >= 1) {
                widthPercent = 1f
            }
            if (widthPercent < 0) {
                widthPercent = 0f
            }
            if (widthPercent != mIndicatorWidthPercent) {
                mIndicatorWidthPercent = widthPercent
                invalidate()
            }
        }
    }

    fun setIndicatorOffset(indicatorOffsetPx: Int) {
        this.mIndicatorOffset = indicatorOffsetPx
        invalidate()
    }

    fun setIndicatorHeight(indicatorLineHeightPx: Int) {
        this.mIndicatorHeight = indicatorLineHeightPx
        invalidate()
    }

    fun setIndicatorColor(indicatorColor: Int) {
        this.mIndicatorColor = indicatorColor
        invalidate()
    }

    fun setIndicatorColorId(resId: Int) {
        this.mIndicatorColor = resources.getColor(resId)
        invalidate()
    }

    fun setDividerWidth(dividerWidth: Int) {
        this.mDividerWidth = dividerWidth
        invalidate()
    }

    fun setDividerPadding(dividerPaddingPx: Int) {
        this.mDividerPadding = dividerPaddingPx
        invalidate()
    }

    fun setDividerColor(dividerColor: Int) {
        this.mDividerColor = dividerColor
        invalidate()
    }

    fun setDividerColorId(resId: Int) {
        this.mDividerColor = resources.getColor(resId)
        invalidate()
    }

    fun setTopLineHeight(topLineHeightPx: Int) {
        this.mTopLineHeight = topLineHeightPx
        invalidate()
    }

    fun setTopLineColor(color: Int) {
        this.mTopLineColor = color
        invalidate()
    }

    fun setTopLineColorId(resId: Int) {
        this.mTopLineColor = resources.getColor(resId)
        invalidate()
    }

    fun setBottomLineHeight(underlineHeightPx: Int) {
        this.mBottomLineHeight = underlineHeightPx
        invalidate()
    }

    fun setBottomLineColor(bottomLineColor: Int) {
        this.mBottomLineColor = bottomLineColor
        invalidate()
    }

    fun setBottomLineColorId(resId: Int) {
        this.mBottomLineColor = resources.getColor(resId)
        invalidate()
    }

    fun setTextAllCaps(textAllCaps: Boolean) {
        this.mTextAllCaps = textAllCaps
    }

    fun setTextSize(textSizePx: Int) {
        this.mTextSize = textSizePx
        updateTabStyles()
    }

    fun setTextColor(textColor: Int) {
        this.mTextColor = textColor
        updateTabStyles()
    }

    fun setTextColorId(resId: Int) {
        this.mTextColorResId = resId
        updateTabStyles()
    }

    fun setTextTypeface(typeface: Typeface, style: Int) {
        this.mTextTypeFace = typeface
        this.mTextTypefaceStyle = style
        updateTabStyles()
    }

    /**
     * {first middle last full} or {normal}
     *
     * @param resIds
     */
    fun setItemBackground(vararg resIds: Int) {
        val size = resIds?.size ?: 0
        if (size == 1) {
            this.mItemBackground = resIds[0]
        } else {
            if (size > 0) {
                this.mItemBackgroundFirst = resIds[0]
            }
            if (size > 1) {
                this.mItemBackground = resIds[1]
            }
            if (size > 2) {
                this.mItemBackgroundLast = resIds[2]
            }
            if (size > 3) {
                this.mItemBackgroundFull = resIds[4]
            }
        }
    }

    fun setItemPaddingHorizonal(paddingHorizonalPixel: Int) {
        this.mItemMinPaddingHorizontal = paddingHorizonalPixel
    }

    fun setItemPaddingTop(paddingTopPixel: Int) {
        this.mItemMinPaddingTop = paddingTopPixel
    }

    fun setItemPaddingBottom(paddingBottomPixel: Int) {
        this.mItemMinPaddingBottom = paddingBottomPixel
    }

    fun smoothScroll(from: Int, to: Int, l: Animation.AnimationListener) {
        val childCount = itemViewCount
        if (from >= 0 && to >= 0 && from < childCount && to < childCount) {
            if (animation != null) {
                animation.cancel()
                clearAnimation()
            }
            val horizontal = isOrientationHorizontal
            val scrollFrom = computeScrollOffset(getVirtualChildAt(from, true), 0, false, horizontal)
            val scrollTo = computeScrollOffset(getVirtualChildAt(to, true), 0, false, horizontal)
            if (scrollTo != scrollFrom) {
                val absDx = Math.abs(scrollTo - scrollFrom)
                val anim = ScrollAnimation(scrollFrom, scrollTo)
                var measureWidth = measuredWidth
                if (measureWidth == 0) {
                    measureWidth = Math.max(suggestedMinimumWidth, 1)
                }
                anim.duration = Math.min(4000, absDx * 1800 / measureWidth).toLong()
                anim.interpolator = LinearInterpolator()
                anim.setAnimationListener(l)
                startAnimation(anim)
            }
        }
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        selectedPosition = savedState.currentPosition
        requestLayout()
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.currentPosition = selectedPosition
        return savedState
    }

    internal class SavedState : View.BaseSavedState {
        var currentPosition: Int = 0
        @JvmField  val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
            override fun createFromParcel(source: Parcel) = SavedState(source)

            override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
        }

        constructor(superState: Parcelable) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            currentPosition = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPosition)
        }
    }

    interface ITabClickEvent {
        fun onTabClicked(parent: PageScrollTab, cur: View, curPos: Int, pre: View?, prePos: Int): Boolean
    }

    internal inner class ScrollAnimation(private val mScrollFrom: Int, private val mScrollTo: Int) : Animation() {

        override fun applyTransformation(time: Float, t: Transformation) {
            val current = (mScrollFrom + (mScrollTo - mScrollFrom) * time).toInt()
            scrollTo(current, 0)
        }
    }

    companion object {

        private val TAB_INDEX = R.id.widgetLayoutViewIndexType
        private val ATTRS = intArrayOf(android.R.attr.textSize, android.R.attr.textColor)
    }
}