package com.rexy.widgets.layout

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.rexy.widgetlayout.R


class RefreshIndicator : WrapLayout, NestRefreshLayout.OnRefreshListener {
    private var mProgressBar: ProgressBar? = null
    private var mImageView: ImageView? = null //当是刷新时不为null,加载更多时为null .
    private var mTextView: TextView? = null
    private var mLastRotateType = 0
    private var isRefreshViewAdded: Boolean = false
    private var isRefreshPullType: Boolean = false
    private var mIndicatorTexts = arrayOf("获取数据中", "下拉刷新", "上拉加载更多", "松开刷新", "松开加载更多", "请放手刷新", "请放手加载更多")

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        gravity = Gravity.CENTER
        eachLineMinItemCount = 1
        eachLineMaxItemCount = 2
        isEachLineCenterHorizontal = true
        isEachLineCenterVertical = true
        minimumHeight = (context.resources.displayMetrics.density * 50).toInt()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeRefreshViewInner()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeRefreshViewInner()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isRefreshViewAdded) {
            isRefreshViewAdded = buildRefreshViewInnerIfNeed()
        }
    }

    private fun removeRefreshViewInner() {
        removeAllViewsInLayout()
        isRefreshViewAdded = false
        mProgressBar = null
        mTextView = null
        mImageView = null
    }

    private fun buildRefreshViewInnerIfNeed(): Boolean {
        if (!isRefreshViewAdded && parent is NestRefreshLayout<*>) {
            val parent = parent as NestRefreshLayout<*>
            if (parent.getRefreshPullIndicator() === this) {
                isRefreshPullType = true
                isRefreshViewAdded = true
            }
            if (parent.getRefreshPushIndicator() === this) {
                isRefreshPullType = false
                isRefreshViewAdded = true
            }
            if (isRefreshViewAdded) {
                removeRefreshViewInner()
                buildRefreshViewInner(isRefreshPullType)
                isRefreshViewAdded = true
            }
        }
        return isRefreshViewAdded
    }

    private fun buildRefreshViewInner(header: Boolean) {
        val context = context
        val density = context.resources.displayMetrics.density
        mTextView = TextView(context)
        mProgressBar = ProgressBar(context)
        mImageView = ImageView(context)
        mImageView!!.setImageResource(R.drawable.widget_layout_icon_refresh_down)
        mTextView!!.textSize = 16f
        mTextView!!.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        val lpLeft = WrapLayout.LayoutParams(-2, -2)
        lpLeft.gravity = Gravity.CENTER
        lpLeft.maxWidth = (density * 35).toInt()
        lpLeft.maxHeight = lpLeft.maxWidth
        addView(mImageView, lpLeft)
        addView(mProgressBar, lpLeft)
        addView(mTextView)
    }

    private fun rotateArrow(view: View?, reversed: Boolean, optHeader: Boolean) {
        val rotateType = if (reversed) 1 else -1
        if (rotateType != mLastRotateType) {
            val from = if (reversed) 0 else 180
            val to = if (reversed) 180 else 360
            val rotate = RotateAnimation(from.toFloat(), to.toFloat(), RotateAnimation.RELATIVE_TO_SELF,
                    0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)
            rotate.duration = 150
            rotate.fillAfter = true
            view?.clearAnimation()
            view?.startAnimation(rotate)
            mLastRotateType = rotateType
        }
    }

    override fun onRefreshStateChanged(parent: NestRefreshLayout<*>, state: Int, preState: Int, moveDistance: Int) {
        if (isRefreshViewAdded) {
            if (state != preState && !parent.isRefreshing()) {
                if (state == NestRefreshLayout.OnRefreshListener.STATE_IDLE) {
                    mLastRotateType = 0
                    if (mImageView != null) {
                        mImageView!!.clearAnimation()
                    }
                } else {
                    mTextView!!.text = mIndicatorTexts[state]
                }
                if (mProgressBar != null) {
                    mProgressBar!!.visibility = View.GONE
                }
                if (mImageView != null) {
                    mImageView!!.visibility = View.VISIBLE
                    if (preState == NestRefreshLayout.OnRefreshListener.STATE_PULL_READY) {
                        if (state == NestRefreshLayout.OnRefreshListener.STATE_PULL_BEYOND_READY) {
                            rotateArrow(mImageView, true, true)
                        } else if (state == NestRefreshLayout.OnRefreshListener.STATE_PULL_TO_READY) {
                            rotateArrow(mImageView, false, true)
                        }
                    }
                    if (preState == NestRefreshLayout.OnRefreshListener.STATE_PUSH_READY) {
                        if (state == NestRefreshLayout.OnRefreshListener.STATE_PUSH_BEYOND_READY) {
                            rotateArrow(mImageView, true, false)
                        } else if (state == NestRefreshLayout.OnRefreshListener.STATE_PUSH_TO_READY) {
                            rotateArrow(mImageView, false, false)
                        }
                    }
                }
            }
        }
    }

    override fun onRefresh(parent: NestRefreshLayout<*>, refresh: Boolean) {
        if (isRefreshViewAdded) {
            mTextView!!.text = mIndicatorTexts[0]
            if (mImageView != null) {
                mImageView!!.visibility = View.GONE
                mImageView!!.clearAnimation()
            }
            if (mProgressBar != null) {
                mProgressBar!!.visibility = View.VISIBLE
            }
        }
    }
}
