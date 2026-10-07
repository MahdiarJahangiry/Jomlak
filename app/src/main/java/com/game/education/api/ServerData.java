package com.game.education.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ServerData {

    // ============ کلاس‌های موجود (بدون تغییر) ============

    public class SaveBack {
        @SerializedName("apiVersion") String apiVersion;
        @SerializedName("success") Boolean success;
        public String getApiVersion() { return apiVersion; }
        public Boolean getSuccess() { return success; }
    }

    public static class Message {
        @SerializedName("apiVersion") String apiVersion;
        @SerializedName("success") Boolean success;
        @SerializedName("result") Result result;
        @SerializedName("error") String error;
        @SerializedName("paginate") Boolean paginate;
        @SerializedName("message") String message;

        public String getApiVersion() { return apiVersion; }
        public Boolean getSuccess() { return success; }
        public Result getResult() { return result; }
        public String getError() { return error; }
        public Boolean getPaginate() { return paginate; }
        public String getMessage() { return message; }
    }

    public static class Result {
        @SerializedName("token") String token;
        @SerializedName("version") String version;
        @SerializedName("packages") List<Packages> packages;
        @SerializedName("coin") Integer coin;
        @SerializedName("scour_all") Integer scour_all;
        @SerializedName("scour_one") Integer scour_one;
        @SerializedName("scour_tow") Integer scour_tow;
        @SerializedName("scour_tree") Integer scour_tree;
        @SerializedName("scour_four") Integer scour_four;
        @SerializedName("user") String user;
        @SerializedName("lang") String lang;
        @SerializedName("code") String code;
        @SerializedName("level") String level;
        @SerializedName("scour_level") String scour_level;
        @SerializedName("path") String path;
        @SerializedName("avatar") String avatar;
        @SerializedName("items") ItemsProfile items;
        @SerializedName("affiliateCode") String affiliateCode;

        public String getAffiliateCode() { return affiliateCode; }
        public String getAvatar() { return avatar; }
        public String getPath() { return path; }
        public ItemsProfile getItems() { return items; }
        public String getScour_level() { return scour_level; }
        public String getLevel() { return level; }
        public Integer getCoin() { return coin; }
        public Integer getScour_all() { return scour_all; }
        public Integer getScour_one() { return scour_one; }
        public Integer getScour_tow() { return scour_tow; }
        public Integer getScour_tree() { return scour_tree; }
        public Integer getScour_four() { return scour_four; }
        public String getUser() { return user; }
        public String getLang() { return lang; }
        public String getCode() { return code; }
        public String getToken() { return token; }
        public List<Packages> getPackages() { return packages; }
        public String getVersion() { return version; }
    }

    // ============ کلاس‌های دیگر (بدون تغییر) ============

    public class Packages {
        @SerializedName("id") int id;
        @SerializedName("coins") int coins;
        @SerializedName("title") String fa_title;
        @SerializedName("icon") String icon;
        @SerializedName("price") int price;

        public int getId() { return id; }
        public int getCoins() { return coins; }
        public String getFa_title() { return fa_title; }
        public String getIcon() { return icon; }
        public int getPrice() { return price; }
    }

    public class levels {
        @SerializedName("items") List<Items> items;
        public List<Items> getItems() { return items; }
    }

    public class Data {
        @SerializedName("items") ItemsProfile items;
        public ItemsProfile getItems() { return items; }
    }

    public static class ItemsProfile {
        @SerializedName("username") String username;
        @SerializedName("name") String name;
        @SerializedName("age") String age;
        @SerializedName("avatar") String avatar;
        @SerializedName("email") String email;
        @SerializedName("email_verified") Boolean email_verified;
        @SerializedName("about_me") String about_me;
        @SerializedName("surname") String surname;
        @SerializedName("birthday") String birthday;
        @SerializedName("gender") String gender;
        @SerializedName("affiliate_code") String affiliate_code;
        @SerializedName("not_used_affiliate") Integer not_used_affiliate;
        @SerializedName("balance") Integer balance;
        @SerializedName("about") List<About> about;

        public String getEmail() { return email; }
        public Boolean getEmail_verified() { return email_verified; }
        public List<About> getAbout() { return about; }
        public Integer getNot_used_affiliate() { return not_used_affiliate; }
        public Integer getBalance() { return balance; }
        public String getAffiliate_code() { return affiliate_code; }
        public String getUsername() { return username; }
        public String getName() { return name; }
        public String getAge() { return age; }
        public String getAvatar() { return avatar; }
        public String getAbout_me() { return about_me; }
        public String getSurname() { return surname; }
        public String getBirthday() { return birthday; }
        public String getGender() { return gender; }
    }

    public class About {
        @SerializedName("title") String title;
        @SerializedName("value") String value;
        public String getTitle() { return title; }
        public String getValue() { return value; }
    }

    public class Items {
        @SerializedName("id") int id;
        @SerializedName("title") String title;
        @SerializedName("avatar") String avatar;
        @SerializedName("voice") String voice;
        @SerializedName("sponsor") Sponsor sponsor;
        @SerializedName("celebrity") Celebrity celebrity;
        @SerializedName("levelCount") int levelCount;

        public Sponsor getSponsor() { return sponsor; }
        public int getLevelCount() { return levelCount; }
        public Celebrity getCelebrity() { return celebrity; }
        public String getAvatar() { return avatar; }
        public String getVoice() { return voice; }
        public int getId() { return id; }
        public String getTitle() { return title; }
    }

    public class Sponsor {
        @SerializedName("title") String title;
        @SerializedName("image") String image;
        public String getTitle() { return title; }
        public String getImage() { return image; }
    }

    public class Celebrity {
        @SerializedName("id") int id;
        @SerializedName("title") String title;
        @SerializedName("avatar") String avatar;
        @SerializedName("voice") String voice;

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAvatar() { return avatar; }
        public String getVoice() { return voice; }
    }

    // ============================================
    // ✅ کلاس‌های ربات - مستقیماً زیر ServerData (نه داخل ItemsProfile!)
    // ============================================

    public static class Title {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
        @SerializedName("name") public String name;
    }

    public static class Category {
        @SerializedName("id") public int id;
        @SerializedName("name") public String name;
        @SerializedName("title") public String title;
        @SerializedName("titleId") public int titleId;
    }

    public static class Base {
        @SerializedName("id") public int id;
        @SerializedName("name") public String name;
        @SerializedName("title") public String title;
        @SerializedName("categoryId") public int categoryId;
    }

    public static class Chapter {
        @SerializedName("id") public int id;
        @SerializedName("name") public String name;
        @SerializedName("title") public String title;
        @SerializedName("baseId") public int baseId;
    }

    public static class Sentence {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
        @SerializedName("chapterId") public int chapterId;
    }

    public static class TitleResponse {
        @SerializedName("success") public Boolean success;
        @SerializedName("result") public List<Title> result;
    }

    public static class CategoryResponse {
        @SerializedName("success") public Boolean success;
        @SerializedName("result") public List<Category> result;
    }

    public static class BaseResponse {
        @SerializedName("success") public Boolean success;
        @SerializedName("result") public List<Base> result;
    }

    public static class ChapterResponse {
        @SerializedName("success") public Boolean success;
        @SerializedName("result") public List<Chapter> result;
    }

    public static class SentenceResponse {
        @SerializedName("success") public Boolean success;
        @SerializedName("items") public List<Sentence> items;
        @SerializedName("result") public List<Sentence> result;
    }
}