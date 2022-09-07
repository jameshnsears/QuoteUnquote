package com.github.jameshnsears.quoteunquote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.history.AbstractHistoryDatabase;
import com.github.jameshnsears.quoteunquote.database.history.CurrentDAO;
import com.github.jameshnsears.quoteunquote.database.history.CurrentEntity;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteDAO;
import com.github.jameshnsears.quoteunquote.database.history.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.database.history.PreviousDAO;
import com.github.jameshnsears.quoteunquote.database.history.PreviousEntity;
import com.github.jameshnsears.quoteunquote.database.history.ReportedDAO;
import com.github.jameshnsears.quoteunquote.database.history.external.AbstractHistoryExternalDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationDAO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.database.quotation.external.AbstractQuotationExternalDatabase;
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

    @Nullable
    public static boolean useInternalDatabase = true;

    @NonNull
    protected final SecureRandom secureRandom = new SecureRandom();

    @Nullable
    public AbstractQuotationDatabase abstractQuotationDatabase;
    @Nullable
    public AbstractQuotationExternalDatabase abstractQuotationExternalDatabase;
    @Nullable
    public AbstractHistoryDatabase abstractHistoryDatabase;
    @Nullable
    public PreviousDAO previousDAO;
    @Nullable
    public AbstractHistoryExternalDatabase abstractHistoryExternalDatabase;
    @Nullable
    public PreviousDAO previousExternalDAO;
    @Nullable
    protected QuotationDAO quotationDAO;
    @Nullable
    protected QuotationDAO quotationExternalDAO;
    @Nullable
    protected FavouriteDAO favouriteDAO;
    @Nullable
    protected ReportedDAO reportedDAO;
    @Nullable
    protected CurrentDAO currentDAO;
    @Nullable
    protected FavouriteDAO favouriteExternalDAO;
    @Nullable
    protected ReportedDAO reportedExternalDAO;
    @Nullable
    protected CurrentDAO currentExternalDAO;

    public DatabaseRepository() {
        //
    }

    protected DatabaseRepository(@NonNull final Context context) {
        abstractQuotationDatabase = AbstractQuotationDatabase.getDatabase(context);
        quotationDAO = abstractQuotationDatabase.quotationDAO();

        abstractQuotationExternalDatabase = AbstractQuotationExternalDatabase.getDatabase(context);
        quotationExternalDAO = abstractQuotationExternalDatabase.quotationExternalDAO();

        abstractHistoryDatabase = AbstractHistoryDatabase.getDatabase(context);
        previousDAO = abstractHistoryDatabase.previousDAO();
        favouriteDAO = abstractHistoryDatabase.favouritesDAO();
        reportedDAO = abstractHistoryDatabase.reportedDAO();
        currentDAO = abstractHistoryDatabase.currentDAO();

        abstractHistoryExternalDatabase = AbstractHistoryExternalDatabase.getDatabase(context);
        previousExternalDAO = abstractHistoryExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = abstractHistoryExternalDatabase.favouritesExternalDAO();
        reportedExternalDAO = abstractHistoryExternalDatabase.reportedExternalDAO();
        currentExternalDAO = abstractHistoryExternalDatabase.currentExternalDAO();
    }

    public static void close(@NonNull final Context context) {
        AbstractQuotationDatabase.getDatabase(context).close();
        AbstractQuotationDatabase.quotationDatabase = null;

        AbstractQuotationExternalDatabase.getDatabase(context).close();
        AbstractQuotationExternalDatabase.quotationExternalDatabase = null;

        AbstractHistoryDatabase.getDatabase(context).close();
        AbstractHistoryDatabase.historyDatabase = null;

        AbstractHistoryExternalDatabase.getDatabase(context).close();
        AbstractHistoryExternalDatabase.historyExternalDatabase = null;
    }

    @NonNull
    public static synchronized DatabaseRepository getInstance(@NonNull final Context context) {
        if (databaseRepository == null) {
            databaseRepository = new DatabaseRepository(context);
        }

        return databaseRepository;
    }

    @NonNull
    public static String getDefaultQuotationDigest() {
        if (useInternalDatabase()) {
            return "7a36e553";
        } else {
            return "00000000";
        }
    }

    @NonNull
    public Single<Integer> countAll() {
        if (useInternalDatabase()) {
            return quotationDAO.countAll();
        }

        return countAllExternal();
    }

    @NonNull
    public Single<Integer> countAllExternal() {
        return quotationExternalDAO.countAll();
    }

    public int countPrevious(final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase()) {
            return previousDAO.countPrevious(widgetId, contentSelection);
        }

        return previousExternalDAO.countPrevious(widgetId, contentSelection);
    }

    private static boolean useInternalDatabase() {
        return useInternalDatabase;
    }

    public int countPreviousDigest(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull String digest) {
        if (useInternalDatabase()) {
            return previousDAO.countPreviousDigest(widgetId, contentSelection, digest);
        }

        return previousExternalDAO.countPreviousDigest(widgetId, contentSelection, digest);
    }

    public int countPrevious(final int widgetId) {
        if (useInternalDatabase()) {
            return previousDAO.countPrevious(widgetId);
        }

        return previousExternalDAO.countPrevious(widgetId);
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
                if (useInternalDatabase()) {
                    countTotalNext = favouriteDAO.countFavourites().blockingGet();
                } else {
                    countTotalNext = favouriteExternalDAO.countFavourites().blockingGet();
                }
                break;

            case AUTHOR:
                if (useInternalDatabase()) {
                    countTotalNext
                            = quotationDAO.getDigestsForAuthor(
                            quotationsPreferences.getContentSelectionAuthor()).size();
                } else {
                    countTotalNext
                            = quotationExternalDAO.getDigestsForAuthor(
                            quotationsPreferences.getContentSelectionAuthor()).size();
                }
                break;

            case SEARCH:
                if (useInternalDatabase()) {
                    countTotalNext = quotationDAO.getSearchTextDigests(
                            "%" + quotationsPreferences.getContentSelectionSearch() + "%").size();
                } else {
                    countTotalNext = quotationExternalDAO.getSearchTextDigests(
                            "%" + quotationsPreferences.getContentSelectionSearch() + "%").size();
                }
                break;

            default:
                // ALL:
                if (useInternalDatabase()) {
                    countTotalNext = quotationDAO.countAll().blockingGet();
                } else {
                    countTotalNext = quotationExternalDAO.countAll().blockingGet();
                }
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
            if (useInternalDatabase()) {
                availableDigests = new HashSet<>(quotationDAO.getDigestsForAuthor(criteria));
            } else {
                availableDigests = new HashSet<>(quotationExternalDAO.getDigestsForAuthor(criteria));
            }
        } else {
            previousDigests = new HashSet<>(getPreviousDigests(widgetId, ContentSelection.SEARCH));
            if (useInternalDatabase()) {
                availableDigests
                        = new HashSet<>(quotationDAO.getSearchTextDigests("%" + criteria + "%"));
            } else {
                availableDigests
                        = new HashSet<>(quotationExternalDAO.getSearchTextDigests("%" + criteria + "%"));
            }
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
        if (useInternalDatabase()) {
            return favouriteDAO.countFavourites();
        } else {
            return favouriteExternalDAO.countFavourites();
        }
    }

    @NonNull
    public QuotationEntity getNextQuotation(final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase()) {
            return getQuotation(previousDAO.getLastPrevious(widgetId, contentSelection).digest);
        } else {
            return getQuotation(previousExternalDAO.getLastPrevious(widgetId, contentSelection).digest);
        }
    }

    @NonNull
    public String getLastPreviousDigest(final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase()) {
            return previousDAO.getLastPrevious(widgetId, contentSelection).digest;
        } else {
            return previousExternalDAO.getLastPrevious(widgetId, contentSelection).digest;
        }
    }

    @NonNull
    public List<String> getPreviousDigests(final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase()) {
            return previousDAO.getPreviousDigests(widgetId, contentSelection);
        } else {
            return previousExternalDAO.getPreviousDigests(widgetId, contentSelection);
        }
    }

    @NonNull
    public List<PreviousEntity> getPrevious() {
        if (useInternalDatabase()) {
            return previousDAO.getAllPrevious();
        } else {
            return previousExternalDAO.getAllPrevious();
        }
    }

    @NonNull
    public void alignHistoryWithQuotations(int widgetId, @NonNull Context context) {
        boolean alignmentRequired = false;

        if (useInternalDatabase()) {
            for (PreviousEntity previousEntity : previousDAO.getAllPrevious()) {
                if (quotationDAO.getQuotation(previousEntity.digest) == null) {
                    Timber.d("align=%s", previousEntity.digest);
                    alignmentRequired = true;
                    currentDAO.erase(previousEntity.digest);
                    previousDAO.erase(previousEntity.digest);
                    favouriteDAO.erase(previousEntity.digest);
                }
            }
        } else {
            for (PreviousEntity previousEntity : previousExternalDAO.getAllPrevious()) {
                if (quotationExternalDAO.getQuotation(previousEntity.digest) == null) {
                    Timber.d("align=%s", previousEntity.digest);
                    alignmentRequired = true;
                    currentExternalDAO.erase(previousEntity.digest);
                    previousExternalDAO.erase(previousEntity.digest);
                    favouriteExternalDAO.erase(previousEntity.digest);
                }
            }
        }

        if (alignmentRequired) {
            QuotationsPreferences quotationsPreferences
                    = new QuotationsPreferences(widgetId, context);
            quotationsPreferences.setContentSelection(ContentSelection.ALL);

            markAsCurrent(
                    widgetId,
                    getLastPreviousDigest(widgetId, ContentSelection.ALL));
        }
    }

    @NonNull
    public List<String> getFavouriteDigests() {
        if (useInternalDatabase()) {
            return favouriteDAO.getFavouriteDigests();
        } else {
            return favouriteExternalDAO.getFavouriteDigests();
        }
    }

    @NonNull
    public List<FavouriteEntity> getFavourites() {
        if (useInternalDatabase()) {
            return favouriteDAO.getFavourites();
        } else {
            return favouriteExternalDAO.getFavourites();
        }
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts() {
        if (useInternalDatabase()) {
            return quotationDAO.getAuthorsAndQuotationCounts(1);
        } else {
            return quotationExternalDAO.getAuthorsAndQuotationCounts(1);
        }
    }

    @NonNull
    public Integer countSearchText(@NonNull final String text, boolean favouritesOnly) {
        if (favouritesOnly) {
            int searchCount = 0;
            if (useInternalDatabase()) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);
                    if (quotationEntity.author.contains(text) || quotationEntity.quotation.contains(text)) {
                        searchCount += 1;
                    }
                }
            } else {
                for (String digest : favouriteExternalDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationExternalDAO.getQuotation(digest);
                    if (quotationEntity.author.contains(text) || quotationEntity.quotation.contains(text)) {
                        searchCount += 1;
                    }
                }
            }

            return searchCount;
        } else {
            if (useInternalDatabase()) {
                return quotationDAO.countSearchText("%" + text + "%");
            } else {
                return quotationExternalDAO.countSearchText("%" + text + "%");
            }
        }
    }

    @NonNull
    public List<QuotationEntity> getAllQuotations() {
        if (useInternalDatabase()) {
            return quotationDAO.getAllQuotations();
        } else {
            return quotationExternalDAO.getAllQuotations();
        }
    }

    @NonNull
    public QuotationEntity getQuotation(@NonNull final String digest) {
        if (useInternalDatabase()) {
            return quotationDAO.getQuotation(digest);
        } else {
            return quotationExternalDAO.getQuotation(digest);
        }
    }

    public void markAsPrevious(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String digest) {
        if (useInternalDatabase()) {
            if (getQuotation(digest) != null) {
                previousDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
            }
        } else {
            if (getQuotation(digest) != null) {
                previousExternalDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
            }
        }
    }

    public void markAsFavourite(@NonNull final String digest) {
        if (useInternalDatabase()) {
            if (favouriteDAO.isFavourite(digest) == 0) {
                if (getQuotation(digest) != null) {
                    favouriteDAO.markAsFavourite(new FavouriteEntity(digest));
                }
            }
        } else {
            if (getQuotation(digest) != null) {
                if (favouriteExternalDAO.isFavourite(digest) == 0) {
                    favouriteExternalDAO.markAsFavourite(new FavouriteEntity(digest));
                }
            }
        }
    }

    public void markAsCurrent(
            final int widgetId,
            @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        if (useInternalDatabase()) {
            currentDAO.erase(widgetId);
            currentDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
        } else {
            currentExternalDAO.erase(widgetId);
            currentExternalDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
        }
    }

    @NonNull
    public QuotationEntity getCurrentQuotation(final int widgetId) {
        QuotationEntity quotationEntity;

        if (useInternalDatabase()) {
            quotationEntity = getQuotation(currentDAO.getCurrentDigest(widgetId));
        } else {
            quotationEntity = getQuotation(currentExternalDAO.getCurrentDigest(widgetId));
        }

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
                if (useInternalDatabase()) {
                    nextQuotationDigests
                            = new LinkedHashSet<>(favouriteDAO.getNextFavouriteDigests());
                } else {
                    nextQuotationDigests
                            = new LinkedHashSet<>(favouriteExternalDAO.getNextFavouriteDigests());
                }
                nextQuotationDigests.removeAll(previousDigests);
                break;

            case AUTHOR:
                LinkedHashSet<String> authorDigests;
                if (useInternalDatabase()) {
                    authorDigests
                            = new LinkedHashSet<>(quotationDAO.getNextAuthorDigest(searchString));
                } else {
                    authorDigests
                            = new LinkedHashSet<>(quotationExternalDAO.getNextAuthorDigest(searchString));
                }
                authorDigests.removeAll(previousDigests);
                nextQuotationDigests = authorDigests;
                break;

            case SEARCH:
                final LinkedHashSet<String> searchDigests;
                if (useInternalDatabase()) {
                    searchDigests
                            = new LinkedHashSet<>(quotationDAO.getNextSearchTextDigests("%" + searchString + "%"));
                } else {
                    searchDigests
                            = new LinkedHashSet<>(quotationExternalDAO.getNextSearchTextDigests("%" + searchString + "%"));
                }
                searchDigests.removeAll(previousDigests);
                nextQuotationDigests = searchDigests;
                break;

            default:
                // ALL:
                final LinkedHashSet<String> allDigests;
                if (useInternalDatabase()) {
                    allDigests = new LinkedHashSet<>(quotationDAO.getNextAllDigests());
                } else {
                    allDigests = new LinkedHashSet<>(quotationExternalDAO.getNextAllDigests());
                }
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

        if (useInternalDatabase()) {
            favouriteDAO.erase(digest);
            previousDAO.erase(widgetId, ContentSelection.FAVOURITES, digest);
        } else {
            favouriteExternalDAO.erase(digest);
            previousExternalDAO.erase(widgetId, ContentSelection.FAVOURITES, digest);
        }
    }

    public void erase() {
        if (useInternalDatabase()) {
            previousDAO.erase();
            currentDAO.erase();
            favouriteDAO.erase();
            reportedDAO.erase();
        } else {
            quotationExternalDAO.erase();

            previousExternalDAO.erase();
            currentExternalDAO.erase();
            favouriteExternalDAO.erase();
            reportedExternalDAO.erase();
        }
    }

    public void eraseForRestore() {
        previousDAO.erase();
        currentDAO.erase();
        previousExternalDAO.erase();
        currentExternalDAO.erase();
    }

    public void erase(final int widgetId) {
        if (useInternalDatabase()) {
            previousDAO.erase(widgetId);
            currentDAO.erase(widgetId);
        } else {
            previousExternalDAO.erase(widgetId);
            currentExternalDAO.erase(widgetId);
        }
    }

    public void erase(final int widgetId, @NonNull final ContentSelection contentSelection) {
        Timber.d("contentType=%d", contentSelection.getContentSelection());

        if (useInternalDatabase()) {
            previousDAO.erase(widgetId, contentSelection);
            currentDAO.erase(widgetId);
        } else {
            previousExternalDAO.erase(widgetId, contentSelection);
            currentExternalDAO.erase(widgetId);
        }
    }

    @NonNull
    public Boolean isFavourite(@NonNull final String digest) {
        if (useInternalDatabase()) {
            return favouriteDAO.isFavourite(digest) > 0;
        } else {
            return favouriteExternalDAO.isFavourite(digest) > 0;
        }
    }

    public void insertQuotationsExternal(
            @NonNull final LinkedHashSet<QuotationEntity> quotationEntityList) {

        for (final QuotationEntity quotationEntity : quotationEntityList) {
            quotationExternalDAO.insertQuotation(quotationEntity);
        }
    }
}
