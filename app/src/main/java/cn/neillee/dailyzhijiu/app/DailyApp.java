package cn.neillee.dailyzhijiu.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.tencent.bugly.beta.Beta;

import java.util.HashSet;
import java.util.Set;

import cn.neillee.dailyzhijiu.di.component.AppComponent;
import cn.neillee.dailyzhijiu.di.component.DaggerAppComponent;
import cn.neillee.dailyzhijiu.di.module.AppModule;
import cn.neillee.dailyzhijiu.utils.img.UniversalAndroidImageLoader;
import io.fabric.sdk.android.Fabric;

/**
 * 作者：Neil on 2016/4/15 23:52.
 * 邮箱：cn.neillee@gmail.com
 */
public class DailyApp extends Application {

    public static int SCREEN_WIDTH = -1;
    public static int SCREEN_HEIGHT = -1;
    public static float DIMEN_RATE = -1.0F;
    public static int DIMEN_DPI = -1;

    private static DailyApp mInstance;
    public static AppComponent appComponent;
    private Set<Activity> allActivities;

    public static Context AppContext = null;

    public static synchronized DailyApp getInstance() {
        return mInstance;
    }

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;

        //初始化屏幕宽高
        getScreenSize();

        //在子线程中初始化
        InitializeService.start(this);

        AppContext = getApplicationContext();
        UniversalAndroidImageLoader.init(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        Beta.installTinker();
    }

    public void addActivity(Activity activity) {
        if (allActivities == null) {
            allActivities = new HashSet<>();
        }
        allActivities.add(activity);
    }

    public void removeActivity(Activity activity) {
        if (allActivities != null) {
            allActivities.remove(activity);
        }
    }

    public void exitApp() {
        if (allActivities != null) {
            synchronized (allActivities) {
                for (Activity activity : allActivities) {
                    activity.finish();
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void getScreenSize() {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(dm);
        DIMEN_RATE = dm.density / 1.0F;
        DIMEN_DPI = dm.densityDpi;
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;
        if (SCREEN_WIDTH > SCREEN_HEIGHT) {
            int t = SCREEN_HEIGHT;
            SCREEN_HEIGHT = SCREEN_WIDTH;
            SCREEN_WIDTH = t;
        }
    }

    public static AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(mInstance))
                    .build();
        }
        return appComponent;
    }
}
