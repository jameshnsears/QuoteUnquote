package com.github.jameshnsears.quoteunquote.cloud

import android.content.Context
import android.os.Handler
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import io.mockk.every
import io.mockk.mockk

class CloudServiceReceiveDouble : CloudServiceReceive() {
    override fun getCloudFavourites(): CloudFavourites {
        val cloudFavourites = mockk<CloudFavourites>()
        every { cloudFavourites.isInternetAvailable } returns true

        val receiveResponse = ReceiveResponse()
        receiveResponse.digests = listOf("d1", "d2")
        every { cloudFavourites.receive(any(), any()) } returns receiveResponse

        return cloudFavourites
    }

    override fun getHandler(): Handler {
        val handler = mockk<Handler>()
        every { handler.post(any()) } returns true
        return handler
    }

    override fun getDatabaseRepository(context: Context): DatabaseRepository {
        val databaseRepository = mockk<DatabaseRepository>()
        every { databaseRepository.markAsFavourite(any()) } returns Unit
        return databaseRepository
    }
}
