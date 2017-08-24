package com.rexy.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.rexy.example.extend.BaseActivity

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-27 17:35
 */
class ActivityCommon : BaseActivity() {
    companion object {
        val KEY_FRAGMENT_NAME: String = "KEY_FRAGMENT_NAME"

        fun launch(context: Context, fragment: Class<out Fragment>) {
            val t = Intent(context, ActivityCommon::class.java)
            t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            t.putExtra(KEY_FRAGMENT_NAME, fragment.name)
            context.startActivity(t)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(KEY_FRAGMENT_NAME)?.let {
            with(supportFragmentManager, { beginTransaction() })
                    .apply {
                        add(android.R.id.content, Fragment.instantiate(this@ActivityCommon, it, Bundle()), "root")
                        commitAllowingStateLoss()
                    }

        }
    }
}