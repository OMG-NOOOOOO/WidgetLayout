package com.rexy.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rexy.common.KBaseFragment

import com.rexy.widgetlayout.example.R

/**
 * TODO:功能说明

 * @author: rexy
 * *
 * @date: 2017-06-05 14:56
 */
class KFragmentPageScrollContainer : KBaseFragment(), View.OnClickListener {

    internal val mToggleViewPage by lazy { view!!.findViewById(R.id.toggleViewPager) as TextView }
    internal val mToggleOrientation by lazy { view!!.findViewById(R.id.toggleOrientation) as TextView }

    internal var mFragmentTags = arrayOf("ScrollView", "ViewPager")

    internal var mVisibleFragmentIndex = 0
    internal var mViewAsScrollView: Boolean = false
    internal var mViewAsVertical: Boolean = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_pagescrollview_container, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mToggleViewPage.setOnClickListener(this)
        mToggleOrientation.setOnClickListener(this)
        switchToFragment(mVisibleFragmentIndex, 1 - mVisibleFragmentIndex)
    }

    fun getDefaultViewTypeOrientation(scrollView: Boolean)=scrollView

    override fun onClick(v: View) {
        if (v === mToggleOrientation) {
            setViewOrientationInner(!mViewAsVertical, true)
        }
        if (v === mToggleViewPage) {
            val willIndex = 1 - mVisibleFragmentIndex
            switchToFragment(willIndex, mVisibleFragmentIndex)
            mVisibleFragmentIndex = willIndex
        }
    }

    fun setViewOrientation(vertical: Boolean) {
        setViewOrientationInner(vertical, false)
    }

    fun setViewOrientationInner(vertical: Boolean, notify: Boolean) {
        if (mViewAsVertical != vertical) {
            mViewAsVertical = vertical
            mToggleOrientation.text = if (vertical) "VERTICAL" else "HORIZONTAL"
            if (notify) {
                val fragment = childFragmentManager.findFragmentByTag(mFragmentTags[mVisibleFragmentIndex])
                if (fragment is KFragmentPageBase) {
                    fragment.setContentOrientation(vertical)
                }
            }
        }
    }

    private fun setViewType(scrollView: Boolean) {
        if (mViewAsScrollView != scrollView) {
            mViewAsScrollView = scrollView
            mToggleViewPage.text = if (scrollView) mFragmentTags[0] else mFragmentTags[1]
        }
    }

    private fun setViewTypeAndOrientation(scrollView: Boolean, vertical: Boolean) {
        mViewAsScrollView = !scrollView
        mViewAsVertical = !vertical
        setViewType(scrollView)
        setViewOrientationInner(vertical, false)
    }

    private fun switchToFragment(willIndex: Int, oldIndex: Int) {
        val scrollView = willIndex == 0
        val fm = childFragmentManager
        val ft = fm.beginTransaction()
        var showFragment: Fragment? = fm.findFragmentByTag(mFragmentTags[willIndex])
        val hideFragment = fm.findFragmentByTag(mFragmentTags[oldIndex])
        if (showFragment == null) {
            val initVertical = getDefaultViewTypeOrientation(scrollView)
            setViewTypeAndOrientation(scrollView, initVertical)
            val arg = Bundle()
            arg.putBoolean(KFragmentPageBase.KEY_VERTICAL, initVertical)
            val fragmentClass = if (scrollView) KFragmentPageScrollView::class.java else KFragmentPageViewPager::class.java
            showFragment = Fragment.instantiate(activity, fragmentClass.name, arg)
            ft.add(R.id.fragmentContainer, showFragment, mFragmentTags[willIndex])
        } else {
            setViewType(scrollView)
            ft.show(showFragment)
        }
        if (hideFragment != null) {
            ft.hide(hideFragment)
        }
        ft.commitAllowingStateLoss()
    }
}
