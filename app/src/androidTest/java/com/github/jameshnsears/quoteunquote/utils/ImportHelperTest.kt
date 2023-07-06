package com.github.jameshnsears.quoteunquote.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Test
import java.io.InputStream

class ImportHelperTest : QuoteUnquoteModelUtility() {
    @Test
    fun csvImportKaa() {
        //  original Kaa.csv (in git history) could not be imported due to some quotes being on multiple lines.
        val inputStream: InputStream = getCsvAsset("Kaa.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.csvImportDatabase(inputStream)

        assertEquals(762, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun csvExportOfFavourites() {
        val inputStream: InputStream = getCsvAsset("Favourites.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.csvImportDatabase(inputStream)

        assertEquals(2, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun csvImportDatabaseWithHeader() {
        val inputStream: InputStream = getCsvAsset("ImportWithHeader.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.csvImportDatabase(inputStream)

        // header is treated as actual data
        assertEquals(2, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun csvImportDatabaseWithoutHeader() {
        val inputStream: InputStream = getCsvAsset("ImportMissingHeader.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.csvImportDatabase(inputStream)

        assertEquals(1, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun csvImportDatabaseOnlyAuthorNoDelimiter() {
        val inputStream: InputStream = getCsvAsset("ImportOnlyAuthorNoDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.csvImportDatabase(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(
                "0 : Index for header 'Quotation' is 1 but CSVRecord only has 1 values!",
                exception.message,
            )
        }
    }

    @Test
    fun csvImportDatabaseOnlyAuthorWithDelimiter() {
        val inputStream: InputStream = getCsvAsset("ImportOnlyAuthorWithDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.csvImportDatabase(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(
                "empty quotation",
                exception.message,
            )
        }
    }

    @Test
    fun csvImportDatabaseEmptyButWithDelimiter() {
        val inputStream: InputStream = getCsvAsset("ImportEmptyButWithDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.csvImportDatabase(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals("empty author", exception.message)
        }
    }

    @Test
    fun csvImportDatabaseMissingQuotation() {
        val inputStream: InputStream = getCsvAsset("ImportMissingQuotation.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.csvImportDatabase(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(
                "empty quotation",
                exception.message,
            )
        }
    }

    @Test
    fun csvImportDatabaseImportEmpty() {
        val inputStream: InputStream = getCsvAsset("ImportEmpty.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.csvImportDatabase(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals("empty file", exception.message)
        }
    }

    private fun getCsvAsset(filename: String) =
        InstrumentationRegistry.getInstrumentation().context.resources.assets
            .open(
                filename,
            )

    @Test
    fun makeDigest() {
        // confirm same as from python
        assertEquals(
            "e5da2450",
            ImportHelper.makeDigest(
                "The only thing that interferes with my learning is my education.",
                "Albert Einstein",
            ),
        )
    }
}
