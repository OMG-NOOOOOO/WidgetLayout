package com.rexy.widgets.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.TextView

/**
 * A extended TextView which has feature like CheckBox
 */
open class CheckText : TextView, Checkable {

    /**
     * indicate whether it's checked
     */
    protected var mChecked = false

    /**
     * whether this widget support to change its check state
     */
    /**
     * whether this widget is supported to change its check state
     * @return true if supported otherwise false
     */
    /**
     * set to support change check state or not
     * @param checkable true support change it's check state
     */
    var isCheckAble = true

    /**
     * whether this widget can click to change its check state.
     */
    /**
     * whether it's supported to change its state by click
     * @return true supported otherwise false
     */
    /**
     * whether this widget is supported to change its check state by click
     * @return true if supported otherwise false
     */
    var isClickCheckAble = false

    /**
     * text to display when it's checked
     */
    internal var mTextOn: CharSequence?=null

    /**
     * text to display when it is unchecked
     */
    internal var mTextOff: CharSequence?=null


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    /**
     * set text to display at different check state
     * @param textOn text to display when it is checked
     * @param textOff text to display when it is unchecked
     */
    fun setTextState(textOn: CharSequence, textOff: CharSequence) {
        if (!TextUtils.isEmpty(textOn) && !TextUtils.isEmpty(textOff)) {
            mTextOn = textOn
            mTextOff = textOff
            text = if (mChecked) mTextOn else mTextOff
        }
    }

    /**
     * whether this widget is checked
     * @return true if checked otherwise false
     */
    override fun isChecked(): Boolean {
        return mChecked
    }

    /**
     * set check state
     */
    override fun setChecked(checked: Boolean) {
        if (isCheckAble) {
            if (mChecked != checked) {
                mChecked = checked
                if (!TextUtils.isEmpty(mTextOn)) {
                    text = if (checked) mTextOn else mTextOff
                }
                refreshDrawableState()
            }
        }
    }

    override fun performClick(): Boolean {
        /*
         * XXX: These are tiny, need some surrounding 'expanded touch area',
		 * which will need to be implemented in Button if we only override
		 * performClick()
		 */
        /* When clicked, toggle the state */
        if (isClickCheckAble) {
            toggle()
        }
        return super.performClick()
    }

    /**
     * set checked if it's unchecked or set unchecked if it's checked
     */
    override fun toggle() {
        isChecked = !mChecked
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    companion object {
        /**
         * we need to merge state_checked when  [.onCreateDrawableState] into old interest drawable state .
         */
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }
}
