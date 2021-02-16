package com.github.jameshnsears.quoteunquote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class WidgetDeletedTest : QuoteUnquoteModelUtility() {
    @Test
    fun twoWidgetsOneDeleted() {
        insertQuotationsTestData01()
        insertQuotationsTestData02()
        quoteUnquoteModelDouble.setDefault(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        quoteUnquoteModelDouble.toggleFavourite(
                WidgetIdHelper.INSTANCE_01_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL)!!.digest)
        quoteUnquoteModelDouble.reportQuotation(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        quoteUnquoteModelDouble.setDefault(WidgetIdHelper.INSTANCE_02_WIDGET_ID)
        val quoteUnquoteModelSpy = Mockito.spy(quoteUnquoteModelDouble)
        Mockito.doReturn(false).`when`(quoteUnquoteModelSpy).selectedContentTypeIsFavourite(ArgumentMatchers.eq(WidgetIdHelper.INSTANCE_02_WIDGET_ID))
        quoteUnquoteModelSpy.toggleFavourite(
                WidgetIdHelper.INSTANCE_02_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL)!!.digest)
        quoteUnquoteModelDouble.reportQuotation(WidgetIdHelper.INSTANCE_02_WIDGET_ID)
        Assert.assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(
                        WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).toLong())
        Assert.assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countPrevious(
                        WidgetIdHelper.INSTANCE_02_WIDGET_ID, ContentSelection.ALL).toLong())
        Assert.assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countFavourites().toLong())
        Assert.assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countReported().toLong())
        quoteUnquoteModelSpy.toggleFavourite(
                WidgetIdHelper.INSTANCE_02_WIDGET_ID,
                quoteUnquoteModelDouble.getNext(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL)!!.digest)
        Assert.assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countFavourites().toLong())

        ////////////////////////////////////////////
        quoteUnquoteModelDouble.delete(WidgetIdHelper.INSTANCE_01_WIDGET_ID)
        Assert.assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_01_WIDGET_ID, ContentSelection.ALL).toLong())
        Assert.assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countFavourites().toLong())
        Assert.assertEquals(
                "",
                1,
                quoteUnquoteModelDouble.countReported().toLong())

        ////////////////////////////////////////////
        quoteUnquoteModelDouble.disable()
        Assert.assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countPrevious(WidgetIdHelper.INSTANCE_02_WIDGET_ID, ContentSelection.ALL).toLong())
        Assert.assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countFavourites().toLong())
        Assert.assertEquals(
                "",
                0,
                quoteUnquoteModelDouble.countReported().toLong())
    }
}