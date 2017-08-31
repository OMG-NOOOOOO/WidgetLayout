package com.rexy.example.extend.kotlintest

import android.content.Context

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-31 10:19
 */
class KEntity1 {


    val valValue1 = 1

    @field:JvmField
    protected val valValue2 = 2

    var varValue1 = 1

    @field:JvmField
    var varValue2 = 2

    var varValue3 = 3
        private set


    companion object {
        val valComValue1 = 1

        @field:JvmField
        val valComValue2: Int = 2

        @JvmStatic
        val valComValue3: Int = 3

        var varComValue1 = 1

        @field:JvmField
        var varComValue2: Int = 2

        @JvmStatic
        var varComValue3: Int = 3


        fun sayHellow1(context: Context) {
            TestUtils.toast(context, "cls=" + this::class + "->" + this::class.java)
        }

        @JvmStatic
        fun sayHellow2(context: Context) {
            TestUtils.toast(context, "cls=" + this::class + "->" + this::class.java)
        }
    }

    fun sayHellow(context: Context) {
        TestUtils.toast(context, "cls=" + this@KEntity1::class + "->" + this@KEntity1::class.java)
    }
}