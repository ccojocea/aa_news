package com.ccojocea.aanews.data.remote;

import com.ccojocea.aanews.BuildConfig;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
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

    public static final String NEWS_API = "newsApi";

    private static final int TIMEOUT = 30; //seconds

    private static final String HEADER_X_API_KEY = "X-Api-Key";
    private static final String NEWS_API_BASE_URL = "https://newsapi.org/v2/";

    @Provides
    @Singleton
    Retrofit getRetrofit(@Named(NEWS_API) OkHttpClient client) {
        Timber.d("Dagger debug - retrofit builder");
        return new Retrofit.Builder()
                .baseUrl(NEWS_API_BASE_URL)
                // Configure how to serialize / deserialize GSON
                .addConverterFactory(GsonConverterFactory.create())
                // Configure support for returning RxJava observables
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                // Setup client
                .client(client)
                .build();
    }

    /**
     * Client for the NewsAPI requests only, it adds the API key to every request.
     */
    @Provides
    @Singleton
    @Named(NEWS_API)
    OkHttpClient newsApiClient() {
        Timber.d("Dagger debug - newsApiClient()");
        OkHttpClient.Builder builder = baseBuilder();

        // Add an interceptor for reused headers
        // without a key the API answers with "apiKeyMissing", which is shown to the user
        builder.addInterceptor(chain -> {
            if (BuildConfig.NEWS_API_KEY.isEmpty()) {
                return chain.proceed(chain.request());
            }
            Request request = chain.request().newBuilder().addHeader(HEADER_X_API_KEY, BuildConfig.NEWS_API_KEY).build();
            return chain.proceed(request);
        });

        if (BuildConfig.DEBUG) {
            // Prepare a logging interceptor to make sure the requests / responses are shown in logs
            builder.addInterceptor(loggingInterceptor(HttpLoggingInterceptor.Level.BODY));

            //Setup proxy for Charles
            //builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.100", 8888)));
        }
        return builder.build();
    }

    /**
     * Client for everything else, e.g. Glide loading the article images from many different hosts.
     * It must not send the API key, and logging image bodies would only waste memory.
     */
    @Provides
    @Singleton
    OkHttpClient okHttpClient() {
        Timber.d("Dagger debug - okHttpClient()");
        OkHttpClient.Builder builder = baseBuilder();
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor(HttpLoggingInterceptor.Level.BASIC));
        }
        return builder.build();
    }

    private static OkHttpClient.Builder baseBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT, TimeUnit.SECONDS);
        builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT));
        return builder;
    }

    private static HttpLoggingInterceptor loggingInterceptor(HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(s -> Timber.d(s));
        loggingInterceptor.setLevel(level);
        return loggingInterceptor;
    }

}
