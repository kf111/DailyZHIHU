package com.neil.dailyzhihu.ui.column;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableGridView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.neil.dailyzhihu.api.API;
import com.neil.dailyzhihu.listener.OnContentLoadingFinishedListener;
import com.neil.dailyzhihu.R;
import com.neil.dailyzhihu.adapter.SectionGridAdapter;
import com.neil.dailyzhihu.bean.orignallayer.SectionList;
import com.neil.dailyzhihu.ui.widget.BaseActivity;
import com.neil.dailyzhihu.api.AtyExtraKeyConstant;
import com.neil.dailyzhihu.utils.GsonDecoder;
import com.neil.dailyzhihu.utils.load.LoaderFactory;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 作者：Neil on 2017/1/19 18:18.
 * 邮箱：cn.neillee@gmail.com
 */

public class NavColumnsActivity extends BaseActivity implements ObservableScrollViewCallbacks, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String LOG_TAG = CertainColumnActivity.class.getSimpleName();

    @Bind(R.id.gv_sections)
    ObservableGridView gvSections;
    @Bind(R.id.tv_header)
    TextView tvHeader;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private List<SectionList.DataBean> mDatas;
    private View.OnClickListener upBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavColumnsActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);
        ButterKnife.bind(this);

        // 将ToolBar设置为ActionBar
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_back);
        mToolbar.setNavigationOnClickListener(upBtnListener);
//        getSupportActionBar().setNavigationMode();
//        View header = LayoutInflater.from(mContext).inflate(R.layout.section_listview_header, null, false);
//        header.setOnClickListener(this);
//        gvSections.addHeaderView(header);
//        gvSections.setScrollViewCallbacks(this);
        gvSections.setOnItemClickListener(this);
        tvHeader.setOnClickListener(this);
//        Utility.setGridViewHeightBasedOnChildren(gvSections);

        LoaderFactory.getContentLoader().loadContent(API.SECTIONS,
                new OnContentLoadingFinishedListener() {
            @Override
            public void onFinish(String content) {
                Logger.json(content);
                SectionList sectionList = GsonDecoder.getDecoder().decoding(content, SectionList.class);
                mDatas = sectionList.getData();
                SectionGridAdapter adapter = new SectionGridAdapter(NavColumnsActivity.this, sectionList);
                gvSections.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = this.getSupportActionBar();
        if (ab == null) {
            return;
        }
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SectionList.DataBean bean = mDatas.get((int) id);
        int sectionId = bean.getStoryId();
        String sectionName = bean.getTitle();
        Intent intent = new Intent(this, CertainColumnActivity.class);
        intent.putExtra(AtyExtraKeyConstant.SECTION_ID, sectionId);
        intent.putExtra(AtyExtraKeyConstant.SECTION_NAME, sectionName);
        startActivity(intent);
    }

    // header被点击
    @Override
    public void onClick(View v) {
//        Intent intent = new Intent(this, SectionSettingActivity.class);
//        this.startActivity(intent);
        Toast.makeText(this, "栏目订阅被点击！", Toast.LENGTH_SHORT).show();
    }
}