package com.example.wsj.splashdemo.http.interceptor;

import android.text.TextUtils;

import com.example.wsj.splashdemo.ColumnApplication;
import com.example.wsj.splashdemo.utils.NetWorkUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wangshijia on 2017/2/3 下午3:55.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */
//该缓存方案是有网的时候从服务器取没网的时候就从本地取，如果并未从服务器取过，那么就
public class CacheInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!NetWorkUtils.isNetWorkAvailable(ColumnApplication.getContext())) {//如果网络不可用
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)//强制从缓存取 对应的强制从网络取为CacheControl.FORCE_NETWORK();
                    .build();
        }

        Response response = chain.proceed(request);

        if (NetWorkUtils.isNetWorkAvailable(ColumnApplication.getContext()) ) {
            int maxAge = 0;
            // 有网络时 设置缓存超时时间0个小时
            // String cacheControl = request.cacheControl().toString();
            // 如果单个请求不同请在请求中写上Cache-control头则按照对应的配置进行本地缓存时间配置
            String cacheControl = request.cacheControl().toString();
            if (TextUtils.isEmpty(cacheControl)) {
                response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .build();
            } else {
                response.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .build();
            }
        } else {
            // 无网络时，设置超时为4周
            int maxStale = 60 * 60 * 24 * 28;
            response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .build();
        }
        return response;
    }
}
