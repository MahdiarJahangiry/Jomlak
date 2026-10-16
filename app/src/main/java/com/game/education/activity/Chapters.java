package com.game.education.activity;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.game.education.App;
import com.game.education.R;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.dialog.DialogRegister;
import com.game.education.model.AdsTapsell;
import com.game.education.model.CustomLevel;
import com.game.education.model.CustomPoint;
import com.game.education.model.GoogleLoginHelper;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.game.education.model.Tollbar;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Chapters extends AppCompatActivity {

    public GoogleLoginHelper googleHelper;
    SoundPlayer sound;
    Tollbar tol;
    Prefs prefs;
    LinearLayout li_1,li_2,li_3,li_point;
    List<Integer> array_id;
    List<CustomPoint> array_point;
    int count = 1,count_1 = 0;
    int id,le,start = 0,end,point = 1,max = 1,leath = 0;
    ImageView img_let,img_right,img_close,img_back;
    TextView txt_level;
    //List<Integer> array_level;
    TextView img_video;
    AdsTapsell adsTapsell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chapters);

        FullScreen();

        googleHelper = new GoogleLoginHelper(this, new GoogleLoginHelper.LoginListener() {
            @Override
            public void onSuccess(GoogleSignInAccount account) {

                LoginGoogle(account.getEmail(), account.getDisplayName(), App.Pass);

            }

            @Override
            public void onError(Exception e) {

                Toast.makeText(Chapters.this,
                        "ورود ناموفق",
                        Toast.LENGTH_SHORT).show();

            }
        });

        sound = new SoundPlayer(this);
        tol = new Tollbar(this);
        prefs = new Prefs(this);
        array_id = new ArrayList<>();
        array_point = new ArrayList<>();
        //array_level = new ArrayList<>();

        adsTapsell = new AdsTapsell(this);
        img_video = findViewById(R.id.img_video);
        img_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                adsTapsell.GetVideo();
            }
        });

        img_close = findViewById(R.id.img_close);
        txt_level = findViewById(R.id.txt_level);
        txt_level.setText(App.con.getResources().getString(R.string.title_level));
        li_point = findViewById(R.id.li_point);
        img_let = findViewById(R.id.img_let);
        img_let.setVisibility(View.INVISIBLE);
        img_right = findViewById(R.id.img_right);
        img_right.setVisibility(View.INVISIBLE);


        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(Chapters.this, Main.class);
                startActivity(i);
                finish();
            }
        });
        img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                finish();
            }
        });

        img_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (point < max){
                    sound.PlayClick();
                    point++;
                    end = point * 9;
                    if (end > array_id.size()){
                        end = array_id.size();
                    }
                    if (point == max){
                        img_right.setVisibility(View.INVISIBLE);
                    }
                    img_let.setVisibility(View.VISIBLE);
                    SetLevels();
                    ChangePoint();
                }
            }
        });

        img_let.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (point > 1){
                    sound.PlayClick();
                    point--;
                    end = point * 9;
                    start = end - 9;
                    if (point == 1){
                        img_let.setVisibility(View.INVISIBLE);
                    }
                    img_right.setVisibility(View.VISIBLE);
                    SetLevels();
                    ChangePoint();
                }
            }
        });

        li_1 = findViewById(R.id.li_1);
        li_2 = findViewById(R.id.li_2);
        li_3 = findViewById(R.id.li_3);
        Bundle bundle=getIntent().getExtras();
        id = bundle.getInt("id");

        le = prefs.GetLevel("le_"+id);

        GetDate();

    }//main

    void GetDate()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.levels> call =apiInterface.chapters(id, prefs.GetLang());
        Callback<ServerData.levels> callback = new Callback<ServerData.levels>() {
            @Override
            public void onResponse(Call<ServerData.levels> call, Response<ServerData.levels> response) {
                if (response.body().getItems() != null) {
                    leath = 0;
                    if (response.body().getItems().size() > 0) {
                        for (int i = 0; i < response.body().getItems().size(); i++) {
                            int level = response.body().getItems().get(i).getLevelCount();
                            if (level > 0){
                                //for (int j=0; j<level; j++){
                                    array_id.add(response.body().getItems().get(i).getId());
                                    //array_level.add(j+1);
                                    leath++;
                                    if (leath % 9 == 0){
                                        max++;
                                    }
                               // }

                            }
                        }
                        if (array_id.size() % 9 == 0){
                            max--;
                        }
                        if (array_id.size() > 9){
                            end = 9;
                            img_right.setVisibility(View.VISIBLE);
                        }else {
                            end = array_id.size();
                        }
                        for (int i=0; i<max; i++){
                            CustomPoint customPoint = new CustomPoint(Chapters.this);
                            if (i == 0){
                                customPoint.card_point.setCardBackgroundColor(getResources().getColor(R.color.red));
                            }else {
                                customPoint.card_point.setCardBackgroundColor(getResources().getColor(R.color.white));
                            }
                            array_point.add(customPoint);
                            li_point.addView(customPoint);
                        }
                        if (le > 9) {
                            ChangeLevel();
                        }else {
                            SetLevels();
                        }
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Chapters.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.levels> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count_1++;
                if(count_1 > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Chapters.this);
                }else {
                    GetDate();
                }
            }
        };
        call.enqueue(callback);
    }

    private void LoginGoogle(String email, String username, String pass)
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.login_email(email,pass);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess() != null) {
                        if (response.body().getSuccess()) {
                            prefs.SetEmail(email);
                            prefs.SetUsername(username);
                            prefs.SetPass(pass);
                            prefs.SaveGoogle(true);
                            prefs.SaveEmailVerified(true);
                            prefs.SaveCode(response.body().getResult().getAffiliateCode());
                            prefs.SetToken(response.body().getResult().getToken());
                            GetBackupInfo();
                        } else {
                            AdsTapsell.Dialog(response.body().getError(), Chapters.this);
                        }
                    } else {
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Chapters.this);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Chapters.this, "chapters",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Chapters.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Chapters.this);
            }
        };
        call.enqueue(callback);
    }

    public void GetBackupInfo()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.backup_get(App.Token + prefs.GetToken());
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        if (response.body().getResult() != null) {
                            /*if (response.body().getResult().getCoin() != null) {
                                prefs.SetCoin(response.body().getResult().getCoin());
                            }*/
                            if (response.body().getResult().getScour_all() != null) {
                                prefs.SetScourAll(response.body().getResult().getScour_all());
                            }
                            if (response.body().getResult().getScour_one() != null) {
                                prefs.SetScourOne(response.body().getResult().getScour_one());
                            }
                            if (response.body().getResult().getScour_tow() != null) {
                                prefs.SetScourTow(response.body().getResult().getScour_tow());
                            }
                            if (response.body().getResult().getScour_tree() != null) {
                                prefs.SetScourTree(response.body().getResult().getScour_tree());
                            }
                            if (response.body().getResult().getScour_four() != null) {
                                prefs.SetScourFour(response.body().getResult().getScour_four());
                            }
                            if (response.body().getResult().getUser() != null) {
                                prefs.SetName(response.body().getResult().getUser());
                            }
                            if (response.body().getResult().getPath() != null) {
                                prefs.SavePath(response.body().getResult().getPath());
                            }
                            if (response.body().getResult().getAvatar() != null) {
                                prefs.SetAvatar(response.body().getResult().getAvatar());
                            }
                    /*if (response.body().getResult().getLang() != null) {
                        prefs.SetLang(response.body().getResult().getLang());
                        menu.SetLangouch(prefs.GetLang());
                    }*/
                            if (response.body().getResult().getCode() != null) {
                                prefs.SaveCode(response.body().getResult().getCode());
                            }
                            if (response.body().getResult().getLevel() != null) {
                                String[] str = response.body().getResult().getLevel().split(",");
                                String[] scour = response.body().getResult().getScour_level().split(",");
                                for (int i = 0; i < str.length; i++) {
                                    prefs.SetLevel(str[i], Integer.parseInt(scour[i]));
                                    prefs.SaveTitleLevel(str[i]);
                                }
                            }
                        }
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.login), Chapters.this);
                    } else {
                        AdsTapsell.Dialog(response.body().getError(), Chapters.this);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Chapters.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Chapters.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count++;
                if(count > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Chapters.this);
                }else {
                    GetBackupInfo();
                }
            }
        };
        call.enqueue(callback);
    }

    void ChangeLevel()
    {
        int co = 1;
        for (int i=0; i<10; i++){
            if (le > co * 9){
                co++;
                point++;
                end = point * 9;
                start = end - 9;
            }else {
                if (end > array_id.size()){
                    end = array_id.size();
                }
                if (point == max){
                    img_right.setVisibility(View.INVISIBLE);
                }
                img_let.setVisibility(View.VISIBLE);
                SetLevels();
                ChangePoint();
                break;
            }
        }
    }

    void SetLevels()
    {
        int i = 0;
        li_1.removeAllViews();
        li_2.removeAllViews();
        li_3.removeAllViews();
        while (start < end){
            CustomLevel customCategory = new CustomLevel(Chapters.this);
            customCategory.id = array_id.get(start);
            customCategory.id_level = id;
            customCategory.page = 1;//array_level.get(start);
            if (start + 1 < le) {
                //customCategory.img_level.setImageResource(R.drawable.level_icon);
                //customCategory.card_level.setCardBackgroundColor(getResources().getColor(R.color.teal_400));
                customCategory.txt_level.setText((start + 1) + "");
                customCategory.txt_level.setBackgroundResource(R.drawable.green);
                customCategory.img_pic.setBackgroundResource(R.drawable.un_lock);
                customCategory.active = true;
            } else if (start + 1 == le) {
                //customCategory.img_level.setImageResource(0);
                //customCategory.card_level.setCardBackgroundColor(getResources().getColor(R.color.orang_400));
                customCategory.txt_level.setText((start + 1) + "");
                customCategory.txt_level.setBackgroundResource(R.drawable.black);
                customCategory.img_pic.setBackgroundResource(R.drawable.neshan_level);
                customCategory.active = true;
            } else if (start + 1 > le) {
                //customCategory.img_level.setImageResource(R.drawable.level_lock_icon);
               // customCategory.card_level.setCardBackgroundColor(getResources().getColor(R.color.red));
                customCategory.txt_level.setBackgroundResource(R.drawable.red);
                customCategory.img_pic.setBackgroundResource(R.drawable.lock);
                customCategory.txt_level.setText((start + 1) + "");
                customCategory.active = false;
            }
            if (i < 3){
                count = 1;
            }else if (i > 2 && i < 6) {
                count = 2;
            } else if (i > 5) {
                count = 3;
            }
            if (count == 1) {
                li_1.addView(customCategory);
            } else if (count == 2) {
                li_2.addView(customCategory);
            } else if (count == 3) {
                li_3.addView(customCategory);
            }
            start++;
            i++;
        }
    }

    void ChangePoint()
    {
        for (int i=0; i<array_point.size(); i++){
            array_point.get(i).card_point.setCardBackgroundColor(getResources().getColor(R.color.white));
        }
        array_point.get(point-1).card_point.setCardBackgroundColor(getResources().getColor(R.color.red));
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



}