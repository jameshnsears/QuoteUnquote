package com.github.jameshnsears.quoteunquote.utils.scraper

import com.github.jameshnsears.quoteunquote.scraper.Scraper
import com.github.jameshnsears.quoteunquote.scraper.ScraperQuotationException
import com.github.jameshnsears.quoteunquote.scraper.ScraperSourceException
import com.github.jameshnsears.quoteunquote.scraper.ScraperUrlException
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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

        // 20230316 - UI changed
        val document3 =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/3/Verse of the Day - 1 Peter 5_10 (KJV) _ The Bible App _ Bible.com.xml")
        assertEquals(
            "1 Peter 5:10 KJV",
            scraper.getQuotation(
                document3,
                xpath = "//*[@id=\"react-app-VOTD\"]/div/div[2]/div[1]/div[1]/div/div/div/div[1]/div[2]/div/div/h3",
            ),
        )
        // PROBLEM, the result is split into two nodes!
        assertEquals(
            "But the God of all grace, who hath called us unto his eternal glory by Christ Jesus, after that ye have suffered a while, make you perfect, stablish, strengthen, settle",
            scraper.getSource(
                document3,
                xpath = "//*[@id=\"react-app-VOTD\"]/div/div[2]/div[1]/div[1]/div/div/div/div[2]/div/div/span/span[2]",
            ),
        )
        assertEquals(
            "you",
            scraper.getSource(
                document3,
                xpath = "//*[@id=\"react-app-VOTD\"]/div/div[2]/div[1]/div[1]/div/div/div/div[2]/div/div/span/span[3]/span",
            ),
        )
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

    @Test(expected = ScraperSourceException::class)
    fun `xml - html element not supported by jsoup`() {
        // curl https://ghq1fjr4d3.execute-api.eu-north-1.amazonaws.com/default/myQuotes -o myQuotes.txt
        val document =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/xml/myQuotes.txt")

        assertEquals(
            "Source1",
            scraper.getSource(
                document,
                "/html/body/root/quotes/quote/source",
            ),
        )
    }

    @Test
    fun `xml - with renamed element`() {
        val document =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/xml/myQuotes-fixed.txt")

        assertEquals(
            "Source1",
            scraper.getSource(
                document,
                "/html/body/root/quotes/quote/s",
            ),
        )
        assertEquals(
            "Source1",
            scraper.getSource(
                document,
                "//s",
            ),
        )

        assertEquals(
            "Quotation1",
            scraper.getSource(
                document,
                "/html/body/root/quotes/quote/text",
            ),
        )
        assertEquals(
            "Quotation1",
            scraper.getSource(
                document,
                "//text",
            ),
        )
    }

    @Test
    fun buatmudah() {
        val document =
            scraper.getDocumentFromResources("/ExternalDatabaseWeb/buatmudah/quote.xml")

        assertThat(
            scraper.getSource(
                document,
                "//quote/text",
            ),
            `is`("Isi quote"),
        )

        // had to replace "source" tag with "s"
        assertThat(
            scraper.getSource(
                document,
                "//quote/s",
            ),
            `is`("sumber"),
        )
    }
}
