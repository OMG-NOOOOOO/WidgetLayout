package com.rexy.example.extend.kotlintest

import android.content.Context

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-25 17:39
 */
class TestObject {
    @JvmField val SHOW: Int = 1
    val HIDE: Int = 2

    companion object {
        val SHOW_COMPANION: Int = 1
        val HIDE_COMPANION: Int = 2

        @JvmStatic fun sayStaticHellow(context: Context) {
            TestUtils.toast(context, "hellow, I am " + this.javaClass)
        }
    }

    fun sayHellow(context: Context) {
        TestUtils.toast(context, "hellow, I am " + this@TestObject.javaClass)
    }


}