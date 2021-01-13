package com.github.jameshnsears.quoteunquote.report

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.WidgetIdTestHelper
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import io.mockk.every
import io.mockk.mockk

class ReportActivityDouble : ReportActivity() {
    override fun getQuoteUnquoteModel(): QuoteUnquoteModel {
        val quoteUnquoteModel: QuoteUnquoteModel = mockk()
        every { quoteUnquoteModel.markAsReported(any()) } returns Unit
        every { quoteUnquoteModel.isReported(any()) } returns false
        every { quoteUnquoteModel.getNext(any(), any()) } returns QuotationEntity("d", "a", "q")
        return quoteUnquoteModel
    }

    override fun broadcastFinishIntent() {
        // don't broadcast anything for test(s)
    }

    companion object {
        fun getIntent(): Intent {
            val intent = Intent(ApplicationProvider.getApplicationContext(), ReportActivityDouble::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetIdTestHelper.WIDGET_ID)
            return intent
        }
    }
}
