package com.forthorn.projecting.api;

import com.forthorn.projecting.baserx.BaseResponse;
import com.forthorn.projecting.entity.IMAccount;
import com.forthorn.projecting.entity.UserList;

import java.io.File;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import rx.Observable;

/**
 * des:ApiService
 */
public interface ApiService {

    /**
     * 获取联系人列表
     */
    @GET("/v1/users/")
    Call<UserList> getContactList(
            @Header("Cache-Control") String cacheControl,
            @Header("Authorization") String auth,
            @Query("start") String start,
            @Query("count") String count
    );


    /**
     * 获取天气
     */
    @FormUrlEncoded
    @POST("/advert_online/basic/api/v1/android/weather/weather")
    Observable<BaseResponse> getWeather(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipmentId,
            @Field("equipment_code") String equipmentCode
    );

    /**
     * 获取账户密码
     */
    @FormUrlEncoded
    @POST("/advert_online/basic/api/v1/android/weather/im")
    Call<IMAccount> getIMAccount(
            @Header("Cache-Control") String cacheControl,
            @Field("code") String equipmentCode
    );

    /**
     * 设置音量
     */
    @FormUrlEncoded
    @POST("/api/v1/android/ad/volume")
    Call<BaseResponse> setVolume(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipment_id,
            @Field("equipment_code") String equipment_code,
            @Field("volume") String volume
    );

    /**
     * 休眠
     */
    @FormUrlEncoded
    @POST("/api/v1/android/ad/sleep")
    Observable<BaseResponse> setSleep(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipment_id,
            @Field("equipment_code") String equipment_code
    );


    /**
     * 休眠
     */
    @FormUrlEncoded
    @POST("/api/v1/android/ad/wake_up")
    Observable<BaseResponse> setWakeUp(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipment_id,
            @Field("equipment_code") String equipment_code
    );


    /**
     * 上传截图
     */
    @Multipart
    @POST("/api/v1/android/ad/upload_z")
    Observable<BaseResponse> uploadSnapshoot(
            @Header("Cache-Control") String cacheControl,
            @Field("capture_time") String capture_time,
            @Field("equipment_id") String equipment_id,
            @Part MultipartBody.Part  attachment);

//    MultipartBody.Part avatarPt = MultipartBody.Part.createFormData("file",
//            Md5Security.getMD5(avatarPath) + ".jpeg", avatarRB);



}
