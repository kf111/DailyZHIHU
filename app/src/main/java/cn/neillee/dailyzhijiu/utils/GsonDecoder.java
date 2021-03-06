package cn.neillee.dailyzhijiu.utils;

import com.google.gson.Gson;
import cn.neillee.dailyzhijiu.model.bean.orignal.OriginalStory;

/**
 * 作者：Neil on 2016/4/16 23:50.
 * 邮箱：cn.neillee@gmail.com
 */
public class GsonDecoder {
    private GsonDecoder() {
    }

    public static GsonDecoder getDecoder() {
        return new GsonDecoder();
    }

    public <T extends OriginalStory> T decoding(String gsonStr, Class<T> classOfT) {
        T t;
        Gson gson = new Gson();
        t = gson.fromJson(gsonStr, classOfT);
        return t;
    }
}
