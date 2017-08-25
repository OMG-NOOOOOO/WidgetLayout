package com.rexy.widgets.layout

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.rexy.widgetlayout.R
import com.rexy.widgets.adapter.ItemProvider
import com.rexy.widgets.view.CheckText
import java.util.*

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-06-02 15:47
 */
class LabelLayout : WrapLayout {

    private var mTextSize = 15
    var labelBackground: Drawable? = null
        set(labelBackground) {
            if (field !== labelBackground) {
                field = labelBackground
            }
        }
    private var mTextColorList: ColorStateList? = null

    var itemProvider: ItemProvider? = null
        set(provider) {
            if (itemProvider !== provider) {
                removeAllViewsInLayout()
                field = provider
                if (provider != null) {
                    buildLabels(provider, provider.count)
                }
            }
        }

    private var labelClickListener: OnLabelClickListener? = null
        private set

    private val mCachedView = LinkedList<View>()

    private val mHierarchyChangeListener = object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View, child: View?) {
            if (parent === this@LabelLayout && child != null) {
                child.setOnClickListener(mInnerClicker)
            }
        }

        override fun onChildViewRemoved(parent: View, child: View?) {
            if (parent === this@LabelLayout && child != null) {
                child.setOnClickListener(null)
                if (child.getTag(TAG_VIEW_TYPE) is Int) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        child.setBackgroundDrawable(null)
                    } else {
                        child.background = null
                    }
                    mCachedView.add(child)
                }
            }
        }
    }

    private val mInnerClicker = View.OnClickListener { child ->
        if (labelClickListener != null) {
            labelClickListener!!.onLabelClick(this@LabelLayout, child)
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val density = context.resources.displayMetrics.density
        mTextSize *= density.toInt()
        val attr = if (attrs == null)
            null
        else
            context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textSize, android.R.attr.textColor))
        if (attr != null) {
            mTextSize = attr.getDimensionPixelSize(0, mTextSize)
            try {
                mTextColorList = attr.getColorStateList(1)
                val textColor = attr.getColor(1, 0xFF333333.toInt())
                if (mTextColorList != null && textColor != 0) {
                    mTextColorList = ColorStateList.valueOf(textColor)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            attr.recycle()
        }
        setOnHierarchyChangeListener(mHierarchyChangeListener)
    }

    private fun buildLabels(provider: ItemProvider, itemCount: Int) {
        val viewProvider = provider as? ItemProvider.ViewProvider
        val viewType = viewProvider != null
        for (i in 0 until itemCount) {
            val view: View
            if (viewType) {
                val itemType = viewProvider!!.getViewType(i)
                val cacheView = getCacheView(itemType)
                view = viewProvider.getView(i, cacheView, this@LabelLayout)
                if (view !== cacheView) {
                    view.setTag(TAG_VIEW_TYPE, itemType)
                }
            } else {
                view = makeLabel(provider.getTitle(i), null, null, 0)
            }
            view.tag = provider.getItem(i)
            addView(view)
        }
    }

    private fun getCacheView(viewType: Int): View? {
        val its = mCachedView.iterator()
        while (its.hasNext()) {
            val view = its.next()
            if (view.getTag(TAG_VIEW_TYPE) is Int && view.getTag(TAG_VIEW_TYPE) as Int === viewType) {
                its.remove()
                return view
            }
        }
        return null
    }

    private fun makeLabel(label: CharSequence, background: Drawable?, textColor: ColorStateList?, textSize: Int): CheckText {
        var background = background
        var textColor = textColor
        var textSize = textSize
        var tab = getCacheView(TAG_SPECIAL) as CheckText?
        if (tab == null) {
            tab = CheckText(context)
            tab.gravity = Gravity.CENTER
            tab.setSingleLine()
            tab.includeFontPadding = false
            tab.setTag(TAG_VIEW_TYPE, TAG_SPECIAL)
        }
        tab.text = label
        background = if (background == null) labelBackground else background
        textColor = if (textColor == null) mTextColorList else textColor
        textSize = if (textSize <= 0) mTextSize else textSize
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        if (textColor != null && tab.textColors !== textColor) {
            tab.setTextColor(textColor)
        }
        if (tab.background !== background) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                tab.setBackgroundDrawable(background)
            } else {
                tab.background = background
            }
        }
        return tab
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnHierarchyChangeListener(mHierarchyChangeListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setOnHierarchyChangeListener(null)
        mCachedView.clear()
    }

    var textSizePixel: Int
        get() = mTextSize
        set(textSize) {
            if (mTextSize != textSize) {
                mTextSize = textSize
            }
        }

    var textColorList: ColorStateList?
        get() = mTextColorList
        set(colorList) {
            if (mTextColorList !== colorList) {
                mTextColorList = colorList
            }
        }

    fun setOnLabelClickListener(l: OnLabelClickListener) {
        labelClickListener = l
    }

    fun setTextColor(textColor: Int) {
        if (textColor != 0 && (mTextColorList == null || mTextColorList!!.defaultColor != textColor)) {
            mTextColorList = ColorStateList.valueOf(textColor)
        }
    }

    interface OnLabelClickListener {
        fun onLabelClick(parent: LabelLayout, labelView: View)
    }

    companion object {
        private val TAG_VIEW_TYPE = R.id.widgetLayoutViewIndexType
        private val TAG_SPECIAL = TAG_VIEW_TYPE
    }

}