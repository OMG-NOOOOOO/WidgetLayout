package com.rexy.widgets.view;

import android.support.v4.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by rexy on 18/1/6.
 */
public class ViewHierarchyTree extends ViewHierarchyInfo {
    private SparseIntArray mHierarchyCountArray = new SparseIntArray();
    private SparseArray<ViewHierarchyInfo> mHierarchyNodeArray = new SparseArray();

    public static ViewHierarchyTree create(View root) {
        return new ViewHierarchyTree(root, -1, 0, null);
    }

    static ViewHierarchyTree create(View root, int index, int levelIndex, ViewHierarchyInfo parent) {
        return new ViewHierarchyTree(root, index, levelIndex, parent);
    }

    private ViewHierarchyTree(View root, int index, int levelIndex, ViewHierarchyInfo parent) {
        super(root, index, levelIndex, parent);
        mHierarchyCountArray.put(getLevel(), 1);
        mHierarchyNodeArray.put(0, this);
        if (getChildArr() != null) {
            analyzeViewHierarchy(Pair.create(this, (ViewGroup) root));
        }
    }

    private void analyzeViewHierarchy(Pair<? extends ViewHierarchyInfo, ViewGroup> root) {
        Queue<Pair<? extends ViewHierarchyInfo, ViewGroup>> queue = new LinkedList();
        queue.offer(root);
        int arrayIndex = mHierarchyNodeArray.size();
        int levelIndex = 0, level = 0;
        Pair<? extends ViewHierarchyInfo, ViewGroup> pair;
        while ((pair = queue.poll()) != null) {
            ViewHierarchyInfo parent = pair.first;
            ViewGroup layout = pair.second;
            int size = layout.getChildCount();
            for (int i = 0; i < size; i++) {
                View child = layout.getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    int curLevel = parent == null ? 0 : (parent.mLevel + 1);
                    if (curLevel != level) {
                        level = curLevel;
                        levelIndex = 0;
                    }
                    ViewHierarchyInfo node = ViewHierarchyInfo.obtain(child, i, levelIndex++, parent);
                    mHierarchyCountArray.put(node.mLevel, mHierarchyCountArray.get(curLevel, 0) + 1);
                    mHierarchyNodeArray.put(arrayIndex++, node);
                    if (node.mChildArr != null) {
                        queue.offer(Pair.create(node, (ViewGroup) child));
                    }
                }
            }
        }
    }

    public ViewHierarchyInfo getViewHierarchyInfo(int level, int levelIndex) {
        int sum = levelIndex;
        for (int i = 0; i < level; i++) {
            sum += mHierarchyCountArray.get(i, 0);
        }
        if (sum >= 0 && sum < mHierarchyNodeArray.size()) {
            return mHierarchyNodeArray.get(sum);
        }
        return null;
    }

    public int getCountOfViewGroup() {
        return getCountOfNode() - getCountOfView();
    }

    public int getCountOfView() {
        return getLeafCount();
    }

    public int getCountOfNode() {
        return mHierarchyNodeArray.size();
    }

    public int getCountOfNode(int level) {
        return mHierarchyCountArray.get(level);
    }

    public int getCountOfView(int level) {
        int start = 0, leafCount = 0;
        while (start < level) {
            start += mHierarchyCountArray.get(start);
        }
        for (int end = start + getCountOfNode(level); start < end; start++) {
            if (mHierarchyNodeArray.get(start).isLeaf()) {
                leafCount++;
            }
        }
        return leafCount;
    }

    public int getCountOfViewGroup(int level) {
        return getCountOfNode(level) - getCountOfView(level);
    }

    public int getHierarchyCount() {
        return mHierarchyCountArray.size();
    }

    public float getArgHierarchyCount() {
        final float sum = getCountOfNode();
        float result = 0;
        final int hierarchyCount = getHierarchyCount();
        for (int i = 0; i < hierarchyCount; i++) {
            result += (getCountOfNode(i) * (i + 1) / sum);
        }
        return result;
    }

    public SparseIntArray getHierarchyCountArray() {
        return mHierarchyCountArray;
    }

    public SparseArray<ViewHierarchyInfo> getHierarchyNodeArray() {
        return mHierarchyNodeArray;
    }

    public StringBuilder dumpNodeWeight(StringBuilder sb) {
        sb = sb == null ? new StringBuilder() : sb;
        int size = mHierarchyCountArray.size();
        int weight = 0;
        for (int i = 0; i < size; i++) {
            int value = mHierarchyCountArray.get(i);
            weight += (value * (i + 1));
            sb.append(" | ").append(value);
        }
        sb.insert(0, weight);
        return sb;
    }

    public void destroy() {
        super.destroy(true);
        mHierarchyCountArray.clear();
        mHierarchyNodeArray.clear();
    }
}
