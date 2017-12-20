package com.rexy.widgets.layout;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;

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
 * <!-- content margin just like margin but different-->
 * <attr name="contentMarginHorizontal" format="dimension" />
 * <attr name="contentMarginVertical" format="dimension" />
 * <attr name="contentMargin" format="dimension" />
 * <attr name="contentMarginLeft" format="dimension"/>
 * <attr name="contentMarginTop" format="dimension"/>
 * <attr name="contentMarginRight" format="dimension"/>
 * <attr name="contentMarginBottom" format="dimension"/>
 * <!--item margin between at orientation of horizontal and vertical-->
 * <attr name="itemMarginBetween" format="dimension"/>
 * <attr name="itemMarginHorizontal" format="dimension"/>
 * <attr name="itemMarginVertical" format="dimension"/>
 * <p>
 * <p>
 * <!--水平分割线Drawable-->
 * <attr name="dividerHorizontal" format="reference"/>
 * <!--水平分割线颜色-->
 * <attr name="dividerColorHorizontal" format="color"/>
 * <!--水平分割线宽-->
 * <attr name="dividerWidthHorizontal" format="dimension"/>
 * <!--水平分割线开始和结束padding-->
 * <attr name="dividerPaddingHorizontal" format="dimension"/>
 * <attr name="dividerPaddingHorizontalStart" format="dimension"/>
 * <attr name="dividerPaddingHorizontalEnd" format="dimension"/>
 * <p>
 * <!--垂直分割线Drawable-->
 * <attr name="dividerVertical" format="reference"/>
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
    Rect mContentMargin = new Rect();
    int mItemMarginHorizontal;
    int mItemMarginVertical;
    DividerDrawable mBorderLeft;
    DividerDrawable mBorderTop;
    DividerDrawable mBorderRight;
    DividerDrawable mBorderBottom;

    DividerDrawable mDividerHorizontal;
    DividerDrawable mDividerVertical;

    private BorderDivider(TypedArray attr, int dividerWidthDefault) {
        int widthDefault = attr == null ? dividerWidthDefault : attr.getDimensionPixelSize(R.styleable.BaseViewGroup_defaultBorderDividerWidth, dividerWidthDefault);
        mBorderLeft = DividerDrawable.from(attr, widthDefault,
                R.styleable.BaseViewGroup_borderLeft,
                R.styleable.BaseViewGroup_borderLeftColor,
                R.styleable.BaseViewGroup_borderLeftWidth,
                R.styleable.BaseViewGroup_borderLeftMargin,
                R.styleable.BaseViewGroup_borderLeftMarginStart,
                R.styleable.BaseViewGroup_borderLeftMarginEnd
        );
        mBorderTop = DividerDrawable.from(attr, widthDefault,
                R.styleable.BaseViewGroup_borderTop,
                R.styleable.BaseViewGroup_borderTopColor,
                R.styleable.BaseViewGroup_borderTopWidth,
                R.styleable.BaseViewGroup_borderTopMargin,
                R.styleable.BaseViewGroup_borderTopMarginStart,
                R.styleable.BaseViewGroup_borderTopMarginEnd
        );
        mBorderRight = DividerDrawable.from(attr, widthDefault,
                R.styleable.BaseViewGroup_borderRight,
                R.styleable.BaseViewGroup_borderRightColor,
                R.styleable.BaseViewGroup_borderRightWidth,
                R.styleable.BaseViewGroup_borderRightMargin,
                R.styleable.BaseViewGroup_borderRightMarginStart,
                R.styleable.BaseViewGroup_borderRightMarginEnd
        );
        mBorderBottom = DividerDrawable.from(attr, widthDefault,
                R.styleable.BaseViewGroup_borderBottom,
                R.styleable.BaseViewGroup_borderBottomColor,
                R.styleable.BaseViewGroup_borderBottomWidth,
                R.styleable.BaseViewGroup_borderBottomMargin,
                R.styleable.BaseViewGroup_borderBottomMarginStart,
                R.styleable.BaseViewGroup_borderBottomMarginEnd
        );
        mDividerHorizontal = DividerDrawable.from(attr, widthDefault,
                R.styleable.BaseViewGroup_dividerHorizontal,
                R.styleable.BaseViewGroup_dividerColorHorizontal,
                R.styleable.BaseViewGroup_dividerWidthHorizontal,
                R.styleable.BaseViewGroup_dividerPaddingHorizontal,
                R.styleable.BaseViewGroup_dividerPaddingHorizontalStart,
                R.styleable.BaseViewGroup_dividerPaddingHorizontalEnd
        );
        mDividerVertical = DividerDrawable.from(attr, widthDefault,
                R.styleable.BaseViewGroup_dividerVertical,
                R.styleable.BaseViewGroup_dividerColorVertical,
                R.styleable.BaseViewGroup_dividerWidthVertical,
                R.styleable.BaseViewGroup_dividerPaddingVertical,
                R.styleable.BaseViewGroup_dividerPaddingVerticalStart,
                R.styleable.BaseViewGroup_dividerPaddingVerticalEnd
        );
        if (attr != null) {
            boolean hasMarginH = attr.hasValue(R.styleable.BaseViewGroup_contentMarginHorizontal);
            boolean hasMarginV = attr.hasValue(R.styleable.BaseViewGroup_contentMarginVertical);
            boolean hasItemMargin = attr.hasValue(R.styleable.BaseViewGroup_itemMargin);
            int itemMargin = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_itemMargin, 0);
            int margin = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMargin, 0);
            int marginH = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginHorizontal, margin);
            int marginV = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginVertical, margin);
            mContentMargin.left = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginLeft, hasMarginH ? marginH : mContentMargin.left);
            mContentMargin.top = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginTop, hasMarginV ? marginV : mContentMargin.top);
            mContentMargin.right = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginRight, hasMarginH ? marginH : mContentMargin.right);
            mContentMargin.bottom = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_contentMarginBottom, hasMarginV ? marginV : mContentMargin.bottom);
            mItemMarginHorizontal = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_itemMarginHorizontal, hasItemMargin ? itemMargin : mItemMarginHorizontal);
            mItemMarginVertical = attr.getDimensionPixelSize(R.styleable.BaseViewGroup_itemMarginVertical, hasItemMargin ? itemMargin : mItemMarginVertical);
        }
    }

    public DividerDrawable getBorderLeft() {
        return mBorderLeft;
    }

    public DividerDrawable getBorderTop() {
        return mBorderTop;
    }

    public DividerDrawable getBorderRight() {
        return mBorderRight;
    }

    public DividerDrawable getBorderBottom() {
        return mBorderBottom;
    }


    public boolean isVisibleDividerHorizontal() {
        return mDividerHorizontal.isValidated(true);
    }

    public boolean isVisibleDividerVertical() {
        return mDividerVertical.isValidated(true);
    }

    public boolean isVisibleBorder() {
        return mBorderLeft.isValidated(false) ||
                mBorderTop.isValidated(false) ||
                mBorderRight.isValidated(false) ||
                mBorderBottom.isValidated(false);
    }

    public void drawBorder(Canvas canvas, int viewWidth, int viewHeight) {
        if (mBorderLeft.isValidated(true)) {
            mBorderLeft.draw(canvas, 0, viewHeight, 0, false);
        }
        if (mBorderRight.isValidated(true)) {
            mBorderRight.draw(canvas, 0, viewHeight, viewWidth - mBorderRight.getDividerWidth(), false);
        }
        if (mBorderTop.isValidated(true)) {
            mBorderTop.draw(canvas, 0, viewWidth, 0, true);
        }
        if (mBorderBottom.isValidated(true)) {
            mBorderBottom.draw(canvas, 0, viewWidth, viewHeight - mBorderBottom.getDividerWidth(), true);
        }
    }

    public void drawDivider(Canvas canvas, int from, int to, int start, boolean horizontal) {
        DividerDrawable drawer = horizontal ? mDividerHorizontal : mDividerVertical;
        drawer.draw(canvas, from, to, start, horizontal);
    }

    public void setContentMarginHorizontal(int contentMarginHorizontal) {
        mContentMargin.left = contentMarginHorizontal;
        mContentMargin.right = contentMarginHorizontal;
    }

    public void setContentMarginVertical(int contentMarginVertical) {
        mContentMargin.top = contentMarginVertical;
        mContentMargin.bottom = contentMarginVertical;
    }

    public void setContentMargin(int left, int top, int right, int bottom) {
        mContentMargin.set(left, top, right, bottom);
    }

    public void setContentMarginLeft(int left) {
        mContentMargin.left = left;
    }

    public void setContentMarginTop(int top) {
        mContentMargin.top = top;
    }

    public void setContentMarginRight(int right) {
        mContentMargin.right = right;
    }

    public void setContentMarginBottom(int bottom) {
        mContentMargin.bottom = bottom;
    }

    public Rect getContentMargin() {
        return mContentMargin;
    }

    public void setItemMarginBetween(int itemMarginBetween) {
        mItemMarginHorizontal = itemMarginBetween;
        mItemMarginVertical = itemMarginBetween;
    }

    public void setItemMarginHorizontal(int itemMarginHorizontal) {
        mItemMarginHorizontal = itemMarginHorizontal;
    }

    public void setItemMarginVertical(int itemMarginVertical) {
        mItemMarginVertical = itemMarginVertical;
    }

    public int getItemMarginHorizontal() {
        return mItemMarginHorizontal;
    }

    public int getItemMarginVertical() {
        return mItemMarginVertical;
    }

    public void applyContentMargin(Rect outRect) {
        outRect.left += mContentMargin.left;
        outRect.top += mContentMargin.top;
        outRect.right += mContentMargin.right;
        outRect.bottom += mContentMargin.bottom;
    }

    public static BorderDivider from(TypedArray attr, int dividerWidthDefault) {
        return new BorderDivider(attr, dividerWidthDefault);
    }
}