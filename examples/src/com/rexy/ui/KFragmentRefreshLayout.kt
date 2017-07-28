package com.rexy.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.rexy.common.KBaseFragment
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.group.BaseViewGroup
import com.rexy.widgets.group.NestRefreshLayout
import com.rexy.widgets.group.RefreshIndicator
import com.rexy.widgets.group.ScrollLayout

/**
 * TODO:功能说明

 * @author: rexy
 * *
 * @date: 2017-06-05 15:03
 */
class KFragmentRefreshLayout : KBaseFragment(), NestRefreshLayout.OnRefreshListener {
    internal var mRefreshLayout: NestRefreshLayout<RefreshIndicator>? = null
    internal var mScrollView: ScrollLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRefreshLayout = inflater?.inflate(R.layout.fragment_refreshlayout, container, false) as NestRefreshLayout<RefreshIndicator>
        mScrollView = mRefreshLayout!!.findViewById(R.id.scrollView) as ScrollLayout
        initScrollView(mScrollView!!, true)
        initRefreshLayout(mRefreshLayout!!)
        mRefreshLayout!!.setScrollChild(mScrollView)//自定义 View 需要。ScrollView ,AbsListView RecyclerView 不需要。
        return mRefreshLayout
    }

    private fun initRefreshLayout(refreshLayout: NestRefreshLayout<RefreshIndicator>) {
        val inflater = LayoutInflater.from(activity)
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.refreshPullIndicator = RefreshIndicator(inflater.context)
        refreshLayout.refreshPushIndicator = RefreshIndicator(inflater.context)
        refreshLayout.isRefreshPushEnable = true
    }

    private fun initScrollView(scrollView: ScrollLayout, init: Boolean) {
        LayoutInflater.from(activity).inflate(R.layout.pagescrollview_scrollview_child, scrollView, true)
        if (init) {
            scrollView.gravity = Gravity.CENTER
            scrollView.setBackgroundColor(0xAA000000.toInt())
            scrollView.isVerticalScrollBarEnabled = true
            scrollView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        }
        val clickListener = View.OnClickListener { v -> mScrollView!!.scrollToItem(mScrollView!!.indexOfChild(v), -1, true) }
        for (i in 0..scrollView.childCount - 1) {
            val lp = scrollView.getChildAt(i).layoutParams as BaseViewGroup.LayoutParams
            lp.gravity = Gravity.CENTER_HORIZONTAL
            lp.bottomMargin = 30
            lp.topMargin = lp.bottomMargin
            lp.rightMargin = 30
            lp.leftMargin = lp.rightMargin
            scrollView.getChildAt(i).setOnClickListener(clickListener)
        }
    }

    override fun onRefresh(parent: NestRefreshLayout<*>, refresh: Boolean) {
        Toast.makeText(activity, if (refresh) "pull refresh" else "push load more", Toast.LENGTH_SHORT).show()
        mScrollView?.postDelayed({
            if (refresh) {
                mScrollView?.removeAllViews()
                initScrollView(mScrollView!!, false)
            } else {
                val tv = TextView(activity)
                tv.setPadding(20, 20, 20, 20)
                tv.text = "load more:" + System.currentTimeMillis()
                mScrollView?.addView(tv)
                mScrollView?.scrollToItem(mScrollView!!.childCount - 1, -1, false)
            }
            parent.setRefreshComplete()
        }, 1200)
    }

    override fun onRefreshStateChanged(parent: NestRefreshLayout<*>, state: Int, preState: Int, moveAbsDistance: Int) {}
}
