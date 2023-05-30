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

import java.util.LinkedHashSet;
import java.util.List;

public final class DatabaseRepositoryDouble extends DatabaseRepository {
    private static DatabaseRepositoryDouble databaseRepositoryDouble;

    private DatabaseRepositoryDouble(@NonNull final Context context) {
        createQuotationsDatabaseInternal();
        createHistoryDatabaseInternal();

        createQuotationsDatabaseExternal();
        createHistoryDatabaseExternal();

        this.context = context;
    }

    private void createHistoryDatabaseExternal() {
        abstractHistoryExternalDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AbstractHistoryExternalDatabase.class)
                .allowMainThreadQueries()
                .build();
        previousExternalDAO = abstractHistoryExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = abstractHistoryExternalDatabase.favouritesExternalDAO();
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
            databaseRepositoryDouble = new DatabaseRepositoryDouble(context);
        }

        return databaseRepositoryDouble;
    }

    public void eraseAllDatabsaes() {
        databaseRepositoryDouble.abstractQuotationDatabase.quotationDAO().eraseQuotations();
        previousDAO.erase();
        currentDAO.erase();
        favouriteDAO.erase();

        databaseRepositoryDouble.abstractQuotationExternalDatabase.quotationExternalDAO().eraseQuotations();
        previousExternalDAO.erase();
        currentExternalDAO.erase();
        favouriteExternalDAO.erase();
    }

    public int countCurrent(int widgetId) {
        if (useInternalDatabase()) {
            return currentDAO.countCurrent(widgetId);
        } else {
            return currentExternalDAO.countCurrent(widgetId);
        }
    }

    public void insertQuotations(
            @NonNull final List<QuotationEntity> quotationEntityList) {
        for (final QuotationEntity quotationEntity : quotationEntityList) {

            if (useInternalDatabase()) {
                quotationDAO.insertQuotation(quotationEntity);
            } else {
                quotationExternalDAO.insertQuotation(quotationEntity);
            }
        }
    }

    public void eraseQuotation(@NonNull String digest) {
        if (useInternalDatabase()) {
            quotationDAO.eraseQuotations(digest);
        } else {
            quotationExternalDAO.eraseQuotations(digest);
        }
    }

    public LinkedHashSet<String> getNextAllDigests() {
        if (useInternalDatabase()) {
            return new LinkedHashSet<>(quotationDAO.getNextAllDigests());
        }

        return new LinkedHashSet<>(quotationExternalDAO.getNextAllDigests());
    }
}
