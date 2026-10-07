package com.game.education.model;

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
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.jgabrielfreitas.core.BlurImageView;

import ir.tapsell.plus.AdRequestCallback;
import ir.tapsell.plus.AdShowListener;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.model.TapsellPlusAdModel;
import ir.tapsell.plus.model.TapsellPlusErrorModel;

public class AdsTapsell
{
    Activity act;
    Prefs prefs;
    SoundPlayer sound;
    boolean active = false;

    public AdsTapsell(Activity activity)
    {
        act = activity;
        prefs = new Prefs(activity);
        sound = new SoundPlayer(activity);
    }


    public void GetVideo()
    {

        Toast.makeText(act, App.con.getResources().getString(R.string.load_ads), Toast.LENGTH_SHORT).show();
        TapsellPlus.requestRewardedVideoAd(
                act,
                App.ZONE_ID_VIDEO,
                new AdRequestCallback() {
                    @Override
                    public void response(TapsellPlusAdModel tapsellPlusAdModel) {
                        //super.response(tapsellPlusAdModel);
                        String rewardedResponseId = tapsellPlusAdModel.getResponseId();
                        ShowTapsellVideo(rewardedResponseId);
                    }

                    @Override
                    public void error(String message) {
                        Dialog(App.con.getResources().getString(R.string.error_ads),act);
                    }
                });
    }

    public void ShowTapsellVideo(String rewardedResponseId)
    {
        active = false;
        TapsellPlus.showRewardedVideoAd( act, rewardedResponseId,
                new AdShowListener() {
                    @Override
                    public void onOpened(TapsellPlusAdModel tapsellPlusAdModel) {
                        super.onOpened(tapsellPlusAdModel);
                    }

                    @Override
                    public void onClosed(TapsellPlusAdModel tapsellPlusAdModel) {
                        super.onClosed(tapsellPlusAdModel);
                        if (!active) {
                            Dialog(App.con.getResources().getString(R.string.dialog_ads),act);
                        }
                    }

                    @Override
                    public void onRewarded(TapsellPlusAdModel tapsellPlusAdModel) {
                        super.onRewarded(tapsellPlusAdModel);
                        active = true;
                        changeCoin(App.Video,"video");
                    }

                    @Override
                    public void onError(TapsellPlusErrorModel tapsellPlusErrorModel) {
                        super.onError(tapsellPlusErrorModel);
                        Dialog(App.con.getResources().getString(R.string.error1_ads),act);
                    }
                });
    }

    public void changeCoin(int amount, String type){
        CoinManager.addCoin(act, amount,type, new CoinManager.CoinCallback() {

            @Override
            public void onSuccess() {
                sound.PlayHint();
                Dialog(App.Video + App.con.getResources().getString(R.string.coin_ads),act);
            }

            @Override
            public void onError(String error) {

            }
        });
    }


    public static void Dialog(String desc,Activity act)
    {
        SoundPlayer sound = new SoundPlayer(act);
        Dialog dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.gozareshseyf);
        TextView txt_title = dialog.findViewById(R.id.txt_title);
        txt_title.setText(App.con.getResources().getString(R.string.toast));
        TextView txt_toast = dialog.findViewById(R.id.txt_toast);
        txt_toast.setText(desc);
        ImageView txt_close = dialog.findViewById(R.id.img_close);
        //txt_close.setText(App.con.getResources().getString(R.string.close));
        ImageView img_back = dialog.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        txt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                YoYo.with(Techniques.ZoomOut)
                        .duration(500)
                        .repeat(0)
                        .playOn(re_up);
                new Handler().postDelayed(() -> {
                    dialog.dismiss();
                }, 400);
            }
        });


        int w = act.getResources().getDisplayMetrics().widthPixels;
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
