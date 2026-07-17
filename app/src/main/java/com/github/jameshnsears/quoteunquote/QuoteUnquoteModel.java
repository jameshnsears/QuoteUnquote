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
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.db.h.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.db.q.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity;
import com.github.jameshnsears.quoteunquote.scraper.Scraper;
import com.github.jameshnsears.quoteunquote.scraper.ScraperData;
import com.github.jameshnsears.quoteunquote.scraper.ScraperQuotationException;
import com.github.jameshnsears.quoteunquote.scraper.ScraperSourceException;
import com.github.jameshnsears.quoteunquote.scraper.ScraperUrlException;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Single;
import timber.log.Timber;

public class QuoteUnquoteModel {
    private static final int TIMEOUT_SECONDS = 30;
    private static final int TIMEOUT_BACKUP_SECONDS = 60;

    @Nullable
    public DatabaseRepository databaseRepository;

    @Nullable
    public List<AuthorPOJO> cachedAuthorPOJOList;

    @Nullable
    protected Context context;

    protected boolean useInternalDatabase = true;

    public QuoteUnquoteModel() {
    }

    public QuoteUnquoteModel(int widgetId, @NonNull final Context widgetContext) {
        context = widgetContext;
        databaseRepository = DatabaseRepository.getInstance(this.context);

        if (widgetId != -1) {
            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);
            useInternalDatabase = quotationsPreferences.getDatabaseInternal();
        }
    }

    public void setUseInternalDatabase(boolean useInternalDatabase) {
        this.useInternalDatabase = useInternalDatabase;
    }

    private boolean isNextNew(
        final int widgetId, @NonNull QuotationsPreferences quotationsPreferences, final boolean randomNext) {
        int availableQuotations = databaseRepository.countNext(useInternalDatabase, quotationsPreferences);

        if (databaseRepository.findPositionInPrevious(useInternalDatabase, widgetId, quotationsPreferences) == availableQuotations) {
            return false;
        }

        return !randomNext
            || databaseRepository.countPreviousCriteria(useInternalDatabase, widgetId, quotationsPreferences.getContentSelection())
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

        return runTask(() -> {
            List<String> previousDigests = getPreviousDigests(widgetId, contentSelection, criteria);

            if (previousDigests.isEmpty()) {
                return null;
            }

            int priorDigestIndex = previousDigests.indexOf(digest) + 1;
            if (priorDigestIndex == previousDigests.size()) {
                priorDigestIndex -= 1;
            }

            if (priorDigestIndex < 0) {
                return null;
            }

            return databaseRepository.getQuotation(useInternalDatabase, previousDigests.get(priorDigestIndex));
        }, null);
    }

    @Nullable
    public String getLastPreviousDigest(final int widgetId) {
        return runTask(() -> {
            List<String> lastPreviousDigests = databaseRepository.
                getPreviousDigests(
                    useInternalDatabase,
                    widgetId,
                    getContentPreferences(widgetId).getContentSelection(),
                    getContentPreferences(widgetId).getContentSelectionAllExclusion());

            if (lastPreviousDigests.isEmpty()) {
                return "";
            }

            return lastPreviousDigests.get(0);
        }, null);
    }

    @Nullable
    public QuotationEntity getCurrentQuotation(
        final int widgetId) {
        return runTask(() ->
            databaseRepository.getCurrentQuotation(useInternalDatabase, widgetId), null);
    }

    @Nullable
    public String getCurrentPosition(
        final int widgetId,
        @NonNull final QuotationsPreferences quotationsPreferences) {
        return runTask(() -> {
            int positionInPrevious = databaseRepository.findPositionInPrevious(useInternalDatabase, widgetId, quotationsPreferences);

            if (positionInPrevious == 0) {
                positionInPrevious += 1;
            }

            return String.format(Locale.ENGLISH, "%d/%d",
                positionInPrevious,
                databaseRepository.countNext(useInternalDatabase, quotationsPreferences));
        }, null);
    }

    public void markAsCurrentNext(
        final int widgetId,
        final boolean randomNext) {

        final QuotationsPreferences quotationsPreferences = getContentPreferences(widgetId);

        ContentSelection contentSelection = quotationsPreferences.getContentSelection();
        Timber.d("markAsCurrentNext.contentSelection=%s", contentSelection.toString());
        Timber.d("markAsCurrentNext.randomNext=%b", randomNext);

        runAction(() -> {
            QuotationEntity nextQuotation = getNextQuotation(widgetId, randomNext);

            List<String> previous = getPreviousDigests(
                widgetId,
                contentSelection,
                quotationsPreferences.getContentSelectionAllExclusion());

            if (!previous.contains(nextQuotation.digest)) {
                databaseRepository.markAsPrevious(
                    useInternalDatabase,
                    widgetId,
                    contentSelection,
                    nextQuotation.digest);
            } else {
                if (!isNextNew(widgetId, quotationsPreferences, randomNext) && randomNext) {
                    Timber.d("purge Previous");
                    resetPrevious(widgetId, contentSelection);
                    markAsCurrentDefault(widgetId);
                    nextQuotation = getCurrentQuotation(widgetId);
                } else {
                    Timber.d("NO purge Previous");
                }
            }

            addToPreviousAll(widgetId, nextQuotation);

            databaseRepository.markAsCurrent(
                useInternalDatabase,
                widgetId,
                nextQuotation.digest);
        });
    }

    private void addToPreviousAll(final int widgetId, @NonNull final QuotationEntity quotationEntity) {
        if (getContentPreferences(widgetId).getContentAddToPreviousAll()) {
            if (!getPreviousDigests(
                widgetId, ContentSelection.ALL, getContentPreferences(widgetId).getContentSelectionAllExclusion())
                .contains(quotationEntity.digest)) {
                databaseRepository.markAsPrevious(
                    useInternalDatabase,
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
        return runTask(() -> {
            switch (getContentPreferences(widgetId).getContentSelection()) {
                case FAVOURITES:
                    return databaseRepository.getNextQuotation(
                        useInternalDatabase,
                        widgetId,
                        ContentSelection.FAVOURITES,
                        "",
                        randomNext,
                        getContentPreferences(widgetId));
                case AUTHOR:
                    return databaseRepository.getNextQuotation(
                        useInternalDatabase,
                        widgetId,
                        ContentSelection.AUTHOR,
                        getContentPreferences(widgetId).getContentSelectionAuthor(),
                        randomNext,
                        getContentPreferences(widgetId));
                case SEARCH:
                    return databaseRepository.getNextQuotation(
                        useInternalDatabase,
                        widgetId,
                        ContentSelection.SEARCH,
                        getContentPreferences(widgetId).getContentSelectionSearch(),
                        randomNext,
                        getContentPreferences(widgetId));
                default:
                    return databaseRepository.getNextQuotation(
                        useInternalDatabase,
                        widgetId,
                        ContentSelection.ALL,
                        getContentPreferences(widgetId).getContentSelectionAllExclusion(),
                        randomNext,
                        getContentPreferences(widgetId));
            }
        }, null);
    }

    public void markAsCurrent(final int widgetId, @NonNull final String digest) {
        runAction(() -> databaseRepository.markAsCurrent(useInternalDatabase, widgetId, digest));
    }

    public void markAsCurrentDefault(final int widgetId) {
        runAction(() -> {
            ContentSelection contentSelection = getContentPreferences(widgetId).getContentSelection();

            setDefault(widgetId, contentSelection);

            QuotationEntity quotationEntity
                = databaseRepository.getNextQuotation(useInternalDatabase, widgetId, contentSelection);

            if (quotationEntity != null) {
                addToPreviousAll(widgetId, quotationEntity);

                databaseRepository.markAsCurrent(
                    useInternalDatabase,
                    widgetId,
                    quotationEntity.digest);
            }
        });
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
        runAction(() -> databaseRepository.alignHistoryWithQuotations(useInternalDatabase, widgetId, context));
    }

    @NonNull
    public List<String> getPreviousDigests(
        final int widgetId,
        @NonNull final ContentSelection contentSelection,
        @NonNull final String criteria) {
        return runTask(() ->
            databaseRepository.getPreviousDigests(useInternalDatabase, widgetId, contentSelection, criteria), Collections.emptyList());
    }

    private void setDefaultAuthor(int widgetId) {
        if (countPreviousAuthor(widgetId) == 0) {
            databaseRepository.erase(useInternalDatabase, widgetId, ContentSelection.AUTHOR);
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
            databaseRepository.erase(useInternalDatabase, widgetId, ContentSelection.SEARCH);
            markAsCurrentNext(widgetId, false);
        }
    }

    private void setDefaultAll(final int widgetId) {
        if (countPrevious(widgetId, ContentSelection.ALL) == 0) {
            databaseRepository.markAsPrevious(
                useInternalDatabase, widgetId, ContentSelection.ALL, DatabaseRepository.getDefaultQuotationDigest(useInternalDatabase));
        }
    }

    public int countPrevious(final int widgetId,
                             @NonNull final ContentSelection contentSelection) {
        return runTask(() -> databaseRepository.countPreviousCriteria(useInternalDatabase, widgetId, contentSelection), 0);
    }

    public int countPrevious(final int widgetId) {
        return runTask(() -> databaseRepository.countPreviousCriteria(useInternalDatabase, widgetId), 0);
    }

    public int countPreviousAuthor(final int widgetId) {
        return runTask(() -> databaseRepository.countPreviousCriteria(useInternalDatabase, widgetId, ContentSelection.AUTHOR,
            getContentPreferences(widgetId)), 0);
    }

    public int countPreviousSearch(final int widgetId) {
        return runTask(() -> databaseRepository.countPreviousCriteria(useInternalDatabase, widgetId, ContentSelection.SEARCH,
            getContentPreferences(widgetId)), 0);
    }

    public int countFavouritesWithoutRx() {
        return runTask(() -> databaseRepository.countFavourites(useInternalDatabase).blockingGet(), 0);
    }

    public int toggleFavourite(final int widgetId, @NonNull final String digest) {
        return runTask(() -> {
            List<String> favourites = databaseRepository.getFavouritesDigests(useInternalDatabase);

            if (!favourites.contains(digest)) {
                databaseRepository.markAsFavourite(useInternalDatabase, digest);
            } else {
                databaseRepository.eraseFavourite(useInternalDatabase, widgetId, digest);
            }

            return databaseRepository.countFavourites(useInternalDatabase).blockingGet();
        }, 0);
    }

    public boolean isFavourite(@NonNull final String digest) {
        return runTask(() -> databaseRepository.isFavourite(useInternalDatabase, digest), false);
    }

    public QuotationEntity getQuotation(@NonNull final String digest) {
        return runTask(() -> databaseRepository.getQuotation(useInternalDatabase, digest), null);
    }

    public List<QuotationEntity> getAllQuotations() {
        return runTask(() -> databaseRepository.getAllQuotations(useInternalDatabase), Collections.emptyList());
    }

    public void delete(final int widgetId) {
        runAction(() -> databaseRepository.erase(useInternalDatabase, widgetId));
    }

    public void disable() {
        runAction(() -> databaseRepository.erase(useInternalDatabase));
    }

    public void resetPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        Timber.d("contentSelection=%d", contentSelection.getContentSelection());

        runAction(() -> databaseRepository.erase(useInternalDatabase, widgetId, contentSelection));
    }

    public void markAsCurrentPrevious(
        final int widgetId) {
        runAction(() -> {
            QuotationEntity previousQuotation = getPreviousQuotation(
                widgetId,
                getContentPreferences(widgetId).getContentSelection(),
                getCurrentQuotation(widgetId).digest,
                getContentPreferences(widgetId).getContentSelectionAllExclusion());

            databaseRepository.markAsCurrent(useInternalDatabase, widgetId, previousQuotation.digest);
        });
    }

    public void markAsCurrentLastPrevious(final int widgetId) {
        runAction(() -> databaseRepository.markAsCurrent(
            useInternalDatabase,
            widgetId,
            getLastPreviousDigest(widgetId)));
    }

    @NonNull
    public Single<Integer> countAllMinusExclusions(final int widgetId) {
        return runTask(() ->
            databaseRepository.countAllMinusExclusions(
                useInternalDatabase,
                getContentPreferences(widgetId).getContentSelectionAllExclusion()), Single.just(0));
    }

    @NonNull
    public boolean externalDatabaseContainsQuotations() {
        return runTask(() -> databaseRepository.countAllExternal().blockingGet() > 0, false);
    }

    @NonNull
    public Single<List<Integer>> authorsQuotationCount() {
        return runTask(() -> databaseRepository.getAuthorsQuotationCount(useInternalDatabase), Single.just(Collections.emptyList()));
    }

    @NonNull
    public List<Integer> authorsQuotationCountAsList() {
        return runTask(() -> databaseRepository.getAuthorsQuotationCount(useInternalDatabase).blockingGet(), Collections.emptyList());
    }

    @NonNull
    public Single<List<AuthorPOJO>> authors(int authorCount) {
        return runTask(() -> databaseRepository.getAuthorsAndQuotationCounts(useInternalDatabase, (authorCount == -1) ? 1 : authorCount), Single.just(Collections.emptyList()));
    }

    @NonNull
    public List<AuthorPOJO> authorsAsList(int authorCount) {
        return runTask(() -> databaseRepository.getAuthorsAndQuotationCounts(useInternalDatabase, (authorCount == -1) ? 1 : authorCount).blockingGet(), Collections.emptyList());
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

        if (cachedAuthorPOJOList != null) {
            for (final AuthorPOJO authorPOJO : cachedAuthorPOJOList) {
                if (authorPOJO.author.equals(author)) {
                    countAuthorQuotations = authorPOJO.count;
                    break;
                }
            }
        }
        return countAuthorQuotations;
    }

    public int authorsIndex(@NonNull final String author) {
        int index = 0;
        if (cachedAuthorPOJOList != null) {
            for (final AuthorPOJO authorPOJO : cachedAuthorPOJOList) {
                if (authorPOJO.author.equals(author)) {
                    break;
                } else {
                    index++;
                }
            }
        }
        return index;
    }

    @NonNull
    public Single<Integer> countFavourites() {
        return runTask(() -> databaseRepository.countFavourites(useInternalDatabase), Single.just(0));
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotations(@NonNull final String text, boolean favouritesOnly) {
        return getSearchQuotations(-1, text, favouritesOnly);
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotations(int widgetId, @NonNull final String text, boolean favouritesOnly) {
        return runTask(() -> databaseRepository.getSearchQuotations(useInternalDatabase, widgetId, text, favouritesOnly), Collections.emptyList());
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotationsRegEx(@NonNull final String text, boolean favouritesOnly) {
        return getSearchQuotationsRegEx(-1, text, favouritesOnly);
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotationsRegEx(int widgetId, @NonNull final String text, boolean favouritesOnly) {
        return runTask(() -> databaseRepository.getSearchQuotationsRegEx(useInternalDatabase, widgetId, text, favouritesOnly), Collections.emptyList());
    }

    @NonNull
    public Integer countQuotationWithSearchTextRegEx(@NonNull final String regEx, boolean favouritesOnly) {
        return countQuotationWithSearchTextRegEx(-1, regEx, favouritesOnly);
    }

    @NonNull
    public Integer countQuotationWithSearchTextRegEx(int widgetId, @NonNull final String regEx, boolean favouritesOnly) {
        return runTask(() -> databaseRepository.countSearchTextRegEx(useInternalDatabase, widgetId, regEx, favouritesOnly), 0);
    }

    @NonNull
    public Integer countQuotationWithSearchText(@NonNull final String searchText, boolean favouritesOnly) {
        return countQuotationWithSearchText(-1, searchText, favouritesOnly);
    }

    @NonNull
    public Integer countQuotationWithSearchText(int widgetId, @NonNull final String searchText, boolean favouritesOnly) {
        return runTask(() -> databaseRepository.countSearchText(useInternalDatabase, widgetId, searchText, favouritesOnly), 0);
    }

    @NonNull
    public String transferBackup(@NonNull final Context context) {
        return runTask(() -> {
            TransferBackup transferBackup = new TransferBackup(context);
            Transfer transfer = transferBackup.transfer(databaseRepository);
            return transferBackup.asJson(transfer);
        }, TIMEOUT_BACKUP_SECONDS, "");
    }

    @NonNull
    public List<QuotationEntity> getFavourites() {
        return runTask(() -> {
            List<FavouriteEntity> favouritesDigestList = databaseRepository.getFavourites(useInternalDatabase);
            Collections.reverse(favouritesDigestList);

            List<QuotationEntity> favouriteQuotationsList = new ArrayList<>();
            for (FavouriteEntity favourite : favouritesDigestList) {
                // check quotation still exists, following an inplace .apk update
                QuotationEntity quotationEntity = databaseRepository.getQuotation(useInternalDatabase, favourite.digest);
                if (quotationEntity != null) {
                    favouriteQuotationsList.add(quotationEntity);
                }
            }

            return favouriteQuotationsList;
        }, Collections.emptyList());
    }

    @Nullable
    public List<QuotationEntity> exportFavourites() {
        return runTask(() -> {
            final ArrayList<QuotationEntity> exportedFavourites = new ArrayList<>();

            for (final String favouriteDigest : databaseRepository.getFavouritesDigests(useInternalDatabase)) {
                final QuotationEntity quotationEntity
                    = databaseRepository.getQuotation(useInternalDatabase, favouriteDigest);

                if (quotationEntity != null) {
                    exportedFavourites.add(quotationEntity);
                } else {
                    Timber.w("misaligned:%s", favouriteDigest);
                }
            }

            return exportedFavourites;
        }, null);
    }

    public void insertQuotationsExternal(
        @NonNull final LinkedHashSet<QuotationEntity> quotations) {
        runAction(() -> {
            useInternalDatabase = false;
            databaseRepository.erase(useInternalDatabase);
            databaseRepository.insertQuotationsExternal(quotations);
        });
    }

    @NonNull
    public List<QuotationEntity> getQuotationsForAuthor(@NonNull final String author) {
        return runTask(() ->
            databaseRepository.getQuotationsForAuthor(useInternalDatabase, author), Collections.emptyList());
    }

    public void insertQuotationExternal(int widgetId, QuotationEntity quotation) {
        runAction(() -> {
            useInternalDatabase = false;
            databaseRepository.insertQuotationExternal(quotation);

            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

            if (databaseRepository.findPositionInPrevious(useInternalDatabase, widgetId, quotationsPreferences) + 1
                == databaseRepository.countNext(useInternalDatabase, quotationsPreferences)) {
                // as a courtesy, move to latest quotation if user was at adjacent previous
                markAsCurrentNext(widgetId, false);
            }
        });
    }

    public ScraperData getWebPage(
        @NonNull final Context context,
        @NonNull final String url,
        @NonNull final String xpathQuotation,
        @NonNull final String xpathSource
    ) {
        return runTask(() -> {
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
        }, new ScraperData());
    }

    public void insertWebPage(
        final int widgetId,
        @NonNull final String quotation,
        @NonNull final String source,
        @NonNull final String digest
    ) {
        Timber.d("scraper: %s; %s; %s", digest, source, quotation);

        runAction(() -> {
            if (digest.equals(ImportHelper.DEFAULT_DIGEST)) {
                databaseRepository.erase(false);
            }

            insertQuotationExternal(
                widgetId,
                new QuotationEntity(
                    digest,
                    "?",
                    source,
                    quotation));
        });
    }

    public String getPosition(int widgetId, String digest) {
        return runTask(() -> {
            QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

            String quotationPosition = getCurrentPosition(
                widgetId,
                quotationsPreferences);

            if (digest.equals(getLastPreviousDigest(widgetId))) {
                quotationPosition = "\u2316  " + quotationPosition + " ";
            }

            return quotationPosition;
        }, "");
    }

    public boolean isDuplicate(
        String author,
        String quotation
    ) {
        return runTask(() -> {
            List<QuotationEntity> quotationsForAuthor = getQuotationsForAuthor(author);

            for (QuotationEntity quotationAuthor : quotationsForAuthor) {
                if (quotationAuthor.quotation.equals(quotation)) {
                    return true;
                }
            }

            return false;
        }, false);
    }

    public void append(
        String author,
        String quotation
    ) {
        runAction(() -> {
            LinkedHashSet<QuotationEntity> quotationEntityLinkedHashSet = new LinkedHashSet<>();

            String digest = ImportHelper.DEFAULT_DIGEST;
            if (databaseRepository.countAll(false).blockingGet() > 0) {
                digest = ImportHelper.makeDigest(author, quotation);
            }

            quotationEntityLinkedHashSet.add(
                new QuotationEntity(
                    digest,
                    "?",
                    author,
                    quotation
                )
            );

            databaseRepository.insertQuotationsExternal(quotationEntityLinkedHashSet);
        });
    }

    public void update(
        String digest,
        String author,
        String quotation
    ) {
        runAction(() -> databaseRepository.updateQuotationUsingDigest(
            false,
            digest,
            author,
            quotation
        ));
    }

    public void delete(
        int widgetId,
        String digest
    ) {
        runAction(() -> {
            List<QuotationEntity> allQuotations = getAllQuotations();

            databaseRepository.deleteQuotation(false, digest);
            databaseRepository.deleteFavourite(false, digest);
            databaseRepository.deletePrevious(false, digest);

            if (allQuotations != null) {
                allQuotations.removeIf(q -> q.digest.equals(digest));

                if (digest.equals(ImportHelper.DEFAULT_DIGEST) && !allQuotations.isEmpty()) {
                    databaseRepository.updateQuotationUsingAuthorQuotation(
                        false,
                        ImportHelper.DEFAULT_DIGEST,
                        allQuotations.get(0).author,
                        allQuotations.get(0).quotation
                    );

                    boolean wasFavourite = databaseRepository.isFavourite(false, allQuotations.get(0).digest);
                    if (wasFavourite) {
                        databaseRepository.markAsFavourite(false, ImportHelper.DEFAULT_DIGEST);
                    }

                    databaseRepository.deleteFavourite(false, allQuotations.get(0).digest);
                    databaseRepository.deletePrevious(false, allQuotations.get(0).digest);
                }

                if (!allQuotations.isEmpty()) {
                    if (databaseRepository.getPrevious(false).isEmpty()) {
                        databaseRepository.markAsCurrent(
                            false,
                            widgetId,
                            ImportHelper.DEFAULT_DIGEST
                        );

                        databaseRepository.markAsPrevious(
                            false,
                            widgetId,
                            ContentSelection.ALL,
                            ImportHelper.DEFAULT_DIGEST
                        );
                    }
                }
            }
        });
    }

    private void runAction(Runnable runnable) {
        runAction(runnable, TIMEOUT_SECONDS);
    }

    private void runAction(Runnable runnable, int timeoutSeconds) {
        try {
            QuoteUnquoteWidget.getExecutorService().submit(runnable).get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            Timber.e(e);
        }
    }

    private <T> T runTask(Callable<T> callable, T defaultValue) {
        return runTask(callable, TIMEOUT_SECONDS, defaultValue);
    }

    private <T> T runTask(Callable<T> callable, int timeoutSeconds, T defaultValue) {
        try {
            return QuoteUnquoteWidget.getExecutorService().submit(callable).get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            Timber.e(e);
        }
        return defaultValue;
    }
}
