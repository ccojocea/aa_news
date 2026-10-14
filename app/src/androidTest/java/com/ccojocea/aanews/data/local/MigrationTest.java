package com.ccojocea.aanews.data.local;

import android.database.Cursor;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Checks the migrations against the schemas exported to app/schemas.
 */
@RunWith(AndroidJUnit4.class)
public class MigrationTest {

    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase.class);

    @Test
    public void migrate1To2_keepsBookmarks() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        db.execSQL("INSERT INTO SavedArticleEntity (url, source, author, title, urlToImage, publishedAt, description, content, isSaved) "
                + "VALUES ('https://example.com/a_b', '{\"name\":\"Example\"}', 'Author', 'Title', NULL, '2020-06-12T14:22:06Z', NULL, 'Content', 1)");
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2);

        try (Cursor cursor = db.query("SELECT url, title FROM SavedArticleEntity")) {
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals("https://example.com/a_b", cursor.getString(0));
            assertEquals("Title", cursor.getString(1));
        }
    }

}
