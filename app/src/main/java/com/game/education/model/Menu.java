package com.game.education.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.activity.Dashboard;
import com.game.education.activity.Main;
import com.game.education.activity.Profile;
import com.game.education.activity.Shop;
import com.game.education.activity.TitleBadges;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.dialog.DialogRegister;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Menu
{
    public ImageView img_close,img_profile,img_menu;
    RelativeLayout re_menu;
    LinearLayout li_badges,li_menu;
    TextView txt_name,txt_scour,txt_coin,txt_share,txt_my_proile,txt_badges,txt_shop,txt_seting,txt_language,txt_username,txt_scour_all
            ,txt_status,txt_about,txt_contact;
    ImageView img_coin;
    Prefs prefs;
    SoundPlayer sound;
    Timer timer_coin;
    Dialog dialog_seting,dialog_lang,dialog_status,dialog_about;
    TextView txt_category_1,txt_category_2,txt_category_3,txt_category_4,txt_scour_4,txt_scour_1,txt_category,txt_scour_2,txt_scour_3,txt_scour_all_2;
    ProgressBar progress_bar;
    int count = 0;
    private final String[] musicTitles = {
            "موزیک بی کلام",
            "موزیک آرامش بخش «با هندزفری»"
    };

    Activity act;
    public Menu(Activity activity)
    {
        act = activity;
        prefs = new Prefs(act);
        sound = new SoundPlayer(act);
        re_menu = act.findViewById(R.id.re_menu);
        re_menu.setVisibility(View.GONE);

        li_menu = act.findViewById(R.id.li_menu);
        txt_scour_all = act.findViewById(R.id.txt_scour_all);
        //txt_username = act.findViewById(R.id.txt_username);
        txt_language = act.findViewById(R.id.txt_language);
        txt_seting = act.findViewById(R.id.txt_seting);
        txt_shop = act.findViewById(R.id.txt_shop);
        txt_my_proile = act.findViewById(R.id.txt_my_proile);
        txt_share = act.findViewById(R.id.txt_share);
        img_menu = act.findViewById(R.id.img_menu);
        txt_coin = act.findViewById(R.id.txt_coin);
        img_coin = act.findViewById(R.id.img_coin);
        li_badges = act.findViewById(R.id.li_badges);
        txt_scour = act.findViewById(R.id.txt_scour);
        txt_name = act.findViewById(R.id.txt_name);
        img_profile = act.findViewById(R.id.img_profile);
        txt_badges = act.findViewById(R.id.txt_badges);
        txt_status = act.findViewById(R.id.txt_status);
        txt_about = act.findViewById(R.id.txt_about);
        txt_contact = act.findViewById(R.id.txt_contact);

        //txt_username.setText(App.con.getResources().getString(R.string.name_show));
        txt_scour_all.setText(App.con.getResources().getString(R.string.scour_all));
        txt_shop.setText(App.con.getResources().getString(R.string.shop));
        txt_status.setText(App.con.getResources().getString(R.string.status));
        txt_badges.setText(App.con.getResources().getString(R.string.badgs));
        txt_seting.setText(App.con.getResources().getString(R.string.seting));
        txt_about.setText(App.con.getResources().getString(R.string.about));
        txt_language.setText(App.con.getResources().getString(R.string.select_language));
        txt_my_proile.setText(App.con.getResources().getString(R.string.profile));
        //txt_contact.setText(App.con.getResources().getString(R.string.support));
        //txt_share.setText(App.con.getResources().getString(R.string.frind_menu));

        txt_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!prefs.GetUsername().equals("")) {
                    Intent i = new Intent(act, Profile.class);
                    act.startActivity(i);
                }else {
                    new DialogRegister(activity,"main",((Main) activity).googleHelper);
                }
                CloseMenu();
            }
        });

        txt_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialoStatus();
                CloseMenu();
            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!prefs.GetUsername().equals("")) {
                    Intent i = new Intent(act, Profile.class);
                    act.startActivity(i);
                }else {
                    new DialogRegister(activity,"main",((Main) activity).googleHelper);
                }
                CloseMenu();
            }
        });

        if (!prefs.GetAvatar().equals("")){
            Glide.with(activity)
                    .load(prefs.GetAvatar())
                    .into(img_profile);
        }

        img_close = act.findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseMenu();
            }
        });

        txt_seting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogSeting();
                CloseMenu();
            }
        });
        txt_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialoLang();
                CloseMenu();
            }
        });

        txt_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogAbout();
                CloseMenu();
            }
        });
        img_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowMenu();
            }
        });

        txt_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(act, Shop.class);
                act.startActivity(i);
                CloseMenu();
            }
        });
        txt_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialoFrinds();
                CloseMenu();
            }
        });

        txt_my_proile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!prefs.GetUsername().equals("")) {
                    Intent i = new Intent(act, Profile.class);
                    act.startActivity(i);
                }else {
                    new DialogRegister(activity,"main",((Main) activity).googleHelper);
                }
                CloseMenu();
            }
        });

        txt_badges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(act, TitleBadges.class);
                act.startActivity(i);
                CloseMenu();
            }
        });

        li_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        SetCoin();
    }

    void GetDate()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.levels> call = apiInterface.titles_with_token(App.Token + prefs.GetToken(), prefs.GetLang());
        Callback<ServerData.levels> callback = new Callback<ServerData.levels>() {
            @Override
            public void onResponse(Call<ServerData.levels> call, Response<ServerData.levels> response) {
                if(response.body().getItems() != null) {
                    if (response.body().getItems().size() > 0) {
                        progress_bar.setVisibility(View.GONE);
                        txt_category.setText(App.con.getResources().getString(R.string.scour_all));
                        txt_category_1.setText(App.con.getResources().getString(R.string.cateory) + " " +response.body().getItems().get(0).getTitle());
                        txt_category_2.setText(App.con.getResources().getString(R.string.cateory) + " " +response.body().getItems().get(1).getTitle());
                        if (response.body().getItems().size() >= 3) {
                            txt_scour_3.setText(prefs.GetScourTree() + ":");
                            txt_category_3.setText(App.con.getResources().getString(R.string.cateory) + " " + response.body().getItems().get(2).getTitle());
                        }
                        if (response.body().getItems().size() >= 4) {
                            txt_scour_4.setText(prefs.GetScourFour() + ":");
                            txt_category_4.setText(App.con.getResources().getString(R.string.cateory) + " " + response.body().getItems().get(3).getTitle());
                        }
                        txt_scour_1.setText(prefs.GetScourOne() + ":");
                        txt_scour_2.setText(prefs.GetScourTow() + ":");
                        txt_scour_all_2.setText(prefs.GetScour()+"");
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerData.levels> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count++;
                if(count > 4) {
                    progress_bar.setVisibility(View.GONE);
                    AdsTapsell.Dialog(act.getResources().getString(R.string.internet), act);
                }else {
                    GetDate();
                }
            }
        };
        call.enqueue(callback);
    }

    public  void DialoStatus()
    {
        dialog_status = new Dialog(act);
        dialog_status.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_status.setContentView(R.layout.dialog_status);
        dialog_status.setCancelable(false);
        dialog_status.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progress_bar = dialog_status.findViewById(R.id.progress_bar);
        RelativeLayout re_up = dialog_status.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_status.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_status.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.vazyate1);

        txt_category_1 = dialog_status.findViewById(R.id.txt_category_1);
        txt_scour_1 = dialog_status.findViewById(R.id.txt_scour_1);
        txt_category_2 = dialog_status.findViewById(R.id.txt_category_2);
        txt_scour_2 = dialog_status.findViewById(R.id.txt_scour_2);
        txt_category_3 = dialog_status.findViewById(R.id.txt_category_3);
        txt_scour_3 = dialog_status.findViewById(R.id.txt_scour_3);
        txt_category_4 = dialog_status.findViewById(R.id.txt_category_4);
        txt_scour_4 = dialog_status.findViewById(R.id.txt_scour_4);
        txt_category = dialog_status.findViewById(R.id.txt_category);
        txt_scour_all_2 = dialog_status.findViewById(R.id.txt_scour);

        GetDate();

        TextView txt_title = dialog_status.findViewById(R.id.txt_title);
        txt_title.setText(App.con.getResources().getString(R.string.status));

        Button btnDashboard = dialog_status.findViewById(R.id.btnDashboard);
        ImageView img_close = dialog_status.findViewById(R.id.img_close);
        ImageView img_back = dialog_status.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_status,re_up);
            }
        });

        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound.PlayClick();
                CloseDialog(dialog_status,re_up);
                if (prefs.GetUsername().equals("")) {
                    new DialogRegister(act,"main",((Main) act).googleHelper);
                }else {
                    Intent intent = new Intent(act, Dashboard.class);
                    act.startActivity(intent);
                }
            }
        });


        int w = act.getResources().getDisplayMetrics().widthPixels;
        dialog_status.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_status.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 100);
    }

    public  void DialogSeting()
    {
        dialog_seting = new Dialog(act);
        dialog_seting.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_seting.setContentView(R.layout.dialog_seting);
        dialog_seting.setCancelable(false);
        dialog_seting.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog_seting.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_seting.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_seting.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.tanzimatseyf1);

        ImageView img_back = dialog_seting.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);

        Spinner spinner_music = dialog_seting.findViewById(R.id.spinner_music);
        TextView txt_title = dialog_seting.findViewById(R.id.txt_title);
        TextView txt_vlome = dialog_seting.findViewById(R.id.txt_vlome);
        TextView txt_talker = dialog_seting.findViewById(R.id.txt_talker);
        TextView txt_music = dialog_seting.findViewById(R.id.txt_music);
        txt_music.setText(App.con.getResources().getString(R.string.music));
        txt_title.setText(App.con.getResources().getString(R.string.seting));
        txt_vlome.setText(App.con.getResources().getString(R.string.vloum));
        txt_talker.setText(App.con.getResources().getString(R.string.talker));



        CheckBox checkbox = dialog_seting.findViewById(R.id.checkbox);
        CheckBox checkbox_music = dialog_seting.findViewById(R.id.checkbox_music);

        if (prefs.GetTalker()){
            checkbox.setChecked(true);
        }else {
            checkbox.setChecked(false);
        }

        if (prefs.GetMusic()){
            checkbox_music.setChecked(true);
        }else {
            checkbox_music.setChecked(false);
        }

        checkbox_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (checkbox_music.isChecked()){
                    prefs.SaveMusic(true);
                    sound.StartMusic();
                }else {
                    prefs.SaveMusic(false);
                    SoundPlayer.StopMusic();
                }
            }
        });

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (checkbox.isChecked()){
                    prefs.SaveTalker(true);
                }else {
                    prefs.SaveTalker(false);
                }
            }
        });
        SeekBar seekbar = dialog_seting.findViewById(R.id.seekbar);
        int vo = (int) (prefs.GetVloume() * 10);
        seekbar.setProgress(vo);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float ss = (i / 10.0f);
                prefs.SetVloume(ss);
                SoundPlayer.Volume(prefs.GetVloume());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ImageView img_close = dialog_seting.findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_seting,re_up);
            }
        });


        spinnerState(spinner_music);

        int w = act.getResources().getDisplayMetrics().widthPixels;
        dialog_seting.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_seting.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 100);
    }

    private void spinnerState(Spinner spinner_music) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                act,
                R.layout.custom_spiner,
                musicTitles
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_music.setAdapter(adapter);

        spinner_music.setSelection(prefs.getMusicCounter());


        spinner_music.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                prefs.saveMusicCounter(position);
                sound.StartMusic();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void SetLangouch(String str)
    {
        prefs.SetLang(str);
        App.setLocale();
        act.finish();
        Intent i = new Intent(act, Main.class);
        act.startActivity(i);
    }

    public  void DialoLang()
    {
        dialog_lang = new Dialog(act);
        dialog_lang.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_lang.setContentView(R.layout.custom_language);
        dialog_lang.setCancelable(false);
        dialog_lang.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog_lang.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_lang.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_lang.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.zabanseyf);

        TextView txt_title = dialog_lang.findViewById(R.id.txt_title);
        txt_title.setText(App.con.getResources().getString(R.string.select_language));

        ImageView img_back = dialog_lang.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        ImageView img_close = dialog_lang.findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_lang,re_up);
            }
        });

        TextView txt_en = dialog_lang.findViewById(R.id.txt_en);
        txt_en.setText(App.con.getResources().getString(R.string.en));
        txt_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                SetLangouch("en");
                dialog_lang.dismiss();
            }
        });
        TextView txt_fa = dialog_lang.findViewById(R.id.txt_fa);
        txt_fa.setText(App.con.getResources().getString(R.string.fa));
        txt_fa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                SetLangouch("fa");
                dialog_lang.dismiss();
            }
        });


        int w = act.getResources().getDisplayMetrics().widthPixels;
        dialog_lang.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_lang.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 100);
    }

    public  void DialogAbout()
    {
        dialog_about = new Dialog(act);
        dialog_about.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_about.setContentView(R.layout.dialog_about);
        dialog_about.setCancelable(false);
        dialog_about.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog_about.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_about.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_about.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.darbaremaseyf);

        TextView txt_about = dialog_about.findViewById(R.id.txt_about);
        txt_about.setText(prefs.GetAbout());
        CardView card_telegram = dialog_about.findViewById(R.id.card_telegram);
        if (prefs.GetAboutTelegram().equals("")){
            card_telegram.setVisibility(View.GONE);
        }
        card_telegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(prefs.GetAboutTelegram()));
                act.startActivity(i);
            }
        });
        CardView card_insta = dialog_about.findViewById(R.id.card_insta);
        if (prefs.GetAboutInsta().equals("")){
            card_insta.setVisibility(View.GONE);
        }
        card_insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Uri uri = Uri.parse(prefs.GetAboutInsta());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.instagram.android");
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(prefs.GetAboutInsta())));
                }
            }
        });
        CardView card_phone = dialog_about.findViewById(R.id.card_phone);
        if (prefs.GetAboutPhone().equals("")){
            card_phone.setVisibility(View.GONE);
        }
        card_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+prefs.GetAboutPhone()));
                act.startActivity(intent);
            }
        });
        CardView card_email = dialog_about.findViewById(R.id.card_email);
        if (prefs.GetEmailSupport().equals("")){
            card_email.setVisibility(View.GONE);
        }
        card_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", prefs.GetEmailSupport(), null));
                act.startActivity(Intent.createChooser(intent, "Select Mail"));
            }
        });

        ImageView img_back = dialog_about.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        ImageView img_close = dialog_about.findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_about,re_up);
            }
        });
        TextView txt_version = dialog_about.findViewById(R.id.txt_version);
        txt_version.setText(App.VERSIONNAME);
        TextView txt_title = dialog_about.findViewById(R.id.txt_title);
        txt_title.setText(App.con.getResources().getString(R.string.about));


        int w = act.getResources().getDisplayMetrics().widthPixels;
        dialog_about.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_about.show();

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

    public void SetCoin()
    {
        txt_coin.setText(String.valueOf(prefs.GetCoin()));
    }



    public void ShowMenu()
    {
        sound.PlayClick();
        if (prefs.GetName().equals("")){
            txt_name.setText(App.con.getResources().getString(R.string.guest));
        }else {
            txt_name.setText(prefs.GetName());
        }
        if (!prefs.GetAvatar().equals("")){
            Glide.with(act)
                    .load(prefs.GetAvatar())
                    .into(img_profile);
        }else {
            Glide.with(act)
                    .load(R.drawable.logo)
                    .into(img_profile);
        }

        SetBadge();

        txt_scour.setText(String.valueOf(prefs.GetScour()));
        re_menu.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideInLeft)
                .duration(500)
                .repeat(0)
                .playOn(re_menu);
    }

    private void SetBadge()
    {
        String bad = prefs.GetBadge();
        if (!bad.equals("")){
            String[] str = bad.split(",");
            for (int i=0; i<str.length; i++){
                CustomBadge customBadge = new CustomBadge(act);
                customBadge.card_badge.setVisibility(View.GONE);
                customBadge.img_badge.setImageResource(Integer.parseInt(str[i]));
                li_badges.addView(customBadge);
            }
        }
    }

    public void CloseMenu()
    {
        sound.PlayClick();
        YoYo.with(Techniques.SlideOutLeft)
                .duration(500)
                .repeat(0)
                .playOn(re_menu);

        new Handler().postDelayed(() -> {
            re_menu.setVisibility(View.GONE);
        }, 500);
    }


    public void SetAnimCoin()
    {
        timer_coin = new Timer();
        timer_coin.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                act.runOnUiThread(() -> {
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .repeat(0)
                            .playOn(img_coin);
                });
            }
        }, 5000, 10000);
    }

    public void SetTimerCansel()
    {
        if (timer_coin != null) {
            timer_coin.cancel();
        }
    }

    public void DialoFrinds()
    {
        Intent shairIntent = new Intent(Intent.ACTION_SEND);
        shairIntent.setType("text/plain");
        String shareBody = App.con.getResources().getString(R.string.app_Tozih_1) + " " + prefs.GetCode() + " " + App.con.getResources().getString(R.string.app_Tozih_2);
        shairIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        act.startActivity(Intent.createChooser(shairIntent, App.con.getResources().getString(R.string.share_title)));
        /*BitmapDrawable drawable = (BitmapDrawable) img_profile.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        String bit_path = MediaStore.Images.Media.insertImage(act.getContentResolver(),bitmap,"title",null);
        Uri uri = Uri.parse(bit_path);
        String shareBody = App.con.getResources().getString(R.string.app_Tozih_1) + " " + prefs.GetCode() + " " + App.con.getResources().getString(R.string.app_Tozih_2);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");
        act.startActivity(Intent.createChooser(shareIntent, App.con.getResources().getString(R.string.share_title)));*/
    }


}//class
