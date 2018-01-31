package com.rexy.widgets;

import android.view.Gravity;
import android.view.ViewGroup;

import com.rexy.widgets.view.ViewHierarchyTree;

/**
 * Created by rexy on 18/1/5.
 */

public class ViewHelper {

    public static ViewHierarchyTree getViewHierarchyOf(ViewGroup root){
        return ViewHierarchyTree.create(root);
    }

    public static int getContentStartH(int containerLeft, int containerRight, int contentWillSize, int contentMarginLeft, int contentMarginRight, int gravity) {
        if (gravity != -1 || gravity != 0) {
            int start;
            final int mask = Gravity.HORIZONTAL_GRAVITY_MASK;
            final int maskCenter = Gravity.CENTER_HORIZONTAL;
            final int maskEnd = Gravity.RIGHT;
            final int okGravity = gravity & mask;
            if (maskCenter == okGravity) {//center
                start = (int) (containerLeft +0.5f+ (containerRight - containerLeft - (contentWillSize + contentMarginRight - contentMarginLeft)) / 2f);
            } else if (maskEnd == okGravity) {//end
                start = containerRight - contentWillSize - contentMarginRight;
            } else {//start
                start = containerLeft + contentMarginLeft;
            }
            return start;
        }
        return containerLeft + contentMarginLeft;
    }

    public static int getContentStartV(int containerTop, int containerBottom, int contentWillSize, int contentMarginTop, int contentMarginBottom, int gravity) {
        if (gravity != -1 || gravity != 0) {
            int start;
            final int mask = Gravity.VERTICAL_GRAVITY_MASK;
            final int maskCenter = Gravity.CENTER_VERTICAL;
            final int maskEnd = Gravity.BOTTOM;
            final int okGravity = gravity & mask;
            if (maskCenter == okGravity) {//center
                start = (int) (containerTop +0.5f+ (containerBottom - containerTop - (contentWillSize + contentMarginBottom - contentMarginTop)) / 2f);
            } else if (maskEnd == okGravity) {//end
                start = containerBottom - contentWillSize - contentMarginBottom;
            } else {//start
                start = containerTop + contentMarginTop;
            }
            return start;
        }
        return containerTop + contentMarginTop;
    }
}
