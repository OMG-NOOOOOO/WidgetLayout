package com.rexy.example

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.ToggleButton
import com.rexy.example.extend.BaseFragment
import com.rexy.model.TestPageTransformer
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.layout.BaseViewGroup
import com.rexy.widgets.layout.PageScrollView

/**
 * TODO:功能说明

 * @author: rexy
 * *
 * @date: 2017-06-05 15:02
 */
open abstract class FragmentPageBase : BaseFragment(), CompoundButton.OnCheckedChangeListener {

    protected var mDensity: Float = 0.toFloat()
    private var mContentVertical: Boolean = false
    private var mPageTransformer = TestPageTransformer()

    private lateinit var mToggleAnim: ToggleButton
    private lateinit var  mToggleCenter: ToggleButton
    protected lateinit var  mPageScrollView: PageScrollView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let { mContentVertical = it.getBoolean(KEY_VERTICAL) }
        mContentVertical = !mContentVertical
        setContentOrientationInner(!mContentVertical, false)
    }


    private val parentContainerFragment: FragmentPageScrollContainer?
        get() {
            if (parentFragment is FragmentPageScrollContainer) {
                return parentFragment as FragmentPageScrollContainer
            }
            if (targetFragment is FragmentPageScrollContainer) {
                return targetFragment as FragmentPageScrollContainer
            }
            return null
        }

    override fun onFragmentVisibleChanged(visible: Boolean, fromLifecycle: Boolean) {
        if (visible) {
            parentContainerFragment?.setViewOrientation(mContentVertical)
        }
    }

    fun setContentOrientation(vertical: Boolean) {
        setContentOrientationInner(vertical, false)
    }

    protected open fun initView(root: View) {
        mPageScrollView = root.findViewById(R.id.pageScrollView) as PageScrollView
        mToggleAnim = root.findViewById(R.id.toggleTransform) as ToggleButton
        mToggleCenter = root.findViewById(R.id.toggleChildCenter) as ToggleButton
        mToggleAnim.setOnCheckedChangeListener(this)
        mToggleCenter.setOnCheckedChangeListener(this)
    }

    protected open fun setContentOrientationInner(vertical: Boolean, init: Boolean): Boolean {
        if (mContentVertical != vertical) {
            mContentVertical = vertical
            mPageScrollView?.let {
                it.orientation = if (vertical) BaseViewGroup.VERTICAL else BaseViewGroup.HORIZONTAL
            }
            if (init) {
                adjustTransformAnimation(mToggleAnim.isChecked)
                adjustChildLayoutCenter(mToggleCenter.isChecked)
            }
            return true
        }
        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mDensity = context!!.resources.displayMetrics.density
    }

    private fun adjustTransformAnimation(haveAnim: Boolean) {
        mPageScrollView.let { it.pageTransformer = if (haveAnim) mPageTransformer else null }
    }

    private fun adjustChildLayoutCenter(layoutCenter: Boolean) {
        mPageScrollView.let { it.isChildCenter = layoutCenter }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (mToggleAnim === buttonView) {
            adjustTransformAnimation(isChecked)
        }
        if (mToggleCenter === buttonView) {
            adjustChildLayoutCenter(isChecked)
        }
    }

    companion object {
        val KEY_VERTICAL = "KEY_VERTICAL"
    }
}
