package com.github.jameshnsears.quoteunquote.database.history

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AbstractHistoryDatabaseMigrationTest {
    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AbstractHistoryDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(DATABASE, 1)

        val values = ContentValues()
        values.put("widget_id", 1)
        values.put("content_type", ContentSelection.ALL.contentSelection)
        values.put("digest", "d")
        db.insert("previous", SQLiteDatabase.CONFLICT_REPLACE, values)

        db.close()

        helper.runMigrationsAndValidate(DATABASE, 2, true,
                AbstractHistoryDatabase.MIGRATION_1_2)

        val databaseRepository = DatabaseRepository.getInstance(getApplicationContext())
        val previousEntity = databaseRepository.previousDAO?.getPrevious(1, ContentSelection.ALL)
        assertEquals(ContentSelection.ALL.contentSelection, previousEntity?.contentSelection)
    }

    companion object {
        const val DATABASE = "history"
    }
}
