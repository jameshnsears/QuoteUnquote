package com.github.jameshnsears.quoteunquote.cloud

import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CloudFavouritesTest {
    lateinit var cloudFavourites: CloudFavourites
    lateinit var gson: Gson

    @Before
    fun setUp() {
        cloudFavourites = CloudFavourites()
        gson = Gson()
    }

    @After
    fun shutdown() {
        cloudFavourites.shutdown()
    }

    @Test
    fun `internet is available`() {
        assertTrue("", cloudFavourites.isInternetAvailable)
    }

    @Test
    fun `00 save known code`() {
        assertTrue("", cloudFavourites.save(Gson().toJson(RequestTestHelper.sendRequest())))
    }

    @Test
    fun `01 receive unknown code`() {
        val requestReceive = RequestReceive()
        requestReceive.code = "dcb9pNXX9e"

        val digests = cloudFavourites.receive(CloudFavourites.TIMEOUT_SECONDS, gson.toJson(requestReceive))

        assertEquals("", 0, digests.size.toLong())
    }

    @Test
    fun `02 receive known code`() {
        val actual = cloudFavourites.receive(30, Gson().toJson(RequestTestHelper.receiveRequest()))
        assertEquals("", 2, actual.size.toLong())

        val expected = ArrayList<String>()
        expected.add("d0")
        expected.add("d1")

        assertEquals("", expected, actual)
    }
}
