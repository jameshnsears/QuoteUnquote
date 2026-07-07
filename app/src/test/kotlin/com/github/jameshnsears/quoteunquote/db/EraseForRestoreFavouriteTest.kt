package com.github.jameshnsears.quoteunquote.db

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.db.h.FavouriteDAO
import com.github.jameshnsears.quoteunquote.db.h.FavouriteEntity
import com.github.jameshnsears.quoteunquote.db.h.HistoryDatabase
import com.github.jameshnsears.quoteunquote.db.h.HistoryExternalDatabase
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Verifies that [DatabaseRepository.eraseForRestore] clears favourite tables
 * so that loading the same .json file twice does not produce duplicate rows.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.S])
class EraseForRestoreFavouriteTest : ShadowLoggingHelper() {
    private lateinit var historyDb: HistoryDatabase
    private lateinit var historyExternalDb: HistoryExternalDatabase
    private lateinit var favouriteDAO: FavouriteDAO
    private lateinit var favouriteExternalDAO: FavouriteDAO

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()

        historyDb =
            Room
                .inMemoryDatabaseBuilder(context, HistoryDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        favouriteDAO = historyDb.favouritesDAO()

        historyExternalDb =
            Room
                .inMemoryDatabaseBuilder(context, HistoryExternalDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        favouriteExternalDAO = historyExternalDb.favouritesExternalDAO()
    }

    @After
    fun tearDown() {
        historyDb.close()
        historyExternalDb.close()
    }

    @Test
    fun internalFavouriteDaoEraseRemovesAllFavourites() {
        favouriteDAO.markAsFavourite(FavouriteEntity("digest1"))
        favouriteDAO.markAsFavourite(FavouriteEntity("digest2"))
        assertThat(favouriteDAO.countFavourites().blockingGet(), equalTo(2))

        favouriteDAO.erase()
        assertThat(favouriteDAO.countFavourites().blockingGet(), equalTo(0))
    }

    @Test
    fun externalFavouriteDaoEraseRemovesAllFavourites() {
        favouriteExternalDAO.markAsFavourite(FavouriteEntity("digestA"))
        favouriteExternalDAO.markAsFavourite(FavouriteEntity("digestB"))
        favouriteExternalDAO.markAsFavourite(FavouriteEntity("digestC"))
        assertThat(favouriteExternalDAO.countFavourites().blockingGet(), equalTo(3))

        favouriteExternalDAO.erase()
        assertThat(favouriteExternalDAO.countFavourites().blockingGet(), equalTo(0))
    }
}
