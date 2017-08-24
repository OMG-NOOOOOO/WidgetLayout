package com.rexy.example.extend

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.TextView

/**
 * TODO:功能说明
 *
 * @author: renzheng657
 * @date: 2017-08-24 14:38
 */
class FadeTextButton : TextView {
    companion object {
        private val TAG = FadeTextButton::class.java.simpleName
        private val FLAG_TOUCH = 1
        private val FLAG_ANIM_NOW = 2
        private val FLAG_FLADE_FLOAT = 4
        private val FLAG_FADE_ENABLE = 8
        private val FLAG_WILL_STOP = 16
        private val FLAG_LAST_FADE = 32
        private val FLAG_FADE_EXCEPT_STATELIST = 64
    }

    private var mPressAlphaTo = 0.75f
    private var mCurrentAlpha = 1f
    private var mFadeDuration = 250
    private var mTouchSlop = 0
    private var mFloatDrawable: Drawable? = null

    internal var mFlag = FLAG_FLADE_FLOAT or FLAG_FADE_ENABLE

    constructor(context: Context) : super(context) {
        initInner(context)
    }

    constructor(context: Context, attr: AttributeSet) : this(context, attr, 0)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle) {
        initInner(context)
    }

    fun initInner(context: Context) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    fun setFadeFloat(fadeFloat: Boolean) {
        mFlag = when (fadeFloat) {
            true -> mFlag or FLAG_FLADE_FLOAT
            false -> mFlag or FLAG_FLADE_FLOAT.inv()
        }
    }

    fun setPressAlphaTo(alpha: Float) {
        mPressAlphaTo = Math.min(Math.max(alpha, 0f), 1f)
    }

    fun setPressFadeAble(fadeAble: Boolean, exceptSelectDrawable: Boolean) {
        mFlag = when (fadeAble) {
            true -> mFlag or FLAG_FADE_ENABLE
            false -> mFlag or FLAG_FADE_ENABLE.inv()
        }
        mFlag = when (exceptSelectDrawable) {
            true -> mFlag or FLAG_FADE_EXCEPT_STATELIST
            false -> mFlag or FLAG_FADE_EXCEPT_STATELIST.inv()
        }
    }

    fun setFadeDuration(duration: Int) {
        mFadeDuration = duration
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val flag1 = FLAG_FADE_ENABLE or FLAG_FLADE_FLOAT
        val flag2 = FLAG_TOUCH or FLAG_ANIM_NOW or FLAG_WILL_STOP
        if (flag1 == flag1 and mFlag && 0 != flag2 and mFlag) {
            val fadeDrawable = getFadeDrawable()
            fadeDrawable.setBounds(0, 0, width, height)
            fadeDrawable.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (FLAG_FADE_ENABLE == FLAG_FADE_ENABLE and mFlag && !(FLAG_FADE_EXCEPT_STATELIST == mFlag and FLAG_FADE_EXCEPT_STATELIST && background is StateListDrawable)) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                mFlag = mFlag or FLAG_TOUCH
                startFadeAnim()
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mFlag = mFlag or FLAG_TOUCH.inv()
                stopFadeAnim()
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (FLAG_TOUCH == FLAG_TOUCH and mFlag) {
                    if (!pointInView(event.x, event.y, mTouchSlop.toFloat())) {
                        mFlag = mFlag or FLAG_TOUCH.inv()
                        stopFadeAnim()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun pointInView(localX: Float, localY: Float, slop: Float): Boolean {
        return localX >= -slop && localY >= -slop && localX < right - left + slop &&
                localY < bottom - top + slop
    }

    private fun startFadeAnim() {
        val anim = FadeAnimation(1f, mPressAlphaTo)
        anim.duration = mFadeDuration.toLong()
        startAnimation(anim)
    }

    private fun stopFadeAnim() {
        mFlag = mFlag or FLAG_WILL_STOP
        if (FLAG_ANIM_NOW == FLAG_ANIM_NOW and mFlag) {
            clearAnimation()
        }
        val anim = FadeAnimation(mCurrentAlpha, 1f)
        anim.duration = (mFadeDuration * 0.8f).toInt().toLong()
        startAnimation(anim)
    }


    private fun getFadeDrawable(): Drawable {
        if (mFloatDrawable == null) {
            mFloatDrawable = ColorDrawable(0xff000000.toInt())
        }
        return mFloatDrawable as Drawable
    }


    private inner class FadeAnimation(private val mFromAlpha: Float, private val mToAlpha: Float) : Animation() {

        init {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    mFlag = mFlag or FLAG_ANIM_NOW
                    if (FLAG_TOUCH == FLAG_TOUCH and mFlag) {
                        mFlag = mFlag or FLAG_LAST_FADE
                    } else {
                        mFlag = mFlag or FLAG_LAST_FADE.inv()
                    }
                }

                override fun onAnimationEnd(animation: Animation) {
                    mFlag = mFlag or FLAG_ANIM_NOW.inv()
                    if (FLAG_WILL_STOP == FLAG_WILL_STOP and mFlag && 0 == FLAG_LAST_FADE and mFlag) {
                        mFlag = mFlag or FLAG_WILL_STOP.inv()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            mCurrentAlpha = mFromAlpha + (mToAlpha - mFromAlpha) * interpolatedTime
            if (FLAG_FLADE_FLOAT == FLAG_FLADE_FLOAT and mFlag) {
                val drawable = getFadeDrawable()
                drawable.alpha = (255 * (1 - mCurrentAlpha)).toInt()
                invalidate()
            } else {
                mCurrentAlpha *= mCurrentAlpha
                alpha = mCurrentAlpha
            }
        }
    }


}