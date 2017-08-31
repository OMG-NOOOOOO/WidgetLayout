package com.rexy.example

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.rexy.example.extend.BaseFragment
import com.rexy.example.extend.FadeTextButton
import com.rexy.example.utils.dp
import com.rexy.example.utils.v
import com.rexy.model.DecorationOffsetLinear
import com.rexy.model.TestRecyclerAdapter
import com.rexy.widgetlayout.example.R
import com.rexy.widgets.layout.BaseViewGroup
import com.rexy.widgets.layout.NestFloatLayout
import com.rexy.widgets.layout.WrapLayout

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-28 13:32
 */
class FragmentNestFloatLayout : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)=
            activity.v<NestFloatLayout> {
                setFloatViewIndex(1)
                setNestViewIndex(2)
                layoutParams = ViewGroup.LayoutParams(-1, -1)
                v<ImageView> {
                    val lp = BaseViewGroup.LayoutParams(-1, -2)
                    lp.leftMargin = dp(15)
                    lp.rightMargin = dp(15)
                    layoutParams = lp
                    setImageResource(R.drawable.image)
                    scaleType = ImageView.ScaleType.FIT_XY
                    maxHeight = dp(300)
                }
                v<FadeTextButton> {
                    val lp = BaseViewGroup.LayoutParams(-1, dp(40))
                    lp.leftMargin = dp(15)
                    lp.rightMargin = dp(15)
                    lp.topMargin = dp(5)
                    gravity = Gravity.CENTER
                    layoutParams = lp
                    setBackgroundColor(resources.getColor(R.color.optionBackground))
                    setTextColor(resources.getColor(R.color.textButton))
                    text = "FLOAT BUTTON FOR NEST LIST"
                }
                v<WrapLayout> {
                    val lp = BaseViewGroup.LayoutParams(-1, -2)
                    lp.leftMargin = dp(15)
                    lp.rightMargin = dp(15)
                    lp.topMargin = dp(5)
                    lp.bottomMargin = dp(5)
                    layoutParams = lp
                    v<RecyclerView> {
                        layoutParams = WrapLayout.LayoutParams(-1, -2)
                        val padding = dp(10)
                        setPadding(padding, padding, padding, padding)
                        minimumHeight = dp(100)
                        setBackgroundColor(resources.getColor(R.color.partBackground))
                        adapter = TestRecyclerAdapter(activity, MutableList(30) { "item " + (it + 1) })
                        layoutManager = LinearLayoutManager(activity)
                        addItemDecoration(DecorationOffsetLinear(false, 20))
                    }
                }
            }
}