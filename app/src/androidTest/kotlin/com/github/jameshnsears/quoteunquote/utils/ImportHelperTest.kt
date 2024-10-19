package com.github.jameshnsears.quoteunquote.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ImportHelperTest : QuoteUnquoteModelUtility() {
    @Test
    fun csvImportKaa() {
        //  original Kaa.csv (in git history) could not be imported due to some quotes being on multiple lines.
        val inputStream: InputStream = getImportAsset("Kaa.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertEquals(762, quotationEntityLinkedHashSet.size)

        quoteUnquoteModelDouble.insertQuotationsExternal(quotationEntityLinkedHashSet)
        quoteUnquoteModelDouble.setDefault(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        assertEquals(
            "00000000",
            quoteUnquoteModelDouble.databaseRepository!!.previous[0].digest,
        )
    }

    @Test
    fun csvExportOfFavourites() {
        val inputStream: InputStream = getImportAsset("Favourites.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertEquals(2, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun importCsvWithHeader() {
        val inputStream: InputStream = getImportAsset("ImportWithHeader.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        // header is treated as actual data
        assertEquals(2, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun importCsvWithoutHeader() {
        val inputStream: InputStream = getImportAsset("ImportMissingHeader.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertEquals(1, quotationEntityLinkedHashSet.size)
    }

    @Test
    fun importCsvOnlyAuthorNoDelimiter() {
        val inputStream: InputStream = getImportAsset("ImportOnlyAuthorNoDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.importCsv(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(3, exception.lineNumber)
            assertEquals(
                "Index for header 'Quotation' is 1 but CSVRecord only has 1 values!",
                exception.message,
            )
        }
    }

    @Test
    fun importCsvOnlyAuthorWithDelimiter() {
        val inputStream: InputStream = getImportAsset("ImportOnlyAuthorWithDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.importCsv(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(4, exception.lineNumber)
            assertEquals(
                "empty quotation",
                exception.message,
            )
        }
    }

    @Test
    fun importCsvEmptyButWithDelimiter() {
        val inputStream: InputStream = getImportAsset("ImportEmptyButWithDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.importCsv(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(1, exception.lineNumber)
            assertEquals("empty author", exception.message)
        }
    }

    @Test
    fun importCsvMissingQuotation() {
        val inputStream: InputStream = getImportAsset("ImportMissingQuotation.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.importCsv(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(2, exception.lineNumber)
            assertEquals(
                "empty quotation",
                exception.message,
            )
        }
    }

    @Test
    fun csvImportDatabaseImportEmpty() {
        val inputStream: InputStream = getImportAsset("ImportEmpty.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.importCsv(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertEquals(-1, exception.lineNumber)
            assertEquals("empty file", exception.message)
        }
    }

    @Test
    fun fortuneImportValidSpecific() {
        val importHelper = ImportHelper()

        val aClaude = "fortune/JKirchartz/AClaude"
        val quotations = importHelper.importFortune(
            aClaude,
            getImportAsset(aClaude),
        )

        assertEquals(18, quotations.size)

        assertEquals(
            ImportHelper.DEFAULT_DIGEST,
            quotations.iterator().next().digest,
        )

        val wblake = "fortune/JKirchartz/wblake"
        assertEquals(
            80,
            importHelper.importFortune(
                wblake,
                getImportAsset(wblake),
            ).size,
        )

        val art = "fortune/www.shlomifish.org/art"
        assertEquals(
            474,
            importHelper.importFortune(
                art,
                getImportAsset(art),
            ).size,
        )

        val zippy = "fortune/www.shlomifish.org/zippy"
        assertEquals(
            548,
            importHelper.importFortune(
                zippy,
                getImportAsset(zippy),
            ).size,
        )
    }

    @Test
    fun fortuneImportValidBulk() {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets

        val importHelper = ImportHelper()

        val queue = ArrayDeque<String>()
        queue.add("fortune")

        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val files = assetManager.list(path)?.toList() ?: emptyList()

            for (file in files) {
                val filePath = if (path.isNotEmpty()) "$path/$file" else file
                if (assetManager.list(filePath)?.isNotEmpty() == true) {
                    queue.add(filePath)
                } else {
                    try {
                        val quotations = importHelper.importFortune(
                            filePath,
                            assetManager.open(filePath),
                        )

                        val iterator = quotations.iterator()

                        assertEquals(
                            ImportHelper.DEFAULT_DIGEST,
                            iterator.next().digest,
                        )

                        while (iterator.hasNext()) {
                            assertNotEquals(
                                ImportHelper.DEFAULT_DIGEST,
                                iterator.next().digest,
                            )
                        }
                    } catch (e: Exception) {
                        fail(e.message)
                    }
                }
            }
        }
    }

    @Test
    fun fortuneImportInvalid() {
        val importHelper = ImportHelper()

        assertFailsWith<ImportHelper.ImportHelperException> {
            val empty = "fortune-invalid/empty"
            importHelper.importFortune(
                empty,
                getImportAsset(empty),
            )
        }

        assertFailsWith<ImportHelper.ImportHelperException> {
            val multipleDelimiter = "fortune-invalid/multiple-delimiters"
            importHelper.importFortune(
                multipleDelimiter,
                getImportAsset(multipleDelimiter),
            )
        }

        assertFailsWith<ImportHelper.ImportHelperException> {
            val noDelimiter = "fortune-invalid/no-delimiter"
            importHelper.importFortune(
                noDelimiter,
                getImportAsset(noDelimiter),
            )
        }

        assertFailsWith<ImportHelper.ImportHelperException> {
            val noQuotation = "fortune-invalid/no-quotation"
            importHelper.importFortune(
                noQuotation,
                getImportAsset(noQuotation),
            )
        }
    }

    private fun getImportAsset(filename: String) =
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
