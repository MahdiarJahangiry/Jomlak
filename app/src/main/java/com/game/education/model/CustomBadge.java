package com.game.education.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;

public class CustomBadge extends LinearLayout {

    public TextView txt_badge;
    public ImageView img_badge;
    public RelativeLayout re_Item;
    public CardView card_badge;
    public int id;
    public boolean active = false;
    View view;
    Activity activity;
    SoundPlayer sound;

    public CustomBadge(Activity context) {
        super(context);
        this.activity = context;
        init(context);
    }
    public CustomBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_badge,this,true);

        sound = new SoundPlayer(activity);

        card_badge = view.findViewById(R.id.card_badge);
        img_badge = view.findViewById(R.id.img_badge);
        re_Item = view.findViewById(R.id.re_Item);
        txt_badge = view.findViewById(R.id.txt_badge);
        re_Item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active) {
                    sound.PlayClick();
                    DialoShare();
                }
            }
        });



    }//main


    public  void DialoShare()
    {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_badge);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BitmapDrawable drawable = (BitmapDrawable) img_badge.getDrawable();
        Bitmap bitmap = drawable.getBitmap();


        ImageView img_close = dialog.findViewById(R.id.img_close);
        img_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ImageView img_share = dialog.findViewById(R.id.img_share);
        img_share.setImageBitmap(bitmap);

        TextView txt_share = dialog.findViewById(R.id.txt_share);
        txt_share.setText(txt_badge.getText().toString());
        TextView btn_share = dialog.findViewById(R.id.btn_share);
        btn_share.setText(App.con.getResources().getString(R.string.share));
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                BitmapDrawable drawable = (BitmapDrawable) img_share.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                String bit_path = MediaStore.Images.Media.insertImage(activity.getContentResolver(),bitmap,"title",null);
                Uri uri = Uri.parse(bit_path);
                String shareBody = "\n\n" + txt_badge.getText().toString();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/png");
                activity.startActivity(Intent.createChooser(shareIntent, App.con.getResources().getString(R.string.share_title)));
            }
        });



        int w = getResources().getDisplayMetrics().widthPixels;
        double w2 = (90 * w) / 100;
        w = (int) w2;
        dialog.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 100);
    }



}//class
