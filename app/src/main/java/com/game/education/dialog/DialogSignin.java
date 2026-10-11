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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.activity.Main;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.model.AdsTapsell;
import com.game.education.model.CoinManager;
import com.game.education.model.GoogleLoginHelper;
import com.game.education.model.Menu;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.jgabrielfreitas.core.BlurImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DialogSignin {

    Dialog dialog_signin;
    Prefs prefs;
    SoundPlayer sound;
    Menu menu;
    int count = 0;

    public DialogSignin(Activity activity, int co, String type, GoogleLoginHelper googleHelper){
        dialog_signin = new Dialog(activity);
        dialog_signin.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_signin.setContentView(R.layout.dialog_signin);
        dialog_signin.setCancelable(false);
        dialog_signin.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        sound = new SoundPlayer(activity);
        prefs = new Prefs(activity);
        menu = new Menu(activity);

        TextView txt_title = dialog_signin.findViewById(R.id.txt_title);
        if (co == 1){
            txt_title.setText(App.con.getResources().getString(R.string.signup));
        }else {
            txt_title.setText(App.con.getResources().getString(R.string.signin));
        }

        ImageView img_back = dialog_signin.findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                new DialogRegister(activity,type,googleHelper);
            }
        });
        RelativeLayout re_up = dialog_signin.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);
        BlurImageView img_background = dialog_signin.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_signin.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.profileseyf);
        ImageView img_close = dialog_signin.findViewById(R.id.img_close);
        img_close.setVisibility(View.GONE);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_signin,re_up);
            }
        });

        EditText edt_code = dialog_signin.findViewById(R.id.edt_code);
        edt_code.setHint(App.con.getResources().getString(R.string.code_moaref));
        if (co != 1){
            edt_code.setVisibility(View.GONE);
        }
        EditText edt_email = dialog_signin.findViewById(R.id.edt_email);
        edt_email.setHint(App.con.getResources().getString(R.string.username));
        EditText edt_pass = dialog_signin.findViewById(R.id.edt_pass);
        edt_pass.setHint(App.con.getResources().getString(R.string.password));
        Button btn_signin = dialog_signin.findViewById(R.id.btn_signin);
        btn_signin.setText(App.con.getResources().getString(R.string.login_register));
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edt_email.getText().toString().trim();
                String pass = edt_pass.getText().toString().trim();
                if (username.length() >= 5) {
                    sound.PlayClick();
                    if(pass.length() > 7){
                        if (co == 1) {
                            Register(username, pass,edt_code.getText().toString(), activity, type,googleHelper);
                        }else {
                            Login(username,pass,activity,type,googleHelper);
                        }
                    }else {
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.no_pass),activity);
                    }
                }else {
                    AdsTapsell.Dialog("نام کاربری حداقل 5 کاراکتر باید باشد",activity);
                }
            }
        });


        int w = activity.getResources().getDisplayMetrics().widthPixels;
        dialog_signin.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_signin.show();

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
        dialog_signin.dismiss();
    }

    public void Register(String username, String pass,String code, Activity activity, String type, GoogleLoginHelper googleHelper)
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.register(username,pass,code);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                android.util.Log.d("LOGIN_DEBUG", "Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("LOGIN_DEBUG", "Success: " + response.body().getSuccess());
                    android.util.Log.d("LOGIN_DEBUG", "Error Msg: " + response.body().getError());
                if (response.code() == 200) {
                    if (response.body().getSuccess() != null) {
                        if (response.body().getSuccess()) {
                            prefs.SetUsername(username);
                            prefs.SetPass(pass);
                            prefs.SaveGoogle(false);
                            prefs.SaveCode(response.body().getResult().getAffiliateCode());
                            prefs.SetToken(response.body().getResult().getToken());
                            dismiss();
                            changeCoin(App.Register,"add",type,activity);

                        } else {
                            AdsTapsell.Dialog(response.body().getError(), activity);
                        }
                    }else {
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),activity);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    dismiss();
                    new DialogRegister(activity,type,googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),activity);
                }
                } else {
                    android.util.Log.d("LOGIN_DEBUG", "Response not successful or body null");
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                android.util.Log.e("LOGIN_DEBUG", "Network Failed: " + t.getMessage(), t);
                Log.e(App.LogName, t.toString());
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),activity);
            }
        };
        call.enqueue(callback);
    }

    public void changeCoin(int amount, String typeCoin, String type, Activity activity){
        CoinManager.addCoin(activity, amount,typeCoin, new CoinManager.CoinCallback() {

            @Override
            public void onSuccess() {
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.register), activity);
                menu.SetCoin();
                sound.PlayHint();
                if(type.equals("main")){
                    ((Main) activity).li_register.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void Login(String username, String pass, Activity activity, String type, GoogleLoginHelper googleHelper)
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.login(username,pass);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess() != null) {
                        if (response.body().getSuccess()) {
                            prefs.SetUsername(username);
                            prefs.SetPass(pass);
                            prefs.SaveGoogle(false);
                            prefs.SaveCode(response.body().getResult().getAffiliateCode());
                            prefs.SetToken(response.body().getResult().getToken());
                            if(type.equals("main")){
                                ((Main) activity).li_register.setVisibility(View.GONE);
                            }
                            GetBackupInfo(activity,googleHelper);
                        } else {
                            AdsTapsell.Dialog(response.body().getError(), activity);
                        }
                    } else {
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), activity);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    dismiss();
                    new DialogRegister(activity,type,googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),activity);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),activity);
            }
        };
        call.enqueue(callback);
    }

    public void GetBackupInfo(Activity activity, GoogleLoginHelper googleHelper)
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
                        menu.SetCoin();

                        dismiss();
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.login), activity);
                    } else {
                        AdsTapsell.Dialog(response.body().getError(), activity);
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    dismiss();
                    new DialogRegister(activity, "main",googleHelper);
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),activity);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count++;
                if(count > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), activity);
                }else {
                    GetBackupInfo(activity, googleHelper);
                }
            }
        };
        call.enqueue(callback);
    }

}
