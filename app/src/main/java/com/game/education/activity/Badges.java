package com.game.education.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.education.App;
import com.game.education.R;
import com.game.education.model.AdsTapsell;
import com.game.education.model.CustomBadge;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.game.education.model.Tollbar;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Badges extends AppCompatActivity {

    SoundPlayer sound;
    Prefs prefs;
    Tollbar tol;
    int id;
    ImageView img_title;
    LinearLayout li_1,li_2,li_3,li_4;
    List<Integer> array_drawble = new ArrayList<>();
    List<Integer> array_scour = new ArrayList<>();
    List<String> array_sentences = new ArrayList<>();
    TextView img_video;
    AdsTapsell adsTapsell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_badges);



        FullScreen();
        sound = new SoundPlayer(this);
        tol = new Tollbar(this);
        prefs = new Prefs(this);

        BlurImageView img_background = findViewById(R.id.img_background);
        img_background.setImageResource(R.drawable.back_main);
        img_title = findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.neshanha1);

        li_1 = findViewById(R.id.li_1);
        li_2 = findViewById(R.id.li_2);
        li_3 = findViewById(R.id.li_3);
        li_4 = findViewById(R.id.li_4);

        adsTapsell = new AdsTapsell(this);
        img_video = findViewById(R.id.img_video);
        img_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                adsTapsell.GetVideo();
            }
        });

        Bundle bundle=getIntent().getExtras();
        id = bundle.getInt("id");
        tol.txt_title.setText(bundle.getString("title"));

        array_sentences.add(App.con.getResources().getString(R.string.scour_200));
        array_sentences.add(App.con.getResources().getString(R.string.scour_100));
        array_sentences.add(App.con.getResources().getString(R.string.scour_50));
        array_sentences.add(App.con.getResources().getString(R.string.scour_750));
        array_sentences.add(App.con.getResources().getString(R.string.scour_500));
        array_sentences.add(App.con.getResources().getString(R.string.scour_400));
        array_sentences.add(App.con.getResources().getString(R.string.scour_3000));
        array_sentences.add(App.con.getResources().getString(R.string.scour_2000));
        array_sentences.add(App.con.getResources().getString(R.string.scour_1000));
        array_sentences.add(App.con.getResources().getString(R.string.scour_10000));
        array_sentences.add(App.con.getResources().getString(R.string.scour_7500));
        array_sentences.add(App.con.getResources().getString(R.string.scour_5000));

        array_scour.add(200);
        array_scour.add(100);
        array_scour.add(50);
        array_scour.add(750);
        array_scour.add(500);
        array_scour.add(400);
        array_scour.add(3000);
        array_scour.add(2000);
        array_scour.add(1000);
        array_scour.add(10000);
        array_scour.add(7500);
        array_scour.add(5000);

        if (id == 1) {
            array_drawble.add(R.drawable.badg_1);
            array_drawble.add(R.drawable.badg_1_noghre);
            array_drawble.add(R.drawable.badg_1_boronz);
            array_drawble.add(R.drawable.badg_2);
            array_drawble.add(R.drawable.badg_2_noghre);
            array_drawble.add(R.drawable.badg_2_boronz);
            array_drawble.add(R.drawable.badg_3);
            array_drawble.add(R.drawable.badg_3_noghre);
            array_drawble.add(R.drawable.badg_3_boronz);
            array_drawble.add(R.drawable.badg_4);
            array_drawble.add(R.drawable.badg_4_noghre);
            array_drawble.add(R.drawable.badg_4_boronz);
        } else if (id == 2) {
            array_drawble.add(R.drawable.share_medalion_03);
            array_drawble.add(R.drawable.share_edalion_02);
            array_drawble.add(R.drawable.share_medalion_01);
            array_drawble.add(R.drawable.share_medalion_06);
            array_drawble.add(R.drawable.share_medalion_05);
            array_drawble.add(R.drawable.share_medalion_04);
            array_drawble.add(R.drawable.share_medalion_09);
            array_drawble.add(R.drawable.share_medalion_08);
            array_drawble.add(R.drawable.share_medalion_07);
            array_drawble.add(R.drawable.share_medalion_12);
            array_drawble.add(R.drawable.share_medalion_11);
            array_drawble.add(R.drawable.share_medalion_10);
        } else if (id >= 3) {
            array_drawble.add(R.drawable.badg_1);
            array_drawble.add(R.drawable.badg_1_noghre);
            array_drawble.add(R.drawable.badg_1_boronz);
            array_drawble.add(R.drawable.badg_2);
            array_drawble.add(R.drawable.badg_2_noghre);
            array_drawble.add(R.drawable.badg_2_boronz);
            array_drawble.add(R.drawable.badg_3);
            array_drawble.add(R.drawable.badg_3_noghre);
            array_drawble.add(R.drawable.badg_3_boronz);
            array_drawble.add(R.drawable.badg_4);
            array_drawble.add(R.drawable.badg_4_noghre);
            array_drawble.add(R.drawable.badg_4_boronz);
        }

        for (int i=0; i<12; i++) {
            CustomBadge customBadge = new CustomBadge(Badges.this);
            customBadge.img_badge.setImageResource(array_drawble.get(i));
            customBadge.txt_badge.setText(array_sentences.get(i));
            if (i < 3){
                li_1.addView(customBadge);
            }else if (i > 2 && i < 6){
                li_2.addView(customBadge);
            }else if (i > 5 && i < 9){
                li_3.addView(customBadge);
            }else if (i > 8 && i < 12){
                li_4.addView(customBadge);
            }
            if (App.IdBack == 1){
                if (array_scour.get(i) <= prefs.GetScourOne()) {
                    customBadge.card_badge.setVisibility(View.GONE);
                    customBadge.active = true;
                    prefs.SetBadge(array_drawble.get(i).toString());
                }
            }else if (App.IdBack == 2){
                if (array_scour.get(i) <= prefs.GetScourTow()){
                    customBadge.card_badge.setVisibility(View.GONE);
                    customBadge.active = true;
                    prefs.SetBadge(array_drawble.get(i).toString());
                }
            }else if (App.IdBack == 3){
                if (array_scour.get(i) <= prefs.GetScourTree()){
                    customBadge.card_badge.setVisibility(View.GONE);
                    customBadge.active = true;
                    prefs.SetBadge(array_drawble.get(i).toString());
                }
            }else if (App.IdBack == 4){
                if (array_scour.get(i) <= prefs.GetScourFour()){
                    customBadge.card_badge.setVisibility(View.GONE);
                    customBadge.active = true;
                    prefs.SetBadge(array_drawble.get(i).toString());
                }
            }
        }


    }//main



    @Override
    protected void onResume() {
        super.onResume();
        SoundPlayer.PlayMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundPlayer.PauseMusic();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        FullScreen();
    }

    public void FullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            FullScreen();
        }
    }



}//class