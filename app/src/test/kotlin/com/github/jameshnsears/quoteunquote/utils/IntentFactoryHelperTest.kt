package com.github.jameshnsears.quoteunquote.utils

import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.listview.ListViewService
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class IntentFactoryHelperTest {
    @Test
    fun createSimpleIntent() {
        val intent = IntentFactoryHelper.createIntent(WidgetIdHelper.WIDGET_ID_01)

        val intentBundle = intent.extras as Bundle
        assertThat(
            intentBundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID),
            equalTo(WidgetIdHelper.WIDGET_ID_01),
        )
    }

    @Test
    fun createComplexIntent() {
        val intent =
            IntentFactoryHelper.createIntent(
                getApplicationContext(),
                ListViewService::class.java,
                WidgetIdHelper.WIDGET_ID_01,
            )

        assertThat(intent.extras, notNullValue())
    }

    @Test
    fun createIntentAction() {
        val intent =
            IntentFactoryHelper.createIntentAction(
                getApplicationContext(),
                WidgetIdHelper.WIDGET_ID_01,
                IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION,
            )

        assertThat(intent.extras, notNullValue())
    }

    @Test
    fun createIntentActionView() {
        val intent = IntentFactoryHelper.createIntentActionView("wikipedia.com")

        assertThat(intent.data, notNullValue())
    }
}
