package com.rexy.widgets.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.rexy.widgetlayout.R;

/**
 * 描述 Divider 和 margin 的信息类,可独立画divider。目前只支持纯色divider。
 * 具体属性见
 * <!--左边线的颜色，宽度，和边线padding-->
 * <attr name="borderLeft" format="reference"/>
 * <attr name="borderLeftColor" format="color"/>
 * <attr name="borderLeftWidth" format="dimension"/>
 * <attr name="borderLeftMargin" format="dimension"/>
 * <attr name="borderLeftMarginStart" format="dimension"/>
 * <attr name="borderLeftMarginEnd" format="dimension"/>
 * <p>
 * <!--上边线的颜色，宽度，和边线padding-->
 * <attr name="borderTop" format="reference"/>
 * <attr name="borderTopColor" format="color"/>
 * <attr name="borderTopWidth" format="dimension"/>
 * <attr name="borderTopMargin" format="dimension"/>
 * <attr name="borderTopMarginStart" format="dimension"/>
 * <attr name="borderTopMarginEnd" format="dimension"/>
 * <p>
 * <!--右边线的颜色，宽度，和边线padding-->
 * <attr name="borderRight" format="reference"/>
 * <attr name="borderRightColor" format="color"/>
 * <attr name="borderRightWidth" format="dimension"/>
 * <attr name="borderRightMargin" format="dimension"/>
 * <attr name="borderRightMarginStart" format="dimension"/>
 * <attr name="borderRightMarginEnd" format="dimension"/>
 * <p>
 * <!--下边线的颜色，宽度，和边线padding-->
 * <attr name="borderBottom" format="reference"/>
 * <attr name="borderBottomColor" format="color"/>
 * <attr name="borderBottomWidth" format="dimension"/>
 * <attr name="borderBottomMargin" format="dimension"/>
 * <attr name="borderBottomMarginStart" format="dimension"/>
 * <attr name="borderBottomMarginEnd" format="dimension"/>
 * <p>
 * <!--内容四边的间距，不同于padding -->
 * <attr name="contentMarginLeft" format="dimension"/>
 * <attr name="contentMarginTop" format="dimension"/>
 * <attr name="contentMarginRight" format="dimension"/>
 * <attr name="contentMarginBottom" format="dimension"/>
 * <!--水平方向和垂直方向Item 的间距-->
 * <attr name="contentMarginHorizontal" format="dimension"/>
 * <attr name="contentMarginVertical" format="dimension"/>
 * <p>
 * <p>
 * <!--水平分割线颜色-->
 * <attr name="dividerColorHorizontal" format="color"/>
 * <!--水平分割线宽-->
 * <attr name="dividerWidthHorizontal" format="dimension"/>
 * <!--水平分割线开始和结束padding-->
 * <attr name="dividerPaddingHorizontal" format="dimension"/>
 * <attr name="dividerPaddingHorizontalStart" format="dimension"/>
 * <attr name="dividerPaddingHorizontalEnd" format="dimension"/>
 * <p>
 * <!--垂直分割线颜色-->
 * <attr name="dividerColorVertical" format="color"/>
 * <!--垂直分割线宽-->
 * <attr name="dividerWidthVertical" format="dimension"/>
 * <!--垂直分割线开始 和结束padding-->
 * <attr name="dividerPaddingVertical" format="dimension"/>
 * <attr name="dividerPaddingVerticalStart" format="dimension"/>
 * <attr name="dividerPaddingVerticalEnd" format="dimension"/>
 *
 * @author: rexy
 * @date: 2017-06-02 10:26
 */
public class BorderDivider {

    private Paint mPaintHorizontal;
    private Paint mPaintVertical;
    private Paint mPaintBorder;

    private boolean mResetPaintHorizontal = true;
    private boolean mResetPaintVertical = true;

    float mDensity = 1;

    //左边线的drawable,颜色，宽度，和边线padding
    Drawable mBorderLeft;
    int mBorderLeftColor;
    int mBorderLeftWidth;
    int mBorderLeftMarginStart;
    int mBorderLeftMarginEnd;

    //上边线的drawable,颜色，宽度，和边线padding
    Drawable mBorderTop;
    int mBorderTopColor;
    int mBorderTopWidth;
    int mBorderTopMarginStart;
    int mBorderTopMarginEnd;

    //右边线的drawable,颜色，宽度，和边线padding
    Drawable mBorderRight;
    int mBorderRightColor;
    int mBorderRightWidth;
    int mBorderRightMarginStart;
    int mBorderRightMarginEnd;

    //下边线的drawable颜色，宽度，和边线padding
    Drawable mBorderBottom;
    int mBorderBottomColor;
    int mBorderBottomWidth;
    int mBorderBottomMarginStart;
    int mBorderBottomMarginEnd;

    //内容四个边距的距离。
    private int mContentMarginLeft = 0;
    private int mContentMarginTop = 0;
    private int mContentMarginRight = 0;
    private int mContentMarginBottom = 0;

    //水平方向Item 的间距
    private int mContentMarginHorizontal = 0;
    //垂直方向Item 的间距
    private int mContentMarginVertical = 0;

    private Drawable mDividerHorizontal;
    //水平分割线颜色
    private int mDividerColorHorizontal = 0;
    //水平分割线宽
    private int mDividerWidthHorizontal = 0;
    //水平分割线开始padding
    private int mDividerPaddingHorizontalStart = 0;
    //水平分割线结束padding
    private int mDividerPaddingHorizontalEnd = 0;

    private Drawable mDividerVertical;
    //垂直分割线颜色
    private int mDividerColorVertical = 0;
    //垂直分割线宽
    private int mDividerWidthVertical = 0;
    //垂直分割线开始padding
    private int mDividerPaddingVerticalStart = 0;
    //垂直分割线结束padding
    private int mDividerPaddingVerticalEnd = 0;

    private Callback mCallback;

    private BorderDivider(float density) {
        mDensity = density;
        mContentMarginHorizontal *= density;
        mContentMarginVertical *= density;
        mDividerWidthHorizontal = (int) (0.5f + density * 0.5f);
        mDividerWidthVertical = (int) (0.5f + density * 0.5f);
        mDividerPaddingHorizontalStart *= density;
        mDividerPaddingVerticalStart *= density;
        mDividerPaddingHorizontalEnd *= density;
        mDividerPaddingVerticalEnd *= density;
        mBorderLeftWidth = (int) (0.5f + density * 0.5f);
        mBorderLeftMarginStart *= density;
        mBorderLeftMarginEnd *= density;
        mBorderTopWidth = (int) (0.5f + density * 0.5f);
        mBorderTopMarginStart *= density;
        mBorderTopMarginEnd *= density;
        mBorderRightWidth = (int) (0.5f + density * 0.5f);
        mBorderRightMarginStart *= density;
        mBorderRightMarginEnd *= density;
        mBorderBottomWidth = (int) (0.5f + density * 0.5f);
        mBorderBottomMarginStart *= density;
        mBorderBottomMarginEnd *= density;
        mContentMarginLeft *= density;
        mContentMarginTop *= density;
        mContentMarginRight *= density;
        mContentMarginBottom *= density;
    }

    public int dip(float dip) {
        return (int) (0.5f + dip * mDensity);
    }

    //start border left settings,include drawable,color,width,margin.
    public void setBorderLeft(Drawable borderLeft) {
        if (mBorderLeft != borderLeft) {
            mBorderLeft = borderLeft;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderLeftColor(int color) {
        if (mBorderLeftColor != color) {
            mBorderLeftColor = color;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderLeftWidth(int width) {
        if (mBorderLeftWidth != width) {
            mBorderLeftWidth = width;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderLeftMarginStart(int marginStart) {
        if (mBorderLeftMarginStart != marginStart) {
            mBorderLeftMarginStart = marginStart;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderLeftMarginEnd(int marginEnd) {
        if (mBorderLeftMarginEnd != marginEnd) {
            mBorderLeftMarginEnd = marginEnd;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    //start border top settings,include drawable,color,width,margin.
    public void setBorderTop(Drawable borderTop) {
        if (mBorderTop != borderTop) {
            mBorderTop = borderTop;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderTopColor(int color) {
        if (mBorderTopColor != color) {
            mBorderTopColor = color;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderTopWidth(int width) {
        if (mBorderTopWidth != width) {
            mBorderTopWidth = width;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderTopMarginStart(int marginStart) {
        if (mBorderTopMarginStart != marginStart) {
            mBorderTopMarginStart = marginStart;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderTopMarginEnd(int marginEnd) {
        if (mBorderTopMarginEnd != marginEnd) {
            mBorderTopMarginEnd = marginEnd;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    //start border right settings,include drawable,color,width,margin.
    public void setBorderRight(Drawable borderRight) {
        if (mBorderRight != borderRight) {
            mBorderRight = borderRight;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderRightColor(int color) {
        if (mBorderRightColor != color) {
            mBorderRightColor = color;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderRightWidth(int width) {
        if (mBorderRightWidth != width) {
            mBorderRightWidth = width;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderRightMarginStart(int marginStart) {
        if (mBorderRightMarginStart != marginStart) {
            mBorderRightMarginStart = marginStart;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderRightMarginEnd(int marginEnd) {
        if (mBorderRightMarginEnd != marginEnd) {
            mBorderRightMarginEnd = marginEnd;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    //start border bottom settings,include drawable,color,width,margin.
    public void setBorderBottom(Drawable borderBottom) {
        if (mBorderBottom != borderBottom) {
            mBorderBottom = borderBottom;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderBottomColor(int color) {
        if (mBorderBottomColor != color) {
            mBorderBottomColor = color;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderBottomWidth(int width) {
        if (mBorderBottomWidth != width) {
            mBorderBottomWidth = width;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderBottomMarginStart(int marginStart) {
        if (mBorderBottomMarginStart != marginStart) {
            mBorderBottomMarginStart = marginStart;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setBorderBottomMarginEnd(int marginEnd) {
        if (mBorderBottomMarginEnd != marginEnd) {
            mBorderBottomMarginEnd = marginEnd;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }


    //start whole content margin settings.
    public void setContentMarginLeft(int contentMarginLeft) {
        if (mContentMarginLeft != contentMarginLeft) {
            mContentMarginLeft = contentMarginLeft;
            if (mCallback != null) {
                mCallback.requestLayout();
            }
        }
    }

    public void setContentMarginTop(int contentMarginTop) {
        if (mContentMarginTop != contentMarginTop) {
            mContentMarginTop = contentMarginTop;
            if (mCallback != null) {
                mCallback.requestLayout();
            }
        }
    }

    public void setContentMarginRight(int contentMarginRight) {
        if (mContentMarginRight != contentMarginRight) {
            mContentMarginRight = contentMarginRight;
            if (mCallback != null) {
                mCallback.requestLayout();
            }
        }
    }

    public void setContentMarginBottom(int contentMarginBottom) {
        if (mContentMarginBottom != contentMarginBottom) {
            mContentMarginBottom = contentMarginBottom;
            if (mCallback != null) {
                mCallback.requestLayout();
            }
        }
    }

    //start content item margin settings.
    public void setContentMarginHorizontal(int contentMarginHorizontal) {
        if (mContentMarginHorizontal != contentMarginHorizontal) {
            mContentMarginHorizontal = contentMarginHorizontal;
            if (mCallback != null) {
                mCallback.requestLayout();
            }
        }
    }

    public void setContentMarginVertical(int contentMarginVertical) {
        if (mContentMarginVertical != contentMarginVertical) {
            mContentMarginVertical = contentMarginVertical;
            if (mCallback != null) {
                mCallback.requestLayout();
            }
        }
    }


    //start divider setting at vertical direction
    public void setDividerDrawableVertical(Drawable drawableVertical) {
        if (mDividerVertical != drawableVertical) {
            mDividerVertical = drawableVertical;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerColorVertical(int color) {
        if (mDividerColorVertical != color) {
            mDividerColorVertical = color;
            mResetPaintVertical = true;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerWidthVertical(int width) {
        if (mDividerWidthVertical != width) {
            mDividerWidthVertical = width;
            mResetPaintVertical = true;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerPaddingVerticalStart(int paddingStart) {
        if (mDividerPaddingVerticalStart != paddingStart) {
            mDividerPaddingVerticalStart = paddingStart;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerPaddingVerticalEnd(int paddingEnd) {
        if (mDividerPaddingVerticalEnd != paddingEnd) {
            mDividerPaddingVerticalEnd = paddingEnd;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    //start divider setting at horizontal direction
    public void setDividerDrawableHorizontal(Drawable drawableHorizontal) {
        if (mDividerHorizontal != drawableHorizontal) {
            mDividerHorizontal = drawableHorizontal;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerColorHorizontal(int color) {
        if (mDividerColorHorizontal != color) {
            mDividerColorHorizontal = color;
            mResetPaintHorizontal = true;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerWidthHorizontal(int width) {
        if (mDividerWidthHorizontal != width) {
            mDividerWidthHorizontal = width;
            mResetPaintHorizontal = true;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerPaddingHorizontalStart(int paddingStart) {
        if (mDividerPaddingHorizontalStart != paddingStart) {
            mDividerPaddingHorizontalStart = paddingStart;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    public void setDividerPaddingHorizontalEnd(int paddingEnd) {
        if (mDividerPaddingHorizontalEnd != paddingEnd) {
            mDividerPaddingHorizontalEnd = paddingEnd;
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    }

    //start get border left settings
    public Drawable getBorderLeft() {
        return mBorderLeft;
    }

    public int getBorderLeftColor() {
        return mBorderLeftColor;
    }

    public int getBorderLeftWidth() {
        return mBorderLeftWidth;
    }

    public int getBorderLeftMarginStart() {
        return mBorderLeftMarginStart;
    }

    public int getBorderLeftMarginEnd() {
        return mBorderLeftMarginEnd;
    }


    //start get border top settings
    public Drawable getBorderTop() {
        return mBorderTop;
    }

    public int getBorderTopColor() {
        return mBorderTopColor;
    }

    public int getBorderTopWidth() {
        return mBorderTopWidth;
    }

    public int getBorderTopMarginStart() {
        return mBorderTopMarginStart;
    }

    public int getBorderTopMarginEnd() {
        return mBorderTopMarginEnd;
    }


    //start get border right settings
    public Drawable getBorderRight() {
        return mBorderRight;
    }

    public int getBorderRightColor() {
        return mBorderRightColor;
    }

    public int getBorderRightWidth() {
        return mBorderRightWidth;
    }

    public int getBorderRightMarginStart() {
        return mBorderRightMarginStart;
    }

    public int getBorderRightMarginEnd() {
        return mBorderRightMarginEnd;
    }


    //start get border bottom settings
    public Drawable getBorderBottom() {
        return mBorderBottom;
    }

    public int getBorderBottomColor() {
        return mBorderBottomColor;
    }

    public int getBorderBottomWidth() {
        return mBorderBottomWidth;
    }

    public int getBorderBottomMarginStart() {
        return mBorderBottomMarginStart;
    }

    public int getBorderBottomMarginEnd() {
        return mBorderBottomMarginEnd;
    }


    //start get  whole content margin settings
    public int getContentMarginLeft() {
        return mContentMarginLeft;
    }

    public int getContentMarginTop() {
        return mContentMarginTop;
    }

    public int getContentMarginRight() {
        return mContentMarginRight;
    }

    public int getContentMarginBottom() {
        return mContentMarginBottom;
    }

    //start get content item margin settings
    public int getContentMarginHorizontal() {
        return mContentMarginHorizontal;
    }

    public int getContentMarginVertical() {
        return mContentMarginVertical;
    }

    //start get divider settings at horizontal direction
    public Drawable getDividerHorizontal() {
        return mDividerHorizontal;
    }

    public int getDividerWidthHorizontal() {
        return mDividerWidthHorizontal;
    }

    public int getDividerColorHorizontal() {
        return mDividerColorHorizontal;
    }

    public int getDividerPaddingHorizontalStart() {
        return mDividerPaddingHorizontalStart;
    }

    public int getDividerPaddingHorizontalEnd() {
        return mDividerPaddingHorizontalEnd;
    }


    //start get divider settings at vertical direction
    public Drawable getDividerVertical() {
        return mDividerVertical;
    }

    public int getDividerWidthVertical() {
        return mDividerWidthVertical;
    }

    public int getDividerColorVertical() {
        return mDividerColorVertical;
    }

    public int getDividerPaddingVerticalStart() {
        return mDividerPaddingVerticalStart;
    }

    public int getDividerPaddingVerticalEnd() {
        return mDividerPaddingVerticalEnd;
    }

    public boolean isVisibleDividerHorizontal(boolean initPaintIfNeed) {
        boolean result = mDividerWidthHorizontal > 0 && (mDividerColorHorizontal != 0 || mDividerHorizontal != null);
        if (initPaintIfNeed && result) {
            if (mPaintHorizontal == null) {
                mPaintHorizontal = new Paint();
                mPaintHorizontal.setStyle(Paint.Style.FILL);
            }
            if (mResetPaintHorizontal) {
                mPaintHorizontal.setStrokeWidth(mDividerWidthHorizontal);
                mPaintHorizontal.setColor(mDividerColorHorizontal);
                mResetPaintHorizontal = false;
            }
        }
        return result;
    }

    public boolean isVisibleDividerVertical(boolean initPaintIfNeed) {
        boolean result = mDividerWidthVertical > 0 || (mDividerColorVertical != 0 && mDividerVertical != null);
        if (initPaintIfNeed && result) {
            if (mPaintVertical == null) {
                mPaintVertical = new Paint();
                mPaintVertical.setStyle(Paint.Style.FILL);
            }
            if (mResetPaintVertical) {
                mPaintVertical.setStrokeWidth(mDividerWidthVertical);
                mPaintVertical.setColor(mDividerColorVertical);
                mResetPaintVertical = false;
            }
        }
        return result;
    }

    public void applyContentMargin(Rect outRect) {
        outRect.left += mContentMarginLeft;
        outRect.top += mContentMarginTop;
        outRect.right += mContentMarginRight;
        outRect.bottom += mContentMarginBottom;
    }

    public void drawBorder(Canvas canvas, int viewWidth, int viewHeight) {
        boolean drawLeft = mBorderLeftColor != 0 && mBorderLeftWidth > 0;
        boolean drawTop = mBorderTopColor != 0 && mBorderTopWidth > 0;
        boolean drawRight = mBorderRightColor != 0 && mBorderRightWidth > 0;
        boolean drawBottom = mBorderBottomColor != 0 && mBorderBottomWidth > 0;
        if (drawLeft || drawTop || drawRight || drawBottom) {
            if (mPaintBorder == null) {
                mPaintBorder = new Paint();
                mPaintBorder.setStyle(Paint.Style.FILL);
            }
            float startX, startY, endX, endY;
            if (drawLeft) {
                startY = mBorderLeftMarginStart;
                endY = viewHeight - mBorderLeftMarginEnd;
                if (mBorderLeft == null) {
                    mPaintBorder.setColor(mBorderLeftColor);
                    mPaintBorder.setStrokeWidth(mBorderLeftWidth);
                    startX = endX = mBorderLeftWidth / 2;
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder);
                } else {
                    startX = 0;
                    endX = mBorderLeftWidth;
                    mBorderLeft.setBounds((int) (startX), (int) (startY), (int) (endX), (int) (endY));
                    mBorderLeft.draw(canvas);
                }
            }
            if (drawRight) {
                startY = mBorderRightMarginStart;
                endY = viewHeight - mBorderRightMarginEnd;
                if (mBorderRight == null) {
                    mPaintBorder.setColor(mBorderRightColor);
                    mPaintBorder.setStrokeWidth(mBorderRightWidth);
                    startX = endX = viewWidth - mBorderRightWidth / 2;
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder);
                } else {
                    startX = viewWidth - mBorderLeftWidth;
                    endX = viewWidth;
                    mBorderRight.setBounds((int) (startX), (int) (startY), (int) (endX), (int) (endY));
                    mBorderRight.draw(canvas);
                }
            }
            if (drawTop) {
                startX = mBorderTopMarginStart;
                endX = viewWidth - mBorderTopMarginEnd;
                if (mBorderTop == null) {
                    mPaintBorder.setColor(mBorderTopColor);
                    mPaintBorder.setStrokeWidth(mBorderTopWidth);
                    startY = endY = mBorderTopWidth / 2;
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder);
                } else {
                    startY = 0;
                    endY = mBorderTopWidth;
                    mBorderTop.setBounds((int) (startX), (int) (startY), (int) (endX), (int) (endY));
                    mBorderTop.draw(canvas);
                }
            }
            if (drawBottom) {
                startX = mBorderBottomMarginStart;
                endX = viewWidth - mBorderBottomMarginEnd;
                if (mBorderBottom == null) {
                    mPaintBorder.setColor(mBorderBottomColor);
                    mPaintBorder.setStrokeWidth(mBorderBottomWidth);
                    startY = endY = viewHeight - mBorderBottomWidth / 2;
                    canvas.drawLine(startX, startY, endX, endY, mPaintBorder);
                } else {
                    startY = viewHeight - mBorderBottomWidth;
                    endY = viewHeight;
                    mBorderBottom.setBounds((int) (startX), (int) (startY), (int) (endX), (int) (endY));
                    mBorderBottom.draw(canvas);
                }
            }
        }
    }

    public void drawDividerH(Canvas canvas, float xStart, float xEnd, float y) {
        xStart += mDividerPaddingHorizontalStart;
        xEnd -= mDividerPaddingHorizontalEnd;
        if (xEnd > xStart && mPaintHorizontal != null) {
            if (mDividerHorizontal == null) {
                canvas.drawLine(xStart, y, xEnd, y, mPaintHorizontal);
            } else {
                float paintWidth = (int) ((mPaintHorizontal.getStrokeWidth() + 0.5f) / 2);
                mDividerHorizontal.setBounds((int) xStart, (int) (y - paintWidth), (int) xEnd, (int) (y + paintWidth));
                mDividerHorizontal.draw(canvas);
            }

        }
    }

    public void drawDividerV(Canvas canvas, float yStart, float yEnd, float x) {
        yStart += mDividerPaddingVerticalStart;
        yEnd -= mDividerPaddingVerticalEnd;
        if (yEnd > yStart && mPaintVertical != null) {
            if (mDividerVertical == null) {
                canvas.drawLine(x, yStart, x, yEnd, mPaintVertical);
            } else {
                float paintWidth = (int) ((mPaintHorizontal.getStrokeWidth() + 0.5f) / 2);
                mDividerVertical.setBounds((int) (x - paintWidth), (int) yStart, (int) (x + paintWidth), (int) yEnd);
                mDividerVertical.draw(canvas);
            }
        }
    }

    public static BorderDivider from(Context context, TypedArray attr) {
        BorderDivider dm = new BorderDivider(context.getResources().getDisplayMetrics().density);
        if (attr != null) {
            if (attr.hasValue(R.styleable.BaseViewGroup_borderLeft)) {
                dm.mBorderLeft = attr.getDrawable(R.styleable.BaseViewGroup_borderLeft);
            }
            dm.mBorderLeftColor = attr.getColor(R.styleable.BaseViewGroup_borderLeftColor, dm.mBorderLeftColor);
            dm.mBorderLeftWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftWidth, dm.mBorderLeftWidth);
            boolean hasBorderLeftMargin = attr.hasValue(R.styleable.BaseViewGroup_borderLeftMargin);
            int borderLeftMargin = hasBorderLeftMargin ? attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftMargin, 0) : 0;
            dm.mBorderLeftMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftMarginStart, hasBorderLeftMargin ? borderLeftMargin : dm.mBorderLeftMarginStart);
            dm.mBorderLeftMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderLeftMarginEnd, hasBorderLeftMargin ? borderLeftMargin : dm.mBorderLeftMarginEnd);

            if (attr.hasValue(R.styleable.BaseViewGroup_borderTop)) {
                dm.mBorderTop = attr.getDrawable(R.styleable.BaseViewGroup_borderTop);
            }
            dm.mBorderTopColor = attr.getColor(R.styleable.BaseViewGroup_borderTopColor, dm.mBorderTopColor);
            dm.mBorderTopWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopWidth, dm.mBorderTopWidth);
            boolean hasBorderTopMargin = attr.hasValue(R.styleable.BaseViewGroup_borderTopMargin);
            int borderTopMargin = hasBorderTopMargin ? attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopMargin, 0) : 0;
            dm.mBorderTopMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopMarginStart, hasBorderTopMargin ? borderTopMargin : dm.mBorderTopMarginStart);
            dm.mBorderTopMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderTopMarginEnd, hasBorderTopMargin ? borderTopMargin : dm.mBorderTopMarginEnd);

            if (attr.hasValue(R.styleable.BaseViewGroup_borderRight)) {
                dm.mBorderRight = attr.getDrawable(R.styleable.BaseViewGroup_borderRight);
            }
            dm.mBorderRightColor = attr.getColor(R.styleable.BaseViewGroup_borderRightColor, dm.mBorderRightColor);
            dm.mBorderRightWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightWidth, dm.mBorderRightWidth);
            boolean hasBorderRightMargin = attr.hasValue(R.styleable.BaseViewGroup_borderRightMargin);
            int borderRightMargin = hasBorderRightMargin ? attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightMargin, 0) : 0;
            dm.mBorderRightMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightMarginStart, hasBorderRightMargin ? borderRightMargin : dm.mBorderRightMarginStart);
            dm.mBorderRightMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderRightMarginEnd, hasBorderRightMargin ? borderRightMargin : dm.mBorderRightMarginEnd);

            if (attr.hasValue(R.styleable.BaseViewGroup_borderBottom)) {
                dm.mBorderBottom = attr.getDrawable(R.styleable.BaseViewGroup_borderBottom);
            }
            dm.mBorderBottomColor = attr.getColor(R.styleable.BaseViewGroup_borderBottomColor, dm.mBorderBottomColor);
            dm.mBorderBottomWidth = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomWidth, dm.mBorderBottomWidth);
            boolean hasBorderBottomMargin = attr.hasValue(R.styleable.BaseViewGroup_borderBottomMargin);
            int borderBottomMargin = hasBorderBottomMargin ? attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomMargin, 0) : 0;
            dm.mBorderBottomMarginStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomMarginStart, hasBorderBottomMargin ? borderBottomMargin : dm.mBorderBottomMarginStart);
            dm.mBorderBottomMarginEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_borderBottomMarginEnd, hasBorderBottomMargin ? borderBottomMargin : dm.mBorderBottomMarginEnd);

            dm.mContentMarginLeft = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginLeft, dm.mContentMarginLeft);
            dm.mContentMarginTop = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginTop, dm.mContentMarginTop);
            dm.mContentMarginRight = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginRight, dm.mContentMarginRight);
            dm.mContentMarginBottom = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginBottom, dm.mContentMarginBottom);

            dm.mContentMarginHorizontal = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginHorizontal, dm.mContentMarginHorizontal);
            dm.mContentMarginVertical = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginVertical, dm.mContentMarginVertical);

            dm.mDividerColorHorizontal = attr.getColor(R.styleable.BaseViewGroup_dividerColorHorizontal, dm.mDividerColorHorizontal);
            dm.mDividerWidthHorizontal = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerWidthHorizontal, dm.mDividerWidthHorizontal);
            boolean hasDividerPaddingHorizontal = attr.hasValue(R.styleable.BaseViewGroup_dividerPaddingHorizontal);
            int dividerPaddingHorizontal = hasDividerPaddingHorizontal ? attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingHorizontal, 0) : 0;
            dm.mDividerPaddingHorizontalStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingHorizontalStart, hasDividerPaddingHorizontal ? dividerPaddingHorizontal : dm.mDividerPaddingHorizontalStart);
            dm.mDividerPaddingHorizontalEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingHorizontalEnd, hasDividerPaddingHorizontal ? dividerPaddingHorizontal : dm.mDividerPaddingHorizontalEnd);

            dm.mDividerColorVertical = attr.getColor(R.styleable.BaseViewGroup_dividerColorVertical, dm.mDividerColorVertical);
            dm.mDividerWidthVertical = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerWidthVertical, dm.mDividerWidthVertical);
            boolean hasDividerPaddingVertical = attr.hasValue(R.styleable.BaseViewGroup_dividerPaddingVertical);
            int dividerPaddingVertical = hasDividerPaddingVertical ? attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingVertical, 0) : 0;
            dm.mDividerPaddingVerticalStart = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingVerticalStart, hasDividerPaddingVertical ? dividerPaddingVertical : dm.mDividerPaddingVerticalStart);
            dm.mDividerPaddingVerticalEnd = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_dividerPaddingVerticalEnd, hasDividerPaddingVertical ? dividerPaddingVertical : dm.mDividerPaddingVerticalEnd);

        }
        return dm;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    interface Callback {
        void requestLayout();

        void invalidate();
    }
}
