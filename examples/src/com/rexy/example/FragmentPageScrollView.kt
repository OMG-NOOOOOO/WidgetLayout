package com.rexy.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ToggleButton

import com.rexy.widgetlayout.example.R
import com.rexy.widgets.layout.BaseViewGroup
import com.rexy.widgets.layout.PageScrollView

/**
 * TODO:功能说明

 * @author: rexy
 * *
 * @date: 2017-06-05 15:02
 */
class FragmentPageScrollView : FragmentPageBase() {

    private var mToggleFloatStart: ToggleButton?=null
    private var mToggleFloatEnd: ToggleButton?=null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return inflater!!.inflate(R.layout.fragment_pagescrollview_scrollview, container, false).also {
          initView(it)
      }
    }

    override fun initView(root: View) {
        super.initView(root)
        mToggleFloatStart = root.findViewById(R.id.toggleFloatFirst) as ToggleButton
        mToggleFloatEnd = root.findViewById(R.id.toggleFloatEnd) as ToggleButton
        mToggleFloatStart!!.setOnCheckedChangeListener(this)
        mToggleFloatEnd!!.setOnCheckedChangeListener(this)
        initPageScrollViewItemClick(mPageScrollView!!)
    }

    public override fun setContentOrientationInner(vertical: Boolean, init: Boolean): Boolean {
        adjustFloatViewParams(vertical)
        val handled = super.setContentOrientationInner(vertical, init)
        if (handled && init) {
            adjustFloatIndex(true, mToggleFloatStart!!.isChecked)
            adjustFloatIndex(false, mToggleFloatEnd!!.isChecked)
        }
        return handled
    }

    private fun initPageScrollViewItemClick(scrollView: PageScrollView) {
        scrollView.setLogTag("dev", false)
        val pageClick1 = View.OnClickListener { v ->
            val index = scrollView.indexOfItemView(v)
            if (index >= 0) {
                scrollView.scrollTo(index, 0, -1)
            }
        }
        val pageItemCount = scrollView.itemViewCount
        for (i in 0 until pageItemCount) {
            scrollView.getItemView(i)!!.setOnClickListener(pageClick1)
        }
    }

    private fun adjustFloatViewParams(vertical: Boolean) {
        val pageItemCount = mPageScrollView!!.itemViewCount
        if (pageItemCount >= 2) {
            val lp1 = mPageScrollView!!.getItemView(0)!!.layoutParams as BaseViewGroup.LayoutParams
            val lp2 = mPageScrollView!!.getItemView(pageItemCount - 1)!!.layoutParams as BaseViewGroup.LayoutParams
            val sizeShort = (mDensity * 60).toInt()
            val sizeLong = (mDensity * 350).toInt()
            if (vertical) {
                lp2.width = sizeLong
                lp1.width = lp2.width
                lp2.height = sizeShort
                lp1.height = lp2.height
            } else {
                lp2.width = sizeShort
                lp1.width = lp2.width
                lp2.height = sizeLong
                lp1.height = lp2.height
            }
        }
    }

    private fun adjustFloatIndex(header: Boolean, needAdded: Boolean) {
        var floatIndex = -1
        if (needAdded) {
            floatIndex = if (header) 0 else mPageScrollView!!.itemViewCount - 1
        }
        if (header) {
            mPageScrollView!!.floatViewStartIndex = floatIndex
        } else {
            mPageScrollView!!.floatViewEndIndex = floatIndex
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        super.onCheckedChanged(buttonView, isChecked)
        if (buttonView === mToggleFloatStart) {
            adjustFloatIndex(true, isChecked)
        }
        if (buttonView === mToggleFloatEnd) {
            adjustFloatIndex(false, isChecked)
        }
    }
}
