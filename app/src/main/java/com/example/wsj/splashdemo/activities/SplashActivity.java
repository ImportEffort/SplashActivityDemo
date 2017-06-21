package com.example.wsj.splashdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wsj.splashdemo.R;
import com.example.wsj.splashdemo.UserCenter;
import com.example.wsj.splashdemo.entity.Constants;
import com.example.wsj.splashdemo.entity.Splash;
import com.example.wsj.splashdemo.service.SplashDownLoadService;
import com.example.wsj.splashdemo.utils.SerializableUtils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SplashActivity extends AppCompatActivity {

    private Splash mSplash;
    @BindView(R.id.sp_bg)
    ImageView mSpBgImage;
    @BindView(R.id.sp_jump_btn)
    Button mSpJumpBtn;
    //由于CountDownTimer有一定的延迟，所以这里设置3400
    private CountDownTimer countDownTimer = new CountDownTimer(3400, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mSpJumpBtn.setText("跳过(" + millisUntilFinished / 1000 + "s)");
        }

        @Override
        public void onFinish() {
            mSpJumpBtn.setText("跳过(" + 0 + "s)");
            gotoLoginOrMainActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        showAndDownSplash();
    }


    @OnClick({R.id.sp_bg, R.id.sp_jump_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sp_bg:
                gotoWebActivity();
                break;
            case R.id.sp_jump_btn:
                gotoLoginOrMainActivity();
                break;
        }
    }

    private void gotoWebActivity() {

        if (mSplash != null && mSplash.click_url != null) {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("url", mSplash.click_url);
            intent.putExtra("title", mSplash.title);
            intent.putExtra("fromSplash", true);
            startActivity(intent);
            finish();
        }
    }

    private void showAndDownSplash() {
        showSplash();
        startImageDownLoad();
    }

    private void showSplash() {
        mSplash = getLocalSplash();
        if (mSplash != null && !TextUtils.isEmpty(mSplash.savePath)) {
            Log.d("SplashDemo","SplashActivity 获取本地序列化成功" + mSplash);
            Glide.with(this).load(mSplash.savePath).dontAnimate().into(mSpBgImage);
            startClock();
        } else {
            mSpJumpBtn.setVisibility(View.INVISIBLE);
            mSpJumpBtn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gotoLoginOrMainActivity();
                }
            }, 1000);
        }
    }

    private void startImageDownLoad() {
        SplashDownLoadService.startDownLoadSplashImage(this, Constants.DOWNLOAD_SPLASH);
    }

    private Splash getLocalSplash() {
        Splash splash = null;
        try {
            Log.d("存储路径",Constants.SPLASH_PATH);//修改为存储到内存卡中，不需要动态申请权限
            // /data/user/0/com.example.wsj.splashdemo/files/alpha/splash
            File serializableFile = SerializableUtils.getSerializableFile(Constants.SPLASH_PATH,
                    Constants.SPLASH_FILE_NAME);
            splash = (Splash) SerializableUtils.readObject(serializableFile);
        } catch (IOException e) {
            Log.d("SplashDemo","SplashActivity 获取本地序列化闪屏失败" + e.getMessage());
        }
        return splash;
    }


    private void startClock() {
        mSpJumpBtn.setVisibility(View.VISIBLE);
        countDownTimer.start();
    }

    private void gotoLoginOrMainActivity() {
        countDownTimer.cancel();
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null)
            countDownTimer.cancel();
    }
}
