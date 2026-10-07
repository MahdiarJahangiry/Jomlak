package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.game.education.R;
import com.game.education.activity.Shop;

public class CustomShop extends LinearLayout {

    public TextView txt_coin,txt_price;
    public ImageView img_shop,img_coin;
    public int id,coin;
    public String title;
    SoundPlayer sound;
    LinearLayout line_view;
    View view;
    Activity activity;

    public CustomShop(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomShop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomShop(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_shop,this,true);

        sound = new SoundPlayer(activity);
        img_shop = view.findViewById(R.id.img_shop);
        txt_coin = view.findViewById(R.id.txt_coin);
        txt_price = view.findViewById(R.id.txt_price);
        img_coin = view.findViewById(R.id.img_coin);
        line_view = view.findViewById(R.id.line_view);
        line_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                ((Shop) activity).BuyCoin(title,coin);
            }
        });
        txt_price.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



    }//main






}//class