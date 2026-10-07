package com.game.education.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.game.education.App;
import com.game.education.R;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import com.game.education.model.AdsTapsell;
import com.game.education.model.Animations;
import com.game.education.model.CoinManager;
import com.game.education.model.CustomAnswer;
import com.game.education.model.CustomBlur;
import com.game.education.model.CustomShowAnswer;
import com.game.education.model.Item;
import com.game.education.model.LineView;
import com.game.education.model.Prefs;
import com.game.education.model.Sentence;
import com.game.education.model.SoundPlayer;
import com.jgabrielfreitas.core.BlurImageView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Game extends AppCompatActivity {

    public static int color = -1;
    Timer timer_coin;
    public List<Integer> array_color = new ArrayList<>();
    public List<Integer> array_drawble = new ArrayList<>();
    private LineView line_view;
    private RelativeLayout re_word,btn_change;
    private TextView btn_1,btn_2,btn_3,btn_4,btn_5,btn_6,btn_7,btn_8,txt_level,txt_coin;
    private TextView txt_check;
    private String text = "";
    private String question = "";
    private String answer = "";
    private List<TextView> array_btn;
    private List<String> array_ads_image;
    private List<String> array_ads_title;
    private List<String> array_word;
    private List<String> array_question;
    private List<String> array_pic;
    private List<String> array_voice;
    private List<String> array_voice_jomle;
    private List<String> array_author;
    private List<Float> array_x,array_y;
    private List<TextView> array_item;
    private List<CustomAnswer> array_answer;
    private int index = 0,count,level,score,page;
    private LinearLayout linear_answer,linear_show_answer,li_ads,btn_hint;
    private LinearLayout re_blur;
    private Prefs prefs;
    private List<Integer> array_count;
    private List<Integer> array_id;
    private float target_x,target_y;
    public SoundPlayer sound;
    private String[] qu;
    private LottieAnimationView gift;
    int id,id_level,level_category,count_1 = 0;
    NestedScrollView scrollView;
    ImageView img_back,img_coin;
    CustomBlur customBlur;
    MediaPlayer mp,mp_2;
    Dialog dialog_report;
    ImageView img_ads,img_hint;
    ImageView img_logo;
    TextView img_video;
    TextView txt_ads;
    CardView card_ads;
    AdsTapsell adsTapsell;
    View btnMagnet;
    public int magnetIndex = 0;
    public int checkIndex = 0;
    private boolean magnetEnable = false;
    private boolean isFirst = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        FullScreen();
        SetRandomTheme();

        prefs = new Prefs(this);
        sound = new SoundPlayer(this);

        Bundle bundle=getIntent().getExtras();
        id = bundle.getInt("id");
        id_level = bundle.getInt("id_level");
        level_category = bundle.getInt("level_category");
        page = bundle.getInt("page");

        gift = findViewById(R.id.gift);


        line_view = findViewById(R.id.line_view);
        line_view.activity = this;
        line_view.gift = gift;

        adsTapsell = new AdsTapsell(this);
        img_video = findViewById(R.id.img_video);
        img_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                adsTapsell.GetVideo();
            }
        });

        btnMagnet = findViewById(R.id.btnMagnet);
        card_ads = findViewById(R.id.card_ads);
        txt_ads = findViewById(R.id.txt_ads);
        img_ads = findViewById(R.id.img_ads);
        img_logo = findViewById(R.id.img_logo);
        scrollView = findViewById(R.id.scrollView);
        re_blur = findViewById(R.id.re_blur);
        li_ads = findViewById(R.id.li_ads);
        li_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(Game.this,Shop.class);
                startActivity(i);
            }
        });
        img_coin = findViewById(R.id.img_coin);
        txt_coin = findViewById(R.id.txt_coin);
        img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                Intent i = new Intent(Game.this,Chapters.class);
                i.putExtra("id",id_level);
                startActivity(i);
                finish();
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);



        if (prefs.GetLevel("le_" + id_level) == level_category){
            level = prefs.GetLe(id_level);
            count = prefs.GetCount(id_level);
        }

        line_view.array_item = new ArrayList<>();
        array_item = new ArrayList<>();
        array_x = new ArrayList<>();
        array_y = new ArrayList<>();
        array_word = new ArrayList<>();
        array_btn = new ArrayList<>();
        array_count = new ArrayList<>();
        array_answer = new ArrayList<>();
        array_question = new ArrayList<>();
        array_author = new ArrayList<>();
        array_pic = new ArrayList<>();
        array_voice = new ArrayList<>();
        array_id = new ArrayList<>();
        array_voice_jomle = new ArrayList<>();
        array_ads_image = new ArrayList<>();
        array_ads_title = new ArrayList<>();


        txt_level = findViewById(R.id.txt_level);
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);
        btn_5 = findViewById(R.id.btn_5);
        btn_6 = findViewById(R.id.btn_6);
        btn_7 = findViewById(R.id.btn_7);
        btn_8 = findViewById(R.id.btn_8);
        array_item.add(btn_1);
        array_item.add(btn_2);
        array_item.add(btn_3);
        array_item.add(btn_4);
        array_item.add(btn_5);
        array_item.add(btn_6);
        array_item.add(btn_7);
        array_item.add(btn_8);
        linear_answer = findViewById(R.id.linear_answer);
        linear_show_answer = findViewById(R.id.linear_show_answer);
        re_word = findViewById(R.id.re_word);
        re_word.setVisibility(View.INVISIBLE);


        img_hint = findViewById(R.id.img_hint);
        btn_hint = findViewById(R.id.btn_hint);
        btn_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hint();
            }
        });
        Glide.with(Game.this)
                .load(R.drawable.exir8967)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(img_hint);

        btn_change = findViewById(R.id.btn_change);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayMonsterJump();
                Change();
            }
        });

        new Handler().postDelayed(() -> {
            float with = btn_1.getWidth();
            float high = btn_1.getHeight();
            target_x = (size.x / 2) - (with/2);
            target_y = (size.y / 2) - (high/2);
            for (int i=0; i<array_item.size(); i++){
                SetPosItem(array_item.get(i));
            }
        }, 300);

        magnetEnable = prefs.getMagnet(id,level);

        btnMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (magnetEnable){
                    Toast.makeText(Game.this, "مگنت فعال است", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (prefs.GetCoin() >= App.Magnet) {
                    lowCoin(App.Magnet,"spend");
                    sound.PlayBonusWord();
                    prefs.saveMagnet(id,level, true);
                    magnetEnable = true;
                }else {
                    sound.PlayClick();
                    Intent i = new Intent(Game.this,Shop.class);
                    startActivity(i);
                }
            }
        });

        GetDate();

    }//main

    public void OnWordSelected(String word, int count){

        if(!magnetEnable)
            return;

        String[] words = answer.split(" ");

        if(word.equals(words[magnetIndex])){

            magnetIndex++;

            if(!line_view.array_item.get(count).getMagnet()){
                checkIndex++;

                if(checkIndex % 2 == 0 && magnetIndex < words.length){
                    line_view.ShowMagnet(words[magnetIndex]);
                }
            }

        }else{

            magnetIndex = 0;

        }
    }

    void GetDate()
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.levels> call = apiInterface.bases_with_token(App.Token + prefs.GetToken(), prefs.GetLang(), id);
        Callback<ServerData.levels> callback = new Callback<ServerData.levels>() {
            @Override
            public void onResponse(Call<ServerData.levels> call, Response<ServerData.levels> response) {
                if (response.body().getItems() != null) {
                    if (response.body().getItems().size() > 0) {
                        for (int i = 0; i < response.body().getItems().size(); i++) {
                            array_id.add(response.body().getItems().get(i).getId());
                            if (question.equals("")) {
                                question = response.body().getItems().get(i).getTitle();
                            } else {
                                question = question + ":" + response.body().getItems().get(i).getTitle();
                            }
                            if (response.body().getItems().get(i).getVoice() != null){
                                array_voice_jomle.add(response.body().getItems().get(i).getVoice());
                            }else {
                                array_voice_jomle.add("null");
                            }
                            if (response.body().getItems().get(i).getSponsor() != null) {
                                if (response.body().getItems().get(i).getSponsor().getImage() != null) {
                                    array_ads_image.add(response.body().getItems().get(i).getSponsor().getImage());
                                } else {
                                    array_ads_image.add("null");
                                }
                                if (response.body().getItems().get(i).getSponsor().getTitle() != null) {
                                    array_ads_title.add(response.body().getItems().get(i).getSponsor().getTitle());
                                }else{
                                    array_ads_title.add("null");
                                }
                            }else {
                                array_ads_image.add("null");
                                array_ads_title.add("null");
                            }
                            if (response.body().getItems().get(i).getCelebrity() != null) {
                                if (response.body().getItems().get(i).getCelebrity().getTitle() != null) {
                                    array_author.add(response.body().getItems().get(i).getCelebrity().getTitle());
                                } else {
                                    array_author.add(App.con.getResources().getString(R.string.nulll));
                                }
                                if (response.body().getItems().get(i).getCelebrity().getAvatar() != null && !response.body().getItems().get(i).getCelebrity().getAvatar().equals("http://tarhelohemandegar.ir/storage/")) {
                                    array_pic.add(response.body().getItems().get(i).getCelebrity().getAvatar());
                                } else {
                                    array_pic.add("null");
                                }
                                if (response.body().getItems().get(i).getCelebrity().getVoice() != null && !response.body().getItems().get(i).getCelebrity().getVoice().equals("http://tarhelohemandegar.ir/storage/")) {
                                    array_voice.add(response.body().getItems().get(i).getCelebrity().getVoice());
                                } else {
                                    array_voice.add("null");
                                }
                            }else {
                                array_author.add(App.con.getResources().getString(R.string.nulll));
                                array_pic.add("null");
                                array_voice.add("null");
                            }
                        }

                        String[] str = question.split(":");
                        for (int i = 0; i < str.length; i++) {
                            array_question.add(str[i]);
                        }
                        SetAnswer();
                    }else {
                        //Toast.makeText(App.context, App.con.getResources().getString(R.string.no_level), Toast.LENGTH_SHORT).show();
                        Toast.makeText(App.context, App.con.getResources().getString(R.string.no_level), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Game.this,Chapters.class);
                        i.putExtra("id",id_level);
                        startActivity(i);
                        finish();
                    }
                }
                if (response.code() == 401){
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.login_token),Game.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.levels> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                count_1++;
                if(count_1 > 4) {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet), Game.this);
                }else {
                    GetDate();
                }
            }
        };
        call.enqueue(callback);
    }

    void SetVoice()
    {
        mp = null;
        mp_2 = null;
        if (!array_voice.get(level).equals("null")) {
            mp = new MediaPlayer();
            try {
                mp.setDataSource(array_voice.get(level));
                mp.prepare();
                mp.setVolume(prefs.GetVloume(),prefs.GetVloume());
                mp.setLooping(false);
            } catch (IOException e) {
                Log.e(App.LogName, "prepare() failed");
            }
        }
        if (!array_voice_jomle.get(level).equals("null")) {
            mp_2 = new MediaPlayer();
            try {
                mp_2.setDataSource(array_voice_jomle.get(level));
                mp_2.prepare();
                mp_2.setVolume(prefs.GetVloume(),prefs.GetVloume());
                mp_2.setLooping(false);
            } catch (IOException e) {
                Log.e(App.LogName, "prepare() failed");
            }
        }
    }

    private void SetRandomTheme()
    {
        array_color.add(getResources().getColor(R.color.orang_400));
        array_color.add(getResources().getColor(R.color.orang_400));
        array_color.add(getResources().getColor(R.color.orang_400));
        array_color.add(getResources().getColor(R.color.orang_400));
        array_color.add(getResources().getColor(R.color.orang_400));
        array_drawble.add(R.drawable.word_select_5);
        array_drawble.add(R.drawable.word_select_5);
        array_drawble.add(R.drawable.word_select_5);
        array_drawble.add(R.drawable.word_select_5);
        array_drawble.add(R.drawable.word_select_5);

        GetRandom();
    }

    private void GetRandom()
    {
        //Random random = new Random();
        color++;//random.nextInt(5);
        if (color > 4){
            color = 0;
        }
        txt_check = findViewById(R.id.txt_check);
        txt_check.setBackgroundResource(array_drawble.get(color));
    }

    private void EndScroll()
    {
        new Handler().postDelayed(() -> {
            View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
            int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
            int sy = scrollView.getScrollY();
            int sh = scrollView.getHeight();
            int delta = bottom - (sy + sh);
            scrollView.smoothScrollBy(0, delta);
        }, 400);
    }

    private void SetAnswer()
    {
        RestartLevel();
        qu = array_question.get(level).split("،");
        txt_level.setText(App.con.getResources().getString(R.string.level) + (level+1) + "/" + array_question.size() + "\n" + App.con.getResources().getString(R.string.section) + (count+1) + "/" + qu.length);
        answer = qu[count].trim();
        String[] str = answer.split(" ");
        index = str.length;
        if (prefs.GetLang().equals("fa")){
            for (int i = str.length-1; i >= 0; i--) {
                CustomAnswer customAnswer = new CustomAnswer(this);
                customAnswer.answer = str[i];
                array_answer.add(customAnswer);
                array_word.add(str[i]);
                linear_answer.addView(customAnswer);
            }
        }else {
            for (int i = 0; i < str.length; i++) {
                CustomAnswer customAnswer = new CustomAnswer(this);
                customAnswer.answer = str[i];
                array_answer.add(customAnswer);
                linear_answer.addView(customAnswer);
            }
        }
        SetBtnWord();
        if (prefs.GetLevel("le_" + id_level) == level_category) {
            linear_show_answer.removeAllViews();
            for (int i = 0; i < count; i++) {
                NewShowAnswer(qu[i].trim());
            }
            if (prefs.GetLang().equals("fa")){
                for (int i = array_answer.size()-1; i>=prefs.GetHintFa(id); i--) {
                    array_answer.get(i).SetAnswer();
                }
            }else {
                for (int i = 0; i<prefs.GetHintEn(id); i++) {
                    array_answer.get(i).SetAnswer();
                }
            }
        }

    }

    private void RestartLevel()
    {
        for (int i=0; i<array_item.size(); i++){
            array_item.get(i).setVisibility(View.VISIBLE);
        }

        if (!array_ads_image.get(level).equals("null")){
            card_ads.setVisibility(View.VISIBLE);
            txt_ads.setVisibility(View.GONE);
            Glide.with(Game.this)
                    .load(array_ads_image.get(level))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(img_ads);
        }else {
            if (!array_ads_title.get(level).equals("null")){
                card_ads.setVisibility(View.GONE);
                txt_ads.setVisibility(View.VISIBLE);
                txt_ads.setText(array_ads_title.get(level));
            }else {
                card_ads.setVisibility(View.GONE);
                txt_ads.setVisibility(View.GONE);
            }
        }

        if (customBlur == null) {
            if (!array_pic.get(level).equals("null")) {
                customBlur = new CustomBlur(Game.this);
                Glide.with(Game.this)
                        .load(array_pic.get(level))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(customBlur.img_background);
                customBlur.active = true;
                re_blur.addView(customBlur);
            }
            SetVoice();
        }
        txt_coin.setText(String.valueOf(prefs.GetCoin()));
        line_view.array_item.clear();
        array_word.clear();
        array_btn.clear();
        array_count.clear();
        array_answer.clear();
        scrollView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,100));
    }

    private void SetBtnWord()
    {
        if (index == 8){
            for (int i=0; i<array_item.size(); i++){
                array_btn.add(array_item.get(i));
            }
        }else if (index == 7){
            btn_5.setVisibility(View.INVISIBLE);
            array_btn.add(btn_1);
            array_btn.add(btn_2);
            array_btn.add(btn_3);
            array_btn.add(btn_4);
            array_btn.add(btn_6);
            array_btn.add(btn_7);
            array_btn.add(btn_8);
        }else if (index == 6){
            btn_3.setVisibility(View.INVISIBLE);
            btn_7.setVisibility(View.INVISIBLE);
            array_btn.add(btn_1);
            array_btn.add(btn_2);
            array_btn.add(btn_4);
            array_btn.add(btn_5);
            array_btn.add(btn_6);
            array_btn.add(btn_8);
        }else if (index == 5){
            btn_4.setVisibility(View.INVISIBLE);
            btn_5.setVisibility(View.INVISIBLE);
            btn_6.setVisibility(View.INVISIBLE);
            array_btn.add(btn_1);
            array_btn.add(btn_2);
            array_btn.add(btn_3);
            array_btn.add(btn_7);
            array_btn.add(btn_8);
        }else if (index == 4){
            btn_2.setVisibility(View.INVISIBLE);
            btn_4.setVisibility(View.INVISIBLE);
            btn_6.setVisibility(View.INVISIBLE);
            btn_8.setVisibility(View.INVISIBLE);
            array_btn.add(btn_1);
            array_btn.add(btn_3);
            array_btn.add(btn_5);
            array_btn.add(btn_7);
        }else if (index == 3){
            btn_2.setVisibility(View.INVISIBLE);
            btn_4.setVisibility(View.INVISIBLE);
            btn_5.setVisibility(View.INVISIBLE);
            btn_6.setVisibility(View.INVISIBLE);
            btn_8.setVisibility(View.INVISIBLE);
            array_btn.add(btn_1);
            array_btn.add(btn_3);
            array_btn.add(btn_7);
        }else if (index == 2){
            btn_1.setVisibility(View.INVISIBLE);
            btn_2.setVisibility(View.INVISIBLE);
            btn_4.setVisibility(View.INVISIBLE);
            btn_5.setVisibility(View.INVISIBLE);
            btn_6.setVisibility(View.INVISIBLE);
            btn_8.setVisibility(View.INVISIBLE);
            array_btn.add(btn_3);
            array_btn.add(btn_7);
        }else {
            //Toast.makeText(App.context, App.con.getResources().getString(R.string.no_text), Toast.LENGTH_SHORT).show();
            Toast.makeText(App.context, "id: "+array_id.get(level) , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Game.this,Chapters.class);
            intent.putExtra("id",id_level);
            startActivity(intent);
            finish();
        }

        new Handler().postDelayed(() -> {
            for (int i=0; i<array_btn.size(); i++){
                SetPosition(array_btn.get(i),array_word.get(RandomCount(array_word.size())));
            }
            for (int i=0; i<array_item.size(); i++){
                array_item.get(i).setX(target_x);
                array_item.get(i).setY(target_y);
            }
            new Handler().postDelayed(() -> {
                re_word.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    sound.PlayMonsterJump();
                    SetAnim();
                }, 100);
            }, 300);
        }, 300);

    }


    private void Change()
    {
        Animations anim = new Animations();
        for (int i = 0; i < line_view.array_item.size(); i++) {
            anim.SetAnim(line_view.array_item.get(i).btn, target_x, target_y);
        }
        new Handler().postDelayed(() -> {
            for (int i = 0; i < line_view.array_item.size(); i++) {
                anim.SetAnim(line_view.array_item.get(i).btn, line_view.array_item.get(i).Mx, line_view.array_item.get(i).My);
            }
            array_count.clear();
            for (int i=0; i<line_view.array_item.size(); i++){
                String str = array_word.get(RandomCount(array_word.size()));
                line_view.array_item.get(i).word = str;
                line_view.array_item.get(i).btn.setText(str);
            }
        }, 300);
    }

    private void SetAnim()
    {
        Animations anim = new Animations();
        for (int i = 0; i < array_item.size(); i++) {
            anim.SetAnim(array_item.get(i), array_x.get(i), array_y.get(i));
        }
    }

    private void Hint()
    {
        if (prefs.GetCoin() >= App.Hint) {
            sound.PlayBonusWord();
            if (prefs.GetLang().equals("fa")){
                for (int i = array_answer.size()-1; i >= 0 ; i--) {
                    if (!array_answer.get(i).active) {
                        array_answer.get(i).SetAnswer();
                        lowCoin(App.Hint,"spend");
                        prefs.SaveHintFa(id,i);
                        break;
                    }
                }
            }else {
                for (int i = 0; i < array_answer.size(); i++) {
                    if (!array_answer.get(i).active) {
                        array_answer.get(i).SetAnswer();
                        lowCoin(App.Hint,"spend");
                        prefs.SaveHintEn(id,i+1);
                        break;
                    }
                }
            }

        }else {
            sound.PlayClick();
            Intent i = new Intent(Game.this,Shop.class);
            startActivity(i);
        }
    }

    void SetPosition(TextView btn, String word)
    {
        float with = btn.getWidth();
        float high = btn.getHeight();
        float Mx1 = btn.getX() + (with/2);
        float My1 = btn.getY() + (high/2);
        btn.setText(word);
        Item item = new Item(false,Mx1,My1,word,btn,btn.getX(),btn.getY());
        line_view.array_item.add(item);
    }

    void SetPosItem(TextView btn)
    {
        array_x.add(btn.getX());
        array_y.add(btn.getY());
    }

    public void SetText(String str)
    {
        txt_check.setVisibility(View.VISIBLE);
        if (text.equals("")){
            text = str;
        }else {
            text = text + " " + str;
        }
        txt_check.setText(text);
    }

    private void NewShowAnswer(String answer)
    {
        CustomShowAnswer customShowAnswer = new CustomShowAnswer(Game.this);
        customShowAnswer.txt_answer.setText(answer);
        customShowAnswer.txt_answer.setBackgroundResource(array_drawble.get(color));
        linear_show_answer.addView(customShowAnswer);
        EndScroll();
    }

    public void CheckAnswer()
    {
        if (text.equals(answer)){
            sound.PlaySuccess();
            re_word.setVisibility(View.INVISIBLE);
            linear_answer.removeAllViews();
            prefs.SaveHintFa(id,8);
            prefs.SaveHintEn(id,0);
            if (prefs.GetLevel("le_" + id_level) != level_category) {
                NewShowAnswer(answer);
            }
            count++;
            if (count >= qu.length){
                SetEnd();
            }else {
                SetAnswer();
            }
            if (prefs.GetLevel("le_" + id_level) == level_category){
                prefs.SaveLe(id_level,level);
                prefs.SaveCount(id_level,count);
            }
        }else {
            isFirst = false;
            sound.PlayWrong();
        }
        text = "";
        txt_check.setVisibility(View.INVISIBLE);
        txt_check.setText(text);
    }

    private void SetEnd()
    {
        scrollView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
        if (prefs.GetLevel("le_" + id_level) == level_category) {
            NewShowAnswer(answer);
        }
        String[] ss = array_question.get(level).split(" ");
        score = ss.length;
        if (!array_pic.get(level).equals("null")) {
            customBlur.img_background.setBlur(0);
            customBlur.CanslTimer();
        }
        NewShowAnswer(array_author.get(level));
        if (prefs.GetTalker() && mp_2 != null){
            mp_2.start();
            GetPlayTalker();
        }else {
            if (prefs.GetTalker() && mp != null){
                mp.start();
            }
        }

        setSentence(array_id.get(level));

        level++;
        magnetEnable = prefs.getMagnet(id,level);
        count = 0;
        new Handler().postDelayed(() -> {
            DialoWin();
        }, 5000);
    }

    private void GetPlayTalker()
    {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (!mp_2.isPlaying()){
                        timer.cancel();
                        if (prefs.GetTalker() && mp != null){
                            mp.start();
                        }
                    }
                });
            }
        }, 1000, 1000);
    }


    public  void DialoWin()
    {
        sound.PlayLevelEnd();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_win);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);
        LinearLayout li_coin = dialog.findViewById(R.id.li_coin);
        LinearLayout liCoinCambo = dialog.findViewById(R.id.liCoinCambo);
        liCoinCambo.setVisibility(View.GONE);
        TextView txt_cambo = dialog.findViewById(R.id.txt_cambo);
        TextView txt_coin_2 = dialog.findViewById(R.id.txt_coin);
        TextView txt_end_level = dialog.findViewById(R.id.txt_end_level);
        txt_end_level.setText(App.con.getResources().getString(R.string.end_level) + level);
        ImageView img_report = dialog.findViewById(R.id.img_report);
        img_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                if (!prefs.GetToken().equals("")) {
                    DialogReport();
                }else {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.sabt_desc),Game.this);
                }
            }
        });
        ImageView img_back = dialog.findViewById(R.id.img_back);
        RelativeLayout img_share = dialog.findViewById(R.id.img_share);
        ImageView img_next = dialog.findViewById(R.id.img_next);


        if (prefs.GetLevel("le_" + id_level) == level_category) {
            int cambo = 0;
            if(isFirst){
                if(score >= 16) {
                    cambo = score;
                }else{
                    cambo = 1;
                }
                liCoinCambo.setVisibility(View.VISIBLE);
                txt_cambo.setText("+" + cambo);
            }
            txt_coin_2.setText("+" + score);
            int scoreNew = score + cambo;
            changeCoin(scoreNew,"add");
            prefs.AddScour();
            if (App.IdBack == 1){
                prefs.AddScourOne();
            }else if (App.IdBack == 2){
                prefs.AddScourTow();
            }else if (App.IdBack == 3){
                prefs.AddScourTree();
            }else if (App.IdBack == 4){
                prefs.AddScourFour();
            }
            if (level >= array_question.size()) {
                prefs.AddLevel("le_" + id_level);
                prefs.SaveLe(id_level,0);
                prefs.SaveCount(id_level,0);
            }

            new Handler().postDelayed(() -> {
                li_coin.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp)
                        .duration(500)
                        .repeat(0)
                        .playOn(li_coin);

                YoYo.with(Techniques.Shake)
                        .duration(500)
                        .repeat(0)
                        .delay(500)
                        .playOn(li_coin);
                txt_coin.setText(String.valueOf(prefs.GetCoin()));
                sound.PlayHint();
            }, 700);
        }


        img_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp != null){
                    mp.stop();
                }
                sound.PlayClick();
                linear_show_answer.removeAllViews();
                GetRandom();
                line_view.SetPaint();
                if (level < array_question.size()) {
                    if (re_blur.getChildCount() > 0) {
                        re_blur.removeAllViews();
                        customBlur = null;
                    }
                    YoYo.with(Techniques.ZoomOut)
                            .duration(300)
                            .repeat(0)
                            .playOn(re_up);
                    new Handler().postDelayed(() -> {
                        dialog.dismiss();
                        SetAnswer();
                    }, 200);
                }else {
                    Intent i = new Intent(Game.this,Chapters.class);
                    i.putExtra("id",id_level);
                    startActivity(i);
                    finish();
                }
            }
        });

        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!prefs.GetEmail().equals("")) {
                    if (mp != null){
                        mp.stop();
                    }
                    sound.PlayClick();
                    Intent shairIntent = new Intent(Intent.ACTION_SEND);
                    shairIntent.setType("text/plain");
                    String shareBody = App.con.getResources().getString(R.string.app_Tozih_1) + " " + prefs.GetCode() + " " + App.con.getResources().getString(R.string.app_Tozih_2);
                    shairIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(shairIntent, App.con.getResources().getString(R.string.share_title)));
                }else {
                    AdsTapsell.Dialog(App.con.getResources().getString(R.string.email_verified),Game.this);
                }

            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp != null){
                    mp.stop();
                }
                sound.PlayClick();
                Intent i = new Intent(Game.this,Chapters.class);
                i.putExtra("id",id_level);
                startActivity(i);
                finish();
            }
        });


        int w = getResources().getDisplayMetrics().widthPixels;
        dialog.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 150);
    }

    public void changeCoin(int amount, String type){
        CoinManager.addCoin(this, amount,type, new CoinManager.CoinCallback() {

            @Override
            public void onSuccess() {
                txt_coin.setText(String.valueOf(prefs.GetCoin()));
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void lowCoin(int amount, String type){
        CoinManager.addCoin(this, amount,type, new CoinManager.CoinCallback() {

            @Override
            public void onSuccess() {
                txt_coin.setText(prefs.GetCoin()+"");
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public  void DialogReport()
    {
        dialog_report = new Dialog(this);
        dialog_report.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_report.setContentView(R.layout.dialog_report);
        dialog_report.setCancelable(true);
        dialog_report.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = dialog_report.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        EditText edt_report = dialog_report.findViewById(R.id.edt_report);
        edt_report.setHint(App.con.getResources().getString(R.string.error_5));
        TextView txt_error_1 = dialog_report.findViewById(R.id.txt_error_1);
        txt_error_1.setText(App.con.getResources().getString(R.string.error_1));
        txt_error_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetReport(txt_error_1.getText().toString());
                sound.PlayClick();
                CloseDialog(dialog_report,re_up);
            }
        });
        TextView txt_error_2 = dialog_report.findViewById(R.id.txt_error_2);
        txt_error_2.setText(App.con.getResources().getString(R.string.error_2));
        txt_error_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetReport(txt_error_2.getText().toString());
                sound.PlayClick();
                CloseDialog(dialog_report,re_up);
            }
        });
        TextView txt_error_3 = dialog_report.findViewById(R.id.txt_error_3);
        txt_error_3.setText(App.con.getResources().getString(R.string.error_3));
        txt_error_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetReport(txt_error_3.getText().toString());
                sound.PlayClick();
                CloseDialog(dialog_report,re_up);
            }
        });
        Button btn_send = dialog_report.findViewById(R.id.btn_send);
        btn_send.setText(App.con.getResources().getString(R.string.send));
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_report.length() > 0) {
                    SetReport(edt_report.getText().toString());
                    sound.PlayClick();
                    CloseDialog(dialog_report,re_up);
                }
            }
        });
        TextView txt_error_4 = dialog_report.findViewById(R.id.txt_error_4);
        txt_error_4.setText(App.con.getResources().getString(R.string.error_4));
        txt_error_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetReport(txt_error_4.getText().toString());
                sound.PlayClick();
                CloseDialog(dialog_report,re_up);
            }
        });

        ImageView img_close = dialog_report.findViewById(R.id.img_close);
        //txt_en.setText(App.con.getResources().getString(R.string.no_update));
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                CloseDialog(dialog_report,re_up);
            }
        });



        int w = getResources().getDisplayMetrics().widthPixels;
        dialog_report.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_report.show();

        new Handler().postDelayed(() -> {
            re_up.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn)
                    .duration(500)
                    .repeat(0)
                    .playOn(re_up);
        }, 100);
    }

    private void SetReport(String matn)
    {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.report(App.Token + prefs.GetToken(),array_id.get(level),matn);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if (response.body().getSuccess()){
                    // AdsTapsell.Dialog(response.body().getMessage(),Game.this);
                }else {
                    AdsTapsell.Dialog(response.body().getMessage(),Game.this);
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {
                Log.e(App.LogName, t.toString());
                AdsTapsell.Dialog(App.con.getResources().getString(R.string.internet),Game.this);
            }
        };
        call.enqueue(callback);
    }

    private void setSentence(int id)
    {
        Sentence sentence = new Sentence(id);
        Log.e(App.LogName,"SentenceId: "+ id);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = apiInterface.sentence_complete(App.Token + prefs.GetToken(),sentence);
        Callback<ServerData.Message> callback = new Callback<ServerData.Message>() {
            @Override
            public void onResponse(Call<ServerData.Message> call, Response<ServerData.Message> response)
            {
                if(response.isSuccessful()){
                    if (response.body().getSuccess()){
                        Log.e(App.LogName,"SentenceId: "+ id);
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

    Integer RandomCount(int max){
        Random rand = new Random();
        int co = rand.nextInt(max);
        while (array_count.contains(co)) {
            co = rand.nextInt(max);
        }
        array_count.add(co);
        return co;
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();  // ✅ اضافه شد
        Intent i = new Intent(Game.this, Chapters.class);
        i.putExtra("id", id_level);
        startActivity(i);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetAnimCoin();
        SoundPlayer.PlayMusic();
        txt_coin.setText(String.valueOf(prefs.GetCoin()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SetTimerCansel();
        if (customBlur != null) {
            customBlur.CanslTimer();
        }
        color = -1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SetTimerCansel();
        SoundPlayer.PauseMusic();
    }


    public void SetAnimCoin()
    {
        timer_coin = new Timer();
        timer_coin.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
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