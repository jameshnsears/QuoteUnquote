package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.csv

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelDouble
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivityDouble
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import org.junit.Rule
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertEquals

class FilesCsvViewModelTest : QuoteUnquoteModelUtility() {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(ConfigureActivityDouble::class.java)

    @get:Rule
    val composeRule = createAndroidComposeRule<ConfigureActivityDouble>()

    @Test
    fun updateSelectedIndex() {
        val quoteUnquoteModelDouble = QuoteUnquoteModelDouble()

        val inputStream: InputStream = getImportAsset("ImportAlphabetical.csv")
        val importHelper = ImportHelper()
        quoteUnquoteModelDouble.insertQuotationsExternal(importHelper.importCsv(inputStream))
        DatabaseRepository.useInternalDatabase = false

        assertEquals(5, quoteUnquoteModelDouble.allQuotations.size)

        val filesCsvViewModel = FilesCsvViewModel(
            1,
            quoteUnquoteModelDouble,
        )

        /*
        A||A1
        A||A2
        B||B1
        B||B2
        B||The Default Quotation
         */
        val quotationEntity =
            quoteUnquoteModelDouble.allQuotations[2] // as sorted by db: B||B1

        filesCsvViewModel.populateTextFields(
            quotationEntity.digest,
            quotationEntity.author,
            quotationEntity.quotation,
        )
        filesCsvViewModel.setSelectedItemIndex(2)

        // simulate change
        filesCsvViewModel.populateTextFields(
            quotationEntity.digest,
            "Z",
            quotationEntity.quotation,
        )
        filesCsvViewModel.buttonSavePressed()

        Thread.sleep(2000)
        /*
        A||A1
        A||A2
        B||B2
        B||The Default Quotation
        Z||B1
         */
        assertEquals(4, filesCsvViewModel.getSelectedIndex())
    }
}
