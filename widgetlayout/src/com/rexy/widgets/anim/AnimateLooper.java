/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.rexy.widgets.anim;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Choreographer;


abstract class AnimateLooper {
    protected long mLastTime;
    protected boolean mStarted;
    AnimateQueue mLooperAnimater = null;

    public void setLooperAnimater(AnimateQueue looperAnimater) {
        mLooperAnimater = looperAnimater;
    }


    protected abstract void start();

    protected abstract void stop();

    protected void recycle() {
        mLooperAnimater = null;
    }


    public static AnimateLooper createLooper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return ChoreographerAnimateLooper.create();
        } else {
            return LegacyAnimateLooper.create();
        }
    }

    private static class LegacyAnimateLooper extends AnimateLooper {

        private final Handler mHandler;
        private final Runnable mLooperRunnable;


        public static AnimateLooper create() {
            return new LegacyAnimateLooper(new Handler());
        }

        public LegacyAnimateLooper(Handler handler) {
            mHandler = handler;
            mLooperRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!mStarted || mLooperAnimater == null) {
                        return;
                    }
                    long currentTime = SystemClock.uptimeMillis();
                    mLooperAnimater.loop(currentTime - mLastTime, currentTime);
                    mLastTime = currentTime;
                    mHandler.post(mLooperRunnable);
                }
            };
        }

        @Override
        public void start() {
            if (mStarted) {
                return;
            }
            mStarted = true;
            mLastTime = SystemClock.uptimeMillis();
            mHandler.removeCallbacks(mLooperRunnable);
            mHandler.post(mLooperRunnable);
        }

        @Override
        public void stop() {
            mStarted = false;
            mHandler.removeCallbacks(mLooperRunnable);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static class ChoreographerAnimateLooper extends AnimateLooper {

        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mFrameCallback;

        public static ChoreographerAnimateLooper create() {
            return new ChoreographerAnimateLooper(Choreographer.getInstance());
        }

        public ChoreographerAnimateLooper(Choreographer choreographer) {
            mChoreographer = choreographer;
            mFrameCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    if (!mStarted || mLooperAnimater == null) {
                        return;
                    }
                    long currentTime = SystemClock.uptimeMillis();
                    mLooperAnimater.loop(currentTime - mLastTime, currentTime);
                    mLastTime = currentTime;
                    mChoreographer.postFrameCallback(mFrameCallback);
                }
            };
        }

        @Override
        public void start() {
            if (mStarted) {
                return;
            }
            mStarted = true;
            mLastTime = SystemClock.uptimeMillis();
            mChoreographer.removeFrameCallback(mFrameCallback);
            mChoreographer.postFrameCallback(mFrameCallback);
        }

        @Override
        public void stop() {
            mStarted = false;
            mChoreographer.removeFrameCallback(mFrameCallback);
        }
    }
}