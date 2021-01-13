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
    public final ExecutorService executorService = Executors.newFixedThreadPool(3);
    @Nullable
    public DatabaseRepository databaseRepository;
    @Nullable
    protected Context context;

    public QuoteUnquoteModel() {
    }

    public QuoteUnquoteModel(@NonNull final Context widgetContext) {
        this.context = widgetContext;
        databaseRepository = getDatabaseRepository();
    }

    public DatabaseRepository getDatabaseRepository() {
        return DatabaseRepository.getInstance(this.context);
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void setDefaultQuotation(final int widgetId) {
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
                                widgetId, contentSelection, getPreferencesAuthorSearch(widgetId), randomNext);
                        break;
                    case SEARCH:
                        quotationEntity = databaseRepository.getNext(
                                widgetId, contentSelection, getPreferencesTextSearch(widgetId), randomNext);
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
            if (!future.get()) {
                throw new NoNextQuotationAvailableException(contentSelection);
            }
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    public String getPreferencesAuthorSearch(final int widgetId) {
        return new ContentPreferences(widgetId, context).getContentSelectionAuthorName();
    }

    public String getPreferencesTextSearch(final int widgetId) {
        return new ContentPreferences(widgetId, context).getContentSelectionSearchText();
    }

    @NonNull
    public QuotationEntity getNext(
            final int widgetId,
            @NonNull final ContentSelection contentSelection) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: contentType=%s", widgetId, contentSelection.getContentType());
        Timber.d(logMsg);

        final Future<QuotationEntity> future = executorService.submit(() -> {
            // check for first time use
            try {
                switch (contentSelection) {
                    case ALL:
                        if (countPrevious(widgetId, contentSelection) == 0) {
                            setDefaultQuotation(widgetId);
                        }
                        break;

                    case AUTHOR:
                        if (countPreviousAuthor(widgetId) == 0) {
                            databaseRepository.deletePrevious(widgetId, contentSelection);
                            setNext(widgetId, contentSelection, false);
                        }
                        break;

                    case FAVOURITES:
                        if (countPrevious(widgetId, contentSelection) == 0) {
                            setNext(widgetId, contentSelection, false);
                        }
                        break;

                    case SEARCH:
                        if (countPreviousQuotationText(widgetId) == 0) {
                            databaseRepository.deletePrevious(widgetId, contentSelection);
                            setNext(widgetId, contentSelection, false);
                        }
                        break;

                    default:
                        Timber.e(contentSelection.getContentType().toString());
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

    public int countPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        return databaseRepository.countPrevious(widgetId, contentSelection);
    }

    public int countPreviousAuthor(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.AUTHOR, getPreferencesAuthorSearch(widgetId));
    }

    public int countPreviousQuotationText(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentSelection.SEARCH, getPreferencesTextSearch(widgetId));
    }

    public void toggleFavourite(final int widgetId, @NonNull final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: digest=%s", widgetId, digest);
        Timber.d(logMsg);

        final Future future = executorService.submit(() -> {
            if (!isFavourite(widgetId, digest)) {
                databaseRepository.markAsFavourite(digest);

                QuotationEntity quotationEntity = getNext(
                        widgetId,
                        getSelectedContentType(widgetId));

                ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                properties.put("Favourite",
                        "digest=" + digest + "; author=" + quotationEntity.author + "; quotation=" + quotationEntity.quotation);
                AuditEventHelper.auditEvent("FAVOURITE", properties);
            } else {
                databaseRepository.deleteFavourite(widgetId, digest);
                try {
                    if (databaseRepository.countFavourites().blockingGet() == 0) {
                        if (isRadioButtonFavouriteSelected(widgetId)) {
                            setNext(widgetId, ContentSelection.ALL, true);
                        }
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

    public boolean isRadioButtonFavouriteSelected(final int widgetId) {
        return new ContentPreferences(widgetId, context).getContentSelection().equals(ContentSelection.FAVOURITES);
    }

    public boolean isFavourite(final int widgetId, @NonNull final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: digest=%s", widgetId, digest);

        final Future<Integer> future = executorService.submit(() -> databaseRepository.countIsFavourite(digest));

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

    public void removeDatabaseEntriesForInstance(final int widgetId) {
        logWidgetId(widgetId);

        final Future future = executorService.submit(() -> {
            databaseRepository.deletePrevious(widgetId);
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void removeDatabaseEntriesForAllInstances() {
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

    public void deletePrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: contentType=%d", widgetId, contentSelection.getContentType());
        Timber.d(logMsg);

        final Future future = executorService.submit(() -> {
            databaseRepository.deletePrevious(widgetId, contentSelection);
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void markAsReported(final int widgetId) {
        logWidgetId(widgetId);

        final Future future = executorService.submit(() -> {
            List<String> previousQuotations = databaseRepository.getPrevious(
                    widgetId, getSelectedContentType(widgetId));

            databaseRepository.markAsReported(previousQuotations.get(0));
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public ContentSelection getSelectedContentType(final int widgetId) {
        return new ContentPreferences(widgetId, context).getContentSelection();
    }

    public boolean isReported(final int widgetId) {
        logWidgetId(widgetId);

        final Future<Integer> future = executorService.submit(() -> {
            QuotationEntity quotationEntity = getNext(
                    widgetId,
                    getSelectedContentType(widgetId));

            return databaseRepository.countIsReported(quotationEntity.digest);
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
