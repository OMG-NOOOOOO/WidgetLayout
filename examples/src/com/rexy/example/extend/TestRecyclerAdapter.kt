package com.rexy.model

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rexy.example.extend.FadeTextButton
import com.rexy.widgetlayout.example.R

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-07-28 09:24
 */
class TestRecyclerAdapter(private var context: Context, private var items: MutableList<String>?) : RecyclerView.Adapter<TestRecyclerAdapter.KTestRecyclerHolder>() {

    override fun getItemCount() = items?.size ?: 0

    override fun onCreateViewHolder(p: ViewGroup, type: Int): KTestRecyclerHolder {
        val textView = FadeTextButton(context)
        textView.textSize = 18f
        textView.isClickable = true
        textView.layoutParams = RecyclerView.LayoutParams(-1, -2)
        textView.setPadding(30, 20, 30, 20)
        textView.setTextColor(ColorStateList.valueOf(context.resources.getColor(R.color.textButton)))
        textView.setBackgroundColor(context.resources.getColor(R.color.itemBackground))
        return KTestRecyclerHolder(textView)
    }

    override fun onBindViewHolder(holder: KTestRecyclerHolder, position: Int) {
        if (holder.itemView is TextView) {
            holder.itemView.text = items?.get(position)
        }
    }

    class KTestRecyclerHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}