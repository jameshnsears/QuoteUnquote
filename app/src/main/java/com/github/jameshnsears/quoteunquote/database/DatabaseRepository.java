package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.database.history.AbstractDatabaseHistory;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.database.history.FavouritesDAO;
import com.github.jameshnsears.quoteunquote.database.history.PreviousDAO;
import com.github.jameshnsears.quoteunquote.database.history.PreviousEntity;
import com.github.jameshnsears.quoteunquote.database.history.ReportedDAO;
import com.github.jameshnsears.quoteunquote.database.history.ReportedEntity;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractDatabaseQuotation;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationDAO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.security.SecureRandom;
import java.util.List;

import io.reactivex.Single;
import timber.log.Timber;

public class DatabaseRepository {
    @NonNull
    public static final String DEFAULT_QUOTATION_DIGEST = "1624c314";
    private static DatabaseRepository databaseRepository;
    @NonNull
    protected final SecureRandom secureRandom = new SecureRandom();
    @Nullable
    protected AbstractDatabaseQuotation abstractDatabaseQuotation;
    @Nullable
    protected QuotationDAO quotationDAO;
    @Nullable
    protected AbstractDatabaseHistory abstractDatabaseHistory;
    @Nullable
    protected PreviousDAO previousDAO;
    @Nullable
    protected FavouritesDAO favouritesDAO;
    @Nullable
    protected ReportedDAO reportedDAO;

    protected DatabaseRepository() {
        //
    }

    private DatabaseRepository(@NonNull final Context context) {
        abstractDatabaseQuotation = AbstractDatabaseQuotation.getDatabase(context);
        quotationDAO = abstractDatabaseQuotation.quotationsDAO();
        abstractDatabaseHistory = AbstractDatabaseHistory.getDatabase(context);
        previousDAO = abstractDatabaseHistory.previousDAO();
        favouritesDAO = abstractDatabaseHistory.favouritesDAO();
        reportedDAO = abstractDatabaseHistory.reportedDAO();
    }

    public static synchronized DatabaseRepository getInstance(@NonNull final Context context) {
        if (databaseRepository == null) {
            databaseRepository = new DatabaseRepository(context);
        }

        return databaseRepository;
    }

    public Single<Integer> countAll() {
        return quotationDAO.countAll();
    }

    public int countPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return previousDAO.countPrevious(widgetId, contentSelection);
    }

    public String getPreviousNextCounts(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @Nullable final String criteria) {
        int countPrevious = countPrevious(widgetId, contentSelection);

        int countNext = 0;
        switch (contentSelection) {
            case FAVOURITES:
                countNext = favouritesDAO.countFavourites().blockingGet().intValue();
                break;

            case AUTHOR:
                countNext = quotationDAO.getAuthors(criteria).size();
                break;

            case SEARCH:
                countNext = quotationDAO.getQuotationText("%" + criteria + "%").size();
                break;

            default:
                // ALL:
                countNext = quotationDAO.countAll().blockingGet().intValue();
                break;
        }

        return String.format("@ %d/%d", countPrevious, countNext);
    }

    public int countPrevious(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String criteria) {
        List<String> digestsPrevious;
        List<String> availableDigests;

        if (contentSelection == ContentSelection.AUTHOR) {
            digestsPrevious = previousDAO.getAllPrevious(widgetId, ContentSelection.AUTHOR);
            availableDigests = quotationDAO.getAuthors(criteria);
        } else {
            digestsPrevious = previousDAO.getAllPrevious(widgetId, ContentSelection.SEARCH);
            availableDigests = quotationDAO.getQuotationText("%" + criteria + "%");
        }

        int countPrevious = 0;
        for (final String digest : availableDigests) {
            if (digestsPrevious.contains(digest)) {
                countPrevious++;
            }
        }

        return countPrevious;
    }

    @NonNull
    public Single<Integer> countFavourites() {
        return favouritesDAO.countFavourites();
    }

    @NonNull
    public QuotationEntity getNextQuotation(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return getQuotation(previousDAO.getPrevious(widgetId, contentSelection).digest);
    }

    @NonNull
    public List<String> getAllPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        final List<String> previousOrdered = previousDAO.getAllPrevious(widgetId, contentSelection);
        logDigests(previousOrdered);

        return previousOrdered;
    }

    @NonNull
    public List<String> getFavourites() {
        final List<String> favouriteQuotations = favouritesDAO.getFavourites();
        logDigests(favouriteQuotations);

        return favouriteQuotations;
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthors() {
        return quotationDAO.authors();
    }

    @NonNull
    public Integer countQuotationsText(@NonNull final String text) {
        return quotationDAO.countQuotationsText("%" + text + "%");
    }

    public QuotationEntity getQuotation(final String digest) {
        return quotationDAO.getQuotation(digest);
    }

    public void markAsPrevious(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String digest) {
        Timber.d("%d: contentType=%d; digest=%s", widgetId, contentSelection.getContentType(), digest);

        previousDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
    }

    public void markAsFavourite(@NonNull final String digest) {
        if (favouritesDAO.countFavourite(digest) == 0 && quotationDAO.getQuotation(digest) != null) {
            Timber.d("digest=%s", digest);
            favouritesDAO.markAsFavourite(new FavouriteEntity(digest));
        }
    }

    public void markAsReported(@NonNull final String digest) {
        if (reportedDAO.countReported(digest) == 0) {
            Timber.d("digest=%s", digest);
            reportedDAO.markAsReported(new ReportedEntity(digest));
        }
    }

    @NonNull
    public QuotationEntity getNextQuotation(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String searchString,
            final boolean randomNext)
            throws NoNextQuotationAvailableException {
        Timber.d("%d: contentType=%d; searchString=%s", widgetId, contentSelection.getContentType(), searchString);

        List<String> availableQuotations;

        switch (contentSelection) {
            case FAVOURITES:
                availableQuotations
                        = favouritesDAO.getFavourites(getAllPrevious(widgetId, contentSelection));
                break;

            case AUTHOR:
                availableQuotations
                        = quotationDAO.getAuthors(
                        searchString, getAllPrevious(widgetId, contentSelection));
                break;

            case SEARCH:
                availableQuotations
                        = quotationDAO.getQuotationText(
                        "%" + searchString + "%", getAllPrevious(widgetId, contentSelection));
                break;

            default:
                // ALL:
                availableQuotations
                        = quotationDAO.getAll(
                        getAllPrevious(widgetId, contentSelection));
                break;
        }

        if (availableQuotations.isEmpty()) {
            throw new NoNextQuotationAvailableException();
        }

        if (randomNext) {
            return getQuotation(availableQuotations.get(getRandomIndex(availableQuotations)));
        }

        return getQuotation(availableQuotations.get(0));
    }

    public int getRandomIndex(@NonNull final List<String> availableNextQuotations) {
        return secureRandom.nextInt(availableNextQuotations.size());
    }

    private void logDigests(@NonNull final List<String> digests) {
        int index = 0;
        for (final String digest : digests) {
            Timber.d("index=%d, digest=%s", index, digest);
            index += 1;
        }
    }

    public void deleteFavourite(final int widgetId, @NonNull final String digest) {
        Timber.d("%d: digest=%s", widgetId, digest);
        favouritesDAO.deleteFavourite(digest);
        previousDAO.deleteAll(widgetId, ContentSelection.FAVOURITES, digest);
    }

    public void deleteFavourites() {
        favouritesDAO.deleteAll();
    }

    public void deletePrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        Timber.d("%d: contentType=%d", widgetId, contentSelection.getContentType());
        previousDAO.deleteAll(widgetId, contentSelection);
    }

    public void deletePrevious() {
        previousDAO.deleteAll();
    }

    public void deletePrevious(final int widgetId) {
        Timber.d("widgetId=%d", widgetId);
        previousDAO.deleteAll(widgetId);
    }

    public void deleteReported() {
        reportedDAO.deleteAll();
    }

    @NonNull
    public Integer countFavourite(@NonNull final String digest) {
        return favouritesDAO.countFavourite(digest);
    }

    @NonNull
    public Integer countReported(@NonNull final String digest) {
        return reportedDAO.countReported(digest);
    }
}
