package com.github.jameshnsears.quoteunquote.listview

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ListViewProviderTest {
    private lateinit var context: Context
    private val widgetId = 1
    private val quotationDigest = "digest"
    private val quotationText = "The quotation text"
    private val authorText = "The author text"
    private val positionText = "1 / 10"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkConstructor(QuoteUnquoteModel::class)
        mockkConstructor(AppearancePreferences::class)
    }

    @After
    fun tearDown() {
        unmockkConstructor(QuoteUnquoteModel::class)
        unmockkConstructor(AppearancePreferences::class)
    }

    @Test
    fun getViewAt() {
        // Arrange
        val intent = Intent()
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)

        val quotationEntity = QuotationEntity(quotationDigest, "wiki", authorText, quotationText)

        every { anyConstructed<QuoteUnquoteModel>().getCurrentQuotation(widgetId) } returns quotationEntity
        every { anyConstructed<QuoteUnquoteModel>().getPosition(widgetId, quotationDigest) } returns positionText

        // Mock AppearancePreferences to return specific values
        every { anyConstructed<AppearancePreferences>().appearanceTextFamily } returns "Serif"
        every { anyConstructed<AppearancePreferences>().appearanceTextStyle } returns "Regular"
        every { anyConstructed<AppearancePreferences>().appearanceTextForceItalicRegular } returns false
        every { anyConstructed<AppearancePreferences>().appearanceTextCenter } returns false
        every { anyConstructed<AppearancePreferences>().appearanceTextRightSource } returns false
        every { anyConstructed<AppearancePreferences>().appearanceQuotationTextSize } returns 16
        every { anyConstructed<AppearancePreferences>().appearanceQuotationTextColour } returns "#000000"
        every { anyConstructed<AppearancePreferences>().appearanceAuthorTextSize } returns 14
        every { anyConstructed<AppearancePreferences>().appearanceAuthorTextColour } returns "#000000"
        every { anyConstructed<AppearancePreferences>().appearanceAuthorTextHide } returns false
        every { anyConstructed<AppearancePreferences>().appearancePositionTextSize } returns 12
        every { anyConstructed<AppearancePreferences>().appearancePositionTextColour } returns "#000000"
        every { anyConstructed<AppearancePreferences>().appearancePositionTextHide } returns false
        every { anyConstructed<AppearancePreferences>().appearanceForceFollowSystemTheme } returns false

        val listViewProvider = ListViewProvider(context, intent)
        listViewProvider.onDataSetChanged()

        // Act
        val remoteViews = listViewProvider.getViewAt(0)

        // Assert
        assertThat(remoteViews, notNullValue())

        val parent = LinearLayout(context)
        val view = remoteViews.apply(context, parent)

        val textViewQuotation = view.findViewById<TextView>(R.id.textViewRowQuotation)
        val textViewAuthor = view.findViewById<TextView>(R.id.textViewRowAuthor)
        val textViewPosition = view.findViewById<TextView>(R.id.textViewRowPosition)

        // Verify text content
        assertThat(textViewQuotation.text.toString(), equalTo(quotationText + "\n"))
        assertThat(textViewAuthor.text.toString(), equalTo(authorText + "\n"))
        assertThat(textViewPosition.text.toString(), equalTo(positionText))
    }

    @Test
    fun getViewAt_nullQuotation() {
        // Arrange
        val intent = Intent()
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)

        every { anyConstructed<QuoteUnquoteModel>().getCurrentQuotation(widgetId) } returns null

        // Mock AppearancePreferences for constructor calls
        every { anyConstructed<AppearancePreferences>().appearanceTextFamily } returns "Serif"
        every { anyConstructed<AppearancePreferences>().appearanceTextStyle } returns "Regular"
        every { anyConstructed<AppearancePreferences>().appearanceTextForceItalicRegular } returns false
        every { anyConstructed<AppearancePreferences>().appearanceTextCenter } returns false
        every { anyConstructed<AppearancePreferences>().appearanceTextRightSource } returns false

        val listViewProvider = ListViewProvider(context, intent)
        listViewProvider.onDataSetChanged()

        // Act
        val remoteViews = listViewProvider.getViewAt(0)

        // Assert
        assertThat(remoteViews, notNullValue())

        val parent = LinearLayout(context)
        val view = remoteViews.apply(context, parent)

        val textViewQuotation = view.findViewById<TextView>(R.id.textViewRowQuotation)

        // When quotationEntity is null, getRemoteViews() won't call setRemoteViewQuotation()
        assertThat(textViewQuotation.text.toString(), equalTo(""))
    }
}
