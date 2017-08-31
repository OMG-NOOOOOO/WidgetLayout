package com.rexy.example

import android.os.Bundle
import android.view.View
import com.rexy.example.extend.BaseActivity
import com.rexy.example.extend.kotlintest.*
import com.rexy.widgetlayout.example.R

/**
 * Created by rexy on 17/7/28.
 */
class ActivityMain : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example_entry)
        findViewById(R.id.buttonColumn)?.setOnClickListener(this)
        findViewById(R.id.buttonPageScroll)?.setOnClickListener(this)
        findViewById(R.id.buttonWrapLabel)?.setOnClickListener(this)
        findViewById(R.id.buttonNestFloat)?.setOnClickListener(this)
        findViewById(R.id.buttonRefresh)?.setOnClickListener(this)
        findViewById(R.id.buttonHierarchy)?.setOnClickListener(this)

/*      val kentity = KEntity1()
        kentity.sayHellow(this)
        KEntity1.sayHellow1(this)
        KEntity1.sayHellow2(this)
        TestUtils.toast(this, "test utils")
        pop("pop")
        say1(this, "say1")
        say2(this,"say2")
        say3(this,"say3")
        sayTest(this,"sayTest")*/
    }

    override fun onClick(v: View) {
        val cls=when (v.id) {
            R.id.buttonColumn -> FragmentColumnLayout::class.java
            R.id.buttonPageScroll -> FragmentPageScrollContainer::class.java
            R.id.buttonWrapLabel -> FragmentWrapLabelLayout::class.java
            R.id.buttonNestFloat -> FragmentNestFloatLayout::class.java
            R.id.buttonRefresh -> FragmentRefreshLayout::class.java
            R.id.buttonHierarchy -> FragmentHierarchyLayout::class.java
            else -> null
        }
        cls?.let {  ActivityCommon.launch(this, it) }
    }
}