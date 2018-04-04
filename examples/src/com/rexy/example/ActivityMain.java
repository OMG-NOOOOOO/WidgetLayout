package com.rexy.example;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rexy.example.extend.BaseActivity;
import com.rexy.example.extend.CardDrawable;
import com.rexy.example.extend.ViewUtils;
import com.rexy.widgetlayout.example.R;
import com.rexy.widgets.adpter.ItemProvider;
import com.rexy.widgets.layout.SlideSelectView;

/**
 * Created by rexy on 17/4/11.
 */
public class ActivityMain extends BaseActivity implements View.OnClickListener {

    SlideSelectView mSlideSelectView;
    TextView mTvIndicator;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_entry);
        mSlideSelectView = ViewUtils.view(this, R.id.slideView);
        mTvIndicator=ViewUtils.view(this,R.id.tvIndicator);
        ViewUtils.view(this, R.id.buttonColumn).setOnClickListener(this);
        ViewUtils.view(this, R.id.buttonPageScroll).setOnClickListener(this);
        ViewUtils.view(this, R.id.buttonWrapLabel).setOnClickListener(this);
        ViewUtils.view(this, R.id.buttonNestFloat).setOnClickListener(this);
        ViewUtils.view(this, R.id.buttonRefresh).setOnClickListener(this);
        ViewUtils.view(this, R.id.buttonHierarchy).setOnClickListener(this);

        Drawable d = CardDrawable.newBuilder(this)
                .top(5).color(0xFF666666, 0xFF0000FF).radiusHalf()
                .bottom(5).color(0xFF666666, 0xFF000000).radiusHalf()
                .left(5).color(0xFF666666, 0xFF00FFFF).radiusHalf()
                .right(5).color(0xFF666666, 0xFFFF00FF).radiusHalf()
                .build();
        ViewUtils.setBackground(ViewUtils.view(this, R.id.imageView), d);
        mSlideSelectView.setItemProvider(new ItemProvider.ViewProvider() {
            @Override
            public int getViewType(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView text = new TextView(parent.getContext());
                text.setBackgroundColor(0xFFeeeeee);
                text.setText(getTitle(position));
                text.setMinWidth(200);
                text.setMinHeight(100);
                text.setGravity(Gravity.CENTER);
                text.setIncludeFontPadding(false);
                return text;
            }

            @Override
            public CharSequence getTitle(int position) {
                return String.valueOf(getItem(position));
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public int getCount() {
                return 20;
            }
        });
        mSlideSelectView.setSlideSelectListener(new SlideSelectView.SlideSelectListener() {
            @Override
            public void onItemSelected(int selectedIndex, int previousIndex,ViewGroup p) {
                mTvIndicator.setText(String.format("itemSelected(current=%d,previous=%d)",selectedIndex,previousIndex));
            }

            @Override
            public void onItemFling(int index, float offsetPercent,ViewGroup p) {
                mTvIndicator.setText(String.format("itemFling(selected=%d,index=%d,offset=%.2f)",mSlideSelectView.getSelectedIndex(),index,offsetPercent));
            }
        });
        mSlideSelectView.setSelectedItem(mSlideSelectView.getItemViewCount()>>1,false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.buttonColumn == id) {
            ActivityCommon.launch(this, FragmentColumnLayout.class);
        }
        if (R.id.buttonPageScroll == id) {
            ActivityCommon.launch(this, FragmentPageScrollContainer.class);
        }
        if (R.id.buttonWrapLabel == id) {
            ActivityCommon.launch(this, FragmentWrapLabelLayout.class);
        }
        if (R.id.buttonNestFloat == id) {
            ActivityCommon.launch(this, FragmentNestFloatLayout.class);
        }
        if (R.id.buttonRefresh == id) {
            ActivityCommon.launch(this, FragmentRefreshLayout.class);
        }
        if (R.id.buttonHierarchy == id) {
            ActivityCommon.launch(this, FragmentHierarchyLayout.class);
        }
    }
}
