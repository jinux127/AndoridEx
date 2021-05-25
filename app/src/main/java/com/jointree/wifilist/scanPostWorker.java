package com.jointree.wifilist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jointree.wifilist.RetrofitService.RetrofitService;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class scanPostWorker extends Worker {

    String SERVER_URL = BuildConfig.SERVER_URL;

    String wifiBaseURL = SERVER_URL + "wifi/";

    private static String TAG = "scanPostWorker";
    Call<String> call;


    public scanPostWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data data = getInputData();
        String json = getInputData().getString("json");
        int flag = getInputData().getInt("flag",0);


        //       gosn 선언
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

//        okhttp로깅
        // Create a new object from HttpLoggingInterceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        // Add Interceptor to HttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();


//        Retrofit 연습
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(wifiBaseURL) // baseurl 생성 마지막에 "/" 필수
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create(gson)) //JSON변환해줄 Gson변환기 등록
                .client(client)
                .build();

//        Retrofit 인스턴스 인터페이스 객체 구현
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        Log.d("CCC","flag: "+ flag);

//        인터페이스 연결
        if (flag ==1){
            call = retrofitService.getDatas(json);
        }else if (flag ==2){
            call = retrofitService.scanLocation(json);
        }else if (flag ==3){
            call = retrofitService.scanLocation2(json);

        }

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call< String> call, Response<String> response) {
                if (response.isSuccessful()){
                    Log.d("Retrofit", "onResponse: body(), 결과:"+response.body());
                }else {
                    Log.d("Retrofit", "onResponse: 실패");
                }
            }

            @Override
            public void onFailure(Call< String> call, Throwable t) {
                Log.d("Retrofit", "onFailure 통신실패 : " + t.getMessage());

            }

        });
//        Data data1 = new Data.Builder()
//                .putString("strBody",strBody[0])
//                .build();
//        Result.success(data1); 이렇게 사용

//        Retrofit2 사용 끝
        return Result.success();
    }
}
