package com.rexy.widgets.layout

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent

import com.rexy.widgetlayout.R

import java.lang.ref.WeakReference

/**
 * 支持NestScrollView 方式的嵌套滑动。要求嵌套滑动的child 必须实现NestedScrollingChild接口。
 * 如可使用 RecyclerView 作为列表视图，NestScrollView 作为滑动子视图。
 * 作用上
 * 1. xml 中可用nestViewIndex指定实现了NestedScrollingChild接口的子 View 所在的直接 child 的索引。
 * floatViewIndex指定需要悬停的 View 所在的直接 Child 的索引。
 * 2. java 中建议使用setNestViewId setFloatViewId 来指定，也可通过setNestViewIndex,setFloatViewIndex来分别指定能嵌套滑动的view和悬停 View.
 *
 *
 * <declare-styleable name="NestFloatLayout">
 *
 * <attr name="nestViewIndex" format="integer"></attr>
 *
 * <attr name="floatViewIndex" format="integer"></attr>
</declare-styleable> *
 *
 * @date: 2017-05-27 17:36
 */
class NestFloatLayout : ScrollLayout, NestedScrollingParent {
    internal var mNestChild: WeakReference<View>? = null
    internal var mFloatView: WeakReference<View>? = null
    internal var mFloatViewId: Int = 0
    internal var mFloatViewIndex = -1
    internal var mNestChildId: Int = 0
    internal var mNestChildIndex = -1

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
        val attr = if (attrs == null) null else context.obtainStyledAttributes(attrs, R.styleable.NestFloatLayout)
        if (attr != null) {
            mFloatViewIndex = attr.getInt(R.styleable.NestFloatLayout_floatViewIndex, mFloatViewIndex)
            mNestChildIndex = attr.getInt(R.styleable.NestFloatLayout_nestViewIndex, mNestChildIndex)
            attr.recycle()
        }
        setTouchScrollEnable(true)
    }

    fun setNestViewId(nestChildId: Int) {
        if (mNestChildId != nestChildId) {
            mNestChildId = nestChildId
        }
    }

    fun setFloatViewId(floatViewId: Int) {
        if (mFloatViewId != floatViewId) {
            mFloatViewId = floatViewId
        }
    }

    fun setNestViewIndex(index: Int) {
        if (mNestChildIndex != index) {
            mNestChildIndex = index
        }
    }

    fun setFloatViewIndex(index: Int) {
        if (mFloatViewIndex != index) {
            mFloatViewIndex = index
        }
    }

    private fun findDirectChildView(view: View): View? {
        var view = view
        var parent: ViewParent? = view.parent
        while (parent != null && parent !== this@NestFloatLayout) {
            if (parent is View) {
                view = parent
                parent = view.parent
            } else {
                break
            }
        }
        return if (parent === this@NestFloatLayout) {
            view
        } else null
    }

    private fun findViewByIndexAndId(index: Int, id: Int): View? {
        var view: View? = if (index >= 0) getChildAt(index) else null
        if (view == null && id != 0) {
            view = findViewById(id)
        }
        return view
    }

    private fun ensureNestFloatView() {
        if (mFloatViewId != 0 || mFloatViewIndex != -1) {
            var floatView = findViewByIndexAndId(mFloatViewIndex, mFloatViewId)
            if (floatView != null) {
                mFloatView = null
                mFloatViewId = 0
                mFloatViewIndex = -1
                floatView = findDirectChildView(floatView)
                if (floatView != null) {
                    mFloatView = WeakReference(floatView)
                }
            }
        }
        if (mNestChildId != 0 || mNestChildIndex != -1) {
            var nestChild = findViewByIndexAndId(mNestChildIndex, mNestChildId)
            if (nestChild != null) {
                mNestChildId = 0
                mNestChild = null
                nestChild = findDirectChildView(nestChild)
                if (nestChild != null) {
                    mNestChild = WeakReference(nestChild)
                }
            }
        }
    }

    val floatView: View?
        get() {
            if (mFloatViewId != 0 || mFloatViewIndex != -1) {
                ensureNestFloatView()
            }
            return if (mFloatView == null) null else mFloatView!!.get()
        }

    val nestView: View?
        get() {
            if (mNestChildId != 0 || mNestChildIndex != -1) {
                ensureNestFloatView()
            }
            return if (mNestChild == null) null else mNestChild!!.get()
        }

    override fun dispatchMeasure(widthMeasureSpecContent: Int, heightMeasureSpecContent: Int) {
        var heightMeasureSpecContent = heightMeasureSpecContent
        heightMeasureSpecContent = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpecContent), View.MeasureSpec.UNSPECIFIED)
        val childCount = childCount
        var contentWidth = 0
        var contentHeight = 0
        var childState = 0
        var virtualHeight = 0
        var itemPosition = 0
        val nestView = nestView
        val floatView = floatView
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (skipChild(child)) continue
            var parentHeightMeasure = heightMeasureSpecContent
            val params = child.layoutParams as BaseViewGroup.LayoutParams
            if (nestView === child && virtualHeight > 0) {
                parentHeightMeasure = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpecContent), View.MeasureSpec.AT_MOST)
                params.measure(child, itemPosition++, widthMeasureSpecContent, parentHeightMeasure, 0, virtualHeight)
            } else {
                params.measure(child, itemPosition++, widthMeasureSpecContent, parentHeightMeasure, 0, contentHeight)
            }
            val itemWidth = params.width(child)
            val itemHeight = params.height(child)
            if (floatView === child) {
                virtualHeight = itemHeight
            } else {
                if (virtualHeight > 0) {
                    virtualHeight += itemHeight
                }
            }
            contentHeight += itemHeight
            if (contentWidth < itemWidth) {
                contentWidth = itemWidth
            }
            childState = childState or child.measuredState
        }
        setContentSize(contentWidth, contentHeight, childState)
    }

    override fun dispatchLayout(contentLeft: Int, contentTop: Int) {
        var childLeft: Int
        var childTop: Int
        var childRight: Int
        var childBottom: Int
        val contentRight = contentLeft + contentWidth
        childTop = contentTop
        val count = childCount
        for (i in 0..count - 1) {
            val child = getChildAt(i)
            if (skipChild(child)) continue
            val params = child.layoutParams as BaseViewGroup.LayoutParams
            childTop += params.topMargin()
            childBottom = childTop + child.measuredHeight
            childLeft = getContentStartH(contentLeft, contentRight, child.measuredWidth, params.leftMargin(), params.rightMargin(), params.gravity)
            childRight = childLeft + child.measuredWidth
            child.layout(childLeft, childTop, childRight, childBottom)
            childTop = childBottom + params.bottomMargin()
        }
    }

    override fun ignoreSelfTouch(fromIntercept: Boolean, e: MotionEvent): Boolean {
        var ignore = super.ignoreSelfTouch(fromIntercept, e)
        if (!ignore) {
            val nestView = nestView
            if (nestView != null) {
                ignore = e.y + scrollY >= nestView.top
            }
        }
        return ignore
    }

    //start: NestedScrollingParent
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        val floatView = floatView
        val acceptedNestedScroll = nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 && floatView != null
        if (isLogAccess) {
            print("nest", String.format("onStartNestedScroll(child=%s,target=%s,nestedScrollAxes=%d,accepted=%s)", child.javaClass.simpleName.toString(), target.javaClass.simpleName.toString(), nestedScrollAxes, acceptedNestedScroll))
        }
        return acceptedNestedScroll
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        if (isLogAccess) {
            print("nest", String.format("onNestedScrollAccepted(child=%s,target=%s,nestedScrollAxes=%d)", child.javaClass.simpleName.toString(), target.javaClass.simpleName.toString(), nestedScrollAxes))
        }
    }

    override fun onStopNestedScroll(target: View) {
        if (isLogAccess) {
            print("nest", String.format("onStopNestedScroll(target=%s)", target.javaClass.simpleName.toString()))
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (isLogAccess) {
            print("nest", String.format("onNestedScroll(target=%s,dxConsumed=%d,dyConsumed=%d,dxUnconsumed=%d,dyUnconsumed=%d)", target.javaClass.simpleName.toString(), dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed))
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        val floatView = floatView
        if (floatView != null) {
            val maxSelfScrolled = verticalScrollRange
            val curSelfScrolled = scrollY
            var consumedY = 0
            if (dy > 0 && curSelfScrolled < maxSelfScrolled) {
                consumedY = Math.min(dy, maxSelfScrolled - curSelfScrolled)
            }
            if (dy < 0 && curSelfScrolled > 0 && !ViewCompat.canScrollVertically(target, -1)) {
                consumedY = Math.max(dy, -curSelfScrolled)
            }
            if (consumedY != 0) {
                scrollBy(0, consumedY)
                invalidate()
                print("nest", "consumed:" + consumedY)
            }
            consumed[1] = consumedY
        }
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        if (isLogAccess) {
            print("nest", String.format("onNestedFling(target=%s,vx=%.1f,vy=%.1f,consumed=%s)", target.javaClass.simpleName.toString(), velocityX, velocityY, consumed))
        }
        var watched = false
        //以下是对快速滑动NestView 的补偿。
        if (velocityY > 1 && scrollY >= 0) {
            fling(0, 0, 0, velocityY.toInt())
            watched = true
        }
        if (velocityY < -1 && scrollY <= verticalScrollRange) {
            fling(0, 0, 0, velocityY.toInt())
            watched = true
        }
        return watched
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        if (isLogAccess) {
            print("nest", String.format("onNestedPreFling(target=%s,vx=%.1f,vy=%.1f)", target.javaClass.simpleName.toString(), velocityX, velocityY))
        }
        //如果列表可快速滑动返回 false,否则返回true.  down - //up+
        return false
    }

    override fun getNestedScrollAxes(): Int {
        if (isLogAccess) {
            print("nest", "getNestedScrollAxes")
        }
        return if (isTouchScrollVerticalEnable(true)) ViewCompat.SCROLL_AXIS_VERTICAL else 0
    }
    //end:NestedScrollingParent
}
