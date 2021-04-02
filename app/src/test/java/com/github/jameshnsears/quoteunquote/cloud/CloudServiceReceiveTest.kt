package com.github.jameshnsears.quoteunquote.cloud

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentCloud
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CloudServiceReceiveTest {
    @Test
    fun startService() {
        val intent = Intent(getApplicationContext(), CloudServiceReceiveDouble::class.java)

        val service = Robolectric.setupService(CloudServiceReceiveDouble::class.java)
        val contentCloud = ContentCloud()
        service.bindService(intent, contentCloud.serviceConnection, Context.BIND_AUTO_CREATE)

        val contentFragment = mockk<ContentFragment>()
        every { contentFragment.setFavouriteCount() } returns Unit

        service.receive(contentFragment, "0123456789")
    }
}
