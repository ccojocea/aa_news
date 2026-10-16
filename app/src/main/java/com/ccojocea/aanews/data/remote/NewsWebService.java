package com.ccojocea.aanews.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccojocea.aanews.models.responses.ArticlesResponse;

import javax.inject.Inject;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

public class NewsWebService {

    public static final int PAGE_SIZE = 20;

    // the free (developer) plan of newsapi.org returns at most 100 results for a query
    public static final int MAX_RESULTS = 100;

    private static final String API_ROUTE_EVERYTHING = "everything";
    private static final String API_ROUTE_HEADLINES = "top-headlines";

    //Request parameters
    private static final String PARAM_Q = "q";
    private static final String PARAM_PAGE_SIZE = "pageSize";
    private static final String PARAM_PAGE = "page";

    // specific based on route
    private static final String PARAM_HEADLINES_CATEGORY = "category";
    private static final String PARAM_HEADLINES_COUNTRY = "country";

    private static final String PARAM_EVERYTHING_LANGUAGE = "language";
    private static final String PARAM_EVERYTHING_SORT_BY = "sortBy"; //relevancy, popularity, publishedAt(default)
    private static final String PARAM_EVERYTHING_SEARCH_IN = "searchIn"; //title, description, content (default: all)

    // only match articles that have the query in their title, the other fields often just mention it in passing
    public static final String SEARCH_IN_TITLE = "title";

    private final NewsApi newsApi;

    @Inject
    public NewsWebService(Retrofit retrofit) {
        Timber.d("Dagger debug - NewsWebService()");
        this.newsApi = retrofit.create(NewsApi.class);
    }

    /**
     * @param country  2-letter ISO 3166-1 code, see NewsCountry
     * @param category null for all categories, see NewsCategory
     * @param page     1-based page index
     */
    public Single<ArticlesResponse> fetchTopHeadlines(@NonNull String country, @Nullable String category, int page, int pageSize) {
        return newsApi.fetchTopHeadlines(country, category, pageSize, page);
    }

    /**
     * @param query    keywords or phrases, supports AND / OR / NOT and quotes for exact matches
     * @param language null for all languages, see NewsLanguage
     * @param sortBy   null for the default (publishedAt), see NewsSortBy
     * @param searchIn null to search all fields, e.g. {@link #SEARCH_IN_TITLE}
     * @param page     1-based page index
     */
    public Single<ArticlesResponse> fetchEverything(@NonNull String query, @Nullable String language, @Nullable String sortBy,
                                                    @Nullable String searchIn, int page, int pageSize) {
        return newsApi.fetchEverything(query, language, sortBy, searchIn, pageSize, page);
    }

    private interface NewsApi {

        // null parameters are not added to the request
        @GET(API_ROUTE_EVERYTHING)
        Single<ArticlesResponse> fetchEverything(
                @Query(PARAM_Q) String query,
                @Query(PARAM_EVERYTHING_LANGUAGE) String language,
                @Query(PARAM_EVERYTHING_SORT_BY) String sortBy,
                @Query(PARAM_EVERYTHING_SEARCH_IN) String searchIn,
                @Query(PARAM_PAGE_SIZE) Integer pageSize,
                @Query(PARAM_PAGE) Integer page
        );

        @GET(API_ROUTE_HEADLINES)
        Single<ArticlesResponse> fetchTopHeadlines(
                @Query(PARAM_HEADLINES_COUNTRY) String country,
                @Query(PARAM_HEADLINES_CATEGORY) String category,
                @Query(PARAM_PAGE_SIZE) Integer pageSize,
                @Query(PARAM_PAGE) Integer page
        );

    }

}
