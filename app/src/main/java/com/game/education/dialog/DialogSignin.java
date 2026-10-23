package com.game.education.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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

public class DialogSignin {

    private static final String TAG = "DIALOG_SIGNIN";
    private static final int MAX_BACKUP_RETRY = 2;

    private final Dialog dialog_signin;
    private final Prefs prefs;
    private final SoundPlayer sound;
    private final Menu menu;
    private final ApiInterface apiInterface;
    private final Activity activity;
    private int backupRetryCount = 0;

    public DialogSignin(Activity activity, int co, String type, GoogleLoginHelper googleHelper) {
        this.activity = activity;
        this.prefs = new Prefs(activity);
        this.sound = new SoundPlayer(activity);
        this.menu = new Menu(activity);
        this.apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);

        dialog_signin = new Dialog(activity);
        dialog_signin.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_signin.setContentView(R.layout.dialog_signin);
        dialog_signin.setCancelable(false);
        if (dialog_signin.getWindow() != null) {
            dialog_signin.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView txt_title = dialog_signin.findViewById(R.id.txt_title);
        txt_title.setText(co == 1 ? activity.getString(R.string.signup) : activity.getString(R.string.signin));

        ImageView img_back = dialog_signin.findViewById(R.id.img_back);
        img_back.setOnClickListener(v -> {
            hideKeyboard();
            dismiss();
            new DialogRegister(activity, type, googleHelper);
        });

        RelativeLayout re_up = dialog_signin.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);
        BlurImageView img_background = dialog_signin.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_signin.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.profileseyf);
        ImageView img_close = dialog_signin.findViewById(R.id.img_close);
        img_close.setVisibility(View.GONE);
        img_close.setOnClickListener(view -> {
            sound.PlayClick();
            hideKeyboard();
            CloseDialog(re_up);
        });

        EditText edt_code = dialog_signin.findViewById(R.id.edt_code);
        edt_code.setHint(activity.getString(R.string.code_moaref));
        if (co != 1) edt_code.setVisibility(View.GONE);

        EditText edt_email = dialog_signin.findViewById(R.id.edt_email);
        edt_email.setHint(activity.getString(R.string.username));
        EditText edt_pass = dialog_signin.findViewById(R.id.edt_pass);
        edt_pass.setHint(activity.getString(R.string.password));
        Button btn_signin = dialog_signin.findViewById(R.id.btn_signin);
        btn_signin.setText(activity.getString(R.string.login_register));

        btn_signin.setOnClickListener(view -> {
            String username = edt_email.getText().toString().trim();
            String pass = edt_pass.getText().toString().trim();

            if (username.length() < 4) {
                AdsTapsell.Dialog("نام کاربری حداقل باید ۴ کاراکتر باشد", activity);
                return;
            }
            if (pass.length() < 5) {
                AdsTapsell.Dialog(activity.getResources().getString(R.string.no_pass), activity);
                return;
            }

            hideKeyboard();
            sound.PlayClick();
            btn_signin.setEnabled(false);

            if (co == 1) {
                Register(username, pass, edt_code.getText().toString().trim(), type, googleHelper, btn_signin);
            } else {
                Login(username, pass, type, googleHelper, btn_signin);
            }
        });

        if (dialog_signin.getWindow() != null) {
            int w = activity.getResources().getDisplayMetrics().widthPixels;
            dialog_signin.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (activityAlive()) {
            dialog_signin.show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!activityAlive()) return;
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn).duration(500).repeat(0).playOn(re_up);
        }, 100);
    }

    private boolean activityAlive() {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void hideKeyboard() {
        try {
            View view = dialog_signin.getCurrentFocus();
            if (view == null && activity != null) view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ignored) { }
    }

    private void CloseDialog(RelativeLayout re_up) {
        YoYo.with(Techniques.ZoomOut).duration(400).repeat(0).playOn(re_up);
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 350);
    }

    public void dismiss() {
        try {
            if (dialog_signin != null && dialog_signin.isShowing()) dialog_signin.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Dismiss dialog error: " + e.getMessage());
        }
    }

    public void Register(String username, String pass, String code, String type,
                         GoogleLoginHelper googleHelper, Button actionBtn) {
        Call<ServerData.Message> call = apiInterface.register(username, pass, code);
        call.enqueue(new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response) {
                if (actionBtn != null) actionBtn.setEnabled(true);
                if (!activityAlive()) return;

                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess()) && body.getResult() != null) {
                        ServerData.Result res = body.getResult();
                        prefs.SetUsername(username);
                        prefs.SetPass(pass);
                        prefs.SaveGoogle(false);
                        if (res.getAffiliateCode() != null) prefs.SaveCode(res.getAffiliateCode());
                        if (res.getToken() != null) prefs.SetToken(res.getToken());
                        dismiss();
                        changeCoin(App.Register, "add", type);
                        return;
                    } else {
                        // نمایش امن خطای دریافتی از سرور (مثل Internal Error) بدون کرش
                        String errorMsg = body.getError();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "خطا در ثبت‌نام. ممکن است نام کاربری تکراری باشد.";
                        }
                        AdsTapsell.Dialog(errorMsg, activity);
                        return;
                    }
                }

                if (response.code() == 401) {
                    prefs.SetUsername("");
                    dismiss();
                    new DialogRegister(activity, type, googleHelper);
                    AdsTapsell.Dialog(activity.getString(R.string.login_token), activity);
                } else {
                    AdsTapsell.Dialog("خطای ارتباط با سرور (کد " + response.code() + ")", activity);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                if (actionBtn != null) actionBtn.setEnabled(true);
                Log.e(TAG, "Register failure: " + (t != null ? t.getMessage() : "unknown"));
                if (activityAlive()) AdsTapsell.Dialog(activity.getString(R.string.internet), activity);
            }
        });
    }

    public void Login(String username, String pass, String type,
                      GoogleLoginHelper googleHelper, Button actionBtn) {
        Call<ServerData.Message> call = apiInterface.login(username, pass);
        call.enqueue(new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response) {
                if (actionBtn != null) actionBtn.setEnabled(true);
                if (!activityAlive()) return;

                if (response.isSuccessful() && response.body() != null) {
                    ServerData.Message body = response.body();
                    if (Boolean.TRUE.equals(body.getSuccess())) {
                        prefs.SetUsername(username);
                        prefs.SetPass(pass);
                        prefs.SaveGoogle(false);
                        if (body.getResult() != null) {
                            if (body.getResult().getAffiliateCode() != null)
                                prefs.SaveCode(body.getResult().getAffiliateCode());
                            if (body.getResult().getToken() != null)
                                prefs.SetToken(body.getResult().getToken());
                        }
                        if ("main".equals(type) && activity instanceof Main) {
                            Main mainActivity = (Main) activity;
                            if (mainActivity.li_register != null) {
                                mainActivity.li_register.setVisibility(View.GONE);
                            }
                        }

                        dismiss();
                        AdsTapsell.Dialog(activity.getString(R.string.login), activity);
                        GetBackupInfo(googleHelper);
                        return;
                    } else {
                        String errorMsg = body.getError();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "نام کاربری یا رمز عبور نامعتبر است";
                        }
                        AdsTapsell.Dialog(errorMsg, activity);
                        return;
                    }
                }

                if (response.code() == 401) {
                    prefs.SetUsername("");
                    dismiss();
                    new DialogRegister(activity, type, googleHelper);
                    AdsTapsell.Dialog(activity.getString(R.string.login_token), activity);
                } else {
                    AdsTapsell.Dialog("نام کاربری یا رمز عبور اشتباه است (کد " + response.code() + ")", activity);
                    Log.e(TAG, "Login failed code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                if (actionBtn != null) actionBtn.setEnabled(true);
                Log.e(TAG, "Login Network Failed: " + (t != null ? t.getMessage() : "unknown"));
                if (activityAlive()) AdsTapsell.Dialog(activity.getString(R.string.internet), activity);
            }
        });
    }

    public void changeCoin(int amount, String typeCoin, String type) {
        CoinManager.addCoin(activity, amount, typeCoin, new CoinManager.CoinCallback() {
            @Override
            public void onSuccess() {
                if (!activityAlive()) return;
                AdsTapsell.Dialog(activity.getString(R.string.register), activity);
                menu.SetCoin();
                sound.PlayHint();

                // بررسی امن اکتیویتی و ویو
                if ("main".equals(type) && activity instanceof Main) {
                    Main mainActivity = (Main) activity;
                    if (mainActivity.li_register != null) {
                        mainActivity.li_register.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Coin change error: " + error);
            }
        });
    }
        public void GetBackupInfo(GoogleLoginHelper googleHelper) {
        String token = prefs.GetToken();
        if (token == null || token.isEmpty()) return;

        Call<ServerData.Message> call = apiInterface.backup_get(App.Token + token);
        call.enqueue(new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response) {
                if (!activityAlive()) return;

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
                                String[] levels = res.getLevel().split(",");
                                String[] scores = res.getScour_level().split(",");
                                int limit = Math.min(levels.length, scores.length);
                                for (int i = 0; i < limit; i++) {
                                    if (!levels[i].trim().isEmpty() && !scores[i].trim().isEmpty()) {
                                        prefs.SetLevel(levels[i].trim(), Integer.parseInt(scores[i].trim()));
                                        prefs.SaveTitleLevel(levels[i].trim());
                                    }
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parsing level backup: " + ex.getMessage());
                            }
                        }
                        menu.SetCoin();
                    }
                } else if (response.code() == 401) {
                    prefs.SetUsername("");
                    dismiss();
                    new DialogRegister(activity, "main", googleHelper);
                    AdsTapsell.Dialog(activity.getString(R.string.login_token), activity);
                } else {
                    retryBackupOrGiveUp(googleHelper);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(TAG, "Backup failure: " + t.getMessage(), t);
                retryBackupOrGiveUp(googleHelper);
            }
        });
    }

    private void retryBackupOrGiveUp(GoogleLoginHelper googleHelper) {
        if (!activityAlive()) return;
        if (backupRetryCount < MAX_BACKUP_RETRY) {
            backupRetryCount++;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (activityAlive()) GetBackupInfo(googleHelper);
            }, 2000);
        } else {
            AdsTapsell.Dialog(activity.getString(R.string.internet), activity);
        }
    }
}
