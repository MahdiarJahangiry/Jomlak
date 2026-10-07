package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.game.education.R;
import com.game.education.activity.Chapters;
import com.game.education.activity.Game;
import com.game.education.dialog.DialogMessage;
import com.game.education.dialog.DialogRegister;

public class CustomLevel extends LinearLayout {

    public TextView txt_level;
    public ImageView img_level,img_pic;
    public LinearLayout card_level;
    public int id,id_level,page;
    public boolean active = false;
    SoundPlayer sound;
    Prefs prefs;
    View view;
    Activity activity;

    public CustomLevel(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomLevel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomLevel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_level,this,true);

        sound = new SoundPlayer(activity);
        prefs = new Prefs(activity);

        //img_level = view.findViewById(R.id.img_level);
        img_pic = view.findViewById(R.id.img_pic);
        card_level = view.findViewById(R.id.card_level);
        txt_level = view.findViewById(R.id.txt_level);
        card_level.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (active) {
                    if (prefs.GetUsername().equals("")) {
                        new DialogMessage(activity, new DialogMessage.DialogListener() {

                            @Override
                            public void onClose() {
                                Intent i = new Intent(activity, Game.class);
                                i.putExtra("id", id);
                                i.putExtra("page", page);
                                i.putExtra("id_level", id_level);
                                i.putExtra("level_category", Integer.parseInt(txt_level.getText().toString()));
                                activity.startActivity(i);
                                activity.finish();
                            }

                            @Override
                            public void onRegister() {
                                new DialogRegister(activity, "Chapters", ((Chapters) activity).googleHelper);
                            }

                        });
                    }else {
                        Intent i = new Intent(activity, Game.class);
                        i.putExtra("id", id);
                        i.putExtra("page", page);
                        i.putExtra("id_level", id_level);
                        i.putExtra("level_category", Integer.parseInt(txt_level.getText().toString()));
                        activity.startActivity(i);
                        activity.finish();
                    }

                }
            }
        });



    }//main





}//class
