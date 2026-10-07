package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.game.education.App;

public class Prefs
{
    SharedPreferences shared;
    SharedPreferences.Editor editor;
    Activity act;

    public Prefs(Activity activity)
    {
        act = activity;

        shared = App.context.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        editor = shared.edit();

    }

    public void Clear()
    {
        editor.clear();
        editor.commit();
    }

    public void SaveTitleLevel(String le)
    {
        boolean ti = true;
        String title = shared.getString("title_level", "");
        if (title.equals("")){
            editor.putString("title_level",le);
            editor.commit();
        }else {
            String[] str = title.split(",");
            for (int i=0; i<str.length; i++){
                if (str[i].equals(le)){
                    ti = false;
                }
            }
            if (ti){
                editor.putString("title_level",title + "," +le);
                editor.commit();
            }
        }
    }

    public String GetTitleLevel()
    {
        return shared.getString("title_level", "");
    }

    public void AddLevel(String le)
    {
        int level = shared.getInt(le, 1);
        int add = level + 1;
        editor.putInt(le,add);
        editor.commit();
        SaveTitleLevel(le);
    }

    public int GetLevel(String le)
    {
        return shared.getInt(le, 1);
    }

    public void SetLevel(String le,int ad)
    {
        editor.putInt(le,ad);
        editor.commit();
    }

    public void AddCoin(int ad)
    {
        int coin = shared.getInt("coin", 300);
        int add = coin + ad;
        editor.putInt("coin",add);
        editor.commit();
    }

    public void LowCoin(int ad)
    {
        int coin = shared.getInt("coin", 300);
        int add = coin - ad;
        editor.putInt("coin",add);
        editor.commit();
    }

    public int GetCoin()
    {
        return shared.getInt("coin", 300);
    }


    public void SetCoin(int co)
    {
        editor.putInt("coin",co);
        editor.commit();
    }


    public void SetScourAll(int ad)
    {
        editor.putInt("scour_all",ad);
        editor.commit();
    }

    public void AddScour()
    {
        int scour = shared.getInt("scour_all", 0);
        int add = scour + 1;
        editor.putInt("scour_all",add);
        editor.commit();
    }

    public int GetScour()
    {
        return shared.getInt("scour_all", 0);
    }



    public void SetScourOne(int ad)
    {
        editor.putInt("scour_one",ad);
        editor.commit();
    }

    public void AddScourOne()
    {
        int scour = shared.getInt("scour_one", 0);
        int add = scour + 1;
        editor.putInt("scour_one",add);
        editor.commit();
    }

    public int GetScourOne()
    {
        return shared.getInt("scour_one", 0);
    }

    public void SetScourTow(int ad)
    {
        editor.putInt("scour_tow",ad);
        editor.commit();
    }

    public void AddScourTow()
    {
        int scour = shared.getInt("scour_tow", 0);
        int add = scour + 1;
        editor.putInt("scour_tow",add);
        editor.commit();
    }

    public int GetScourTow()
    {
        return shared.getInt("scour_tow", 0);
    }

    public void SetScourTree(int ad)
    {
        editor.putInt("scour_tree",ad);
        editor.commit();
    }

    public void AddScourTree()
    {
        int scour = shared.getInt("scour_tree", 0);
        int add = scour + 1;
        editor.putInt("scour_tree",add);
        editor.commit();
    }

    public int GetScourTree()
    {
        return shared.getInt("scour_tree", 0);
    }


    public void SetScourFour(int ad)
    {
        editor.putInt("scour_four",ad);
        editor.commit();
    }

    public void AddScourFour()
    {
        int scour = shared.getInt("scour_four", 0);
        int add = scour + 1;
        editor.putInt("scour_four",add);
        editor.commit();
    }

    public int GetScourFour()
    {
        return shared.getInt("scour_four", 0);
    }

    public String GetName()
    {
        return shared.getString("name", "");
    }


    public void SetName(String str)
    {
        editor.putString("name",str);
        editor.commit();
    }

    public String GetLang()
    {
        return shared.getString("language", "fa");
    }


    public void SetLang(String str)
    {
        editor.putString("language",str);
        editor.commit();
    }

    public String GetToken()
    {
        return shared.getString("token", "");
    }


    public void SetToken(String str)
    {
        editor.putString("token",str);
        editor.commit();
    }

    public String GetUsername()
    {
        return shared.getString("username", "");
    }


    public void SetUsername(String str)
    {
        editor.putString("username",str);
        editor.commit();
    }

    public String GetEmail()
    {
        return shared.getString("email", "");
    }


    public void SetEmail(String str)
    {
        editor.putString("email",str);
        editor.commit();
    }

    public String GetPass()
    {
        return shared.getString("pass", "");
    }

    public void saveMusicCounter(int count)
    {
        editor.putInt("music_counter",count);
        editor.commit();
    }

    public int getMusicCounter()
    {
        return shared.getInt("music_counter", 0);
    }


    public void SetPass(String str)
    {
        editor.putString("pass",str);
        editor.commit();
    }

    public String GetAvatar()
    {
        return shared.getString("avatar", "");
    }


    public void SetAvatar(String str)
    {
        editor.putString("avatar",str);
        editor.commit();
    }

    public void SaveCode(String str)
    {
        editor.putString("code",str);
        editor.commit();
    }

    public String GetCode()
    {
        return shared.getString("code", "");
    }


    public void SaveTalker(boolean fa)
    {
        editor.putBoolean("talker",fa);
        editor.commit();
    }

    public boolean GetTalker()
    {
        return shared.getBoolean("talker", true);
    }

    public void SaveMusic(boolean fa)
    {
        editor.putBoolean("music",fa);
        editor.commit();
    }

    public boolean GetMusic()
    {
        return shared.getBoolean("music", true);
    }

    public void SetVloume(float str)
    {
        editor.putFloat("vloume",str);
        editor.commit();
    }

    public Float GetVloume()
    {
        return shared.getFloat("vloume", 1.0f);
    }


    public void saveMagnet(int id, int level, boolean fa)
    {
        editor.putBoolean("isMagnet_"+id+"_"+level,fa);
        editor.commit();
    }

    public boolean getMagnet(int id, int level)
    {
        return shared.getBoolean("isMagnet_"+id+"_"+level, false);
    }

    public void SaveLe(int id,int ad)
    {
        editor.putInt("save_le_"+id,ad);
        editor.commit();
    }

    public Integer GetLe(int id)
    {
        return shared.getInt("save_le_"+id, 0);
    }

    public void SaveCount(int id,int ad)
    {
        editor.putInt("save_count_"+id,ad);
        editor.commit();
    }

    public Integer GetCount(int id)
    {
        return shared.getInt("save_count_"+id, 0);
    }


    public void SaveHintFa(int le,int ad)
    {
        editor.putInt("save_hint_fa_"+le,ad);
        editor.commit();
    }

    public Integer GetHintFa(int le)
    {
        return shared.getInt("save_hint_fa_"+le, 8);
    }

    public void SaveHintEn(int le,int ad)
    {
        editor.putInt("save_hint_en_" + le,ad);
        editor.commit();
    }

    public Integer GetHintEn(int le)
    {
        return shared.getInt("save_hint_en_" + le, 0);
    }

    public void SaveAbout(String str)
    {
        editor.putString("about",str);
        editor.commit();
    }

    public String GetAbout()
    {
        return shared.getString("about", "");
    }

    public void SaveAboutVersion(String str)
    {
        editor.putString("about_version",str);
        editor.commit();
    }

    public String GetAboutVersion()
    {
        return shared.getString("about_version", "");
    }

    public void SaveAboutTelegram(String str)
    {
        editor.putString("about_telegram",str);
        editor.commit();
    }

    public String GetAboutTelegram()
    {
        return shared.getString("about_telegram", "");
    }

    public void SaveAboutInsta(String str)
    {
        editor.putString("about_insta",str);
        editor.commit();
    }

    public String GetAboutInsta()
    {
        return shared.getString("about_insta", "");
    }

    public void SaveAboutPhone(String str)
    {
        editor.putString("about_phone",str);
        editor.commit();
    }

    public String GetAboutPhone()
    {
        return shared.getString("about_phone", "");
    }

    public void SaveEmailSupport(String str)
    {
        editor.putString("email_support",str);
        editor.commit();
    }

    public String GetEmailSupport()
    {
        return shared.getString("email_support", "");
    }

    public void SavePath(String str)
    {
        editor.putString("path",str);
        editor.commit();
    }

    public String GetPath()
    {
        return shared.getString("path", "");
    }

    public void SaveAds()
    {
        editor.putBoolean("no_ads",false);
        editor.commit();
    }

    public Boolean GetAds()
    {
        return shared.getBoolean("no_ads", true);
    }

    public String GetBadge()
    {
        return shared.getString("badge_all", "");
    }

    public void SetBadge(String ad)
    {
        boolean ti = true;
        String bad = shared.getString("badge_all", "");
        if (bad.equals("")){
            bad = ad;
            editor.putString("badge_all",bad);
            editor.commit();
        }else {
            String[] str = bad.split(",");
            for (int i=0; i<str.length; i++){
                if (str[i].equals(ad)){
                    ti = false;
                }
            }
            if (ti){
                bad += "," + ad;
                editor.putString("badge_all",bad);
                editor.commit();
            }
        }
    }

    public void SaveGoogle(boolean fa)
    {
        editor.putBoolean("google",fa);
        editor.commit();
    }

    public boolean GetGoogle()
    {
        return shared.getBoolean("google", false);
    }


    public void SaveEmailVerified(boolean fa)
    {
        editor.putBoolean("emailVerified",fa);
        editor.commit();
    }

    public boolean GetEmailVerified()
    {
        return shared.getBoolean("emailVerified", false);
    }

}//class
