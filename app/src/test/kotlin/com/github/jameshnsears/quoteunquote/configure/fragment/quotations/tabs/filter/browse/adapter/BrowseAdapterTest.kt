package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.adapter

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class BrowseAdapterTest : ShadowLoggingHelper() {
    private lateinit var context: Context
    private lateinit var browseDataList: MutableList<BrowseData>
    private lateinit var adapter: BrowseAdapter
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        context =
            ApplicationProvider.getApplicationContext<Application>().apply {
                setTheme(R.style.AppTheme)
            }
        browseDataList =
            mutableListOf(
                BrowseData("1", "Quotation 1", "Source 1", false, "digest1"),
                BrowseData("2", "Quotation 2", "Source 2", false, "digest2"),
            )
        adapter = BrowseAdapter(WidgetIdHelper.WIDGET_ID_01, browseDataList, BrowseAdapter.DIALOG.FAVOURITES)

        recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        mockkConstructor(QuoteUnquoteModel::class)
        mockkConstructor(AppearancePreferences::class)

        every { anyConstructed<QuoteUnquoteModel>().isFavourite(any()) } returns false
    }

    @After
    fun tearDown() {
        unmockkConstructor(QuoteUnquoteModel::class)
        unmockkConstructor(AppearancePreferences::class)
    }

    @Test
    fun getItemCount() {
        assertThat(adapter.itemCount, equalTo(2))
    }

    @Test
    fun onBindViewHolder_favouriteVisible() {
        every { anyConstructed<QuoteUnquoteModel>().isFavourite("digest1") } returns true

        val viewHolder = adapter.onCreateViewHolder(recyclerView, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.textViewSequentialIndex.text, equalTo("1"))
        assertThat(viewHolder.textViewQuotation.text, equalTo("Quotation 1"))
        assertThat(viewHolder.textViewSource.text, equalTo("Source 1"))
        assertThat(viewHolder.textViewFavourite.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun onBindViewHolder_favouriteGone() {
        every { anyConstructed<QuoteUnquoteModel>().isFavourite("digest1") } returns false

        val viewHolder = adapter.onCreateViewHolder(recyclerView, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.textViewFavourite.visibility, equalTo(View.GONE))
    }

    @Test
    fun onBindViewHolder_sourceDialog() {
        adapter = BrowseAdapter(WidgetIdHelper.WIDGET_ID_01, browseDataList, BrowseAdapter.DIALOG.SOURCE)

        val viewHolder = adapter.onCreateViewHolder(recyclerView, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertThat(viewHolder.textViewSource.visibility, equalTo(View.GONE))
    }

    @Test
    fun addData() {
        val newData = listOf(BrowseData("3", "Quotation 3", "Source 3", false, "digest3"))
        adapter.addData(newData, 2)

        assertThat(adapter.itemCount, equalTo(3))
    }

    @Test
    fun onClick_startsShareIntent() {
        every { anyConstructed<AppearancePreferences>().appearanceToolbarShareNoSource } returns false

        recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        recyclerView.layout(0, 0, 1000, 1000)

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0) as BrowseAdapter.ViewHolder
        viewHolder.itemView.performClick()

        val expectedIntent = Shadows.shadowOf(context as Application).nextStartedActivity
        assertThat(expectedIntent.action, equalTo(Intent.ACTION_CHOOSER))
    }

    @Test
    fun onClick_startsShareIntent_noSource() {
        every { anyConstructed<AppearancePreferences>().appearanceToolbarShareNoSource } returns true

        recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        recyclerView.layout(0, 0, 1000, 1000)

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0) as BrowseAdapter.ViewHolder
        viewHolder.itemView.performClick()

        val expectedIntent = Shadows.shadowOf(context as Application).nextStartedActivity
        assertThat(expectedIntent.action, equalTo(Intent.ACTION_CHOOSER))
    }

    @Test
    fun onLongClick_togglesFavourite() {
        every { anyConstructed<QuoteUnquoteModel>().isFavourite("digest1") } returns false
        every { anyConstructed<QuoteUnquoteModel>().toggleFavourite(any(), any()) } returns 1

        recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        recyclerView.layout(0, 0, 1000, 1000)

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0) as BrowseAdapter.ViewHolder

        // Initial state
        assertThat(viewHolder.textViewFavourite.visibility, equalTo(View.GONE))

        // Long click
        viewHolder.itemView.performLongClick()

        verify { anyConstructed<QuoteUnquoteModel>().toggleFavourite(WidgetIdHelper.WIDGET_ID_01, "digest1") }
        assertThat(viewHolder.textViewFavourite.visibility, equalTo(View.VISIBLE))
    }
}
