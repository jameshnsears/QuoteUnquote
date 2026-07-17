package com.github.jameshnsears.quoteunquote.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.github.jameshnsears.quoteunquote.db.h.HistoryDatabase;
import com.github.jameshnsears.quoteunquote.db.h.HistoryExternalDatabase;
import com.github.jameshnsears.quoteunquote.db.q.ExternalDatabase;
import com.github.jameshnsears.quoteunquote.db.q.QuotationDatabase;
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity;

import java.util.LinkedHashSet;
import java.util.List;

public final class DatabaseRepositoryDouble extends DatabaseRepository {
    private static DatabaseRepositoryDouble databaseRepositoryDouble;

    private DatabaseRepositoryDouble(@NonNull final Context context) {
        super(context);
        createQuotationsDatabaseInternal();
        createHistoryDatabaseInternal();

        createQuotationsDatabaseExternal();
        createHistoryDatabaseExternal();
    }

    public static synchronized DatabaseRepositoryDouble getInstance(@NonNull Context context) {
        if (databaseRepositoryDouble == null) {
            databaseRepositoryDouble = new DatabaseRepositoryDouble(context);
            DatabaseRepository.databaseRepository = databaseRepositoryDouble;
        }

        return databaseRepositoryDouble;
    }

    private void createHistoryDatabaseExternal() {
        historyExternalDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        HistoryExternalDatabase.class)
                .allowMainThreadQueries()
                .build();
        previousExternalDAO = historyExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = historyExternalDatabase.favouritesExternalDAO();
        currentExternalDAO = historyExternalDatabase.currentExternalDAO();
    }

    private void createHistoryDatabaseInternal() {
        historyDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        HistoryDatabase.class)
                .allowMainThreadQueries()
                .build();
        previousDAO = historyDatabase.previousDAO();
        favouriteDAO = historyDatabase.favouritesDAO();
        currentDAO = historyDatabase.currentDAO();
    }

    private void createQuotationsDatabaseExternal() {
        externalDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        ExternalDatabase.class)
                .allowMainThreadQueries()
                .build();
        quotationExternalDAO = externalDatabase.quotationExternalDAO();
    }

    public void createQuotationsDatabaseInternal() {
        quotationDatabase = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        QuotationDatabase.class)
                .allowMainThreadQueries()
                .build();
        quotationDAO = quotationDatabase.quotationDAO();
    }

    public void useInternalDatabaseFromAsset() {
        if (quotationDatabase != null) {
            quotationDatabase.close();
        }
        quotationDatabase = QuotationDatabase.getDatabase(ApplicationProvider.getApplicationContext());
        quotationDAO = quotationDatabase.quotationDAO();
    }

    public void eraseAllDatabases() {
        clearCache(true);
        clearCache(false);

        quotationDAO.eraseQuotations();
        previousDAO.erase();
        currentDAO.erase();
        favouriteDAO.erase();

        quotationExternalDAO.eraseQuotations();
        previousExternalDAO.erase();
        currentExternalDAO.erase();
        favouriteExternalDAO.erase();
    }

    public int countCurrent(boolean useInternalDatabase, int widgetId) {
        if (useInternalDatabase) {
            return currentDAO.countCurrent(widgetId);
        } else {
            return currentExternalDAO.countCurrent(widgetId);
        }
    }

    public void insertQuotations(
            boolean useInternalDatabase,
            @NonNull final List<QuotationEntity> quotationEntityList) {
        clearCache(useInternalDatabase);
        for (final QuotationEntity quotationEntity : quotationEntityList) {

            if (useInternalDatabase) {
                quotationDAO.insertQuotation(quotationEntity);
            } else {
                quotationExternalDAO.insertQuotation(quotationEntity);
            }
        }
    }

    public void eraseQuotation(boolean useInternalDatabase, @NonNull String digest) {
        clearCache(useInternalDatabase);
        if (useInternalDatabase) {
            quotationDAO.eraseQuotations(digest);
        } else {
            quotationExternalDAO.eraseQuotations(digest);
        }
    }

    public LinkedHashSet<String> getNextAllDigests(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return new LinkedHashSet<>(quotationDAO.getNextAllDigests());
        }

        return new LinkedHashSet<>(quotationExternalDAO.getNextAllDigests());
    }
}
