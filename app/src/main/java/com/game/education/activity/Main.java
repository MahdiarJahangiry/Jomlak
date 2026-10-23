package com.game.education.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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
import com.game.education.model.AdsTapsell;
import com.game.education.model.CoinManager;
import com.game.education.model.GoogleLoginHelper;
import com.game.education.model.Menu;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    public GoogleLoginHelper googleHelper;
    public LinearLayout li_register;

    private Prefs prefs;
    private Menu menu;
    private SoundPlayer sound;
    private AdsTapsell adsTapsell;
    private ApiInterface apiInterface;
    private TextView img_video;
    private TextView txt_register;

    private int backupRetryCount = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ۱. ابتدا setContentView صدا زده می‌شود تا DecorView تشکیل شود
        setContentView(R.layout.activity_main);

        // ۲. حالا بدون هیچ خطایی فول‌اسکرین اعمال می‌شود
        applyFullScreen();

        initDependencies();
        initViews();
        setupBackNavigation();

        sound.StartMusic();

        GetAbout();
        GetVersion();
    }


    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }

    private void initDependencies() {
        prefs = new Prefs(this);
        menu = new Menu(this);
        sound = new SoundPlayer(this);
        adsTapsell = new AdsTapsell(this);
        apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);

        googleHelper = new GoogleLoginHelper(this, new GoogleLoginHelper.LoginListener() {
            @Override
            public void onSuccess(GoogleSignInAccount account) {
                if (account != null && account.getEmail() != null) {
                    LoginGoogle(account.getEmail(), account.getDisplayName(), App.Pass);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(Main.this, "ورود ناموفق با گوگل", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        img_video = findViewById(R.id.img_video);
        txt_register = findViewById(R.id.txt_register);


        TextView txt_frinds = findViewById(R.id.txt_frinds);
        if (txt_frinds != null) {
            txt_frinds.setText(getString(R.string.frinds_main));
        }

        LinearLayout li_ads = findViewById(R.id.li_ads);
        if (li_ads != null) {
            li_ads.setOnClickListener(v -> {
                sound.PlayClick();
                startActivity(new Intent(Main.this, Shop.class));
            });
        }

        if (img_video != null) {
            img_video.setOnClickListener(v -> {
                sound.PlayClick();
                adsTapsell.GetVideo();
            });
        }

           LinearLayout li_frinds = findViewById(R.id.li_frinds);
        if (li_frinds != null) {
            li_frinds.setOnClickListener(v -> {
                sound.PlayClick();
                if (!prefs.GetEmail().isEmpty()) {
                    DialoFrinds();
                } else {
                    AdsTapsell.Dialog(getString(R.string.email_verified), Main.this);
                }
            });
        }

        ImageView img_play = findViewById(R.id.img_play);
        // گیت احراز هویت: جلوگیری از ورود بدون لاگین یا ثبت نام
        img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound.PlayClick();
                if (prefs.GetUsername().trim().isEmpty() || prefs.GetToken().trim().isEmpty()) {
                    Toast.makeText(Main.this, "لطفاً ابتدا وارد حساب کاربری خود شوید", Toast.LENGTH_SHORT).show();
                    new DialogRegister(Main.this, "main", googleHelper);
                } else {
                    Intent intent = new Intent(Main.this, Titles.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        LinearLayout li_bot = findViewById(R.id.li_bot);
        if (li_bot != null) {
            li_bot.setOnClickListener(v -> {
                sound.PlayClick();
                startActivity(new Intent(Main.this, BotActivity.class));
            });
        }
    }

    private void SaveBackupInfo() {
        String titleLevels = prefs.GetTitleLevel();
        StringBuilder scourLevelBuilder = new StringBuilder();

        if (titleLevels != null && !titleLevels.isEmpty()) {
            String[] str = titleLevels.split(",");
            for (int i = 0; i < str.length; i++) {
                if (i > 0) {
                    scourLevelBuilder.append(",");
                }
                scourLevelBuilder.append(prefs.GetLevel(str[i]));
            }
        }

        SaveBackup saveBackup = new SaveBackup(
                prefs.GetCoin(),
                prefs.GetScour(),
                prefs.GetScourOne(),
                prefs.GetScourTow(),
                prefs.GetScourTree(),
                prefs.GetScourFour(),
                prefs.GetName(),
                prefs.GetLang(),
                prefs.GetCode(),
                prefs.GetTitleLevel(),
                scourLevelBuilder.toString(),
                prefs.GetPath(),
                prefs.GetAvatar()
        );

        Call<ServerData.Message> call = apiInterface.backup_save(App.Token + prefs.GetToken(), saveBackup);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "SaveBackupInfo failed: " + t.getMessage());
            }
        });
    }

    void GetVersion() {
        Call<ServerData.Message> call = apiInterface.version();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess()) && body.getResult() != null && body.getResult().getVersion() != null) {
                        try {
                            if (Integer.parseInt(body.getResult().getVersion()) > App.VERSIONCODE) {
                                DialogVersion();
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "GetVersion failed: " + t.getMessage());
            }
        });
    }

    void GetAffiliate() {
        Call<ServerData.Message> call = apiInterface.init_affiliate(App.Token + prefs.GetToken());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess()) && body.getResult() != null && body.getResult().getItems() != null) {
                        int notUsed = body.getResult().getItems().getNot_used_affiliate();
                        if (notUsed > 0) {
                            int af = notUsed * App.Davat;
                            GetInit(af);
                        }
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "GetAffiliate failed: " + t.getMessage());
            }
        });
    }

    void GetInit(int af) {
        Call<ServerData.Message> call = apiInterface.affiliate(App.Token + prefs.GetToken());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (Boolean.TRUE.equals(response.body().getSuccess())) {
                        changeCoin(af, "add");
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "GetInit failed: " + t.getMessage());
            }
        });
    }

    public void changeCoin(int amount, String type) {
        CoinManager.addCoin(this, amount, type, new CoinManager.CoinCallback() {
            @Override
            public void onSuccess() {
                menu.SetCoin();
                AdsTapsell.Dialog(getString(R.string.affiliate), Main.this);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "CoinManager error: " + error);
            }
        });
    }

    void GetAbout() {
        Call<ServerData.Message> call = apiInterface.about();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess()) && body.getResult() != null && body.getResult().getItems() != null) {
                        List<ServerData.About> aboutList = body.getResult().getItems().getAbout();
                        if (aboutList != null) {
                            for (int i = 0; i < aboutList.size(); i++) {
                                ServerData.About item = aboutList.get(i);
                                if (item != null && item.getValue() != null) {
                                    String val = item.getValue();
                                    switch (i) {
                                        case 0:
                                            try {
                                                if (Integer.parseInt(val) > App.VERSIONCODE) {
                                                    DialogVersion();
                                                }
                                            } catch (NumberFormatException ignored) {}
                                            break;
                                        case 1:
                                            prefs.SaveAboutTelegram(val);
                                            break;
                                        case 2:
                                            prefs.SaveAboutInsta(val);
                                            break;
                                        case 3:
                                            prefs.SaveEmailSupport(val);
                                            break;
                                        case 4:
                                            prefs.SaveAboutPhone(val);
                                            break;
                                        case 5:
                                            prefs.SaveAbout(val);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "GetAbout failed: " + t.getMessage());
            }
        });
    }

    public void DialogVersion() {
        if (isFinishing() || isDestroyed()) return;

        Dialog dialog_ver = new Dialog(this);
        dialog_ver.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_ver.setContentView(R.layout.dialog_version);
        dialog_ver.setCancelable(false);

        if (dialog_ver.getWindow() != null) {
            dialog_ver.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RelativeLayout re_up = dialog_ver.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_ver.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);

        ImageView img_title = dialog_ver.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.logo);

        TextView txt_dec = dialog_ver.findViewById(R.id.txt_dec);
        TextView txt_title_view = dialog_ver.findViewById(R.id.txt_title);
        txt_dec.setText(getString(R.string.new_version_title));
        txt_title_view.setText(getString(R.string.new_version));

        ImageView img_back = dialog_ver.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);

        ImageView img_close = dialog_ver.findViewById(R.id.img_close);
        img_close.setOnClickListener(v -> {
            sound.PlayClick();
            CloseDialog(dialog_ver, re_up);
        });

        TextView txt_fa = dialog_ver.findViewById(R.id.txt_fa);
        txt_fa.setText(getString(R.string.update));
        txt_fa.setOnClickListener(v -> {
            sound.PlayClick();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.url_app)));
            startActivity(i);
        });

        if (dialog_ver.getWindow() != null) {
            int w = getResources().getDisplayMetrics().widthPixels;
            dialog_ver.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog_ver.show();

        mainHandler.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                re_up.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn)
                        .duration(500)
                        .repeat(0)
                        .playOn(re_up);
            }
        }, 100);
    }

    public void DialoFrinds() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = getString(R.string.app_Tozih_1) + " " + prefs.GetCode() + " " + getString(R.string.app_Tozih_2);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }

    private void CloseDialog(Dialog dialog, RelativeLayout re_up) {
        YoYo.with(Techniques.ZoomOut)
                .duration(500)
                .repeat(0)
                .playOn(re_up);
        mainHandler.postDelayed(() -> {
            try {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception ignored) {}
        }, 400);
    }

    private void LoginGoogle(String email, String username, String pass) {
        Call<ServerData.Message> call = apiInterface.login_email(email, pass);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess())) {
                        prefs.SetEmail(email);
                        prefs.SetUsername(username);
                        prefs.SetPass(pass);
                        prefs.SaveGoogle(true);
                        prefs.SaveEmailVerified(true);
                        if (body.getResult() != null) {
                            if (body.getResult().getAffiliateCode() != null) {
                                prefs.SaveCode(body.getResult().getAffiliateCode());
                            }
                            if (body.getResult().getToken() != null) {
                                prefs.SetToken(body.getResult().getToken());
                            }
                        }
                        GetBackupInfo();
                    } else {
                        AdsTapsell.Dialog(body.getError() != null ? body.getError() : "خطا در ورود", Main.this);
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                } else {
                    AdsTapsell.Dialog(getString(R.string.internet), Main.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "LoginGoogle failure: " + t.getMessage());
                AdsTapsell.Dialog(getString(R.string.internet), Main.this);
            }
        });
    }

    public void GetBackupInfo() {
        String token = prefs.GetToken();
        if (token == null || token.isEmpty()) return;

        Call<ServerData.Message> call = apiInterface.backup_get(App.Token + token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                backupRetryCount = 0;
                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess()) && body.getResult() != null) {
                        ServerData.Result res = body.getResult();
                        if (res.getScour_all() != null) prefs.SetScourAll(res.getScour_all());
                        if (res.getScour_one() != null) prefs.SetScourOne(res.getScour_one());
                        if (res.getScour_tow() != null) prefs.SetScourTow(res.getScour_tow());
                        if (res.getScour_tree() != null) prefs.SetScourTree(res.getScour_tree());
                        if (res.getScour_four() != null) prefs.SetScourFour(res.getScour_four());
                        if (res.getUser() != null) prefs.SetName(res.getUser());
                        if (res.getPath() != null) prefs.SavePath(res.getPath());
                        if (res.getAvatar() != null) prefs.SetAvatar(res.getAvatar());
                        if (res.getCode() != null) prefs.SaveCode(res.getCode());

                        if (res.getLevel() != null && res.getScour_level() != null) {
                            try {
                                String[] str = res.getLevel().split(",");
                                String[] scour = res.getScour_level().split(",");
                                int limit = Math.min(str.length, scour.length);
                                for (int i = 0; i < limit; i++) {
                                    prefs.SetLevel(str[i].trim(), Integer.parseInt(scour[i].trim()));
                                    prefs.SaveTitleLevel(str[i].trim());
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Level parsing error: " + ex.getMessage());
                            }
                        }
                        menu.SetCoin();
                        AdsTapsell.Dialog(getString(R.string.login), Main.this);
                    } else {
                        AdsTapsell.Dialog(body.getError(), Main.this);
                    }
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "GetBackupInfo failed: " + t.getMessage());
                backupRetryCount++;
                if (backupRetryCount <= 3 && !isFinishing() && !isDestroyed()) {
                    mainHandler.postDelayed(() -> GetBackupInfo(), 2000);
                } else {
                    AdsTapsell.Dialog(getString(R.string.internet), Main.this);
                }
            }
        });
    }

    public void getBalance() {
        String token = prefs.GetToken();
        if (token == null || token.isEmpty()) return;

        Call<ServerData.Message> call = apiInterface.getBalance(App.Token + token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess()) && body.getResult() != null && body.getResult().getItems() != null) {
                        if (body.getResult().getItems().getBalance() != null) {
                            prefs.SetCoin(body.getResult().getItems().getBalance());
                        }
                        menu.SetCoin();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {
                Log.e(TAG, "getBalance failed: " + t.getMessage());
            }
        });
    }

    private void handleUnauthorized() {
        prefs.SetUsername("");
        if (!isFinishing() && !isDestroyed()) {
            new DialogRegister(Main.this, "main", googleHelper);
            AdsTapsell.Dialog(getString(R.string.login_token), Main.this);
        }
    }

    private void applyFullScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (getWindow() != null && getWindow().getDecorView() != null) {
                    getWindow().setDecorFitsSystemWindows(false);
                    WindowInsetsController controller = getWindow().getInsetsController();
                    if (controller != null) {
                        controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    }
                }
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                View decorView = getWindow().getDecorView();
                if (decorView != null) {
                    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);
                }
            }
        } catch (Exception ignored) {
            // جلوگیری از کرش سیستم‌عامل در دیوایس‌ها و رام‌های مختلف
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ۱. توابع صوتی و بصری اصلی منو و بازی (همیشه باید اجرا شوند)
        if (menu != null) {
            menu.SetAnimCoin();
            menu.SetCoin();
        }
        SoundPlayer.PlayMusic();

        // ۲. بررسی دقیق وضعیت لاگین کاربر (ترکیب امن نام کاربری و توکن)
        boolean isLoggedIn = prefs != null &&
                !prefs.GetUsername().trim().isEmpty() &&
                !prefs.GetToken().trim().isEmpty();

        // ۳. مدیریت وضعیت صفحه و درخواست‌های سرور بر اساس وضعیت لاگین
        if (isLoggedIn) {
            // اگر کاربر لاگین کرده است: اطلاعات پشتیبان، کدتخفیف و موجودی بروزرسانی شود
            SaveBackupInfo();
            GetAffiliate();
            getBalance();

            if (txt_register != null) {
                txt_register.setText(getString(R.string.signup));
            }
            if (li_register != null) {
                li_register.setVisibility(View.GONE); // مخفی کردن دکمه ثبت‌نام برای کاربر لاگین‌کرده
            }
        } else {
            // اگر کاربر هنوز لاگین نکرده است: دکمه ثبت‌نام نمایان و دیالوگ باز شود
            if (txt_register != null) {
                txt_register.setText(getString(R.string.register_main));
            }
            if (li_register != null) {
                li_register.setVisibility(View.VISIBLE); // نمایش دکمه ثبت‌نام
            }

            // گارد امنیتی Lifecycle برای جلوگیری از کرش BadTokenException هنگام ساخت دیالوگ
            if (!isFinishing() && !isDestroyed()) {
                new DialogRegister(Main.this, "main", googleHelper);
            }
        }

        // ۴. مدیریت بخش ویدیو/تبلیغات
        if (prefs != null && !prefs.GetAds() && img_video != null) {
            img_video.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        menu.SetTimerCansel();
        SoundPlayer.PauseMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        menu.SetTimerCansel();
        SoundPlayer.StopMusic();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        applyFullScreen();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullScreen();
        }
    }
}
