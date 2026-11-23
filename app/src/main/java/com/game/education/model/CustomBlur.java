package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.util.Timer;
import java.util.TimerTask;
import com.game.education.R;
import com.jgabrielfreitas.core.BlurImageView;


public class CustomBlur extends LinearLayout {

    public BlurImageView img_background;
    public boolean active = false;
    ImageView img_blur;
    View view;
    Activity activity;
    Timer timer;

    public CustomBlur(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomBlur(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomBlur(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_blur,this,true);

        img_background = view.findViewById(R.id.img_background);
        img_blur = view.findViewById(R.id.img_blur);


        new Handler().postDelayed(() -> {
            if (active) {
                SetBlur();
            }
        }, 200);


    }//main


    private final android.os.Handler blurHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable blurRunnable;

    void SetBlur() {
        // پاک کردن تداخل‌های قبلی
        CanslTimer();

        blurRunnable = new Runnable() {
            @Override
            public void run() {
                if (activity == null || activity.isFinishing()) {
                    return;
                }

                if (img_background != null && img_background.getDrawable() != null) {
                    android.graphics.drawable.Drawable drawable = img_background.getDrawable();

                    // بررسی نوع تصویر برای جلوگیری از خطای ClassCastException مربوط به GIF
                    if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                        try {
                            img_background.setBlur(15);
                            img_blur.setVisibility(GONE);
                        } catch (Exception e) {
                            img_blur.setVisibility(GONE);
                        }
                    } else {
                        // اگر GIF یا غیر Bitmap باشد، فقط لایه مات‌کننده را برمی‌داریم تا کرش نکند
                        img_blur.setVisibility(GONE);
                    }
                    return; // کار تمام شد، نیازی به تکرار نیست
                }

                // اگر تصویر هنوز لود نشده بود، بعد از ۵۰۰ میلی‌ثانیه دوباره چک کن (جایگزین امن schedule)
                blurHandler.postDelayed(this, 500);
            }
        };

        // شروع اولین بررسی
        blurHandler.post(blurRunnable);
    }

    public void CanslTimer() {
        if (blurHandler != null && blurRunnable != null) {
            blurHandler.removeCallbacks(blurRunnable);
        }
    }


}//class
