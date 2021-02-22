package com.github.jameshnsears.quoteunquote.cloud

import android.app.Service.START_NOT_STICKY
import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentViewModelDouble
import com.github.jameshnsears.quoteunquote.database.DatabaseTestHelper
import io.mockk.every
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test


class CloudServiceSendTest: DatabaseTestHelper() {
    @Rule
    @JvmField
    val serviceTestRule = ServiceTestRule()

    @Test
    fun startService() {
        val cloudServiceSend = spyk(CloudServiceSend())

        val cloudFavourites = spyk(CloudFavourites())
        every { cloudFavourites.save(any())} returns true

        every { cloudServiceSend.getCloudFavourites() } returns cloudFavourites



        assertFalse(CloudServiceSend.isRunning(getApplicationContext()))

        insertQuotationsTestData01()
        markDefaultQuotationAsFavourite()

        val serviceIntent = Intent(getApplicationContext(), CloudServiceSend::class.java)
        val contentViewModel = ContentViewModelDouble()
        serviceIntent.putExtra("savePayload", contentViewModel.favouritesToSend)
        serviceIntent.putExtra("localCodeValue", "0123456789")

        serviceTestRule.startService(Intent(InstrumentationRegistry.getInstrumentation().targetContext,
                CloudServiceSend::class.java))

/*
        IBinder binder = mServiceRule.
                bindService(new Intent(InstrumentationRegistry.getTargetContext(),
                        MyService.class));
        MyService service = ((MyService.LocalBinder) binder).getService();
 */
    }
}
