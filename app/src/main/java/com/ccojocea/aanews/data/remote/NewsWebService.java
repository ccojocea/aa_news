package com.ccojocea.aanews.data.remote;

import com.ccojocea.aanews.BuildConfig;
import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.models.dto.ArticleDto;
import com.ccojocea.aanews.data.models.responses.ArticlesResponse;
import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

public class NewsWebService {

    private static final int TIMEOUT_MILLISECONDS = 30 * 1000;

    private static final String HEADER_X_API_KEY = "X-Api-Key";
    private static final String NEWS_API_KEY = "3ea4cb83232b4f30aa1c4c5b7c4535c9";

    private static final String API_BASE_URL = "https://newsapi.org/v2/";
    private static final String API_ROUTE_EVERYTHING = "everything";
    private static final String API_ROUTE_HEADLINES = "top-headlines";

    //Request parameters
    private static final String PARAM_Q = "q";
    private static final String PARAM_SOURCES = "sources";
    private static final String PARAM_PAGE_SIZE = "pageSize";
    private static final String PARAM_PAGE = "page";

    // specific based on route
    private static final String PARAM_HEADLINES_CATEGORY = "category";
    private static final String PARAM_HEADLINES_COUNTRY = "country";

    private static final String PARAM_EVERYTHING_LANGUAGE = "language";
    private static final String PARAM_EVERYTHING_SORT_BY = "sortBy"; //relevancy, popularity, published At(default)
    private static final String PARAM_EVERYTHING_FROM = "from";
    private static final String PARAM_EVERYTHING_TO = "to";

    private final NewsApi newsApi;

    public NewsWebService() {
        OkHttpClient client = initialiseClient();

        // Create the Retrofit API
        newsApi = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                // Configure how to serialize / deserialize GSON
                .addConverterFactory(GsonConverterFactory.create())
                // Configure support for returning RxJava observables
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                // Setup client
                .client(client)
                .build()
                // Once everything is setup, build the API based on the defined interface
                .create(NewsApi.class);
    }

    // TODO Add params
    public Single<List<ArticleDto>> fetchTopHeadlines() {
        return newsApi.fetchTopHeadlines(null, null, App.getApp().getLocale().getCountry())
                .map(articlesResponse -> articlesResponse.articles);
    }

    // TODO Add params
    public Single<List<ArticleDto>> fetchEverything(String query) {
        return newsApi.fetchEverything(query, App.getApp().getLocale().getLanguage())
                .map(articlesResponse -> articlesResponse.articles);
    }

    public Single<List<ArticleDto>> fetchPagedTopHeadlines(int page) {
        return newsApi.fetchPagedTopHeadlines(null, null, App.getApp().getLocale().getCountry(), page)
                .map(articlesResponse -> articlesResponse.articles);
    }

    //TODO PAGINATION
    public Observable<List<ArticleDto>> executeNewsApi(int index) {
        return newsApi.fetchListNews(App.getApp().getLocale().getCountry(), null)
                .map(articlesResponse -> articlesResponse.articles);
    }

    private static OkHttpClient initialiseClient() {
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

        builder.connectTimeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.readTimeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.writeTimeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));

        return builder.build();
    }

    private interface NewsApi {

        @GET(API_ROUTE_EVERYTHING)
        Single<ArticlesResponse> fetchEverything(
                @Query(PARAM_Q) String query,
                @Query(PARAM_EVERYTHING_LANGUAGE) String language
        );

        @GET(API_ROUTE_HEADLINES)
        Single<ArticlesResponse> fetchTopHeadlines(
                @Query(PARAM_Q) String query,
                @Query(PARAM_HEADLINES_CATEGORY) String category,
                @Query(PARAM_HEADLINES_COUNTRY) String country
        );

        @GET(API_ROUTE_HEADLINES)
        Single<ArticlesResponse> fetchPagedTopHeadlines(
                @Query(PARAM_Q) String query,
                @Query(PARAM_HEADLINES_CATEGORY) String category,
                @Query(PARAM_HEADLINES_COUNTRY) String country,
                @Query(PARAM_PAGE) Integer page
        );

        //TODO PAGINATION
        @GET(API_ROUTE_HEADLINES)
        Observable<ArticlesResponse> fetchListNews(
                @Query(PARAM_HEADLINES_COUNTRY) String country,
                @Query(PARAM_PAGE) Integer page
        );

    }

}
