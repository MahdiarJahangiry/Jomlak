package com.game.education.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.game.education.App;
import com.game.education.R;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.model.AdsTapsell;
import com.game.education.model.CustomTitles;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.game.education.model.Tollbar;
import com.jgabrielfreitas.core.BlurImageView;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TitleBadges extends AppCompatActivity {

    TextView txt_titr;
    SoundPlayer sound;
    Prefs prefs;
    Tollbar tol;
    int count = 0;
    boolean active = true;
    LinearLayout le_right,li_left;
    TextView img_video;
    AdsTapsell adsTapsell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_titles);

        FullScreen();
        sound = new SoundPlayer(this);
        tol = new Tollbar(this);
        prefs = new Prefs(this);
        BlurImageView img_background = findViewById(R.id.img_background);
        img_background.setImageResource(R.drawable.back_main);
        ImageView img_title = findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.neshanha1);
        tol.txt_title.setText("");
        txt_titr = findViewById(R.id.txt_title);
        txt_titr.setText(App.con.getResources().getString(R.string.badgs));
        le_right = findViewById(R.id.le_right);
        li_left = findViewById(R.id.li_left);

        adsTapsell = new AdsTapsell(this);
        img_video = findViewById(R.id.img_video);
        img_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                adsTapsell.GetVideo();
            }
        });

        GetDate();

    }//main

    void GetDate()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.levels> call = apiInterface.titles( prefs.GetLang());
        Callback<ServerData.levels> callback = new Callback<ServerData.levels>() {
            @Override
            public void onResponse(Call<ServerData.levels> call, Response<ServerData.levels> response) {
                if(response.body().getItems() != null) {
                    if (response.body().getItems().size() > 0) {
                        for (int i = 0; i < response.body().getItems().size(); i++) {
                            CustomTitles customTitles = new CustomTitles(TitleBadges.this);
                            customTitles.IdBack = i+1;
                            customTitles.badg = true;
                            customTitles.id = i+1;
                            customTitles.txt_title.setText(response.body().getItems().get(i).getTitle());
                            if (active) {
                                le_right.addView(customTitles);
                                active = false;
                            } else {
                                li_left.addView(customTitles);
                                active = true;
                            }
                        }
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),TitleBadges.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.levels> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count++;
                if(count > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), TitleBadges.this);
                }else {
                    GetDate();
                }
            }
        };
        call.enqueue(callback);
    }


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