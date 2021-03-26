package com.github.jameshnsears.quoteunquote;

import android.content.Context;
import android.util.Log;

import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.NoNextQuotationAvailableException;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentType;
import com.github.jameshnsears.quoteunquote.utils.Preferences;
import com.microsoft.appcenter.Flags;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QuoteUnquoteModel {
    private static final String LOG_TAG = QuoteUnquoteModel.class.getSimpleName();

    public final ExecutorService executorService = Executors.newFixedThreadPool(3);
    protected DatabaseRepository databaseRepository;
    protected Context context;

    public QuoteUnquoteModel() {
    }

    public QuoteUnquoteModel(final Context context) {
        Log.d(LOG_TAG, "QuoteUnquoteModel");

        this.context = context;
        databaseRepository = new DatabaseRepository(context);
    }

    public void shutdown() {
        Log.d(LOG_TAG, String.format("%s", new Object() {
        }.getClass().getEnclosingMethod().getName()));

        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void setDefaultQuotation(final int widgetId) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName());
        Log.d(LOG_TAG, logMsg);

        final Future future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);

            databaseRepository.markAsPrevious(
                    widgetId,
                    ContentType.ALL,
                    databaseRepository.getQuotation(DatabaseRepository.DEFAULT_QUOTATION_DIGEST).digest);
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void setNext(final int widgetId, final ContentType contentType)
            throws NoNextQuotationAvailableException {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s: contentType=%d", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                contentType.getContentType());
        Log.d(LOG_TAG, logMsg);

        final Future<Boolean> future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);

            try {
                QuotationEntity quotationEntity = null;

                switch (contentType) {
                    case ALL:
                    case FAVOURITES:
                        quotationEntity = databaseRepository.getNextRandom(
                                widgetId, contentType, null);
                        break;
                    case AUTHOR:
                        quotationEntity = databaseRepository.getNextRandom(
                                widgetId, contentType, getPreferencesAuthorSearch(widgetId));
                        break;
                    case QUOTATION_TEXT:
                        quotationEntity = databaseRepository.getNextRandom(
                                widgetId, contentType, getPreferencesTextSearch(widgetId));
                        break;
                    case REPORT:
                        break;
                }

                databaseRepository.markAsPrevious(widgetId, contentType, quotationEntity.digest);
                return true;
            } catch (NoNextQuotationAvailableException e) {
                return false;
            }
        });

        try {
            if (!future.get()) {
                throw new NoNextQuotationAvailableException(contentType);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public String getPreferencesAuthorSearch(final int widgetId) {
        final Preferences preferences = new Preferences(widgetId, context);
        return preferences.getSharedPreferenceString(Preferences.FRAGMENT_CONTENT, Preferences.SPINNER_AUTHORS);
    }

    public String getPreferencesTextSearch(final int widgetId) {
        final Preferences preferences = new Preferences(widgetId, context);
        return preferences.getSharedPreferenceString(Preferences.FRAGMENT_CONTENT, Preferences.EDIT_TEXT_KEYWORDS);
    }

    public QuotationEntity getNext(final int widgetId, final ContentType contentType) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s: contentType=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                contentType.getContentType());
        Log.d(LOG_TAG, logMsg);

        final Future<QuotationEntity> future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);

            // check for first time use
            try {
                switch (contentType) {
                    case ALL:
                        if (countPrevious(widgetId, contentType) == 0) {
                            setDefaultQuotation(widgetId);
                        }
                        break;

                    case AUTHOR:
                        if (countPreviousAuthor(widgetId) == 0) {
                            databaseRepository.deletePrevious(widgetId, contentType);
                            setNext(widgetId, contentType);
                        }
                        break;

                    case FAVOURITES:
                        if (countPrevious(widgetId, contentType) == 0) {
                            setNext(widgetId, contentType);
                        }
                        break;

                    case QUOTATION_TEXT:
                        if (countPreviousQuotationText(widgetId) == 0) {
                            databaseRepository.deletePrevious(widgetId, contentType);
                            setNext(widgetId, contentType);
                        }
                        break;

                    default:
                        Log.e(LOG_TAG, contentType.getContentType().toString());
                        break;
                }
            } catch (NoNextQuotationAvailableException e) {
                Log.w(LOG_TAG, e.getMessage());
            }

            return databaseRepository.getNext(widgetId, contentType);
        });

        QuotationEntity quotationEntity = null;
        try {
            quotationEntity = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }

        return quotationEntity;
    }

    public int countPrevious(final int widgetId, final ContentType contentType) {
        return databaseRepository.countPrevious(widgetId, contentType);
    }

    public int countPreviousAuthor(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentType.AUTHOR, getPreferencesAuthorSearch(widgetId));
    }

    public int countPreviousQuotationText(final int widgetId) {
        return databaseRepository.countPrevious(widgetId, ContentType.QUOTATION_TEXT, getPreferencesTextSearch(widgetId));
    }

    public void toggleFavourite(final int widgetId, final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s: digest=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                digest);
        Log.d(LOG_TAG, logMsg);

        final Future future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);

            if (!isFavourite(widgetId, digest)) {
                databaseRepository.markAsFavourite(digest);

                QuotationEntity quotationEntity = getNext(
                        widgetId,
                        getSelectedContentType(widgetId));

                ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                properties.put("Favourite",
                        "digest=" + digest + "; author=" + quotationEntity.author + "; quotation=" + quotationEntity.quotation);
                AuditEventHelper.auditAppCenter(AuditEventHelper.FAVOURITE, properties, Flags.NORMAL);
            } else {
                databaseRepository.deleteFavourite(widgetId, digest);
                try {
                    if (databaseRepository.countFavourites().blockingGet() == 0) {
                        if (isRadioButtonFavouriteSelected(widgetId)) {
                            switchFromFavouriteToAllContent(widgetId);
                            setNext(widgetId, ContentType.ALL);
                        }
                    } else {
                        setNext(widgetId, ContentType.FAVOURITES);
                    }
                } catch (NoNextQuotationAvailableException e) {
                    Log.w(LOG_TAG, e.getMessage());
                }
            }
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRadioButtonFavouriteSelected(final int widgetId) {
        final Preferences preferences = new Preferences(widgetId, context);
        return preferences.getSharedPreferenceBoolean(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES, false);
    }

    public boolean isFavourite(final int widgetId, final String digest) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s: digest=%s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                digest);

        final Future<Integer> future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);
            return databaseRepository.countIsFavourite(digest);
        });

        boolean isFavourite = false;
        try {
            if (future.get() == 1) {
                isFavourite = true;
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
        Log.d(LOG_TAG, logMsg + "; isFavourite=" + isFavourite);

        return isFavourite;
    }

    public void removeDatabaseEntriesForInstance(final int widgetId) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName());
        Log.d(LOG_TAG, logMsg);

        final Future future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);
            databaseRepository.deletePrevious(widgetId);
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void removeDatabaseEntriesForAllInstances() {
        final String logMsg = String.format(Locale.ENGLISH, "%s",
                new Object() {
                }.getClass().getEnclosingMethod().getName());
        Log.d(LOG_TAG, logMsg);

        final Future future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);
            databaseRepository.deletePrevious();
            databaseRepository.deleteFavourites();
            databaseRepository.deleteReported();
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public void deletePrevious(final int widgetId, final ContentType contentType) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s: contentType=%d", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName(),
                contentType.getContentType());
        Log.d(LOG_TAG, logMsg);

        final Future future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);
            databaseRepository.deletePrevious(widgetId, contentType);
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    protected void switchFromFavouriteToAllContent(final int widgetId) {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final Preferences preferences = new Preferences(widgetId, context);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, true);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_AUTHOR, false);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES, false);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT, false);
    }

    public void markAsReported(final int widgetId) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName());

        Log.d(LOG_TAG, logMsg);

        final Future future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);

            List<String> previousQuotations = databaseRepository.getPrevious(
                    widgetId, getSelectedContentType(widgetId));

            databaseRepository.markAsReported(previousQuotations.get(0));
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
    }

    public ContentType getSelectedContentType(final int widgetId) {
        final Preferences preferences = new Preferences(widgetId, context);
        return preferences.getSelectedContentType();
    }

    public boolean isReported(final int widgetId) {
        final String logMsg = String.format(Locale.ENGLISH, "%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName());

        Log.d(LOG_TAG, logMsg);

        final Future<Integer> future = executorService.submit(() -> {
            Thread.currentThread().setName(Thread.currentThread().getId() + ": " + logMsg);

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
            Log.w(LOG_TAG, e.toString());
            Thread.currentThread().interrupt();
        }
        Log.d(LOG_TAG, logMsg + "; isReported=" + isReported);

        return isReported;
    }
}
