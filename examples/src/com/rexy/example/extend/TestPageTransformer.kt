package com.rexy.model

import android.view.View

import com.rexy.widgets.layout.PageScrollView

/**
 * Created by rexy on 17/4/12.
 */

class TestPageTransformer : PageScrollView.PageTransformer {
    companion object {
        private val MIN_SCALE = 0.7f
        private val MIN_ALPHA = 0.3f
    }

    var mAdjustTranslate = false

    override fun transformPage(view: View, position: Float, horizontal: Boolean) {
        val pageSize = if (horizontal) view.width else view.height
        when {
            position < -1 -> { // [-Infinity,-1)way off-screen to the left
                view.alpha = MIN_ALPHA
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
            }
            position <= 1 -> { // [-1,1]
                val percent = 1 - Math.abs(position)
                val scale = MIN_SCALE + (1 - MIN_SCALE) * percent
                if (mAdjustTranslate) {
                    val horizontalMargin = pageSize * (1 - scale) / 2
                    if (position > 0) {
                        if (horizontal) {
                            view.translationX = horizontalMargin
                        } else {
                            view.translationY = horizontalMargin
                        }
                    } else {
                        if (horizontal) {
                            view.translationX = -horizontalMargin
                        } else {
                            view.translationY = -horizontalMargin
                        }
                    }
                }
                view.scaleX = scale
                view.scaleY = scale
                view.alpha = MIN_ALPHA + (1 - MIN_ALPHA) * percent
            }
            else -> { // (1,+Infinity]page is way off-screen to the right.
                view.alpha = MIN_ALPHA
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
            }
        }
    }

    override fun recoverTransformPage(view: View, horizontal: Boolean) {
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
        if (mAdjustTranslate) {
            if (horizontal) {
                view.translationX = 0f
            } else {
                view.translationY = 0f
            }
        }
    }
}
