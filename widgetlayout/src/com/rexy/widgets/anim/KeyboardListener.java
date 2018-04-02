package com.rexy.widgets.anim;

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**

 *
 * @author: rexy
 * @date: 2016-05-19 13:23
 */
public final class KeyboardListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private Rect mVisibleViewArea;
    private int mMinKeyboardHeightDetected;
    private int mKeyboardHeight = 0;
    private int mRealKeyBoardHeight = 0;
    private WeakReference<View> mTargeView;
    private List<IKeyboardListener> mKeyBoardListeners = new ArrayList<>(4);

    public KeyboardListener(int minKeyboardHeightDetected) {
        this(null, minKeyboardHeightDetected);
    }

    public KeyboardListener(View target, int minKeyboardHeightDetected) {
        mMinKeyboardHeightDetected = minKeyboardHeightDetected;
        if (target != null) {
            bindTarget(target);
        }
    }

    public void bindTarget(View target) {
        if (target != null) {
            mTargeView = new WeakReference<View>(target);
            mVisibleViewArea = mVisibleViewArea == null ? new Rect() : mVisibleViewArea;
            if (mMinKeyboardHeightDetected <= 0) {
                mMinKeyboardHeightDetected = 120;
            }
            target.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    public void unbindTarget(View target, boolean cleanListener) {
        View targetView = target;
        if (target == null && mTargeView != null) {
            targetView = mTargeView.get();
        }
        if (targetView != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                targetView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
                targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        }
        if (cleanListener) {
            mKeyBoardListeners.clear();
            mVisibleViewArea = null;
            mTargeView.clear();
            mTargeView = null;
        }
    }

    public void registerKeyboardChanged(IKeyboardListener l, boolean register) {
        if (l != null) {
            if (register) {
                if (!mKeyBoardListeners.contains(l)) {
                    mKeyBoardListeners.add(l);
                    if (mRealKeyBoardHeight > 0) {
                        l.onKeyboardChanged(mTargeView.get(), isKeyboardVisible(), mRealKeyBoardHeight);
                    }
                }
            } else {
                mKeyBoardListeners.remove(l);
            }
        }
    }


    public boolean isKeyboardVisible() {
        return mKeyboardHeight > 0;
    }

    public int getKeyboardHeight() {
        return mRealKeyBoardHeight;
    }

    @Override
    public void onGlobalLayout() {
        View targetView = mTargeView == null ? null : mTargeView.get();
        if (targetView != null) {
            targetView.getRootView().getWindowVisibleDisplayFrame(mVisibleViewArea);
            final int heightDiff = targetView.getResources().getDisplayMetrics().heightPixels - mVisibleViewArea.bottom;
            if (mKeyboardHeight != heightDiff && heightDiff > mMinKeyboardHeightDetected) {
                // keyboard is now showing, or the keyboard height has changed
                mKeyboardHeight = heightDiff;
                mRealKeyBoardHeight = mKeyboardHeight;
                onKeyboardStateChanged(targetView);
            } else if (mKeyboardHeight != 0 && heightDiff <= mMinKeyboardHeightDetected) {
                // keyboard is now hidden
                mKeyboardHeight = 0;
                onKeyboardStateChanged(targetView);
            }
        }
    }

    private void onKeyboardStateChanged(View bindView) {
        Iterator<IKeyboardListener> its = mKeyBoardListeners.iterator();
        while (its.hasNext()) {
            its.next().onKeyboardChanged(bindView, mKeyboardHeight > 0, mRealKeyBoardHeight);
        }
    }

    public interface IKeyboardListener {
        void onKeyboardChanged(View bindView, boolean visible, int keyboardHeigh);
    }
}
