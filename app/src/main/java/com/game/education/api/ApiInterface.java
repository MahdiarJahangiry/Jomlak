package com.game.education.api;

import com.game.education.body.SaveBackup;
import com.game.education.model.AddCoin;
import com.game.education.model.Sentence;
import com.game.education.model.UpdateEmail;
import com.game.education.model.Verify;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface
{
    // ============================================
    // ✅ متدهای ربات (بدون توکن)
    // ============================================

    @GET("v1/titles")
    Call<ServerData.TitleResponse> titles(
            @Query("lang") String lang
    );

    @GET("v1/categories")
    Call<ServerData.CategoryResponse> categories(
            @Query("titleId") int titleId,
            @Query("lang") String lang
    );

    @GET("v1/bases")
    Call<ServerData.BaseResponse> bases(
            @Query("categoryId") int categoryId,
            @Query("lang") String lang
    );

    @GET("v1/chapters")
    Call<ServerData.ChapterResponse> chapters(
            @Query("baseId") int baseId,
            @Query("lang") String lang
    );

    @GET("v1/sentences")
    Call<ServerData.SentenceResponse> sentences(
            @Query("chapterId") int chapterId,
            @Query("lang") String lang
    );

    @POST("v1/sentences/complete")
    Call<ServerData.Message> sentences_complete(
            @Header("Authorization") String auth,
            @Body java.util.Map<String, Object> body
    );

    // ============================================
    // ✅ متدهای اپلیکیشن اصلی (با توکن) - نام‌ها اصلاح شد
    // ============================================

    @Headers("Content-Type: application/json")
    @POST("v1/register")
    Call<ServerData.Message> register(
            @Query("username") String username,
            @Query("password") String password,
            @Query("affiliate_code") String affiliate_code
    );

    @Headers("Content-Type: application/json")
    @POST("v1/login")
    Call<ServerData.Message> login(
            @Query("username") String username,
            @Query("password") String password
    );

    @Headers("Content-Type: application/json")
    @POST("v1/login-email")
    Call<ServerData.Message> login_email(
            @Query("username") String username,
            @Query("password") String password
    );

    @Headers("Content-Type: application/json")
    @GET("v1/version")
    Call<ServerData.Message> version();

    @Headers("Content-Type: application/json")
    @GET("v1/titles")
    Call<ServerData.levels> titles_with_token(
            @Header("Authorization") String token,
            @Query("lang") String lang
    );

    @Headers("Content-Type: application/json")
    @GET("v1/categories")
    Call<ServerData.levels> categories_with_token(
            @Header("Authorization") String token,
            @Query("lang") String lang,
            @Query("titleId") Integer titleId
    );

    @Headers("Content-Type: application/json")
    @GET("v1/bases")
    Call<ServerData.levels> bases_with_token(
            @Header("Authorization") String token,
            @Query("lang") String lang,
            @Query("categoryId") Integer categoryId
    );

    @Headers("Content-Type: application/json")
    @GET("v1/sentences")
    Call<ServerData.levels> sentences_with_token(
            @Header("Authorization") String token,
            @Query("lang") String lang,
            @Query("chapterId") Integer chapterId,
            @Query("page") Integer page
    );

    @Headers("Content-Type: application/json")
    @GET("v1/chapters")
    Call<ServerData.levels> chapters_with_token(
            @Header("Authorization") String token,
            @Query("lang") String lang,
            @Query("baseId") Integer baseId
    );

    @Headers("Content-Type: application/json")
    @POST("v1/backup/save")
    Call<ServerData.Message> backup_save(
            @Header("Authorization") String token,
            @Body SaveBackup saveBackup
    );

    @Headers("Content-Type: application/json")
    @GET("v1/backup/get")
    Call<ServerData.Message> backup_get(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @GET("v1/packages")
    Call<ServerData.Message> packages(
            @Header("Authorization") String token,
            @Query("lang") String lang
    );

    @Headers("Content-Type: application/json")
    @GET("v1/profile")
    Call<ServerData.Data> profile(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @GET("v1/init")
    Call<ServerData.Message> init_affiliate(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @GET("v1/about")
    Call<ServerData.Message> about();

    @Headers("Content-Type: application/json")
    @POST("v1/affiliate/use")
    Call<ServerData.Message> affiliate(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("v1/report/issue")
    Call<ServerData.Message> report(
            @Header("Authorization") String token,
            @Query("sentence_id") Integer sentence_id,
            @Query("reason") String reason
    );

    @Multipart
    @POST("v1/profile/update")
    Call<ServerData.Message> profile_update(
            @Header("Authorization") String token,
            @Query("name") String name,
            @Query("age") String age,
            @Part MultipartBody.Part image,
            @Query("about_me") String about_me,
            @Query("surname") String surname,
            @Query("birthday") String birthday,
            @Query("gender") String gender,
            @Query("password") String password
    );

    @Headers("Content-Type: application/json")
    @GET("v1/wallet/balance")
    Call<ServerData.Message> getBalance(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("v1/wallet/add")
    Call<ServerData.Message> addCoin(
            @Header("Authorization") String token,
            @Body AddCoin addCoin
    );

    @Headers("Content-Type: application/json")
    @POST("v1/wallet/spend")
    Call<ServerData.Message> spend(
            @Header("Authorization") String token,
            @Body AddCoin addCoin
    );

    @Headers("Content-Type: application/json")
    @POST("v1/wallet/video-watched")
    Call<ServerData.Message> videoWatched(
            @Header("Authorization") String token,
            @Body AddCoin addCoin
    );

    @Headers("Content-Type: application/json")
    @POST("v1/email/update")
    Call<ServerData.Message> updateEmail(
            @Header("Authorization") String token,
            @Body UpdateEmail updateEmail
    );

    @Headers("Content-Type: application/json")
    @POST("v1/email/verification/send")
    Call<ServerData.Message> sendVerificationEmail(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("v1/email/verify")
    Call<ServerData.Message> verify(
            @Header("Authorization") String token,
            @Body Verify verify
    );

    @Headers("Content-Type: application/json")
    @POST("v1/sentences/complete")
    Call<ServerData.Message> sentence_complete(
            @Header("Authorization") String token,
            @Body Sentence sentence
    );
}