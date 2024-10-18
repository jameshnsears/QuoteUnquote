package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.csv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel

class FilesCsvViewModelFactory(
    private val widgetId: Int,
    private val quoteUnquoteModel: QuoteUnquoteModel,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilesCsvViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FilesCsvViewModel(
                widgetId,
                quoteUnquoteModel,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
