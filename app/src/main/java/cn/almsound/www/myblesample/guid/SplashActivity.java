package cn.almsound.www.myblesample.guid;

import android.content.Intent;

import com.jackiepenghe.baselibrary.BaseSplashActivity;

/**
 *
 * @author jacke
 * @date 2018/1/18 0018
 */

public class SplashActivity extends BaseSplashActivity {
    @Override
    protected void onCreate() {
        Intent intent = new Intent(this,WelcomeActivity.class);
        startActivity(intent);
        onBackPressed();
    }
}
