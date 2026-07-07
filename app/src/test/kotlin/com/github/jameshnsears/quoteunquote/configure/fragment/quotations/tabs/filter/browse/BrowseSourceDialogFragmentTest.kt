package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse

import android.app.Application
import android.os.Build
import android.view.View
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.adapter.BrowseAdapter
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class BrowseSourceDialogFragmentTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialState() {
        val quoteUnquoteModel = mockk<QuoteUnquoteModel>()
        every { quoteUnquoteModel.getQuotationsForAuthor(any()) } returns emptyList()
        every { quoteUnquoteModel.isFavourite(any()) } returns false

        launchFragment(themeResId = R.style.AppTheme) {
            BrowseSourceDialogFragmentDouble(quoteUnquoteModel)
        }.use { scenario ->
            scenario.onFragment { fragment ->
                assertThat(
                    fragment.fragmentQuotationsTabFilterBrowseDialogBinding?.recycleViewBrowse,
                    notNullValue(),
                )
            }
        }
    }

    @Test
    fun setCachedRecyclerViewData_returnsFormattedData() {
        mockkConstructor(QuotationsPreferences::class)
        mockkConstructor(QuoteUnquoteModel::class)
        try {
            every { anyConstructed<QuotationsPreferences>().contentSelectionAuthor } returns "Test Author"

            val quotations =
                listOf(
                    QuotationEntity("digest1", "wiki", "Author A", "Quotation 1"),
                    QuotationEntity("digest2", "wiki", "Author B", "Quotation 2"),
                )

            val quoteUnquoteModel = mockk<QuoteUnquoteModel>()
            every { quoteUnquoteModel.getQuotationsForAuthor("Test Author") } returns quotations
            every { quoteUnquoteModel.isFavourite("digest1") } returns true
            every { quoteUnquoteModel.isFavourite("digest2") } returns false
            every { anyConstructed<QuoteUnquoteModel>().isFavourite("digest1") } returns true
            every { anyConstructed<QuoteUnquoteModel>().isFavourite("digest2") } returns false

            launchFragment(themeResId = R.style.AppTheme) {
                BrowseSourceDialogFragmentDouble(quoteUnquoteModel)
            }.use { scenario ->
                scenario.onFragment { fragment ->
                    val recyclerView =
                        fragment.fragmentQuotationsTabFilterBrowseDialogBinding!!.recycleViewBrowse
                    val adapter = recyclerView.adapter as BrowseAdapter

                    assertThat(adapter.itemCount, equalTo(2))

                    // First item: is a favourite → favourite VISIBLE
                    val viewHolder0 = adapter.onCreateViewHolder(recyclerView, 0)
                    adapter.onBindViewHolder(viewHolder0, 0)
                    assertThat(viewHolder0.textViewSequentialIndex.text.toString(), equalTo("1"))
                    assertThat(viewHolder0.textViewQuotation.text.toString(), equalTo("Quotation 1"))
                    assertThat(viewHolder0.textViewSource.visibility, equalTo(View.GONE))
                    assertThat(viewHolder0.textViewFavourite.visibility, equalTo(View.VISIBLE))

                    // Second item: not a favourite → favourite GONE
                    val viewHolder1 = adapter.onCreateViewHolder(recyclerView, 1)
                    adapter.onBindViewHolder(viewHolder1, 1)
                    assertThat(viewHolder1.textViewSequentialIndex.text.toString(), equalTo("2"))
                    assertThat(viewHolder1.textViewQuotation.text.toString(), equalTo("Quotation 2"))
                    assertThat(viewHolder1.textViewFavourite.visibility, equalTo(View.GONE))
                }
            }
        } finally {
            unmockkConstructor(QuoteUnquoteModel::class)
            unmockkConstructor(QuotationsPreferences::class)
        }
    }
}
