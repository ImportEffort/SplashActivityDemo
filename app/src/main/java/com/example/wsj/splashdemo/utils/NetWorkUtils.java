package com.example.wsj.splashdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by wangshijia on 2017/2/3 下午4:00.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */

public class NetWorkUtils {

    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        return !(info == null || info.getState() != NetworkInfo.State.CONNECTED);
    }
}
