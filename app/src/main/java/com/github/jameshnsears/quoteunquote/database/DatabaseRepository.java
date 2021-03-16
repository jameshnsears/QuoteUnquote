package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.history.CurrentDAO;
import com.github.jameshnsears.quoteunquote.database.history.CurrentEntity;
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
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.security.SecureRandom;
import java.util.Collections;
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
    public PreviousDAO previousDAO;
    @Nullable
    protected AbstractQuotationDatabase abstractQuotationDatabase;
    @Nullable
    protected QuotationDAO quotationDAO;
    @Nullable
    protected AbstractHistoryDatabase abstractHistoryDatabase;
    @Nullable
    protected FavouriteDAO favouriteDAO;
    @Nullable
    protected ReportedDAO reportedDAO;
    @Nullable
    protected CurrentDAO currentDAO;

    protected DatabaseRepository() {
        //
    }

    private DatabaseRepository(@NonNull final Context context) {
        abstractQuotationDatabase = AbstractQuotationDatabase.getDatabase(context);
        quotationDAO = abstractQuotationDatabase.quotationsDAO();
        abstractHistoryDatabase = AbstractHistoryDatabase.getDatabase(context);
        previousDAO = abstractHistoryDatabase.previousDAO();
        favouriteDAO = abstractHistoryDatabase.favouritesDAO();
        reportedDAO = abstractHistoryDatabase.reportedDAO();
        currentDAO = abstractHistoryDatabase.currentDAO();
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

    public String getQuotationPosition(
            final int widgetId,
            @NonNull final ContentPreferences contentPreferences) {

        List<String> allPrevious = getAllPrevious(widgetId, contentPreferences.getContentSelection());
        Collections.reverse(allPrevious);

        int position = 0;
        if (allPrevious.size() != 0) {
            String currentDigest = getCurrentQuotation(widgetId).digest;
            position = allPrevious.indexOf(currentDigest) + 1;
        }
       
        return String.format("@ %d/%d", position, countNext(widgetId, contentPreferences));
    }

    public int countNext(
            final int widgetId,
            @NonNull final ContentPreferences contentPreferences) {
        int countTotalNext = 0;

        switch (contentPreferences.getContentSelection()) {
            case FAVOURITES:
                countTotalNext = favouriteDAO.countFavourites().blockingGet().intValue();
                break;

            case AUTHOR:
                countTotalNext = quotationDAO.getAuthors(contentPreferences.getContentSelectionAuthor()).size();
                break;

            case SEARCH:
                countTotalNext = quotationDAO.getQuotationText(
                        "%" + contentPreferences.getContentSelectionSearch() + "%").size();
                break;

            default:
                // ALL:
                countTotalNext = quotationDAO.countAll().blockingGet().intValue();
                break;
        }
        return countTotalNext;
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
        return favouriteDAO.countFavourites();
    }

    @NonNull
    public QuotationEntity getNextQuotation(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return getQuotation(previousDAO.getPrevious(widgetId, contentSelection).digest);
    }

    @NonNull
    public List<String> getAllPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        final List<String> previousOrdered = previousDAO.getAllPrevious(widgetId, contentSelection);
        return previousOrdered;
    }

    @NonNull
    public List<String> getFavourites() {
        final List<String> favouriteQuotations = favouriteDAO.getFavourites();
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
        Timber.d("contentType=%d; digest=%s", contentSelection.getContentSelection(), digest);
        previousDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
    }

    public void markAsFavourite(@NonNull final String digest) {
        if (favouriteDAO.isFavourite(digest) == 0 && quotationDAO.getQuotation(digest) != null) {
            Timber.d("digest=%s", digest);
            favouriteDAO.markAsFavourite(new FavouriteEntity(digest));
        }
    }

    public void markAsReported(@NonNull final String digest) {
        if (reportedDAO.isReported(digest) == 0) {
            Timber.d("digest=%s", digest);
            reportedDAO.markAsReported(new ReportedEntity(digest));
        }
    }

    public void markAsCurrent(
            final int widgetId,
            @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        currentDAO.erase(widgetId);
        currentDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
    }

    public QuotationEntity getCurrentQuotation(final int widgetId) {
        QuotationEntity quotationEntity = getQuotation(currentDAO.getCurrent(widgetId));
        if (quotationEntity != null) {
            Timber.d("digest=%s", quotationEntity.digest);
        }
        return quotationEntity;
    }

    @NonNull
    public QuotationEntity getNextQuotation(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String searchString,
            final boolean randomNext)
            throws NoNextQuotationAvailableException {
        Timber.d("contentType=%d; searchString=%s", contentSelection.getContentSelection(), searchString);

        List<String> availableQuotations;

        switch (contentSelection) {
            case FAVOURITES:
                availableQuotations
                        = favouriteDAO.getFavourites(getAllPrevious(widgetId, contentSelection));
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

    public void eraseFavourite(final int widgetId, @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        favouriteDAO.deleteFavourite(digest);
        previousDAO.erase(widgetId, ContentSelection.FAVOURITES, digest);
    }

    public void erase() {
        previousDAO.erase();
        currentDAO.erase();
        favouriteDAO.erase();
        reportedDAO.erase();
    }

    public void erase(final int widgetId) {
        previousDAO.erase(widgetId);
        currentDAO.erase(widgetId);
    }

    public void erase(final int widgetId, @NonNull final ContentSelection contentSelection) {
        Timber.d("contentType=%d", contentSelection.getContentSelection());
        previousDAO.erase(widgetId, contentSelection);
        currentDAO.erase(widgetId);
    }

    @NonNull
    public Integer countFavourite(@NonNull final String digest) {
        return favouriteDAO.isFavourite(digest);
    }

    @NonNull
    public Integer countReported(@NonNull final String digest) {
        return reportedDAO.isReported(digest);
    }
}
