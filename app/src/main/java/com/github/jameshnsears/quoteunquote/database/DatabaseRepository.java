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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    private DatabaseRepository(@NonNull Context context) {
        this.abstractQuotationDatabase = AbstractQuotationDatabase.getDatabase(context);
        this.quotationDAO = this.abstractQuotationDatabase.quotationsDAO();
        this.abstractHistoryDatabase = AbstractHistoryDatabase.getDatabase(context);
        this.previousDAO = this.abstractHistoryDatabase.previousDAO();
        this.favouriteDAO = this.abstractHistoryDatabase.favouritesDAO();
        this.reportedDAO = this.abstractHistoryDatabase.reportedDAO();
        this.currentDAO = this.abstractHistoryDatabase.currentDAO();
    }

    public static void resetDatabaseInstances(@NonNull Context context) {
        AbstractQuotationDatabase.quotationDatabase = null;
        AbstractHistoryDatabase.historyDatabase = null;
        getInstance(context);
    }

    public static synchronized DatabaseRepository getInstance(@NonNull Context context) {
        if (DatabaseRepository.databaseRepository == null) {
            DatabaseRepository.databaseRepository = new DatabaseRepository(context);
        }

        return DatabaseRepository.databaseRepository;
    }

    public Single<Integer> countAll() {
        return this.quotationDAO.countAll();
    }

    public int countPrevious(int widgetId, @NonNull ContentSelection contentSelection) {
        return this.previousDAO.countPrevious(widgetId, contentSelection);
    }

    public int positionInPrevious(
            int widgetId,
            @NonNull ContentPreferences contentPreferences) {

        final List<String> allPrevious = this.getPreviousDigests(widgetId, contentPreferences.getContentSelection());
        Collections.reverse(allPrevious);

        int position = 0;
        if (!allPrevious.isEmpty()) {
            final String currentDigest = this.getCurrentQuotation(widgetId).digest;
            position = allPrevious.indexOf(currentDigest) + 1;
        }

        return position;
    }

    public int countNext(@NonNull ContentPreferences contentPreferences) {
        final int countTotalNext;

        switch (contentPreferences.getContentSelection()) {
            case FAVOURITES:
                countTotalNext = this.favouriteDAO.countFavourites().blockingGet();
                break;

            case AUTHOR:
                countTotalNext = this.quotationDAO.getDigestsForAuthor(contentPreferences.getContentSelectionAuthor()).size();
                break;

            case SEARCH:
                countTotalNext = this.quotationDAO.getSearchTextDigests(
                        "%" + contentPreferences.getContentSelectionSearch() + "%").size();
                break;

            default:
                // ALL:
                countTotalNext = this.quotationDAO.countAll().blockingGet();
                break;
        }
        return countTotalNext;
    }

    public int countPrevious(
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @NonNull String criteria) {
        final List<String> digestsPrevious;
        final List<String> availableDigests;

        if (contentSelection == ContentSelection.AUTHOR) {
            digestsPrevious = this.previousDAO.getPreviousDigests(widgetId, ContentSelection.AUTHOR);
            availableDigests = this.quotationDAO.getDigestsForAuthor(criteria);
        } else {
            digestsPrevious = this.previousDAO.getPreviousDigests(widgetId, ContentSelection.SEARCH);
            availableDigests = this.quotationDAO.getSearchTextDigests("%" + criteria + "%");
        }

        int countPrevious = 0;
        for (String digest : availableDigests) {
            if (digestsPrevious.contains(digest)) {
                countPrevious++;
            }
        }

        return countPrevious;
    }

    @NonNull
    public Single<Integer> countFavourites() {
        return this.favouriteDAO.countFavourites();
    }

    @NonNull
    public QuotationEntity getNextQuotation(int widgetId, @NonNull ContentSelection contentSelection) {
        return this.getQuotation(this.previousDAO.getPrevious(widgetId, contentSelection).digest);
    }

    @NonNull
    public List<String> getPreviousDigests(int widgetId, @NonNull ContentSelection contentSelection) {
        return this.previousDAO.getPreviousDigests(widgetId, contentSelection);
    }

    @NonNull
    public List<String> getFavourites() {
        return this.favouriteDAO.getFavouriteDigests();
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts() {
        if (BuildConfig.DEBUG) {
            return this.quotationDAO.getAuthorsAndQuotationCounts(1);
        }
        return this.quotationDAO.getAuthorsAndQuotationCounts(5);
    }

    @NonNull
    public Integer countSearchText(@NonNull String text) {
        return this.quotationDAO.countSearchText("%" + text + "%");
    }

    public QuotationEntity getQuotation(String digest) {
        return this.quotationDAO.getQuotation(digest);
    }

    public void markAsPrevious(
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @NonNull String digest) {
        Timber.d("contentType=%d; digest=%s", contentSelection.getContentSelection(), digest);
        this.previousDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
    }

    public void markAsFavourite(@NonNull String digest) {
        if (this.favouriteDAO.isFavourite(digest) == 0) {
            Timber.d("digest=%s", digest);
            this.favouriteDAO.markAsFavourite(new FavouriteEntity(digest));
        }
    }

    public void markAsReported(@NonNull String digest) {
        Timber.d("digest=%s", digest);
        this.reportedDAO.markAsReported(new ReportedEntity(digest));
    }

    public void markAsCurrent(
            int widgetId,
            @NonNull String digest) {
        Timber.d("digest=%s", digest);
        this.currentDAO.erase(widgetId);
        this.currentDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
    }

    public QuotationEntity getCurrentQuotation(int widgetId) {
        final QuotationEntity quotationEntity = this.getQuotation(this.currentDAO.getCurrentDigest(widgetId));
        if (quotationEntity != null) {
            Timber.d("digest=%s", quotationEntity.digest);
        }
        return quotationEntity;
    }

    @NonNull
    public QuotationEntity getNextQuotation(
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @Nullable String searchString,
            boolean randomNext) {
        Timber.d("contentType=%d; searchString=%s", contentSelection.getContentSelection(), searchString);

        final List<String> nextQuotationDigests = this.getNextQuotationDigests(widgetId, contentSelection, searchString);

        final QuotationEntity currentQuotation = this.getCurrentQuotation(widgetId);
        final QuotationEntity nextQuotation;

        if (!randomNext) {
            final List<String> previousQuotations = this.getPreviousDigests(widgetId, contentSelection);

            if (previousQuotations.isEmpty()) {
                nextQuotation = this.getQuotation(nextQuotationDigests.get(0));
            } else {
                int indexInPrevious = previousQuotations.indexOf(currentQuotation.digest);

                if (indexInPrevious != 0) {
                    // move through previous quotations
                    indexInPrevious -= 1;
                    nextQuotation = this.getQuotation(previousQuotations.get(indexInPrevious));
                } else {
                    if (!nextQuotationDigests.isEmpty()) {
                        // use a new quotation
                        nextQuotation = this.getQuotation(nextQuotationDigests.get(0));
                    } else {
                        // we've run out of new quotations
                        nextQuotation = currentQuotation;
                    }
                }
            }
        } else {
            if (!nextQuotationDigests.isEmpty()) {
                nextQuotation = this.getQuotation(nextQuotationDigests.get(this.getRandomIndex(nextQuotationDigests)));
            } else {
                // we've run out of new quotations
                nextQuotation = currentQuotation;
            }
        }

        return nextQuotation;
    }

    private synchronized List<String> getNextQuotationDigests(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @Nullable final String searchString) {
        // insertion order required
        LinkedHashSet<String> nextQuotationDigests;

        // no order needed
        HashSet<String> previousDigests
                =  new HashSet<>(this.getPreviousDigests(widgetId, contentSelection));

        switch (contentSelection) {
            case FAVOURITES:
                nextQuotationDigests
                        = new LinkedHashSet<>(this.favouriteDAO.getNextFavouriteDigests());
                nextQuotationDigests.removeAll(previousDigests);
                break;

            case AUTHOR:
                LinkedHashSet<String> authorDigests
                        = new LinkedHashSet<>(this.quotationDAO.getNextAuthorDigest(searchString));
                authorDigests.removeAll(previousDigests);
                nextQuotationDigests = authorDigests;
                break;

            case SEARCH:
                LinkedHashSet<String> searchDigests
                        = new LinkedHashSet<>(this.quotationDAO.getNextSearchTextDigests("%" + searchString + "%"));
                searchDigests.removeAll(previousDigests);
                nextQuotationDigests = searchDigests;
                break;

            default:
                // ALL:
                LinkedHashSet<String> allDigests = new LinkedHashSet<>(this.quotationDAO.getNextAllDigests());
                allDigests.removeAll(previousDigests);
                nextQuotationDigests = allDigests;
                break;
        }

        return new ArrayList<>(nextQuotationDigests);
    }

    public int getRandomIndex(@NonNull List<String> availableNextQuotations) {
        return this.secureRandom.nextInt(availableNextQuotations.size());
    }

    public void erasePrevious(int widgetId, @NonNull String digest) {
        Timber.d("digest=%s", digest);
        this.previousDAO.erase(widgetId, ContentSelection.ALL, digest);
        this.previousDAO.erase(widgetId, ContentSelection.AUTHOR, digest);
        this.previousDAO.erase(widgetId, ContentSelection.SEARCH, digest);
    }

    public void eraseFavourite(int widgetId, @NonNull String digest) {
        Timber.d("digest=%s", digest);
        this.favouriteDAO.deleteFavourite(digest);
        this.previousDAO.erase(widgetId, ContentSelection.FAVOURITES, digest);
    }

    public void erase() {
        this.previousDAO.erase();
        this.currentDAO.erase();
        this.favouriteDAO.erase();
        this.reportedDAO.erase();
    }

    public void erase(int widgetId) {
        this.previousDAO.erase(widgetId);
        this.currentDAO.erase(widgetId);
    }

    public void erase(int widgetId, @NonNull ContentSelection contentSelection) {
        Timber.d("contentType=%d", contentSelection.getContentSelection());
        this.previousDAO.erase(widgetId, contentSelection);
        this.currentDAO.erase(widgetId);
    }

    @NonNull
    public Boolean isFavourite(@NonNull String digest) {
        return this.favouriteDAO.isFavourite(digest) > 0;
    }

    @NonNull
    public Boolean isReported(@NonNull String digest) {
        return this.reportedDAO.isReported(digest) > 0;
    }
}
