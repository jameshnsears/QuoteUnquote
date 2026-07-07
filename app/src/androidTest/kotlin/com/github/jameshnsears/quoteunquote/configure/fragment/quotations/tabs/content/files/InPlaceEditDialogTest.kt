package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import android.os.Bundle
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivityDouble
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class InPlaceEditDialogTest : QuoteUnquoteModelUtility() {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(ConfigureActivityDouble::class.java)

    @get:Rule
    val composeRule = createAndroidComposeRule<ConfigureActivityDouble>()

    @Test
    fun contentCsvInPlaceEditDialog() {
        val widgetId = 1

        val scenario =
            launchFragmentInContainer<InPlaceEditDialogDouble>(
                fragmentArgs =
                    Bundle().apply {
                        putInt("widgetId", widgetId)
                    },
            )

        scenario.onFragment { contentCsvInPlaceEditDialog ->
            assertThat(contentCsvInPlaceEditDialog.quoteUnquoteModel.allQuotations.size, equalTo(19894))
        }

        composeRule.onNodeWithText("Save").assertExists().performClick()
    }
}
