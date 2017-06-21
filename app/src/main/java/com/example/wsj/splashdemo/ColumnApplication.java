package com.example.wsj.splashdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by wangshijia on 2017/6/21 下午12:25.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */

public class ColumnApplication extends Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        UserCenter.initInstance(this);
    }
}
