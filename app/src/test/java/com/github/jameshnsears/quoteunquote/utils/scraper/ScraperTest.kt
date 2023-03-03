package com.github.jameshnsears.quoteunquote.utils.scraper

import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ScraperTest {
    private val scraper = Scraper()

    // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test
    @Test
    fun `valid parameters`() = runTest {
        val document1 =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/1/Bible Verse of the Day _ YouVersion.xml")

        assertEquals(
            "As the hart panteth after the water brooks, So panteth my soul after thee, O God.",
            scraper.getQuotation(document1),
        )

        assertEquals("Psalm 42:1 (KJV)", scraper.getSource(document1))

        val document2 =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/2/Bible Verse of the Day _ YouVersion.xml")

        assertEquals(
            "Behold, I send you forth as sheep in the midst of wolves: be ye therefore wise as serpents, and harmless as doves.",
            scraper.getQuotation(document2),
        )

        assertEquals("Matthew 10:16 (KJV)", scraper.getSource(document2))
    }

    @Test(expected = ScraperUrlException::class)
    fun `Unable to contact URL - empty`() {
        scraper.getDocumentFromUrl("")
    }

    @Test(expected = ScraperUrlException::class)
    fun `Unable to contact URL - Expected URL scheme`() {
        scraper.getDocumentFromUrl("hoot://pop.com")
    }

    @Test(expected = ScraperUrlException::class)
    fun `Unable to contact URL - Name or service not known`() {
        scraper.getDocumentFromUrl("https://bible.zom")
    }

    @Test(expected = ScraperUrlException::class)
    fun `Unable to contact URL - Timeout`() {
        val scraperSpy = spyk(Scraper())
        every { scraperSpy.requestBuilder(any()) } throws java.net.SocketTimeoutException("Timeout")

        scraperSpy.getDocumentFromUrl()
    }

    @Test(expected = ScraperUrlException::class)
    fun `Unable to contact URL - Interrupted`() {
        val scraperSpy = spyk(Scraper())
        every { scraperSpy.requestBuilder(any()) } throws java.net.SocketTimeoutException("Interrupted")

        scraperSpy.getDocumentFromUrl()
    }

    @Test(expected = ScraperQuotationException::class)
    fun `invalid Xpaths - ScraperQuotationException`() {
        val document =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/1/Bible Verse of the Day _ YouVersion.xml")

        scraper.getQuotation(document, "/")
    }

    @Test(expected = ScraperSourceException::class)
    fun `invalid Xpaths - ScraperSourceException`() {
        val document =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/1/Bible Verse of the Day _ YouVersion.xml")

        scraper.getSource(document, "/1")
    }
}
