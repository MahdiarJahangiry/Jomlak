package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.Timer;
import java.util.TimerTask;

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


    void SetBlur()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(() -> {
                    if (img_background.getDrawable() != null) {
                        timer.cancel();
                        img_background.setBlur(15);
                        img_blur.setVisibility(GONE);
                    }
                });
            }
        }, 0, 500);
    }

    public void CanslTimer()
    {
        if (timer != null) {
            timer.cancel();
        }
    }



}//class
