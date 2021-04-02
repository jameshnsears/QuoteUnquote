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
        val db = helper.createDatabase(
            AbstractHistoryDatabase.DATABASE_NAME,
            1
        )

        val values = ContentValues()
        values.put("widget_id", 1)
        values.put("content_type", ContentSelection.ALL.contentSelection)
        values.put("digest", "d")
        db.insert("previous", SQLiteDatabase.CONFLICT_REPLACE, values)

        db.close()

        helper.runMigrationsAndValidate(
            AbstractHistoryDatabase.DATABASE_NAME,
            2,
            true,
            AbstractHistoryDatabase.MIGRATION_1_2
        )

        // the migration wipes `previous`
        val databaseRepository = DatabaseRepository.getInstance(getApplicationContext())
        assertEquals(0, databaseRepository.countPrevious(1, ContentSelection.ALL))
    }
}
