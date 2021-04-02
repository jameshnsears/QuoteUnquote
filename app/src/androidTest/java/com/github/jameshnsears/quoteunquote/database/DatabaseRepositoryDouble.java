package com.github.jameshnsears.quoteunquote.database;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

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
        favouriteDAO = abstractHistoryDatabase.favouritesDAO();
        reportedDAO = abstractHistoryDatabase.reportedDAO();
        currentDAO = abstractHistoryDatabase.currentDAO();
    }

    public static synchronized DatabaseRepositoryDouble getInstance() {
        if (databaseRepositoryDouble == null) {
            databaseRepositoryDouble = new DatabaseRepositoryDouble();
        }

        return databaseRepositoryDouble;
    }

    @Override
    public void erase() {
        databaseRepositoryDouble.abstractQuotationDatabase.quotationsDAO().erase();
        super.erase();
    }

    public void insertQuotations(@NonNull final List<QuotationEntity> quotationEntityList) {
        for (final QuotationEntity quotationEntity : quotationEntityList) {
            quotationDAO.insertQuotation(quotationEntity);
        }
    }

    public int countReported() {
        return reportedDAO.countReported();
    }

    public int countCurrent(final int widgetId) {
        return currentDAO.countCurrent(widgetId);
    }
}
