package com.rexy.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rexy.example.extend.BaseFragment
import com.rexy.widgetlayout.example.R

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-28 14:27
 */
class FragmentHierarchyLayout : BaseFragment(){
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_hierarchylayout, container, false)
    }
}