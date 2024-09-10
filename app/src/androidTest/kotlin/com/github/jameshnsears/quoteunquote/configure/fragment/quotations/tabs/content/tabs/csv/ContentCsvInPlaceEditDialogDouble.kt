package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs.csv

import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelDouble
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection

class ContentCsvInPlaceEditDialogDouble() : ContentCsvInPlaceEditDialog() {
    private var databaseRepositoryDouble: DatabaseRepositoryDouble =
        DatabaseRepositoryDouble.getInstance(ApplicationProvider.getApplicationContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        quoteUnquoteModel = QuoteUnquoteModelDouble()
        populateExternal(widgetId)
        quoteUnquoteModel.databaseRepository = databaseRepositoryDouble
    }

    private fun populateExternal(widgetId: Int) {
        DatabaseRepositoryDouble.useInternalDatabase = false

        insertExternalQuotations()

        databaseRepositoryDouble.markAsCurrent(
            widgetId,
            DatabaseRepositoryDouble.getDefaultQuotationDigest(),
        )

        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.ALL,
            DatabaseRepositoryDouble.getDefaultQuotationDigest(),
        )

        databaseRepositoryDouble.markAsFavourite(
            databaseRepositoryDouble.getCurrentQuotation(widgetId).digest,
        )
    }

    private fun insertExternalQuotations() {
        DatabaseRepositoryDouble.useInternalDatabase = false

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
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

        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }
}
