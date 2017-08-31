package com.rexy.example.extend.kotlintest

import android.content.Context

fun say3(context: Context, msg: CharSequence) {
    TestUtils.toast(context, msg)
}