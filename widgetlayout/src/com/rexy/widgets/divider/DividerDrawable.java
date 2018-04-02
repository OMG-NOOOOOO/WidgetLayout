package com.rexy.widgets.divider;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class DividerDrawable {

    Paint mPaint;
    Drawable mDivider;

    int mDividerColor;
    int mDividerWidth;

    int mDividerMarginStart;
    int mDividerMarginEnd;

    private DividerDrawable(int defaultWidth) {
        mDividerWidth = defaultWidth;
    }

    public void setDivider(Drawable divider) {
        mDivider = divider;
    }

    public void setDividerColor(int color) {
        mDividerColor = color;
        if (mPaint != null) {
            mPaint.setColor(color);
        }
    }

    public void setDividerWidth(int width) {
        mDividerWidth = width;
        if (mPaint != null) {
            mPaint.setStrokeWidth(width);
        }
    }

    public void setDividerMargin(int margin) {
        mDividerMarginStart = margin;
        mDividerMarginEnd = margin;
    }

    public void setDividerMarginStart(int marginStart) {
        mDividerMarginStart = marginStart;
    }

    public void setDividerMarginEnd(int marginEnd) {
        mDividerMarginEnd = marginEnd;
    }

    public Drawable getDivider() {
        return mDivider;
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public int getDividerWidth() {
        return mDividerWidth;
    }

    public int getDividerMarginStart() {
        return mDividerMarginStart;
    }

    public int getDividerMarginEnd() {
        return mDividerMarginEnd;
    }

    public boolean isValidated(boolean initPaintIfNeed) {
        boolean validated = mDividerWidth != 0 && (mDivider != null || mDividerColor != 0);
        if (validated && initPaintIfNeed && (mPaint == null && mDivider == null)) {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setColor(mDividerColor);
            mPaint.setStrokeWidth(mDividerWidth);
        }
        return validated;
    }

    public void draw(Canvas canvas, int from, int to, int middle, boolean horizontal) {
        from += mDividerMarginStart;
        to -= mDividerMarginEnd;
        if (to > from && mDividerWidth > 0 && (mDivider != null || mPaint != null)) {
            if (horizontal) {
                if (mDivider == null) {
                    canvas.drawLine(from, middle, to, middle, mPaint);
                } else {
                    float halfWidth = mDividerWidth / 2f;
                    float middleStart = middle - halfWidth + 0.25f, middleEnd = middle + halfWidth + 0.25f;
                    mDivider.setBounds(from, (int) middleStart, to, (int) middleEnd);
                    mDivider.draw(canvas);
                }
            } else {
                if (mDivider == null) {
                    canvas.drawLine(middle, from, middle, to, mPaint);
                } else {
                    float halfWidth = mDividerWidth / 2f;
                    float middleStart = middle - halfWidth + 0.25f, middleEnd = middle + halfWidth + 0.25f;
                    mDivider.setBounds((int) middleStart, from, (int) middleEnd, to);
                    mDivider.draw(canvas);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int result = mDividerColor;
        result = 31 * result + mDividerWidth;
        result = 31 * result + mDividerMarginStart;
        result = 31 * result + mDividerMarginEnd;
        return result + (mDivider == null ? 0 : mDivider.hashCode());
    }

    public static DividerDrawable from(TypedArray attr, int defaultWidth, int attrDivider, int attrDividerColor, int attrDividerWidth, int attrDividerMargin, int attrDividerMarginStart, int attrDividerMarginEnd) {
        DividerDrawable dm = new DividerDrawable(defaultWidth);
        if (attr != null) {
            if (attr.hasValue(attrDivider)) {
                dm.mDivider = attr.getDrawable(attrDivider);
            }
            dm.mDividerColor = attr.getColor(attrDividerColor, dm.mDividerColor);
            dm.mDividerWidth = attr.getDimensionPixelSize(attrDividerWidth, dm.mDividerWidth);
            boolean hasDefaultMargin = attr.hasValue(attrDividerMargin);
            int defaultMargin = hasDefaultMargin ? attr.getDimensionPixelSize(attrDividerMargin, 0) : 0;
            dm.mDividerMarginStart = attr.getDimensionPixelSize(attrDividerMarginStart, hasDefaultMargin ? defaultMargin : dm.mDividerMarginStart);
            dm.mDividerMarginEnd = attr.getDimensionPixelSize(attrDividerMarginEnd, hasDefaultMargin ? defaultMargin : dm.mDividerMarginEnd);
        }
        return dm;
    }
}