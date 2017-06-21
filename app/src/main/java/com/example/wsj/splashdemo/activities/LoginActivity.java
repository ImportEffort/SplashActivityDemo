package com.example.wsj.splashdemo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.wsj.splashdemo.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("登录界面");
    }
}
