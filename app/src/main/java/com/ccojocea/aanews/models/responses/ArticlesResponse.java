package com.ccojocea.aanews.models.responses;

import com.ccojocea.aanews.models.dto.ArticleDto;

import java.util.List;

public class ArticlesResponse extends BaseResponse {

    public final int totalResults;
    public final List<ArticleDto> articles;

    ArticlesResponse(String status, String code, String message, int totalResults, List<ArticleDto> articles) {
        super(status, code, message);
        this.totalResults = totalResults;
        this.articles = articles;
    }

}
