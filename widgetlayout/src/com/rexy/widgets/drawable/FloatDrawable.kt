package com.rexy.widgets.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock

/**
 * a customized color drawable that support animation to change its alpha.
 * @author: rexy
 * @date: 2017-02-16 14:00
 */
class FloatDrawable : Drawable, Runnable, Animatable {
    /**
     * current color alpha in range [0...255]
     */
    internal var mCurrentAlpha = 0
    internal var mAnimStartTime: Long = 0
    internal var mDevDebug = true
    internal var mRunning = false
    internal var mAnimating = false
    internal var mRectRound = RectF()
    internal var mPaint = Paint()


    /**
     * min supported alpha
     */
    private var mMinAlpha = 0
    /**
     * max supported alpha
     */
    private var mMaxAlpha = 50
    /**
     * animation duration for color alpha change between current and a target alpha
     */
    private var mDuration = 120
    /**
     * color rgb value  exclude alpha
     */
    private var mColorWithOutAlpha = 0xFF000000.toInt()
    /**
     * a hove rectangle shape round corner radius
     */
    private var mRoundRadius = 0

    private var mAnimToVisible = false

    private fun print(msg: String) {
        println(msg)
    }

    constructor(color: Int) {
        mPaint.color = mColorWithOutAlpha and 0x00FFFFFF or (mCurrentAlpha shl 24)
        color(color)
    }

    constructor(color: Int, minAlpha: Int, maxAlpha: Int) {
        mPaint.color = mColorWithOutAlpha and 0x00FFFFFF or (mCurrentAlpha shl 24)
        alpha(minAlpha, maxAlpha)
        color(color)
    }

    /**
     * set current color alpha
     */
    fun alpha(alpha: Int): FloatDrawable {
        if (mCurrentAlpha != alpha && alpha >= mMinAlpha && alpha <= mMaxAlpha) {
            mCurrentAlpha = alpha
            if (mRunning) {
                mAnimStartTime = SystemClock.uptimeMillis() - mDuration + ((if (mAnimToVisible) mCurrentAlpha - mMinAlpha else mMaxAlpha - mCurrentAlpha) * mDuration / (mMaxAlpha - mMinAlpha).toFloat()).toInt()
            }
            mPaint.color = mColorWithOutAlpha and 0x00FFFFFF or (mCurrentAlpha shl 24)
            invalidateSelf()
        }
        return this@FloatDrawable
    }

    /**
     * set min and max color alpha to change between
     */
    fun alpha(minAlpha: Int, maxAlpha: Int): FloatDrawable {
        var minAlpha = minAlpha
        var maxAlpha = maxAlpha
        if (minAlpha < 0 || minAlpha > 255) {
            minAlpha = mMinAlpha
        }
        if (maxAlpha < 0 || maxAlpha > 255) {
            maxAlpha = mMinAlpha
        }
        if (minAlpha <= maxAlpha && (minAlpha != mMinAlpha || maxAlpha != mMaxAlpha)) {
            mMinAlpha = minAlpha
            mMaxAlpha = maxAlpha
            mCurrentAlpha = if (mAnimToVisible) mMaxAlpha else mMinAlpha
            if (mRunning) {
                val percent = (SystemClock.uptimeMillis() - mAnimStartTime) / mDuration.toFloat()
                val startAlpha = if (mAnimToVisible) mMinAlpha else mMaxAlpha
                mCurrentAlpha = startAlpha + Math.round((mCurrentAlpha - startAlpha) * percent)
            }
            mPaint.color = mColorWithOutAlpha and 0x00FFFFFF or (mCurrentAlpha shl 24)
            invalidateSelf()
        }
        return this@FloatDrawable
    }

    /**
     * set color
     */
    fun color(color: Int): FloatDrawable {
        val alpha = color.ushr(24)
        val colorWithoutAlpha = color and 0x00FFFFFF or 0xFF000000.toInt()
        var changed = false
        if (alpha != mCurrentAlpha && alpha >= mMinAlpha && alpha <= mMaxAlpha) {
            mCurrentAlpha = alpha
            changed = true
        }
        if (colorWithoutAlpha != mColorWithOutAlpha) {
            mColorWithOutAlpha = colorWithoutAlpha
            changed = true
        }
        if (changed) {
            mPaint.color = mColorWithOutAlpha and 0x00FFFFFF or (mCurrentAlpha shl 24)
            invalidateSelf()
        }
        return this@FloatDrawable
    }

    /**
     * set duration for animation
     */
    fun duration(duration: Int): FloatDrawable {
        if (duration > 0) {
            val deta = mDuration - duration
            if (mRunning) {
                mAnimStartTime += deta.toLong()
            }
            mDuration = duration
        }
        return this@FloatDrawable
    }

    fun radius(roundRadius: Int): FloatDrawable {
        if (mRoundRadius != roundRadius) {
            mRoundRadius = roundRadius
            if (mRoundRadius > 0) {
                mPaint.isAntiAlias = true
                mPaint.isDither = true
            } else {
                mPaint.isAntiAlias = false
                mPaint.isDither = false
            }
            invalidateSelf()
        }
        return this@FloatDrawable
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        if (mDevDebug) {
            print(String.format("setVisible(visible=%s,restart=%s)", visible, restart))
        }
        if (visible) {
            if (restart || changed) {
                var targetAlpha = mCurrentAlpha
                if (mAnimating) {
                    if (restart) {
                        mAnimStartTime = SystemClock.uptimeMillis()
                        targetAlpha = if (mAnimToVisible) mMinAlpha else mMaxAlpha
                    } else {
                        targetAlpha = mCurrentAlpha + if (mAnimToVisible) 1 else -1
                        mAnimStartTime = SystemClock.uptimeMillis() + (mDuration * (if (mAnimToVisible) mMinAlpha - targetAlpha else targetAlpha - mMaxAlpha) / (mMaxAlpha - mMinAlpha).toFloat()).toInt()
                    }
                }
                setFrame(targetAlpha, true, mColorWithOutAlpha != 0 && mMaxAlpha > mMinAlpha && mAnimating)
            }
        } else {
            unscheduleSelf(this)
        }
        return changed
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    /**
     * animate to visible or invisible
     * @param toVisible target visible state
     */
    fun start(toVisible: Boolean) {
        mAnimating = true
        if (mRunning) {
            if (mAnimToVisible != toVisible) {
                mAnimToVisible = toVisible
                mAnimStartTime = SystemClock.uptimeMillis() + (mDuration * (if (toVisible) mMinAlpha - mCurrentAlpha else mCurrentAlpha - mMaxAlpha) / (mMaxAlpha - mMinAlpha).toFloat()).toInt()
            }
        } else {
            mAnimToVisible = toVisible
            var targetFrame = mCurrentAlpha
            if (targetFrame < mMinAlpha || targetFrame > mMaxAlpha) {
                targetFrame = if (mAnimToVisible) mMinAlpha else mMaxAlpha
            } else {
                mAnimStartTime = SystemClock.uptimeMillis() + (mDuration * (if (toVisible) mMinAlpha - targetFrame else targetFrame - mMaxAlpha) / (mMaxAlpha - mMinAlpha).toFloat()).toInt()
            }
            if (mDevDebug) {
                print(String.format("start(toVisible=%s)", mAnimToVisible))
            }
            setFrame(targetFrame, false, mMaxAlpha > mMinAlpha && mColorWithOutAlpha != 0)
        }
    }

    override fun start() {
        start(if (mRunning) mAnimToVisible else !mAnimToVisible)
    }

    override fun stop() {
        mAnimating = false
        if (isRunning) {
            if (mDevDebug) {
                print(String.format("stop(lastVisible=%s)", mAnimToVisible))
            }
            unscheduleSelf(this)
        }
    }

    override fun isRunning(): Boolean {
        return mRunning
    }

    override fun run() {
        val percent = (SystemClock.uptimeMillis() - mAnimStartTime) / mDuration.toFloat()
        val animFinished = percent > 1f || percent < 0
        val endAlpha = if (mAnimToVisible) mMaxAlpha else mMinAlpha
        if (animFinished) {
            if (mDevDebug) {
                print(String.format("runfinish(toVisible=%s,targetAlpha=%d)", mAnimToVisible, endAlpha))
            }
            setFrame(endAlpha, false, false)
            stop()
        } else {
            val startAlpha = if (mAnimToVisible) mMinAlpha else mMaxAlpha
            val targetAlpha = startAlpha + Math.round((endAlpha - startAlpha) * percent)
            if (mDevDebug) {
                print(String.format("running(toVisible=%s,percent=%3f,targetAlpha=%d)", mAnimToVisible, percent, targetAlpha))
            }
            setFrame(targetAlpha, false, true)
        }
    }

    private fun setFrame(frame: Int, unschedule: Boolean, animate: Boolean) {
        var frame = frame
        if (animate) {
            if (frame < mMinAlpha) {
                frame = mMinAlpha
            }
            if (frame > mMaxAlpha) {
                frame = mMaxAlpha
            }
        }
        mAnimating = animate
        if (frame != mCurrentAlpha) {
            mCurrentAlpha = frame
            mPaint.color = mColorWithOutAlpha and 0x00FFFFFF or (mCurrentAlpha shl 24)
            invalidateSelf()
        }
        if (unschedule) {
            unscheduleSelf(this)
        }
        if (animate) {
            mRunning = true
            scheduleSelf(this, SystemClock.uptimeMillis() + 16)
        }
    }

    override fun draw(canvas: Canvas) {
        if (mCurrentAlpha > 0 && mCurrentAlpha <= 255) {
            if (mRoundRadius <= 0) {
                canvas.drawRect(bounds, mPaint)
            } else {
                mRectRound.set(bounds)
                canvas.drawRoundRect(mRectRound, mRoundRadius.toFloat(), mRoundRadius.toFloat(), mPaint)
            }
        }
    }

    override fun unscheduleSelf(what: Runnable) {
        if (what === this) {
            mRunning = false
        }
        super.unscheduleSelf(what)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

}
