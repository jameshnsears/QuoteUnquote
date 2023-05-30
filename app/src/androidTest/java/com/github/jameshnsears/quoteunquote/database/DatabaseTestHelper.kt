package com.github.jameshnsears.quoteunquote.database

import android.content.Context
import android.os.Build
import androidx.annotation.NonNull
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import junit.framework.TestCase.assertTrue
import org.junit.Before

abstract class DatabaseTestHelper {
    @NonNull
    var databaseRepositoryDouble: DatabaseRepositoryDouble =
        DatabaseRepositoryDouble.getInstance(ApplicationProvider.getApplicationContext())

    @JvmField
    val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    open fun before() {
        databaseRepositoryDouble.eraseAllDatabsaes()
        PreferencesFacade.erase(context)
        DatabaseRepository.useInternalDatabase = true
    }

    fun insertQuotationTestData01() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "w1",
                "a0",
                "q0",
            ),
        )
        quotationEntityList.add(QuotationEntity("d1234567", "w1", "a1", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertQuotationTestData02() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2345678", "w1", "a2", "q1"))
        quotationEntityList.add(QuotationEntity("d3456789", "w1", "a2", "q3"))
        quotationEntityList.add(QuotationEntity("d4567890", "w1", "a4", "q1"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun insertQuotationTestData03() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d5678901", "w1", "a5", "q1"))
        quotationEntityList.add(QuotationEntity("d6789012", "w1", "a2", "q6"))
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    fun setDefaultQuotationAll(widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.ALL,
            getDefaultQuotation().digest,
        )

        databaseRepositoryDouble.markAsCurrent(
            widgetId,
            getDefaultQuotation().digest,
        )
    }

    fun setDefaultQuotationAuthor(widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.AUTHOR,
            getDefaultQuotation().digest,
        )
    }

    fun setDefaultQuotationSearch(widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.SEARCH,
            getDefaultQuotation().digest,
        )
    }

    fun markDefaultQuotationAsFavourite() {
        databaseRepositoryDouble.markAsFavourite(getDefaultQuotation().digest)
    }

    fun getDefaultQuotation(): QuotationEntity {
        return databaseRepositoryDouble.getQuotation(DatabaseRepository.getDefaultQuotationDigest())
    }

    fun populateInternal(widgetId: Int) {
        DatabaseRepositoryDouble.useInternalDatabase = true

        insertInternalQuotations()

        databaseRepositoryDouble.markAsCurrent(
            widgetId,
            "d1234567",
        )

        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.ALL,
            "d1234567",
        )

        databaseRepositoryDouble.markAsFavourite(
            databaseRepositoryDouble.getCurrentQuotation(widgetId).digest,
        )

        assertTrue(databaseRepositoryDouble.countAll().blockingGet() == 5)
    }

    fun insertInternalQuotations() {
        DatabaseRepositoryDouble.useInternalDatabase = true

        insertQuotationTestData01()
        insertQuotationTestData02()
    }

    fun populateExternal(widgetId: Int) {
        DatabaseRepositoryDouble.useInternalDatabase = false

        insertExternalQuotations()

        databaseRepositoryDouble.markAsCurrent(
            widgetId,
            DatabaseRepositoryDouble.getDefaultQuotationDigest(),
        )

        databaseRepositoryDouble.markAsPrevious(
            widgetId,
            ContentSelection.ALL,
            DatabaseRepositoryDouble.getDefaultQuotationDigest(),
        )

        databaseRepositoryDouble.markAsFavourite(
            databaseRepositoryDouble.getCurrentQuotation(widgetId).digest,
        )

        assertTrue(databaseRepositoryDouble.countAll().blockingGet() == 2)
    }

    fun insertExternalQuotations() {
        DatabaseRepositoryDouble.useInternalDatabase = false

        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(),
                "",
                "external_a0",
                "external_q0",
            ),
        )
        quotationEntityList.add(
            QuotationEntity(
                "00000001",
                "",
                "external_a1",
                "external_q1",
            ),
        )
        databaseRepositoryDouble.insertQuotations(quotationEntityList)
    }

    protected fun canWorkWithMockk() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}
