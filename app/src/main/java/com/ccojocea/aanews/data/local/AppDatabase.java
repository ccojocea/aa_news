package com.ccojocea.aanews.data.local;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ccojocea.aanews.common.App;
import com.ccojocea.aanews.data.local.dao.ArticleDao;
import com.ccojocea.aanews.data.local.dao.SavedArticleDao;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.models.entity.SavedArticleEntity;

import timber.log.Timber;

@Database(entities = {ArticleEntity.class, SavedArticleEntity.class}, version = 2)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "aa_news_database";

    private static AppDatabase APP_DATABASE;

    /**
     * Initialize app database only once per application
     **/
    public static void initialize(@NonNull App application) {
        APP_DATABASE = androidx.room.Room.databaseBuilder(application.getApplicationContext(), AppDatabase.class, AppDatabase.DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            /*Article Entity*/
            //create temp table
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ArticleEntityTemp " +
                    "(url TEXT NOT NULL, source TEXT NOT NULL, author TEXT NOT NULL, title TEXT NOT NULL, " +
                    "urlToImage TEXT, publishedAt TEXT NOT NULL, description TEXT, content TEXT NOT NULL, " +
                    "isSaved INTEGER NOT NULL, PRIMARY KEY(url))");
            //copy data from ArticleEntity
            database.execSQL(
                    "INSERT INTO ArticleEntityTemp (url, source, author, title, urlToImage, publishedAt, description, content, isSaved) " +
                    "SELECT url, source, author, title, urlToImage, publishedAt, description, content, isSaved FROM ArticleEntity");
            //delete old table
            database.execSQL("DROP TABLE ArticleEntity");
            //rename temp table
            database.execSQL("ALTER TABLE ArticleEntityTemp RENAME TO ArticleEntity");

            /*SavedArticleEntity*/
            //create temp table
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS SavedArticleEntityTemp " +
                    "(url TEXT NOT NULL, source TEXT NOT NULL, author TEXT NOT NULL, title TEXT NOT NULL, " +
                    "urlToImage TEXT, publishedAt TEXT NOT NULL, description TEXT, content TEXT NOT NULL, " +
                    "isSaved INTEGER NOT NULL, PRIMARY KEY(url))");
            //copy data from ArticleEntity
            database.execSQL(
                    "INSERT INTO SavedArticleEntityTemp (url, source, author, title, urlToImage, publishedAt, description, content, isSaved) " +
                    "SELECT url, source, author, title, urlToImage, publishedAt, description, content, isSaved FROM SavedArticleEntity");
            //delete old table
            database.execSQL("DROP TABLE SavedArticleEntity");
            //rename temp table
            database.execSQL("ALTER TABLE SavedArticleEntityTemp RENAME TO SavedArticleEntity");

        }
    };

}
