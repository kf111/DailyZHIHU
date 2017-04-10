package com.neil.dailyzhihu.presenter;

import com.neil.dailyzhihu.base.RxPresenter;
import com.neil.dailyzhihu.listener.OnContentLoadListener;
import com.neil.dailyzhihu.model.bean.orignal.CertainStoryBean;
import com.neil.dailyzhihu.model.bean.orignal.StoryExtraInfoBean;
import com.neil.dailyzhihu.model.http.api.API;
import com.neil.dailyzhihu.presenter.constract.StoryDetailContract;
import com.neil.dailyzhihu.utils.GsonDecoder;
import com.neil.dailyzhihu.utils.load.LoaderFactory;

import javax.inject.Inject;

/**
 * 作者：Neil on 2017/4/6 17:17.
 * 邮箱：cn.neillee@gmail.com
 */

public class StoryDetailPresenter extends RxPresenter<StoryDetailContract.View> implements StoryDetailContract.Presenter {

    @Inject
    public StoryDetailPresenter() {
    }

    @Override
    public void getStoryData(int storyId) {
        LoaderFactory.getContentLoader().loadContent(API.STORY_PREFIX + storyId, new OnContentLoadListener() {
            @Override
            public void onSuccess(String content, String url) {
                mView.showContent(GsonDecoder.getDecoder().decoding(content, CertainStoryBean.class));
            }

            @Override
            public void onFailure(String errMsg) {
                mView.showError(errMsg);
            }
        });
    }

    @Override
    public void getStoryExtras(int storyId) {
        LoaderFactory.getContentLoader().loadContent(API.STORY_EXTRA_PREFIX + storyId, new OnContentLoadListener() {
            @Override
            public void onSuccess(String content, String url) {
                mView.showExtras(GsonDecoder.getDecoder().decoding(content, StoryExtraInfoBean.class));
            }

            @Override
            public void onFailure(String errMsg) {
                mView.showError(errMsg);
            }
        });
    }
}
