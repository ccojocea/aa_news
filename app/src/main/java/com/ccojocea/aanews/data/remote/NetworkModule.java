package com.ccojocea.aanews.data.remote;

import android.webkit.WebSettings;

import com.ccojocea.aanews.BuildConfig;
import com.ccojocea.aanews.common.App;

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
    public static final String IMAGES = "images";

    // image CDNs of many news sites refuse requests that don't come from a browser (e.g. "okhttp/4.12.0")
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String ACCEPT_IMAGES = "image/webp,image/apng,image/*,*/*;q=0.8";

    private static volatile String userAgent;

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
     * Client for Glide, loading the article images from many different hosts.
     * It must not send the API key, and logging image bodies would only waste memory.
     */
    @Provides
    @Singleton
    @Named(IMAGES)
    OkHttpClient imagesClient() {
        Timber.d("Dagger debug - imagesClient()");
        OkHttpClient.Builder builder = baseBuilder();
        // identify as the WebView that shows the same articles, the default okhttp user agent is often blocked
        builder.addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                .header(HEADER_USER_AGENT, getUserAgent())
                .header(HEADER_ACCEPT, ACCEPT_IMAGES)
                .build()));
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor(HttpLoggingInterceptor.Level.BASIC));
        }
        return builder.build();
    }

    private static String getUserAgent() {
        if (userAgent == null) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(App.getApp());
            } catch (RuntimeException e) {
                // no WebView available (e.g. it is being updated)
                Timber.w(e, "Could not read the WebView user agent");
                String systemAgent = System.getProperty("http.agent");
                userAgent = systemAgent != null ? systemAgent : "Mozilla/5.0 (Linux; Android)";
            }
        }
        return userAgent;
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
