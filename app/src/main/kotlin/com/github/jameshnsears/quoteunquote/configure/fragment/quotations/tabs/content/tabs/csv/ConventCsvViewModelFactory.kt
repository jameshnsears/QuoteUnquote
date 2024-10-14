package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs.csv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel

class ConventCsvViewModelFactory(
    private val widgetId: Int,
    private val quoteUnquoteModel: QuoteUnquoteModel,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConventCsvViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConventCsvViewModel(
                widgetId,
                quoteUnquoteModel,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
