package com.example.wsj.splashdemo.http;


import android.content.Context;
import android.support.annotation.NonNull;

import com.example.wsj.splashdemo.ColumnApplication;
import com.example.wsj.splashdemo.http.interceptor.CacheInterceptor;
import com.example.wsj.splashdemo.http.interceptor.LoggingInterceptor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.wsj.splashdemo.entity.Constants.API_DEBUG_SERVER_URL;


/**
 * Created by wangshijia on 2017/2/4 下午3:33.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 * <p>
 * eg: 全局 单利网络请求Client 请求方法 调用静态方法getInstance().***(); 其中*** 为ApiStore中定义的请求方法
 */

public class HttpClient {

    private static final int CONNECT_TIME_OUT = 3000;
    private static final int READ_TIME_OUT = 5000;
    private static final int WRITE_TIME_OUT = 5000;

    private static Retrofit retrofit = null;

    private Context mContext;

    public HttpClient(Context context) {
        mContext = context;
    }

    public static ApiStores getInstance() {
        Retrofit retrofit = getClient();
        return retrofit.create(ApiStores.class);
    }


    private static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            // 缓存设置
            setCacheConfig(builder);
            // 公共参数
//            builder.addInterceptor(new AddQueryParameterInterceptor());
            // https设置
            setHttpsConfig(builder);
            //设置超时和重连
            setTimeOutConfig(builder);
            //错误重连
            builder.retryOnConnectionFailure(true);
            //Log信息拦截器，debug模式下打印log
            String BASE_URL = setLogConfig(builder);
            OkHttpClient okHttpClient = builder.build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

    private static void setHttpsConfig(OkHttpClient.Builder builder) {
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
    }

    @NonNull
    private static String setLogConfig(OkHttpClient.Builder builder) {
        String BASE_URL;
        BASE_URL = API_DEBUG_SERVER_URL;
        LoggingInterceptor interceptor = new LoggingInterceptor();
        interceptor.setLevel(LoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        return BASE_URL;
    }

    private static void setCacheConfig(OkHttpClient.Builder builder) {
        File cacheFile = new File(ColumnApplication.getContext().getExternalCacheDir(), "ColumnCache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);
        builder.cache(cache).addInterceptor(new CacheInterceptor());
    }

    private static void setTimeOutConfig(OkHttpClient.Builder builder) {
        builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS);
        builder.writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS);
    }
}
