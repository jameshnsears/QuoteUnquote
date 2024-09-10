package com.github.jameshnsears.quoteunquote.scraper

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class Scraper {
    fun getDocumentFromResources(resource: String): Document {
        val html = this.javaClass.getResource(resource).readText()
        return Jsoup.parse(html)
    }

    @Throws(ScraperUrlException::class)
    fun getDocumentFromUrl(url: String = "https://www.bible.com/verse-of-the-day/"): Document {
        try {
            val response = OkHttpClient().newCall(requestBuilder(url)).execute()
            val html = response.body?.string()
            return Jsoup.parse(html!!)
        } catch (e: Exception) {
            Timber.e("scraper: Exception=%s", e.message)
            throw ScraperUrlException(e.message)
        }
    }

    fun requestBuilder(url: String): Request {
        // https://www.baeldung.com/okhttp-timeouts
        return Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/110.0",
            )
            .header("Accept-Language", "en-US,en;q=0.5")
            .build()
    }

    @Throws(ScraperQuotationException::class)
    fun getQuotation(
        document: Document,
        xpath: String = "//*[@id=\"votd_wrapper\"]/div/div/div[1]/div[2]/p[1]",
    ): String {
        try {
            val quotation = getXpath(document, xpath)
            return quotation
        } catch (e: ScraperXpathException) {
            throw ScraperQuotationException(e.message)
        }
    }

    @Throws(ScraperSourceException::class)
    fun getSource(
        document: Document,
        xpath: String = "//*[@id=\"votd_wrapper\"]/div/div/div[1]/div[2]/p[2]",
    ): String {
        try {
            val source = getXpath(document, xpath)
            return source
        } catch (e: ScraperXpathException) {
            throw ScraperSourceException(e.message)
        }
    }

    private fun getXpath(document: Document, xpath: String): String {
        try {
            val xpathResult = document.selectXpath(xpath).text()

            if (xpathResult == "") throw ScraperXpathException("")

            val maxLength = 1000
            if (xpathResult.length >= maxLength) throw ScraperXpathException("length")

            return xpathResult
        } catch (e: Exception) {
            throw ScraperXpathException(e.message)
        }
    }
}
