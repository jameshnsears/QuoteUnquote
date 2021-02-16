package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDisabledTest : QuoteUnquoteModelUtility() {
    @Test
    fun widgetDisabled() {
        val quoteUnquoteWidget = spyk<QuoteUnquoteWidget>()
        every { quoteUnquoteWidget.getQuoteUnquoteModelInstance(any()) } returns quoteUnquoteModelDouble

        insertDataset01()
        setDefaultQuotation()
        markDefaultQuotationAsFavourite()
//        makeDefaultQuotationAsAuthor()
//        makeDefaultQuotationAsSearch()
        markDefaultQuotationAsReported()

        /*
        TODO

        tidy up WidgetDeletedTest - check for         assertTrue(contentPreferences.contentFavouritesLocalCode.length == 10)


        in insertDataset01 create
        makeDefaultQuotationAsAuthor()
        makeDefaultQuotationAsSearch()


        assertTrue(quoteUnquoteModelDouble.countPreviousAuthor() == 0)
        assertTrue(quoteUnquoteModelDouble.countPreviousSearch() == 0)

        tidyup WidgetToolbarFavouriteTest - i.e. the big unit tests, they are way way way too big

         */

        quoteUnquoteWidget.onEnabled(context)

        val contentPreferences = ContentPreferences(context)
        assertTrue(contentPreferences.contentFavouritesLocalCode.length == 10)

        quoteUnquoteWidget.onDisabled(context)

        assertTrue(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID) == 0)

        assertTrue(contentPreferences.contentFavouritesLocalCode.isEmpty())

        // TODO but all other pref's should be gone
    }
}
