package com.rexy.example.extend

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.app.Fragment
import android.view.View

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-24 14:16
 */
class ViewUtils {
    companion object {
        fun <T : View> view(aty: Activity, id: Int) = aty?.findViewById(id) as T

        fun <T : View> view(fragment: Fragment, id: Int): T {
            val rootView = fragment?.view;
            return rootView?.findViewById(id) as T
        }

        fun <T : View> view(root: View, id: Int) = root?.findViewById(id) as T

        fun setBackground(v: View?, d: Drawable?) {
            if (v != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    v.setBackgroundDrawable(d);
                } else {
                    v.background = d
                }
            }
        }
    }
}