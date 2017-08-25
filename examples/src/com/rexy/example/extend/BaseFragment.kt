package com.rexy.example.extend

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-27 17:11
 */
open abstract class BaseFragment : Fragment() {

    private var visibleStatus = -1

    override fun onResume() {
        super.onResume()
        if (visibleStatus == -1) {
            visibleStatus = 1
            fragmentVisibleChanged(true, true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (visibleStatus == 1) {
            visibleStatus = -1
            fragmentVisibleChanged(false, true)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        visibleStatus = if (hidden) 0 else 1
        fragmentVisibleChanged(visibleStatus == 1, false)
    }

    protected open fun fragmentVisibleChanged(visible: Boolean, fromLifecycle: Boolean) {
        onFragmentVisibleChanged(visible, fromLifecycle)
    }

    open protected fun onFragmentVisibleChanged(visible: Boolean, fromLifecycle: Boolean) {}

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        arguments?.let { outState?.putAll(it) }
    }
}