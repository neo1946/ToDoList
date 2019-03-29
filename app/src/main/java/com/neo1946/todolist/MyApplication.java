package com.neo1946.todolist;

import android.app.Application;
import android.provider.Settings;

import com.avos.avoscloud.AVOSCloud;
import com.neo1946.todolist.util.SDCardUtil;

public class MyApplication extends Application {

    public static MyApplication application;

    public static final boolean isTest = true;
    public static final String SP_NAME = "todolist";
    @Override
    public void onCreate() {
        super.onCreate();
        initLeanCloudSdk();
        initSDUtil();
        application = this;
    }

    private void initSDUtil() {
        SDCardUtil.android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private void initLeanCloudSdk() {
        AVOSCloud.initialize(this,"KYxF2dkVY4hOb40zrUtQeOqj-gzGzoHsz","hp9OdJLOGCgNPtJEa80KiPBe");
        AVOSCloud.setDebugLogEnabled(isTest);
    }

}
