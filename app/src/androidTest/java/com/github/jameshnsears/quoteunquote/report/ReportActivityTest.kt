package com.github.jameshnsears.quoteunquote.report

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportActivityTest : QuoteUnquoteModelUtility() {
    companion object {
        fun getIntent(): Intent {
            val intent = Intent(ApplicationProvider.getApplicationContext(), ReportActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdHelper.INSTANCE_01_WIDGET_ID)
            return intent
        }
    }

    @Test
    fun reportQuotation() {
        insertQuotationsTestData01()
        setDefaultQuotationAsPreviousAll(WidgetIdHelper.INSTANCE_01_WIDGET_ID)

        assertEquals(ContentSelection.ALL, quoteUnquoteModelDouble.selectedContentType(1))
        assertFalse(quoteUnquoteModelDouble.isReported(1))

        quoteUnquoteModelDouble.reportQuotation(1)

        assertTrue(quoteUnquoteModelDouble.isReported(1))
    }

    @Test
    fun reportActivity() {
        val reportActivity = spyk<ReportActivity>()
    }

    /*
                assertFalse(activity.hasQuotationAlreadyBeenReported())

            val spinnerReason: Spinner = activity.findViewById(R.id.spinnerReason)
            assertThat("", spinnerReason.adapter.count, Is.`is`(6))

            val expectedConcurrentHashMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()
            expectedConcurrentHashMap["Report"] = "digest=d; author=a; reason=Attribution; notes="
            assertTrue(expectedConcurrentHashMap == activity.auditProperties)

     */
}
