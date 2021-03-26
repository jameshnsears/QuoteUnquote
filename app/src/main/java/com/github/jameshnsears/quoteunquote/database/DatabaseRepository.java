package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;
import android.util.Log;

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteDAO;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.database.history.PreviousDAO;
import com.github.jameshnsears.quoteunquote.database.history.PreviousEntity;
import com.github.jameshnsears.quoteunquote.database.history.ReportedDAO;
import com.github.jameshnsears.quoteunquote.database.history.ReportedEntity;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationDAO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentType;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;

public class DatabaseRepository {
    public static final String DEFAULT_QUOTATION_DIGEST = "1624c314";
    private static final String LOG_TAG = DatabaseRepository.class.getSimpleName();
    protected AbstractQuotationDatabase abstractQuotationDatabase;
    protected QuotationDAO quotationDAO;
    protected AbstractHistoryDatabase abstractHistoryDatabase;
    protected PreviousDAO previousDAO;
    protected FavouriteDAO favouriteDAO;
    protected ReportedDAO reportedDAO;
    protected SecureRandom secureRandom = new SecureRandom();

    public DatabaseRepository() {
    }

    public DatabaseRepository(final Context context) {
        abstractQuotationDatabase = AbstractQuotationDatabase.getDatabase(context);
        quotationDAO = abstractQuotationDatabase.quotationsDAO();
        abstractHistoryDatabase = AbstractHistoryDatabase.getDatabase(context);
        previousDAO = abstractHistoryDatabase.contentDAO();
        favouriteDAO = abstractHistoryDatabase.favouritesDAO();
        reportedDAO = abstractHistoryDatabase.reportedDAO();
    }

    public Single<Integer> countAll() {
        return quotationDAO.countAll();
    }

    public int countPrevious(final int widgetId, final ContentType contentType) {
        return previousDAO.countPrevious(widgetId, contentType);
    }

    public int countPrevious(final int widgetId, final ContentType contentType, final String criteria) {
        List<String> digestsPrevious;
        List<String> availableDigests;

        if (contentType == ContentType.AUTHOR) {
            digestsPrevious = previousDAO.getPrevious(widgetId, ContentType.AUTHOR);
            availableDigests = quotationDAO.getAuthors(criteria);
        } else {
            digestsPrevious = previousDAO.getPrevious(widgetId, ContentType.QUOTATION_TEXT);
            availableDigests = quotationDAO.getQuotationText("%" + criteria + "%");
        }

        int countDigestInPrevious = 0;
        for (final String digest : availableDigests) {
            if (digestsPrevious.contains(digest)) {
                countDigestInPrevious++;
            }
        }

        return countDigestInPrevious;
    }

    public Single<Integer> countFavourites() {
        return favouriteDAO.countFavourites();
    }

    public QuotationEntity getNext(final int widgetId, final ContentType contentType) {
        return getQuotation(previousDAO.getNext(widgetId, contentType).digest);
    }

    public List<String> getPrevious(final int widgetId, final ContentType contentType) {
        final List<String> previousOrdered = previousDAO.getPrevious(widgetId, contentType);
        logDigests(new Object() {
        }.getClass().getEnclosingMethod().getName(), previousOrdered);

        return previousOrdered;
    }

    public List<String> getFavourites() {
        final List<String> favouriteQuotations = favouriteDAO.getFavourites();
        logDigests(new Object() {
        }.getClass().getEnclosingMethod().getName(), favouriteQuotations);

        return favouriteQuotations;
    }

    public Single<List<AuthorPOJO>> getAuthorsWithAtLeastFiveQuotations() {
        return quotationDAO.authorsWithAtLeastFiveQuotations();
    }

    public Single<List<AuthorPOJO>> getAuthors() {
        return quotationDAO.authors();
    }

    public Integer countQuotationsText(final String text) {
        if ("".equals(text)) {
            return 0;
        } else {
            return quotationDAO.countQuotationsText("%" + text + "%");
        }
    }

    public QuotationEntity getQuotation(final String digest) {
        return quotationDAO.getQuotation(digest);
    }

    public void markAsPrevious(final int widgetId, final ContentType contentType, final String digest) {
        Log.d(LOG_TAG, String.format("%d: %s: contentType=%d; digest=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                contentType.getContentType(), digest));

        previousDAO.markAsPrevious(new PreviousEntity(widgetId, contentType, digest));
    }

    public void markAsFavourite(final String digest) {
        Log.d(LOG_TAG, String.format("%s: digest=%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName(), digest));

        if (favouriteDAO.countIsFavourite(digest) == 0 && quotationDAO.getQuotation(digest) != null) {
            favouriteDAO.markAsFavourite(new FavouriteEntity(digest));
        }
    }

    public void markAsReported(final String digest) {
        if (reportedDAO.countIsReported(digest) == 0) {
            Log.d(LOG_TAG, String.format("%s: digest=%s",
                    new Object() {
                    }.getClass().getEnclosingMethod().getName(),
                    digest));
            reportedDAO.markAsReported(new ReportedEntity(digest));
        }
    }

    public QuotationEntity getNextRandom(final int widgetId, final ContentType contentType, final String searchString)
            throws NoNextQuotationAvailableException {
        Log.d(LOG_TAG, String.format(Locale.ENGLISH,
                "%d: %s: contentType=%d; searchString=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                contentType.getContentType(), searchString));

        List<String> availableQuotations = new ArrayList<>();

        switch (contentType) {
            case ALL:
                availableQuotations
                        = quotationDAO.getAll(
                        getPrevious(widgetId, contentType));
                break;

            case FAVOURITES:
                availableQuotations
                        = favouriteDAO.getFavourites(getPrevious(widgetId, contentType));
                break;

            case AUTHOR:
                availableQuotations
                        = quotationDAO.getAuthors(
                        searchString, getPrevious(widgetId, contentType));
                break;

            case QUOTATION_TEXT:
                availableQuotations
                        = quotationDAO.getQuotationText(
                        "%" + searchString + "%", getPrevious(widgetId, contentType));
                break;

            default:
                Log.e(LOG_TAG, contentType.getContentType().toString());
                break;
        }

        if (availableQuotations.isEmpty()) {
            throw new NoNextQuotationAvailableException(contentType);
        }

        return getQuotation(availableQuotations.get(geRandomIndex(availableQuotations)));
    }

    public int geRandomIndex(final List<String> availableNextQuotations) {
        if (BuildConfig.USE_PROD_QUOTATION_ORDER) {
            return secureRandom.nextInt(availableNextQuotations.size());
        } else {
            return 0;
        }
    }

    private void logDigests(final String source, final List<String> digests) {
        int index = 0;
        for (final String digest : digests) {
            Log.d(LOG_TAG, String.format("%s: index=%d, digest=%s", source, index, digest));
            index += 1;
        }
    }

    public void deleteFavourite(final int widgetId, final String digest) {
        Log.d(LOG_TAG, String.format("%d: %s: digest=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                digest));

        favouriteDAO.deleteFavourite(digest);
        previousDAO.deletePrevious(widgetId, ContentType.FAVOURITES, digest);
    }

    public void deleteFavourites() {
        Log.d(LOG_TAG, String.format("%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        favouriteDAO.deleteFavourites();
    }

    public void deletePrevious(final int widgetId, final ContentType contentType) {
        Log.d(LOG_TAG, String.format("%d: %s: contentType=%d", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                contentType.getContentType()));

        previousDAO.deletePrevious(widgetId, contentType);
    }

    public void deletePrevious() {
        Log.d(LOG_TAG, String.format("%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        previousDAO.deletePrevious();
    }

    public void deletePrevious(final int widgetId) {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        previousDAO.deletePrevious(widgetId);
    }

    public void deleteReported() {
        Log.d(LOG_TAG, String.format("%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        reportedDAO.deleteReported();
    }

    public Integer countIsFavourite(final String digest) {
        return favouriteDAO.countIsFavourite(digest);
    }

    public Integer countIsReported(final String digest) {
        return reportedDAO.countIsReported(digest);
    }
}
