package com.github.jameshnsears.quoteunquote.utils

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class IntentFactoryHelperTest {
    @Test
    fun createIntent() {
        val intent = IntentFactoryHelper.createIntent(WidgetIdHelper.INSTANCE_01_WIDGET_ID)

        assertEquals(WidgetIdHelper.INSTANCE_01_WIDGET_ID, intent.extras?.get(AppWidgetManager.EXTRA_APPWIDGET_ID))
    }

    @Test
    fun createIntentShare() {
        val shareIntent = IntentFactoryHelper.createIntentShare("s", "q")
        val bundle = (shareIntent.extras?.get("android.intent.extra.INTENT") as Intent).extras

        assertEquals("s", bundle?.get("android.intent.extra.SUBJECT"))
        assertEquals("q", bundle?.get("android.intent.extra.TEXT"))
    }
}
