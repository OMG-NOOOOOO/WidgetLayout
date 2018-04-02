package com.rexy.widgets.adpter;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 这个数据适配器是不需要提供界面,只提供数据的.
 * 作者：rexy on 15/7/10 10:12
 * 邮箱：rexy@pingan.com.cn
 */
public class AbsDataAdapter<T> {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    protected List<T> mItems;

    public AbsDataAdapter(List<T> items) {
        mItems = items == null ? new ArrayList<T>(16) : items;
    }

    public void add(T item, boolean isEnd) {
        if (item != null) {
            if (isEnd) {
                mItems.add(item);
            } else {
                mItems.add(0, item);
            }
        }
    }

    public void add(List<T> items, boolean isEnd) {
        if (items != null && items.size() > 0) {
            if (isEnd) {
                mItems.addAll(items);
            } else {
                mItems.addAll(0, items);
            }
        }
    }

    /**
     * clear all items from the list.
     */
    public void clear() {
        mItems.clear();
    }

    public boolean insert(T item, int where) {
        if (where < 0 || where > getCount()) {
            return false;
        } else {
            mItems.add(where, item);
            return true;
        }
    }

    public int indexOf(Object item) {
        return mItems.indexOf(item);
    }

    public void set(List<T> items) {
        if (items == null) {
            mItems = new ArrayList<T>(16);
        } else {
            mItems = items;
        }
    }

    public boolean set(int index, T item) {
        if (item != null && index >= 0 && index < getCount()) {
            mItems.set(index, item);
            return true;
        }
        return false;
    }

    public T remove(int which) {
        if (which < 0 || which >= getCount()) {
            return null;
        } else {
            T item = mItems.remove(which);
            return item;
        }
    }

    public boolean remove(T item) {
        return mItems.remove(item);
    }

    public boolean swap(int from, int to) {
        if (from == to || from < 0 || to < 0 || from >= getCount() || to >= getCount()) {
            return false;
        } else {
            T tf = mItems.get(from);
            mItems.set(from, mItems.get(to));
            mItems.set(to, tf);
            return true;
        }
    }

    public void sort(Comparator<? super T> comparator) {
        Collections.sort(mItems, comparator);
    }

    public int getCount() {
        return mItems.size();
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int pos) {
        return pos;
    }

    public List<T> getItems() {
        return mItems;
    }

    public boolean enable(int pos) {
        return true;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }
}