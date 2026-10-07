package com.game.education.body;

import androidx.annotation.NonNull;

public class SaveBackup
{
    public int coin;
    public int scour_all;
    public int scour_one;
    public int scour_tow;
    public int scour_tree;
    public int scour_four;
    public String user;
    public String lang;
    public String code;
    public String level;
    public String scour_level;
    public String path;
    public String avatar;

    public SaveBackup(int coin,int scour_all,int scour_one,int scour_tow,int scour_tree,int scour_four,String user,String lang,String code,String level,String scour_level,String path
    ,String avatar){
        this.coin = coin;
        this.scour_all = scour_all;
        this.scour_one = scour_one;
        this.scour_tow = scour_tow;
        this.scour_tree = scour_tree;
        this.scour_four = scour_four;
        this.user = user;
        this.lang = lang;
        this.code = code;
        this.level = level;
        this.scour_level = scour_level;
        this.path = path;
        this.avatar = avatar;
    }

    @NonNull
    @Override
    public String toString() {
        return "coin:" + coin + ", scour_all:" + scour_all + ", scour_one:" + scour_one + ", scour_tow:" + scour_tow + ", scour_tree:" + scour_tree + ", scour_four:" + scour_four + ", user:" + user + ", lang:" + lang + ", code:" + code + ", level:" + level + ", scour_level:" + scour_level + ", path:" + path  + ", avatar:" + avatar;
    }
}
