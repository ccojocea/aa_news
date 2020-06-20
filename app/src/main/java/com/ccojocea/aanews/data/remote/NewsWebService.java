package com.ccojocea.aanews.data.remote;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.models.dto.ArticleDto;
import com.ccojocea.aanews.models.responses.ArticlesResponse;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

public class NewsWebService {

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

    @Inject
    public NewsWebService(Retrofit retrofit) {
        Timber.d("Dagger debug - NewsWebService()");
        this.newsApi = retrofit.create(NewsApi.class);
    }

    // TODO Add params
    public Single<List<ArticleDto>> fetchTopHeadlines() {
        return newsApi.fetchTopHeadlines(null, "sports", App.getApp().getLocale().getCountry())
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
