package com.github.jameshnsears.quoteunquote.db

import android.content.Context
import android.os.Build
import androidx.annotation.NonNull
import androidx.test.core.app.ApplicationProvider
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.preference.PreferencesFacade
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
    }

    fun insertQuotationTestData01(useInternalDatabase: Boolean) {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(useInternalDatabase),
                "w1",
                "a0",
                "q0",
            ),
        )
        quotationEntityList.add(QuotationEntity("d1234567", "w1", "a1", "q1"))
        databaseRepositoryDouble.insertQuotations(useInternalDatabase, quotationEntityList)
    }

    fun insertQuotationTestData02(useInternalDatabase: Boolean) {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d2345678", "w1", "a2", "q1"))
        quotationEntityList.add(QuotationEntity("d3456789", "w1", "a2", "q3"))
        quotationEntityList.add(QuotationEntity("d4567890", "w1", "a4", "q1"))
        databaseRepositoryDouble.insertQuotations(useInternalDatabase, quotationEntityList)
    }

    fun insertQuotationTestData03(useInternalDatabase: Boolean) {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(QuotationEntity("d5678901", "w1", "a5", "q1"))
        quotationEntityList.add(QuotationEntity("d6789012", "w1", "a2", "q6"))
        databaseRepositoryDouble.insertQuotations(useInternalDatabase, quotationEntityList)
    }

    fun setDefaultQuotationAll(useInternalDatabase: Boolean, widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            useInternalDatabase,
            widgetId,
            ContentSelection.ALL,
            getDefaultQuotation(useInternalDatabase).digest,
        )

        databaseRepositoryDouble.markAsCurrent(
            useInternalDatabase,
            widgetId,
            getDefaultQuotation(useInternalDatabase).digest,
        )
    }

    fun setDefaultQuotationAuthor(useInternalDatabase: Boolean, widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            useInternalDatabase,
            widgetId,
            ContentSelection.AUTHOR,
            getDefaultQuotation(useInternalDatabase).digest,
        )
    }

    fun setDefaultQuotationSearch(useInternalDatabase: Boolean, widgetId: Int) {
        databaseRepositoryDouble.markAsPrevious(
            useInternalDatabase,
            widgetId,
            ContentSelection.SEARCH,
            getDefaultQuotation(useInternalDatabase).digest,
        )
    }

    fun markDefaultQuotationAsFavourite(useInternalDatabase: Boolean) {
        databaseRepositoryDouble.markAsFavourite(useInternalDatabase, getDefaultQuotation(useInternalDatabase).digest)
    }

    fun getDefaultQuotation(useInternalDatabase: Boolean): QuotationEntity =
        databaseRepositoryDouble.getQuotation(useInternalDatabase, DatabaseRepository.getDefaultQuotationDigest(useInternalDatabase))

    fun populateInternal(widgetId: Int) {
        insertInternalQuotations()

        databaseRepositoryDouble.markAsCurrent(
            true,
            widgetId,
            "d1234567",
        )

        databaseRepositoryDouble.markAsPrevious(
            true,
            widgetId,
            ContentSelection.ALL,
            "d1234567",
        )

        databaseRepositoryDouble.markAsFavourite(
            true,
            databaseRepositoryDouble.getCurrentQuotation(true, widgetId).digest,
        )

        assertThat(databaseRepositoryDouble.countAll(true).blockingGet(), equalTo(5))
    }

    fun insertInternalQuotations() {
        insertQuotationTestData01(true)
        insertQuotationTestData02(true)
    }

    fun populateExternal(widgetId: Int) {
        insertExternalQuotations()

        databaseRepositoryDouble.markAsCurrent(
            false,
            widgetId,
            DatabaseRepository.getDefaultQuotationDigest(false),
        )

        databaseRepositoryDouble.markAsPrevious(
            false,
            widgetId,
            ContentSelection.ALL,
            DatabaseRepository.getDefaultQuotationDigest(false),
        )

        databaseRepositoryDouble.markAsFavourite(
            false,
            databaseRepositoryDouble.getCurrentQuotation(false, widgetId).digest,
        )

        assertThat(databaseRepositoryDouble.countAll(false).blockingGet(), equalTo(2))
    }

    fun insertExternalQuotations() {
        val quotationEntityList: MutableList<QuotationEntity> = ArrayList()
        quotationEntityList.add(
            QuotationEntity(
                DatabaseRepository.getDefaultQuotationDigest(false),
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
        databaseRepositoryDouble.insertQuotations(false, quotationEntityList)
    }

    protected fun canWorkWithMockk() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}
