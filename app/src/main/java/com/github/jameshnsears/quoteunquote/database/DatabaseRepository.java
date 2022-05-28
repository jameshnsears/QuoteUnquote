package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.Single;
import timber.log.Timber;

public class DatabaseRepository {
    @NonNull
    public static DatabaseRepository databaseRepository;
    @NonNull
    protected final SecureRandom secureRandom = new SecureRandom();
    @Nullable
    public PreviousDAO previousDAO;
    @Nullable
    public AbstractQuotationDatabase abstractQuotationDatabase;
    @Nullable
    public AbstractHistoryDatabase abstractHistoryDatabase;
    @Nullable
    protected QuotationDAO quotationDAO;
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

    public static void close(@NonNull final Context context) {
        AbstractQuotationDatabase.getDatabase(context).close();
        AbstractQuotationDatabase.quotationDatabase = null;

        AbstractHistoryDatabase.getDatabase(context).close();
        AbstractHistoryDatabase.historyDatabase = null;
    }

    @NonNull
    public static synchronized DatabaseRepository getInstance(@NonNull final Context context) {
        if (DatabaseRepository.databaseRepository == null) {
            DatabaseRepository.databaseRepository = new DatabaseRepository(context);
        }

        return DatabaseRepository.databaseRepository;
    }

    @NonNull
    public static String getDefaultQuotationDigest() {
        if (BuildConfig.FLAVOR.equals("emanuelkebede")) {
            return "e8fa8bc3";
        }

        if (BuildConfig.DEBUG) {
            return "7a36e553";
        }

        return "1624c314";
    }

    @NonNull
    public Single<Integer> countAll() {
        return quotationDAO.countAll();
    }

    public int countPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return previousDAO.countPrevious(widgetId, contentSelection);
    }

    public int countPreviousDigest(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull String digest) {
        return previousDAO.countPreviousDigest(widgetId, contentSelection, digest);
    }

    public int countPrevious(final int widgetId) {
        return previousDAO.countPrevious(widgetId);
    }

    public int positionInPrevious(
            final int widgetId,
            @NonNull final QuotationsPreferences quotationsPreferences) {

        List<String> allPrevious = getPreviousDigests(widgetId, quotationsPreferences.getContentSelection());
        Collections.reverse(allPrevious);

        int position = 0;
        if (!allPrevious.isEmpty()) {
            String currentDigest = getCurrentQuotation(widgetId).digest;
            position = allPrevious.indexOf(currentDigest) + 1;
        }

        return position;
    }

    public int countNext(@NonNull final QuotationsPreferences quotationsPreferences) {
        int countTotalNext;

        switch (quotationsPreferences.getContentSelection()) {
            case FAVOURITES:
                countTotalNext = favouriteDAO.countFavourites().blockingGet();
                break;

            case AUTHOR:
                countTotalNext = quotationDAO.getDigestsForAuthor(quotationsPreferences.getContentSelectionAuthor()).size();
                break;

            case SEARCH:
                countTotalNext = quotationDAO.getSearchTextDigests(
                        "%" + quotationsPreferences.getContentSelectionSearch() + "%").size();
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
        HashSet<String> previousDigests;
        HashSet<String> availableDigests;

        if (contentSelection == ContentSelection.AUTHOR) {
            previousDigests = new HashSet<>(getPreviousDigests(widgetId, ContentSelection.AUTHOR));
            availableDigests = new HashSet<>(quotationDAO.getDigestsForAuthor(criteria));
        } else {
            previousDigests = new HashSet<>(getPreviousDigests(widgetId, ContentSelection.SEARCH));
            availableDigests = new HashSet<>(quotationDAO.getSearchTextDigests("%" + criteria + "%"));
        }

        int countPrevious = 0;
        for (final String digest : availableDigests) {
            if (previousDigests.contains(digest)) {
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
        return getQuotation(previousDAO.getLastPrevious(widgetId, contentSelection).digest);
    }

    @NonNull
    public String getLastPreviousDigest(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return previousDAO.getLastPrevious(widgetId, contentSelection).digest;
    }

    @NonNull
    public List<String> getPreviousDigests(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return previousDAO.getPreviousDigests(widgetId, contentSelection);
    }

    @NonNull
    public List<PreviousEntity> getPrevious() {
        return previousDAO.getLastPrevious();
    }

    @NonNull
    public List<String> getFavouriteDigests() {
        return favouriteDAO.getFavouriteDigests();
    }

    @NonNull
    public List<FavouriteEntity> getFavourites() {
        return favouriteDAO.getFavourites();
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

    @NonNull
    public List<QuotationEntity> getAllQuotations() {
        return quotationDAO.getAllQuotations();
    }

    @NonNull
    public QuotationEntity getQuotation(@NonNull final String digest) {
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

    public void markAsCurrent(
            final int widgetId,
            @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        currentDAO.erase(widgetId);
        currentDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
    }

    @NonNull
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
            List<String> previousQuotations = getPreviousDigests(widgetId, contentSelection);

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

    private synchronized List<String> getNextQuotationDigests(
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @Nullable String searchString) {
        // insertion order required
        final LinkedHashSet<String> nextQuotationDigests;

        // no order needed
        final HashSet<String> previousDigests
                = new HashSet<>(getPreviousDigests(widgetId, contentSelection));

        switch (contentSelection) {
            case FAVOURITES:
                nextQuotationDigests
                        = new LinkedHashSet<>(favouriteDAO.getNextFavouriteDigests());
                nextQuotationDigests.removeAll(previousDigests);
                break;

            case AUTHOR:
                final LinkedHashSet<String> authorDigests
                        = new LinkedHashSet<>(quotationDAO.getNextAuthorDigest(searchString));
                authorDigests.removeAll(previousDigests);
                nextQuotationDigests = authorDigests;
                break;

            case SEARCH:
                final LinkedHashSet<String> searchDigests
                        = new LinkedHashSet<>(quotationDAO.getNextSearchTextDigests("%" + searchString + "%"));
                searchDigests.removeAll(previousDigests);
                nextQuotationDigests = searchDigests;
                break;

            default:
                // ALL:
                final LinkedHashSet<String> allDigests = new LinkedHashSet<>(quotationDAO.getNextAllDigests());
                allDigests.removeAll(previousDigests);
                nextQuotationDigests = allDigests;
                break;
        }

        return new ArrayList<>(nextQuotationDigests);
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

    public void eraseForRestore() {
        previousDAO.erase();
        currentDAO.erase();
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
}
