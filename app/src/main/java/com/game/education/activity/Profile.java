package com.game.education.activity;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.model.AdsTapsell;
import com.game.education.model.GoogleLoginHelper;
import com.game.education.model.Prefs;
import com.game.education.model.SoundPlayer;
import com.game.education.model.Tollbar;
import com.game.education.model.UpdateEmail;
import com.game.education.model.Verify;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jgabrielfreitas.core.BlurImageView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Profile extends AppCompatActivity {

    public GoogleLoginHelper googleHelper;
    Tollbar tol;
    RadioButton radio_1,radio_2,radio_3;
    TextView txt_username,txt_name,txt_pass,txt_display_name,txt_sure_name,txt_date,
            txt_gender,txt_man,txt_women,txt_sure,txt_age,txt_code,txt_email;
    private final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 50;
    private final int CAMERA = 60;
    CardView add_img;
    Dialog dialog_select;
    SoundPlayer sound;
    ImageView img_proile,img_title;
    File imageFile;
    Bitmap bitmap;
    Button btn_update;
    EditText edt_date,edt_username,edt_display_name,edt_pass,edt_name,edt_sure_name,edt_age,ed_code,edt_email,edtCodeEmail;
    Prefs prefs;
    String gender = "";
    ProgressDialog progressDialog;
    Button txt_logout,btnUpdateEmail;
    int count = 0;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private static final int BUFFER_SIZE = 1024 * 2;
    private boolean isVerifyEmail = false;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        FullScreen();
        tol = new Tollbar(this);
        sound = new SoundPlayer(this);
        prefs = new Prefs(this);
        tol.txt_title.setText(App.con.getResources().getString(R.string.profile));
        txt_logout = findViewById(R.id.txt_logout);
        txt_email = findViewById(R.id.txt_email);
        radio_1 = findViewById(R.id.radio_1);
        radio_2 = findViewById(R.id.radio_2);
        radio_3 = findViewById(R.id.radio_3);
        add_img = findViewById(R.id.add_img);
        img_proile = findViewById(R.id.img_proile);
        txt_username = findViewById(R.id.txt_username);
        txt_name = findViewById(R.id.txt_name);
        txt_pass = findViewById(R.id.txt_pass);
        txt_display_name = findViewById(R.id.txt_display_name);
        txt_sure_name = findViewById(R.id.txt_sure_name);
        txt_date = findViewById(R.id.txt_date);
        txt_gender = findViewById(R.id.txt_gender);
        txt_man = findViewById(R.id.txt_man);
        txt_women = findViewById(R.id.txt_women);
        txt_sure = findViewById(R.id.txt_sure);
        btn_update = findViewById(R.id.btn_update);
        edt_date = findViewById(R.id.edt_date);
        edt_username = findViewById(R.id.edt_username);
        edt_display_name = findViewById(R.id.edt_display_name);
        edt_pass = findViewById(R.id.edt_pass);
        edt_name = findViewById(R.id.edt_name);
        edt_sure_name = findViewById(R.id.edt_sure_name);
        txt_age = findViewById(R.id.txt_age);
        edt_age = findViewById(R.id.edt_age);
        txt_code = findViewById(R.id.txt_code);
        ed_code = findViewById(R.id.ed_code);
        edtCodeEmail = findViewById(R.id.edtCodeEmail);
        edt_email = findViewById(R.id.edt_email);
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnUpdateEmail.setVisibility(View.GONE);
        edtCodeEmail.setVisibility(View.GONE);
        edt_email.setEnabled(false);

        btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVerifyEmail){
                    String code = edtCodeEmail.getText().toString().trim();
                    if(!code.equals("")) {
                        verifyEmail();
                    }
                }else {
                    email = edt_email.getText().toString().trim();
                    if (email.contains("@")) {
                        UpdateEmail();
                    }else {
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.no_email), Profile.this);
                    }
                }
            }
        });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        if (data != null && data.hasExtra(ImagePicker.EXTRA_FILE_PATH)) {

                            Uri uri = data.getData();

                            if (uri != null) {

                                String pathPic = getFilePathFromURI(this, uri);
                                imageFile = new File(pathPic);

                                Glide.with(this)
                                        .load(imageFile)
                                        .into(img_proile);
                            }
                        }
                    }

                });

        BlurImageView img_background = findViewById(R.id.img_background);
        img_background.setImageResource(R.drawable.back_main);

        img_title = findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.profileseyf);

        googleHelper = new GoogleLoginHelper(this, new GoogleLoginHelper.LoginListener() {
            @Override
            public void onSuccess(GoogleSignInAccount account) {

            }

            @Override
            public void onError(Exception e) {

                Toast.makeText(Profile.this,
                        "خروج ناموفق",
                        Toast.LENGTH_SHORT).show();

            }
        });

        txt_logout.setText(App.con.getResources().getString(R.string.logout_menu));
        txt_code.setText(App.con.getResources().getString(R.string.code_davat));
        btn_update.setText(App.con.getResources().getString(R.string.edit));
        txt_username.setText(App.con.getResources().getString(R.string.username));
        txt_email.setText(App.con.getResources().getString(R.string.email));
        txt_display_name.setText(App.con.getResources().getString(R.string.name));
        txt_pass.setText(App.con.getResources().getString(R.string.password));
        txt_name.setText(App.con.getResources().getString(R.string.name_optinal));
        txt_sure_name.setText(App.con.getResources().getString(R.string.family_optinal));
        txt_date.setText(App.con.getResources().getString(R.string.date_optinal));
        edt_date.setHint(App.con.getResources().getString(R.string.hint_date));
        edtCodeEmail.setHint(App.con.getResources().getString(R.string.code_email));
        txt_gender.setText(App.con.getResources().getString(R.string.gender_optinal));
        txt_man.setText(App.con.getResources().getString(R.string.men));
        txt_women.setText(App.con.getResources().getString(R.string.women));
        txt_sure.setText(App.con.getResources().getString(R.string.not_sure));
        txt_age.setText(App.con.getResources().getString(R.string.age_optinal));
        btnUpdateEmail.setText(App.con.getResources().getString(R.string.update));

        txt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                SignOut();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                //if (sourceUri != null || !prefs.GetPath().equals("")) {
                UpdatePofile();
                /*}else {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.profile_pic),Profile.this);
                }*/
            }
        });

        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                DialogSelect();
            }
        });

        radio_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                radio_1.setChecked(true);
                radio_2.setChecked(false);
                radio_3.setChecked(false);
                gender = "man";
            }
        });

        radio_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                radio_2.setChecked(true);
                radio_1.setChecked(false);
                radio_3.setChecked(false);
                gender = "women";
            }
        });

        radio_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                radio_3.setChecked(true);
                radio_2.setChecked(false);
                radio_1.setChecked(false);
                gender = "sure";
            }
        });

        GetPofile();


    }//main

    private void SignOut()
    {
        googleHelper.signOut();
        AdsTapsell.Dialog(App.con.getResources().getString(R.string.logout), Profile.this);
        prefs.Clear();
        finish();
    }

    private void DialogWait()
    {
        progressDialog = new ProgressDialog(Profile.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(App.con.getResources().getString(R.string.please));
        progressDialog.show();
    }

    private void GetPofile()
    {
        DialogWait();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Data> call = apiInterface.profile(App.Token + prefs.GetToken());
        Callback<ServerData.Data> callback = new Callback<ServerData.Data>() {
            @Override
            public void onResponse(Call<ServerData.Data> call, Response<ServerData.Data> response)
            {
                if (response.code() == 200) {
                    if (response.body().getItems() != null) {
                        if (response.body().getItems().getAvatar() != null){
                            Glide.with(Profile.this)
                                    .load(response.body().getItems().getAvatar())
                                    .into(img_proile);
                        }
                        edt_username.setText(response.body().getItems().getUsername());
                        prefs.SaveEmailVerified(response.body().getItems().getEmail_verified());
                        if(!prefs.GetEmailVerified()){
                            edt_email.setEnabled(true);
                            btnUpdateEmail.setVisibility(View.VISIBLE);
                        }
                        if (response.body().getItems().getEmail() != null) {
                            edt_email.setText(response.body().getItems().getEmail());
                        }
                        if (response.body().getItems().getName() != null) {
                            edt_display_name.setText(response.body().getItems().getName());
                        }
                        if (response.body().getItems().getAbout_me() != null) {
                            edt_name.setText(response.body().getItems().getAbout_me());
                        }
                        if (response.body().getItems().getSurname() != null) {
                            edt_sure_name.setText(response.body().getItems().getSurname());
                        }
                        if (response.body().getItems().getBirthday() != null) {
                            edt_date.setText(response.body().getItems().getBirthday());
                        }
                        if (response.body().getItems().getAge() != null) {
                            edt_age.setText(response.body().getItems().getAge());
                        }
                        if (response.body().getItems().getAffiliate_code() != null){
                            ed_code.setText(response.body().getItems().getAffiliate_code());
                            prefs.SaveCode(response.body().getItems().getAffiliate_code());
                        }
                        if (!prefs.GetGoogle()) {
                            edt_pass.setText(prefs.GetPass());
                        }
                        if (response.body().getItems().getGender() != null) {
                            if (response.body().getItems().getGender().equals("man")) {
                                radio_1.setChecked(true);
                                radio_2.setChecked(false);
                                radio_3.setChecked(false);
                                gender = "man";
                            } else if (response.body().getItems().getGender().equals("women")) {
                                radio_1.setChecked(false);
                                radio_2.setChecked(true);
                                radio_3.setChecked(false);
                                gender = "women";
                            } else if (response.body().getItems().getGender().equals("sure")) {
                                radio_1.setChecked(false);
                                radio_2.setChecked(false);
                                radio_3.setChecked(true);
                                gender = "sure";
                            }
                        }

                    }
                }else if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Profile.this);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerData.Data> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                progressDialog.dismiss();
                count++;
                if(count > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Profile.this);
                }else {
                    GetPofile();
                }
            }
        };
        call.enqueue(callback);
    }

    private void UpdatePofile()
    {
        DialogWait();

        MultipartBody.Part logo = null;
        if (imageFile != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), imageFile);
            logo = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);
        }

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.profile_update(App.Token + prefs.GetToken(),edt_display_name.getText().toString(),
                edt_age.getText().toString(),logo,edt_name.getText().toString(),edt_sure_name.getText().toString(),edt_date.getText().toString(),gender,prefs.GetPass());
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        if (response.body().getResult().getItems().getAvatar() != null) {
                            prefs.SetAvatar(response.body().getResult().getItems().getAvatar());
                        }
                        prefs.SetName(response.body().getResult().getItems().getName());
                        AdsTapsell.Dialog(App.con.getResources().getString(R.string.profile_update), Profile.this);
                    }
                }else if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Profile.this);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                progressDialog.dismiss();
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Profile.this);
            }
        };
        call.enqueue(callback);
    }

    private void UpdateEmail()
    {
        DialogWait();

        UpdateEmail updateEmail = new UpdateEmail(email);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.updateEmail(App.Token + prefs.GetToken(),updateEmail);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        sendVerificationEmail();
                    }
                }else if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Profile.this);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                progressDialog.dismiss();
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Profile.this);
            }
        };
        call.enqueue(callback);
    }

    private void sendVerificationEmail()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.sendVerificationEmail(App.Token + prefs.GetToken());
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        AdsTapsell.Dialog("کد تایید به ایمیل شما ارسال شد",Profile.this);
                        edtCodeEmail.setVisibility(View.VISIBLE);
                        btnUpdateEmail.setText("ارسال کد");
                        isVerifyEmail = true;
                    }
                }else if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Profile.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Profile.this);
            }
        };
        call.enqueue(callback);
    }

    private void verifyEmail()
    {
        DialogWait();

        Verify verify = new Verify(edtCodeEmail.getText().toString().trim());

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.verify(App.Token + prefs.GetToken(),verify);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.code() == 200) {
                    if (response.body().getSuccess()) {
                        prefs.SaveEmailVerified(true);
                        prefs.SetEmail(email);
                        edtCodeEmail.setVisibility(View.GONE);
                        btnUpdateEmail.setVisibility(View.GONE);
                        edt_email.setEnabled(false);
                        isVerifyEmail = false;
                        AdsTapsell.Dialog("ایمیل تایید شد.",Profile.this);
                    }
                }else if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Profile.this);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                progressDialog.dismiss();
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Profile.this);
            }
        };
        call.enqueue(callback);
    }


    public  void DialogSelect()
    {
        dialog_select = new Dialog(this);
        dialog_select.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_select.setContentView(R.layout.dialog_select_avatar);
        dialog_select.setCancelable(false);
        dialog_select.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog_select.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        BlurImageView img_background = dialog_select.findViewById(R.id.img_background);
        img_background.setBlur(App.Blur);
        ImageView img_title = dialog_select.findViewById(R.id.img_title);
        img_title.setImageResource(R.drawable.taghirezabanseyf1);
        TextView txt_title = dialog_select.findViewById(R.id.txt_title);
        txt_title.setText(App.con.getResources().getString(R.string.select_pic));

        ImageView img_back = dialog_select.findViewById(R.id.img_back);
        img_back.setVisibility(View.GONE);
        ImageView img_close = dialog_select.findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_select,re_up);
            }
        });
        TextView txt_camera = dialog_select.findViewById(R.id.txt_camera);
        txt_camera.setText(App.con.getResources().getString(R.string.camera));
        txt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                galleryLauncher.launch(
                        ImagePicker.with(Profile.this)
                                .crop(512f, 512f)
                                .cameraOnly()
                                .maxResultSize(512, 512, true)
                                .setMultipleAllowed(false)
                                .galleryMimeTypes(new String[]{
                                        "image/*"
                                })
                                .createIntent()
                );
                CloseDialog(dialog_select,re_up);
            }
        });
        TextView txt_galery = dialog_select.findViewById(R.id.txt_galery);
        txt_galery.setText(App.con.getResources().getString(R.string.galery));
        txt_galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                galleryLauncher.launch(
                        ImagePicker.with(Profile.this)
                                .crop(512f, 512f)
                                .galleryOnly()
                                .maxResultSize(512, 512, true)
                                .setMultipleAllowed(false)
                                .galleryMimeTypes(new String[]{
                                        "image/png",
                                        "image/jpg",
                                        "image/jpeg"
                                })
                                .createIntent()
                );
                CloseDialog(dialog_select,re_up);
            }
        });


        int w = getResources().getDisplayMetrics().widthPixels;
        dialog_select.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_select.show();

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


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
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

    public String getFilePathFromURI(Context context, Uri contentUri) {

        String fileName = getFileName(contentUri);

        if (!TextUtils.isEmpty(fileName)) {

            File copyFile = new File(
                    context.getFilesDir(),
                    UUID.randomUUID().toString() + "_" + fileName
            );

            copy(context, contentUri, copyFile);

            return copyFile.getAbsolutePath();
        }

        return null;
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {

        if (uri == null)
            return null;

        String fileName = null;

        if (uri.toString().startsWith("content://")) {

            Cursor cursor = null;

            try {

                cursor = getContentResolver().query(uri,
                        null,
                        null,
                        null,
                        null);

                if (cursor != null && cursor.moveToFirst()) {

                    fileName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }

            } finally {

                if (cursor != null)
                    cursor.close();

            }

        } else if (uri.toString().startsWith("file://")) {

            File myFile = new File(uri.getPath());

            fileName = myFile.getName();

        }

        return fileName;
    }

    public void copy(Context context, Uri srcUri, File dstFile) {

        try {

            InputStream inputStream =
                    context.getContentResolver().openInputStream(srcUri);

            if (inputStream == null)
                return;

            OutputStream outputStream =
                    new FileOutputStream(dstFile);

            copyStream(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    public int copyStream(InputStream input,
                          OutputStream output)
            throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in =
                new BufferedInputStream(input, BUFFER_SIZE);

        BufferedOutputStream out =
                new BufferedOutputStream(output, BUFFER_SIZE);

        int count = 0;
        int n;

        try {

            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {

                out.write(buffer, 0, n);

                count += n;

            }

            out.flush();

        } finally {

            try {

                out.close();

            } catch (IOException e) {

                Log.e("CopyStream", e.getMessage(), e);

            }

            try {

                in.close();

            } catch (IOException e) {

                Log.e("CopyStream", e.getMessage(), e);

            }

        }

        return count;
    }

}//class