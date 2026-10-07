package com.game.education.model;

import android.app.Activity;
import android.util.Log;
import com.game.education.App;
import com.game.education.api.ApiInterface;
import com.game.education.api.RetrofitClient;
import com.game.education.api.ServerData;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CoinManager {

    public interface CoinCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void addCoin(
            Activity context,
            int amount,
            String type,
            CoinCallback callback
    ) {

        Prefs prefs = new Prefs(context);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        String uuid = UUID.randomUUID().toString();
        AddCoin addCoin = new AddCoin(amount,"Manual credit",uuid);

        Call<ServerData.Message> call = null;
        if(type.equals("add")){
            call = apiInterface.addCoin(App.Token + prefs.GetToken(), addCoin);
        }else if(type.equals("spend")){
            call = apiInterface.spend(App.Token + prefs.GetToken(), addCoin);
        }else {
            call = apiInterface.videoWatched(App.Token + prefs.GetToken(), addCoin);
        }

        call.enqueue(new Callback<ServerData.Message>() {

            @Override
            public void onResponse(Call<ServerData.Message> call,
                                   Response<ServerData.Message> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getSuccess()) {

                    if (response.body().getResult() != null &&
                            response.body().getResult().getItems().getBalance() != null) {

                        prefs.SetCoin(response.body().getResult().getItems().getBalance());
                    }

                    if (callback != null)
                        callback.onSuccess();

                } else {

                    if (callback != null)
                        callback.onError("Server Error");
                }
            }

            @Override
            public void onFailure(Call<ServerData.Message> call, Throwable t) {

                Log.e(App.LogName, t.toString());

                if (callback != null)
                    callback.onError(t.getMessage());
            }
        });
    }

}
