package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.game.education.R;

public class CustomItem extends LinearLayout {

    public Boolean active;
    public float x,y;
    public float Mx,My;
    public String word;
    public TextView btn;
    public RelativeLayout re_view;
    View view;
    Activity activity;

    public CustomItem(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_item,this,true);

        btn = view.findViewById(R.id.btn);
        re_view = view.findViewById(R.id.re_view);

        new Handler().postDelayed(() -> {
            re_view.setX(100f);
            re_view.setY(100f);
        }, 1000);


    }//main





}//class