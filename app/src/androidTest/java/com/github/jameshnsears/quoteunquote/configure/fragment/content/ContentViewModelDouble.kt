package com.github.jameshnsears.quoteunquote.configure.fragment.content

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble

class ContentViewModelDouble(databaseRepositoryDouble: DatabaseRepositoryDouble): ContentViewModel(getApplicationContext()) {
    init {
        databaseRepository = DatabaseRepositoryDouble.getInstance();
    }

    override fun shutdown() {
        executorService.shutdown()
    }

    // TODO rm this model and re-use QuoteUnquoteModel?
}
