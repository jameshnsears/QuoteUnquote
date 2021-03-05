package com.github.jameshnsears.quoteunquote.database

import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseRepositoryTest : DatabaseTestHelper() {
    @Test
    fun getPreviousNextCountsAll() {
        insertQuotationTestData01()
        assertEquals(" @ 0/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))

        setDefaultQuotationAll(WidgetIdHelper.WIDGET_ID_01)

        assertEquals(" @ 1/2", databaseRepositoryDouble.getPreviousNextCounts(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL))
    }

//    @Test
//    fun getPreviousNextCountsAuthor() {
//
//    }

    @Test
    fun getPreviousNextCountsFavourites() {

    }

//    @Test
//    fun getPreviousNextCountsSearch() {
//
//    }
}
