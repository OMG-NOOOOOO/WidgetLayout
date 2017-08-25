package com.rexy.example

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ToggleButton
import com.rexy.example.extend.BaseFragment
import com.rexy.example.extend.FadeTextButton
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.layout.BaseViewGroup
import com.rexy.widgets.layout.WrapLayout
import java.util.*

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-28 10:44
 */
class FragmentViewOption : BaseFragment(){

    private val mExampleView by lazy { view!!.findViewById(R.id.exampleView) }
    private val mExampleParent by lazy { view!!.findViewById(R.id.exampleParent)as WrapLayout }
    private val mEditMinWidth by lazy { view!!.findViewById(R.id.editMinWidth)as EditText }
    private val mEditMaxWidth by lazy { view!!.findViewById(R.id.editMaxWidth)as EditText }
    private val mEditMinHeight by lazy { view!!.findViewById(R.id.editMinHeight)as EditText }
    private val mEditMaxHeight by lazy { view!!.findViewById(R.id.editMaxHeight)as EditText }

    private var mDensity: Float = 0.toFloat()
    //left,right,centerH
    private val mToggleHorizontal by lazy {
        arrayOf(view!!.findViewById( R.id.toggleLeft) as ToggleButton
            ,view!!.findViewById( R.id.toggleRight) as ToggleButton
            ,view!!.findViewById( R.id.toggleCenterHorizontal) as ToggleButton) }

    //top,bottom,centerV
    private val mToggleVertical by lazy {
        arrayOf(view!!.findViewById( R.id.toggleTop) as ToggleButton
                ,view!!.findViewById( R.id.toggleBottom) as ToggleButton
                ,view!!.findViewById( R.id.toggleCenterVertical) as ToggleButton) }

    private val mAligns = intArrayOf(Gravity.LEFT, Gravity.RIGHT, Gravity.CENTER_HORIZONTAL, Gravity.TOP, Gravity.BOTTOM, Gravity.CENTER_VERTICAL)

    private var mMinWidth:Int = 0
    private var mMaxWidth:Int = 0
    private var mMinHeight:Int = 0
    private var mMaxHeight:Int = 0
    private var mColor = 0xFFFF0000.toInt()
    private var mGravity = Gravity.CENTER

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_viewoption, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view!!.let { initView(it) }
    }

    private fun initView(root: View) {
        mDensity = root.resources.displayMetrics.density
        val checker = CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> checkAllAlignGravity(buttonView as ToggleButton, isChecked) }
        mToggleHorizontal.forEach { it.setOnCheckedChangeListener(checker) }
        mToggleVertical.forEach { it.setOnCheckedChangeListener(checker) }
        checkAllAlignGravity(null, false)
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                checkAllWidthHeightLimit()
            }
        }
        mEditMinWidth.addTextChangedListener(textWatcher)
        mEditMaxWidth.addTextChangedListener(textWatcher)
        mEditMinHeight.addTextChangedListener(textWatcher)
        mEditMaxHeight.addTextChangedListener(textWatcher)
        checkAllWidthHeightLimit()
        val clicker = View.OnClickListener { v ->
            val viewId = v.id
            if (R.id.buttonAddView == viewId) {
                val t = Intent()
                t.putExtra("minWidth", mMinWidth)
                t.putExtra("maxWidth", mMaxWidth)
                t.putExtra("minHeight", mMinHeight)
                t.putExtra("maxHeight", mMaxHeight)
                t.putExtra("color", mColor)
                t.putExtra("gravity", mGravity)
                activity.setResult(Activity.RESULT_OK, t)
                activity.finish()
            } else {
                if (v is FadeTextButton) {
                    if (TextUtils.isEmpty(v.text) && v.background is ColorDrawable) {
                        val cd = v.background as ColorDrawable
                        mColor = cd.color
                        mExampleView.setBackgroundColor(mColor)
                    }
                }
            }
        }
        root.findViewById(R.id.buttonAddView).setOnClickListener(clicker)
        val viewsId = intArrayOf(R.id.viewColor1, R.id.viewColor2, R.id.viewColor3, R.id.viewColor4)
        for (i in viewsId.indices) {
            root.findViewById(viewsId[i]).setOnClickListener(clicker)
        }
        root.findViewById(viewsId[Random(System.currentTimeMillis()).nextInt(viewsId.size)]).performClick()
    }

    private fun checkGravity(optIndex: Int, shift: Int): Int {
        val optList = if (shift > 0) mToggleVertical else mToggleHorizontal
        if (optIndex == -1) {
            optList.indices
                    .filter { optList[it].isChecked }
                    .forEach { return mAligns[it + shift] }
        } else {
            val optButton = optList[optIndex]
            if (optButton.isChecked) {
                optList.indices
                        .filter { it != optIndex }
                        .forEach { optList[it].isChecked = false }
                return mAligns[optIndex + shift]
            }
        }
        optList[0].isChecked=true
        return mAligns[shift]
    }

    private fun checkLimitSize(edit: EditText): Int {
        val str = edit.text.toString()
        var value: Int
        val defValue = 0
        try {
            value = Integer.parseInt(str)
            if (value < 0) {
                value = defValue
            } else {
                value *= mDensity.toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            value = defValue
        }
        return value
    }

    private fun checkAllWidthHeightLimit() {
        mMinWidth = checkLimitSize(mEditMinWidth)
        mMaxWidth = checkLimitSize(mEditMaxWidth)
        mMinHeight = checkLimitSize(mEditMinHeight)
        mMaxHeight = checkLimitSize(mEditMaxHeight)
        mExampleView.let {
            var blp: BaseViewGroup.LayoutParams?
            if (it.layoutParams is BaseViewGroup.LayoutParams) {
                blp = it.layoutParams as BaseViewGroup.LayoutParams
                blp.maxWidth = mMaxWidth
                blp.maxHeight = mMaxHeight
            }
            it.minimumWidth = mMinWidth
            it.minimumHeight = mMinHeight
            it.requestLayout()
        }
    }

    private var mLockAlign = false

    private fun findToggleIndex(button: ToggleButton?, list: Array<ToggleButton>?): Int {
        if (button == null || list == null) return -1
        return list.indexOf(button)
    }

    private fun checkAllAlignGravity(button: ToggleButton?, checked: Boolean) {
        if (mLockAlign) return
        mLockAlign = true
        val optIndexH = findToggleIndex(button, mToggleHorizontal)
        val optIndexV = findToggleIndex(button, mToggleVertical)
        val gravity = checkGravity(optIndexH, 0) or checkGravity(optIndexV, mToggleHorizontal.size)
        var changed = false
        if (mExampleView.layoutParams is WrapLayout.LayoutParams) {
            val lp = mExampleView.layoutParams as WrapLayout.LayoutParams
            if (lp.gravity != gravity) {
                lp.gravity = gravity
                changed = true
            }
        }
        if (mExampleParent.gravity != gravity) {
            mExampleParent.gravity = gravity
            changed = true
        }
        if (changed) {
            mGravity = gravity
            mExampleParent.requestLayout()
        }
        mLockAlign = false
    }
}