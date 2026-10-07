package com.game.education.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.jgabrielfreitas.core.BlurImageView;
import java.io.IOException;
import java.io.InputStream;

public class DialogPravate {

    Dialog dialog_private;

    public DialogPravate(Activity activity){

        dialog_private = new Dialog(activity);
        dialog_private.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_private.setContentView(R.layout.dialog_private);
        dialog_private.setCancelable(true);
        dialog_private.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        SoundPlayer sound = new SoundPlayer(activity);

        RelativeLayout re_up = dialog_private.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);
        TextView txt_private = dialog_private.findViewById(R.id.txt_private);
        ImageView img_close = dialog_private.findViewById(R.id.img_close);
        ImageView img_back = dialog_private.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        BlurImageView img_background = dialog_private.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_private.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.profileseyf);
        TextView txt_title = dialog_private.findViewById(R.id.txt_title);
        txt_title.setText(App.con.getResources().getString(R.string.title_private));

        String displayText = "";
        InputStream tarjomer = null;
        try {
            tarjomer = activity.getResources().openRawResource(R.raw.private_policy);
            int fileLen = tarjomer.available();
            byte[] fileBuffer = new byte[fileLen];
            tarjomer.read(fileBuffer);
            tarjomer.close();
            displayText = new String(fileBuffer);

        } catch (IOException e) {
        }
        txt_private.setText(displayText);

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_private,re_up);
            }
        });

        int w = activity.getResources().getDisplayMetrics().widthPixels;
        dialog_private.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_private.show();
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
        dialog_private.dismiss();
    }

}
