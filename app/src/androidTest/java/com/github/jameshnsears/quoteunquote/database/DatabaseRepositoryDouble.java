package com.github.jameshnsears.quoteunquote.database;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.util.List;

public final class DatabaseRepositoryDouble extends DatabaseRepository {
    private static DatabaseRepositoryDouble databaseRepositoryDouble;

    private DatabaseRepositoryDouble() {
        abstractQuotationDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractQuotationDatabase.class)
                .allowMainThreadQueries()
                .build();

        quotationDAO = abstractQuotationDatabase.quotationsDAO();

        abstractHistoryDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractHistoryDatabase.class)
                .allowMainThreadQueries()
                .build();

        previousDAO = abstractHistoryDatabase.previousDAO();
        favouritesDAO = abstractHistoryDatabase.favouritesDAO();
        reportedDAO = abstractHistoryDatabase.reportedDAO();
        currentDAO = abstractHistoryDatabase.currentDAO();
    }

    public static synchronized DatabaseRepositoryDouble getInstance() {
        if (databaseRepositoryDouble == null) {
            databaseRepositoryDouble = new DatabaseRepositoryDouble();
        }

        return databaseRepositoryDouble;
    }

    public void empty() {
        databaseRepositoryDouble.abstractQuotationDatabase.quotationsDAO().deleteAll();

        databaseRepositoryDouble.abstractHistoryDatabase.previousDAO().deleteAll();
        databaseRepositoryDouble.abstractHistoryDatabase.favouritesDAO().deleteAll();
        databaseRepositoryDouble.abstractHistoryDatabase.reportedDAO().deleteAll();
        databaseRepositoryDouble.abstractHistoryDatabase.currentDAO().deleteAll();
    }

    public void insertQuotations(final List<QuotationEntity> quotationEntityList) {
        for (final QuotationEntity quotationEntity : quotationEntityList) {
            quotationDAO.insertQuotation(quotationEntity);
        }
    }

    public int countReported() {
        return reportedDAO.countReported().intValue();
    }

    public int countCurrent(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return currentDAO.countCurrent(widgetId, contentSelection);
    }
}
