package cn.neillee.dailyzhijiu.utils;

import android.content.Context;

import cn.neillee.dailyzhijiu.R;
import cn.neillee.dailyzhijiu.model.bean.orignal.StoryExtraInfoBean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者：Neil on 2016/4/16 20:16.
 * 邮箱：cn.neillee@gmail.com
 */
public class Formater {
    public static String formatStoryExtra(StoryExtraInfoBean extra) {
        String result = "热度：-，评论：-L + -S";
        if (extra != null)
            result = String.format("热度：%d，评论：%dL + %dS", extra.getPopularity(),
                    extra.getLongComments(), extra.getShortComments());
        return result;
    }

    public static String formatUrl(String head, int id) {
        return head + id;
    }

    public static String formatUrl(String head, String id) {
        return head + id;
    }

    public static String formatUrl(String head, int id, String tail) {
        return head + id + tail;
    }

    public static String formatData(String dataFormat, long timeStamp) {
        if (timeStamp == 0) {
            return "";
        }
        timeStamp = timeStamp * 1000;
        SimpleDateFormat format = new SimpleDateFormat(dataFormat);
        return format.format(new Date(timeStamp));
    }

    public static String fromatUpdateVersionInfo(Context context, String versionName, int versionCode) {
        return context.getResources().getString(R.string.updating_version) +": "+ versionName + "(" + versionCode + ")";
    }

    public static String fromatUpdatePgSize(Context context, String size) {
        return context.getResources().getString(R.string.updating_update_pkg_size) + ": " + size;
    }

    public static String fromatOneDayOnPicInfo(Context context, String name) {
        return context.getResources().getString(R.string.splash_one_pic_per_day) + " · " + name;
    }
}
