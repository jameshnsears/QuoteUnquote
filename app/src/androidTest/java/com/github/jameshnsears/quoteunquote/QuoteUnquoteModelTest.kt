package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteUnquoteModelTest : QuoteUnquoteModelUtility() {
    @Test
    fun authorsSorted() {
        val sortedList =
            quoteUnquoteModelDouble.authorsSorted(
                mutableListOf(
                    AuthorPOJO("c"),
                    AuthorPOJO("b"),
                    AuthorPOJO("a")
                )
            )

        assertTrue(sortedList[0].equals("a"))
        assertTrue(sortedList[2].equals("c"))
    }

    @Test
    fun countAuthorQuotations() {
        val authorPOJO = AuthorPOJO("a2")
        authorPOJO.count = 2
        quoteUnquoteModelDouble.cachedAuthorPOJOList = mutableListOf(authorPOJO)

        assertEquals(2, quoteUnquoteModelDouble.countAuthorQuotations("a2"))
    }

    private fun gson(): Gson {
        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        return builder.create()
    }

    @Test
    fun transferBackup() {
        val expectedJson = """{
  "code": "",
  "current": [],
  "favourite": [],
  "previous": [],
  "settings": []
}"""

        val actualJson = quoteUnquoteModelDouble.transferBackup(context)

        assertEquals(
            gson().toJson(expectedJson),
            gson().toJson(actualJson)
        )
    }

    @Test
    fun exportFavourites() {
        assertEquals(0, quoteUnquoteModelDouble.exportFavourites()?.size)

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d1", "w1", "a1", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        databaseRepositoryDouble.markAsFavourite("d1")

        assertEquals(1, quoteUnquoteModelDouble.exportFavourites()?.size)
    }

    @Test
    fun countQuotationWithSearchText() {
        assertEquals(
            0,
            quoteUnquoteModelDouble.countQuotationWithSearchText("a", false)
        )
        insertInternalQuotations()
        assertEquals(
            5,
            quoteUnquoteModelDouble.countQuotationWithSearchText("a", false)
        )

        DatabaseRepository.useInternalDatabase = false
        assertEquals(
            0,
            quoteUnquoteModelDouble.countQuotationWithSearchText("external", false)
        )
        insertExternalQuotations()
        assertEquals(
            2,
            quoteUnquoteModelDouble.countQuotationWithSearchText("external", false)
        )
    }

    @Test
    fun externalDatabaseContainsQuotations() {
        DatabaseRepository.useInternalDatabase = false

        assertFalse(quoteUnquoteModelDouble.externalDatabaseContainsQuotations())

//        insertExternalQuotations()

        val quotationEntityList = LinkedHashSet<QuotationEntity>()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "",
                "external_a0",
                "external_q0"
            )
        )

        quoteUnquoteModelDouble.insertQuotationsExternal(quotationEntityList)

        assertTrue(quoteUnquoteModelDouble.externalDatabaseContainsQuotations())
    }

    @Test
    fun getQuotation() {
        assertNull(quoteUnquoteModelDouble.getQuotation("1624c314"))

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(DatabaseRepository.getDefaultQuotationDigest(), "w1", "a1", "q1")
        )
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        assertEquals(
            "a1",
            quoteUnquoteModelDouble.getQuotation(DatabaseRepository.getDefaultQuotationDigest()).author
        )
    }

    @Test
    fun countPrevious() {
        assertEquals(0, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))

        // arrange
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "w1",
                "a1",
                "q1"
            )
        )
        quotationEntityList.add(QuotationEntity("d2", "w2", "a2", "q2"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        // act
        quoteUnquoteModelDouble.markAsCurrentDefault(WidgetIdHelper.WIDGET_ID_01)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        // assert
        assertEquals(2, quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01))
    }

    @Test
    fun setDefaultSearch() {
        assertNull(quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01))

        // arrange
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "w1",
                "a1",
                "q1"
            )
        )
        quotationEntityList.add(QuotationEntity("d2", "w2", "a2", "q2"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        // act
        quoteUnquoteModelDouble.setDefault(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.SEARCH
        )

        // assert
        assertEquals(
            DatabaseRepository.getDefaultQuotationDigest(),
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest
        )
    }

    @Test
    fun alignHistoryWithQuotations() {
        // arrange
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "w1",
                "a1",
                "q1"
            )
        )
        quotationEntityList.add(QuotationEntity("d2", "w2", "a2", "q2"))
        quotationEntityList.add(QuotationEntity("d3", "w3", "a3", "q3"))
        quotationEntityList.add(QuotationEntity("d4", "w4", "a4", "q4"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)

        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            DatabaseRepository.getDefaultQuotationDigest()
        )
        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "d2"
        )
        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "d3"
        )
        databaseRepositoryDouble.markAsPrevious(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "d4"
        )

        databaseRepositoryDouble.markAsCurrent(
            WidgetIdHelper.WIDGET_ID_01,
            "d3"
        )

        databaseRepositoryDouble.markAsFavourite("d3")

        // act
        databaseRepositoryDouble.eraseQuotation("d3") // restore has fewer quotations
        quoteUnquoteModelDouble.alignHistoryWithQuotations(WidgetIdHelper.WIDGET_ID_01)

        // assert
        assertEquals(3, databaseRepositoryDouble.countPreviousCriteria(WidgetIdHelper.WIDGET_ID_01))

        for (previousEntity in databaseRepositoryDouble.previous) {
            assertNotEquals("d3", previousEntity.digest)
        }

        assertEquals(0, databaseRepositoryDouble.countFavourites().blockingGet())

        assertEquals(
            "d4",
            databaseRepositoryDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01).digest
        )
    }
}
