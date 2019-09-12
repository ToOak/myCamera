package com.ppk.mycamera;

import android.app.Application;

import com.ppk.mycamera.utils.CrashHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.ppk.mycamera.utils.LogUtil.LOG_MODE;

public class MyApplication extends Application {
    private static MyApplication app;
    public static long startTime = -1L;
//    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINA);
    public static DateFormat dateFormat = new SimpleDateFormat("HH_mm_ss_SSS", Locale.CHINESE);

    public static MyApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        init();
    }

    private void init() {
        LOG_MODE = true;
        CrashHandler.getInstance().init(getApplicationContext());
    }
}
