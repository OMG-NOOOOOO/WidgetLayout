package com.rexy.widgets.drawable;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.view.Gravity;
/**
 * Created by rexy on 2018/1/31.
 */
public class LimitInsetDrawable extends InsetDrawable {
    private int mGravity = 0;
    private int mMaxWidth = -1;
    private int mMaxHeight = -1;

    private Rect mInset = new Rect();
    private int mDrawableWidth = 0;
    private int mDrawableHeight = 0;

    public LimitInsetDrawable(Drawable drawable, int maxWidth, int maxHeight, int gravity, Rect inset) {
        super(drawable, inset.left, inset.top, inset.right, inset.bottom);
        mGravity = gravity;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mInset = inset;
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();
    }

    private int getContentStartH(int containerLeft, int containerRight, int contentWillSize, int contentMarginLeft, int contentMarginRight, int gravity) {
        if (gravity != -1 || gravity != 0) {
            int start;
            final int mask = Gravity.HORIZONTAL_GRAVITY_MASK;
            final int maskCenter = Gravity.CENTER_HORIZONTAL;
            final int maskEnd = Gravity.RIGHT;
            final int okGravity = gravity & mask;
            if (maskCenter == okGravity) {//center
                start = containerLeft + (containerRight - containerLeft - (contentWillSize + contentMarginRight - contentMarginLeft)) / 2;
            } else if (maskEnd == okGravity) {//end
                start = containerRight - contentWillSize - contentMarginRight;
            } else {//start
                start = containerLeft + contentMarginLeft;
            }
            return start;
        }
        return containerLeft + contentMarginLeft;
    }

    private int getContentStartV(int containerTop, int containerBottom, int contentWillSize, int contentMarginTop, int contentMarginBottom, int gravity) {
        if (gravity != -1 || gravity != 0) {
            int start;
            final int mask = Gravity.VERTICAL_GRAVITY_MASK;
            final int maskCenter = Gravity.CENTER_VERTICAL;
            final int maskEnd = Gravity.BOTTOM;
            final int okGravity = gravity & mask;
            if (maskCenter == okGravity) {//center
                start = containerTop + (containerBottom - containerTop - (contentWillSize + contentMarginBottom - contentMarginTop)) / 2;
            } else if (maskEnd == okGravity) {//end
                start = containerBottom - contentWillSize - contentMarginBottom;
            } else {//start
                start = containerTop + contentMarginTop;
            }
            return start;
        }
        return containerTop + contentMarginTop;
    }

    private int getSize(int contentSize, int boundsSize, int maxSize) {
        if (contentSize > 0) {
            if (maxSize > 0 && contentSize > maxSize) {
                return Math.min(maxSize, boundsSize);
            }
            return Math.min(contentSize, boundsSize);
        } else {
            if (maxSize > 0 && boundsSize > maxSize) {
                return maxSize;
            }
            return boundsSize;
        }
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        int newLeft = left;
        int newTop = top;
        int newRight = right;
        int newBottom = bottom;
        int maybeWidth = getSize(mDrawableWidth, right - left, mMaxWidth);
        int maybeHeight = getSize(mDrawableHeight, bottom - top, mMaxHeight);
        if (maybeWidth > 0 && maybeHeight > 0) {
            newLeft = getContentStartH(left, right, maybeWidth, mInset.left, mInset.right, mGravity);
            newTop = getContentStartV(top, bottom, maybeHeight, mInset.top, mInset.bottom, mGravity);
            if ((mDrawableWidth > maybeWidth || mDrawableHeight > maybeHeight)) {
                float rateDrawable = mDrawableWidth / mDrawableHeight;
                float rateContainer = maybeWidth / maybeHeight;
                if (rateDrawable != rateContainer) {
                    if (rateDrawable > rateContainer) {
                        int oldBottom = newTop + maybeHeight;
                        maybeHeight = (int) (maybeWidth / rateDrawable);
                        newTop = getContentStartV(newTop, oldBottom, maybeHeight, 0, 0, mGravity);
                    } else {
                        int oldRight = newLeft + maybeWidth;
                        maybeWidth = (int) (maybeHeight * rateDrawable);
                        newLeft = getContentStartH(newLeft, oldRight, maybeWidth, 0, 0, mGravity);
                    }
                }
            }
            newRight = newLeft + maybeWidth;
            newBottom = newTop + maybeHeight;
            newLeft -= mInset.left;
            newTop -= mInset.top;
            newRight += mInset.right;
            newBottom += mInset.bottom;
        }
        super.setBounds(newLeft, newTop, newRight, newBottom);
    }

    @Override
    public int getIntrinsicWidth() {
        int width = super.getIntrinsicWidth();
        if (width > 0 && width > mMaxWidth) {
            width = mMaxWidth;
        }
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        int height = super.getIntrinsicHeight();
        if (height > 0 && height > mMaxHeight) {
            height = mMaxHeight;
        }
        return height;
    }
}

