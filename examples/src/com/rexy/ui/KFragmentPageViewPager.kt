package com.rexy.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton

import com.rexy.widgetlayout.example.R
import com.rexy.widgets.group.PageScrollTab
import com.rexy.widgets.group.PageScrollView

/**
 * TODO:功能说明

 * @author: rexy
 * *
 * @date: 2017-06-05 15:02
 */
class KFragmentPageViewPager : KFragmentPageBase() {

    internal var mSlideTab: PageScrollTab?=null
    internal var mToggleHeader: ToggleButton?=null
    internal var mToggleFooter: ToggleButton?=null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_pagescrollview_viewpager, container, false).also {
            initView(it)
        }
    }

    override fun initView(root: View) {
        super.initView(root)
        mSlideTab = root.findViewById(R.id.pageTabs) as PageScrollTab
        mToggleHeader = root.findViewById(R.id.togglePageHeader) as ToggleButton
        mToggleFooter = root.findViewById(R.id.togglePageFooter) as ToggleButton
        mToggleHeader!!.setOnCheckedChangeListener(this)
        mToggleFooter!!.setOnCheckedChangeListener(this)
        initPageTab(mPageScrollView!!, mSlideTab!!)
    }

    private fun initPageTab(scrollView: PageScrollView, tabHost: PageScrollTab) {
        val pageClick1 = View.OnClickListener { v ->
            val index = scrollView.indexOfItemView(v)
            if (index >= 0) {
                scrollView.scrollToCentre(index, 0, -1)
            }
        }
        val pageItemCount = scrollView.itemViewCount
        for (i in 0..pageItemCount - 1) {
            val child = scrollView.getItemView(i)
            child.setOnClickListener(pageClick1)
            if (child is TextView) {
                val text = child.text
                tabHost.addTabItem(text.subSequence(TextUtils.indexOf(text, 'i'), text.length), true)
            }
        }

        tabHost.setTabClickListener { parent, cur, curPos, pre, prePos ->
            scrollView.scrollToCentre(curPos, 0, -1)
            false
        }
        scrollView.setOnPageChangeListener(object : PageScrollView.OnPageChangeListener {
            override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {}

            override fun onScrollStateChanged(state: Int, oldState: Int) {
                tabHost.callPageScrollStateChanged(state, scrollView.currentItem)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                tabHost.callPageScrolled(position, positionOffset)
            }

            override fun onPageSelected(position: Int, oldPosition: Int) {
                tabHost.callPageSelected(position)
            }
        })
    }

    public override fun setContentOrientationInner(vertical: Boolean, init: Boolean): Boolean {
        val handled = super.setContentOrientationInner(vertical, init)
        if (handled) {
            adjustPageHeaderAndFooter(vertical, true, mToggleHeader!!.isChecked)
            adjustPageHeaderAndFooter(vertical, false, mToggleFooter!!.isChecked)
        }
        return handled
    }

    private fun adjustPageHeaderAndFooter(vertical: Boolean, header: Boolean, needAdded: Boolean) {
        var pageHeaderFooter: TextView? = null
        if (needAdded) {
            pageHeaderFooter = LayoutInflater.from(activity).inflate(if (vertical)
                R.layout.pagescrollview_headerfooter_vertical
            else
                R.layout.pagescrollview_headerfooter_horizontal, mPageScrollView, false) as TextView
            if (header) {
                pageHeaderFooter.text = "I am page Header"
            } else {
                pageHeaderFooter.text = "I am page Footer"
            }
        }
        if (header) {
            mPageScrollView!!.pageHeaderView = pageHeaderFooter
        } else {
            mPageScrollView!!.pageFooterView = pageHeaderFooter
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        super.onCheckedChanged(buttonView, isChecked)
        if (buttonView === mToggleHeader) {
            adjustPageHeaderAndFooter(mPageScrollView!!.orientation == PageScrollView.VERTICAL, true, isChecked)
        }
        if (buttonView === mToggleFooter) {
            adjustPageHeaderAndFooter(mPageScrollView!!.orientation == PageScrollView.VERTICAL, false, isChecked)
        }
    }
}
