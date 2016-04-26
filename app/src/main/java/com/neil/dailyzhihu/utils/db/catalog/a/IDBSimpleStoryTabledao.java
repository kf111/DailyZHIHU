package com.neil.dailyzhihu.utils.db.catalog.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.neil.dailyzhihu.bean.cleanlayer.SimpleStory;
import com.neil.dailyzhihu.utils.db.catalog.HottestCatalogDB;

import java.util.ArrayList;
import java.util.List;

public class IDBSimpleStoryTabledao implements IDBSimpleStoryTable {
    private static final String LOG_TAG = IDBSimpleStoryTabledao.class.getSimpleName();
    private SQLiteDatabase writable;
    private SQLiteDatabase readable;
    private String tableName;


    public IDBSimpleStoryTabledao(Context context) {
        MyDBHelper openHelper = new MyDBHelper(context);
        readable = openHelper.getReadableDatabase();
        writable = openHelper.getWritableDatabase();
        tableName = MyDBHelper.ConstantDB.SIMPLE_STORY_TABLE_NAME;
    }

    @Override
    public long addSimpleStory(SimpleStory story) {
        int storyId = story.getStoryId();
        if (querySimpleStoryById(storyId) != null) {
            Log.e(LOG_TAG, "insert error:this story exists");
            return 0;
        }
        String title = story.getTitle();
        int type = story.getType();
        String gaPrefix = story.getGaPrefix();
        String imageUrl = story.getImageUrl();
        String imagePath = story.getImagePath();
        String date = story.getDate();
        String downloadedTimestamp = story.getDownloadTimeStamp();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_ID, storyId);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_TITLE, title);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_TYPE, type);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_GA_PREFIX, gaPrefix);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_IMAGE_URL, imageUrl);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_IMAGE_PATH, imagePath);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_DOWNLOADED_TIME_STAMP, downloadedTimestamp);
        contentValues.put(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_DATE, date);
        int res = (int) writable.insert(tableName, null, contentValues);
        if (res >= 0)
            Log.e(LOG_TAG, "----" + title + type + gaPrefix + imageUrl + imagePath + date + downloadedTimestamp);
        return res;
    }

    @Override
    public int dropSimpleStory(int storyId) {
        return writable.delete(tableName, MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_ID + "=?", new String[]{storyId + ""});
    }

    @Override
    public int updateSimpleStory(int storyId, ContentValues contentValues) {
        if (querySimpleStoryById(Integer.valueOf(storyId)) == null) {//表内部存在
            Log.e(LOG_TAG, "update error:this story doesnot exists");
            return -1;
        }
        return writable.update(tableName, contentValues, MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_ID + "=?", new String[]{storyId + ""});
    }

    @Override
    public SimpleStory querySimpleStoryById(int storyId) {
        Cursor cursor = readable.query(tableName, null, MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_ID + "=?", new String[]{storyId + ""}, null, null, null);
        SimpleStory story = null;
        if (cursor.moveToFirst()) {
            story = cursor2SimpleStory(cursor);
        }
        cursor.close();
        return story;
    }

    @Override
    public List<SimpleStory> queryStoryCatalogByDownloadedDate(String storyDownloadedDate) {
        List<SimpleStory> simpleStoryList = null;
        Cursor cursor = readable.query(HottestCatalogDB.STORY_TABLE_NAME, null, HottestCatalogDB.KEY_STORY_DOWNLOADED_DATE + "=?", new String[]{storyDownloadedDate}, null, null, null);
        if (cursor.moveToFirst()) {
            simpleStoryList = new ArrayList<>();
            Log.e(LOG_TAG, "cursor:" + cursor.getCount());
            do {
                SimpleStory story = cursor2SimpleStory(cursor);
                if (story != null)
                    simpleStoryList.add(story);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return simpleStoryList;
    }

    @Override
    public List<SimpleStory> queryAllSimpleStory() {
        List<SimpleStory> simpleStoryList = null;
        Cursor cursor = readable.query(tableName, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            simpleStoryList = new ArrayList<>();
            Log.e(LOG_TAG, "cursor:" + cursor.getCount());
            do {
                SimpleStory story = cursor2SimpleStory(cursor);
                if (story != null)
                    simpleStoryList.add(story);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return simpleStoryList;
    }

    private SimpleStory cursor2SimpleStory(Cursor cursor) {
        String storyId = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_ID));
        String title = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_TITLE));
        String imageUrl = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_IMAGE_URL));
        String imagePath = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_IMAGE_PATH));
        String downloadedTimestamp = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_DOWNLOADED_TIME_STAMP));
        String type = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_TYPE));
        String gaPrefix = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_GA_PREFIX));
        String date = cursor.getString(cursor.getColumnIndex(MyDBHelper.ConstantDB.KEY_SIMPLE_STORY_DATE));
        SimpleStory simpleStory = new SimpleStory(Integer.valueOf(storyId), gaPrefix, title, Integer.valueOf(type), imageUrl, imagePath, date, downloadedTimestamp);
        return simpleStory;
    }
}
