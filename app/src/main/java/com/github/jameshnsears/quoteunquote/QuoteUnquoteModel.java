package com.github.jameshnsears.quoteunquote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import timber.log.Timber;

public class QuoteUnquoteModel {
    @NonNull
    public final ExecutorService executorService = Executors.newFixedThreadPool(4);
    @Nullable
    public DatabaseRepository databaseRepository;
    @Nullable
    protected Context context;

    public QuoteUnquoteModel() {
    }

    public QuoteUnquoteModel(@NonNull final Context widgetContext) {
        context = widgetContext;
        databaseRepository = DatabaseRepository.getInstance(this.context);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void setNextQuotation(
            final int widgetId,
            final boolean randomNext)
            throws NoNextQuotationAvailableException {
        final ContentPreferences contentPreferences = getContentPreferences(widgetId);

        final Future<Boolean> future = executorService.submit(() -> {
            try {
                QuotationEntity quotationEntity = null;

                switch (contentPreferences.getContentSelection()) {
                    case ALL:
                        quotationEntity = databaseRepository.getNextQuotation(
                                widgetId,
                                ContentSelection.ALL,
                                null,
                                randomNext);
                        break;
                    case FAVOURITES:
                        quotationEntity = databaseRepository.getNextQuotation(
                                widgetId,
                                ContentSelection.FAVOURITES,
                                null,
                                randomNext);
                        break;
                    case AUTHOR:
                        quotationEntity = databaseRepository.getNextQuotation(
                                widgetId,
                                ContentSelection.AUTHOR,
                                contentPreferences.getContentSelectionAuthorName(),
                                randomNext);
                        break;
                    case SEARCH:
                        quotationEntity = databaseRepository.getNextQuotation(
                                widgetId,
                                ContentSelection.SEARCH,
                                contentPreferences.getContentSelectionSearchText(),
                                randomNext);
                        break;
                    default:
                        // REPORT:
                        break;
                }

                databaseRepository.markAsPrevious(
                        widgetId,
                        contentPreferences.getContentSelection(),
                        quotationEntity.digest);
                return true;
            } catch (NoNextQuotationAvailableException e) {
                return false;
            }
        });

        try {
            if (Boolean.FALSE.equals(future.get())) {
                throw new NoNextQuotationAvailableException();
            }
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    public ContentPreferences getContentPreferences(int widgetId) {
        return new ContentPreferences(widgetId, context);
    }

    @Nullable
    public QuotationEntity getNextQuotation(
            final int widgetId,
            @NonNull final ContentSelection contentSelection) {

        final Future<QuotationEntity> future = executorService.submit(() -> {
            // check for first time use
            String criteria = null;
            try {
                switch (contentSelection) {
                    case AUTHOR:
                        setDefaultAuthor(widgetId);
                        criteria = new ContentPreferences(widgetId, context).getContentSelectionAuthorName();
                        break;

                    case FAVOURITES:
                        setDefaultFavourite(widgetId);
                        break;

                    case SEARCH:
                        setDefaultSearch(widgetId);
                        criteria = new ContentPreferences(widgetId, context).getContentSelectionSearchText();
                        break;

                    default:
                        setDefaultAll(widgetId);
                        break;
                }
            } catch (NoNextQuotationAvailableException e) {
                Timber.d(e);
            }

            QuotationEntity quotationEntity = databaseRepository.getNextQuotation(widgetId, contentSelection);
            String previousCounts = databaseRepository.getPreviousCounts(widgetId, contentSelection, criteria);

            return new QuotationEntity(
                    quotationEntity.digest,
                    quotationEntity.author + " " + previousCounts,
                    quotationEntity.quotation);
        });

        QuotationEntity quotationEntity = null;
        try {
            quotationEntity = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    private void setDefaultAuthor(int widgetId) throws NoNextQuotationAvailableException {
        if (countPreviousAuthor(widgetId) == 0) {
            databaseRepository.deletePrevious(widgetId, ContentSelection.AUTHOR);
            setNextQuotation(widgetId, false);
        }
    }

    private void setDefaultFavourite(int widgetId) throws NoNextQuotationAvailableException {
        if (countPrevious(widgetId, ContentSelection.FAVOURITES) == 0) {
            setNextQuotation(widgetId, false);
        }
    }

    private void setDefaultSearch(int widgetId) throws NoNextQuotationAvailableException {
        if (countPreviousSearch(widgetId) == 0) {
            databaseRepository.deletePrevious(widgetId, ContentSelection.SEARCH);
            setNextQuotation(widgetId, false);
        }
    }

    private void setDefaultAll(int widgetId) {
        if (countPrevious(widgetId, ContentSelection.ALL) == 0) {
            final Future future = executorService.submit(() -> {
                QuotationEntity defaultQuotation = databaseRepository.getQuotation(DatabaseRepository.DEFAULT_QUOTATION_DIGEST);

                databaseRepository.markAsPrevious(widgetId, ContentSelection.ALL, defaultQuotation.digest);
            });

            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                Timber.w(e.toString());
                Thread.currentThread().interrupt();
            }
        }
    }

    public int countPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return databaseRepository.countPrevious(widgetId, contentSelection);
    }

    public int countPreviousAuthor(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR,
                getContentPreferences(widgetId).getContentSelectionAuthorName());
    }

    public int countPreviousSearch(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH,
                getContentPreferences(widgetId).getContentSelectionSearchText());
    }

    public int toggleFavourite(final int widgetId, @NonNull final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: digest=%s", widgetId, digest);
        Timber.d(logMsg);

        final Future<Integer> future = executorService.submit(() -> {
            if (!isFavourite(widgetId, digest)) {
                databaseRepository.markAsFavourite(digest);

                QuotationEntity quotationEntity = getNextQuotation(
                        widgetId,
                        getContentPreferences(widgetId).getContentSelection());

                ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                properties.put("Favourite",
                        "digest=" + digest + "; author=" + quotationEntity.author + "; quotation=" + quotationEntity.quotation);
                AuditEventHelper.auditEvent("FAVOURITE", properties);
            } else {
                databaseRepository.deleteFavourite(widgetId, digest);
            }

            return databaseRepository.countFavourites().blockingGet();
        });

        int favouritesCount = 0;
        try {
            favouritesCount = future.get().intValue();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }

        return favouritesCount;
    }

    public boolean isFavourite(final int widgetId, @NonNull final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: digest=%s", widgetId, digest);

        final Future<Integer> future = executorService.submit(() -> databaseRepository.countFavourite(digest));

        boolean isFavourite = false;
        try {
            if (future.get() == 1) {
                isFavourite = true;
            }
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
        Timber.d(logMsg + "; isFavourite=" + isFavourite);

        return isFavourite;
    }

    public void delete(final int widgetId) {
        final Future future = executorService.submit(() ->
                databaseRepository.deletePrevious(widgetId)
        );

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void disable() {
        final Future future = executorService.submit(() -> {
            databaseRepository.deletePrevious();
            databaseRepository.deleteFavourites();
            databaseRepository.deleteReported();
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void resetPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: contentType=%d", widgetId, contentSelection.getContentType());
        Timber.d(logMsg);

        final Future future = executorService.submit(() ->
                databaseRepository.deletePrevious(widgetId, contentSelection)
        );

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void markAsReported(final int widgetId) {
        final Future future = executorService.submit(() -> {
            List<String> previousQuotations = databaseRepository.getAllPrevious(
                    widgetId, getContentPreferences(widgetId).getContentSelection());

            databaseRepository.markAsReported(previousQuotations.get(0));
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public boolean isReported(final int widgetId) {
        final Future<Integer> future = executorService.submit(() -> {
            QuotationEntity quotationEntity = getNextQuotation(
                    widgetId,
                    getContentPreferences(widgetId).getContentSelection());

            return databaseRepository.countReported(quotationEntity.digest);
        });

        boolean isReported = false;
        try {
            if (future.get() != 0) {
                isReported = true;
            }
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
        Timber.d("isReported=%b", isReported);

        return isReported;
    }
}
