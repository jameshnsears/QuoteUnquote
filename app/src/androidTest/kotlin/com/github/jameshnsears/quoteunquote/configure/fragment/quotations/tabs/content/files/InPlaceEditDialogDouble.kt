package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelDouble
import com.github.jameshnsears.quoteunquote.db.DatabaseRepositoryDouble

class InPlaceEditDialogDouble : ContentCsvInPlaceEditDialog() {
    private var databaseRepositoryDouble: DatabaseRepositoryDouble =
        DatabaseRepositoryDouble.getInstance(ApplicationProvider.getApplicationContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        quoteUnquoteModel = QuoteUnquoteModelDouble()
        quoteUnquoteModel.setUseInternalDatabase(true)
        quoteUnquoteModel.databaseRepository = databaseRepositoryDouble
    }
}
