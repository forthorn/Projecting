package com.forthorn.projecting.api;

import com.forthorn.projecting.baserx.BaseResponse;
import com.forthorn.projecting.entity.IMAccount;
import com.forthorn.projecting.entity.TaskList;
import com.forthorn.projecting.entity.TaskRes;
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
     * 统一更新信息
     */
    @FormUrlEncoded
    @POST("/v1/android/ad/update_ad_status")
    Call<BaseResponse> updateStatus(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") int equipmentId,
            @Field("is_sleep") int isSleep,
            @Field("volume") int volume
    );

    /**
     * 获取天气
     */
    @FormUrlEncoded
    @POST("/api/v1/android/weather/weather")
    Observable<BaseResponse> getWeather(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipmentId,
            @Field("equipment_code") String equipmentCode
    );

    /**
     * 获取整个时段的广告
     */
    @FormUrlEncoded
    @POST("/api/v1/android/ad/ad_time")
    Call<TaskRes> getTaskList(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipmentId,
            @Field("date") String date
    );

    /**
     * 获取账户密码
     */
    @FormUrlEncoded
    @POST("/api/v1/android/weather/im")
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
    Call<BaseResponse> setSleep(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipment_id,
            @Field("equipment_code") String equipment_code
    );


    /**
     * 唤醒
     */
    @FormUrlEncoded
    @POST("/api/v1/android/ad/wake_up")
    Call<BaseResponse> setWakeUp(
            @Header("Cache-Control") String cacheControl,
            @Field("equipment_id") String equipment_id,
            @Field("equipment_code") String equipment_code
    );


    /**
     * 上传截图
     */
    @Multipart
    @POST("/api/v1/android/ad/upload_z")
    Call<BaseResponse> uploadSnapshoot(
            @Header("Cache-Control") String cacheControl,
            @Part("capture_time") RequestBody capture_time,
            @Part("equipment_id") RequestBody equipment_id,
            @Part MultipartBody.Part attachment);

//    MultipartBody.Part avatarPt = MultipartBody.Part.createFormData("file",
//            Md5Security.getMD5(avatarPath) + ".jpeg", avatarRB);


}
