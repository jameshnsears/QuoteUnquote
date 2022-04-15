package com.github.jameshnsears.quoteunquote.utils

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.listview.ListViewService
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class IntentFactoryHelperTest {
    @Test
    fun createSimpleIntent() {
        val intent = IntentFactoryHelper.createIntent(WidgetIdHelper.WIDGET_ID_01)
        assertEquals(WidgetIdHelper.WIDGET_ID_01, intent.extras?.get(AppWidgetManager.EXTRA_APPWIDGET_ID))
    }

    @Test
    fun createIntentShare() {
        val shareIntent = IntentFactoryHelper.createIntentShare("s", "q\na\n")
        val bundle = (shareIntent.extras?.get("android.intent.extra.INTENT") as Intent).extras

        assertEquals("s", bundle?.get("android.intent.extra.SUBJECT"))
        assertEquals("q\na\n", bundle?.get("android.intent.extra.TEXT"))
    }

    @Test
    fun createComplexIntent() {
        val intent = IntentFactoryHelper.createIntent(
            getApplicationContext(),
            ListViewService::class.java, WidgetIdHelper.WIDGET_ID_01
        )

        assertNotNull(intent.extras)
    }

    @Test
    fun createIntentAction() {
        val intent = IntentFactoryHelper.createIntentAction(
            getApplicationContext(), WidgetIdHelper.WIDGET_ID_01,
            IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION
        )

        assertNotNull(intent.extras)
    }

    @Test
    fun createIntentActionView() {
        val intent = IntentFactoryHelper.createIntentActionView("wikipedia.com")

        assertNotNull(intent.data)
    }
}
