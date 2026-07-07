package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelDouble
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.db.DatabaseRepositoryDouble
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection

class InPlaceEditDialogDouble : ContentCsvInPlaceEditDialog() {
    private var databaseRepositoryDouble: DatabaseRepositoryDouble =
        DatabaseRepositoryDouble.getInstance(ApplicationProvider.getApplicationContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        quoteUnquoteModel = QuoteUnquoteModelDouble()
        quoteUnquoteModel.setUseInternalDatabase(false)
        populateExternal(widgetId)
        quoteUnquoteModel.databaseRepository = databaseRepositoryDouble
    }

    private fun populateExternal(widgetId: Int) {
        insertExternalQuotations()

        databaseRepositoryDouble.markAsCurrent(
            false,
            widgetId,
            DatabaseRepository.getDefaultQuotationDigest(false),
        )

        databaseRepositoryDouble.markAsPrevious(
            false,
            widgetId,
            ContentSelection.ALL,
            DatabaseRepository.getDefaultQuotationDigest(false),
        )

        databaseRepositoryDouble.markAsFavourite(
            false,
            databaseRepositoryDouble.getCurrentQuotation(false, widgetId).digest,
        )
    }

    private fun insertExternalQuotations() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(false),
                "",
                "a1",
                "q-a1-1",
            ),
        )
        quotationEntityList.add(
            QuotationEntity(
                "00000001",
                "",
                "a1",
                "q-a1-2",
            ),
        )
        quotationEntityList.add(
            QuotationEntity(
                "00000002",
                "",
                "b1",
                "q-b1-1",
            ),
        )

        databaseRepositoryDouble.insertQuotations(false, quotationEntityList)
    }
}
