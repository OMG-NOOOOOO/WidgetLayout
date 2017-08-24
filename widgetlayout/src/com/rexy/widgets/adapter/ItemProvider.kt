package com.rexy.widgets.adapter

import android.view.View
import android.view.ViewGroup

/**
 * use to provide item data at a appointed position
 * or a View for a ViewGroup
 * @author: rexy
 * @date: 2016-01-06 10:20
 */
interface ItemProvider {
    /**
     * get title at a appointed position
     */
    fun getTitle(position: Int): CharSequence

    /**
     * get item at a appointed position
     */
    fun getItem(position: Int): Any

    /**
     * item total count
     */
    val count: Int

    /**
     * a ViewGroup can hold this interface reference to obtain child View
     */
    interface ViewProvider : ItemProvider {
        /**
         * get View type at a position
         */
        fun getViewType(position: Int): Int

        /**
         * get View by a appointed position
         * @param position  position of data list
         * @param convertView may be null it there is no cache View can reuse
         * @param parent  parent ViewGroup who conceive this interface reference
         */
        fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    }
}
