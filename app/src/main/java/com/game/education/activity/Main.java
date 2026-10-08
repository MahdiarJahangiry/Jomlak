package com.game.education.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.body.SaveBackup;
import com.game.education.dialog.DialogRegister;
import com.game.education.dialog.DialogSignin;
import com.game.education.model.AdsTapsell;
import com.game.education.model.CoinManager;
import com.game.education.model.GoogleLoginHelper;
import com.game.education.model.Menu;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jgabrielfreitas.core.BlurImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Main extends AppCompatActivity {

    public GoogleLoginHelper googleHelper;
    ImageView img_play,img_co,img_logo;
    public LinearLayout li_register,li_frinds,li_ads;
    Prefs prefs;
    Menu menu;
    SoundPlayer sound;
    TextView img_video;
    AdsTapsell adsTapsell;
    Dialog dialog_ver,dialog_private;
    TextView txt_frinds,txt_register;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        FullScreen();

        prefs = new Prefs(this);
        menu = new Menu(this);
        sound = new SoundPlayer(this);
        adsTapsell = new AdsTapsell(this);
        img_video = findViewById(R.id.img_video);

        googleHelper = new GoogleLoginHelper(this, new GoogleLoginHelper.LoginListener() {
            @Override
            public void onSuccess(GoogleSignInAccount account) {

                LoginGoogle(account.getEmail(), account.getDisplayName(), App.Pass);

            }

            @Override
            public void onError(Exception e) {

                Toast.makeText(Main.this,
                        "ورود ناموفق",
                        Toast.LENGTH_SHORT).show();

            }
        });

        sound.StartMusic();

        img_co = findViewById(R.id.img_co);
        li_ads = findViewById(R.id.li_ads);
        img_logo = findViewById(R.id.img_logo);
        txt_frinds = findViewById(R.id.txt_frinds);
        txt_register = findViewById(R.id.txt_register);
        txt_frinds.setText(App.con.getResources().getString(R.string.frinds_main));

        li_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(Main.this, Shop.class);
                startActivity(i);
            }
        });
        img_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                adsTapsell.GetVideo();
            }
        });

        li_register = findViewById(R.id.li_register);
        li_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (prefs.GetUsername().equals("")) {
                    new DialogRegister(Main.this, "main", googleHelper);
                }
            }
        });
        li_frinds = findViewById(R.id.li_frinds);
        li_frinds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (!prefs.GetEmail().equals("")) {
                    DialoFrinds();
                } else {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.email_verified), Main.this);
                }
            }
        });

        img_play = findViewById(R.id.img_play);
        img_play.setOnClickListener(new View.OnClickListener() {
           @Override
          public void onClick(View view) {
              sound.PlayClick();
              Intent i = new Intent(Main.this, Titles.class);
            startActivity(i);
            }
        });

            LinearLayout li_bot = findViewById(R.id.li_bot);
            li_bot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sound.PlayClick();
                    Intent i = new Intent(Main.this, BotActivity.class);
                    startActivity(i);
                }
            });

        GetAbout();
        GetVersion();


    }






    private void SaveBackupInfo()
    {
        String scour_level = "";
        String[] str = prefs.GetTitleLevel().split(",");
        for (int i=0; i<str.length; i++){
            if (scour_level.equals("")){
                scour_level = String.valueOf(prefs.GetLevel(str[i]));
            }else {
                scour_level = scour_level + "," + prefs.GetLevel(str[i]);
            }
        }
        SaveBackup saveBackup = new SaveBackup(prefs.GetCoin(),prefs.GetScour(),prefs.GetScourOne(),prefs.GetScourTow()
                ,prefs.GetScourTree(),prefs.GetScourFour(),prefs.GetName(),prefs.GetLang(),prefs.GetCode(),prefs.GetTitleLevel(),scour_level,prefs.GetPath(),prefs.GetAvatar());

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.backup_save(App.Token + prefs.GetToken(),saveBackup);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {

                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                //AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Main.this);
            }
        };
        call.enqueue(callback);
    }

    void GetVersion()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.version();
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.body().getSuccess()){
                    if (Integer.parseInt(response.body().getResult().getVersion()) > App.VERSIONCODE){
                        DialogVersion();
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                //AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Main.this);
            }
        };
        call.enqueue(callback);
    }

    void GetAffiliate()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.init_affiliate(App.Token + prefs.GetToken());
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        if (response.body().getResult().getItems().getNot_used_affiliate() > 0) {
                            int af = response.body().getResult().getItems().getNot_used_affiliate() * App.Davat;
                            GetInit(af);
                        }
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                //AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Main.this);
            }
        };
        call.enqueue(callback);
    }

    void GetInit(int af)
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.affiliate(App.Token + prefs.GetToken());
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        changeCoin(af,"add");
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                //AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Main.this);
            }
        };
        call.enqueue(callback);
    }

    public void changeCoin(int amount, String type){
        CoinManager.addCoin(this, amount,type, new CoinManager.CoinCallback() {

            @Override
            public void onSuccess() {
                menu.SetCoin();
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.affiliate), Main.this);
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    void GetAbout()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.about();
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        for (int i = 0; i < response.body().getResult().getItems().getAbout().size(); i++) {
                            if (response.body().getResult().getItems().getAbout().get(i).getValue() != null) {
                                if (i == 0) {
                                    if (Integer.parseInt(response.body().getResult().getItems().getAbout().get(i).getValue()) > App.VERSIONCODE) {
                                        DialogVersion();
                                    }
                                } else if (i == 1) {
                                    prefs.SaveAboutTelegram(response.body().getResult().getItems().getAbout().get(i).getValue());
                                } else if (i == 2) {
                                    prefs.SaveAboutInsta(response.body().getResult().getItems().getAbout().get(i).getValue());
                                } else if (i == 3) {
                                    prefs.SaveEmailSupport(response.body().getResult().getItems().getAbout().get(i).getValue());
                                } else if (i == 4) {
                                    prefs.SaveAboutPhone(response.body().getResult().getItems().getAbout().get(i).getValue());
                                } else if (i == 5) {
                                    prefs.SaveAbout(response.body().getResult().getItems().getAbout().get(i).getValue());
                                }
                            }
                        }
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                //AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Main.this);
            }
        };
        call.enqueue(callback);
    }

    public  void DialogVersion()
    {
        dialog_ver = new Dialog(this);
        dialog_ver.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_ver.setContentView(R.layout.dialog_version);
        dialog_ver.setCancelable(false);
        dialog_ver.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog_ver.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_ver.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_ver.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.logo);

        TextView txt_dec = dialog_ver.findViewById(R.id.txt_dec);
        TextView txt_title = dialog_ver.findViewById(R.id.txt_title);
        txt_dec.setText(App.con.getResources().getString(R.string.new_version_title));
        txt_title.setText(App.con.getResources().getString(R.string.new_version));

        ImageView img_back = dialog_ver.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);

        ImageView txt_en = dialog_ver.findViewById(R.id.img_close);
        //txt_en.setText(App.con.getResources().getString(R.string.no_update));
        txt_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_ver,re_up);
            }
        });


        TextView txt_fa = dialog_ver.findViewById(R.id.txt_fa);
        txt_fa.setText(App.con.getResources().getString(R.string.update));
        txt_fa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.url_app)));
                startActivity(i);
            }
        });


        int w = getResources().getDisplayMetrics().widthPixels;
        dialog_ver.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_ver.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 100);
    }

    public void DialoFrinds()
    {
        Intent shairIntent = new Intent(Intent.ACTION_SEND);
        shairIntent.setType("text/plain");
        String shareBody = App.con.getResources().getString(R.string.app_Tozih_1) + " " + prefs.GetCode() + " " + App.con.getResources().getString(R.string.app_Tozih_2);
        shairIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shairIntent, App.con.getResources().getString(R.string.share_title)));
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
                            li_register.setVisibility(View.GONE);
                            GetBackupInfo();
                        } else {
                            AdsTapsell.Dialog(response.body().getError(), Main.this);
                        }
                    } else {
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Main.this);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Main.this);
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
                        menu.SetCoin();
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.login), Main.this);
                    } else {
                        AdsTapsell.Dialog(response.body().getError(), Main.this);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    new DialogRegister(Main.this, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Main.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count++;
                if(count > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Main.this);
                }else {
                    GetBackupInfo();
                }
            }
        };
        call.enqueue(callback);
    }

    public void getBalance()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.getBalance(App.Token + prefs.GetToken());
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        if (response.body().getResult() != null) {
                            if (response.body().getResult().getItems().getBalance() != null) {
                                prefs.SetCoin(response.body().getResult().getItems().getBalance());
                            }

                        }
                        menu.SetCoin();
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
            }
        };
        call.enqueue(callback);
    }



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        menu.SetAnimCoin();
        menu.SetCoin();
        SoundPlayer.PlayMusic();
        if (!prefs.GetUsername().equals("")) {
            SaveBackupInfo();
            GetAffiliate();
            getBalance();
        }
        if (!prefs.GetAds()){
            img_video.setVisibility(View.GONE);
        }
        if (prefs.GetUsername().equals("")) {
            txt_register.setText(App.con.getResources().getString(R.string.register_main));
            li_register.setVisibility(View.VISIBLE);
            new DialogRegister(Main.this, "main",googleHelper);
        }else {
            txt_register.setText(App.con.getResources().getString(R.string.signup));
            li_register.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        menu.SetTimerCansel();
        SoundPlayer.StopMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        menu.SetTimerCansel();
        SoundPlayer.PauseMusic();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FullScreen();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
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