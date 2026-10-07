package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.R;
import com.game.education.activity.Game;

public class CustomAnswer extends LinearLayout {

    public TextView txt_answer;
    public String answer;
    public boolean active = false;
    View view;
    Activity activity;

    public CustomAnswer(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomAnswer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomAnswer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_answer,this,true);

        txt_answer = view.findViewById(R.id.txt_answer);




    }//main


    public void SetAnswer()
    {
        txt_answer.setText(answer);
        //txt_answer.setTextColor(getResources().getColor(R.color.black));
        active = true;
        YoYo.with(Techniques.SlideInUp)
                .duration(300)
                .repeat(0)
                .playOn(txt_answer);
    }



}//class