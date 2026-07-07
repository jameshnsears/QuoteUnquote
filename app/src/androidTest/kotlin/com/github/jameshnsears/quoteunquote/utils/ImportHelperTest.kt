package com.github.jameshnsears.quoteunquote.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelUtility
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertFailsWith

class ImportHelperTest : QuoteUnquoteModelUtility() {
    @Test
    fun csvImportKaa() {
        //  original Kaa.csv (in git history) could not be imported due to some quotes being on multiple lines.
        val inputStream: InputStream = getImportAsset("Kaa.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertThat(quotationEntityLinkedHashSet.size, equalTo(762))

        quoteUnquoteModelDouble.insertQuotationsExternal(quotationEntityLinkedHashSet)
        quoteUnquoteModelDouble.setDefault(WidgetIdHelper.WIDGET_ID_01, ContentSelection.ALL)
        assertThat(
            quoteUnquoteModelDouble.databaseRepository!!.getPrevious(false)[0].digest,
            equalTo("00000000"),
        )
    }

    @Test
    fun csvExportOfFavourites() {
        val inputStream: InputStream = getImportAsset("Favourites.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertThat(quotationEntityLinkedHashSet.size, equalTo(2))
    }

    @Test
    fun csvImportWithDupeLines() {
        val inputStream: InputStream = getImportAsset("ImportWithDupeLines.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertThat(databaseRepositoryDouble.countAllExternal().blockingGet(), equalTo(0))

        quotationEntityLinkedHashSet.forEach { quotationEntity ->
            databaseRepositoryDouble.insertQuotationExternal(quotationEntity)
        }

        assertThat(databaseRepositoryDouble.countAllExternal().blockingGet(), equalTo(3))
    }

    @Test
    fun importCsvWithHeader() {
        val inputStream: InputStream = getImportAsset("ImportWithHeader.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        // header is treated as actual data
        assertThat(quotationEntityLinkedHashSet.size, equalTo(2))
    }

    @Test
    fun importCsvWithoutHeader() {
        val inputStream: InputStream = getImportAsset("ImportMissingHeader.csv")

        val importHelper = ImportHelper()
        val quotationEntityLinkedHashSet = importHelper.importCsv(inputStream)

        assertThat(quotationEntityLinkedHashSet.size, equalTo(1))
    }

    @Test
    fun importCsvOnlyAuthorNoDelimiter() {
        val inputStream: InputStream = getImportAsset("ImportOnlyAuthorNoDelimiter.csv")

        val importHelper = ImportHelper()
        try {
            importHelper.importCsv(inputStream)
            fail()
        } catch (exception: ImportHelper.ImportHelperException) {
            assertThat(exception.lineNumber, equalTo(3))
            assertThat(
                exception.message,
                equalTo("Index for header 'Quotation' is 1 but CSVRecord only has 1 values!"),
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
            assertThat(exception.lineNumber, equalTo(4))
            assertThat(
                exception.message,
                equalTo("empty quotation"),
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
            assertThat(exception.lineNumber, equalTo(1))
            assertThat(exception.message, equalTo("empty author"))
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
            assertThat(exception.lineNumber, equalTo(2))
            assertThat(
                exception.message,
                equalTo("empty quotation"),
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
            assertThat(exception.lineNumber, equalTo(-1))
            assertThat(exception.message, equalTo("empty file"))
        }
    }

    @Test
    fun fortuneImportValidSpecific() {
        val importHelper = ImportHelper()

        val aClaude = "fortune/JKirchartz/AClaude"
        val quotations =
            importHelper.importFortune(
                aClaude,
                getImportAsset(aClaude),
            )

        assertThat(quotations.size, equalTo(18))

        assertThat(
            quotations.iterator().next().digest,
            equalTo(ImportHelper.DEFAULT_DIGEST),
        )

        val wblake = "fortune/JKirchartz/wblake"
        assertThat(
            importHelper
                .importFortune(
                    wblake,
                    getImportAsset(wblake),
                ).size,
            equalTo(80),
        )

        val art = "fortune/www.shlomifish.org/art"
        assertThat(
            importHelper
                .importFortune(
                    art,
                    getImportAsset(art),
                ).size,
            equalTo(474),
        )

        val zippy = "fortune/www.shlomifish.org/zippy"
        assertThat(
            importHelper
                .importFortune(
                    zippy,
                    getImportAsset(zippy),
                ).size,
            equalTo(548),
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
                        val quotations =
                            importHelper.importFortune(
                                filePath,
                                assetManager.open(filePath),
                            )

                        val iterator = quotations.iterator()

                        assertThat(
                            iterator.next().digest,
                            equalTo(ImportHelper.DEFAULT_DIGEST),
                        )

                        while (iterator.hasNext()) {
                            assertThat(
                                iterator.next().digest,
                                not(equalTo(ImportHelper.DEFAULT_DIGEST)),
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

    @Test
    fun makeDigest() {
        // confirm same as from python
        assertThat(
            ImportHelper.makeDigest(
                "The only thing that interferes with my learning is my education.",
                "Albert Einstein",
            ),
            equalTo("e5da2450"),
        )
    }
}
