package com.example.wsj.splashdemo;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wsj.splashdemo.entity.User;

/**
 * Created by wangshijia on 2014/6/18 上午10:12.
 * Copyright (c) 2017. alpha, Inc. All rights reserved.
 */
public class UserCenter {
    private static String TAG = "UserCenter";

    private Context mContext;

    private SharedPreferences preferences;

    // ***单例的用户中心
    private static UserCenter mUserCenter;

    private UserCenter() {
    }

    static UserCenter initInstance(Context context) {
        if (mUserCenter == null) {
            mUserCenter = new UserCenter();
            mUserCenter.mContext = context;
            mUserCenter.preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);

        }
        return mUserCenter;
    }

    public static UserCenter getInstance() {
        return mUserCenter;
    }

    // *****Token相关
    private static String KEY_TOKEN = "tokens";

    private String token;

    public String getToken() {
        if (token == null) {
            token = getLocalToken();
        }
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        saveLocalToken(token);
    }

    private void saveLocalToken(String token) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                TAG, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    private String getLocalToken() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                TAG, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // CurrentUser 相关
    private User mCurrentUser = null;

    public User getCurrentUser() {
        if (mCurrentUser == null) {
            mCurrentUser = getCurrentUserFromLocal();
            if (mCurrentUser == null) {
                throw new IllegalStateException("CurrentUser has not been initialized");
            }

        }
        return mCurrentUser;
    }

    public void setCurrentUser(User user) {
        if (mCurrentUser == null)
            mCurrentUser = new User();
        mCurrentUser.uid = user.uid;//uid
        saveCurrentLocalUser(mCurrentUser);
    }

    private void saveCurrentLocalUser(User user) {
        preferences.edit()
                .putLong("uid", user.uid)
                .apply();
    }

    private User getCurrentUserFromLocal() {
        User user = new User();
        user.uid = preferences.getLong("uid", 0);
        return user;

    }
}
