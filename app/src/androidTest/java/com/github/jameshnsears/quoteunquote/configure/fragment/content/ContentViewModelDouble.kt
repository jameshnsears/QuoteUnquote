package com.github.jameshnsears.quoteunquote.configure.fragment.content

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.github.jameshnsears.quoteunquote.database.DatabaseRepositoryDouble

class ContentViewModelDouble : ContentViewModel(getApplicationContext()) {
    init {
        databaseRepository = DatabaseRepositoryDouble.getInstance()
    }

    override fun localCode(): String {
        return "bc5yX41a20"
    }
}
