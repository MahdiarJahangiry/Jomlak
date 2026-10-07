package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.R;

public class CustomShowAnswer extends LinearLayout {

    public TextView txt_answer;
    public LinearLayout li_up;
    View view;
    Activity activity;

    public CustomShowAnswer(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomShowAnswer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomShowAnswer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_show_answer,this,true);

        txt_answer = view.findViewById(R.id.txt_answer);
        li_up = view.findViewById(R.id.li_up);
        li_up.setVisibility(INVISIBLE);

        new Handler().postDelayed(() -> {
            li_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInUp)
                    .duration(300)
                    .repeat(0)
                    .playOn(li_up);
        }, 200);


    }//main





}//class