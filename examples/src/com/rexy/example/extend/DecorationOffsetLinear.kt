package com.rexy.model

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View

/**
 * This class can only be used in the RecyclerView which use a LinearLayoutManager or
 * its subclass.
 */
class DecorationOffsetLinear @JvmOverloads constructor(private val isHorizontal: Boolean, private val mContentOffset: Int, private val mContentOffsetStart: Int = 0, private val mContentOffsetEnd: Int = 0) : RecyclerView.ItemDecoration() {

    private val mTypeOffsetsFactories = SparseArray<OffsetsCreator>()
    private var isNoMoreData = true
    private var isApplyOffsetToEdge= false

    fun setApplyOffsetToEdge(isOffsetEdge: Boolean) {
        this.isApplyOffsetToEdge = isOffsetEdge
    }

    fun setNoMoreData(noMoreData: Boolean) {
        isNoMoreData = noMoreData
    }

    fun registerTypeOffset(itemType: Int, offsetsCreator: OffsetsCreator) {
        mTypeOffsetsFactories.put(itemType, offsetsCreator)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val adapterPosition = parent.getChildAdapterPosition(view)
        val adapterLastCountIndex = state!!.itemCount - 1
        val itemOffset = getDividerOffsets(parent, view, adapterPosition)
        val contentItemFirst = adapterPosition == 0
        val contentItemLast = adapterPosition == adapterLastCountIndex
        val dataLastItem = contentItemLast && isNoMoreData
        if (isHorizontal) {
            outRect.right = if (dataLastItem) mContentOffsetEnd else itemOffset
            if (isApplyOffsetToEdge) {
                outRect.top = itemOffset
                outRect.bottom = itemOffset
            }
            if (contentItemFirst) {
                outRect.left = mContentOffsetStart
            }
        } else {
            outRect.bottom = if (dataLastItem) mContentOffsetEnd else itemOffset
            if (isApplyOffsetToEdge) {
                outRect.left = itemOffset
                outRect.right = itemOffset
            }
            if (contentItemFirst) {
                outRect.top = mContentOffsetStart
            }
        }
    }

    private fun getDividerOffsets(parent: RecyclerView, view: View, adapterPosition: Int): Int {
        if (mTypeOffsetsFactories.size() == 0) {
            return mContentOffset
        }
        val itemType = parent.adapter.getItemViewType(adapterPosition)
        val offsetsCreator = mTypeOffsetsFactories.get(itemType)
        if (offsetsCreator != null) {
            return offsetsCreator.create(parent, adapterPosition)
        }
        return mContentOffset
    }

    interface OffsetsCreator {
        fun create(parent: RecyclerView, adapterPosition: Int): Int
    }
}
