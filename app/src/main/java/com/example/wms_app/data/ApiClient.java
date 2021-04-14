package com.example.wms_app.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.wms_app.utilities.DateDeserializer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static String SERVICE_ADDRESS = "http://89.216.113.44:8228/";

    private final static String SERVICE_URI = SERVICE_ADDRESS + "KolibriService.svc/";

    public static Retrofit retrofit;

    public static Retrofit getApiClient() {

        try {
            //Okhttp client
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder
                    .connectTimeout(30, TimeUnit.SECONDS) //time for connecting to server and start to receive data
                    .readTimeout(30, TimeUnit.SECONDS)     //time that passes between two bytes that are received from the server
                    .writeTimeout(15, TimeUnit.SECONDS); //time that passes between two bytes that are sent to the server


//        builder.addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Interceptor.Chain chain) throws IOException {
//                Request original = chain.request();
//
//                Request request = original.newBuilder()
//                        .header("Accept", "application/json")
//                        .header("Content-type", "application/json")
//                        .build();
//
//                return chain.proceed(request);
//            }
//        });


//            if(BuildConfig.DEBUG) {
//                //Interceptor - used to see the data being sent and received
//                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//                builder.addInterceptor(interceptor);
//            }

            if (retrofit == null) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
                Gson gson = gsonBuilder.create();

                retrofit = new Retrofit.Builder().baseUrl(SERVICE_URI)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(builder.build()).build();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrofit;

    }
}
