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
class TestRecyclerAdapter(var mContext: Context, var mItems:List<String> ?) :RecyclerView.Adapter<TestRecyclerAdapter.KTestRecyclerHolder>(){

    override fun getItemCount()= mItems?.size?:0

    override fun onCreateViewHolder(p: ViewGroup, type: Int): KTestRecyclerHolder {
        val textView = FadeTextButton(mContext)
        textView.textSize = 18f
        textView.isClickable = true
        textView.layoutParams = RecyclerView.LayoutParams(-1, -2)
        textView.setPadding(30, 20, 30, 20)
        textView.setTextColor(ColorStateList.valueOf(mContext.resources.getColor(R.color.textButton)))
        textView.setBackgroundColor(mContext.resources.getColor(R.color.itemBackground))
        return KTestRecyclerHolder(textView)
    }

    override fun onBindViewHolder(holder: KTestRecyclerHolder, position: Int) {
        if(holder.itemView is TextView){
            holder.itemView.text=mItems?.get(position);
        }
    }

    class KTestRecyclerHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}