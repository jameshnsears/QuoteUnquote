package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.BuildConfig;
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

    public DatabaseRepository() {
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

    public static void resetDatabaseInstances(@NonNull final Context context) {
        AbstractQuotationDatabase.quotationDatabase = null;
        AbstractHistoryDatabase.historyDatabase = null;
        DatabaseRepository.getInstance(context);
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

    public int positionInPrevious(
            final int widgetId,
            @NonNull final ContentPreferences contentPreferences) {

        List<String> allPrevious = getPrevious(widgetId, contentPreferences.getContentSelection());
        Collections.reverse(allPrevious);

        int position = 0;
        if (!allPrevious.isEmpty()) {
            String currentDigest = getCurrentQuotation(widgetId).digest;
            position = allPrevious.indexOf(currentDigest) + 1;
        }

        return position;
    }

    public int countNext(@NonNull final ContentPreferences contentPreferences) {
        int countTotalNext;

        switch (contentPreferences.getContentSelection()) {
            case FAVOURITES:
                countTotalNext = favouriteDAO.countFavourites().blockingGet();
                break;

            case AUTHOR:
                countTotalNext = quotationDAO.getDigestsForAuthor(contentPreferences.getContentSelectionAuthor()).size();
                break;

            case SEARCH:
                countTotalNext = quotationDAO.getSearchTextDigests(
                        "%" + contentPreferences.getContentSelectionSearch() + "%").size();
                break;

            default:
                // ALL:
                countTotalNext = quotationDAO.countAll().blockingGet();
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
            availableDigests = quotationDAO.getDigestsForAuthor(criteria);
        } else {
            digestsPrevious = previousDAO.getAllPrevious(widgetId, ContentSelection.SEARCH);
            availableDigests = quotationDAO.getSearchTextDigests("%" + criteria + "%");
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
    public List<String> getPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return previousDAO.getAllPrevious(widgetId, contentSelection);
    }

    @NonNull
    public List<String> getFavourites() {
        return favouriteDAO.getFavouriteDigests();
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts() {
        if (BuildConfig.DEBUG) {
            return quotationDAO.getAuthorsAndQuotationCounts(1);
        }
        return quotationDAO.getAuthorsAndQuotationCounts(5);
    }

    @NonNull
    public Integer countSearchText(@NonNull final String text) {
        return quotationDAO.countSearchText("%" + text + "%");
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
        if (favouriteDAO.isFavourite(digest) == 0) {
            Timber.d("digest=%s", digest);
            favouriteDAO.markAsFavourite(new FavouriteEntity(digest));
        }
    }

    public void markAsReported(@NonNull final String digest) {
        Timber.d("digest=%s", digest);
        reportedDAO.markAsReported(new ReportedEntity(digest));
    }

    public void markAsCurrent(
            final int widgetId,
            @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        currentDAO.erase(widgetId);
        currentDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
    }

    public QuotationEntity getCurrentQuotation(final int widgetId) {
        QuotationEntity quotationEntity = getQuotation(currentDAO.getCurrentDigest(widgetId));
        if (quotationEntity != null) {
            Timber.d("digest=%s", quotationEntity.digest);
        }
        return quotationEntity;
    }

    @NonNull
    public QuotationEntity getNextQuotation(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @Nullable final String searchString,
            final boolean randomNext) {
        Timber.d("contentType=%d; searchString=%s", contentSelection.getContentSelection(), searchString);

        List<String> nextQuotationDigests = getNextQuotationDigests(widgetId, contentSelection, searchString);

        QuotationEntity currentQuotation = getCurrentQuotation(widgetId);
        QuotationEntity nextQuotation;

        if (!randomNext) {
            List<String> previousQuotations = getPrevious(widgetId, contentSelection);

            if (previousQuotations.isEmpty()) {
                nextQuotation = getQuotation(nextQuotationDigests.get(0));
            } else {
                int indexInPrevious = previousQuotations.indexOf(currentQuotation.digest);

                if (indexInPrevious != 0) {
                    // move through previous quotations
                    indexInPrevious -= 1;
                    nextQuotation = getQuotation(previousQuotations.get(indexInPrevious));
                } else {
                    if (!nextQuotationDigests.isEmpty()) {
                        // use a new quotation
                        nextQuotation = getQuotation(nextQuotationDigests.get(0));
                    } else {
                        // we've run out of new quotations
                        nextQuotation = currentQuotation;
                    }
                }
            }
        } else {
            if (!nextQuotationDigests.isEmpty()) {
                nextQuotation = getQuotation(nextQuotationDigests.get(getRandomIndex(nextQuotationDigests)));
            } else {
                // we've run out of new quotations
                nextQuotation = currentQuotation;
            }
        }

        return nextQuotation;
    }

    private List<String> getNextQuotationDigests(int widgetId, @NonNull ContentSelection contentSelection, @Nullable String searchString) {
        List<String> nextQuotationDigests;
        switch (contentSelection) {
            case FAVOURITES:
                nextQuotationDigests
                        = favouriteDAO.getNextFavouriteDigests(getPrevious(widgetId, contentSelection));
                break;

            case AUTHOR:
                nextQuotationDigests
                        = quotationDAO.getNextAuthorDigest(
                        searchString, getPrevious(widgetId, contentSelection));
                break;

            case SEARCH:
                nextQuotationDigests
                        = quotationDAO.getNextSearchTextDigests(
                        "%" + searchString + "%", getPrevious(widgetId, contentSelection));
                break;

            default:
                // ALL:
                nextQuotationDigests
                        = quotationDAO.getNextAllDigests(
                        getPrevious(widgetId, contentSelection));
                break;
        }
        return nextQuotationDigests;
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
    public Boolean isFavourite(@NonNull final String digest) {
        return favouriteDAO.isFavourite(digest) > 0;
    }

    @NonNull
    public Boolean isReported(@NonNull final String digest) {
        return reportedDAO.isReported(digest) > 0;
    }
}
