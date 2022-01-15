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
        this.abstractQuotationDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractQuotationDatabase.class)
                .allowMainThreadQueries()
                .build();

        this.quotationDAO = this.abstractQuotationDatabase.quotationsDAO();

        this.abstractHistoryDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractHistoryDatabase.class)
                .allowMainThreadQueries()
                .build();

        this.previousDAO = this.abstractHistoryDatabase.previousDAO();
        this.favouriteDAO = this.abstractHistoryDatabase.favouritesDAO();
        this.reportedDAO = this.abstractHistoryDatabase.reportedDAO();
        this.currentDAO = this.abstractHistoryDatabase.currentDAO();
    }

    public static synchronized DatabaseRepositoryDouble getInstance() {
        if (DatabaseRepositoryDouble.databaseRepositoryDouble == null) {
            DatabaseRepositoryDouble.databaseRepositoryDouble = new DatabaseRepositoryDouble();
        }

        return DatabaseRepositoryDouble.databaseRepositoryDouble;
    }

    @Override
    public void erase() {
        DatabaseRepositoryDouble.databaseRepositoryDouble.abstractQuotationDatabase.quotationsDAO().erase();
        super.erase();
    }

    public void insertQuotations(@NonNull List<QuotationEntity> quotationEntityList) {
        for (QuotationEntity quotationEntity : quotationEntityList) {
            this.quotationDAO.insertQuotation(quotationEntity);
        }
    }

    public int countReported() {
        return this.reportedDAO.countReported();
    }

    public int countCurrent(int widgetId) {
        return this.currentDAO.countCurrent(widgetId);
    }

    public List<String> getNextAllDigests() {
        return this.quotationDAO.getNextAllDigests();
    }
}
