package com.game.education.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import com.jgabrielfreitas.core.BlurImageView;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.model.SoundPlayer;

public class DialogMessage {

    public interface DialogListener{
        void onClose();
        void onRegister();
    }

    private DialogListener listener;
    Dialog dialog;

    public DialogMessage(Activity activity, DialogListener listener){

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_version);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.listener = listener;
        SoundPlayer sound = new SoundPlayer(activity);

        RelativeLayout re_up = dialog.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.logo);

        TextView txt_dec = dialog.findViewById(R.id.txt_dec);
        TextView txt_title = dialog.findViewById(R.id.txt_title);
        txt_dec.setText(App.con.getResources().getString(R.string.desc_message));
        txt_title.setText(App.con.getResources().getString(R.string.title_message));

        ImageView img_back = dialog.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);

        ImageView txt_en = dialog.findViewById(R.id.img_close);
        //txt_en.setText(App.con.getResources().getString(R.string.no_update));
        txt_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog,re_up);
                new Handler().postDelayed(() -> {
                    if(listener != null){
                        listener.onClose();
                    }
                }, 400);
            }
        });


        TextView txt_fa = dialog.findViewById(R.id.txt_fa);
        txt_fa.setText(App.con.getResources().getString(R.string.register_main));
        txt_fa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog,re_up);
                if(listener != null){
                    listener.onRegister();
                }
            }
        });

        int w = activity.getResources().getDisplayMetrics().widthPixels;
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

    private void CloseDialog(Dialog dialog, RelativeLayout re_up)
    {
        YoYo.with(Techniques.ZoomOut)
                .duration(500)
                .repeat(0)
                .playOn(re_up);
        new Handler().postDelayed(() -> {
            dialog.dismiss();
        }, 400);
    }

    public void dismiss(){
        dialog.dismiss();
    }

}
