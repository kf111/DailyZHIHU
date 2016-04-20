package com.neil.dailyzhihu.bean.story;

import com.neil.dailyzhihu.bean.UniversalStoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neil on 2016/3/23.
 */
public class HotStory {
    /**
     * news_id : 8029459
     * url : http://news-at.zhihu.com/api/2/news/8029459
     * thumbnail : http://pic3.zhimg.com/3e0a30c21c318f0d4142d4631dde4f96.jpg
     * title : 供电公司会为了多收电费而故意调高电压吗？
     */
    private List<RecentBean> recent;

    public List<RecentBean> getRecent() {
        return recent;
    }

    public class RecentBean implements UniversalStoryBean {
        private int news_id;
        /**
         * URL还失效了
         */
        private String url;
        private String thumbnail;
        private String title;

        @Override
        public int getStoryId() {
            return news_id;
        }

        @Override
        public List<String> getImages() {
            List<String> images = new ArrayList<>();
            images.add(thumbnail);
            return images;
        }

        @Override
        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getThumbnail() {
            return thumbnail;
        }
    }
}