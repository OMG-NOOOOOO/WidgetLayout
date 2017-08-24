package com.rexy.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.adapter.ItemProvider
import com.rexy.widgets.layout.LabelLayout
import com.rexy.widgets.layout.WrapLayout

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-28 10:27
 */
class FragmentWrapLabelLayout : FragmentViewPicker() {
    private val mLabelLayout by lazy { view?.findViewById(R.id.labelLayout) as LabelLayout }
    private val mWrapLayout by lazy { view?.findViewById(R.id.wrapLayout) as  WrapLayout}
    private val mToggleOpt by lazy { view?.findViewById(R.id.toggleOptView) as ToggleButton }

    private var mMinRandomWidth = 45
    private var mMaxRandomWidth = 90
    private var mMinRandomHeight = 35
    private var mMaxRandomHeight = 70

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_wraplabel, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewProperties(mLabelLayout, mWrapLayout)
        buildRandomView(7, true)
        mToggleOpt.isChecked = !mToggleOpt.isChecked
    }

    private fun initViewProperties(labelLayout: LabelLayout, wrapLayout: WrapLayout) {
        mMinRandomWidth = (mDensity * mMinRandomWidth).toInt()
        mMaxRandomWidth = (mDensity * mMaxRandomWidth).toInt()
        mMinRandomHeight = (mDensity * mMinRandomHeight).toInt()
        mMaxRandomHeight = (mDensity * mMaxRandomHeight).toInt()
        val mLabels = arrayOf("A", "B", "C", "D", "E", "F", "G", "H")
        labelLayout.itemProvider = object : ItemProvider.ViewProvider {
            override val count: Int
                get()  = mLabels.size //To change initializer of created properties use File | Settings | File Templates.

            override fun getViewType(position: Int) = 0

            override fun getView(position: Int, convertView: View?, parent: ViewGroup) = buildView(getTitle(position), true)

            override fun getTitle(position: Int) = mLabels[position]

            override fun getItem(position: Int) = mLabels[position]
        }

        mLabelLayout.setOnLabelClickListener(object :LabelLayout.OnLabelClickListener{
            override fun onLabelClick(parent: LabelLayout, labelView: View) {
                val tag = labelView.tag
                var text: CharSequence? = tag?.toString()
                if (text == null && labelView is TextView) {
                    text = labelView.text
                }
                if (text != null) {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun buildRandomSize(minSize: Int, maxSize: Int): Int {
        if (maxSize > minSize && minSize > 0) {
            mRandom.setSeed(System.currentTimeMillis() + mRandom.nextInt(maxSize))
            return minSize + mRandom.nextInt(maxSize - minSize + 1)
        }
        return -1
    }

    override fun buildView(minWidth: Int, maxWidth: Int, minHeight: Int, maxHeight: Int, gravity: Int, color: Int, random: Boolean): View {
        var minWidth = minWidth
        var minHeight = minHeight
        if (random) {
            minWidth = buildRandomSize(mMinRandomWidth, mMaxRandomWidth)
            minHeight = buildRandomSize(mMinRandomHeight, mMaxRandomHeight)
        }
        return super.buildView(minWidth, maxWidth, minHeight, maxHeight, gravity, color, random)
    }

    override fun onAddOrRemoveView(addView: View?) {
        with(if(mToggleOpt.isChecked) mLabelLayout else mWrapLayout){
            if(addView==null){
                if(childCount>0){
                    removeViewAt(childCount - 1)
                }
            }else{
                addView(addView)
            }
        }
    }
}
