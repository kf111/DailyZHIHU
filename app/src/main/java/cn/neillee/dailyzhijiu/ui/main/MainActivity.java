package cn.neillee.dailyzhijiu.ui.main;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.neillee.dailyzhijiu.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.neillee.dailyzhijiu.Constant;
import cn.neillee.dailyzhijiu.base.BaseSimpleActivity;
import cn.neillee.dailyzhijiu.model.http.api.AtyExtraKeyConstant;
import cn.neillee.dailyzhijiu.ui.about.AboutActivity;
import cn.neillee.dailyzhijiu.ui.adapter.MainPageFragmentPagerAdapter;
import cn.neillee.dailyzhijiu.ui.column.NavColumnsActivity;
import cn.neillee.dailyzhijiu.ui.setting.SettingActivity;
import cn.neillee.dailyzhijiu.ui.star.StoryStaredActivity;
import cn.neillee.dailyzhijiu.ui.topic.NavTopicsActivity;
import cn.neillee.dailyzhijiu.utils.Settings;
import cn.neillee.dailyzhijiu.utils.SnackbarUtil;

/**
 * MainActivity
 */
public class MainActivity extends BaseSimpleActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.vp_news_tab)
    ViewPager mvpNewsTab;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.app_bar_main)
    LinearLayout mContentMain;

    private Settings mSettings = Settings.getInstance();
    private long lastPressTime = 0;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    protected void initViews() {
        ButterKnife.bind(this);
        setupToolbar(mToolbar, R.string.activity_main);

        String tabsTitleArray[] = {getResources().getString(R.string.tab_latest), getResources()
                .getString(R.string.tab_hot), getResources().getString(R.string.tab_past)};
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new LatestFragment());
        fragmentList.add(new HotFragment());
        fragmentList.add(new PastFragment());
        FragmentPagerAdapter fAdapter = new MainPageFragmentPagerAdapter(getSupportFragmentManager()
                , fragmentList, tabsTitleArray);
        // Tabs与FragmentPager关联
        mvpNewsTab.setAdapter(fAdapter);
        mTabs.setupWithViewPager(mvpNewsTab);
        mTabs.getTabAt(0).setIcon(R.drawable.ic_tab_latest_selector);
        mTabs.getTabAt(1).setIcon(R.drawable.ic_tab_hot_selector);
        mTabs.getTabAt(2).setIcon(R.drawable.ic_tab_past_selector);
        // 初始化抽屉的header界面
        initDrawerLayout(mNavView);
    }

    private void initDrawerLayout(NavigationView navigationView) {
        // 抽屉的设置
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavView.setNavigationItemSelectedListener(this);
        mNavView.setCheckedItem(R.id.nav_mainpage);
        mNavView.setItemIconTintList(null);

        // day night mode setting
        mNavView.getMenu().findItem(R.id.nav_night)
                .setIcon(Settings.isNightMode ? R.drawable.ic_nav_day : R.drawable.ic_nav_night)
                .setTitle(Settings.isNightMode ? R.string.nav_day : R.string.nav_night);

        LinearLayout header = (LinearLayout) navigationView.getHeaderView(0);
        ImageView avatar = (ImageView) header.findViewById(R.id.iv_avatar);
        avatar.setOnClickListener(this);
        TextView name = (TextView) header.findViewById(R.id.tv_name);
        name.setOnClickListener(this);
        TextView email = (TextView) header.findViewById(R.id.tv_email);
        email.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.needRecreate) {
            Settings.needRecreate = false;
            this.recreate();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 抽屉顶部的点击事件
            case R.id.iv_avatar:
            case R.id.tv_name:
            case R.id.tv_email:
//                SnackbarUtil.ShortSnackbarWithTheme(this, mDrawerLayout, getResources().getString(R.string.notify_to_do)).show();
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.nav_mainpage:
                break;
            case R.id.nav_topics:
                intent = new Intent(this, NavTopicsActivity.class);
                break;
            case R.id.nav_columns:
                intent = new Intent(this, NavColumnsActivity.class);
                break;
            case R.id.nav_collection:
                intent = new Intent(this, StoryStaredActivity.class);
//                SnackbarUtil.ShortSnackbar(mContentMain, getResources().getString(R.string.to_do), SnackbarUtil.Confirm).show();
                break;
            case R.id.nav_setting:
                intent = new Intent(this, SettingActivity.class);
                break;
            case R.id.nav_night:
                changeNightMode();
                break;
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                break;
        }
        if (intent != null) startActivityForResult(intent, AtyExtraKeyConstant.EXIT_NORMALLY);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (canExit()) {
            super.onBackPressed();
        }
    }

    protected boolean canExit() {
        if (Settings.isExitConfirm) {
            if (System.currentTimeMillis() - lastPressTime > Constant.EXIT_CONFIRM_TIME) {
                lastPressTime = System.currentTimeMillis();
                SnackbarUtil.ShortSnackbarWithTheme(this, getCurrentFocus(), getResources().getString(R.string.notify_exit_confirm)).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, AtyExtraKeyConstant.EXIT_NORMALLY);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AtyExtraKeyConstant.EXIT_NORMALLY) {
            mNavView.setCheckedItem(R.id.nav_mainpage);
        }
    }
}
