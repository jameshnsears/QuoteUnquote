package com.github.jameshnsears.quoteunquote.cloud

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer
import com.github.jameshnsears.quoteunquote.cloud.transfer.TransferRestoreResponse
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.restore.TransferRestore
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncFragment
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncPreferences
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.S])
class CloudServiceRestoreTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        CloudService.stopRunning()
        mockkConstructor(CloudTransfer::class)
        mockkConstructor(TransferRestore::class)
        mockkConstructor(SyncPreferences::class)
        mockkStatic(DatabaseRepository::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
        CloudService.stopRunning()
    }

    private fun waitForBroadcast(latch: CountDownLatch) {
        val timeout = System.currentTimeMillis() + 5000
        while (latch.count > 0 && System.currentTimeMillis() < timeout) {
            ShadowLooper.idleMainLooper()
            Thread.sleep(100)
        }
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun onStartCommandRestoreSuccess() {
        val widgetId = 1
        val remoteCodeValue = "validCode"
        val intent =
            Intent(context, CloudServiceRestore::class.java).apply {
                putExtra("widgetId", widgetId)
                putExtra("remoteCodeValue", remoteCodeValue)
            }

        every { anyConstructed<CloudTransfer>().isInternetAvailable } returns true

        val mockTransfer = mockk<Transfer>(relaxed = true)
        val mockResponse =
            mockk<TransferRestoreResponse>(relaxed = true) {
                every { reason } returns ""
                every { transfer } returns mockTransfer
            }
        every { anyConstructed<CloudTransfer>().restore(any(), any()) } returns mockResponse

        val mockDatabaseRepository = mockk<DatabaseRepository>(relaxed = true)
        every { DatabaseRepository.getInstance(any()) } returns mockDatabaseRepository

        every { anyConstructed<TransferRestore>().requestJson(any()) } returns "{}"
        every { anyConstructed<TransferRestore>().restore(any(), any(), any()) } returns Unit

        // Mock SyncPreferences to verify interaction
        every { anyConstructed<SyncPreferences>().archiveGoogleCloud } returns true
        every { anyConstructed<SyncPreferences>().archiveSharedStorage } returns false
        every { anyConstructed<SyncPreferences>().archiveGoogleCloud = any() } just Runs
        every { anyConstructed<SyncPreferences>().archiveSharedStorage = any() } just Runs

        val latch = CountDownLatch(1)
        val disposable = CloudEventBus.getEvents().subscribe { event ->
            if (event == SyncFragment.CLOUD_SERVICE_COMPLETED) {
                latch.countDown()
            }
        }

        val serviceController = Robolectric.buildService(CloudServiceRestore::class.java, intent)
        val service = serviceController.create().get()
        service.onStartCommand(intent, 0, 1)

        waitForBroadcast(latch)
        assertThat("Timeout waiting for CloudService completed broadcast", latch.count, equalTo(0L))

        verify { anyConstructed<TransferRestore>().restore(any(), mockDatabaseRepository, mockTransfer) }
        verify { mockDatabaseRepository.alignHistoryWithQuotations(true, widgetId, any()) }
        verify { anyConstructed<SyncPreferences>().archiveGoogleCloud = true }
        verify { anyConstructed<SyncPreferences>().archiveSharedStorage = false }

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(context.getString(R.string.fragment_archive_restore_success)))
        assertThat(CloudService.isRunning(), equalTo(false))

        disposable.dispose()
    }

    @Test
    fun onStartCommandNoInternet() {
        val intent = Intent(context, CloudServiceRestore::class.java)
        every { anyConstructed<CloudTransfer>().isInternetAvailable } returns false

        val latch = CountDownLatch(1)
        val disposable = CloudEventBus.getEvents().subscribe { event ->
            if (event == SyncFragment.CLOUD_SERVICE_COMPLETED) {
                latch.countDown()
            }
        }

        val serviceController = Robolectric.buildService(CloudServiceRestore::class.java, intent)
        serviceController.create().get().onStartCommand(intent, 0, 1)

        waitForBroadcast(latch)
        assertThat(latch.count, equalTo(0L))

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(context.getString(R.string.fragment_archive_internet_missing)))
        assertThat(CloudService.isRunning(), equalTo(false))

        disposable.dispose()
    }

    @Test
    fun onStartCommandRestoreMissingCode() {
        val intent =
            Intent(context, CloudServiceRestore::class.java).apply {
                putExtra("widgetId", 1)
                putExtra("remoteCodeValue", "invalidCode")
            }

        every { anyConstructed<CloudTransfer>().isInternetAvailable } returns true

        val mockResponse =
            mockk<TransferRestoreResponse>(relaxed = true) {
                every { reason } returns "no JSON for code"
            }
        every { anyConstructed<CloudTransfer>().restore(any(), any()) } returns mockResponse
        every { anyConstructed<TransferRestore>().requestJson(any()) } returns "{}"

        val latch = CountDownLatch(1)
        val disposable = CloudEventBus.getEvents().subscribe { event ->
            if (event == SyncFragment.CLOUD_SERVICE_COMPLETED) {
                latch.countDown()
            }
        }

        val serviceController = Robolectric.buildService(CloudServiceRestore::class.java, intent)
        serviceController.create().get().onStartCommand(intent, 0, 1)

        waitForBroadcast(latch)
        assertThat(latch.count, equalTo(0L))

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(context.getString(R.string.fragment_archive_restore_missing_code)))
        assertThat(CloudService.isRunning(), equalTo(false))

        disposable.dispose()
    }

    @Test
    fun onStartCommandAlreadyRunning() {
        CloudService.startRunning()
        val intent = Intent(context, CloudServiceRestore::class.java)

        val serviceController = Robolectric.buildService(CloudServiceRestore::class.java, intent)
        val service = serviceController.create().get()

        val startResult = service.onStartCommand(intent, 0, 1)
        assertThat(startResult, equalTo(Service.START_NOT_STICKY))

        verify(exactly = 0) { anyConstructed<CloudTransfer>().isInternetAvailable }
    }
}
