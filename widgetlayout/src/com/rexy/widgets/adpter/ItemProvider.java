package com.rexy.widgets.adpter;

import android.view.View;
import android.view.ViewGroup;

/**
 * use to provide item data at a appointed position
 * or a View for a ViewGroup
 * @author: rexy
 * @date: 2016-01-06 10:20
 */
public interface ItemProvider {
    /**
     *  get title at a appointed position
     */
    CharSequence getTitle(int position);
    /**
     * get item at a appointed position
     */
    Object getItem(int position);

    /**
     * item total count
     */
    int getCount();

    /**
     * a ViewGroup can hold this interface reference to obtain child View
     */
    interface ViewProvider extends ItemProvider {
        /**
         * get View type at a position
         */
        int getViewType(int position);

        /**
         * get View by a appointed position
         * @param position  position of data list
         * @param convertView may be null it there is no cache View can reuse
         * @param parent  parent ViewGroup who conceive this interface reference
         */
        View getView(int position, View convertView, ViewGroup parent);
    }
}
