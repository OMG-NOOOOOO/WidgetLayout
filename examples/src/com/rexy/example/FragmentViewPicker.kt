package com.rexy.example

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import com.rexy.example.extend.BaseFragment
import com.rexy.example.extend.FadeTextButton
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.layout.BaseViewGroup
import java.util.*

/**
 * Created by rexy on 17/7/28.
 */
abstract class FragmentViewPicker : BaseFragment(){
    private var REQUEST_ADD_VIEW = 1
    protected var mDensity: Float = 0f

    private val mColors = intArrayOf(0xFFFF0000.toInt(), 0xDD5A6390.toInt(), 0xDD7CF499.toInt(), 0xDDff00ff.toInt(), 0xFF0000FF.toInt(), 0xEE8FC320.toInt(), 0xFF295622.toInt(), 0xFFF5A623.toInt())
    internal val mRandom = Random(System.currentTimeMillis())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mDensity = resources.displayMetrics.density
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let {
            val clicker = View.OnClickListener {
                when (it.id) {
                    R.id.viewAdd -> requestAddView()
                    R.id.viewAddFast -> onFastAddView()
                    R.id.viewRemove -> onAddOrRemoveView(null)
                    else -> {
                    }
                }
            }
            with(it.findViewById(R.id.viewAdd)) {
                setOnClickListener(clicker)
            }
            with(it.findViewById(R.id.viewAddFast)) {
                setOnClickListener(clicker)
            }
            with(it.findViewById(R.id.viewRemove)) {
                setOnClickListener(clicker)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(0, 0, 0, "REMOVE").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(0, 1, 1, "ADD").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            requestAddView()
        } else {
            onAddOrRemoveView(null)
        }
        return true
    }

    private fun requestAddView() {
        Intent(activity, ActivityCommon::class.java)
                .apply {putExtra(ActivityCommon.KEY_FRAGMENT_NAME, FragmentViewOption::class.java.name) }
                .let { startActivityForResult(it, REQUEST_ADD_VIEW) }
    }

    open protected fun onFastAddView() {
        buildRandomView(1, true)
    }

    fun buildRandomView(count: Int, alignCenter: Boolean) {
        val seek = System.currentTimeMillis()
        for (i in 0 until count) {
            mRandom.setSeed(seek + i * mRandom.nextInt(Integer.MAX_VALUE))
            onAddOrRemoveView(buildView(-1, -1, -1, -1, if (alignCenter) Gravity.CENTER else 0, mColors[mRandom.nextInt(mColors.size)], true))
        }
    }

    protected fun buildView(text: CharSequence, alignCenter: Boolean): View {
        mRandom.setSeed(System.currentTimeMillis() - text.toString().hashCode() * mRandom.nextInt(Integer.MAX_VALUE))
        val view = buildView(-1, -1, -1, -1, if (alignCenter) Gravity.CENTER else 0, mColors[mRandom.nextInt(mColors.size)], true)
        if (view is TextView) {
            view.text = text
        }
        return view
    }

    open protected fun buildView(minWidth: Int, maxWidth: Int, minHeight: Int, maxHeight: Int, gravity: Int, color: Int, random: Boolean): View {
        val lp = BaseViewGroup.LayoutParams(-2, -2, gravity)
        lp.maxWidth = maxWidth
        lp.maxHeight = maxHeight
        val view = FadeTextButton(activity)
        view.text = "R"
        view.gravity = Gravity.CENTER
        view.minWidth = minWidth
        view.minHeight = minHeight
        view.textSize = 18f
        view.setTextColor(resources.getColor(R.color.textButton))
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        val paddingH = (18 * mDensity).toInt()
        val paddingV = (10 * mDensity).toInt()
        view.setPadding(paddingH, paddingV, paddingH, paddingV)
        if (color != 0) {
            view.setBackgroundColor(color)
        }
        view.layoutParams = lp
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ADD_VIEW && resultCode == Activity.RESULT_OK) {
            data?.let {
                val minWidth = it.getIntExtra("minWidth", -1)
                val maxWidth = it.getIntExtra("maxWidth", -1)
                val minHeight = it.getIntExtra("minHeight", -1)
                val maxHeight = it.getIntExtra("maxHeight", -1)
                val gravity = it.getIntExtra("gravity", 0)
                val color = it.getIntExtra("color", 0xFFFF0000.toInt())
                onAddOrRemoveView(buildView(minWidth, maxWidth, minHeight, maxHeight, gravity, color, false))
            }
        }
    }

    protected abstract fun onAddOrRemoveView(addView: View?)
}