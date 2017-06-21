package com.example.wsj.splashdemo.entity;

import com.example.wsj.splashdemo.ColumnApplication;

/**
 * Created by wangshijia on 2017/2/8 下午2:52.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */

public interface Constants {

    String API_DEBUG_SERVER_URL = "http://beta.goldenalpha.com.cn/";

    String EXTRA_KEY_EXIT = "extra_key_exit";

    String DOWNLOAD_SPLASH = "download_splash";
    String EXTRA_DOWNLOAD = "extra_download";

    //动态闪屏序列化地址
    String SPLASH_PATH = ColumnApplication.getContext().getFilesDir().getAbsolutePath() + "/alpha/splash";

    String SPLASH_FILE_NAME = "splash.srr";
}
