package com.rexy.example.extend.kotlintest

import android.content.Context
import android.widget.Toast

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-31 10:02
 */
fun sayTest(context: Context,msg:CharSequence){
    Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
}