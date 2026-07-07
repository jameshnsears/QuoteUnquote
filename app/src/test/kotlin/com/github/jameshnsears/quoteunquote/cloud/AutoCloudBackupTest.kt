package com.github.jameshnsears.quoteunquote.cloud

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AutoCloudBackupTest {
    private lateinit var context: Context
    private lateinit var workerParameters: WorkerParameters

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workerParameters = mockk(relaxed = true)

        mockkConstructor(CloudTransfer::class)
        mockkConstructor(QuoteUnquoteModel::class)
        mockkConstructor(SyncPreferences::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun doWorkReturnsSuccessWhenBackupIsSuccessful() =
        runBlocking {
            // Arrange
            val widgetId = 1
            every { workerParameters.inputData } returns Data.Builder().putInt(AutoCloudBackup.KEY_WIDGET_ID, widgetId).build()
            val worker = AutoCloudBackup(context, workerParameters)

            every { anyConstructed<CloudTransfer>().isInternetAvailable() } returns true
            every { anyConstructed<CloudTransfer>().backup(any()) } returns true
            every { anyConstructed<QuoteUnquoteModel>().transferBackup(any()) } returns "backup data"

            // Mocking SyncPreferences setter. In Kotlin it looks like a property but it's a Java setter.
            every { anyConstructed<SyncPreferences>().lastSuccessfulCloudBackupTimestamp = any() } returns Unit

            // Act
            val result = worker.doWork()

            // Assert
            assertThat(result, `is`(ListenableWorker.Result.success()))
            verify { anyConstructed<CloudTransfer>().backup("backup data") }
            verify { anyConstructed<SyncPreferences>().lastSuccessfulCloudBackupTimestamp = match { it != "N/A" } }
        }

    @Test
    fun doWorkReturnsRetryWhenNoInternet() =
        runBlocking {
            // Arrange
            val worker = AutoCloudBackup(context, workerParameters)
            every { anyConstructed<CloudTransfer>().isInternetAvailable() } returns false

            // Act
            val result = worker.doWork()

            // Assert
            assertThat(result, `is`(ListenableWorker.Result.retry()))
        }

    @Test
    fun doWorkReturnsSuccessButMarksNAWhenBackupDataIsTooLarge() =
        runBlocking {
            // Arrange
            val widgetId = 1
            every { workerParameters.inputData } returns Data.Builder().putInt(AutoCloudBackup.KEY_WIDGET_ID, widgetId).build()
            val worker = AutoCloudBackup(context, workerParameters)

            every { anyConstructed<CloudTransfer>().isInternetAvailable() } returns true

            // Create string larger than 1048500 bytes
            val largeData = "a".repeat(1048501)
            every { anyConstructed<QuoteUnquoteModel>().transferBackup(any()) } returns largeData

            every { anyConstructed<SyncPreferences>().lastSuccessfulCloudBackupTimestamp = "N/A" } returns Unit

            // Act
            val result = worker.doWork()

            // Assert
            assertThat(result, `is`(ListenableWorker.Result.success()))
            verify(exactly = 0) { anyConstructed<CloudTransfer>().backup(any()) }
            verify { anyConstructed<SyncPreferences>().lastSuccessfulCloudBackupTimestamp = "N/A" }
        }

    @Test
    fun doWorkReturnsRetryWhenExceptionOccurs() =
        runBlocking {
            // Arrange
            val worker = AutoCloudBackup(context, workerParameters)
            every { anyConstructed<CloudTransfer>().isInternetAvailable() } throws RuntimeException("Unexpected error")

            // Act
            val result = worker.doWork()

            // Assert
            assertThat(result, `is`(ListenableWorker.Result.retry()))
        }
}
