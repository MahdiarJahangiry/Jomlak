package com.game.education;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import java.util.Locale;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.calligraphy3.R;
import io.github.inflationx.viewpump.ViewPump;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.TapsellPlusInitListener;
import ir.tapsell.plus.model.AdNetworkError;
import ir.tapsell.plus.model.AdNetworks;

public class App extends Application
{
    public static String LogName = "LogName";
    public static String RSA_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCzfIm5OxNbMtR4kcM3h214EWkr85sjdCnNvOEymajhv5dCHYhTPfauLu0QcjNY+d22k+kQH/JuKm/O4W4WGAxNRVEVT45B9menJBjW14tpBO3rkdHq3fHNkkxPulri65yWu3LRPgWM40v0ok2YVks6rpocfkLwy2aI3xNnReItyQIDAQAB";
    public static String ZONE_ID_VIDEO = "63cfe3bd3a3a0713523e17ee";
    public static String Token = "Bearer ";
    public static String VERSIONNAME = "1.0";
    public static int VERSIONCODE = 1;
    public static int Blur = 20;
    public static int Davat = 100;
    public static int IdBack = 1;
    public static int Video = 1;
    public static int Hint = 25;
    public static int Magnet = 25;
    public static int Register = 200;
    public static String Lang = "fa";
    public static String Pass = "NB)A@UpKzDx5gqmSn8E2fy+WdCwcLrvs";
    @SuppressLint("StaticFieldLeak")
    public static Context context,con;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        setLocale();
        String TAPSELL_KEY = "ensqjccdlltprfiqrgftntmeoamsiokriadndrlajchctmhofeakhtiidpbhqtsjhdsshd";
        TapsellPlus.initialize(this, TAPSELL_KEY,
                new TapsellPlusInitListener() {
                    @Override
                    public void onInitializeSuccess(AdNetworks adNetworks) {
                        Log.d("onInitializeSuccess", adNetworks.name());
                        TapsellPlus.setGDPRConsent(context, true);
                    }

                    @Override
                    public void onInitializeFailed(AdNetworks adNetworks,
                                                   AdNetworkError adNetworkError) {
                        Log.e("onInitializeFailed", "ad network: " + adNetworks.name() + ", error: " +	adNetworkError.getErrorMessage());
                    }
                });

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/iransansmobile.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            VERSIONCODE = pInfo.versionCode;
            VERSIONNAME = "ver " + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static boolean isConnected()
    {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }


    public static void setLocale()
    {
        SharedPreferences shared = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        App.Lang = shared.getString("language", "fa");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            con = updateResources(context, Lang);
        }else {
            // for devices having lower version of android os
            con = updateResourcesLegacy(context, Lang);
        }
    }


    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }




}