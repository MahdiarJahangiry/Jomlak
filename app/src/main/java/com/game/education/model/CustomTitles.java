package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.game.education.App;
import com.game.education.R;
import com.game.education.activity.Badges;
import com.game.education.activity.Bases;
import com.game.education.activity.Categories;
import com.game.education.activity.Chapters;
import com.game.education.activity.Titles;

public class CustomTitles extends LinearLayout {

    public TextView txt_title;
    public ImageView img_item;
    public RelativeLayout re_Item;
    public int id,IdBack;
    public boolean active = true;
    public boolean badg = false;
    SoundPlayer sound;
    View view;
    Activity activity;

    public CustomTitles(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomTitles(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomTitles(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_titles,this,true);

        sound = new SoundPlayer(activity);
        img_item = view.findViewById(R.id.img_item);
        re_Item = view.findViewById(R.id.re_Item);
        txt_title = view.findViewById(R.id.txt_title);
        re_Item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (badg){
                    App.IdBack = IdBack;
                    Intent i = new Intent(activity, Badges.class);
                    i.putExtra("id", id);
                    i.putExtra("title", txt_title.getText().toString());
                    activity.startActivity(i);
                }else {
                    App.IdBack = IdBack;
                    Intent i = new Intent(activity, Categories.class);
                    i.putExtra("id", id);
                    i.putExtra("title", txt_title.getText().toString());
                    activity.startActivity(i);
                }

            }
        });



    }//main





}//class