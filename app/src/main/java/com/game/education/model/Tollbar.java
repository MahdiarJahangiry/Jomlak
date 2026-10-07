package com.game.education.model;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.game.education.R;
import com.game.education.activity.Main;
import com.game.education.activity.Shop;

import java.util.Random;

public class Tollbar
{

    ImageView img_back,img_close,img_background;
    public TextView txt_title;
    SoundPlayer sound;

    Activity act;
    public Tollbar(Activity activity)
    {
        act = activity;
        sound = new SoundPlayer(act);
        txt_title = act.findViewById(R.id.txt_title);
        img_background = act.findViewById(R.id.img_background);
        img_back = act.findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                act.finish();
            }
        });
        img_close = act.findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(activity, Main.class);
                act.startActivity(i);
                act.finish();
            }
        });

    }



}
