package com.example.wsj.splashdemo.http;


import com.example.wsj.splashdemo.entity.Common;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by wangshijia on 2017/2/3 下午1:54.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */

public interface ApiStores {
    @GET("fundworks/media/getFlashScreen")
    Observable<Common> getSplashImage(@Query("type") int type);

}

