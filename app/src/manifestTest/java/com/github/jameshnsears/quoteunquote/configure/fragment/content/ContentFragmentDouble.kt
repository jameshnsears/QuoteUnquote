package com.github.jameshnsears.quoteunquote.configure.fragment.content

import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single

class ContentFragmentDouble(widgetId: Int = WidgetIdHelper.INSTANCE_01_WIDGET_ID) : ContentFragment(widgetId) {
    override fun getContentViewModel(): ContentViewModel {
        val contentViewModel: ContentViewModel = mockk()
        every { contentViewModel.countAll() } returns Single.just(7)

        val authors = listOf(AuthorPOJO("a1"))
        every { contentViewModel.authors() } returns Single.just(authors)
        every { contentViewModel.authorsSorted(authors) } returns listOf("a1")

        every { contentViewModel.authorsIndex(any()) } returns 0
        every { contentViewModel.countAuthorQuotations(any()) } returns 1
        every { contentViewModel.countFavourites() } returns Single.just(0)

        return contentViewModel
    }

    companion object {
        fun newInstance(widgetId: Int): ContentFragmentDouble {
            val fragment = ContentFragmentDouble(widgetId)
            fragment.arguments = null
            return fragment
        }
    }
}
