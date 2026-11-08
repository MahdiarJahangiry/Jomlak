package com.game.education.model;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;

import com.game.education.App;
import com.game.education.R;

import java.util.Random;

public class SoundPlayer
{
    public static MediaPlayer music;
    private AudioAttributes audioAttributes;
    final int SOUND_POOL_MAX = 2;
    private static SoundPool soundPool;
    private static int spin_click;
    private static int success;
    private static int wrong;
    private static int monster_jump;
    private static int hint;
    private static int bonus_word;
    private static int level_end;
    private static int select_1,select_2,select_3,select_4,select_5,select_6,select_7,select_8;
    Prefs prefs;
    Activity act;


    public SoundPlayer(Activity activity)
    {
        act = activity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(SOUND_POOL_MAX)
                    .build();
        }else {
            soundPool = new SoundPool(SOUND_POOL_MAX, AudioManager.STREAM_MUSIC,0);
        }

        prefs = new Prefs(activity);

        spin_click = soundPool.load(activity, R.raw.spin_click,1);
        success = soundPool.load(activity, R.raw.success,1);
        wrong = soundPool.load(activity, R.raw.wrong,1);
        monster_jump = soundPool.load(activity, R.raw.monster_jump,1);
        hint = soundPool.load(activity, R.raw.hint,1);
        bonus_word = soundPool.load(activity, R.raw.bonus_word,1);
        level_end = soundPool.load(activity, R.raw.level_end,1);
        select_1 = soundPool.load(activity, R.raw.select_1,1);
        select_2 = soundPool.load(activity, R.raw.select_2,1);
        select_3 = soundPool.load(activity, R.raw.select_3,1);
        select_4 = soundPool.load(activity, R.raw.select_4,1);
        select_5 = soundPool.load(activity, R.raw.select_5,1);
        select_6 = soundPool.load(activity, R.raw.select_6,1);
        select_7 = soundPool.load(activity, R.raw.select_7,1);
        select_8 = soundPool.load(activity, R.raw.select_8,1);


    }


    public void PlayClick()
    {
        soundPool.play(spin_click,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlaySuccess()
    {
        soundPool.play(success,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlayWrong()
    {
        soundPool.play(wrong,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlayMonsterJump()
    {
        soundPool.play(monster_jump,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlayLevelEnd()
    {
        soundPool.play(level_end,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlayHint()
    {
        soundPool.play(hint,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlayBonusWord()
    {
        soundPool.play(bonus_word,prefs.GetVloume(),prefs.GetVloume(),1,0,1.0f);
    }

    public void PlaySelect(int count)
    {
        if (count == 1) {
            soundPool.play(select_1, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 2) {
            soundPool.play(select_2, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 3) {
            soundPool.play(select_3, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 4) {
            soundPool.play(select_4, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 5) {
            soundPool.play(select_5, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 6) {
            soundPool.play(select_6, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 7) {
            soundPool.play(select_7, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }else if (count == 8) {
            soundPool.play(select_8, prefs.GetVloume(), prefs.GetVloume(), 1, 0, 1.0f);
        }
    }

    public void StartMusic()
    {
        if (prefs.GetMusic()) {
            if (prefs.GetVloume() > 0f) {
                StopMusic();
                int counter = prefs.getMusicCounter();
                if(counter == 0){
                    Random rand = new Random();
                    int ss = rand.nextInt(7);
                    if (ss == 0) {
                        music = MediaPlayer.create(App.context, R.raw.song1);
                    } else if (ss == 1) {
                        music = MediaPlayer.create(App.context, R.raw.song2);
                    } else if (ss == 2) {
                        music = MediaPlayer.create(App.context, R.raw.song3);
                    } else if (ss == 3) {
                        music = MediaPlayer.create(App.context, R.raw.song4);
                    } else if (ss == 4) {
                        music = MediaPlayer.create(App.context, R.raw.song5);
                    } else if (ss == 5) {
                        music = MediaPlayer.create(App.context, R.raw.song6);
                    } else {
                        music = MediaPlayer.create(App.context, R.raw.song7);
                    }
                }else {
                    Random rand = new Random();
                    int ss = rand.nextInt(7);
                    if (ss == 0) {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_1);
                    } else if (ss == 1) {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_2);
                    } else if (ss == 2) {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_3);
                    } else if (ss == 3) {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_4);
                    } else if (ss == 4) {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_5);
                    } else if (ss == 5) {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_6);
                    } else {
                        music = MediaPlayer.create(App.context, R.raw.bikalam_7);
                    }
                }

                music.setVolume(prefs.GetVloume(), prefs.GetVloume());
                music.setLooping(true);
                new Handler().postDelayed(music::start, 200);
            }
        }
    }

    public static void StopMusic()
    {
        if (music != null) {
            music.stop();
        }
    }

    public static void Volume(float vo)
    {

        if (music != null) {
            music.setVolume(vo,vo);
        }
    }

    public static void PauseMusic()
    {
        if (music != null) {
            if (music.isPlaying()) {
                music.pause();
            }
        }
    }

    public static void PlayMusic()
    {
        if (music != null) {
            if (!music.isPlaying()) {
                music.start();
            }
        }
    }



}//class
