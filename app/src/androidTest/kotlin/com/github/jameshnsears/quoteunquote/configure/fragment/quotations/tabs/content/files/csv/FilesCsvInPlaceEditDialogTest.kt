package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.csv

import android.os.Bundle
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivityDouble
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FilesCsvInPlaceEditDialogTest : QuoteUnquoteModelUtility() {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(ConfigureActivityDouble::class.java)

    @get:Rule
    val composeRule = createAndroidComposeRule<ConfigureActivityDouble>()

    @Test
    fun contentCsvInPlaceEditDialog() {
        DatabaseRepository.useInternalDatabase = false

        val widgetId = 1

        val scenario = launchFragmentInContainer<FilesCsvInPlaceEditDialogDouble>(
            fragmentArgs = Bundle().apply {
                putInt("widgetId", widgetId)
            },
        )

        scenario.onFragment { contentCsvInPlaceEditDialog ->
            assertTrue(contentCsvInPlaceEditDialog.quoteUnquoteModel.allQuotations.size == 20272)
        }

        composeRule.onNodeWithText("Save").assertExists().performClick()

        onView(withId(R.id.closeButton)).perform(click())
    }
}
