package com.github.jameshnsears.quoteunquote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer;
import com.github.jameshnsears.quoteunquote.cloud.transfer.backup.TransferBackup;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;

import java.util.ArrayList;
import java.util.Collections;
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

    public QuoteUnquoteModel(@NonNull final Context widgetContext) {
        context = widgetContext;
        databaseRepository = DatabaseRepository.getInstance(this.context);
    }

    private boolean isNextNew(
            final int widgetId, @NonNull QuotationsPreferences quotationsPreferences, final boolean randomNext) {
        int availableQuotations = databaseRepository.countNext(quotationsPreferences);

        if (databaseRepository.positionInPrevious(widgetId, quotationsPreferences) == availableQuotations) {
            return false;
        }

        return !randomNext
                || databaseRepository.countPrevious(widgetId, quotationsPreferences.getContentSelection())
                != availableQuotations;
    }

    @NonNull
    public QuotationsPreferences getContentPreferences(int widgetId) {
        return new QuotationsPreferences(widgetId, context);
    }

    @Nullable
    public QuotationEntity getPreviousDigests(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String digest) {

        final Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {

            List<String> previousDigests = getPreviousDigests(widgetId, contentSelection);

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
            int positionInPrevious = databaseRepository.positionInPrevious(widgetId, quotationsPreferences);

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

        Timber.d("%s; %s", quotationsPreferences.toString(), currentPosition);

        return currentPosition;
    }

    public void markAsCurrentNext(
            final int widgetId,
            final boolean randomNext) {
        Timber.d("%b", randomNext);

        final QuotationsPreferences quotationsPreferences = getContentPreferences(widgetId);

        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            QuotationEntity nextQuotation = getNextQuotation(widgetId, randomNext);

            List<String> previous = getPreviousDigests(widgetId, quotationsPreferences.getContentSelection());

            if (!previous.contains(nextQuotation.digest)) {
                databaseRepository.markAsPrevious(
                        widgetId,
                        quotationsPreferences.getContentSelection(),
                        nextQuotation.digest);
            } else {
                if (!isNextNew(widgetId, quotationsPreferences, randomNext) && randomNext) {
                    nextQuotation = databaseRepository.getQuotation(previous.get(0));
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
            if (!getPreviousDigests(widgetId, ContentSelection.ALL).contains(quotationEntity.digest)) {
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
        QuotationEntity nextQuotation;

        switch (getContentPreferences(widgetId).getContentSelection()) {
            case FAVOURITES:
                nextQuotation = databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.FAVOURITES,
                        null,
                        randomNext);
                break;
            case AUTHOR:
                nextQuotation = databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.AUTHOR,
                        getContentPreferences(widgetId).getContentSelectionAuthor(),
                        randomNext);
                break;
            case SEARCH:
                nextQuotation = databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.SEARCH,
                        getContentPreferences(widgetId).getContentSelectionSearch(),
                        randomNext);
                break;
            default:
                nextQuotation = databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.ALL,
                        null,
                        randomNext);
                break;
        }

        return nextQuotation;
    }

    public void markAsCurrentDefault(
            final int widgetId) {
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

    private void setDefault(
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
    public List<String> getPreviousDigests(
            final int widgetId,
            @NonNull final ContentSelection contentSelection) {
        final Future<List<String>> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                databaseRepository.getPreviousDigests(widgetId, contentSelection));

        List<String> allPreviousDigests = new ArrayList<>();

        try {
            allPreviousDigests = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        Timber.d("%d", allPreviousDigests.size());

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
        return databaseRepository.countPrevious(widgetId, contentSelection);
    }

    public int countPrevious(final int widgetId) {
        final Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> databaseRepository.countPrevious(widgetId));

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
        return databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR,
                getContentPreferences(widgetId).getContentSelectionAuthor());
    }

    public int countPreviousSearch(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH,
                getContentPreferences(widgetId).getContentSelectionSearch());
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
            List<String> favourites = databaseRepository.getFavouriteDigests();

            if (!favourites.contains(digest)) {
                databaseRepository.markAsFavourite(digest);
                auditFavourite(widgetId, digest);
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

    private void auditFavourite(int widgetId, @NonNull String digest) {
        QuotationEntity quotationEntity = getCurrentQuotation(
                widgetId);

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

        Timber.d("%b", isFavourite);
        return isFavourite;
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
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void markAsCurrentPrevious(
            final int widgetId) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            QuotationEntity previousQuotation = getPreviousDigests(
                    widgetId,
                    getContentPreferences(widgetId).getContentSelection(),
                    getCurrentQuotation(widgetId).digest);

            databaseRepository.markAsCurrent(widgetId, previousQuotation.digest);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    public Single<Integer> countAll() {
        return databaseRepository.countAll();
    }

    @NonNull
    public Single<List<AuthorPOJO>> authors() {
        return databaseRepository.getAuthorsAndQuotationCounts();
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

        Timber.d("index=%s; author=%s", index, author);
        return index;
    }

    @NonNull
    public Single<Integer> countFavourites() {
        return databaseRepository.countFavourites();
    }

    @NonNull
    public Integer countQuotationWithSearchText(@NonNull final String text) {
        return databaseRepository.countSearchText(text);
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

        Timber.d(transferJson);
        return transferJson;
    }

    @Nullable
    public List<String> exportFavourites() {
        final Future<ArrayList<String>> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final ArrayList<String> exportedFavourites = new ArrayList<>();

            for (final String favouriteDigest : databaseRepository.getFavouriteDigests()) {
                final QuotationEntity quotationEntity
                        = databaseRepository.getQuotation(favouriteDigest);

                if (quotationEntity != null) {
                    exportedFavourites.add(
                            quotationEntity.quotation + "\n" + quotationEntity.author + "\n");
                } else {
                    Timber.w("misaligned:%s", favouriteDigest);
                }
            }

            return exportedFavourites;
        });

        ArrayList<String> exportedFavourites = null;

        try {
            exportedFavourites = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }

        return exportedFavourites;
    }
}
