package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.history.external.AbstractHistoryExternalDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.database.quotation.external.AbstractQuotationExternalDatabase;

import java.util.List;

public final class DatabaseRepositoryDouble extends DatabaseRepository {
    private static DatabaseRepositoryDouble databaseRepositoryDouble;

    private DatabaseRepositoryDouble() {
        createQuotationsDatabaseInternal();
        createHistoryDatabaseInternal();

        createQuotationsDatabaseExternal();
        createHistoryDatabaseExternal();
    }

    private void createHistoryDatabaseExternal() {
        abstractHistoryExternalDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AbstractHistoryExternalDatabase.class)
                .allowMainThreadQueries()
                .build();
        previousExternalDAO = abstractHistoryExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = abstractHistoryExternalDatabase.favouritesExternalDAO();
        reportedExternalDAO = abstractHistoryExternalDatabase.reportedExternalDAO();
        currentExternalDAO = abstractHistoryExternalDatabase.currentExternalDAO();
    }

    private void createHistoryDatabaseInternal() {
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

    private void createQuotationsDatabaseExternal() {
        abstractQuotationExternalDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AbstractQuotationExternalDatabase.class)
                .allowMainThreadQueries()
                .build();
        quotationExternalDAO = abstractQuotationExternalDatabase.quotationExternalDAO();
    }

    private void createQuotationsDatabaseInternal() {
        abstractQuotationDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AbstractQuotationDatabase.class)
                .allowMainThreadQueries()
                .build();
        quotationDAO = abstractQuotationDatabase.quotationDAO();
    }

    public static synchronized DatabaseRepositoryDouble getInstance(@NonNull Context context) {
        if (databaseRepositoryDouble == null) {
            databaseRepositoryDouble = new DatabaseRepositoryDouble();
        }

        return databaseRepositoryDouble;
    }

    public void eraseAllDatabsaes() {
        databaseRepositoryDouble.abstractQuotationDatabase.quotationDAO().erase();
        previousDAO.erase();
        currentDAO.erase();
        favouriteDAO.erase();
        reportedDAO.erase();

        databaseRepositoryDouble.abstractQuotationExternalDatabase.quotationExternalDAO().erase();
        previousExternalDAO.erase();
        currentExternalDAO.erase();
        favouriteExternalDAO.erase();
        reportedExternalDAO.erase();
    }

    public int countCurrent(int widgetId) {
        if (useInternalDatabase) {
            return currentDAO.countCurrent(widgetId);
        } else {
            return currentExternalDAO.countCurrent(widgetId);
        }
    }

    public List<String> getNextAllDigests() {
        if (useInternalDatabase) {
            return quotationDAO.getNextAllDigests();
        } else {
            return quotationExternalDAO.getNextAllDigests();
        }
    }

    public void insertQuotations(
            @NonNull final List<QuotationEntity> quotationEntityList) {
        for (final QuotationEntity quotationEntity : quotationEntityList) {

            if (useInternalDatabase) {
                quotationDAO.insertQuotation(quotationEntity);
            } else {
                quotationExternalDAO.insertQuotation(quotationEntity);
            }
        }
    }
}
