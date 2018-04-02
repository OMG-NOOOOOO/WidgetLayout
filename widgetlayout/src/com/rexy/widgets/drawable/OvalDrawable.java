package com.rexy.widgets.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2018-02-04 09:47
 */
public class OvalDrawable extends Drawable {


    private Paint mPaint;
    private RectF mRectF = new RectF();

    public OvalDrawable(int color) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        if (isVisible()) {
            invalidateSelf();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRectF.set(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawArc(mRectF, 0, 360, true, mPaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }
}