package com.github.jameshnsears.quoteunquote

import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.db.q.AuthorPOJO
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class QuoteUnquoteModelTest : QuoteUnquoteModelUtility() {
    @Test
    fun isDuplicate() {
        insertExternalQuotations()
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(
            quoteUnquoteModelDouble.isDuplicate(
                "external_a1",
                "external_q1",
            ),
            `is`(true),
        )

        assertThat(
            quoteUnquoteModelDouble.isDuplicate(
                "external_a2",
                "external_q2",
            ),
            `is`(false),
        )
    }

    @Test
    fun append() {
        insertExternalQuotations()
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(2))

        quoteUnquoteModelDouble.append(
            "external_a2",
            "external_q2",
        )

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(3))
        assertThat(
            quoteUnquoteModelDouble.allQuotations[2].digest,
            not(equalTo(ImportHelper.DEFAULT_DIGEST)),
        )
    }

    @Test
    fun appendWhenQuotationsListEmpty() {
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(0))

        quoteUnquoteModelDouble.append(
            "external_a0",
            "external_q0",
        )

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(1))
        assertThat(quoteUnquoteModelDouble.allQuotations[0].digest, equalTo(ImportHelper.DEFAULT_DIGEST))
    }

    @Test
    fun update() {
        insertExternalQuotations()
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        var secondQuotation = quoteUnquoteModelDouble.getQuotation("00000001")
        assertThat(secondQuotation.author, equalTo("external_a1"))
        assertThat(secondQuotation.quotation, equalTo("external_q1"))

        // the digest doesn't change
        quoteUnquoteModelDouble.update(
            "00000001",
            "aa",
            "qq",
        )

        secondQuotation = quoteUnquoteModelDouble.getQuotation("00000001")
        assertThat(secondQuotation.author, equalTo("aa"))
        assertThat(secondQuotation.quotation, equalTo("qq"))
    }

    @Test
    fun deleteNonDefault() {
        insertExternalQuotations()
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(2))

        val secondQuotation = quoteUnquoteModelDouble.allQuotations[1]
        assertThat(secondQuotation.digest, equalTo("00000001"))
        assertThat(secondQuotation.author, equalTo("external_a1"))
        assertThat(secondQuotation.quotation, equalTo("external_q1"))

        quoteUnquoteModelDouble.delete(WidgetIdHelper.WIDGET_ID_01, "00000001")

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(1))
    }

    @Test
    fun deleteDefault() {
        insertExternalQuotations()
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(2))

        var firstQuotation = quoteUnquoteModelDouble.allQuotations[0]
        assertThat(firstQuotation.digest, equalTo(ImportHelper.DEFAULT_DIGEST))
        assertThat(firstQuotation.author, equalTo("external_a0"))
        assertThat(firstQuotation.quotation, equalTo("external_q0"))

        val secondQuotation = quoteUnquoteModelDouble.allQuotations[1]
        assertThat(secondQuotation.digest, equalTo("00000001"))
        assertThat(secondQuotation.author, equalTo("external_a1"))
        assertThat(secondQuotation.quotation, equalTo("external_q1"))

        quoteUnquoteModelDouble.delete(WidgetIdHelper.WIDGET_ID_01, ImportHelper.DEFAULT_DIGEST)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(1))

        firstQuotation = quoteUnquoteModelDouble.allQuotations[0]
        assertThat(firstQuotation.digest, equalTo(ImportHelper.DEFAULT_DIGEST))
        assertThat(firstQuotation.author, equalTo("external_a1"))
        assertThat(firstQuotation.quotation, equalTo("external_q1"))
    }

    @Test
    fun deleteAll() {
        insertExternalQuotations()

        quoteUnquoteModelDouble.setUseInternalDatabase(false)
        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(2))

        quoteUnquoteModelDouble.delete(WidgetIdHelper.WIDGET_ID_01, ImportHelper.DEFAULT_DIGEST)

        assertThat(quoteUnquoteModelDouble.allQuotations[0].author, equalTo("external_a1"))
        assertThat(quoteUnquoteModelDouble.allQuotations[0].quotation, equalTo("external_q1"))

        val previousDigests =
            quoteUnquoteModelDouble.getPreviousDigests(
                WidgetIdHelper.WIDGET_ID_01,
                ContentSelection.ALL,
                "",
            )
        assertThat(previousDigests[0], equalTo(ImportHelper.DEFAULT_DIGEST))

        val currentDigest = quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)
        assertThat(currentDigest?.digest, equalTo(ImportHelper.DEFAULT_DIGEST))

        quoteUnquoteModelDouble.delete(WidgetIdHelper.WIDGET_ID_01, ImportHelper.DEFAULT_DIGEST)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(0))
    }

    @Test
    fun deleteAllThenAppend() {
        insertExternalQuotations()

        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(2))
        quoteUnquoteModelDouble.delete(WidgetIdHelper.WIDGET_ID_01, ImportHelper.DEFAULT_DIGEST)
        quoteUnquoteModelDouble.delete(WidgetIdHelper.WIDGET_ID_01, ImportHelper.DEFAULT_DIGEST)

        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(0))

        quoteUnquoteModelDouble.append("a", "q")
        assertThat(quoteUnquoteModelDouble.allQuotations.size, equalTo(1))
        assertThat(quoteUnquoteModelDouble.allQuotations[0].digest, equalTo(ImportHelper.DEFAULT_DIGEST))
    }

    @Test
    fun authorsSorted() {
        val sortedList =
            quoteUnquoteModelDouble.authorsSorted(
                mutableListOf(
                    AuthorPOJO("c"),
                    AuthorPOJO("b"),
                    AuthorPOJO("a"),
                ),
            )

        assertThat(sortedList[0], equalTo("a"))
        assertThat(sortedList[2], equalTo("c"))
    }

    @Test
    fun countAuthorQuotations() {
        val authorPOJO = AuthorPOJO("a2")
        authorPOJO.count = 2
        quoteUnquoteModelDouble.cachedAuthorPOJOList = mutableListOf(authorPOJO)

        assertThat(quoteUnquoteModelDouble.countAuthorQuotations("a2"), equalTo(2))
    }

    @Test
    fun exportFavourites() {
        assertThat(quoteUnquoteModelDouble.exportFavourites()?.size, equalTo(0))

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d1", "w1", "a1", "q1"))
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        databaseRepositoryDouble.markAsFavourite(true, "d1")

        assertThat(quoteUnquoteModelDouble.exportFavourites()?.size, equalTo(1))
    }

    @Test
    fun countQuotationWithSearchText() {
        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText("a", false),
            equalTo(0),
        )
        insertInternalQuotations()
        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText("a", false),
            equalTo(5),
        )

        quoteUnquoteModelDouble.setUseInternalDatabase(false)
        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText("external", false),
            equalTo(0),
        )
        insertExternalQuotations()
        assertThat(
            quoteUnquoteModelDouble.countQuotationWithSearchText("external", false),
            equalTo(2),
        )
    }

    @Test
    fun externalDatabaseContainsQuotations() {
        quoteUnquoteModelDouble.setUseInternalDatabase(false)

        assertThat(quoteUnquoteModelDouble.externalDatabaseContainsQuotations(), `is`(false))

//        insertExternalQuotations()

        val quotationEntityList = LinkedHashSet<QuotationEntity>()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(false),
                "",
                "external_a0",
                "external_q0",
            ),
        )

        quoteUnquoteModelDouble.insertQuotationsExternal(quotationEntityList)

        assertThat(quoteUnquoteModelDouble.externalDatabaseContainsQuotations(), `is`(true))
    }

    @Test
    fun getQuotation() {
        assertThat(quoteUnquoteModelDouble.getQuotation("1624c314"), nullValue())

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(DatabaseRepository.getDefaultQuotationDigest(true), "w1", "a1", "q1"),
        )
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        assertThat(
            quoteUnquoteModelDouble.getQuotation(DatabaseRepository.getDefaultQuotationDigest(true)).author,
            equalTo("a1"),
        )
    }

    @Test
    fun countPrevious() {
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), equalTo(0))

        // arrange
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(true),
                "w1",
                "a1",
                "q1",
            ),
        )
        quotationEntityList.add(QuotationEntity("d2", "w2", "a2", "q2"))
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        // act
        quoteUnquoteModelDouble.markAsCurrentDefault(WidgetIdHelper.WIDGET_ID_01)
        quoteUnquoteModelDouble.markAsCurrentNext(WidgetIdHelper.WIDGET_ID_01, false)

        // assert
        assertThat(quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.WIDGET_ID_01), equalTo(2))
    }

    @Test
    fun setDefaultSearch() {
        assertThat(quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01), nullValue())

        // arrange
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(true),
                "w1",
                "a1",
                "q1",
            ),
        )
        quotationEntityList.add(QuotationEntity("d2", "w2", "a2", "q2"))
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        // act
        quoteUnquoteModelDouble.setDefault(
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.SEARCH,
        )

        // assert
        assertThat(
            quoteUnquoteModelDouble.getCurrentQuotation(WidgetIdHelper.WIDGET_ID_01)?.digest,
            equalTo(DatabaseRepository.getDefaultQuotationDigest(true)),
        )
    }

    @Test
    fun alignHistoryWithQuotations() {
        // arrange
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(true),
                "w1",
                "a1",
                "q1",
            ),
        )
        quotationEntityList.add(QuotationEntity("d2", "w2", "a2", "q2"))
        quotationEntityList.add(QuotationEntity("d3", "w3", "a3", "q3"))
        quotationEntityList.add(QuotationEntity("d4", "w4", "a4", "q4"))
        databaseRepositoryDouble.insertQuotations(true, quotationEntityList)

        databaseRepositoryDouble.markAsPrevious(
            true,
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            DatabaseRepository.getDefaultQuotationDigest(true),
        )
        databaseRepositoryDouble.markAsPrevious(
            true,
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "d2",
        )
        databaseRepositoryDouble.markAsPrevious(
            true,
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "d3",
        )
        databaseRepositoryDouble.markAsPrevious(
            true,
            WidgetIdHelper.WIDGET_ID_01,
            ContentSelection.ALL,
            "d4",
        )

        databaseRepositoryDouble.markAsCurrent(
            true,
            WidgetIdHelper.WIDGET_ID_01,
            "d3",
        )

        databaseRepositoryDouble.markAsFavourite(true, "d3")

        // act
        databaseRepositoryDouble.eraseQuotation(true, "d3") // restore has fewer quotations
        quoteUnquoteModelDouble.alignHistoryWithQuotations(WidgetIdHelper.WIDGET_ID_01)

        // assert
        assertThat(databaseRepositoryDouble.countPreviousCriteria(true, WidgetIdHelper.WIDGET_ID_01), equalTo(3))

        for (previousEntity in databaseRepositoryDouble.getPrevious(true)) {
            assertThat(previousEntity.digest, not(equalTo("d3")))
        }

        assertThat(databaseRepositoryDouble.countFavourites(true).blockingGet(), equalTo(0))

        assertThat(
            databaseRepositoryDouble.getCurrentQuotation(true, WidgetIdHelper.WIDGET_ID_01).digest,
            equalTo("d4"),
        )
    }
}
