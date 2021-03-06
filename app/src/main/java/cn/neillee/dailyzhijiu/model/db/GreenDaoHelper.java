package cn.neillee.dailyzhijiu.model.db;

import android.content.Context;

import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.database.Database;

import java.util.List;

import cn.neillee.dailyzhijiu.model.bean.orignal.CertainStoryBean;

/**
 * 作者：Neil on 2017/5/30 17:54.
 * 邮箱：cn.neillee@gmail.com
 */

public class GreenDaoHelper {
    private static final String DB_NAME = "DailyApp.db";

    private DaoSession mDaoSession;

    public GreenDaoHelper(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        Database db = helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
    }

    public StarRecord queryStarRecord(int storyId) {
        StarRecordDao recordDao = mDaoSession.getStarRecordDao();
        return recordDao.queryBuilder().where(StarRecordDao
                .Properties.StoryId.eq(storyId)).build().unique();
    }

    public void insertStarRecord(StarRecord record) {
        if (queryStarRecord(record.getStoryId()) != null) {
            Logger.e("Error in insertStarRecord due to EXIST storyId:{}", record.getStoryId());
        } else {
            record.set_id(null);
            mDaoSession.getStarRecordDao().insert(record);
        }
    }

    public void deleteStarRecord(StarRecord record) {
        mDaoSession.getStarRecordDao().delete(record);
    }

    public List<StarRecord> queryAllStarRecord() {
        return mDaoSession.getStarRecordDao().loadAll();
    }

    public void deleteCachedStory(int storyId) {
        CachedStory cachedStory = queryCachedStory(storyId);
        if (cachedStory != null) {
            mDaoSession.getCachedStoryDao().deleteInTx(cachedStory);
        }
    }

    public CachedStory queryCachedStory(int storyId) {
        return mDaoSession.getCachedStoryDao().queryBuilder()
                .where(CachedStoryDao.Properties.StoryId.eq(storyId))
                .build().unique();
    }

    public void cacheCachedStory(CertainStoryBean story) {
        CachedStory cachedStory = queryCachedStory(story.getId());
        if (cachedStory == null || cachedStory.getStoryId() == 0) {
            mDaoSession.getCachedStoryDao().insert(
                    new CachedStory(story.getId(),
                            story.getTitle(), story.getBody(),
                            story.getImage(), story.getImageSource()));
        } else {// todo update
            return;
        }
    }
}
