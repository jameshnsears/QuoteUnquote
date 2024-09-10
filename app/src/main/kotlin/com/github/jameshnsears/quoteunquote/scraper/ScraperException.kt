package com.github.jameshnsears.quoteunquote.scraper

open class ScraperException(message: String?) : java.lang.Exception(message)

class ScraperUrlException(message: String?) : ScraperException(message)

open class ScraperXpathException(message: String?) : ScraperException(message)

class ScraperQuotationException(message: String?) : ScraperXpathException(message)

class ScraperSourceException(message: String?) : ScraperXpathException(message)
