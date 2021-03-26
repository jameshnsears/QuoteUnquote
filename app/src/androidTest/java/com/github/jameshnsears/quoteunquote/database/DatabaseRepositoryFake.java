package com.github.jameshnsears.quoteunquote.database;

import android.util.Log;

import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import io.reactivex.Single;

public class DatabaseRepositoryFake extends DatabaseRepository {
    private static final String LOG_TAG = DatabaseRepositoryFake.class.getSimpleName();

    public DatabaseRepositoryFake() {
        super();

        abstractQuotationDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractQuotationDatabase.class)
                .build();

        quotationDAO = abstractQuotationDatabase.quotationsDAO();

        abstractHistoryDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AbstractHistoryDatabase.class)
                .build();

        previousDAO = abstractHistoryDatabase.contentDAO();
        favouriteDAO = abstractHistoryDatabase.favouritesDAO();
        reportedDAO = abstractHistoryDatabase.reportedDAO();
    }

    public void insertQuotations(final List<QuotationEntity> quotationEntityList) {
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (final QuotationEntity quotationEntity : quotationEntityList) {
            Log.d(LOG_TAG, "insertQuotation: " + quotationEntity.toString());

            final Future future = executorService.submit(() -> quotationDAO.insertQuotation(quotationEntity));

            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(LOG_TAG, e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        executorService.shutdown();
    }

    @Override
    public Single<Integer> countAll() {
        return quotationDAO.countAll();
    }

    public Integer countReported() {
        return reportedDAO.countReported();
    }

    public QuotationEntity getQuotation(final String digest) {
        return quotationDAO.getQuotation(digest);
    }
}
