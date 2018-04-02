package com.rexy.widgets.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rexy.widgetlayout.R;


public class RefreshIndicator extends WrapLayout implements NestRefreshLayout.OnRefreshListener {
    ProgressBar mProgressBar;
    ImageView mImageView; //当是刷新时不为null,加载更多时为null .
    TextView mTextView;
    int mLastRotateType = 0;
    boolean isRefreshViewAdded;
    boolean isRefreshPullType;
    String[] mIndicatorTexts;

    private Drawable mProgressDrawable;

    public RefreshIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public RefreshIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RefreshIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Resources res=context.getResources();
        mIndicatorTexts = new String[]{
            /*获取数据中*/res.getString(R.string.refresh_loading),
            /*下拉刷新*/res.getString(R.string.refresh_pull_down),
            /*上拉加载更多*/res.getString(R.string.refresh_pull_up),
            /*松开刷新*/res.getString(R.string.refresh_release),
            /*松开加载更多*/res.getString(R.string.refresh_release),
           /*请放手刷新*/ res.getString(R.string.refresh_release),
           /*请放手加载更多*/res.getString(R.string.refresh_release),
        };
        setGravity(Gravity.CENTER);
        setEachLineMinItemCount(1);
        setEachLineMaxItemCount(2);
        setEachLineCenterHorizontal(true);
        setEachLineCenterVertical(true);
        setMinimumHeight((int) (context.getResources().getDisplayMetrics().density * 60));
    }

    public void setProgressBarDrawable(Drawable drawable) {
        mProgressDrawable = drawable;
        if (mProgressBar != null) {
            mProgressBar.setIndeterminateDrawable(drawable);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        removeRefreshViewInner();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeRefreshViewInner();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isRefreshViewAdded) {
            isRefreshViewAdded = buildRefreshViewInnerIfNeed();
        }
    }

    private void removeRefreshViewInner() {
        removeAllViewsInLayout();
        isRefreshViewAdded = false;
        mProgressBar = null;
        mTextView = null;
        mImageView = null;
    }

    private boolean buildRefreshViewInnerIfNeed() {
        if (!isRefreshViewAdded && getParent() instanceof NestRefreshLayout) {
            NestRefreshLayout parent = (NestRefreshLayout) getParent();
            if (parent.getRefreshPullIndicator() == this) {
                isRefreshPullType = true;
                isRefreshViewAdded = true;
            }
            if (parent.getRefreshPushIndicator() == this) {
                isRefreshPullType = false;
                isRefreshViewAdded = true;
            }
            if (isRefreshViewAdded) {
                removeRefreshViewInner();
                buildRefreshViewInner(isRefreshPullType);
                isRefreshViewAdded = true;
            }
        }
        return isRefreshViewAdded;
    }

    private void buildRefreshViewInner(boolean header) {
        Context context = getContext();
        float density = context.getResources().getDisplayMetrics().density;
        mTextView = new TextView(context);
        mProgressBar = new ProgressBar(context);
        mImageView = new ImageView(context);
        mImageView.setImageResource(R.mipmap.icon_arrow_down);
        mTextView.setTextSize(16);
        if (mProgressDrawable != null) {
            mProgressBar.setIndeterminateDrawable(mProgressDrawable);
        }
        LayoutParams lpLeft1 = new LayoutParams(-2, -2);
        lpLeft1.gravity = Gravity.CENTER;
        lpLeft1.maxHeight = lpLeft1.maxWidth = (int) (32*density+0.5f);
        lpLeft1.rightMargin = (int) (8*density+0.5f);

        LayoutParams lpLeft2 = new LayoutParams(lpLeft1);
        lpLeft2.width = lpLeft2.height = (int) (20*density+0.5f);
        lpLeft2.rightMargin = (int) (10*density+0.5f);

        addView(mImageView, lpLeft1);
        addView(mProgressBar, lpLeft2);
        addView(mTextView);
    }

    private void rotateArrow(View view, boolean reversed, boolean optHeader) {
        int rotateType = reversed ? 1 : -1;
        if (rotateType != mLastRotateType) {
            int from = reversed ? 0 : 180;
            int to = reversed ? 180 : 360;
            RotateAnimation rotate = new RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF,
                    0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(150);
            rotate.setFillAfter(true);
            view.clearAnimation();
            view.startAnimation(rotate);
            mLastRotateType = rotateType;
        }
    }

    @SuppressLint("RestrictedApi")
    private void updateAnimationDrawable(Drawable target, boolean start) {
        if (target != null) {
            Drawable drawable = target;
            while (drawable instanceof DrawableWrapper) {
                drawable = ((DrawableWrapper) drawable).getWrappedDrawable();
            }
            if (drawable instanceof AnimationDrawable) {
                if (start) {
                    ((AnimationDrawable) drawable).start();
                } else {
                    ((AnimationDrawable) drawable).stop();
                }
            }
        }
    }

    @Override
    public void onRefreshStateChanged(NestRefreshLayout parent, int state, int preState, int moveDistance) {
        if (isRefreshViewAdded) {
            if (state != preState && !parent.isRefreshing()) {
                if (state == NestRefreshLayout.OnRefreshListener.STATE_IDLE) {
                    mLastRotateType = 0;
                    if (mImageView != null) {
                        mImageView.clearAnimation();
                    }
                } else {
                    mTextView.setText(mIndicatorTexts[state]);
                }
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);
                    updateAnimationDrawable(mProgressDrawable, false);
                }
                if (mImageView != null) {
                    mImageView.setVisibility(View.VISIBLE);
                    if (preState == NestRefreshLayout.OnRefreshListener.STATE_PULL_READY) {
                        if (state == NestRefreshLayout.OnRefreshListener.STATE_PULL_BEYOND_READY) {
                            rotateArrow(mImageView, true, true);
                        } else if (state == NestRefreshLayout.OnRefreshListener.STATE_PULL_TO_READY) {
                            rotateArrow(mImageView, false, true);
                        }
                    }
                    if (preState == NestRefreshLayout.OnRefreshListener.STATE_PUSH_READY) {
                        if (state == NestRefreshLayout.OnRefreshListener.STATE_PUSH_BEYOND_READY) {
                            rotateArrow(mImageView, true, false);
                        } else if (state == NestRefreshLayout.OnRefreshListener.STATE_PUSH_TO_READY) {
                            rotateArrow(mImageView, false, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRefresh(NestRefreshLayout parent, boolean refresh) {
        if (isRefreshViewAdded) {
            mTextView.setText(mIndicatorTexts[0]);
            if (mImageView != null) {
                mImageView.setVisibility(View.GONE);
                mImageView.clearAnimation();
            }
            if (mProgressBar != null) {
                if (mProgressBar.getVisibility() != View.VISIBLE) {
                    updateAnimationDrawable(mProgressDrawable, true);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
