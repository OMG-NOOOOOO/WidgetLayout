package com.rexy.widgets.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.TextView;

/**
 * A extended TextView which has feature like CheckBox
 */
public class CheckText extends TextView implements Checkable {
    /**
     * we need to merge state_checked when  {@link #onCreateDrawableState} into old interest drawable state .
     */
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    /**
     * indicate whether it's checked
     */
    protected boolean mChecked = false;

    /**
     * whether this widget support to change its check state
     */
    protected boolean mCheckEnable = true;

    /**
     * whether this widget can click to change its check state.
     */
    protected boolean mClickCheckEnable = false;

    /**
     * text to display when it's checked
     */
    CharSequence mTextOn;

    /**
     * text to display when it is unchecked
     */
    CharSequence mTextOff;



    public CheckText(Context context) {
        super(context);
    }

    public CheckText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * set text to display at different check state
     * @param textOn text to display when it is checked
     * @param textOff text to display when it is unchecked
     */
    public void setTextState(CharSequence textOn, CharSequence textOff) {
        if (!TextUtils.isEmpty(textOn) && !TextUtils.isEmpty(textOff)) {
            mTextOn = textOn;
            mTextOff = textOff;
            setText(mChecked ? mTextOn : mTextOff);
        }
    }

    /**
     * set to support change check state or not
     * @param checkable true support change it's check state
     */
    public void setCheckAble(boolean checkable) {
        mCheckEnable = checkable;
    }

    /**
     * whether this widget is supported to change its check state
     * @return true if supported otherwise false
     */
    public boolean isCheckAble() {
        return mCheckEnable;
    }

    /**
     * whether this widget is supported to change its check state by click
     * @return true if supported otherwise false
     */
    public void setClickCheckAble(boolean checkable) {
        mClickCheckEnable = checkable;
    }

    /**
     * whether it's supported to change its state by click
     * @return true supported otherwise false
     */
    public boolean isClickCheckAble() {
        return mClickCheckEnable;
    }

    /**
     * whether this widget is checked
     * @return true if checked otherwise false
     */
    @Override
    public boolean isChecked() {
        return mChecked;
    }

    /**
     * set check state
     */
    public void setChecked(boolean checked) {
        if (mCheckEnable) {
            if (mChecked != checked) {
                mChecked = checked;
                if (!TextUtils.isEmpty(mTextOn)) {
                    setText(checked ? mTextOn : mTextOff);
                }
                refreshDrawableState();
            }
        }
    }

    @Override
    public boolean performClick() {
        /*
         * XXX: These are tiny, need some surrounding 'expanded touch area',
		 * which will need to be implemented in Button if we only override
		 * performClick()
		 */
        /* When clicked, toggle the state */
        if (mClickCheckEnable) {
            toggle();
        }
        return super.performClick();
    }

    /**
     * set checked if it's unchecked or set unchecked if it's checked
     */
    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}