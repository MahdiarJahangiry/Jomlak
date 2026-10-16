package com.game.education.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
    private Timer timer_coin;
    public final List<Integer> array_color = new ArrayList<>();
    public final List<Integer> array_drawble = new ArrayList<>();

    private LineView line_view;
    private RelativeLayout re_word;
    private TextView btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8;
    private TextView txt_level, txt_coin, txt_check;
    private String text = "";
    private String question = "";
    private String answer = "";

    // تغییر ۲: مقداردهی اولیه تمامی لیست‌ها جهت جلوگیری از NullPointerException
    private List<TextView> array_btn = new ArrayList<>();
    private List<String> array_ads_image = new ArrayList<>();
    private List<String> array_ads_title = new ArrayList<>();
    private List<String> array_word = new ArrayList<>();
    private List<String> array_question = new ArrayList<>();
    private List<String> array_pic = new ArrayList<>();
    private List<String> array_voice = new ArrayList<>();
    private List<String> array_voice_jomle = new ArrayList<>();
    private List<String> array_author = new ArrayList<>();
    private List<Float> array_x = new ArrayList<>();
    private List<Float> array_y = new ArrayList<>();
    private final List<TextView> array_item = new ArrayList<>();
    private final List<CustomAnswer> array_answer = new ArrayList<>();
    private final List<Integer> array_count = new ArrayList<>();
    private final List<Integer> array_id = new ArrayList<>();

    private int index = 0, count = 0, level = 0, score = 0;
    private LinearLayout linear_answer, linear_show_answer, li_ads, btn_hint;
    private LinearLayout re_blur;
    private Prefs prefs;
    private float target_x, target_y;
    public SoundPlayer sound;
    private String[] qu;
    private LottieAnimationView gift;
    private int id, id_level, level_category, count_1 = 0;
    private NestedScrollView scrollView;
    private ImageView img_back, img_coin;
    private CustomBlur customBlur;
    private MediaPlayer mp, mp_2;
    private Dialog dialog_report;
    private ImageView img_ads, img_hint;
    private TextView img_video, txt_ads;
    private CardView card_ads;
    private AdsTapsell adsTapsell;
    private View btnMagnet;
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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getInt("id");
            id_level = bundle.getInt("id_level");
            level_category = bundle.getInt("level_category");
        }

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
        scrollView = findViewById(R.id.scrollView);
        re_blur = findViewById(R.id.re_blur);
        li_ads = findViewById(R.id.li_ads);
        li_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                startActivity(new Intent(Game.this, Shop.class));
            }
        });
        img_coin = findViewById(R.id.img_coin);
        txt_coin = findViewById(R.id.txt_coin);
        img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayClick();
                NavigateBack();
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if (prefs.GetLevel("le_" + id_level) == level_category) {
            level = prefs.GetLe(id_level);
            count = prefs.GetCount(id_level);
        }

        InitLists();

        txt_level = findViewById(R.id.txt_level);
        btn_1 = findViewById(R.id.btn_1); btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3); btn_4 = findViewById(R.id.btn_4);
        btn_5 = findViewById(R.id.btn_5); btn_6 = findViewById(R.id.btn_6);
        btn_7 = findViewById(R.id.btn_7); btn_8 = findViewById(R.id.btn_8);
        array_item.add(btn_1); array_item.add(btn_2); array_item.add(btn_3); array_item.add(btn_4);
        array_item.add(btn_5); array_item.add(btn_6); array_item.add(btn_7); array_item.add(btn_8);

        linear_answer = findViewById(R.id.linear_answer);
        linear_show_answer = findViewById(R.id.linear_show_answer);
        re_word = findViewById(R.id.re_word);
        re_word.setVisibility(View.INVISIBLE);

        img_hint = findViewById(R.id.img_hint);
        btn_hint = findViewById(R.id.btn_hint);
        btn_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { Hint(); }
        });
        Glide.with(this).load(R.drawable.exir8967).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(img_hint);

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.PlayMonsterJump();
                Change();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                float w = btn_1.getWidth();
                float h = btn_1.getHeight();
                target_x = (size.x / 2f) - (w / 2f);
                target_y = (size.y / 2f) - (h / 2f);
                for (TextView tv : array_item) SetPosItem(tv);
            }
        }, 300);

        magnetEnable = prefs.getMagnet(id, level);
        btnMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (magnetEnable) {
                    Toast.makeText(Game.this, "مگنت فعال است", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (prefs.GetCoin() >= App.Magnet) {
                    lowCoin(App.Magnet, "spend");
                    sound.PlayBonusWord();
                    prefs.saveMagnet(id, level, true);
                    magnetEnable = true;
                } else {
                    sound.PlayClick();
                    startActivity(new Intent(Game.this, Shop.class));
                }
            }
        });

        GetDate();
    }

    private void InitLists() {
        if (array_x == null) array_x = new ArrayList<>(); else array_x.clear();
        if (array_y == null) array_y = new ArrayList<>(); else array_y.clear();
        if (array_word == null) array_word = new ArrayList<>(); else array_word.clear();
        if (array_btn == null) array_btn = new ArrayList<>(); else array_btn.clear();
        if (array_question == null) array_question = new ArrayList<>(); else array_question.clear();
        if (array_author == null) array_author = new ArrayList<>(); else array_author.clear();
        if (array_pic == null) array_pic = new ArrayList<>(); else array_pic.clear();
        if (array_voice == null) array_voice = new ArrayList<>(); else array_voice.clear();
        if (array_voice_jomle == null) array_voice_jomle = new ArrayList<>(); else array_voice_jomle.clear();
        if (array_ads_image == null) array_ads_image = new ArrayList<>(); else array_ads_image.clear();
        if (array_ads_title == null) array_ads_title = new ArrayList<>(); else array_ads_title.clear();
    }

    private void NavigateBack() {
        Intent i = new Intent(Game.this, Chapters.class);
        i.putExtra("id", id_level);
        startActivity(i);
        finish();
    }

    public void OnWordSelected(String word, int count) {
        if (!magnetEnable) return;
        String[] words = answer.split(" ");
        if (magnetIndex < words.length && word.equals(words[magnetIndex])) {
            magnetIndex++;
            if (line_view != null && line_view.array_item != null && count < line_view.array_item.size() && !line_view.array_item.get(count).getMagnet()) {
                checkIndex++;
                if (checkIndex % 2 == 0 && magnetIndex < words.length) {
                    line_view.ShowMagnet(words[magnetIndex]);
                }
            }
        } else {
            magnetIndex = 0;
        }
    }

    void GetDate() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerData.levels> call = apiInterface.bases_with_token(App.Token + prefs.GetToken(), prefs.GetLang(), id);
        call.enqueue(new Callback<ServerData.levels>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.levels> call, @NonNull Response<ServerData.levels> response) {
                if (response.body() != null && response.body().getItems() != null) {
                    List<ServerData.Items> items = response.body().getItems();
                    if (!items.isEmpty()) {
                        for (ServerData.Items item : items) {
                            array_id.add(item.getId());
                            question = question.isEmpty() ? item.getTitle() : question + ":" + item.getTitle();
                            array_voice_jomle.add(item.getVoice() != null ? item.getVoice() : "null");

                            if (item.getSponsor() != null) {
                                array_ads_image.add(item.getSponsor().getImage() != null ? item.getSponsor().getImage() : "null");
                                array_ads_title.add(item.getSponsor().getTitle() != null ? item.getSponsor().getTitle() : "null");
                            } else {
                                array_ads_image.add("null"); array_ads_title.add("null");
                            }

                            if (item.getCelebrity() != null) {
                                array_author.add(item.getCelebrity().getTitle() != null ? item.getCelebrity().getTitle() : getString(R.string.nulll));
                                String avatar = item.getCelebrity().getAvatar();
                                array_pic.add((avatar != null && !avatar.contains("storage/")) ? avatar : "null");
                                String vce = item.getCelebrity().getVoice();
                                array_voice.add((vce != null && !vce.contains("storage/")) ? vce : "null");
                            } else {
                                array_author.add(getString(R.string.nulll)); array_pic.add("null"); array_voice.add("null");
                            }
                        }

                        String[] strArr = question.split(":");
                        for (String s : strArr) array_question.add(s);

                        SetAnswer();
                    } else {
                        Toast.makeText(App.context, getString(R.string.no_level), Toast.LENGTH_SHORT).show();
                        NavigateBack();
                    }
                }
                if (response.code() == 401) {
                    prefs.SetUsername("");
                    AdsTapsell.Dialog(getString(R.string.login_token), Game.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerData.levels> call, @NonNull Throwable t) {
                count_1++;
                if (count_1 > 4) AdsTapsell.Dialog(getString(R.string.internet), Game.this);
                else GetDate();
            }
        });
    }

    void SetVoice() {
        if (mp != null) { mp.release(); mp = null; }
        if (mp_2 != null) { mp_2.release(); mp_2 = null; }

        if (array_voice != null && array_voice.size() > level && !array_voice.get(level).equals("null")) {
            mp = new MediaPlayer();
            try {
                mp.setDataSource(array_voice.get(level));
                mp.prepare();
                mp.setVolume(prefs.GetVloume(), prefs.GetVloume());
            } catch (IOException e) { Log.e("Game", "Voice fail"); }
        }
        if (array_voice_jomle != null && array_voice_jomle.size() > level && !array_voice_jomle.get(level).equals("null")) {
            mp_2 = new MediaPlayer();
            try {
                mp_2.setDataSource(array_voice_jomle.get(level));
                mp_2.prepare();
                mp_2.setVolume(prefs.GetVloume(), prefs.GetVloume());
            } catch (IOException e) { Log.e("Game", "Voice2 fail"); }
        }
    }

    private void SetRandomTheme() {
        for (int i = 0; i < 5; i++) {
            array_color.add(ContextCompat.getColor(this, R.color.orang_400));
            array_drawble.add(R.drawable.word_select_5);
        }
        GetRandom();
    }

    private void GetRandom() {
        color++;
        if (color > 4) color = 0;
        txt_check = findViewById(R.id.txt_check);
        if (array_drawble.size() > color) {
            txt_check.setBackgroundResource(array_drawble.get(color));
        }
    }

    private void EndScroll() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View last = scrollView.getChildAt(scrollView.getChildCount() - 1);
                if (last != null) {
                    int bottom = last.getBottom() + scrollView.getPaddingBottom();
                    int sy = scrollView.getScrollY();
                    int sh = scrollView.getHeight();
                    scrollView.smoothScrollBy(0, bottom - (sy + sh));
                }
            }
        }, 400);
    }

    private void SetAnswer() {
        RestartLevel();
        if (array_question == null || array_question.size() <= level) return;
        qu = array_question.get(level).split("،");
        if (count >= qu.length) return;
        txt_level.setText(String.format("%s%d/%d\n%s%d/%d", getString(R.string.level), (level + 1), array_question.size(), getString(R.string.section), (count + 1), qu.length));
        answer = qu[count].trim();
        String[] words = answer.split(" ");
        index = words.length;

        linear_answer.removeAllViews();
        if (prefs.GetLang().equals("fa")) {
            for (int i = words.length - 1; i >= 0; i--) {
                CustomAnswer ca = new CustomAnswer(this);
                ca.answer = words[i];
                array_answer.add(ca);
                array_word.add(words[i]);
                linear_answer.addView(ca);
            }
        } else {
            for (String w : words) {
                CustomAnswer ca = new CustomAnswer(this);
                ca.answer = w;
                array_answer.add(ca);
                linear_answer.addView(ca);
            }
        }
        SetBtnWord();

        if (prefs.GetLevel("le_" + id_level) == level_category) {
            linear_show_answer.removeAllViews();
            for (int i = 0; i < count; i++) NewShowAnswer(qu[i].trim());
            if (prefs.GetLang().equals("fa")) {
                for (int i = array_answer.size() - 1; i >= prefs.GetHintFa(id); i--) {
                    if (i >= 0 && i < array_answer.size()) array_answer.get(i).SetAnswer();
                }
            } else {
                for (int i = 0; i < prefs.GetHintEn(id); i++) {
                    if (i < array_answer.size()) array_answer.get(i).SetAnswer();
                }
            }
        }
    }

    // تغییر ۳: متد RestartLevel به طور کامل در برابر NullPointerException و خارج از محدوده بودن ایندکس ایمن‌سازی شد
    private void RestartLevel() {
        for (TextView tv : array_item) {
            if (tv != null) tv.setVisibility(View.VISIBLE);
        }

        if (array_ads_image != null && array_ads_image.size() > level && !array_ads_image.get(level).equals("null")) {
            card_ads.setVisibility(View.VISIBLE);
            txt_ads.setVisibility(View.GONE);
            Glide.with(this).load(array_ads_image.get(level)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(img_ads);
        } else {
            card_ads.setVisibility(View.GONE);
            if (array_ads_title != null && array_ads_title.size() > level && !array_ads_title.get(level).equals("null")) {
                txt_ads.setVisibility(View.VISIBLE);
                txt_ads.setText(array_ads_title.get(level));
            } else {
                txt_ads.setVisibility(View.GONE);
            }
        }

        if (customBlur == null) {
            if (array_pic != null && array_pic.size() > level && !array_pic.get(level).equals("null")) {
                customBlur = new CustomBlur(this);
                Glide.with(this).load(array_pic.get(level)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(customBlur.img_background);
                customBlur.active = true;
                re_blur.addView(customBlur);
            }
            SetVoice();
        }

        txt_coin.setText(String.valueOf(prefs.GetCoin()));

        // پاکسازی ایمن لیست‌ها بدون امکان بروز NullPointerException
        if (line_view != null) {
            if (line_view.array_item == null) {
                line_view.array_item = new ArrayList<>();
            } else {
                line_view.array_item.clear();
            }
        }
        if (array_word != null) array_word.clear();
        if (array_btn != null) array_btn.clear();
        if (array_count != null) array_count.clear();
        if (array_answer != null) array_answer.clear();

        scrollView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
    }

    private void SetBtnWord() {
        if (index >= 2 && index <= 8) {
            UpdateButtonsVisibility(index);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (TextView btn : array_btn) {
                        if (array_word != null && !array_word.isEmpty()) {
                            SetPosition(btn, array_word.get(RandomCount(array_word.size())));
                        }
                    }
                    for (TextView item : array_item) {
                        item.setX(target_x);
                        item.setY(target_y);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            re_word.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sound.PlayMonsterJump();
                                    SetAnim();
                                }
                            }, 100);
                        }
                    }, 300);
                }
            }, 300);
        } else {
            NavigateBack();
        }
    }

    private void UpdateButtonsVisibility(int wordCount) {
        for (TextView tv : array_item) tv.setVisibility(View.INVISIBLE);
        array_btn.clear();

        if (wordCount == 8) {
            array_btn.addAll(array_item);
        } else if (wordCount == 7) {
            array_btn.add(btn_1); array_btn.add(btn_2); array_btn.add(btn_3);
            array_btn.add(btn_4); array_btn.add(btn_5); array_btn.add(btn_6); array_btn.add(btn_7);
        } else if (wordCount == 6) {
            array_btn.add(btn_1); array_btn.add(btn_2); array_btn.add(btn_3);
            array_btn.add(btn_5); array_btn.add(btn_6); array_btn.add(btn_7);
        } else if (wordCount == 5) {
            array_btn.add(btn_1); array_btn.add(btn_2); array_btn.add(btn_3);
            array_btn.add(btn_5); array_btn.add(btn_7);
        } else if (wordCount == 4) {
            array_btn.add(btn_1); array_btn.add(btn_3); array_btn.add(btn_5); array_btn.add(btn_7);
        } else if (wordCount == 3) {
            array_btn.add(btn_1); array_btn.add(btn_4); array_btn.add(btn_6);
        } else if (wordCount == 2) {
            array_btn.add(btn_2); array_btn.add(btn_6);
        }

        for (TextView b : array_btn) b.setVisibility(View.VISIBLE);
    }

    private void Change() {
        if (line_view == null || line_view.array_item == null) return;
        Animations anim = new Animations();
        for (Item it : line_view.array_item) anim.SetAnim(it.btn, target_x, target_y);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (Item it : line_view.array_item) anim.SetAnim(it.btn, it.Mx, it.My);
                array_count.clear();
                for (Item it : line_view.array_item) {
                    if (array_word != null && !array_word.isEmpty()) {
                        String s = array_word.get(RandomCount(array_word.size()));
                        it.word = s;
                        it.btn.setText(s);
                    }
                }
            }
        }, 300);
    }

    private void SetAnim() {
        Animations anim = new Animations();
        for (int i = 0; i < array_item.size(); i++) {
            if (i < array_x.size() && i < array_y.size()) {
                anim.SetAnim(array_item.get(i), array_x.get(i), array_y.get(i));
            }
        }
    }

    private void Hint() {
        if (prefs.GetCoin() >= App.Hint) {
            sound.PlayBonusWord();
            if (prefs.GetLang().equals("fa")) {
                for (int i = array_answer.size() - 1; i >= 0; i--) {
                    if (!array_answer.get(i).active) {
                        array_answer.get(i).SetAnswer();
                        lowCoin(App.Hint, "spend");
                        prefs.SaveHintFa(id, i);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < array_answer.size(); i++) {
                    if (!array_answer.get(i).active) {
                        array_answer.get(i).SetAnswer();
                        lowCoin(App.Hint, "spend");
                        prefs.SaveHintEn(id, i + 1);
                        break;
                    }
                }
            }
        } else {
            sound.PlayClick();
            startActivity(new Intent(Game.this, Shop.class));
        }
    }

    void SetPosition(TextView btn, String word) {
        float Mx1 = btn.getX() + (btn.getWidth() / 2f);
        float My1 = btn.getY() + (btn.getHeight() / 2f);
        btn.setText(word);
        if (line_view != null) {
            if (line_view.array_item == null) line_view.array_item = new ArrayList<>();
            line_view.array_item.add(new Item(false, Mx1, My1, word, btn, btn.getX(), btn.getY()));
        }
    }

    void SetPosItem(TextView btn) {
        array_x.add(btn.getX());
        array_y.add(btn.getY());
    }

    public void SetText(String str) {
        txt_check.setVisibility(View.VISIBLE);
        text = text.isEmpty() ? str : text + " " + str;
        txt_check.setText(text);
    }

    private void NewShowAnswer(String ans) {
        CustomShowAnswer csa = new CustomShowAnswer(this);
        csa.txt_answer.setText(ans);
        if (array_drawble.size() > color && color >= 0) {
            csa.txt_answer.setBackgroundResource(array_drawble.get(color));
        }
        linear_show_answer.addView(csa);
        EndScroll();
    }

    public void CheckAnswer() {
        if (text.equals(answer)) {
            sound.PlaySuccess();
            re_word.setVisibility(View.INVISIBLE);
            linear_answer.removeAllViews();
            prefs.SaveHintFa(id, 8);
            prefs.SaveHintEn(id, 0);
            if (prefs.GetLevel("le_" + id_level) != level_category) NewShowAnswer(answer);
            count++;
            if (count >= qu.length) SetEnd();
            else SetAnswer();
            if (prefs.GetLevel("le_" + id_level) == level_category) {
                prefs.SaveLe(id_level, level);
                prefs.SaveCount(id_level, count);
            }
        } else {
            isFirst = false;
            sound.PlayWrong();
        }
        text = "";
        txt_check.setVisibility(View.INVISIBLE);
        txt_check.setText("");
    }

    private void SetEnd() {
        scrollView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (prefs.GetLevel("le_" + id_level) == level_category) NewShowAnswer(answer);
        if (array_question != null && array_question.size() > level) {
            score = array_question.get(level).split(" ").length;
        }
        if (array_pic != null && array_pic.size() > level && !array_pic.get(level).equals("null") && customBlur != null) {
            customBlur.img_background.setBlur(0);
            customBlur.CanslTimer();
        }
        if (array_author != null && array_author.size() > level) {
            NewShowAnswer(array_author.get(level));
        }
        if (prefs.GetTalker() && mp_2 != null) {
            mp_2.start();
            GetPlayTalker();
        } else if (prefs.GetTalker() && mp != null) {
            mp.start();
        }

        if (array_id != null && array_id.size() > level) {
            setSentence(array_id.get(level));
        }
        level++;
        count = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { DialoWin(); }
        }, 5000);
    }

    private void GetPlayTalker() {
        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mp_2 == null || !mp_2.isPlaying()) {
                            t.cancel();
                            if (prefs.GetTalker() && mp != null) mp.start();
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    public void DialoWin() {
        sound.PlayLevelEnd();
        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_win);
        d.setCancelable(false);
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RelativeLayout re_up = d.findViewById(R.id.re_up);
        re_up.setVisibility(View.INVISIBLE);

        ((TextView) d.findViewById(R.id.txt_end_level)).setText(String.format("%s%d", getString(R.string.end_level), level));

        d.findViewById(R.id.img_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp != null) mp.stop();
                sound.PlayClick();
                linear_show_answer.removeAllViews();
                GetRandom();
                line_view.SetPaint();
                if (level < array_question.size()) {
                    if (re_blur.getChildCount() > 0) {
                        re_blur.removeAllViews();
                        customBlur = null;
                    }
                    YoYo.with(Techniques.ZoomOut).duration(300).playOn(re_up);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            SetAnswer();
                        }
                    }, 200);
                } else NavigateBack();
            }
        });

        d.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                re_up.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(500).playOn(re_up);
            }
        }, 150);
    }

    public void changeCoin(int amount, String type) {
        CoinManager.addCoin(this, amount, type, new CoinManager.CoinCallback() {
            @Override public void onSuccess() { txt_coin.setText(String.valueOf(prefs.GetCoin())); }
            @Override public void onError(String e) {}
        });
    }

    public void lowCoin(int amount, String type) {
        CoinManager.addCoin(this, amount, type, new CoinManager.CoinCallback() {
            @Override public void onSuccess() { txt_coin.setText(String.valueOf(prefs.GetCoin())); }
            @Override public void onError(String e) {}
        });
    }

    public void DialogReport() {
        dialog_report = new Dialog(this);
        dialog_report.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_report.setContentView(R.layout.dialog_report);
        if (dialog_report.getWindow() != null) dialog_report.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_report.show();
    }

    private void SetReport(String matn) {
        if (array_id == null || array_id.size() <= level) return;
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface api = retrofit.create(ApiInterface.class);
        Call<ServerData.Message> call = api.report(App.Token + prefs.GetToken(), array_id.get(level), matn);
        call.enqueue(new Callback<ServerData.Message>() {
            @Override
            public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {
                if (response.body() != null && !response.body().getSuccess()) AdsTapsell.Dialog(response.body().getMessage(), Game.this);
            }
            @Override public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {}
        });
    }

    private void setSentence(int sid) {
        Sentence s = new Sentence(sid);
        ApiInterface api = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        api.sentence_complete(App.Token + prefs.GetToken(), s).enqueue(new Callback<ServerData.Message>() {
            @Override public void onResponse(@NonNull Call<ServerData.Message> call, @NonNull Response<ServerData.Message> response) {}
            @Override public void onFailure(@NonNull Call<ServerData.Message> call, @NonNull Throwable t) {}
        });
    }

    private void CloseDialog(final Dialog dialog, RelativeLayout re_up) {
        YoYo.with(Techniques.ZoomOut).duration(500).playOn(re_up);
        new Handler().postDelayed(new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }, 400);
    }

    Integer RandomCount(int max) {
        if (max <= 0) return 0;
        if (array_count.size() >= max) array_count.clear();
        Random r = new Random();
        int co = r.nextInt(max);
        while (array_count.contains(co) && array_count.size() < max) {
            co = r.nextInt(max);
        }
        array_count.add(co);
        return co;
    }

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase)); }
    @Override public void onBackPressed() { NavigateBack(); super.onBackPressed(); }
    @Override protected void onResume() { super.onResume(); SetAnimCoin(); SoundPlayer.PlayMusic(); txt_coin.setText(String.valueOf(prefs.GetCoin())); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SetTimerCancel();
        if (mp != null) { mp.release(); mp = null; }
        if (mp_2 != null) { mp_2.release(); mp_2 = null; }
        color = -1;
    }

    public void SetAnimCoin() {
        SetTimerCancel();
        timer_coin = new Timer();
        timer_coin.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override public void run() { YoYo.with(Techniques.Shake).duration(700).playOn(img_coin); }
                });
            }
        }, 5000, 10000);
    }

    public void SetTimerCancel() { if (timer_coin != null) { timer_coin.cancel(); timer_coin = null; } }
    @Override protected void onRestart() { super.onRestart(); FullScreen(); }

    public void FullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) FullScreen();
    }
}
