package com.rexy.widgets.anim;

import android.support.v4.util.Pools;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by rexy on 16/7/12.
 */
class AnimateQueue {

    private static AnimateQueue mInstance = null;

    private static final int MAX_POOL_SIZE = 5;
    private static final Pools.SimplePool<AnimateFrame> mPool = new Pools.SimplePool<>(MAX_POOL_SIZE);

    public static AnimateQueue getInstance() {
        if (mInstance == null) {
            synchronized (AnimateQueue.class) {
                if (mInstance == null) {
                    mInstance = new AnimateQueue();
                }
            }
        }
        return mInstance;
    }

    private AnimateLooper mAnimateLooper;
    private boolean mIsIdle = true;
    private boolean mIsLoopDispatcher;
    private final Queue<AnimateFrame> mLoopQueue = new LinkedList<AnimateFrame>();
    private final Queue<AnimateFrame> mCacheQueue = new LinkedList<AnimateFrame>();
    private final Queue<AnimateFrame> mRecyclQueue = new LinkedList<AnimateFrame>();

    private AnimateQueue() {
        if (mInstance != null) {
            throw new SecurityException("this is a singleton class,already have a instance of " + getClass().getSimpleName());
        }
    }

    protected AnimateLooper getAnimateLooper() {
        if (mAnimateLooper == null) {
            mAnimateLooper = AnimateLooper.createLooper();
            mAnimateLooper.setLooperAnimater(this);
        }
        return mAnimateLooper;
    }

    protected static AnimateFrame obtain(int id, float fromValue, float toValue, int duration, Object extras) {
        AnimateFrame data = mPool.acquire();
        if (data == null) {
            data = new AnimateFrame();
        }
        data.mId = id;
        data.mFromValue = fromValue;
        data.mToValue = toValue;
        data.mDuration = duration;
        data.mExtras = extras;
        return data;
    }

    protected AnimateFrame findFrame(int uniqueId) {
        for (AnimateFrame frame : mLoopQueue) {
            if (frame.mUniqueId == uniqueId) {
                return frame;
            }
        }
        for (AnimateFrame frame : mCacheQueue) {
            if (frame.mUniqueId == uniqueId) {
                return frame;
            }
        }
        return null;
    }

    protected void stopAll(String tag, boolean silence) {
        for (AnimateFrame frame : mLoopQueue) {
            if (tag.equals(frame.mTag)) {
                frame.markAsRecycle();
                if (silence) {
                    frame.mSequence = 0;
                }
            }
        }
        for (AnimateFrame frame : mCacheQueue) {
            if (tag.equals(frame.mTag)) {
                frame.markAsRecycle();
                if (silence) {
                    frame.mSequence = 0;
                }
            }
        }
    }

    protected void registerFrame(AnimateFrame frame) {
        if (mIsLoopDispatcher) {
            mCacheQueue.offer(frame);
        } else {
            mLoopQueue.offer(frame);
            if (mIsIdle) {
                mIsIdle = false;
                getAnimateLooper().start();
            }
        }
    }

    protected void loop(long elapsedMillis, long currentMillis) {
        AnimateFrame optFrame;
        while (null != (optFrame = mRecyclQueue.poll())) {
            mPool.release(optFrame.recycled());
        }
        mIsLoopDispatcher = true;
        Iterator<AnimateFrame> its = mLoopQueue.iterator();
        while (its.hasNext()) {
            optFrame = its.next();
            if (optFrame.loop(elapsedMillis, currentMillis)) {
                its.remove();
                mRecyclQueue.offer(optFrame);
            }
        }
        mIsLoopDispatcher = false;
        while (null != (optFrame = mCacheQueue.poll())) {
            mLoopQueue.offer(optFrame);
        }
        if (mIsIdle = mLoopQueue.isEmpty() && mRecyclQueue.isEmpty()) {
            getAnimateLooper().stop();
        }
    }

    public void recycle() {
        mLoopQueue.clear();
        mRecyclQueue.clear();
        if (mAnimateLooper != null) {
            mAnimateLooper.recycle();
        }
        mInstance = null;
    }

}
