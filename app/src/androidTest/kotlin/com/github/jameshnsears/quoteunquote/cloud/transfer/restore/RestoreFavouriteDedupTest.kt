package com.github.jameshnsears.quoteunquote.cloud.transfer.restore

import com.github.jameshnsears.quoteunquote.cloud.transfer.Current
import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.cloud.transfer.Previous
import com.github.jameshnsears.quoteunquote.cloud.transfer.Settings
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class RestoreFavouriteDedupTest : TransferRestoreUtility() {
    @Test
    fun restoreSameFileTwiceShouldNotDuplicateFavourites() {
        if (canWorkWithMockk()) {
            setupWidgets(intArrayOf(1))

            val transfer =
                Transfer(
                    code = "testCode",
                    current = listOf(Current("d1234567", 1, "internal")),
                    favourites =
                        listOf(
                            Favourite("d1234567", 1, "internal"),
                            Favourite("d2345678", 2, "internal"),
                        ),
                    previous = listOf(Previous(1, "d1234567", 0, 1, "internal")),
                    settings = listOf(mockk<Settings>(relaxed = true)),
                )

            TransferRestore().restore(context, databaseRepositoryDouble, transfer)
            assertThat(databaseRepositoryDouble.getFavouritesDigests(true).size, equalTo(2))

            TransferRestore().restore(context, databaseRepositoryDouble, transfer)
            assertThat(databaseRepositoryDouble.getFavouritesDigests(true).size, equalTo(2))
        }
    }

    @Test
    fun restoreSameFileMultipleTimesShouldNeverDuplicate() {
        if (canWorkWithMockk()) {
            setupWidgets(intArrayOf(1))

            val transfer =
                Transfer(
                    code = "testCode",
                    current = listOf(Current("d1234567", 1, "internal")),
                    favourites =
                        listOf(
                            Favourite("d1234567", 1, "internal"),
                            Favourite("d2345678", 2, "internal"),
                            Favourite("d3456789", 3, "internal"),
                        ),
                    previous = listOf(Previous(1, "d1234567", 0, 1, "internal")),
                    settings = listOf(mockk<Settings>(relaxed = true)),
                )

            repeat(3) {
                TransferRestore().restore(context, databaseRepositoryDouble, transfer)
            }

            assertThat(databaseRepositoryDouble.getFavouritesDigests(true).size, equalTo(3))
        }
    }

    @Test
    fun restoreTwiceWithDifferentDigestsShouldReplaceFavourites() {
        if (canWorkWithMockk()) {
            setupWidgets(intArrayOf(1))

            val transfer1 =
                Transfer(
                    code = "testCode",
                    current = listOf(Current("d1234567", 1, "internal")),
                    favourites =
                        listOf(
                            Favourite("d1234567", 1, "internal"),
                            Favourite("d2345678", 2, "internal"),
                        ),
                    previous = listOf(Previous(1, "d1234567", 0, 1, "internal")),
                    settings = listOf(mockk<Settings>(relaxed = true)),
                )

            val transfer2 =
                Transfer(
                    code = "testCode",
                    current = listOf(Current("d1234567", 1, "internal")),
                    favourites =
                        listOf(
                            Favourite("d3456789", 1, "internal"),
                        ),
                    previous = listOf(Previous(1, "d1234567", 0, 1, "internal")),
                    settings = listOf(mockk<Settings>(relaxed = true)),
                )

            TransferRestore().restore(context, databaseRepositoryDouble, transfer1)
            TransferRestore().restore(context, databaseRepositoryDouble, transfer2)

            val favourites = databaseRepositoryDouble.getFavouritesDigests(true)
            // second restore completely replaces the favourites with the new set
            assertThat(favourites.size, equalTo(1))
            assertThat(favourites.contains("d3456789"), equalTo(true))
        }
    }
}
