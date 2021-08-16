package com.github.jameshnsears.quoteunquote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.SaveRequest;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public QuoteUnquoteModel(@NonNull Context widgetContext) {
        this.context = widgetContext;
        this.databaseRepository = DatabaseRepository.getInstance(context);
    }

    private boolean isNextNew(
            int widgetId, @NonNull final ContentPreferences contentPreferences, boolean randomNext) {
        final int availableQuotations = this.databaseRepository.countNext(contentPreferences);

        if (this.databaseRepository.positionInPrevious(widgetId, contentPreferences) == availableQuotations) {
            return false;
        }

        return !randomNext
                || this.databaseRepository.countPrevious(widgetId, contentPreferences.getContentSelection())
                != availableQuotations;
    }

    @NonNull
    public ContentPreferences getContentPreferences(final int widgetId) {
        return new ContentPreferences(widgetId, this.context);
    }

    @Nullable
    public QuotationEntity getPreviousDigests(
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @NonNull String digest) {

        Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {

            final List<String> previousDigests = this.getPreviousDigests(widgetId, contentSelection);

            int priorDigestIndex = previousDigests.indexOf(digest) + 1;
            if (priorDigestIndex == previousDigests.size()) {
                priorDigestIndex -= 1;
            }

            return this.databaseRepository.getQuotation(previousDigests.get(priorDigestIndex));
        });

        QuotationEntity quotationEntity = null;

        try {
            quotationEntity = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    @Nullable
    public QuotationEntity getCurrentQuotation(
            int widgetId) {

        Future<QuotationEntity> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                this.databaseRepository.getCurrentQuotation(widgetId));

        QuotationEntity quotationEntity = null;

        try {
            quotationEntity = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    @Nullable
    public String getCurrentPosition(
            int widgetId,
            @NonNull ContentPreferences contentPreferences) {
        Future<String> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            int positionInPrevious = this.databaseRepository.positionInPrevious(widgetId, contentPreferences);

            if (positionInPrevious == 0) {
                positionInPrevious += 1;
            }

            return String.format("@ %d/%d",
                    positionInPrevious,
                    this.databaseRepository.countNext(contentPreferences));
        });

        String currentPosition = null;

        try {
            currentPosition = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        Timber.d("%s; %s", contentPreferences.toString(), currentPosition);

        return currentPosition;
    }

    public void markAsCurrentNext(
            int widgetId,
            boolean randomNext) {
        Timber.d("%b", randomNext);

        ContentPreferences contentPreferences = this.getContentPreferences(widgetId);

        Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            QuotationEntity nextQuotation = this.getNextQuotation(widgetId, randomNext);

            final List<String> previous = this.getPreviousDigests(widgetId, contentPreferences.getContentSelection());

            if (!previous.contains(nextQuotation.digest)) {
                this.databaseRepository.markAsPrevious(
                        widgetId,
                        contentPreferences.getContentSelection(),
                        nextQuotation.digest);
            } else {
                if (!this.isNextNew(widgetId, contentPreferences, randomNext) && randomNext) {
                    nextQuotation = this.databaseRepository.getQuotation(previous.get(0));
                }
            }

            this.addToPreviousAll(widgetId, nextQuotation);

            this.databaseRepository.markAsCurrent(
                    widgetId,
                    nextQuotation.digest);
        });

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    private void addToPreviousAll(int widgetId, @NonNull QuotationEntity quotationEntity) {
        if (this.getContentPreferences(widgetId).getContentAddToPreviousAll()) {
            if (!this.getPreviousDigests(widgetId, ContentSelection.ALL).contains(quotationEntity.digest)) {
                this.databaseRepository.markAsPrevious(
                        widgetId,
                        ContentSelection.ALL,
                        quotationEntity.digest);
            }
        }
    }

    @NonNull
    public QuotationEntity getNextQuotation(
            int widgetId,
            boolean randomNext) {
        final QuotationEntity nextQuotation;

        switch (this.getContentPreferences(widgetId).getContentSelection()) {
            case FAVOURITES:
                nextQuotation = this.databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.FAVOURITES,
                        null,
                        randomNext);
                break;
            case AUTHOR:
                nextQuotation = this.databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.AUTHOR,
                        this.getContentPreferences(widgetId).getContentSelectionAuthor(),
                        randomNext);
                break;
            case SEARCH:
                nextQuotation = this.databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.SEARCH,
                        this.getContentPreferences(widgetId).getContentSelectionSearch(),
                        randomNext);
                break;
            default:
                nextQuotation = this.databaseRepository.getNextQuotation(
                        widgetId,
                        ContentSelection.ALL,
                        null,
                        randomNext);
                break;
        }

        return nextQuotation;
    }

    public void markAsCurrentDefault(
            int widgetId) {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final ContentSelection contentSelection = this.getContentPreferences(widgetId).getContentSelection();

            this.setDefault(widgetId, contentSelection);

            final QuotationEntity quotationEntity
                    = this.databaseRepository.getNextQuotation(widgetId, contentSelection);

            this.addToPreviousAll(widgetId, quotationEntity);

            this.databaseRepository.markAsCurrent(
                    widgetId,
                    quotationEntity.digest);
        });

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    private void setDefault(
            int widgetId, @NonNull ContentSelection contentSelection) {
        switch (contentSelection) {
            case AUTHOR:
                this.setDefaultAuthor(widgetId);
                break;

            case FAVOURITES:
                this.setDefaultFavourite(widgetId);
                break;

            case SEARCH:
                this.setDefaultSearch(widgetId);
                break;

            default:
                this.setDefaultAll(widgetId);
                break;
        }
    }

    public List<String> getPreviousDigests(
            int widgetId,
            @NonNull ContentSelection contentSelection) {
        Future<List<String>> future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                this.databaseRepository.getPreviousDigests(widgetId, contentSelection));

        List<String> allPreviousDigests = new ArrayList<>();

        try {
            allPreviousDigests = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        Timber.d("%d", allPreviousDigests.size());

        return allPreviousDigests;
    }

    private void setDefaultAuthor(final int widgetId) {
        if (this.countPreviousAuthor(widgetId) == 0) {
            this.databaseRepository.erase(widgetId, ContentSelection.AUTHOR);
            this.markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultFavourite(final int widgetId) {
        if (this.countPrevious(widgetId, ContentSelection.FAVOURITES) == 0) {
            this.markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultSearch(final int widgetId) {
        if (this.countPreviousSearch(widgetId) == 0) {
            this.databaseRepository.erase(widgetId, ContentSelection.SEARCH);
            this.markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultAll(int widgetId) {
        if (this.countPrevious(widgetId, ContentSelection.ALL) == 0) {
            this.databaseRepository.markAsPrevious(
                    widgetId, ContentSelection.ALL, DatabaseRepository.getDefaultQuotationDigest());
        }
    }

    public int countPrevious(int widgetId,
                             @NonNull ContentSelection contentSelection) {
        return this.databaseRepository.countPrevious(widgetId, contentSelection);
    }

    public int countPreviousAuthor(int widgetId) {
        return this.databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR,
                this.getContentPreferences(widgetId).getContentSelectionAuthor());
    }

    public int countPreviousSearch(int widgetId) {
        return this.databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH,
                this.getContentPreferences(widgetId).getContentSelectionSearch());
    }

    public int countFavouritesWithoutRx() {
        Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            return this.databaseRepository.countFavourites().blockingGet();
        });

        int favouritesCount = 0;
        try {
            favouritesCount = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return favouritesCount;
    }

    public int toggleFavourite(int widgetId, @NonNull String digest) {
        Future<Integer> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final List<String> favourites = this.databaseRepository.getFavourites();

            Timber.d("%d", favourites.size());

            final boolean isFavourite = favourites.contains(digest);

            Timber.d("digest=%s; %b", digest, isFavourite);

            if (!isFavourite) {
                this.databaseRepository.markAsFavourite(digest);
                this.auditFavourite(widgetId, digest);
            } else {
                this.databaseRepository.eraseFavourite(widgetId, digest);
            }

            return this.databaseRepository.countFavourites().blockingGet();
        });

        int favouritesCount = 0;
        try {
            favouritesCount = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        return favouritesCount;
    }

    private void auditFavourite(final int widgetId, @NonNull final String digest) {
        final QuotationEntity quotationEntity = this.getCurrentQuotation(
                widgetId);

        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        properties.put("Favourite",
                "digest=" + digest + "; author=" + quotationEntity.author + "; quotation=" + quotationEntity.quotation);
        AuditEventHelper.auditEvent("FAVOURITE", properties);
    }

    public boolean isFavourite(@NonNull String digest) {
        Future<Boolean> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> this.databaseRepository.isFavourite(digest));

        boolean isFavourite = false;

        try {
            isFavourite = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        Timber.d("%b", isFavourite);
        return isFavourite;
    }

    public void delete(int widgetId) {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                this.databaseRepository.erase(widgetId)
        );

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void disable() {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                this.databaseRepository.erase());

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void resetPrevious(int widgetId, @NonNull ContentSelection contentSelection) {
        Timber.d("contentSelection=%d", contentSelection.getContentSelection());

        Future future = QuoteUnquoteWidget.getExecutorService().submit(() ->
                this.databaseRepository.erase(widgetId, contentSelection)
        );

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void markAsReported(int widgetId) {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final List<String> previousQuotations = this.databaseRepository.getPreviousDigests(
                    widgetId, this.getContentPreferences(widgetId).getContentSelection());

            this.databaseRepository.markAsReported(previousQuotations.get(0));
        });

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public void markAsCurrentPrevious(
            int widgetId) {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final QuotationEntity previousQuotation = this.getPreviousDigests(
                    widgetId,
                    this.getContentPreferences(widgetId).getContentSelection(),
                    this.getCurrentQuotation(widgetId).digest);

            this.databaseRepository.markAsCurrent(widgetId, previousQuotation.digest);
        });

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    public boolean isReported(int widgetId) {
        Future<Boolean> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final QuotationEntity quotationEntity = this.getCurrentQuotation(
                    widgetId);

            return this.databaseRepository.isReported(quotationEntity.digest);
        });

        boolean isReported = false;
        try {
            isReported = future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
        Timber.d("%b", isReported);

        return isReported;
    }

    @NonNull
    public Single<Integer> countAll() {
        return this.databaseRepository.countAll();
    }

    @NonNull
    public Single<List<AuthorPOJO>> authors() {
        return this.databaseRepository.getAuthorsAndQuotationCounts();
    }

    @NonNull
    public List<String> authorsSorted(@NonNull List<AuthorPOJO> unsortedAuthorPOJOList) {
        cachedAuthorPOJOList = unsortedAuthorPOJOList;

        Collections.sort(unsortedAuthorPOJOList);
        ArrayList<String> authors = new ArrayList<>();
        for (AuthorPOJO authorPOJO : unsortedAuthorPOJOList) {
            authors.add(authorPOJO.author);
        }
        return authors;
    }

    public int countAuthorQuotations(@NonNull String author) {
        int countAuthorQuotations = 0;

        for (AuthorPOJO authorPOJO : cachedAuthorPOJOList) {
            if (authorPOJO.author.equals(author)) {
                countAuthorQuotations = authorPOJO.count;
                break;
            }
        }
        return countAuthorQuotations;
    }

    public int authorsIndex(@NonNull String author) {
        int index = 0;
        for (AuthorPOJO authorPOJO : cachedAuthorPOJOList) {
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
        return this.databaseRepository.countFavourites();
    }

    @NonNull
    public Integer countQuotationWithSearchText(@NonNull String text) {
        return this.databaseRepository.countSearchText(text);
    }

    @NonNull
    public String getFavouritesToSend(@NonNull Context context) {
        Future<String> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            final SaveRequest saveRequest = new SaveRequest();

            saveRequest.code = this.localCode(context);
            saveRequest.digests = this.databaseRepository.getFavourites();

            return CloudFavouritesHelper.jsonSendRequest(saveRequest);
        });

        try {
            return future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
            return "";
        }
    }

    @NonNull
    protected String localCode(@NonNull Context context) {
        final ContentPreferences contentPreferences = new ContentPreferences(0, context);
        return contentPreferences.getContentFavouritesLocalCode();
    }

    @Nullable
    public ArrayList<String> exportFavourites() {
        Future<ArrayList<String>> future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            ArrayList<String> exportedFavourites = new ArrayList<String>();

            for (String favouriteDigest : this.databaseRepository.getFavourites()) {
                QuotationEntity quotationEntity
                        = this.databaseRepository.getQuotation(favouriteDigest);

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
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }

        return exportedFavourites;
    }

    public void alignHistoryWithQuotations(int widgetId) {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            List<String> history = new ArrayList<>();
            history.addAll(this.databaseRepository.getPreviousDigests(widgetId, ContentSelection.ALL));
            history.addAll(this.databaseRepository.getPreviousDigests(widgetId, ContentSelection.AUTHOR));
            history.addAll(this.databaseRepository.getPreviousDigests(widgetId, ContentSelection.SEARCH));

            int misalignedCount = 1;
            for (String digest: history) {
                if (this.databaseRepository.getQuotation(digest) == null) {
                    Timber.w("misaligned, previous: %d=%s", misalignedCount, digest);
                    this.databaseRepository.erasePrevious(widgetId, digest);
                    misalignedCount++;
                }
            }
        });

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void alignFavouritesWithQuotations(int widgetId) {
        Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            int misalignedCount = 1;
            for (String digest: this.databaseRepository.getFavourites()) {
                if (this.databaseRepository.getQuotation(digest) == null) {
                    Timber.w("misaligned, favourite: %d=%s", misalignedCount, digest);
                    this.databaseRepository.eraseFavourite(widgetId, digest);
                    misalignedCount++;
                }
            }
        });

        try {
            future.get();
        } catch (@NonNull final ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }
}
