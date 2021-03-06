package cn.neillee.dailyzhijiu.ui.story;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.gson.Gson;
import cn.neillee.dailyzhijiu.R;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import butterknife.BindView;
import cn.neillee.dailyzhijiu.base.BaseActivity;
import cn.neillee.dailyzhijiu.model.bean.orignal.CertainStoryBean;
import cn.neillee.dailyzhijiu.model.bean.orignal.StoryExtraInfoBean;
import cn.neillee.dailyzhijiu.model.db.StarRecord;
import cn.neillee.dailyzhijiu.model.http.api.AtyExtraKeyConstant;
import cn.neillee.dailyzhijiu.presenter.StoryDetailPresenter;
import cn.neillee.dailyzhijiu.presenter.constract.StoryDetailContract;
import cn.neillee.dailyzhijiu.ui.widget.MenuItemBadge;
import cn.neillee.dailyzhijiu.ui.widget.ObservableWebView;
import cn.neillee.dailyzhijiu.utils.SnackbarUtil;
import cn.neillee.dailyzhijiu.utils.img.ImageLoaderWrapper;
import cn.neillee.dailyzhijiu.utils.load.LoaderFactory;

public class StoryDetailActivity extends BaseActivity<StoryDetailPresenter>
        implements ObservableScrollViewCallbacks, StoryDetailContract.View {
    @BindView(R.id.image)
    ImageView mImageView;
    @BindView(R.id.overlay)
    View mOverlayView;
    @BindView(R.id.scroll)
    ObservableScrollView mScrollView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.main)
    FrameLayout mRootView;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.title)
    TextView mTitleView;
    @BindView(R.id.webview)
    ObservableWebView mWebView;

    private MenuItem mStarMenuItem;
    private MenuItem mPraiseMenuItem;
    private MenuItem mCommentMenuItem;

    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private boolean mFabIsShown;

    private Activity mContext = StoryDetailActivity.this;

    private int mStoryId;
    private boolean mStared;
    private String mStoryExtra;
    private String mStoryTitle;
    private String mDefaultImg;

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final String LOG_TAG = StoryDetailActivity.class.getSimpleName();

    @Override
    protected void initEventAndData() {
        setToolbar(mToolBar, "");
        initObservableViewUIParams();

        if (getIntent().getExtras() == null) return;
        mStoryId = getIntent().getIntExtra(AtyExtraKeyConstant.STORY_ID, 0);
        mDefaultImg = getIntent().getStringExtra(AtyExtraKeyConstant.DEFAULT_IMG_URL);
        mPresenter.queryCachedStory(mStoryId);
    }

    @Override
    protected void initInject() {
        getActivityComponent().inject(this);
    }

    @Override
    protected int getLayout() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        return R.layout.activity_story_detail;
    }

    @Override
    public void showContent(CertainStoryBean storyBean) {
        mPresenter.cacheCachedStory(storyBean);
        mStoryTitle = storyBean.getTitle();
        ImageLoaderWrapper loader = LoaderFactory.getImageLoader();
        mDefaultImg = storyBean.getImage() == null ? mDefaultImg : storyBean.getImage();
        loader.displayImage(mImageView, mDefaultImg, null, null);

        mTitleView.setText(mStoryTitle);
        if (!mFabIsShown) {
            mToolBar.setTitle(mStoryTitle);
        }
//        String cssContent = "";
//        if (storyBean.getCss() != null && storyBean.getCss().size() > 0) {// 构建CSS
////                    cssContent = "<style type=\"text/css\">.content-image{width:100%;height:auto}" + mStoryContent.getCss().get(0) + "</style>";
//            cssContent = "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://shared.ydstatic.com/gouwuex/ext/css/extension_3_1.css?version=0.3.5&amp;buildday=22_02_2017_04_25\">" +
//                    "\n<link type=\"text/css\" rel=\"stylesheet\" href=\"http://news-at.zhihu.com/css/news_qa.auto.css?v=4b3e3\">\n" +
//                    "<link type=\"text/css\" rel=\"stylesheet\" href=\"" + storyBean.getCss().get(0) + "\">\n"
//                    + "<style>.headline{display:none;}</style>";
//        }
//        String html = "<html><head>" + cssContent + "</head><body>" + storyBean.getBody() + " </body></html>";
        String html = getFromAssets(storyBean.getBody());
        mWebView.setHorizontalScrollBarEnabled(false);
        // style="width:100%;height:auto"
        WebSettings webSettings = mWebView.getSettings(); // webView: 类WebView的实例
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);  //就是这句
        mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        Logger.t(LOG_TAG).d(html);
    }

    @Override
    public void showError(String errMsg) {

    }

    @Override
    public void showExtras(StoryExtraInfoBean infoBean) {
        mStoryExtra = new Gson().toJson(infoBean);
        if (mCommentMenuItem != null)
            MenuItemBadge.update(mCommentMenuItem, infoBean.getComments() + "");
        if (mPraiseMenuItem != null)
            MenuItemBadge.update(mPraiseMenuItem, infoBean.getPopularity() + "");
    }

    @Override
    public void showStarRecord(StarRecord record, boolean show) {
        mStared = show;
        if (mStarMenuItem != null) mStarMenuItem.setTitle(show ? "已收藏" : "收藏");
        mFab.setBackgroundTintList(ColorStateList.valueOf(getResources()
                .getColor(show ? R.color.colorAccent : R.color.ZHIHUBlue)));
    }

    // 初始化ObservableView相关参数
    private void initObservableViewUIParams() {
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mActionBarSize = getActionBarSize();

        mScrollView.setScrollViewCallbacks(this);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabIsShown && !TextUtils.isEmpty(mStoryTitle))
                    mPresenter.starStory(mStoryId, mStoryTitle, mDefaultImg);
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mFabIsShown) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                    String text = mStoryTitle + " via " + "http://daily.zhihu.com/story/" + mStoryId + "\n(powered by DailyZHIHU)\n";
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
                }
                return true;
            }
        });
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);
        ScrollUtils.addOnGlobalLayoutListener(mWebView, new Runnable() {
            @Override
            public void run() {
                //正好显现出照片
                onScrollChanged(0, false, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_certain_story_menu, menu);
        mStarMenuItem = menu.findItem(R.id.menu_item_action_star);
        mPraiseMenuItem = menu.findItem(R.id.menu_item_action_praise);
        mCommentMenuItem = menu.findItem(R.id.menu_item_action_comment);
        mCommentMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.performIdentifierAction(mCommentMenuItem.getItemId(), 0);
            }
        });
        mPraiseMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.performIdentifierAction(mPraiseMenuItem.getItemId(), 0);
            }
        });
        mPresenter.getStoryExtras(mStoryId);
        mPresenter.queryStarRecord(mStoryId);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_action_text_size:
                //TODO 设置字体大小
                SnackbarUtil.ShortSnackbarWithTheme(mContext, mRootView, mContext.getResources().getString(R.string.notify_to_do)).show();
                break;
            case R.id.menu_item_action_comment:
                Intent intent = new Intent(mContext, StoryCommentActivity.class);
                intent.putExtra(AtyExtraKeyConstant.STORY_ID, mStoryId);
                intent.putExtra(AtyExtraKeyConstant.STORY_EXTRAS, mStoryExtra);
                startActivity(intent);
                break;
            case R.id.menu_item_action_star:
                mPresenter.starStory(mStoryId, mStoryTitle, mDefaultImg);
                break;
            case R.id.menu_item_action_qrcode:
                // 生成二维码
//                final String shareUrl = API.WEB_STORY_PREFIX + mStoryId;
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_qr_display, null, false);
//                ImageView ivQR = (ImageView) view.findViewById(R.id.iv_qrDisplay);
//                AlertDialog dialog = builder.setView(view).setTitle(getResources().getString(R.string.share_qr_code)).create();
//                dialog.show();
//
//                ViewGroup.LayoutParams pm = ivQR.getLayoutParams();
//                final Bitmap bm = QRCodeUtil.getQRBitmap(shareUrl, pm.width, pm.height, null);
//                ivQR.setImageBitmap(bm);
//
//                ivQR.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        String path = StorageOperatingHelper.savingBitmap2SD(mContext, bm, shareUrl);
//                        if (!TextUtils.isEmpty(path))
//                            SnackbarUtil.ShortSnackbar(mRootView, getResources().getString(R.string.notify_saved) + path, SnackbarUtil.Info).show();
//                        return true;
//                    }
//                });
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(AtyExtraKeyConstant.STORY_ID, mStoryId);
        intent.putExtra(AtyExtraKeyConstant.UNSTARED, !mStared);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        ViewHelper.setPivotX(mTitleView, 0);
        ViewHelper.setPivotY(mTitleView, 0);
        // ViewHelper.setScaleX(mTitleView, scale);
        ViewHelper.setScaleY(mTitleView, scale);

        // Translate title text
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);
        int titleTranslationY = maxTitleTranslationY - scrollY;
        ViewHelper.setTranslationY(mTitleView, titleTranslationY);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mFab.getHeight() / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - mFab.getHeight() / 2,
                mActionBarSize - mFab.getHeight() / 2,
                maxFabTranslationY);
        ViewHelper.setTranslationX(mFab, mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
        ViewHelper.setTranslationY(mFab, fabTranslationY);

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(R.attr.barBgColor, typedValue, true);
            mToolBar.setBackgroundColor(typedValue.data);
        } else {
            showFab();
            mToolBar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = this.getSupportActionBar();
        if (ab == null) return;
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) ab.hide();
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) ab.show();
        }
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
            mFab.setVisibility(View.VISIBLE);
            mToolBar.setTitle("");
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
            mFab.setVisibility(View.GONE);
            mToolBar.setTitle(mStoryTitle);
        }
    }

    // PopupWindow消失时，使屏幕恢复正常
    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    /*
    * 获取html文件
    */
    public String getFromAssets(String content) {
        String htmlPath = "webview/html/certain_story.html";
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(htmlPath));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String Result = "";
            while ((line = bufReader.readLine()) != null) {
                Result += line;
                if (line.contains("<!-- 此处加载内容 -->")) {
                    Result += content;
                    Logger.t(LOG_TAG).d(line);
                }
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}