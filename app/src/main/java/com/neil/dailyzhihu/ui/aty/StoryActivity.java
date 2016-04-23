package com.neil.dailyzhihu.ui.aty;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.gson.Gson;
import com.neil.dailyzhihu.Constant;
import com.neil.dailyzhihu.OnContentLoadingFinishedListener;
import com.neil.dailyzhihu.R;
import com.neil.dailyzhihu.adapter.CommentPagerAdapter;
import com.neil.dailyzhihu.adapter.LongCommentListAdapter;
import com.neil.dailyzhihu.bean.LongComment;
import com.neil.dailyzhihu.bean.StoryContent;
import com.neil.dailyzhihu.ui.widget.BaseActivity;
import com.neil.dailyzhihu.ui.widget.CommentAlertDialog;
import com.neil.dailyzhihu.ui.widget.CommentsPopupwindow;
import com.neil.dailyzhihu.ui.widget.ShareMenuPopupWindow;
import com.neil.dailyzhihu.utils.GsonDecoder;
import com.neil.dailyzhihu.utils.LoaderFactory;
import com.neil.dailyzhihu.utils.ShareHelper;
import com.neil.dailyzhihu.utils.StorageOperatingHelper;
import com.neil.dailyzhihu.utils.db.FavoriteStory;
import com.neil.dailyzhihu.utils.db.FavoriteStoryDBdao;
import com.neil.dailyzhihu.utils.db.FavoriteStoryDBdaoFactory;
import com.neil.dailyzhihu.utils.img.ImageLoaderWrapper;
import com.neil.dailyzhihu.utils.share.QQShare;
import com.neil.dailyzhihu.utils.share.QRCodeUtil;
import com.neil.dailyzhihu.utils.share.SinaShare;
import com.neil.dailyzhihu.utils.share.Util;
import com.neil.dailyzhihu.utils.share.WechatShare;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class StoryActivity extends BaseActivity implements ObservableScrollViewCallbacks, AdapterView.OnItemClickListener, PlatformActionListener, View.OnClickListener {

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    @Bind(R.id.image)
    ImageView mImageView;
    @Bind(R.id.overlay)
    View mOverlayView;
    @Bind(R.id.tvContent)
    TextView mTvContent;
    @Bind(R.id.tv_loading_comment)
    TextView mLoadingComment;
    @Bind(R.id.vp_comment)
    ViewPager mVPComment;
    @Bind(R.id.ll_scroll)
    LinearLayout llScroll;
    @Bind(R.id.scroll)
    ObservableScrollView mScrollView;
    @Bind(R.id.title)
    TextView mTitleView;
    @Bind(R.id.fab)
    FloatingActionButton mFab;

    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private boolean mFabIsShown;

    private TextView tvContent;
    private TextView tvImgCopyRight;
//    private TextView tvTitle;

    private TextView tvCommentType;

    private static final String LOG_TAG = StoryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        ButterKnife.bind(this);

        int storyId = getExtras();
        storyId = 8150357;
        if (storyId > 0)
            fillingContent(storyId);

        tvContent = (TextView) findViewById(R.id.tvContent);
//        tvImgCopyRight = (TextView) findViewById(R.id.img_copyright);
//        tvTitle= (TextView) findViewById(R.id.title);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mActionBarSize = getActionBarSize();

        mScrollView.setScrollViewCallbacks(this);
        tvCommentType = (TextView) findViewById(R.id.commentType);
        mLoadingComment.setOnClickListener(this);
        mTitleView.setText(getTitle());
        setTitle(null);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildingCommentPopupWindow();
                Toast.makeText(StoryActivity.this, "FAB is clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);

        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                //挡住照片
                // mScrollView.scrollTo(0, mFlexibleSpaceImageHeight - mActionBarSize);
                //正好显现出照片
                onScrollChanged(0, false, false);
                // If you'd like to start from scrollY == 0, don't write like this:
                //mScrollView.scrollTo(0, 0);
                // The initial scrollY is 0, so it won't invoke onScrollChanged().
                // To do this, use the following:
                //onScrollChanged(0, false, false);

                // You can also achieve it with the following codes.
                // This causes scroll change from 1 to 0.
                //mScrollView.scrollTo(0, 1);
                //mScrollView.scrollTo(0, 0);
            }
        });
    }


    private PopupWindow pw = null;

    //构造并显示mFab点击后弹出的popupwindow
    private void buildingCommentPopupWindow() {
        if (pw != null)
            pw.showAsDropDown(mFab);
        else {
            pw = new CommentsPopupwindow(this, new String[]{"查看评论", "收藏", "分享", "二维码"}, this);
            pw.showAsDropDown(mFab);
        }
    }

    private int getExtras() {
        int storyId = -1;
        if (getIntent().getExtras() != null) {
            storyId = getIntent().getIntExtra(Constant.STORY_ID, 0);
        }
        return storyId;
    }

    private int storyId = -1;
    private String storyTtitle;
    private StoryContent story;

    private void fillingContent(final int storyId) {
        this.storyId = storyId;

        LoaderFactory.getContentLoader().loadContent(Constant.STORY_HEAD + storyId, new OnContentLoadingFinishedListener() {
            @Override
            public void onFinish(String content) {
                Gson gson = new Gson();
                story = gson.fromJson(content, StoryContent.class);
                String body = story.getBody();
                //TODO 在较为特殊的情况下，知乎日报可能将某个主题日报的站外文章推送至知乎日报首页。
                //type=0正常，type特殊情况
                //TODO 图片加载
                ImageLoaderWrapper loader = LoaderFactory.getImageLoader();
                loader.displayImage(mImageView, story.getImage(), null);
                storyTtitle = story.getTitle();
                mTitleView.setText(storyTtitle);
//                tvImgCopyRight.setText(story.getImage_source());
                if (body == null)
                    return;
                tvContent.setText(Html.fromHtml(body));
                tvContent.setVisibility(View.VISIBLE);
            }
        });
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
        ViewHelper.setScaleX(mTitleView, scale);
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // On pre-honeycomb, ViewHelper.setTranslationX/Y does not set margin,
            // which causes FAB's OnClickListener not working.
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFab.getLayoutParams();
            lp.leftMargin = mOverlayView.getWidth() - mFabMargin - mFab.getWidth();
            lp.topMargin = (int) fabTranslationY;
            mFab.requestLayout();
        } else {
            ViewHelper.setTranslationX(mFab, mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
            ViewHelper.setTranslationY(mFab, fabTranslationY);
        }

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0://查看评论
                Toast.makeText(StoryActivity.this, "查看评论", Toast.LENGTH_SHORT).show();
                viewComment();
                break;
            case 1://收藏
                Toast.makeText(StoryActivity.this, "收藏", Toast.LENGTH_SHORT).show();
                starStory();
                break;
            case 2://分享
                Toast.makeText(StoryActivity.this, "分享", Toast.LENGTH_SHORT).show();
                showShareModule();
                break;
            case 3://二维码
                Toast.makeText(StoryActivity.this, "生成二维码", Toast.LENGTH_SHORT).show();
                makingQRCode();
                break;
        }
    }

    private void starStory() {
        if (story != null) {
            FavoriteStoryDBdaoFactory.convertStoryContent2DBStory(story);
            FavoriteStory favoriteStory = FavoriteStoryDBdaoFactory.convertStoryContent2DBStory(story);
            FavoriteStoryDBdao dao = FavoriteStoryDBdaoFactory.getInstance(this);
            if (dao.addStory(favoriteStory) != -1)
                Toast.makeText(StoryActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewComment() {
        if (story == null)
            return;
        //加载页卡
        List<View> views = loadingViewPagerCard();
        CommentAlertDialog commentAlertDialog = new CommentAlertDialog(this, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        }, new CommentPagerAdapter(views), null);
        ViewPager vp = commentAlertDialog.getVp();
        loadingComment(views, vp);
    }

    private void makingQRCode() {
        final String shareUrl = story.getShare_url();
        ImageView ivQR = buildingQRDisplayDilalog();
        ViewGroup.LayoutParams pm = ivQR.getLayoutParams();
        final Bitmap bm = QRCodeUtil.getQRBitmap(shareUrl, pm.width, pm.height, null);
        ivQR.setImageBitmap(bm);
        ivQR.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String path = StorageOperatingHelper.savingBitmap2SD(StoryActivity.this, bm, shareUrl);
                if (!TextUtils.isEmpty(path))
                    Toast.makeText(StoryActivity.this, "保存成功" + path, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private ImageView buildingQRDisplayDilalog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this);
        View view = LayoutInflater.from(StoryActivity.this).
                inflate(R.layout.dialog_qr_display, null, false);
        final ImageView ivQR = (ImageView) view.findViewById(R.id.iv_qrDisplay);
        AlertDialog dialog = builder.setView(view).setTitle("二维码分享").create();
        dialog.show();
        Log.e(LOG_TAG, ivQR.getWidth() + "widthPx," + ivQR.getHeight() + "heightPx");
        return ivQR;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String title = story.getTitle();
            String shareUrl = story.getShare_url();
            String img = story.getImage();
            String text = title + "\tmore? via zhihuDaily--->\t" + shareUrl;
            switch (position) {
                case 0://生成图片
                    Intent intent = new Intent(StoryActivity.this, ImageStoryActivity.class);
                    intent.putExtra(Constant.STORY_BODY, story.getBody());
                    startActivity(intent);
                    break;
                case 1://微信好友
                    WechatShare.wechatShareText(StoryActivity.this, StoryActivity.this, title, text, Util.WECHAT_FRIEND);
                    break;
                case 2://票圈
                    WechatShare.wechatShareText(StoryActivity.this, StoryActivity.this, title, text, Util.WECHAT_MOMENTS);
                    break;
                case 3://空间
                    QQShare.qqShareLink(StoryActivity.this, StoryActivity.this, title, shareUrl, text, "", img, Util.QZONE_NAME);
                    break;
                case 4://QQ
                    QQShare.qqShareLink(StoryActivity.this, StoryActivity.this, title, shareUrl, text, "", img, Util.QQ_NAME);
                    break;
                case 5://新浪微博
                    SinaShare.sinaShareLink(StoryActivity.this, StoryActivity.this, text, "", img, shareUrl);
                    break;
                case 6://复制链接
                    ShareHelper.saveToClipboard(shareUrl, StoryActivity.this);
                    Toast.makeText(StoryActivity.this, "成功复制到剪贴板", Toast.LENGTH_SHORT).show();
                    break;
                case 7://更多
                    String storyText = makeShareText();
                    if (storyText == null)
                        return;
                    Toast.makeText(StoryActivity.this, "更多分享", Toast.LENGTH_SHORT).show();
                    ShareHelper.orignalMsgShare(StoryActivity.this, "StoryActivity", storyText, storyText, null);
                    break;
            }
        }

        private String makeShareText() {
            if (storyId > 0)
                return storyTtitle + Constant.STORY_HEAD + storyId + "\nvia DailyZHIHU";
            return null;
        }
    };

    private void showShareModule() {
        ShareMenuPopupWindow popupWindow = new ShareMenuPopupWindow(this, mOnItemClickListener);
        popupWindow.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    public void share(StoryContent story) {
        if (story == null)
            return;
        ShareHelper.onKeyShareText(StoryActivity.this, story.getTitle(), story.getTitle() + "via zhihuDaily" + story.getShare_url(), story.getImage());
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        Toast.makeText(StoryActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {

    }

    @Override
    public void onCancel(Platform platform, int i) {

    }

    private boolean hasCommentLoaded = false;

    @Override
    public void onClick(View v) {
        if (!hasCommentLoaded) {
            Toast.makeText(this, "正在加载评论", Toast.LENGTH_SHORT).show();
            if (story == null)
                return;
            //加载页卡
            List<View> views = loadingViewPagerCard();
//            loadingComment(views);
            hasCommentLoaded = true;
            mLoadingComment.setText("点击回到顶部");
        } else {
            //// TODO: 2016/4/21 实现回到顶部
            mScrollView.scrollVerticallyTo(0);
        }
    }

    private void loadingComment(final List<View> views, final ViewPager vp) {
        final String tail = "/long-comments";
        final String tailShort = "/short-comments";

        LoaderFactory.getContentLoader().loadContent(Constant.COMMENT_HEAD + storyId + tail, new OnContentLoadingFinishedListener() {
            @Override
            public void onFinish(String content) {
                LongComment longComment = (LongComment) GsonDecoder.getDecoder().decoding(content, LongComment.class);
                List<LongComment.CommentsBean> mDatas = longComment.getComments();
                if (views.get(0) == null) {
                    return;
                }
                View view = views.get(0);
                ListView lv = (ListView) view.findViewById(R.id.lv_comment);
                lv.setAdapter(new LongCommentListAdapter(StoryActivity.this, mDatas));
                LoaderFactory.getContentLoader().loadContent(Constant.COMMENT_HEAD + storyId + tailShort, new OnContentLoadingFinishedListener() {
                    @Override
                    public void onFinish(String content) {
                        LongComment longComment = (LongComment) GsonDecoder.getDecoder().decoding(content, LongComment.class);
                        List<LongComment.CommentsBean> mDatas = longComment.getComments();
                        if (views.get(1) == null) {
                            return;
                        }
                        View view = views.get(1);
                        ListView lv = (ListView) view.findViewById(R.id.lv_comment);
                        if (lv == null) {
                            return;
                        }
                        lv.setAdapter(new LongCommentListAdapter(StoryActivity.this, mDatas));
                        vp.setAdapter(new CommentPagerAdapter(views));
                    }
                });
            }
        });
    }

    //加载页卡
    public List<View> loadingViewPagerCard() {
        List<View> views = new ArrayList<>();
        View view = getLayoutInflater().inflate(R.layout.item_viewpager, null);
        views.add(view);
        view = getLayoutInflater().inflate(R.layout.item_viewpager, null);
        views.add(view);
        return views;
    }
}