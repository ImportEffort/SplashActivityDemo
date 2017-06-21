package com.example.wsj.splashdemo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.wsj.splashdemo.R;
import com.example.wsj.splashdemo.UserCenter;

public class WebActivity extends AppCompatActivity {

    private WebView mWebView;
    private boolean mFromSplash;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = (WebView) findViewById(R.id.web);
        mFromSplash = getIntent().getBooleanExtra("fromSplash", false);
        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        if (title != null && title.length() > 10) {
            title = title.substring(0, 10) + "...";
        }
        setTitle(title);
        mWebView.loadUrl(url);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else if (mFromSplash) {
            gotoLoginOrMainActivity();
        } else {
            super.onBackPressed();
        }
    }

    private void gotoLoginOrMainActivity() {
        if (UserCenter.getInstance().getToken() == null) {
            gotoLoginActivity();
        } else {
            gotoMainActivity();
        }
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
