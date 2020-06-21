package com.ccojocea.aanews.data;

import com.ccojocea.aanews.models.entity.ArticleEntity;

public class NewsHelper {

    private static NewsHelper instance;
    private ArticleEntity articleEntity;

    private NewsHelper(){}

    public static NewsHelper getInstance() {
        if (instance == null) {
            instance = new NewsHelper();
        }
        return instance;
    }

    public ArticleEntity getArticleEntity() {
        return articleEntity;
    }

    public void setArticleEntity(ArticleEntity articleEntity) {
        this.articleEntity = articleEntity;
    }

}
