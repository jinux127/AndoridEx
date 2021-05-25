package com.jointree.wifilist.RetrofitService;

import com.jointree.wifilist.DTO.RetrofitTestDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitService {

    @POST("adminMeasurement.do")
    @Headers("Content-Type: application/json")
    Call<String> getDatas(@Body String wifiList);

    @POST("tani.do")
    @Headers("Content-Type: application/json")
    Call<String> scanLocation(@Body String wifiList);

    @POST("test.do")
    @Headers("Content-Type: application/json")
    Call<String> scanLocation2(@Body String wifiList);

}
