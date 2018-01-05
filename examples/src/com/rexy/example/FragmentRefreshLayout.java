package com.rexy.example;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rexy.example.extend.BaseFragment;
import com.rexy.widgetlayout.example.R;
import com.rexy.widgets.layout.NestRefreshLayout;
import com.rexy.widgets.layout.RefreshIndicator;
import com.rexy.widgets.layout.ScrollLayout;
import com.rexy.example.extend.ViewUtils;

/**
 * TODO:功能说明
 *
 * @author: rexy
 * @date: 2017-06-05 15:03
 */
public class FragmentRefreshLayout extends BaseFragment implements NestRefreshLayout.OnRefreshListener {
    NestRefreshLayout mRefreshLayout;
    ScrollLayout mScrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRefreshLayout = (NestRefreshLayout) inflater.inflate(R.layout.fragment_refreshlayout, container, false);
        mScrollView = ViewUtils.view(mRefreshLayout, R.id.scrollView);
        initScrollView(mScrollView, true);
        initRefreshLayout(mRefreshLayout);
        mRefreshLayout.setScrollChild(mScrollView);//自定义 View 需要。ScrollView ,AbsListView RecyclerView 不需要。
        return mRefreshLayout;
    }

    private void initRefreshLayout(NestRefreshLayout refreshLayout) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshPullIndicator(new RefreshIndicator(inflater.getContext()));
        refreshLayout.setRefreshPushIndicator(new RefreshIndicator(inflater.getContext()));
        refreshLayout.setRefreshPushEnable(true);
        refreshLayout.setRefreshPullEnable(true);
    }

    private void initScrollView(ScrollLayout scrollView, boolean init) {
        LayoutInflater.from(getActivity()).inflate(R.layout.pagescrollview_scrollview_child, scrollView, true);
        if (init) {
            scrollView.setGravity(Gravity.CENTER);
            scrollView.setBackgroundColor(0xAA000000);
            scrollView.setVerticalScrollBarEnabled(true);
            scrollView.setScrollBarStyle(mScrollView.SCROLLBARS_INSIDE_OVERLAY);
        }
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScrollView.scrollToItem(mScrollView.indexOfChild(v), -1, true);
            }
        };
        int n = scrollView.getChildCount();
        for (int i = 0; i < n; i++) {
            ScrollLayout.LayoutParams lp = (ScrollLayout.LayoutParams) scrollView.getChildAt(i).getLayoutParams();
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            lp.topMargin = lp.bottomMargin = 30;
            lp.leftMargin = lp.rightMargin = 30;
            scrollView.getChildAt(i).setOnClickListener(clickListener);
        }
    }

    @Override
    public void onRefresh(final NestRefreshLayout parent, final boolean refresh) {
        Toast.makeText(getActivity(), refresh ? "pull refresh" : "push load more", Toast.LENGTH_SHORT).show();
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refresh) {
                    mScrollView.removeAllViews();
                    initScrollView(mScrollView, false);
                } else {
                    TextView tv = new TextView(getActivity());
                    tv.setPadding(20, 20, 20, 20);
                    tv.setText("load more:" + System.currentTimeMillis());
                    mScrollView.addView(tv);
                    mScrollView.scrollToItem(mScrollView.getChildCount()-1,-1,false);
                }
                parent.setRefreshComplete();
            }
        }, 1200);
    }

    @Override
    public void onRefreshStateChanged(NestRefreshLayout parent, int state, int preState, int moveAbsDistance) {
    }
}
