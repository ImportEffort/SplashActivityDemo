package com.example.wsj.splashdemo.http.interceptor;

import android.text.TextUtils;

import com.example.wsj.splashdemo.UserCenter;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wangshijia on 2017/2/3 下午3:54.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */

public class AddQueryParameterInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request request;
        //String method = originalRequest.method();
       // Headers headers = originalRequest.headers();
        String token = UserCenter.getInstance().getToken();
        String uid = String.valueOf(UserCenter.getInstance().getCurrentUser().uid);
        HttpUrl modifiedUrl = originalRequest.url().newBuilder()
                .addQueryParameter("token", TextUtils.isEmpty(UserCenter.getInstance().getToken()) ? "" : token)
                .addQueryParameter("uid", UserCenter.getInstance().getCurrentUser().uid == 0 ? "" : uid)
                .build();
        request = originalRequest.newBuilder().url(modifiedUrl).build();

        return chain.proceed(request);
    }
}
