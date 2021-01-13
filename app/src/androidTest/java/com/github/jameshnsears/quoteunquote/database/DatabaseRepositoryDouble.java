package com.github.jameshnsears.quoteunquote.database;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.github.jameshnsears.quoteunquote.database.history.AbstractDatabaseHistory;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractDatabaseQuotation;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

import java.util.List;

import io.reactivex.Single;

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

        previousDAO = abstractDatabaseHistory.contentDAO();
        favouriteDAO = abstractDatabaseHistory.favouritesDAO();
        reportedDAO = abstractDatabaseHistory.reportedDAO();
    }

    public static synchronized DatabaseRepositoryDouble getInstance() {
        if (databaseRepositoryDouble == null) {
            databaseRepositoryDouble = new DatabaseRepositoryDouble();
        }

        return databaseRepositoryDouble;
    }

    public void insertQuotations(final List<QuotationEntity> quotationEntityList) {
        for (final QuotationEntity quotationEntity : quotationEntityList) {
            quotationDAO.insertQuotation(quotationEntity);
        }
    }

    @NonNull
    public Single<Integer> countAll() {
        return quotationDAO.countAll();
    }

    @NonNull
    public Integer countReported() {
        return reportedDAO.countReported();
    }

    @NonNull
    public QuotationEntity getQuotation(@NonNull final String digest) {
        return quotationDAO.getQuotation(digest);
    }
}
