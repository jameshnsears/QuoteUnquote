package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.csv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class FilesCsvViewModel(
    val widgetId: Int,
    val quoteUnquoteModel: QuoteUnquoteModel,
) : ViewModel() {
    private val _list = MutableStateFlow<List<QuotationEntity>>(emptyList())
    val list: StateFlow<List<QuotationEntity>> = _list.asStateFlow()

    private val _selectedItemIndex = MutableStateFlow<Int?>(null)
    val selectedItemIndex: StateFlow<Int?> = _selectedItemIndex.asStateFlow()

    private val _digest = MutableStateFlow("")
    val digest: StateFlow<String> = _digest.asStateFlow()

    private val _author = MutableStateFlow("")
    val author: StateFlow<String> = _author.asStateFlow()

    private val _quotation = MutableStateFlow("")
    val quotation: StateFlow<String> = _quotation.asStateFlow()

    init {
        _list.value = quoteUnquoteModel.allQuotations
    }

    fun populateTextFields(digest: String = "", author: String = "", quotation: String = "") {
        _digest.value = digest
        _author.value = author
        _quotation.value = quotation
    }

    fun setSelectedItemIndex(selectedItemIndex: Int? = null) {
        _selectedItemIndex.value = selectedItemIndex
    }

    fun getSelectedIndex() = _selectedItemIndex.value

    fun buttonSavePressed(): Int {
        Timber.d("save")

        val saveResponse: Int

        if (isDuplicate()) {
            saveResponse = 2
        } else {
            if (digest.value.isEmpty()) {
                viewModelScope.launch {
                    quoteUnquoteModel.append(
                        _author.value,
                        _quotation.value,
                    )
                    _list.value = quoteUnquoteModel.allQuotations
                }

                updateSelectedIndex()

                saveResponse = 1
            } else {
                viewModelScope.launch {
                    quoteUnquoteModel.update(
                        _digest.value,
                        _author.value,
                        _quotation.value,
                    )
                    _list.value = quoteUnquoteModel.allQuotations

                    updateSelectedIndex()
                }

                saveResponse = 0
            }
        }

        return saveResponse
    }

    fun updateSelectedIndex() {
        for ((index, quotationEntity) in _list.value.withIndex()) {
            if (quotationEntity.quotation.equals(_quotation.value) &&
                quotationEntity.author.equals(_author.value)
            ) {
                setSelectedItemIndex(index)
                break
            }
        }
    }

    private fun isDuplicate(): Boolean {
        return quoteUnquoteModel.isDuplicate(
            _author.value,
            _quotation.value,
        )
    }

    fun buttonDeletePressed() {
        Timber.d("delete")

        viewModelScope.launch {
            quoteUnquoteModel.delete(
                widgetId,
                _digest.value,
            )
            _list.value = quoteUnquoteModel.allQuotations
        }

        setSelectedItemIndex()

        populateTextFields()
    }
}
