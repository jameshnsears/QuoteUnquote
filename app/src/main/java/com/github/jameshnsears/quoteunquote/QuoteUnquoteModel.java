package com.github.jameshnsears.quoteunquote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer;
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.scraper.Scraper;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperData;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperQuotationException;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperSourceException;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperUrlException;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.reactivex.Single;
import timber.log.Timber;

public class QuoteUnquoteModel {
    @Nullable
    public DatabaseRepository databaseRepository;

    @Nullable
    public List<AuthorPOJO> cachedAuthorPOJOList;

    @Nullable
    protected Context context;

    public QuoteUnquoteModel() {
    }

    public QuoteUnquoteModel(int widgetId, @NonNull final Context widgetContext) {
        context = widgetContext;
        databaseRepository = DatabaseRepository.getInstance(this.context);

        if (widgetId != -1) {
            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);
            if (quotationsPreferences.getDatabaseInternal()) {
                databaseRepository.useInternalDatabase = true;
            } else {
                databaseRepository.useInternalDatabase = false;
            }
        }
    }

    private boolean isNextNew(
            final int widgetId, @NonNull QuotationsPreferences quotationsPreferences, final boolean randomNext) {
        int availableQuotations = databaseRepository.countNext(quotationsPreferences);

        if (databaseRepository.findPositionInPrevious(widgetId, quotationsPreferences) == availableQuotations) {
            return false;
        }

        return !randomNext
                || databaseRepository.countPreviousCriteria(widgetId, quotationsPreferences.getContentSelection())
                != availableQuotations;
    }

    @NonNull
    public QuotationsPreferences getContentPreferences(int widgetId) {
        return new QuotationsPreferences(widgetId, context);
    }

    @Nullable
    public QuotationEntity getPreviousQuotation(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String digest,
            @NonNull final String criteria) {

        final Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {

            List<String> previousDigests = getPreviousDigests(widgetId, contentSelection, criteria);

            int priorDigestIndex = previousDigests.indexOf(digest) + 1;
            if (priorDigestIndex == previousDigests.size()) {
                priorDigestIndex -= 1;
            }

            return databaseRepository.getQuotation(previousDigests.get(priorDigestIndex));
        });

        QuotationEntity quotationEntity = null;

        try {
            quotationEntity = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    @Nullable
    public String getLastPreviousDigest(final int widgetId) {

        final Future<String> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {

            List<String> lastPreviousDigests = databaseRepository.
                    getPreviousDigests(
                            widgetId,
                            getContentPreferences(widgetId).getContentSelection(),
                            getContentPreferences(widgetId).getContentSelectionAllExclusion());

            return lastPreviousDigests.get(0);
        });

        String lastPreviousDigest = null;

        try {
            lastPreviousDigest = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return lastPreviousDigest;
    }

    @Nullable
    public QuotationEntity getCurrentQuotation(
            final int widgetId) {

        final Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.getCurrentQuotation(widgetId));

        QuotationEntity quotationEntity = null;

        try {
            quotationEntity = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    @Nullable
    public String getCurrentPosition(
            final int widgetId,
            @NonNull final QuotationsPreferences quotationsPreferences) {
        final Future<String> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            int positionInPrevious = databaseRepository.findPositionInPrevious(widgetId, quotationsPreferences);

            if (positionInPrevious == 0) {
                positionInPrevious += 1;
            }

            return String.format(Locale.ENGLISH, "%d/%d",
                    positionInPrevious,
                    databaseRepository.countNext(quotationsPreferences));
        });

        String currentPosition = null;

        try {
            currentPosition = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return currentPosition;
    }

    public void markAsCurrentNext(
            final int widgetId,
            final boolean randomNext) {
        Timber.d("randomNext=%b", randomNext);

        final QuotationsPreferences quotationsPreferences = getContentPreferences(widgetId);

        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            QuotationEntity nextQuotation = getNextQuotation(widgetId, randomNext);

            List<String> previous = getPreviousDigests(
                    widgetId,
                    quotationsPreferences.getContentSelection(),
                    quotationsPreferences.getContentSelectionAllExclusion());

            if (!previous.contains(nextQuotation.digest)) {
                databaseRepository.markAsPrevious(
                        widgetId,
                        quotationsPreferences.getContentSelection(),
                        nextQuotation.digest);
            } else {
                if (!isNextNew(widgetId, quotationsPreferences, randomNext) && randomNext) {
                    Timber.d("purge Previous");
                    resetPrevious(widgetId, quotationsPreferences.getContentSelection());
                    markAsCurrentDefault(widgetId);
                    nextQuotation = getCurrentQuotation(widgetId);
                } else {
                    Timber.d("NO purge Previous");
                }
            }

            addToPreviousAll(widgetId, nextQuotation);

            databaseRepository.markAsCurrent(
                    widgetId,
                    nextQuotation.digest);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    private void addToPreviousAll(final int widgetId, @NonNull final QuotationEntity quotationEntity) {
        if (getContentPreferences(widgetId).getContentAddToPreviousAll()) {
            if (!getPreviousDigests(
                    widgetId, ContentSelection.ALL, getContentPreferences(widgetId).getContentSelectionAllExclusion())
                    .contains(quotationEntity.digest)) {
                databaseRepository.markAsPrevious(
                        widgetId,
                        ContentSelection.ALL,
                        quotationEntity.digest);
            }
        }
    }

    @NonNull
    public QuotationEntity getNextQuotation(
            final int widgetId,
            final boolean randomNext) {
        final Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            switch (getContentPreferences(widgetId).getContentSelection()) {
                case FAVOURITES:
                    return databaseRepository.getNextQuotation(
                            widgetId,
                            ContentSelection.FAVOURITES,
                            "",
                            randomNext,
                            getContentPreferences(widgetId));
                case AUTHOR:
                    return databaseRepository.getNextQuotation(
                            widgetId,
                            ContentSelection.AUTHOR,
                            getContentPreferences(widgetId).getContentSelectionAuthor(),
                            randomNext,
                            getContentPreferences(widgetId));
                case SEARCH:
                    return databaseRepository.getNextQuotation(
                            widgetId,
                            ContentSelection.SEARCH,
                            getContentPreferences(widgetId).getContentSelectionSearch(),
                            randomNext,
                            getContentPreferences(widgetId));
                default:
                    return databaseRepository.getNextQuotation(
                            widgetId,
                            ContentSelection.ALL,
                            getContentPreferences(widgetId).getContentSelectionAllExclusion(),
                            randomNext,
                            getContentPreferences(widgetId));
            }
        });

        QuotationEntity nextQuotation = null;

        try {
            nextQuotation = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return nextQuotation;
    }

    public void markAsCurrent(final int widgetId, @NonNull final String digest) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            databaseRepository.markAsCurrent(widgetId, digest);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void markAsCurrentDefault(final int widgetId) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            ContentSelection contentSelection = getContentPreferences(widgetId).getContentSelection();

            setDefault(widgetId, contentSelection);

            QuotationEntity quotationEntity
                    = databaseRepository.getNextQuotation(widgetId, contentSelection);

            addToPreviousAll(widgetId, quotationEntity);

            databaseRepository.markAsCurrent(
                    widgetId,
                    quotationEntity.digest);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void setDefault(
            final int widgetId, @NonNull final ContentSelection contentSelection) {
        switch (contentSelection) {
            case AUTHOR:
                setDefaultAuthor(widgetId);
                break;

            case FAVOURITES:
                setDefaultFavourite(widgetId);
                break;

            case SEARCH:
                setDefaultSearch(widgetId);
                break;

            default:
                setDefaultAll(widgetId);
                break;
        }
    }

    @NonNull
    public void alignHistoryWithQuotations(int widgetId) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            databaseRepository.alignHistoryWithQuotations(widgetId, context);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    public List<String> getPreviousDigests(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String criteria) {
        final Future<List<String>> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.getPreviousDigests(widgetId, contentSelection, criteria));

        List<String> allPreviousDigests = new ArrayList<>();

        try {
            allPreviousDigests = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return allPreviousDigests;
    }

    private void setDefaultAuthor(int widgetId) {
        if (countPreviousAuthor(widgetId) == 0) {
            databaseRepository.erase(widgetId, ContentSelection.AUTHOR);
            markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultFavourite(int widgetId) {
        if (countPrevious(widgetId, ContentSelection.FAVOURITES) == 0) {
            markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultSearch(int widgetId) {
        if (countPreviousSearch(widgetId) == 0) {
            databaseRepository.erase(widgetId, ContentSelection.SEARCH);
            markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultAll(final int widgetId) {
        if (countPrevious(widgetId, ContentSelection.ALL) == 0) {
            databaseRepository.markAsPrevious(
                    widgetId, ContentSelection.ALL, DatabaseRepository.getDefaultQuotationDigest());
        }
    }

    public int countPrevious(final int widgetId,
                             @NonNull final ContentSelection contentSelection) {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.countPreviousCriteria(widgetId, contentSelection));

        int countPrevious = 0;
        try {
            countPrevious = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return countPrevious;
    }

    public int countPrevious(final int widgetId) {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.countPreviousCriteria(widgetId));

        int countPrevious = 0;
        try {
            countPrevious = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return countPrevious;
    }

    public int countPreviousAuthor(final int widgetId) {
        return databaseRepository.countPreviousCriteria(widgetId, ContentSelection.AUTHOR,
                getContentPreferences(widgetId));
    }

    public int countPreviousSearch(final int widgetId) {
        return databaseRepository.countPreviousCriteria(widgetId, ContentSelection.SEARCH,
                getContentPreferences(widgetId));
    }

    public int countFavouritesWithoutRx() {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.countFavourites().blockingGet());

        int favouritesCount = 0;
        try {
            favouritesCount = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return favouritesCount;
    }

    public int toggleFavourite(final int widgetId, @NonNull final String digest) {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            List<String> favourites = databaseRepository.getFavouritesDigests();

            if (!favourites.contains(digest)) {
                databaseRepository.markAsFavourite(digest);
                auditFavourite(digest);
            } else {
                databaseRepository.eraseFavourite(widgetId, digest);
            }

            return databaseRepository.countFavourites().blockingGet();
        });

        int favouritesCount = 0;
        try {
            favouritesCount = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return favouritesCount;
    }

    private void auditFavourite(@NonNull String digest) {
        QuotationEntity quotationEntity = getQuotation(digest);

        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        properties.put("Favourite",
                "digest=" + digest + "; author=" + quotationEntity.author + "; quotation=" + quotationEntity.quotation);
        AuditEventHelper.auditEvent("FAVOURITE", properties);
    }

    public boolean isFavourite(@NonNull final String digest) {
        final Future<Boolean> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.isFavourite(digest));

        boolean isFavourite = false;

        try {
            isFavourite = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return isFavourite;
    }

    public QuotationEntity getQuotation(@NonNull final String digest) {
        final Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.getQuotation(digest));

        QuotationEntity quotationEntity = null;

        try {
            quotationEntity = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    public void delete(final int widgetId) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.erase(widgetId)
        );

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void disable() {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.erase());

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void resetPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        Timber.d("contentSelection=%d", contentSelection.getContentSelection());

        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.erase(widgetId, contentSelection)
        );

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void markAsCurrentPrevious(
            final int widgetId) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            QuotationEntity previousQuotation = getPreviousQuotation(
                    widgetId,
                    getContentPreferences(widgetId).getContentSelection(),
                    getCurrentQuotation(widgetId).digest,
                    getContentPreferences(widgetId).getContentSelectionAllExclusion());

            databaseRepository.markAsCurrent(widgetId, previousQuotation.digest);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void markAsCurrentLastPrevious(final int widgetId) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            databaseRepository.markAsCurrent(
                    widgetId,
                    getLastPreviousDigest(widgetId));
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    public Single<Integer> countAllMinusExclusions(final int widgetId) {
        final Future<Single<Integer>> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.countAllMinusExclusions(
                        getContentPreferences(widgetId).getContentSelectionAllExclusion()));

        Single<Integer> countAllMinusExclusions = Single.just(0);

        try {
            countAllMinusExclusions = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
        return countAllMinusExclusions;
    }

    @NonNull
    public boolean externalDatabaseContainsQuotations() {
        final Future<Boolean> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            if (databaseRepository.countAllExternal().blockingGet() > 0) {
                return true;
            }

            return false;
        });

        boolean externalDatabaseContainsQuotations = false;

        try {
            externalDatabaseContainsQuotations = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return externalDatabaseContainsQuotations;
    }

    @NonNull
    public Single<List<Integer>> authorsQuotationCount() {
        final Future<Single<List<Integer>>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.getAuthorsQuotationCount());

        Single<List<Integer>> authorsQuotationCount = null;

        try {
            authorsQuotationCount = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return authorsQuotationCount;
    }

    @NonNull
    public List<Integer> authorsQuotationCountAsList() {
        final Future<List<Integer>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.getAuthorsQuotationCount().blockingGet());

        List<Integer> authorsQuotationCount = null;

        try {
            authorsQuotationCount = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return authorsQuotationCount;
    }

    @NonNull
    public Single<List<AuthorPOJO>> authors(int authorCount) {
        final Future<Single<List<AuthorPOJO>>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.getAuthorsAndQuotationCounts((authorCount == -1) ? 1 : authorCount));

        Single<List<AuthorPOJO>> authors = null;

        try {
            authors = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return authors;
    }

    @NonNull
    public List<AuthorPOJO> authorsAsList(int authorCount) {
        final Future<List<AuthorPOJO>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> {
            Single<List<AuthorPOJO>> list
                    = databaseRepository.getAuthorsAndQuotationCounts((authorCount == -1) ? 1 : authorCount);
            return list.blockingGet();
        });

        List<AuthorPOJO> authors = null;

        try {
            authors = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return authors;
    }

    @NonNull
    public List<String> authorsSorted(@NonNull final List<AuthorPOJO> unsortedAuthorPOJOList) {
        cachedAuthorPOJOList = unsortedAuthorPOJOList;

        Collections.sort(unsortedAuthorPOJOList);
        final ArrayList<String> authors = new ArrayList<>();
        for (final AuthorPOJO authorPOJO : unsortedAuthorPOJOList) {
            authors.add(authorPOJO.author);
        }
        return authors;
    }

    public int countAuthorQuotations(@NonNull final String author) {
        int countAuthorQuotations = 0;

        for (final AuthorPOJO authorPOJO : cachedAuthorPOJOList) {
            if (authorPOJO.author.equals(author)) {
                countAuthorQuotations = authorPOJO.count;
                break;
            }
        }
        return countAuthorQuotations;
    }

    public int authorsIndex(@NonNull final String author) {
        int index = 0;
        for (final AuthorPOJO authorPOJO : cachedAuthorPOJOList) {
            if (authorPOJO.author.equals(author)) {
                break;
            } else {
                index++;
            }
        }
        return index;
    }

    @NonNull
    public Single<Integer> countFavourites() {
        return databaseRepository.countFavourites();
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotations(@NonNull final String text, boolean favouritesOnly) {
        final Future<List<QuotationEntity>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.getSearchQuotations(text, favouritesOnly));

        List<QuotationEntity> searchResultsList = new ArrayList<>();

        try {
            searchResultsList = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return searchResultsList;
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotationsRegEx(@NonNull final String text, boolean favouritesOnly) {
        final Future<List<QuotationEntity>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.getSearchQuotationsRegEx(text, favouritesOnly));

        List<QuotationEntity> searchResultsList = new ArrayList<>();

        try {
            searchResultsList = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return searchResultsList;
    }

    @NonNull
    public Integer countQuotationWithSearchRegEx(@NonNull final String regEx, boolean favouritesOnly) {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.countSearchTextRegEx(regEx, favouritesOnly));

        Integer searchCount = 0;

        try {
            searchCount = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return searchCount;
    }

    @NonNull
    public Integer countQuotationWithSearchText(@NonNull final String searchText, boolean favouritesOnly) {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.countSearchText(searchText, favouritesOnly));

        Integer searchCount = 0;

        try {
            searchCount = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return searchCount;
    }

    @NonNull
    public String transferBackup(@NonNull final Context context) {
        final Future<String> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            TransferBackup transferBackup = new TransferBackup(context);
            Transfer transfer = transferBackup.transfer(databaseRepository);
            return transferBackup.asJson(transfer);
        });

        String transferJson = "";

        try {
            transferJson = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return transferJson;
    }

    @NonNull
    public List<QuotationEntity> getFavourites() {
        final Future<List<QuotationEntity>> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> {
            List<FavouriteEntity> favouritesDigestList = databaseRepository.getFavourites();
            Collections.reverse(favouritesDigestList);

            List<QuotationEntity> favouriteQuotationsList = new ArrayList<>();
            for (FavouriteEntity favourite : favouritesDigestList) {
                favouriteQuotationsList.add(databaseRepository.getQuotation(favourite.digest));
            }

            return favouriteQuotationsList;
        });

        List<QuotationEntity> favourites = new ArrayList<>();

        try {
            favourites = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return favourites;
    }

    @Nullable
    public List<QuotationEntity> exportFavourites() {
        final Future<ArrayList<QuotationEntity>> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final ArrayList<QuotationEntity> exportedFavourites = new ArrayList<>();

            for (final String favouriteDigest : databaseRepository.getFavouritesDigests()) {
                final QuotationEntity quotationEntity
                        = databaseRepository.getQuotation(favouriteDigest);

                if (quotationEntity != null) {
                    exportedFavourites.add(quotationEntity);
                } else {
                    Timber.w("misaligned:%s", favouriteDigest);
                }
            }

            return exportedFavourites;
        });

        ArrayList<QuotationEntity> exportedFavourites = null;

        try {
            exportedFavourites = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }

        return exportedFavourites;
    }

    public void insertQuotationsExternal(
            @NonNull final LinkedHashSet<QuotationEntity> quotations) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            DatabaseRepository.useInternalDatabase = false;
            databaseRepository.erase();
            databaseRepository.insertQuotationsExternal(quotations);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    public List<QuotationEntity> getQuotationsForAuthor(@NonNull final String author) {
        final Future<ArrayList<QuotationEntity>> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.getQuotationsForAuthor(author));

        ArrayList<QuotationEntity> quotationsForAuthor = null;

        try {
            quotationsForAuthor = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return quotationsForAuthor;
    }

    public void insertQuotationExternal(int widgetId, QuotationEntity quotation) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            DatabaseRepository.useInternalDatabase = false;
            databaseRepository.insertQuotationExternal(quotation);

            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

            if (databaseRepository.findPositionInPrevious(widgetId, quotationsPreferences) + 1
                    == databaseRepository.countNext(quotationsPreferences)) {
                // as a courtesy, move to latest quotation if user was at adjacent previous
                markAsCurrentNext(widgetId, false);
            }
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public ScraperData getWebPage(
            @NonNull final Context context,
            @NonNull final String url,
            @NonNull final String xpathQuotation,
            @NonNull final String xpathSource
    ) {
        final Future<ScraperData> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

            try {
                Scraper scraper = new Scraper();
                Document document = scraper.getDocumentFromUrl(url);

                return new ScraperData(
                        true,
                        scraper.getQuotation(document, xpathQuotation),
                        scraper.getSource(document, xpathSource)
                );
            } catch (ScraperUrlException e) {
                handler.post(() -> Toast.makeText(
                        context,
                        context.getString(R.string.fragment_quotations_database_scrape_endpoint_error),
                        Toast.LENGTH_SHORT).show());
            } catch (ScraperQuotationException e) {
                handler.post(() -> Toast.makeText(
                        context,
                        context.getString(R.string.fragment_quotations_database_scrape_xpath_error_quotation),
                        Toast.LENGTH_SHORT).show());
            } catch (ScraperSourceException e) {
                handler.post(() -> Toast.makeText(
                        context,
                        context.getString(R.string.fragment_quotations_database_scrape_xpath_error_source),
                        Toast.LENGTH_SHORT).show());
            }

            return new ScraperData();
        });

        try {
            return future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return new ScraperData();
    }

    public void insertWebPage(
            final int widgetId,
            @NonNull final String quotation,
            @NonNull final String source,
            @NonNull final String digest
    ) {
        Timber.d("scraper: %s; %s; %s", digest, source, quotation);

        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            if (digest.equals(ImportHelper.DEFAULT_DIGEST)) {
                DatabaseRepository.useInternalDatabase = false;
                databaseRepository.erase();
            }

            insertQuotationExternal(
                    widgetId,
                    new QuotationEntity(
                            digest,
                            "?",
                            source,
                            quotation));
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public String getPosition(int widgetId, String digest) {
        final Future<String> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

            String quotationPosition = getCurrentPosition(
                    widgetId,
                    quotationsPreferences);

            if (digest.equals(getLastPreviousDigest(widgetId))) {
                quotationPosition = "\u2316  " + quotationPosition + " ";
            }

            return quotationPosition;
        });

        try {
            return future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return "";
    }
}
