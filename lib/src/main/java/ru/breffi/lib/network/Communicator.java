package ru.breffi.lib.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;


public final class Communicator {

    private static StoryIoTService authRetrofitService;

    private Communicator() {

    }

    public static StoryIoTService getStoryIoTService() {
        if (authRetrofitService == null) {
            OkHttpClient client = initOkHttpClient();
            Gson converter = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            String baseUrl = "https://staging-iot.storychannels.app/";
            Retrofit retrofit = getRetrofit(client, converter, baseUrl);
            authRetrofitService = retrofit.create(StoryIoTService.class);
        }
        return authRetrofitService;
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
                .readTimeout(300, TimeUnit.SECONDS)
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor);
    }
}