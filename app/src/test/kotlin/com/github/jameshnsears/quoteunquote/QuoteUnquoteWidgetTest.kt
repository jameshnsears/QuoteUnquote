package com.github.jameshnsears.quoteunquote

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.github.jameshnsears.quoteunquote.cloud.CloudTransferHelper
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearancePreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.scraper.ScraperData
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationCoordinator
import com.github.jameshnsears.quoteunquote.utils.notification.NotificationHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class QuoteUnquoteWidgetTest {
    private lateinit var context: Context
    private lateinit var quoteUnquoteWidget: QuoteUnquoteWidget
    private lateinit var mockModel: QuoteUnquoteModel
    private lateinit var mockNotificationCoordinator: NotificationCoordinator
    private lateinit var mockAppWidgetManager: AppWidgetManager
    private val widgetId = 1

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        quoteUnquoteWidget = spyk(QuoteUnquoteWidget(), recordPrivateCalls = true)
        mockModel = mockk(relaxed = true)
        mockNotificationCoordinator = mockk(relaxed = true)

        // Inject the mock model
        quoteUnquoteWidget.setQuoteUnquoteModel(mockModel)

        // Inject mock coordinator via reflection because it's private
        val fieldCoord = QuoteUnquoteWidget::class.java.getDeclaredField("notificationCoordinator")
        fieldCoord.isAccessible = true
        fieldCoord.set(quoteUnquoteWidget, mockNotificationCoordinator)

        // Inject mock NotificationHelper
        val fieldHelper = QuoteUnquoteWidget::class.java.getDeclaredField("notificationHelper")
        fieldHelper.isAccessible = true
        fieldHelper.set(null, mockk<NotificationHelper>(relaxed = true))

        // Stub preferences
        val mockQuotationsPreferences = mockk<QuotationsPreferences>(relaxed = true)
        every { mockQuotationsPreferences.contentSelection } returns ContentSelection.ALL
        every { quoteUnquoteWidget.getQuotationsPreferences(any(), any()) } returns mockQuotationsPreferences

        every { quoteUnquoteWidget.getAppearancePreferences(any(), any()) } returns mockk(relaxed = true)
        every { quoteUnquoteWidget.onUpdate(any(), any(), any()) } returns Unit

        // Mock static AppWidgetManager
        mockkStatic(AppWidgetManager::class)
        mockAppWidgetManager = mockk(relaxed = true)
        every { AppWidgetManager.getInstance(any()) } returns mockAppWidgetManager
        every { mockAppWidgetManager.getAppWidgetIds(any()) } returns intArrayOf(widgetId)

        // Initialize WorkManager for tests
        try {
            WorkManager.initialize(
                context,
                androidx.work.Configuration
                    .Builder()
                    .build(),
            )
        } catch (e: Exception) {
            // Already initialized or failed to initialize
        }

        // Mock AppearancePreferences constructor
        mockkConstructor(AppearancePreferences::class)
        every { anyConstructed<AppearancePreferences>().appearanceToolbarPosition } returns 0
        every { anyConstructed<AppearancePreferences>().appearanceForceFollowSystemTheme } returns false
        every { anyConstructed<AppearancePreferences>().appearanceToolbarFirst } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onEnabled() {
        mockkStatic(CloudTransferHelper::class)
        every { CloudTransferHelper.getLocalCode() } returns "local-code"

        mockkConstructor(QuotationsPreferences::class)
        every { anyConstructed<QuotationsPreferences>().contentLocalCode } returns ""
        every { anyConstructed<QuotationsPreferences>().contentLocalCode = any() } returns Unit

        quoteUnquoteWidget.onEnabled(context)

        verify { anyConstructed<QuotationsPreferences>().contentLocalCode = "local-code" }
        verify { quoteUnquoteWidget.setQuoteUnquoteModel(any()) }
    }

    @Test
    fun onReceive_ToolbarPressedNextRandom() {
        val intent =
            Intent(IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_RANDOM).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentNext(widgetId, true) }
    }

    @Test
    fun onReceive_ToolbarPressedFirst() {
        val intent =
            Intent(IntentFactoryHelper.TOOLBAR_PRESSED_FIRST).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentDefault(widgetId) }
    }

    @Test
    fun onReceive_ToolbarPressedFavourite() {
        val digest = "test-digest"
        val quotation = QuotationEntity(digest, "", "", "")
        every { mockModel.getCurrentQuotation(widgetId) } returns quotation

        // Stub the private method to avoid side effects that might fail in Robolectric
        every {
            quoteUnquoteWidget["onReceiveToolbarPressedFavourite"](
                any<Context>(),
                any<Int>(),
                any<String>(),
                any<AppWidgetManager>(),
            )
        } returns
            Unit

        val intent =
            Intent(IntentFactoryHelper.TOOLBAR_PRESSED_FAVOURITE).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { quoteUnquoteWidget["onReceiveToolbarPressedFavourite"](any<Context>(), widgetId, digest, any<AppWidgetManager>()) }
    }

    @Test
    fun onReceive_ToolbarPressedJump() {
        val intent =
            Intent(IntentFactoryHelper.TOOLBAR_PRESSED_JUMP).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentLastPrevious(widgetId) }
    }

    @Test
    fun onReceive_ToolbarPressedNextSequential() {
        val intent =
            Intent(IntentFactoryHelper.TOOLBAR_PRESSED_NEXT_SEQUENTIAL).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentNext(widgetId, false) }
    }

    @Test
    fun onReceive_ToolbarPressedPrevious() {
        val intent =
            Intent(IntentFactoryHelper.TOOLBAR_PRESSED_PREVIOUS).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentPrevious(widgetId) }
    }

    @Test
    fun onReceive_MyPackageReplaced() {
        val intent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.alignHistoryWithQuotations(widgetId) }
        verify { mockAppWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listViewQuotation) }
    }

    @Test
    fun onReceive_UserPresent() {
        val intent =
            Intent(Intent.ACTION_USER_PRESENT).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { quoteUnquoteWidget.setQuoteUnquoteModel(any()) }
    }

    @Test
    fun onReceive_ToolbarPressedShare() {
        val quotation = QuotationEntity("digest", "source", "author", "quotation")
        every { mockModel.getCurrentQuotation(widgetId) } returns quotation

        val mockAppearancePreferences = mockk<AppearancePreferences>(relaxed = true)
        every { quoteUnquoteWidget.getAppearancePreferences(any(), any()) } returns mockAppearancePreferences
        every { mockAppearancePreferences.appearanceToolbarShareNoSource } returns false

        quoteUnquoteWidget.onReceiveToolbarPressedShare(context, widgetId)

        val startedIntent = shadowOf(context as android.app.Application).nextStartedActivity
        assertThat(startedIntent.action, equalTo(Intent.ACTION_CHOOSER))
    }

    @Test
    fun getTransparencyMask() {
        val mask = quoteUnquoteWidget.getTransparencyMask(5, "#FF112233")
        assertThat(mask, equalTo(0x7F112233))

        val maskFull = quoteUnquoteWidget.getTransparencyMask(0, "#FF000000")
        assertThat(maskFull, equalTo(0xFF000000.toInt()))

        val maskTransparent = quoteUnquoteWidget.getTransparencyMask(10, "#FFFFFFFF")
        assertThat(maskTransparent, equalTo(0x00FFFFFF))
    }

    @Test
    fun markNotificationAsFavourite() {
        val digest = "fav-digest"
        val quotation = "Some quote"
        every { mockModel.isFavourite(digest) } returns true

        val result =
            ReflectionHelpers.callInstanceMethod<String>(
                quoteUnquoteWidget,
                "markNotificationAsFavourite",
                ReflectionHelpers.ClassParameter.from(Int::class.javaPrimitiveType, widgetId),
                ReflectionHelpers.ClassParameter.from(Context::class.java, context),
                ReflectionHelpers.ClassParameter.from(String::class.java, digest),
                ReflectionHelpers.ClassParameter.from(String::class.java, quotation),
            )
        assertThat(result, equalTo("\u2764 Some quote"))

        every { mockModel.isFavourite(digest) } returns false
        val resultNotFav =
            ReflectionHelpers.callInstanceMethod<String>(
                quoteUnquoteWidget,
                "markNotificationAsFavourite",
                ReflectionHelpers.ClassParameter.from(Int::class.javaPrimitiveType, widgetId),
                ReflectionHelpers.ClassParameter.from(Context::class.java, context),
                ReflectionHelpers.ClassParameter.from(String::class.java, digest),
                ReflectionHelpers.ClassParameter.from(String::class.java, quotation),
            )
        assertThat(resultNotFav, equalTo("Some quote"))
    }

    @Test
    fun isNightMode() {
        val config = Configuration()
        config.uiMode = Configuration.UI_MODE_NIGHT_YES
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        assertThat(
            ReflectionHelpers.callInstanceMethod<Boolean>(
                quoteUnquoteWidget,
                "isNightMode",
                ReflectionHelpers.ClassParameter.from(Context::class.java, context),
            ),
            equalTo(true),
        )

        config.uiMode = Configuration.UI_MODE_NIGHT_NO
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        assertThat(
            ReflectionHelpers.callInstanceMethod<Boolean>(
                quoteUnquoteWidget,
                "isNightMode",
                ReflectionHelpers.ClassParameter.from(Context::class.java, context),
            ),
            equalTo(false),
        )
    }

    @Test
    fun onReceive_ActionAppwidgetEnabled() {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_ENABLED)

        quoteUnquoteWidget.onReceive(context, intent)

        verify { quoteUnquoteWidget.onUpdate(context, mockAppWidgetManager, any()) }
    }

    @Test
    fun getWidgetLayout() {
        val layout =
            ReflectionHelpers.callInstanceMethod<Int>(
                quoteUnquoteWidget,
                "getWidgetLayout",
                ReflectionHelpers.ClassParameter.from(Int::class.javaPrimitiveType, widgetId),
                ReflectionHelpers.ClassParameter.from(Context::class.java, context),
            )
        assertThat(layout, equalTo(R.layout.quote_unquote_widget))

        every { anyConstructed<AppearancePreferences>().appearanceToolbarPosition } returns 1
        val layoutRhs =
            ReflectionHelpers.callInstanceMethod<Int>(
                quoteUnquoteWidget,
                "getWidgetLayout",
                ReflectionHelpers.ClassParameter.from(Int::class.javaPrimitiveType, widgetId),
                ReflectionHelpers.ClassParameter.from(Context::class.java, context),
            )
        assertThat(layoutRhs, equalTo(R.layout.quote_unquote_widget_rhs))
    }

    @Test
    fun onDeleted() {
        val widgetIds = intArrayOf(widgetId)
        quoteUnquoteWidget.onDeleted(context, widgetIds)

        verify { mockModel.delete(widgetId) }
    }

    @Test
    fun onDisabled() {
        quoteUnquoteWidget.onDisabled(context)

        verify { mockModel.disable() }
    }

    @Test
    fun displayAppropriateScrapedQuotation_KeepLatestOnly() {
        val mockQuotationsPreferences = mockk<QuotationsPreferences>(relaxed = true)
        every { quoteUnquoteWidget.getQuotationsPreferences(any(), any()) } returns mockQuotationsPreferences
        every { mockQuotationsPreferences.databaseWebKeepLatestOnly } returns true

        quoteUnquoteWidget.displayAppropriateScrapedQuotation(context, widgetId, "quote", "source")

        verify { mockModel.insertWebPage(widgetId, "quote", "source", ImportHelper.DEFAULT_DIGEST) }
    }

    @Test
    fun displayAppropriateScrapedQuotation_SameAsCurrent() {
        val mockQuotationsPreferences = mockk<QuotationsPreferences>(relaxed = true)
        every { quoteUnquoteWidget.getQuotationsPreferences(any(), any()) } returns mockQuotationsPreferences
        every { mockQuotationsPreferences.databaseWebKeepLatestOnly } returns false

        val currentQuotation = QuotationEntity("digest", "source", "author", "quote")
        every { mockModel.getCurrentQuotation(widgetId) } returns currentQuotation

        quoteUnquoteWidget.displayAppropriateScrapedQuotation(context, widgetId, "quote", "author")

        verify(exactly = 0) { mockModel.insertWebPage(any(), any(), any(), any()) }
        verify(exactly = 0) { mockModel.markAsCurrent(any(), any()) }
    }

    @Test
    fun displayAppropriateScrapedQuotation_SameAsDefault() {
        val mockQuotationsPreferences = mockk<QuotationsPreferences>(relaxed = true)
        every { quoteUnquoteWidget.getQuotationsPreferences(any(), any()) } returns mockQuotationsPreferences
        every { mockQuotationsPreferences.databaseWebKeepLatestOnly } returns false

        every { mockModel.getCurrentQuotation(widgetId) } returns QuotationEntity("d1", "s1", "a1", "q1")

        val defaultQuotation = QuotationEntity(ImportHelper.DEFAULT_DIGEST, "source", "author", "quote")
        every { mockModel.getQuotation(ImportHelper.DEFAULT_DIGEST) } returns defaultQuotation

        quoteUnquoteWidget.displayAppropriateScrapedQuotation(context, widgetId, "quote", "author")

        verify { mockModel.markAsCurrent(widgetId, ImportHelper.DEFAULT_DIGEST) }
    }

    @Test
    fun displayAppropriateScrapedQuotation_NewQuotation() {
        val mockQuotationsPreferences = mockk<QuotationsPreferences>(relaxed = true)
        every { quoteUnquoteWidget.getQuotationsPreferences(any(), any()) } returns mockQuotationsPreferences
        every { mockQuotationsPreferences.databaseWebKeepLatestOnly } returns false

        every { mockModel.getCurrentQuotation(widgetId) } returns QuotationEntity("d1", "s1", "a1", "q1")
        every { mockModel.getQuotation(any()) } returns null

        val quotation = "new quote"
        val author = "new author"
        val digest = ImportHelper.makeDigest(quotation, author)

        quoteUnquoteWidget.displayAppropriateScrapedQuotation(context, widgetId, quotation, author)

        verify { mockModel.insertWebPage(widgetId, quotation, author, digest) }
    }

    @Test
    fun onReceive_ActivityFinishedConfiguration_NullCurrent() {
        every { mockModel.getCurrentQuotation(widgetId) } returns null

        val intent =
            Intent(IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentDefault(widgetId) }
    }

    @Test
    fun onReceive_ActivityFinishedConfiguration_ContentSelectionChanged() {
        every { mockModel.getCurrentQuotation(widgetId) } returns QuotationEntity("d", "s", "a", "q")
        val mockQuotationsPreferences = mockk<QuotationsPreferences>(relaxed = true)
        every { quoteUnquoteWidget.getQuotationsPreferences(any(), any()) } returns mockQuotationsPreferences

        // currentContentSelection is ALL by default in QuoteUnquoteWidget
        every { mockQuotationsPreferences.contentSelection } returns ContentSelection.AUTHOR

        val intent =
            Intent(IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentDefault(widgetId) }
    }

    @Test
    fun onReceive_NotificationNext() {
        val intent =
            Intent(IntentFactoryHelper.NOTIFICATION_NEXT_PRESSED).apply {
                putExtra("widgetId", widgetId)
                putExtra("digest", "digest")
                putExtra("notificationId", 100)
                putExtra("notificationEvent", "event")
            }

        quoteUnquoteWidget.onReceive(context, intent)

        // It should trigger markAsCurrentNext
        verify { mockModel.markAsCurrentNext(widgetId, any()) }
    }

    @Test
    fun onReceive_NotificationDismissed() {
        val intent =
            Intent(IntentFactoryHelper.NOTIFICATION_DISMISSED).apply {
                putExtra("notificationId", 123)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockNotificationCoordinator.forgetNotification(123) }
    }

    @Test
    fun onReceive_NotificationFavourite() {
        val digest = "digest"
        val intent =
            Intent(IntentFactoryHelper.NOTIFICATION_FAVOURITE_PRESSED).apply {
                putExtra("widgetId", widgetId)
                putExtra("digest", digest)
                putExtra("notificationId", 789)
                putExtra("notificationEvent", "event")
            }

        every { mockModel.getQuotation(digest) } returns QuotationEntity(digest, "s", "a", "q")

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.toggleFavourite(widgetId, digest) }
    }

    @Test
    fun onReceive_DailyAlarm() {
        val intent =
            Intent(IntentFactoryHelper.DAILY_ALARM).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentNext(widgetId, any()) }
    }

    @Test
    fun onReceive_CustomisableIntervalAlarm() {
        val intent =
            Intent(IntentFactoryHelper.CUSTOMISABLE_INTERVAL_ALARM).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.markAsCurrentNext(widgetId, any()) }
    }

    @Test
    fun onReceive_ScraperAlarm() {
        val intent =
            Intent(IntentFactoryHelper.SCRAPER_ALARM).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        val scraperData = mockk<ScraperData>(relaxed = true)
        every { scraperData.scrapeResult } returns true
        every { scraperData.quotation } returns "scraped-quote"
        every { scraperData.source } returns "scraped-source"
        every { mockModel.getWebPage(any(), any(), any(), any()) } returns scraperData

        quoteUnquoteWidget.onReceive(context, intent)

        verify { mockModel.getWebPage(any(), any(), any(), any()) }
        verify { quoteUnquoteWidget.displayAppropriateScrapedQuotation(any(), widgetId, "scraped-quote", "scraped-source") }
    }

    @Test
    fun onReceive_AllWidgetInstancesFavouriteNotification() {
        val digest = "test-digest"
        val intent =
            Intent(IntentFactoryHelper.ALL_WIDGET_INSTANCES_FAVOURITE_NOTIFICATION).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra("digest", digest)
                putExtra("notificationId", 456)
            }

        every { mockModel.getCurrentQuotation(widgetId) } returns QuotationEntity(digest, "", "", "")

        quoteUnquoteWidget.onReceive(context, intent)

        verify { quoteUnquoteWidget.setHeartColour(any(), widgetId, any()) }
    }

    @Test
    fun onReceive_ThemeChange() {
        val intent = Intent(Intent.ACTION_CONFIGURATION_CHANGED)

        quoteUnquoteWidget.onReceive(context, intent)

        verify { quoteUnquoteWidget.onUpdate(context, mockAppWidgetManager, intArrayOf(widgetId)) }
    }

    @Test
    fun setHeartColour_Favourite() {
        val digest = "fav-digest"
        val quotation = QuotationEntity(digest, "", "", "")
        every { mockModel.getCurrentQuotation(widgetId) } returns quotation
        every { mockModel.isFavourite(digest) } returns true

        val remoteViews = mockk<RemoteViews>(relaxed = true)

        quoteUnquoteWidget.setHeartColour(context, widgetId, remoteViews)

        verify { remoteViews.setImageViewResource(R.id.imageButtonFavourite, R.drawable.ic_toolbar_favorite_red_24) }
    }
}
