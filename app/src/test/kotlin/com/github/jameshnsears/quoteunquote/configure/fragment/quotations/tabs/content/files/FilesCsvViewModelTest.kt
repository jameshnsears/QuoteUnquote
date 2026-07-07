package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FilesCsvViewModelTest {
    private val quoteUnquoteModel = mockk<QuoteUnquoteModel>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsAllQuotations() =
        runTest {
            val quotations =
                listOf(
                    QuotationEntity("d1", "s1", "a1", "q1"),
                    QuotationEntity("d2", "s2", "a2", "q2"),
                )
            every { quoteUnquoteModel.allQuotations } returns quotations

            val viewModel = FilesCsvViewModel(1, quoteUnquoteModel, testDispatcher)

            advanceUntilIdle()

            assertEquals(quotations, viewModel.list.value)
        }

    @Test
    fun buttonSavePressedCallsAppendWhenDigestIsEmpty() =
        runTest {
            val viewModel = FilesCsvViewModel(1, quoteUnquoteModel, testDispatcher)
            viewModel.populateTextFields(author = "author", quotation = "quotation")

            every { quoteUnquoteModel.isDuplicate(any(), any()) } returns false

            var result: Int? = null
            viewModel.buttonSavePressed { result = it }

            advanceUntilIdle()

            verify { quoteUnquoteModel.append("author", "quotation") }
            assertEquals(1, result)
        }

    @Test
    fun buttonDeletePressedCallsDelete() =
        runTest {
            val viewModel = FilesCsvViewModel(1, quoteUnquoteModel, testDispatcher)
            viewModel.populateTextFields(digest = "digest", author = "author", quotation = "quotation")

            viewModel.buttonDeletePressed()

            advanceUntilIdle()

            verify { quoteUnquoteModel.delete(1, "digest") }
            assertEquals("", viewModel.digest.value)
            assertEquals("", viewModel.author.value)
            assertEquals("", viewModel.quotation.value)
        }
}
