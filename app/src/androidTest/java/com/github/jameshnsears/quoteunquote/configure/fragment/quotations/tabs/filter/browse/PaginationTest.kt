package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.fragment.dialog.browse.BrowseData
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PaginationTest : QuoteUnquoteModelUtility() {
    @Test
    fun tryToPageAfterEnd() {
        val pageSize = 10

        val browseFavouritesDialogFragment = BrowseFavouritesDialogFragment(
            WidgetIdHelper.WIDGET_ID_01,
            quoteUnquoteModelDouble,
            "",
            pageSize,
        )

        browseFavouritesDialogFragment.cachedRecyclerViewData =
            MutableList(pageSize) {
                BrowseData(
                    it.toString(),
                    "q" + it,
                    "s" + it,
                    false,
                    "d" + it,
                )
            }

        assertEquals(
            pageSize,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(0).size,
        )

        assertEquals(
            0,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(1).size,
        )
    }

    @Test
    fun dataSizeLessThenPageSize() {
        val pageSize = 10

        val browseFavouritesDialogFragment = BrowseFavouritesDialogFragment(
            WidgetIdHelper.WIDGET_ID_01,
            quoteUnquoteModelDouble,
            "",
            pageSize,
        )

        val dataSize = 5
        browseFavouritesDialogFragment.cachedRecyclerViewData =
            MutableList(dataSize) {
                BrowseData(
                    it.toString(),
                    "q" + it,
                    "s" + it,
                    false,
                    "d" + it,
                )
            }

        assertEquals(
            dataSize,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(0).size,
        )
    }

    @Test
    fun dataSizeEqualToPageSize() {
        val pageSize = 10

        val browseFavouritesDialogFragment = BrowseFavouritesDialogFragment(
            WidgetIdHelper.WIDGET_ID_01,
            quoteUnquoteModelDouble,
            "",
            pageSize,
        )

        browseFavouritesDialogFragment.cachedRecyclerViewData =
            MutableList(pageSize) {
                BrowseData(
                    it.toString(),
                    "q" + it,
                    "s" + it,
                    false,
                    "d" + it,
                )
            }

        assertEquals(
            pageSize,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(0).size,
        )
    }

    @Test
    fun dataSizeGreaterThenPageSize() {
        val pageSize = 5

        val browseFavouritesDialogFragment = BrowseFavouritesDialogFragment(
            WidgetIdHelper.WIDGET_ID_01,
            quoteUnquoteModelDouble,
            "",
            pageSize,
        )

        val dataSize = 12
        browseFavouritesDialogFragment.cachedRecyclerViewData =
            MutableList(dataSize) {
                BrowseData(
                    it.toString(),
                    "q" + it,
                    "s" + it,
                    false,
                    "d" + it,
                )
            }

        assertEquals(
            pageSize,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(0).size,
        )

        assertEquals(
            pageSize,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(1).size,
        )

        assertEquals(
            2,
            browseFavouritesDialogFragment.getCachedRecyclerViewData(2).size,
        )
    }
}
