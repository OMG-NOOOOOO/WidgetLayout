package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.rexy.widgetlayout.R;

import java.lang.ref.WeakReference;

/**
 * 支持NestScrollView 方式的嵌套滑动。要求嵌套滑动的child 必须实现NestedScrollingChild接口。
 * 如可使用 RecyclerView 作为列表视图，NestScrollView 作为滑动子视图。
 * 作用上
 * 1. xml 中可用nestViewIndex指定实现了NestedScrollingChild接口的子 View 所在的直接 child 的索引。
 * floatViewIndex指定需要悬停的 View 所在的直接 Child 的索引。
 * 2. java 中建议使用setNestViewId setFloatViewId 来指定，也可通过setNestViewIndex,setFloatViewIndex来分别指定能嵌套滑动的view和悬停 View.
 * <p>
 * <declare-styleable name="NestFloatLayout">
 * <!--实现了嵌套滑动NestScrollingChild 接口的滑动的 View 所在的直接子 View 索引-->
 * <attr name="nestViewIndex" format="integer"/>
 * <!--需要吸顶到顶部的 View 所在的直接子 View 索引-->
 * <attr name="floatViewIndex" format="integer"/>
 * </declare-styleable>
 *
 * @date: 2017-05-27 17:36
 */
public class NestFloatLayout extends ScrollLayout implements NestedScrollingParent {
    WeakReference<View> mNestChild;
    WeakReference<View> mFloatView;
    int mFloatViewId, mFloatViewIndex = -1;
    int mNestChildId, mNestChildIndex = -1;

    public NestFloatLayout(Context context) {
        super(context);
        init(context, null);
    }

    public NestFloatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NestFloatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public NestFloatLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attr = attrs == null ? null : context.obtainStyledAttributes(attrs, R.styleable.NestFloatLayout);
        if (attr != null) {
            mFloatViewIndex = attr.getInt(R.styleable.NestFloatLayout_floatViewIndex, mFloatViewIndex);
            mNestChildIndex = attr.getInt(R.styleable.NestFloatLayout_nestViewIndex, mNestChildIndex);
            attr.recycle();
        }
    }

    public void setNestViewId(int nestChildId) {
        if (mNestChildId != nestChildId) {
            mNestChildId = nestChildId;
        }
    }

    public void setFloatViewId(int floatViewId) {
        if (mFloatViewId != floatViewId) {
            mFloatViewId = floatViewId;
        }
    }

    public void setNestViewIndex(int index) {
        if (mNestChildIndex != index) {
            mNestChildIndex = index;
        }
    }

    public void setFloatViewIndex(int index) {
        if (mFloatViewIndex != index) {
            mFloatViewIndex = index;
        }
    }

    private View findDirectChildView(View view) {
        ViewParent parent = view.getParent();
        while (parent != null && parent != NestFloatLayout.this) {
            if (parent instanceof View) {
                view = (View) parent;
                parent = view.getParent();
            } else {
                break;
            }
        }
        if (parent == NestFloatLayout.this) {
            return view;
        }
        return null;
    }

    private View findViewByIndexAndId(int index, int id) {
        View view = index >= 0 ? getChildAt(index) : null;
        if (view == null && id != 0) {
            view = findViewById(id);
        }
        return view;
    }

    private void ensureNestFloatView() {
        if (mFloatViewId != 0 || mFloatViewIndex != -1) {
            View floatView = findViewByIndexAndId(mFloatViewIndex, mFloatViewId);
            if (floatView != null) {
                mFloatView = null;
                mFloatViewId = 0;
                mFloatViewIndex = -1;
                floatView = findDirectChildView(floatView);
                if (floatView != null) {
                    mFloatView = new WeakReference(floatView);
                }
            }
        }
        if (mNestChildId != 0 || mNestChildIndex != -1) {
            View nestChild = findViewByIndexAndId(mNestChildIndex, mNestChildId);
            if (nestChild != null) {
                mNestChildId = 0;
                mNestChild = null;
                nestChild = findDirectChildView(nestChild);
                if (nestChild != null) {
                    mNestChild = new WeakReference(nestChild);
                }
            }
        }
    }

    public View getFloatView() {
        if (mFloatViewId != 0 || mFloatViewIndex != -1) {
            ensureNestFloatView();
        }
        return mFloatView == null ? null : mFloatView.get();
    }

    public View getNestView() {
        if (mNestChildId != 0 || mNestChildIndex != -1) {
            ensureNestFloatView();
        }
        return mNestChild == null ? null : mNestChild.get();
    }

    @Override
    protected void dispatchMeasure(int widthMeasureSpecContent, int heightMeasureSpecContent) {
        final int childCount = getChildCount();
        int contentWidth = 0, contentHeight = 0, childState = 0;
        int virtualHeight = 0, itemPosition = 0;
        View nestView = getNestView();
        View floatView = getFloatView();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (skipChild(child)) continue;
            int parentWidthMeasure = widthMeasureSpecContent;
            int parentHeightMeasure = heightMeasureSpecContent;
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (nestView == child && virtualHeight > 0) {
                parentHeightMeasure = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpecContent), MeasureSpec.AT_MOST);
                measure(child, itemPosition++, parentWidthMeasure, parentHeightMeasure, 0, virtualHeight);
            } else {
                measure(child, itemPosition++, parentWidthMeasure, parentHeightMeasure, 0, contentHeight);
            }
            int itemWidth = params.width(child);
            int itemHeight = params.height(child);
            if (floatView == child) {
                virtualHeight = itemHeight;
            } else {
                if (virtualHeight > 0) {
                    virtualHeight += itemHeight;
                }
            }
            contentHeight += itemHeight;
            if (contentWidth < itemWidth) {
                contentWidth = itemWidth;
            }
            childState |= child.getMeasuredState();
        }
        setContentSize(contentWidth, contentHeight, childState);
    }

    @Override
    protected boolean ignoreSelfTouch(boolean fromIntercept, MotionEvent e) {
        boolean ignore = super.ignoreSelfTouch(fromIntercept, e);
        if (!ignore) {
            View nestView = getNestView();
            if (nestView != null) {
                ignore = e.getY() + getScrollY() >= nestView.getTop();
            }
        }
        return ignore;
    }

    //start: NestedScrollingParent
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        View floatView = getFloatView();
        boolean acceptedNestedScroll = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && floatView != null;
        if (isLogAccess()) {
            print("nest", String.format("onStartNestedScroll(child=%s,target=%s,nestedScrollAxes=%d,accepted=%s)", String.valueOf(child.getClass().getSimpleName()), String.valueOf(target.getClass().getSimpleName()), nestedScrollAxes, acceptedNestedScroll));
        }
        return acceptedNestedScroll;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        if (isLogAccess()) {
            print("nest", String.format("onNestedScrollAccepted(child=%s,target=%s,nestedScrollAxes=%d)", String.valueOf(child.getClass().getSimpleName()), String.valueOf(target.getClass().getSimpleName()), nestedScrollAxes));
        }
    }

    @Override
    public void onStopNestedScroll(View target) {
        if (isLogAccess()) {
            print("nest", String.format("onStopNestedScroll(target=%s)", String.valueOf(target.getClass().getSimpleName())));
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (isLogAccess()) {
            print("nest", String.format("onNestedScroll(target=%s,dxConsumed=%d,dyConsumed=%d,dxUnconsumed=%d,dyUnconsumed=%d)", String.valueOf(target.getClass().getSimpleName()), dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed));
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        View floatView = getFloatView();
        if (floatView != null) {
            int maxSelfScrolled = getVerticalScrollRange();
            int curSelfScrolled = getScrollY();
            int consumedY = 0;
            if (dy > 0 && curSelfScrolled < maxSelfScrolled) {
                consumedY = Math.min(dy, maxSelfScrolled - curSelfScrolled);
            }
            if (dy < 0 && curSelfScrolled > 0 && !ViewCompat.canScrollVertically(target, -1)) {
                consumedY = Math.max(dy, -curSelfScrolled);
            }
            if (consumedY != 0) {
                scrollBy(0, consumedY);
                invalidate();
                print("nest", "consumed:" + consumedY);
            }
            consumed[1] = consumedY;
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (isLogAccess()) {
            print("nest", String.format("onNestedFling(target=%s,vx=%.1f,vy=%.1f,consumed=%s)", String.valueOf(target.getClass().getSimpleName()), velocityX, velocityY, consumed));
        }
        boolean watched = false;
        //以下是对快速滑动NestView 的补偿。
        if (velocityY > 1 && getScrollY() >= 0) {
            fling(0, 0, 0, (int) velocityY);
            watched = true;
        }
        if (velocityY < -1 && getScrollY() <= getVerticalScrollRange()) {
            fling(0, 0, 0, (int) velocityY);
            watched = true;
        }
        return watched;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (isLogAccess()) {
            print("nest", String.format("onNestedPreFling(target=%s,vx=%.1f,vy=%.1f)", String.valueOf(target.getClass().getSimpleName()), velocityX, velocityY));
        }
        //如果列表可快速滑动返回 false,否则返回true.  down - //up+
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        if (isLogAccess()) {
            print("nest", "getNestedScrollAxes");
        }
        return isTouchScrollEnable() ? ViewCompat.SCROLL_AXIS_VERTICAL : 0;
    }
    //end:NestedScrollingParent
}
