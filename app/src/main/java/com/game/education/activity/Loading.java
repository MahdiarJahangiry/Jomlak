package com.game.education.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.game.education.R;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Loading extends AppCompatActivity {

    private VideoView videoPlayer;
    private ProgressBar progressBar;
    private ValueAnimator progressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تنظیمات تمام صفحه برای نسخه‌های مختلف
        applyFullScreen();
        setContentView(R.layout.activity_loading);

        initViews();
        setupVideo();
        startLoadingAnimation();
    }

    private void initViews() {
        videoPlayer = findViewById(R.id.video_player);
        progressBar = findViewById(R.id.progressBar);

        // استفاده از ContextCompat برای جلوگیری از Warning منسوخ شدن getDrawable
        progressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.shape_progress_bar));
    }

    private void setupVideo() {
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loading);
        videoPlayer.setVideoURI(videoUri);

        // مدیریت چرخه حیات ویدیو (لوپ کردن در صورت نیاز یا هندل کردن اتمام)
        videoPlayer.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoPlayer.start();
        });
    }

    private void startLoadingAnimation() {
        // بهینه سازی انیمیشن پروگرس بار
        progressAnimator = ValueAnimator.ofInt(0, progressBar.getMax());
        progressAnimator.setDuration(5000);
        progressAnimator.addUpdateListener(animation ->
                progressBar.setProgress((int) animation.getAnimatedValue())
        );

        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFinishing()) {
                    navigateToMain();
                }
            }
        });
        progressAnimator.start();
    }

    private void navigateToMain() {
        Intent intent = new Intent(Loading.this, Main.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void applyFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // متد جدید برای اندروید 11 به بالا
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // متد قدیمی برای اندرویدهای پایین‌تر
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayer != null && !videoPlayer.isPlaying()) {
            videoPlayer.start();
        }
        applyFullScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // جلوگیری از نشت حافظه با متوقف کردن انیمیشن
        if (progressAnimator != null) {
            progressAnimator.removeAllUpdateListeners();
            progressAnimator.cancel();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullScreen();
        }
    }
}
