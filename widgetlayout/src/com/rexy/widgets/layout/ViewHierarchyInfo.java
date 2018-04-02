package com.rexy.widgets.layout;

import android.graphics.Rect;
import android.support.v4.util.Pools;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rexy on 18/1/6.
 */

public class ViewHierarchyInfo {
    private static int MAX_POOL_SIZE = 500;
    private static final Pools.SimplePool<ViewHierarchyInfo> mPool = new Pools.SimplePool(MAX_POOL_SIZE);
    private static int[] LOCATION = new int[]{0, 0};
    int mLevel = -1;
    private int mLevelIndex = -1;
    private int mParentIndex = -1;
    private int mLeafCount = 0;
    private View mView;
    private Object mTag;
    private String name;
    private Rect mWindowLocation = new Rect();
    private ViewHierarchyInfo mParent;
    LinkedList<ViewHierarchyInfo> mChildArr;

    static ViewHierarchyInfo obtain(View view, int index, int levelIndex, ViewHierarchyInfo parent) {
        ViewHierarchyInfo data = mPool.acquire();
        if (data == null) {
            data = new ViewHierarchyInfo(view, index, levelIndex, parent);
        } else {
            data.analyzeHierarchyInfo(view, index, levelIndex, parent);
        }
        return data;
    }

    ViewHierarchyInfo(View view, int index, int levelIndex, ViewHierarchyInfo parent) {
        analyzeHierarchyInfo(view, index, levelIndex, parent);
    }

    private void analyzeHierarchyInfo(View view, int index, int levelIndex, ViewHierarchyInfo parent) {
        this.mView = view;
        this.name = view.getClass().getName();
        mWindowLocation.set(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
        mView.getLocationInWindow(LOCATION);
        mWindowLocation.offset(LOCATION[0], LOCATION[1]);
        if (parent == null) {
            this.mLevel = 0;
        } else {
            this.mLevel = parent.mLevel + 1;
            this.mParent = parent;
            parent.mChildArr.add(this);
        }
        this.mParentIndex = index;
        this.mLevelIndex = levelIndex;
        if (view instanceof ViewGroup) {
            mChildArr = new LinkedList();
            if (index == -1) {
                if (view.getParent() instanceof ViewGroup) {
                    this.mParentIndex = ((ViewGroup) view.getParent()).indexOfChild(view);
                } else {
                    this.mParentIndex = 0;
                }
            }
        }
        computeWeightIfNeed(view, parent);
    }

    private void computeWeightIfNeed(View view, ViewHierarchyInfo parent) {
        boolean calculateWeight = !(view instanceof ViewGroup);
        if (!calculateWeight) {
            ViewGroup p = (ViewGroup) view;
            int count = p.getChildCount();
            calculateWeight = true;
            for (int i = 0; i < count; i++) {
                if (View.GONE != p.getChildAt(i).getVisibility()) {
                    calculateWeight = false;
                    break;
                }
            }
        }
        if (calculateWeight) {
            mLeafCount = 1;
            while (parent != null) {
                parent.mLeafCount = parent.mLeafCount + 1;
                parent = parent.mParent;
            }
        }
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public <CAST extends Object> CAST getTag() {
        return (CAST) mTag;
    }

    public boolean isLeaf() {
        return mChildArr == null;
    }

    public boolean isRoot() {
        return mParent == null;
    }

    public String getMarkName() {
        String name = getSimpleName();
        if (name != null && name.length() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    sb.append(c);
                }
            }
            if (sb.length() == 0) {
                sb.append(name);
            }
            return sb.toString();
        }
        return name;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        String result = name;
        int point = result == null ? -1 : 0;
        if (point == 0) {
            point = result.lastIndexOf('$');
            if (point == -1) {
                point = result.lastIndexOf('.');
            }
        }
        if (point > 0 && point < result.length()) {
            result = result.substring(point + 1);
        }
        return result;
    }

    public View getView() {
        return mView;
    }

    public Rect getWindowLocation() {
        return mWindowLocation;
    }

    public int getLevel() {
        return mLevel;
    }

    public int getLevelIndex() {
        return mLevelIndex;
    }

    public int getLeafCount() {
        return mLeafCount;
    }

    public int getLayoutIndex() {
        return mParentIndex;
    }

    public int getParentIndex() {
        int result = mParentIndex;
        if (mParent != null && mParent.mChildArr != null) {
            result = mParent.mChildArr.indexOf(this);
        }
        return result;
    }

    public ViewHierarchyInfo getParent() {
        return mParent;
    }

    public List<? extends ViewHierarchyInfo> getChildArr() {
        return mChildArr;
    }

    public int getChildCount() {
        if (mChildArr != null) {
            return mChildArr.size();
        }
        return 0;
    }

    private void recycle() {
        destroy(true);
        mLevel = -1;
        mLevelIndex = -1;
        mParentIndex = -1;
        mLeafCount = 0;
        mPool.release(this);
    }

    protected void destroy(boolean recycle) {
        mView = null;
        name = null;
        mWindowLocation.setEmpty();
        mTag = null;
        mParent = null;
        if (mChildArr != null) {
            Iterator<ViewHierarchyInfo> its = mChildArr.iterator();
            while (its.hasNext()) {
                if (recycle) {
                    its.next().recycle();
                } else {
                    its.next().destroy(recycle);
                }
                its.remove();
            }
            mChildArr = null;
        }
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean fullInfo) {
        StringBuilder sb = new StringBuilder();
        if (isRoot()) {
            sb.append("[root ");
        } else if (isLeaf()) {
            sb.append("[leaf ");
        } else {
            sb.append("[node ");
        }
        sb.append(getLevel()).append(',').append(getLevelIndex()).append(']');
        sb.append(' ').append(fullInfo ? getSimpleName() : getMarkName()).append('{');
        sb.append("index=").append(getParentIndex()).append(',');
        sb.append("location=").append(getWindowLocation()).append(',');
        sb.append("count=").append(getChildCount()).append(',');
        sb.append("leaf=").append(getLeafCount());
        return sb.append('}').toString();
    }
}

