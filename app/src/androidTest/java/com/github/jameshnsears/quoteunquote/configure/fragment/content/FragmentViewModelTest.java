package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModelFake;
import com.github.jameshnsears.quoteunquote.DatabaseTestHelper;
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.utils.ContentType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FragmentViewModelTest extends DatabaseTestHelper {

    private FragmentViewModel fragmentViewModel;

    @Before
    public void setUpDatabases() {
        ApplicationProvider.getApplicationContext().deleteDatabase(AbstractHistoryDatabase.DATABASE_NAME);

        // insert's against in memory database defined in QuoteUnquoteModelFake
        quoteUnquoteModel = new QuoteUnquoteModelFake();
        fragmentViewModel = new FragmentViewModelFake(quoteUnquoteModel.getDatabaseRepositoryFake());
    }

    @After
    public void shutdown() {
        fragmentViewModel.shutdown();
    }

    @Test
    public void countAll() {
        insertTestDataSet01();
        assertEquals(
                "",
                2,
                fragmentViewModel.countAll().blockingGet().intValue());

        insertTestDataSet02();
        assertEquals(
                "",
                5,
                fragmentViewModel.countAll().blockingGet().intValue());
    }

    @Test
    public void authors() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        assertEquals(
                "",
                5,
                fragmentViewModel.authors().blockingGet().size());
    }

    @Test
    public void authorsSorted() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        assertEquals(
                "",
                "a0",
                fragmentViewModel.authorsSorted(fragmentViewModel.authors().blockingGet()).get(0));

        assertEquals(
                "",
                "a5",
                fragmentViewModel.authorsSorted(fragmentViewModel.authors().blockingGet()).get(4));
    }

    @Test
    public void countAuthorQuotations() {
        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        fragmentViewModel.authorPOJOList = fragmentViewModel.authors().blockingGet();

        assertEquals(
                "",
                3,
                fragmentViewModel.countAuthorQuotations("a2"));
    }

    @Test
    public void countQuotationWithText() {
        assertEquals("", 0, fragmentViewModel.countQuotationWithText("q1").intValue());

        insertTestDataSet01();
        insertTestDataSet02();
        insertTestDataSet03();

        assertEquals("", 4, fragmentViewModel.countQuotationWithText("q1").intValue());

    }

    @Test
    public void countFavouriteQuotations() {
        assertEquals("", 0, fragmentViewModel.countFavourites().blockingGet().intValue());
    }

    @Test
    public void savePayload() throws NoNextQuotationAvailableException {
        insertTestDataSet01();
        insertTestDataSet02();

        // 1624c314
        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        // d1
        quoteUnquoteModel.setNext(widgetID, ContentType.ALL);
        quoteUnquoteModel.toggleFavourite(
                widgetID,
                quoteUnquoteModel.getNext(widgetID, ContentType.ALL).digest);

        assertTrue("", fragmentViewModel.getSavePayload().contains("\"digests\":[\"d1\",\"1624c314\"]"));
    }
}
