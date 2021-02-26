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
    public final ExecutorService executorService = Executors.newFixedThreadPool(6);
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

    public void setDefault(final int widgetId) {
        logWidgetId(widgetId);

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

    private void logWidgetId(final int widgetId) {
        Timber.d("widgetId=%d", widgetId);
    }

    public void setNext(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            final boolean randomNext)
            throws NoNextQuotationAvailableException {
        final String logMsg = String.format(Locale.ENGLISH, "%d: contentType=%d", widgetId, contentSelection.getContentType());
        Timber.d(logMsg);

        final Future<Boolean> future = executorService.submit(() -> {
            try {
                QuotationEntity quotationEntity = null;

                switch (contentSelection) {
                    case ALL:
                    case FAVOURITES:
                        quotationEntity = databaseRepository.getNext(
                                widgetId, contentSelection, null, randomNext);
                        break;
                    case AUTHOR:
                        quotationEntity = databaseRepository.getNext(
                                widgetId, contentSelection,
                                new ContentPreferences(widgetId, context).getContentSelectionAuthorName(),
                                randomNext);
                        break;
                    case SEARCH:
                        quotationEntity = databaseRepository.getNext(
                                widgetId, contentSelection,
                                new ContentPreferences(widgetId, context).getContentSelectionSearchText(),
                                randomNext);
                        break;
                    default:
                        // REPORT:
                        break;
                }

                databaseRepository.markAsPrevious(widgetId, contentSelection, quotationEntity.digest);
                return true;
            } catch (NoNextQuotationAvailableException e) {
                return false;
            }
        });

        try {
            if (Boolean.FALSE.equals(future.get())) {
                throw new NoNextQuotationAvailableException(contentSelection);
            }
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    @Nullable
    public QuotationEntity getNext(
            final int widgetId,
            @NonNull final ContentSelection contentSelection) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: contentType=%s", widgetId, contentSelection.getContentType());
        Timber.d(logMsg);

        final Future<QuotationEntity> future = executorService.submit(() -> {
            // check for first time use
            try {
                switch (contentSelection) {
                    case AUTHOR:
                        getNextAuthor(widgetId, contentSelection);
                        break;

                    case FAVOURITES:
                        getNextFavourite(widgetId, contentSelection, countPrevious(widgetId, contentSelection) == 0, false);
                        break;

                    case SEARCH:
                        if (countPreviousSearch(widgetId) == 0) {
                            databaseRepository.deletePrevious(widgetId, contentSelection);
                            setNext(widgetId, contentSelection, false);
                        }
                        break;

                    default:
                        // ALL
                        if (countPrevious(widgetId, contentSelection) == 0) {
                            setDefault(widgetId);
                        }
                        break;
                }
            } catch (NoNextQuotationAvailableException e) {
                Timber.d(e);
            }

            return databaseRepository.getNext(widgetId, contentSelection);
        });

        QuotationEntity quotationEntity = null;
        try {
            quotationEntity = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    private void getNextFavourite(int widgetId, @NonNull ContentSelection contentSelection, boolean b, boolean b2) throws NoNextQuotationAvailableException {
        if (b) {
            setNext(widgetId, contentSelection, b2);
        }
    }

    private void getNextAuthor(int widgetId, @NonNull ContentSelection contentSelection) throws NoNextQuotationAvailableException {
        if (countPreviousAuthor(widgetId) == 0) {
            databaseRepository.deletePrevious(widgetId, contentSelection);
            setNext(widgetId, contentSelection, false);
        }
    }

    public int countPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return databaseRepository.countPrevious(widgetId, contentSelection);
    }

    public int countPrevious(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.ALL)
                + databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR)
                + databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH);
    }

    public int countPreviousAuthor(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR,
                new ContentPreferences(widgetId, context).getContentSelectionAuthorName());
    }

    public int countPreviousSearch(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH,
                new ContentPreferences(widgetId, context).getContentSelectionSearchText());
    }

    public void toggleFavourite(final int widgetId, @NonNull final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: digest=%s", widgetId, digest);
        Timber.d(logMsg);

        final Future future = executorService.submit(() -> {
            if (!isFavourite(widgetId, digest)) {
                databaseRepository.markAsFavourite(digest);

                QuotationEntity quotationEntity = getNext(
                        widgetId,
                        selectedContentType(widgetId));

                ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                properties.put("Favourite",
                        "digest=" + digest + "; author=" + quotationEntity.author + "; quotation=" + quotationEntity.quotation);
                AuditEventHelper.auditEvent("FAVOURITE", properties);
            } else {
                databaseRepository.deleteFavourite(widgetId, digest);
                try {
                    if (databaseRepository.countFavourites().blockingGet() == 0) {
                        getNextFavourite(widgetId, ContentSelection.ALL, selectedContentTypeIsFavourite(widgetId), true);
                    } else {
                        setNext(widgetId, ContentSelection.FAVOURITES, true);
                    }
                } catch (NoNextQuotationAvailableException e) {
                    Timber.d(e);
                }
            }
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public boolean selectedContentTypeIsFavourite(final int widgetId) {
        return selectedContentType(widgetId).equals(ContentSelection.FAVOURITES);
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
        logWidgetId(widgetId);

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

    public void reportQuotation(final int widgetId) {
        logWidgetId(widgetId);

        final Future future = executorService.submit(() -> {
            List<String> previousQuotations = databaseRepository.getPrevious(
                    widgetId, selectedContentType(widgetId));

            databaseRepository.markAsReported(previousQuotations.get(0));
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public ContentSelection selectedContentType(final int widgetId) {
        return new ContentPreferences(widgetId, context).getContentSelection();
    }

    public boolean isReported(final int widgetId) {
        logWidgetId(widgetId);

        final Future<Integer> future = executorService.submit(() -> {
            QuotationEntity quotationEntity = getNext(
                    widgetId,
                    selectedContentType(widgetId));

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
