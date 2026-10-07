package com.game.education.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.game.education.App
import com.game.education.R
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.api.ServerData
import com.game.education.model.*
import com.game.education.model.CoinManager.CoinCallback
import com.jgabrielfreitas.core.BlurImageView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ir.myket.billingclient.IabHelper
import ir.myket.billingclient.util.Purchase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Shop : AppCompatActivity() {

    lateinit var tol: Tollbar
    lateinit var prefs: Prefs
    lateinit var sound: SoundPlayer
    lateinit var linear_left: LinearLayout
    lateinit var linear_right: LinearLayout
    lateinit var li_center: LinearLayout
    lateinit var img_title: ImageView
    var mHelper: IabHelper? = null
    var mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener? = null
    var mGotInventoryListener: IabHelper.QueryInventoryFinishedListener? = null
    var active_mHelper = false
    var active = true
    var coin = 0
    var id = ""
    var shop = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_shop)

        FullScreen()

        val img_background = findViewById<BlurImageView>(R.id.img_background)
        img_background.setImageResource(R.drawable.back_main)

        linear_left  = findViewById(R.id.linear_left)
        linear_right  = findViewById(R.id.linear_right)
        li_center  = findViewById(R.id.li_center)
        img_title  = findViewById(R.id.img_title)
        img_title.setImageResource(R.drawable.sandogh)
        prefs = Prefs(this)
        sound = SoundPlayer(this)
        tol = Tollbar(this)
        tol.txt_title.text = App.con.getResources().getString(R.string.shop)



        try {
            mHelper = IabHelper(this, App.RSA_KEY)
            mHelper!!.startSetup(IabHelper.OnIabSetupFinishedListener { result ->
                if (!result.isSuccess) {
                    AdsTapsell.Dialog(App.con.resources.getString(R.string.bazar_eror_1), this@Shop)
                    return@OnIabSetupFinishedListener
                }
                mHelper!!.queryInventoryAsync(mGotInventoryListener)
                active_mHelper = true
            })
        } catch (e: Exception) {
            AdsTapsell.Dialog(App.con.resources.getString(R.string.bazar_eror_1), this@Shop)
        }


        mGotInventoryListener = IabHelper.QueryInventoryFinishedListener { result, inv ->
            if (result.isFailure) {
                Log.d(App.LogName, "Failed to query inventory: \$result")
                return@QueryInventoryFinishedListener
            } else {
                Log.d(App.LogName, "Query inventory was successful.")
                // does the user have the premium upgrade?
                // update UI accordingly
            }
        }

        mPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
            if (result.isFailure) {
                shop = true
                AdsTapsell.Dialog(App.con.resources.getString(R.string.bazar_eror_3), this@Shop)
                return@OnIabPurchaseFinishedListener
            }
            if (!verifyDeveloperPayload(purchase)) {
                AdsTapsell.Dialog(App.con.resources.getString(R.string.bazar_eror_4), this@Shop)
                shop = true
                return@OnIabPurchaseFinishedListener
            }
            if (purchase.sku == id) {
                if (id == "no_ads"){
                    prefs.SaveAds()
                }else {
                    changeCoin(coin,"add", purchase)
                }
            }
            shop = true
        }


        GetDate()

    }//main


    fun BuyCoin(key: String, co: Int) {
        if(shop) {
            if (active_mHelper) {
                id = key
                coin = co
                shop = false
                mHelper?.launchPurchaseFlow(this, key, mPurchaseFinishedListener,  prefs.GetUsername())
            }else{
                AdsTapsell.Dialog(App.con.resources.getString(R.string.bazar_eror_4), this@Shop)
            }
        }
    }

    fun changeCoin(amount: Int, type: String, purchase: Purchase) {
        CoinManager.addCoin(this, amount, type, object : CoinCallback {
            override fun onSuccess() {
                AdsTapsell.Dialog(
                    coin.toString() + App.con.resources.getString(R.string.add_coin),
                    this@Shop
                )
                MasraMahsol(purchase)
                sound.PlayHint()
            }

            override fun onError(error: String?) {
                AdsTapsell.Dialog(App.con.resources.getString(R.string.bazar_eror_3), this@Shop)
            }
        })
    }

    fun GetDate() {
        val retrofit = RetrofitClient.getRetrofitInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        val call = apiInterface.packages(App.Token + prefs.GetToken(), prefs.GetLang())
        val callback: Callback<ServerData.Message> = object : Callback<ServerData.Message> {
            override fun onResponse(call: Call<ServerData.Message>, response: Response<ServerData.Message>)
            {
                if (response.body()!!.success){
                    for (i in 0 until response.body()!!.result.packages.size){
                        val customShop = CustomShop(this@Shop)
                        customShop.id = response.body()!!.result.packages.get(i).id
                        customShop.title = response.body()!!.result.packages.get(i).fa_title
                        customShop.coin = response.body()!!.result.packages.get(i).coins
                        customShop.txt_price.text = response.body()!!.result.packages.get(i).price.toString() + App.con.getResources().getString(R.string.toman)
                        if (response.body()!!.result.packages.get(i).coins == 0){
                            customShop.img_coin.visibility = View.GONE
                            customShop.txt_coin.text = App.con.getResources().getString(R.string.no_ads)
                        }else{
                            customShop.txt_coin.text = response.body()!!.result.packages.get(i).coins.toString() + App.con.getResources().getString(R.string.coin)
                        }
                        Glide.with(this@Shop)
                            .load(response.body()!!.result.packages.get(i).icon)
                            .into(customShop.img_shop)
                        if (i == 0){
                            active = false
                            linear_right.addView(customShop)
                        }else if (i == 1){
                            active = true
                            linear_left.addView(customShop)
                        }else{
                            li_center.addView(customShop)
                        }
                    }
                }
                if (response.code() == 401) {
                    prefs.SetUsername("")
                    AdsTapsell.Dialog(App.con.resources.getString(R.string.login_token), this@Shop)
                }
            }

            override fun onFailure(call: Call<ServerData.Message>, t: Throwable) {
                Log.e(App.LogName, t.toString())
                AdsTapsell.Dialog(App.con.resources.getString(R.string.internet), this@Shop)
            }
        }
        call.enqueue(callback)
    }

    fun MasraMahsol(purchase: Purchase)
    {
        mHelper?.consumeAsync(purchase) { purchase, result ->
            if (result.isSuccess) {

            }
        }
    }

    fun verifyDeveloperPayload(p: Purchase): Boolean {
        return p.developerPayload == prefs.GetUsername()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    override fun onResume() {
        super.onResume()
        SoundPlayer.PlayMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHelper?.dispose()
        mHelper = null
    }

    override fun onPause() {
        super.onPause()
        SoundPlayer.PauseMusic()
    }


    override fun onRestart() {
        super.onRestart()
        FullScreen()
    }

    fun FullScreen() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            FullScreen()
        }
    }

}//class