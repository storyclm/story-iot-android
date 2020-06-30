package ru.breffi.lib.network;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public final class Communicator {

    private static StoryIoTService storyIoTService;

    private Communicator() {

    }

    public static StoryIoTService getStoryIoTService(Gson converter, String baseUrl) {
        if (storyIoTService == null) {
            OkHttpClient client = initOkHttpClient();
            Retrofit retrofit = getRetrofit(client, converter, baseUrl);
            storyIoTService = retrofit.create(StoryIoTService.class);
        }
        return storyIoTService;
    }

    private static OkHttpClient initOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = getHttpBuilder(interceptor);
        return builder.build();
    }

    private static Retrofit getRetrofit(OkHttpClient client, Gson converter, String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(converter))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private static OkHttpClient.Builder getHttpBuilder(HttpLoggingInterceptor interceptor) {
        return new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor);
    }
}