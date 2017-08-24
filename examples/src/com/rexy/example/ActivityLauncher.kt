package com.rexy.example

import android.content.Intent
import android.os.Bundle
import com.rexy.example.extend.BaseActivity

/**
 * Created by rexy on 17/7/28.
 */
class ActivityLauncher : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, ActivityMain::class.java))
        finish()
    }
}