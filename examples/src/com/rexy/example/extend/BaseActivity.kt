package com.rexy.example.extend

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import com.rexy.widgets.layout.HierarchyLayout
import java.util.*

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-27 14:40
 */
open class BaseActivity : FragmentActivity() {

    companion object {
        val KEY_ATY_STYLE = "KEY_ATY_STYLE"
        private var visibleSignal = 0

        val activities = Stack<Activity>()

        fun hasActivityVisible(): Boolean = visibleSignal == 1

        fun exitApp(kill: Boolean) {
            while (!activities.isEmpty()) {
                activities.pop().finish()
            }
            if (kill) {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }

    protected var newIntent: Intent? = null
        private set
        protected get

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT > 10) {
            window.setFlags(0x01000000, 0x01000000)
        }
        val atyTheme = intent?.getIntExtra(KEY_ATY_STYLE, 0) ?: 0
        if (atyTheme != 0) {
            setTheme(atyTheme)
        }
        super.onCreate(savedInstanceState)
        activities.push(this)
    }

    override fun onPostResume() {
        super.onPostResume()
        newIntent?.let {
            if (!handleNewIntent(it, it.extras)) {
                //if nothing done , do something common here
            }
            newIntent = null
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        newIntent = intent
    }

    /**
     * @param newIntent 从onNewIntent而来的Intent.
     * *
     * @param extras Intent里面的getExtras  注意这里的extras是一个深拷贝。
     * *
     * @return 返回true后将不会给父类处理。
     */
    protected open fun handleNewIntent(newIntent: Intent, extras: Bundle): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        visibleSignal++
    }

    override fun onStop() {
        super.onStop()
        visibleSignal--
    }

    override fun onDestroy() {
        super.onDestroy()
        activities.remove(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let { it.add("HIERARCHY").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (TextUtils.equals("HIERARCHY", item?.title)) {
            HierarchyLayout.hierarchy(this, !HierarchyLayout.isHierarchyInstalled(this))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (HierarchyLayout.isHierarchyInstalled(this)) {
            HierarchyLayout.hierarchy(this, false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}