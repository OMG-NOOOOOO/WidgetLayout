package com.rexy.widgets.anim;

import android.os.SystemClock;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * @author: rexy
 * @date: 2016-07-13 13:04
 */
public class AnimateFrame {
    private static Interpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator();
    protected static final int STATE_ANIMATE_WAIT = 0;
    protected static final int STATE_ANIMATE_START = 1;
    protected static final int STATE_ANIMATE_CHANGE = 2;
    protected static final int STATE_ANIMATE_FINISHED = 3;

    protected long mTimeStart = 0;
    protected int mAnimateState = STATE_ANIMATE_WAIT;
    protected int mId = 0, mUniqueId;
    protected int mSequence = 0;
    protected int mDuration = 0;

    protected float mFromValue;
    protected float mToValue;

    protected float mPreValue;
    protected float mCurValue;
    protected Object mExtras;

    protected String mTag = null;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("AnimateFrame#").append(mId).append('{');
        sb.append("from=").append(mFromValue).append(",to=").append(mToValue);
        sb.append(",curent=").append(mCurValue).append(",previous=").append(mPreValue);
        sb.append(",sequence=").append(mSequence).append(",state=").append(mAnimateState).append('}');
        return sb.toString();
    }

    protected ArrayList<IAnimateFrame> mCallbacks = new ArrayList<>(4);
    protected Interpolator mInterpolator = null;

    protected AnimateFrame() {
    }

    protected boolean loop(long elapsedMillis, long loopAtTime) {
        if (mAnimateState == STATE_ANIMATE_FINISHED) {
            if (mSequence > 0) {
                notifyCallback();
                mSequence = 0;
            }
        } else {
            long currentMillis = SystemClock.uptimeMillis();
            if (currentMillis >= mTimeStart) {
                float deta = currentMillis - mTimeStart;
                if (mAnimateState == STATE_ANIMATE_CHANGE) {
                    if (deta >= mDuration) {
                        deta = mDuration;
                        mAnimateState = STATE_ANIMATE_FINISHED;
                    }
                } else {
                    if (mAnimateState == STATE_ANIMATE_WAIT) {
                        mAnimateState = STATE_ANIMATE_START;
                        mCurValue = mFromValue;
                    } else {
                        mAnimateState = STATE_ANIMATE_CHANGE;
                    }
                }
                mPreValue = mCurValue;
                deta = deta * 1000 / mDuration / 1000;
                Interpolator interpol = mInterpolator == null ? DEFAULT_INTERPOLATOR : mInterpolator;
                deta = interpol == null ? deta : interpol.getInterpolation(deta);
                mCurValue = mFromValue + (mToValue - mFromValue) * deta;
                mSequence++;
                notifyCallback();
            }
        }
        return mAnimateState == STATE_ANIMATE_FINISHED;
    }

    protected AnimateFrame recycled() {
        mTag = null;
        mExtras = null;
        mSequence = 0;
        mInterpolator = null;
        mCallbacks.clear();
        return this;
    }

    protected void notifyCallback() {
        for (IAnimateFrame frame : mCallbacks) {
            frame.onAnimFrameChanged(mId, this);
        }
    }

    protected void markAsRecycle() {
        mAnimateState = STATE_ANIMATE_FINISHED;
    }

    public boolean isAnimateStart() {
        return STATE_ANIMATE_START == mAnimateState;
    }

    public boolean isAnimateChange() {
        return STATE_ANIMATE_CHANGE == mAnimateState;
    }

    public boolean isAnimateFinished() {
        return STATE_ANIMATE_FINISHED == mAnimateState;
    }

    public float getCurrentValue() {
        return mCurValue;
    }

    public float getPreviousValue() {
        return mPreValue;
    }

    public int getFrameCount() {
        return mSequence;
    }

    public <T> T getExtras() {
        return (T) mExtras;
    }


    public AnimateFrame addCallback(IAnimateFrame callback) {
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
        return this;
    }

    public AnimateFrame setExtras(Object extras) {
        mExtras = extras;
        return this;
    }

    public AnimateFrame setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        return this;
    }

    public AnimateFrame setTag(String tag) {
        if (mSequence == 0) {
            mTag = tag;
        }
        return this;
    }

    public static AnimateFrame obtain(int id, int duration, IAnimateFrame callback) {
        return obtain(id, 0, 1, duration, callback);
    }

    public static AnimateFrame obtain(int id, float fromValue, float toValue, int duration, IAnimateFrame callback) {
        return obtain(id, fromValue, toValue, duration, null, callback);
    }

    public static AnimateFrame obtain(int id, float fromValue, float toValue, int duration, Object extras, IAnimateFrame callback) {
        AnimateFrame frame = AnimateQueue.obtain(id, fromValue, toValue, duration, extras);
        if (callback != null) {
            frame.mCallbacks.add(callback);
        }
        return frame;
    }

    public void start() {
        startDelayed(0);
    }

    public void startDelayed(int delayed) {
        mTimeStart = SystemClock.uptimeMillis() + delayed;
        mAnimateState = STATE_ANIMATE_WAIT;
        stop(mTag, mId, true);
        mUniqueId = (mTag + "_" + mId).hashCode();
        AnimateQueue.getInstance().registerFrame(this);
    }

    public void stop(boolean immediately) {
        markAsRecycle();
        if (immediately && mSequence > 0) {
            mSequence = 0;
        }
    }

    public static boolean stop(String tag, int id, boolean immediately) {
        AnimateFrame frame = AnimateQueue.getInstance().findFrame((tag + "_" + id).hashCode());
        if (frame != null) {
            frame.stop(immediately);
            return true;
        }
        return false;
    }

    public static void stopAllByTag(String tag, boolean silence) {
        AnimateQueue.getInstance().stopAll(tag, silence);
    }

    public static interface IAnimateFrame {
        void onAnimFrameChanged(int id, AnimateFrame frame);
    }
}
