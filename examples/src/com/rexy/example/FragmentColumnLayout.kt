package com.rexy.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.layout.ColumnLayout

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-28 10:18
 */
class FragmentColumnLayout : FragmentViewPicker() {

    private val mColumnLayout: ColumnLayout by lazy { view?.findViewById(R.id.columnLayout) as ColumnLayout  }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_columnlayout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        buildRandomView(10, false)

    }

    override fun onFastAddView() {
        buildRandomView(1, false)
    }

    override fun onAddOrRemoveView(addView: View?) {
        if (addView == null) {
            val minRemainCount = 3
            if (mColumnLayout.childCount > minRemainCount) {
                mColumnLayout.removeViewAt(mColumnLayout.childCount - 1)
            }
        } else {
            mColumnLayout.addView(addView)
        }
    }
}