package com.github.jameshnsears.quoteunquote.database;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.github.jameshnsears.quoteunquote.database.history.AbstractDatabaseHistory;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractDatabaseQuotation;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

import java.util.List;


public final class DatabaseRepositoryDouble extends DatabaseRepository {
    private static DatabaseRepositoryDouble databaseRepositoryDouble;

    private DatabaseRepositoryDouble() {
        abstractDatabaseQuotation = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractDatabaseQuotation.class)
                .allowMainThreadQueries()
                .build();

        quotationDAO = abstractDatabaseQuotation.quotationsDAO();

        abstractDatabaseHistory = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractDatabaseHistory.class)
                .allowMainThreadQueries()
                .build();

        previousDAO = abstractDatabaseHistory.previousDAO();
        favouritesDAO = abstractDatabaseHistory.favouritesDAO();
        reportedDAO = abstractDatabaseHistory.reportedDAO();
    }

    public static synchronized DatabaseRepositoryDouble getInstance() {
        if (databaseRepositoryDouble == null) {
            databaseRepositoryDouble = new DatabaseRepositoryDouble();
        }

        return databaseRepositoryDouble;
    }

    public void empty() {
        databaseRepositoryDouble.abstractDatabaseQuotation.quotationsDAO().deleteAll();

        databaseRepositoryDouble.abstractDatabaseHistory.previousDAO().deleteAll();
        databaseRepositoryDouble.abstractDatabaseHistory.favouritesDAO().deleteAll();
        databaseRepositoryDouble.abstractDatabaseHistory.reportedDAO().deleteAll();
    }

    public void insertQuotations(final List<QuotationEntity> quotationEntityList) {
        for (final QuotationEntity quotationEntity : quotationEntityList) {
            quotationDAO.insertQuotation(quotationEntity);
        }
    }

    @NonNull
    public Integer countReported() {
        return reportedDAO.countReported();
    }
}
