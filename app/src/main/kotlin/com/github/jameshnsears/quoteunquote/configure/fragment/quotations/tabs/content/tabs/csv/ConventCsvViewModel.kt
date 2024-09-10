package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs.csv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ConventCsvViewModel(val quoteUnquoteModel: QuoteUnquoteModel) : ViewModel() {
    private val _list = MutableStateFlow<List<QuotationEntity>>(emptyList())
    val list: StateFlow<List<QuotationEntity>> = _list.asStateFlow()

    private val _selectedItemIndex = MutableStateFlow<Int?>(null)
    val selectedItemIndex: StateFlow<Int?> = _selectedItemIndex.asStateFlow()

    private val _digest = MutableStateFlow("")
    val digest: StateFlow<String> = _digest.asStateFlow()

    private val _source = MutableStateFlow("")
    val source: StateFlow<String> = _source.asStateFlow()

    private val _quotation = MutableStateFlow("")
    val quotation: StateFlow<String> = _quotation.asStateFlow()

    init {
        _list.value = quoteUnquoteModel.quotationsAll
    }

    fun populateTextFields(digest: String = "", author: String = "", quotation: String = "") {
        Timber.d("digest=$digest")
        Timber.d("author=$author")
        Timber.d("quotation=$quotation")

        _digest.value = digest
        _source.value = author
        _quotation.value = quotation
    }

    fun setSelectedItemIndex(selectedItemIndex: Int? = null) {
        _selectedItemIndex.value = selectedItemIndex
    }

    fun buttonSavePressed(): Int {
        Timber.d("save")

        val saveResponse: Int

        if (isDuplicate()) {
            saveResponse = 2
        } else {
            if (digest.value.isEmpty()) {
                viewModelScope.launch {
                    quoteUnquoteModel.append(
                        _source.value,
                        _quotation.value
                    )
                    _list.value = quoteUnquoteModel.quotationsAll
                }
                quoteUnquoteModel

                saveResponse = 1
            } else {
                // TODO update
                viewModelScope.launch {
                    quoteUnquoteModel.update(
                        _digest.value,
                        _source.value,
                        _quotation.value
                    )
                    _list.value = quoteUnquoteModel.quotationsAll
                }

                saveResponse = 0
            }
        }

        return saveResponse
    }

    private fun isDuplicate() : Boolean {
        return quoteUnquoteModel.isDuplicate(
            _source.value,
            _quotation.value
        )
    }

    fun buttonDeletePressed() {
        Timber.d("delete")

        // TODO delete
        viewModelScope.launch {
            _list.value = quoteUnquoteModel.quotationsAll
        }

        setSelectedItemIndex()

        populateTextFields()
    }
}
