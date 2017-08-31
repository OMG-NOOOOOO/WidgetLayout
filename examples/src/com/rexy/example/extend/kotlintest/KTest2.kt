@file:JvmName("KTest")
@file:JvmMultifileClass
package com.rexy.example.extend.kotlintest
import android.content.Context

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-25 17:36
 */
fun say2(context: Context, msg: CharSequence) {
    TestUtils.toast(context, msg)
}