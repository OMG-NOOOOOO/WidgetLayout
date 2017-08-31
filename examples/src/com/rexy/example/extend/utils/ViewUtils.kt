package com.rexy.example.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-24 14:16
 */
object ViewUtils {

    fun <T : View> view(aty: Activity, id: Int) = aty.findViewById(id) as T

    fun <T : View> view(fragment: Fragment, id: Int): T {
        val rootView = fragment.view
        return rootView?.findViewById(id) as T
    }

    fun <T : View> view(root: View, id: Int) = root.findViewById(id) as T

    fun background(v: View?, d: Drawable?) {
        if (v != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                v.setBackgroundDrawable(d)
            } else {
                v.background = d
            }
        }
    }
}

inline fun <reified VIEW : View> Context.v(block: VIEW.(VIEW) -> Unit): VIEW {
    var construct = VIEW::class.java.getConstructor(Context::class.java)
    val view = construct?.newInstance(this) ?: (VIEW::class.java.getConstructor(Context::class.java, AttributeSet::class.java).newInstance(this, null))
    view.block(view)
    return view
}

inline fun <reified VIEW : View> ViewGroup.v(block: VIEW.(VIEW) -> Unit): VIEW {
    var construct = VIEW::class.java.getConstructor(Context::class.java)
    val view = construct?.newInstance(context) ?: (VIEW::class.java.getConstructor(Context::class.java, AttributeSet::class.java).newInstance(context, null))
    addView(view)
    view.block(view)
    return view
}




inline fun View.dp(v: Number):Int = ((resources.displayMetrics.density * v.toFloat() + 0.5f).toInt())
inline fun View.sp(v: Number):Int = ((resources.displayMetrics.scaledDensity * v.toFloat() + 0.5f).toInt())