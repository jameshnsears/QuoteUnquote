package com.github.jameshnsears.quoteunquote.cloud

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import io.mockk.every
import io.mockk.spyk

class CloudServiceSendDouble : CloudServiceSend() {
    override fun getCloudFavourites(): CloudFavourites {
        val cloudFavourites = spyk(CloudFavourites())
        every { cloudFavourites.save(any()) } returns true
        return cloudFavourites
    }

    inner class LocalBinderDouble : Binder() {
        fun getService() = this@CloudServiceSendDouble
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinderDouble()
    }

    override fun getServiceContext(): Context? {
        return this@CloudServiceSendDouble.applicationContext
    }
}
