package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.game.education.R;

public class CustomPoint extends LinearLayout {

    public CardView card_point;
    View view;
    Activity activity;

    public CustomPoint(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomPoint(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomPoint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_point,this,true);

        card_point = view.findViewById(R.id.card_point);



    }//main





}//class