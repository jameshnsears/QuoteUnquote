package com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore

import android.content.Context
import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.cloud.transfer.Previous
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferUtility
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import com.github.jameshnsears.quoteunquote.db.h.FavouriteEntity
import com.github.jameshnsears.quoteunquote.db.h.PreviousEntity
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

class TransferRestoreTest {
    private val transferRestore = TransferRestore()

    @Test
    fun requestJson() {
        val json = transferRestore.requestJson("remoteCode")
        assertThat(json.contains("\"code\": \"remoteCode\""), equalTo(true))
    }

    @Test
    fun newDeviceTransferTransformer() {
        val current1 = Current("digest1", 1, "internal")
        val current2 = Current("digest2", 2, "internal")

        val previous1 = Previous(1, "digest1", 0, 1, "internal")
        val previous2 = Previous(1, "digest2", 0, 2, "internal")
        val previous2Duplicate = Previous(1, "digest2", 0, 2, "internal")

        val settings1 = mockk<Settings>()
        val settings2 = mockk<Settings>()

        val transfer =
            Transfer(
                "code",
                listOf(current1, current2),
                emptyList(),
                listOf(previous1, previous2, previous2Duplicate),
                listOf(settings1, settings2),
            )

        val transformedTransfer = transferRestore.newDeviceTransferTransformer(transfer)

        assertThat(transformedTransfer.code, equalTo("code"))
        assertThat(transformedTransfer.current.size, equalTo(1))
        assertThat(transformedTransfer.current[0], equalTo(current2))

        assertThat(transformedTransfer.previous.size, equalTo(1))
        assertThat(transformedTransfer.previous[0], equalTo(previous2))

        assertThat(transformedTransfer.settings.size, equalTo(1))
        assertThat(transformedTransfer.settings[0], equalTo(settings2))
    }

    @Test
    fun newDeviceTransferTransformerNoPreviousMatch() {
        val current1 = Current("digest1", 1, "internal")
        val current2 = Current("digest2", 2, "internal")

        val previous1 = Previous(1, "digest1", 0, 1, "internal")

        val settings1 = mockk<Settings>()
        val settings2 = mockk<Settings>()

        val transfer =
            Transfer(
                "code",
                listOf(current1, current2),
                emptyList(),
                listOf(previous1),
                listOf(settings1, settings2),
            )

        val transformedTransfer = transferRestore.newDeviceTransferTransformer(transfer)

        assertThat(transformedTransfer.previous.size, equalTo(0))
    }

    @Test
    fun restoreFavouriteShouldSkipDuplicateDigests() {
        val databaseRepository = mockk<DatabaseRepository>(relaxed = true)
        every { databaseRepository.getQuotation(any(), any()) } answers {
            mockk()
        }
        every { databaseRepository.countAllExternal().blockingGet() } returns 0

        val favouriteList =
            listOf(
                Favourite("digest1", 1, "internal"),
                Favourite("digest2", 2, "internal"),
                Favourite("digest1", 3, "internal"),
                Favourite("digest3", 4, "internal"),
                Favourite("digest2", 5, "internal"),
            )

        transferRestore.restoreFavourite(databaseRepository, favouriteList)

        val capturedEntities = slot<List<FavouriteEntity>>()
        verify(exactly = 1) {
            databaseRepository.insertFavourites(capture(capturedEntities), true)
        }

        assertThat(capturedEntities.captured.size, equalTo(3))

        val capturedDigests = capturedEntities.captured.map { it.digest }
        assertThat(capturedDigests.sorted(), equalTo(listOf("digest1", "digest2", "digest3")))
    }

    @Test
    fun restoreFavouriteShouldSkipEntityWhenQuotationNotFound() {
        val databaseRepository = mockk<DatabaseRepository>(relaxed = true)
        every { databaseRepository.getQuotation(any(), "missingDigest") } returns null
        every { databaseRepository.getQuotation(any(), "existingDigest") } returns mockk()
        every { databaseRepository.countAllExternal().blockingGet() } returns 0

        val favouriteList =
            listOf(
                Favourite("existingDigest", 1, "internal"),
                Favourite("missingDigest", 2, "internal"),
            )

        transferRestore.restoreFavourite(databaseRepository, favouriteList)

        val capturedEntities = slot<List<FavouriteEntity>>()
        verify(exactly = 1) {
            databaseRepository.insertFavourites(capture(capturedEntities), true)
        }

        assertThat(capturedEntities.captured.size, equalTo(1))
        assertThat(capturedEntities.captured[0].digest, equalTo("existingDigest"))
    }

    @Test
    fun restorePreviousShouldSkipDuplicateTriples() {
        mockkObject(TransferUtility)
        every { TransferUtility.getWidgetIds(any()) } returns intArrayOf(1)

        val databaseRepository = mockk<DatabaseRepository>(relaxed = true)
        every { databaseRepository.countAllExternal().blockingGet() } returns 0

        val context = mockk<Context>(relaxed = true)

        val previousList =
            listOf(
                Previous(1, "digestA", 0, 1, "internal"),
                Previous(1, "digestB", 0, 2, "internal"),
                Previous(1, "digestA", 0, 3, "internal"),
                Previous(1, "digestC", 0, 4, "internal"),
                Previous(1, "digestB", 0, 5, "internal"),
            )

        transferRestore.restorePrevious(context, databaseRepository, previousList)

        val capturedEntities = slot<List<PreviousEntity>>()
        verify(exactly = 1) {
            databaseRepository.insertPrevious(capture(capturedEntities), true)
        }

        assertThat(capturedEntities.captured.size, equalTo(3))

        val capturedTriples =
            capturedEntities.captured.map {
                Triple(it.widgetId, it.contentType, it.digest)
            }
        assertThat(
            capturedTriples.sortedBy { it.third },
            equalTo(
                listOf(
                    Triple(1, ContentSelection.ALL, "digestA"),
                    Triple(1, ContentSelection.ALL, "digestB"),
                    Triple(1, ContentSelection.ALL, "digestC"),
                ),
            ),
        )
    }

    @Test
    fun restorePreviousShouldKeepSameDigestForDifferentWidgets() {
        mockkObject(TransferUtility)
        every { TransferUtility.getWidgetIds(any()) } returns intArrayOf(1, 2)

        val databaseRepository = mockk<DatabaseRepository>(relaxed = true)
        every { databaseRepository.countAllExternal().blockingGet() } returns 0

        val context = mockk<Context>(relaxed = true)

        val previousList =
            listOf(
                Previous(1, "digestA", 0, 1, "internal"),
            )

        transferRestore.restorePrevious(context, databaseRepository, previousList)

        val capturedEntities = slot<List<PreviousEntity>>()
        verify(exactly = 1) {
            databaseRepository.insertPrevious(capture(capturedEntities), true)
        }

        // Should have one entry per widget (1, 2) since the single previous item
        // expands across all available widgets
        assertThat(capturedEntities.captured.size, equalTo(2))

        val widgetIds = capturedEntities.captured.map { it.widgetId }.sorted()
        assertThat(widgetIds, equalTo(listOf(1, 2)))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
