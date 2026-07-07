package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import org.junit.Rule
import org.junit.Test

class InPlaceEditTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    class QuoteUnquoteModelDummy : QuoteUnquoteModel() {
        val quotations =
            mutableListOf(
                QuotationEntity("digest1", "wikipedia", "author1", "quotation1"),
                QuotationEntity("digest2", "wikipedia", "author2", "quotation2"),
            )

        override fun getAllQuotations(): List<QuotationEntity> = quotations.toList()

        override fun isDuplicate(
            author: String,
            quotation: String,
        ): Boolean = quotations.any { (it.author == author) && (it.quotation == quotation) }

        override fun append(
            author: String,
            quotation: String,
        ) {
            quotations.add(QuotationEntity("newDigest", "wikipedia", author, quotation))
        }

        override fun update(
            digest: String,
            author: String,
            quotation: String,
        ) {
            val index = quotations.indexOfFirst { it.digest == digest }
            if (index != -1) {
                quotations[index] = QuotationEntity(digest, "wikipedia", author, quotation)
            }
        }

        override fun delete(
            widgetId: Int,
            digest: String,
        ) {
            quotations.removeIf { it.digest == digest }
        }
    }

    @Test
    fun inPlaceEditDisplayListAndSelect() {
        val viewModel = FilesCsvViewModel(1, QuoteUnquoteModelDummy())

        composeTestRule.setContent {
            inPlaceEdit(viewModel)
        }

        // Verify initial state
        composeTestRule.onNodeWithText("quotation1").assertIsDisplayed()
        composeTestRule.onNodeWithText("quotation2").assertIsDisplayed()

        // Select an item
        composeTestRule.onNodeWithText("quotation1").performClick()

        // Verify text fields are populated
        composeTestRule.onNodeWithTag("InPlaceEditTextFields.Source").assertTextContains("author1")
        composeTestRule.onNodeWithTag("InPlaceEditTextFields.Quotation").assertTextContains("quotation1")
    }

    @Test
    fun inPlaceEditSaveNewQuotation() {
        val dummyModel = QuoteUnquoteModelDummy()
        val viewModel = FilesCsvViewModel(1, dummyModel)

        composeTestRule.setContent {
            inPlaceEdit(viewModel)
        }

        // Enter new quotation and author
        composeTestRule.onNodeWithTag("InPlaceEditTextFields.Quotation").performTextInput("New Quotation")
        composeTestRule.onNodeWithTag("InPlaceEditTextFields.Source").performTextInput("New Author")

        composeTestRule.waitForIdle()

        // Save button should be enabled
        composeTestRule.onNodeWithText("Save").assertIsEnabled().performClick()

        composeTestRule.waitForIdle()

        // Verify it was added to the list
        composeTestRule.onAllNodesWithText("New Quotation").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("New Author").assertCountEquals(2)
    }

    @Test
    fun inPlaceEditDeleteQuotation() {
        val dummyModel = QuoteUnquoteModelDummy()
        val viewModel = FilesCsvViewModel(1, dummyModel)

        composeTestRule.setContent {
            inPlaceEdit(viewModel)
        }

        // Select an item
        composeTestRule.onNodeWithText("quotation1").performClick()

        // Delete button should be enabled
        composeTestRule.onNodeWithText("Delete").assertIsEnabled().performClick()

        composeTestRule.waitForIdle()

        // Verify it was removed from the list
        composeTestRule.onNodeWithText("quotation1").assertDoesNotExist()

        // Save and Delete buttons should be disabled/fields cleared
        composeTestRule.onNodeWithText("Delete").assertIsNotEnabled()
    }

    @Test
    fun inPlaceEditUpdateQuotation() {
        val dummyModel = QuoteUnquoteModelDummy()
        val viewModel = FilesCsvViewModel(1, dummyModel)

        composeTestRule.setContent {
            inPlaceEdit(viewModel)
        }

        // Select an item
        composeTestRule.onNodeWithText("quotation1").performClick()

        // Update the quotation text
        composeTestRule.onNodeWithTag("InPlaceEditTextFields.Quotation").performTextReplacement("quotation1 Updated")

        // Save
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.waitForIdle()

        // Verify updated text in the list
        composeTestRule.onAllNodesWithText("quotation1 Updated").assertCountEquals(2)
    }
}
