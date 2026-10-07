package com.game.education.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.model.GoogleLoginHelper;
import com.jgabrielfreitas.core.BlurImageView;
import com.game.education.model.SoundPlayer;

public class DialogRegister {

    Dialog dialog_register;

    public DialogRegister(Activity activity, String type, GoogleLoginHelper googleHelper)
    {
        dialog_register = new Dialog(activity);
        dialog_register.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_register.setContentView(R.layout.content_register);
        dialog_register.setCancelable(false);
        dialog_register.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Log.e(App.LogName,type);

        RelativeLayout re_up = dialog_register.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        SoundPlayer sound = new SoundPlayer(activity);

        LinearLayout li_private = dialog_register.findViewById(R.id.li_private);
        li_private.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                new DialogPravate(activity);
            }
        });

        BlurImageView img_background = dialog_register.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        TextView txt_vorod = dialog_register.findViewById(R.id.txt_vorod);
        TextView txt_title = dialog_register.findViewById(R.id.txt_title);
        TextView txt_title_2 = dialog_register.findViewById(R.id.txt_title_2);
        TextView txt_and = dialog_register.findViewById(R.id.txt_and);
        txt_and.setText(App.con.getResources().getString(R.string.and));
        txt_title.setText(App.con.getResources().getString(R.string.txt_title_register));
        txt_title_2.setText(App.con.getResources().getString(R.string.txt_2_register));

        TextView txt_login = dialog_register.findViewById(R.id.txt_login);
        txt_login.setText(App.con.getResources().getString(R.string.signin));
        txt_vorod.setText(App.con.getResources().getString(R.string.signin_google));
        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                dismiss();
                new DialogSignin(activity,2, type,googleHelper);
            }
        });
        TextView txt_register = dialog_register.findViewById(R.id.txt_register);
        txt_register.setText(App.con.getResources().getString(R.string.signup));
        txt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                dismiss();
                new DialogSignin(activity,1, type,googleHelper);
            }
        });
        ImageView txt_close = dialog_register.findViewById(R.id.txt_close);
        txt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_register,re_up);
            }
        });
        LinearLayout img_gmail = dialog_register.findViewById(R.id.img_gmail);
        img_gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                googleHelper.signIn(dialog_register);
            }
        });


        int w = activity.getResources().getDisplayMetrics().widthPixels;
        dialog_register.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_register.show();

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
        dialog_register.dismiss();
    }


}
