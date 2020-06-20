package com.ccojocea.aanews.data.local;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.local.dao.ArticleDao;
import com.ccojocea.aanews.data.local.dao.SavedArticleDao;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import timber.log.Timber;

@Database(entities = {ArticleEntity.class, SavedArticleEntity.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "aa_news_database";

    private static AppDatabase APP_DATABASE;

    /**
     * Initialize app database only once per application
     **/
    public static void initialize(@NonNull App application) {
        APP_DATABASE = androidx.room.Room.databaseBuilder(application.getApplicationContext(), AppDatabase.class, AppDatabase.DATABASE_NAME)
                .addCallback(new Callback() {
                    //called when database is created the first time
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Timber.d("AANews AppDatabase onCreate");
                    }

                    //called each time the database is opened
                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        Timber.d("AANews AppDatabase onOpen");
                    }
                })
                .fallbackToDestructiveMigration() // This will destroy the tables on a new migration that is undefined
                .build();
    }

    /**
     * Might be null if it is not initialized
     **/
    public static AppDatabase getInstance() {
        return APP_DATABASE;
    }

    public abstract ArticleDao articleDao();

    public abstract SavedArticleDao savedArticleDao();

}
