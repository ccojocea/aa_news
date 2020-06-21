package com.ccojocea.aanews.data.remote;

import com.ccojocea.aanews.BuildConfig;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module
public class NetworkModule {

    private static OkHttpClient CLIENT;

    private static final int TIMEOUT = 30; //seconds

    private static final String HEADER_X_API_KEY = "X-Api-Key";
    private static final String NEWS_API_KEY = "3ea4cb83232b4f30aa1c4c5b7c4535c9";
    private static final String NEWS_API_BASE_URL = "https://newsapi.org/v2/";

    @Provides
    Retrofit getRetrofit() {
        Timber.d("Dagger debug - retrofit builder");
        return new Retrofit.Builder()
                .baseUrl(NEWS_API_BASE_URL)
                // Configure how to serialize / deserialize GSON
                .addConverterFactory(GsonConverterFactory.create())
                // Configure support for returning RxJava observables
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                // Setup client
                .client(okHttpClient())
                .build();
    }

    @Provides
    @Singleton //method gets called each time, hence the static reference
    OkHttpClient okHttpClient() {
        Timber.d("Dagger debug - okHttpClient()");
        if (CLIENT == null) {
            Timber.d("Dagger debug - okHttpClient() NEW");
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            // Add an interceptor for reused headers
            builder.addInterceptor(chain -> {
                Request request = chain.request().newBuilder().addHeader(HEADER_X_API_KEY, NEWS_API_KEY).build();
                return chain.proceed(request);
            });

            if (BuildConfig.DEBUG) {
                // Prepare a logging interceptor to make sure the requests / responses are shown in logs
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(s -> Timber.d(s));
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(loggingInterceptor);

                //Setup proxy for Charles
                //builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.100", 8888)));
            }

            builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
            builder.readTimeout(TIMEOUT, TimeUnit.SECONDS);
            builder.writeTimeout(TIMEOUT, TimeUnit.SECONDS);
            builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT));

            CLIENT = builder.build();
        }
        return CLIENT;
    }

}
