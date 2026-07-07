package com.github.jameshnsears.quoteunquote.utils.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsPreferences
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class NotificationHelperTest {
    private lateinit var context: Context
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationHelper = NotificationHelper(context)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = Shadows.shadowOf(notificationManager)
    }

    @Test
    fun init_createsNotificationChannels() {
        val channels = notificationManager.notificationChannels
        // Device Unlock, Daily Event, Customizable Interval
        assertThat(channels.size, equalTo(3))

        val channelIds = channels.map { it.id }
        assertThat(notificationHelper.notificationChannelDeviceUnlock, notNullValue())
        assertThat(notificationHelper.notificationChannelEventDaily, notNullValue())
        assertThat(notificationHelper.notificationChannelCustomisableInterval, notNullValue())

        assertThat(
            channelIds.contains(context.getString(R.string.notification_channel_screen_unlock)),
            `is`(true),
        )
        assertThat(
            channelIds.contains(context.getString(R.string.notification_channel_specific_time)),
            `is`(true),
        )
        assertThat(
            channelIds.contains(context.getString(R.string.notification_channel_customisable_interval)),
            `is`(true),
        )
    }

    @Test
    fun displayNotificationDeviceUnlock_postsNotification() {
        val content = createNotificationContent(101)

        notificationHelper.displayNotificationDeviceUnlock(content)

        val notification = shadowNotificationManager.getNotification(101)
        assertThat(notification, notNullValue())
        assertThat(notification!!.channelId, equalTo(notificationHelper.notificationChannelDeviceUnlock))
    }

    @Test
    fun displayNotificationEventDaily_postsNotification() {
        val content = createNotificationContent(102)

        notificationHelper.displayNotificationEventDaily(content)

        val notification = shadowNotificationManager.getNotification(102)
        assertThat(notification, notNullValue())
        assertThat(notification!!.channelId, equalTo(notificationHelper.notificationChannelEventDaily))
    }

    @Test
    fun displayNotificationCustomisableInterval_postsNotification() {
        val content = createNotificationContent(103)

        notificationHelper.displayNotificationCustomisableinterval(content)

        val notification = shadowNotificationManager.getNotification(103)
        assertThat(notification, notNullValue())
        assertThat(notification!!.channelId, equalTo(notificationHelper.notificationChannelCustomisableInterval))
    }

    @Test
    fun dismissNotification_cancelsNotification() {
        val content = createNotificationContent(104)
        notificationHelper.displayNotificationDeviceUnlock(content)
        assertThat(shadowNotificationManager.getNotification(104), notNullValue())

        notificationHelper.dismissNotification(context, 104)
        assertThat(shadowNotificationManager.allNotifications.size, equalTo(0))
    }

    @Test
    fun displayNotification_verifyActions() {
        val content = createNotificationContent(105)
        notificationHelper.displayNotificationDeviceUnlock(content)

        val notification = shadowNotificationManager.getNotification(105)
        assertThat(notification, notNullValue())
        // Action 0: Favourite
        // Action 1: Next
        assertThat(notification!!.actions.size, equalTo(2))
        assertThat(notification.actions[0].title, equalTo(context.getString(R.string.notification_action_favourite)))
        assertThat(notification.actions[1].title, equalTo(context.getString(R.string.fragment_appearance_toolbar_next_random_action)))
    }

    @Test
    fun displayNotification_excludeSource_removesContentTitle() {
        val widgetId = 1
        val notificationsPreferences = NotificationsPreferences(widgetId, context)
        notificationsPreferences.excludeSourceFromNotification = true

        val content = createNotificationContent(106).copy(widgetId = widgetId)
        notificationHelper.displayNotificationDeviceUnlock(content)

        val notification = shadowNotificationManager.getNotification(106)
        assertThat(notification, notNullValue())
        val shadowNotification = Shadows.shadowOf(notification)
        assertThat(shadowNotification.contentTitle, nullValue())
    }

    @Test
    fun displayNotification_includeSource_hasContentTitle() {
        val widgetId = 2
        val notificationsPreferences = NotificationsPreferences(widgetId, context)
        notificationsPreferences.excludeSourceFromNotification = false

        val content = createNotificationContent(107).copy(widgetId = widgetId)
        notificationHelper.displayNotificationDeviceUnlock(content)

        val notification = shadowNotificationManager.getNotification(107)
        assertThat(notification, notNullValue())
        val shadowNotification = Shadows.shadowOf(notification)
        assertThat(shadowNotification.contentTitle, equalTo("Author Name"))
    }

    private fun createNotificationContent(notificationId: Int) =
        NotificationContent(
            context = context,
            widgetId = 1,
            author = "Author Name",
            quotation = "This is a test quotation.",
            digest = "digest01",
            isFavourite = false,
            sequential = false,
            notificationId = notificationId,
            notificationEvent = "test_event",
        )
}
