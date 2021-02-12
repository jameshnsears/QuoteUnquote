package com.github.jameshnsears.quoteunquote.configure.fragment.content

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelDouble
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble

class ContentViewModelDouble(): ContentViewModel(getApplicationContext()) {
    init {
        databaseRepository = DatabaseRepositoryDouble.getInstance()
    }
}
