package com.phungnlg.hellodoctor.adapter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Phil on 7/12/2017.
 */

public class Connect {
    private static final String API_BASE_URL = "https://newhellodoctor.firebaseio.com/";

    public static IApiService getRetrofit() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL)
                                                         .addConverterFactory(GsonConverterFactory.create())
                                                         .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        Retrofit retrofit = builder.client(httpClient.build()).build();
        IApiService client = retrofit.create(IApiService.class);
        return client;
    }
}
