package com.ccojocea.aanews.data.local;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.models.entity.ArticleEntity;

public class Converters {

    // ArticleEntity
    @TypeConverter
    public static ArticleEntity.Source stringToSource(@NonNull String stringJson) {
        return Utils.getAppGson().fromJson(stringJson, ArticleEntity.Source.class);
    }

    @TypeConverter
    public static String sourceToString(@NonNull ArticleEntity.Source source) {
        return Utils.getAppGson().toJson(source);
    }

}
