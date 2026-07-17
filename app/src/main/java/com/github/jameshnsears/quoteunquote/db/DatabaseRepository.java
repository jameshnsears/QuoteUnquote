package com.github.jameshnsears.quoteunquote.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Transaction;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.db.h.CurrentDAO;
import com.github.jameshnsears.quoteunquote.db.h.CurrentEntity;
import com.github.jameshnsears.quoteunquote.db.h.FavouriteDAO;
import com.github.jameshnsears.quoteunquote.db.h.FavouriteEntity;
import com.github.jameshnsears.quoteunquote.db.h.HistoryDatabase;
import com.github.jameshnsears.quoteunquote.db.h.HistoryExternalDatabase;
import com.github.jameshnsears.quoteunquote.db.h.PreviousDAO;
import com.github.jameshnsears.quoteunquote.db.h.PreviousEntity;
import com.github.jameshnsears.quoteunquote.db.q.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.db.q.ExternalDatabase;
import com.github.jameshnsears.quoteunquote.db.q.QuotationDAO;
import com.github.jameshnsears.quoteunquote.db.q.QuotationDatabase;
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.reactivex.Single;
import timber.log.Timber;

public class DatabaseRepository {
    @SuppressLint("StaticFieldLeak")
    @Nullable
    public static DatabaseRepository databaseRepository;

    @NonNull
    protected final SecureRandom secureRandom = new SecureRandom();
    private final LruCache<SearchCacheKey, List<QuotationEntity>> searchCache = new LruCache<>(20);
    @Nullable
    public QuotationDatabase quotationDatabase;
    @Nullable
    public ExternalDatabase externalDatabase;
    @Nullable
    public HistoryDatabase historyDatabase;
    @Nullable
    public PreviousDAO previousDAO;
    @Nullable
    public HistoryExternalDatabase historyExternalDatabase;
    @Nullable
    public PreviousDAO previousExternalDAO;
    @Nullable
    protected QuotationDAO quotationDAO;
    @Nullable
    protected QuotationDAO quotationExternalDAO;
    @Nullable
    protected FavouriteDAO favouriteDAO;
    @Nullable
    protected CurrentDAO currentDAO;
    @Nullable
    protected FavouriteDAO favouriteExternalDAO;
    @Nullable
    protected CurrentDAO currentExternalDAO;
    @Nullable
    protected Context context;
    @Nullable
    protected List<QuotationEntity> cacheInternalQuotations;
    @Nullable
    protected Map<String, QuotationEntity> cacheInternalQuotationsMap;
    @Nullable
    protected List<QuotationEntity> cacheExternalQuotations;
    @Nullable
    protected Map<String, QuotationEntity> cacheExternalQuotationsMap;
    @Nullable
    protected Map<Boolean, List<String>> cacheFavouriteDigests;
    @Nullable
    protected Map<String, QuotationEntity> cacheCurrentQuotation;

    protected DatabaseRepository(@NonNull final Context context) {
        Context appContext = context.getApplicationContext();
        quotationDatabase = QuotationDatabase.getDatabase(appContext);
        quotationDAO = quotationDatabase.quotationDAO();

        externalDatabase = ExternalDatabase.getDatabase(appContext);
        quotationExternalDAO = externalDatabase.quotationExternalDAO();

        historyDatabase = HistoryDatabase.getDatabase(appContext);
        previousDAO = historyDatabase.previousDAO();
        favouriteDAO = historyDatabase.favouritesDAO();
        currentDAO = historyDatabase.currentDAO();

        historyExternalDatabase = HistoryExternalDatabase.getDatabase(appContext);
        previousExternalDAO = historyExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = historyExternalDatabase.favouritesExternalDAO();
        currentExternalDAO = historyExternalDatabase.currentExternalDAO();

        this.context = appContext;

        cacheFavouriteDigests = new LinkedHashMap<>();
        cacheCurrentQuotation = new LinkedHashMap<>();
    }

    public static void close(@NonNull final Context context) {
        if (databaseRepository != null) {
            databaseRepository = null;
        }

        QuotationDatabase.getDatabase(context).close();
        QuotationDatabase.quotationDatabase = null;

        ExternalDatabase.getDatabase(context).close();
        ExternalDatabase.externalDatabase = null;

        HistoryDatabase.getDatabase(context).close();
        HistoryDatabase.historyDatabase = null;

        HistoryExternalDatabase.getDatabase(context).close();
        HistoryExternalDatabase.historyExternalDatabase = null;
    }

    @NonNull
    public static synchronized DatabaseRepository getInstance(@NonNull final Context context) {
        if (databaseRepository == null) {
            databaseRepository = new DatabaseRepository(context.getApplicationContext());
        }

        return databaseRepository;
    }

    @NonNull
    public static String getDefaultQuotationDigest(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return "1624c314";
        } else {
            return "00000000";
        }
    }

    @NonNull
    public synchronized List<QuotationEntity> getSearchQuotationsRegEx(boolean useInternalDatabase, @NonNull final String regEx, boolean favouritesOnly) {
        return getSearchQuotationsRegEx(useInternalDatabase, -1, regEx, favouritesOnly);
    }

    @NonNull
    public synchronized List<QuotationEntity> getSearchQuotationsRegEx(boolean useInternalDatabase, int widgetId, @NonNull final String regEx, boolean favouritesOnly) {
        SearchCacheKey key = new SearchCacheKey(useInternalDatabase, regEx, favouritesOnly, true);
        List<QuotationEntity> cached = searchCache.get(key);
        List<QuotationEntity> searchQuotations;

        if (cached != null) {
            searchQuotations = new ArrayList<>(cached);
        } else {
            searchQuotations = new ArrayList<>();
            try {
                final Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
                searchQuotations = searchInternal(useInternalDatabase, favouritesOnly, quotationEntity -> {
                    Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                    Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);
                    return matcherAuthor.find() || matcherQuotation.find();
                });
            } catch (PatternSyntaxException e) {
                Timber.e(e);
            }

            if (!searchQuotations.isEmpty()) {
                searchCache.put(key, new ArrayList<>(searchQuotations));
            }
        }

        return searchQuotations;
    }

    @NonNull
    private QuotationDAO getQuotationDAO(boolean useInternal) {
        return Objects.requireNonNull(useInternal ? quotationDAO : quotationExternalDAO);
    }

    @NonNull
    private FavouriteDAO getFavouriteDAO(boolean useInternal) {
        return Objects.requireNonNull(useInternal ? favouriteDAO : favouriteExternalDAO);
    }

    @NonNull
    private PreviousDAO getPreviousDAO(boolean useInternal) {
        return Objects.requireNonNull(useInternal ? previousDAO : previousExternalDAO);
    }

    @NonNull
    private CurrentDAO getCurrentDAO(boolean useInternal) {
        return Objects.requireNonNull(useInternal ? currentDAO : currentExternalDAO);
    }

    @NonNull
    public Single<Integer> countAll(boolean useInternalDatabase) {
        return getQuotationDAO(useInternalDatabase).countAll();
    }

    @NonNull
    public Single<Integer> countAllMinusExclusions(boolean useInternalDatabase, final String exclusions) {
        if ("".equals(exclusions)) {
            return countAll(useInternalDatabase);
        }

        return Single.just(countAll(useInternalDatabase).blockingGet() - getAllExcludedDigests(useInternalDatabase, exclusions).size());
    }

    @NonNull
    public HashSet<String> getAllExcludedDigests(boolean useInternalDatabase, String exclusions) {
        HashSet<String> digestsExcluded = new HashSet<>();

        for (String exclusion : exclusions.split(";")) {
            // 4 is the smallest author entry
            if (!"".equals(exclusion) && exclusion.length() >= 4) {
                digestsExcluded.addAll(getQuotationDAO(useInternalDatabase).getExclusionDigests(exclusion));
            }
        }

        // we keep the default quotation
        digestsExcluded.remove(getDefaultQuotationDigest(useInternalDatabase));
        return digestsExcluded;
    }

    @NonNull
    public Single<Integer> countAllExternal() {
        return getQuotationDAO(false).countAll();
    }

    public int countPreviousCriteria(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        return getPreviousDAO(useInternalDatabase).countPrevious(widgetId, contentSelection);
    }

    public int countPreviousCriteria(boolean useInternalDatabase, final int widgetId) {
        return getPreviousDAO(useInternalDatabase).countPrevious(widgetId);
    }

    public int findPositionInPrevious(
        boolean useInternalDatabase,
        final int widgetId,
        @NonNull final QuotationsPreferences quotationsPreferences) {

        List<String> allPrevious = getPreviousDigests(
            useInternalDatabase,
            widgetId,
            quotationsPreferences.getContentSelection(),
            quotationsPreferences.getContentSelectionAllExclusion());

        Collections.reverse(allPrevious);

        int position = 0;
        if (!allPrevious.isEmpty()) {
            String currentDigest = getCurrentQuotation(useInternalDatabase, widgetId).digest;
            position = allPrevious.indexOf(currentDigest) + 1;
        }

        return position;
    }

    public ArrayList<QuotationEntity> getQuotationsForAuthor(boolean useInternalDatabase, @NonNull final String author) {
        ArrayList<String> digestsForAuthor = new ArrayList(getQuotationDAO(useInternalDatabase).getDigestsForAuthor(author));
        ArrayList<QuotationEntity> quotationEntityList = new ArrayList<>();

        for (String digest : digestsForAuthor) {
            quotationEntityList.add(getQuotation(useInternalDatabase, digest));
        }

        return quotationEntityList;
    }

    public int countNext(
        boolean useInternalDatabase,
        @NonNull final QuotationsPreferences quotationsPreferences) {
        int countTotalNext;

        switch (quotationsPreferences.getContentSelection()) {
            case FAVOURITES:
                countTotalNext = countFavourites(useInternalDatabase).blockingGet();
                break;

            case AUTHOR:
                countTotalNext
                    = getQuotationDAO(useInternalDatabase).getDigestsForAuthor(
                    quotationsPreferences.getContentSelectionAuthor()).size();
                break;

            case SEARCH:
                if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                    countTotalNext = getSearchQuotationsRegEx(
                        useInternalDatabase,
                        quotationsPreferences.getWidgetId(),
                        quotationsPreferences.getContentSelectionSearch(),
                        quotationsPreferences.getContentSelectionSearchFavouritesOnly()).size();
                } else {
                    countTotalNext = getSearchQuotations(
                        useInternalDatabase,
                        quotationsPreferences.getWidgetId(),
                        quotationsPreferences.getContentSelectionSearch(),
                        quotationsPreferences.getContentSelectionSearchFavouritesOnly()).size();
                }
                break;

            default:
                // ALL:
                countTotalNext
                    = countAllMinusExclusions(
                    useInternalDatabase,
                    quotationsPreferences.getContentSelectionAllExclusion())
                    .blockingGet();
                break;
        }
        return countTotalNext;
    }

    public int countPreviousCriteria(
        boolean useInternalDatabase,
        final int widgetId,
        @NonNull final ContentSelection contentSelection,
        @NonNull final QuotationsPreferences quotationsPreferences
    ) {
        HashSet<String> previousDigests = new HashSet<>(getPreviousDigests(useInternalDatabase, widgetId, contentSelection, ""));
        HashSet<String> availableDigests;

        if (contentSelection == ContentSelection.AUTHOR) {
            availableDigests = new HashSet<>(getQuotationDAO(useInternalDatabase).getDigestsForAuthor(
                quotationsPreferences.getContentSelectionAuthor()
            ));
        } else {

            List<QuotationEntity> searchQuotations = getSearchQuotations(
                useInternalDatabase,
                quotationsPreferences.getWidgetId(),
                quotationsPreferences.getContentSelectionSearch(),
                quotationsPreferences.getContentSelectionSearchFavouritesOnly());

            availableDigests = new HashSet<>();

            for (QuotationEntity quotationEntity : searchQuotations) {
                availableDigests.add(quotationEntity.digest);
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
    public Single<Integer> countFavourites(boolean useInternalDatabase) {
        return getFavouriteDAO(useInternalDatabase).countFavourites();
    }

    @NonNull
    public String getLastPreviousDigest(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        PreviousEntity previousEntity = getPreviousDAO(useInternalDatabase).getLastPrevious(widgetId, contentSelection);

        if (previousEntity == null) {
            return getDefaultQuotationDigest(useInternalDatabase);
        }

        return previousEntity.digest;
    }

    @NonNull
    public List<String> getPreviousDigests(
        boolean useInternalDatabase,
        final int widgetId,
        @NonNull final ContentSelection contentSelection,
        @NonNull final String criteria) {

        List<String> previousDigests = getPreviousDAO(useInternalDatabase).getPreviousDigests(widgetId, contentSelection);

        if (contentSelection.equals(ContentSelection.ALL)) {
            previousDigests.removeAll(getAllExcludedDigests(useInternalDatabase, criteria));
        }

        return previousDigests;
    }

    @NonNull
    public List<PreviousEntity> getPrevious(boolean useInternalDatabase) {
        return getPreviousDAO(useInternalDatabase).getAllPrevious();
    }

    @NonNull
    public void alignHistoryWithQuotations(boolean useInternalDatabase, int widgetId, @NonNull Context context) {
        boolean alignPrevious = alignPreviousWithQuotations(useInternalDatabase);
        boolean alignFavourites = alignFavouritesWithQuotations(useInternalDatabase);
        boolean alignmentRequired = alignPrevious || alignFavourites;

        if (alignmentRequired) {
            QuotationsPreferences quotationsPreferences
                = new QuotationsPreferences(widgetId, context);
            quotationsPreferences.setContentSelection(ContentSelection.ALL);

            markAsCurrent(
                useInternalDatabase,
                widgetId,
                getLastPreviousDigest(useInternalDatabase, widgetId, ContentSelection.ALL));
        }
    }

    private boolean alignPreviousWithQuotations(boolean useInternalDatabase) {
        boolean alignmentRequired = false;
        PreviousDAO previousDAO = getPreviousDAO(useInternalDatabase);
        CurrentDAO currentDAO = getCurrentDAO(useInternalDatabase);

        for (PreviousEntity previousEntity : previousDAO.getAllPrevious()) {
            if (getQuotation(useInternalDatabase, previousEntity.digest) == null) {
                Timber.d("alignPrevious=%s", previousEntity.digest);
                alignmentRequired = true;
                currentDAO.erase(previousEntity.digest);
                previousDAO.erase(previousEntity.digest);
                if (cacheCurrentQuotation != null) {
                    cacheCurrentQuotation.clear();
                }
            }
        }
        return alignmentRequired;
    }

    private boolean alignFavouritesWithQuotations(boolean useInternalDatabase) {
        boolean alignmentRequired = false;
        FavouriteDAO favouriteDAO = getFavouriteDAO(useInternalDatabase);
        QuotationDAO quotationDAO = getQuotationDAO(useInternalDatabase);

        List<String> favouriteDigests = getFavouritesDigests(useInternalDatabase);
        final HashSet<String> quotationDigests = new HashSet<>(quotationDAO.getDigests());
        for (String favouriteDigest : favouriteDigests) {
            if (!quotationDigests.contains(favouriteDigest)) {
                Timber.d("alignFavourite=%s", favouriteDigest);
                alignmentRequired = true;
                favouriteDAO.erase(favouriteDigest);
                if (cacheFavouriteDigests != null) {
                    cacheFavouriteDigests.remove(useInternalDatabase);
                }
            }
        }
        return alignmentRequired;
    }

    @NonNull
    public synchronized List<String> getFavouritesDigests(boolean useInternalDatabase) {
        if (cacheFavouriteDigests != null) {
            List<String> cached = cacheFavouriteDigests.get(useInternalDatabase);
            if (cached != null) {
                return new ArrayList<>(cached);
            }
        }

        List<String> favouriteDigests = getFavouriteDAO(useInternalDatabase).getFavouriteDigests();
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.put(useInternalDatabase, favouriteDigests);
        }
        return new ArrayList<>(favouriteDigests);
    }

    @NonNull
    public List<FavouriteEntity> getFavourites(boolean useInternalDatabase) {
        return getFavouriteDAO(useInternalDatabase).getFavourites();
    }

    @NonNull
    public Single<List<Integer>> getAuthorsQuotationCount(boolean useInternalDatabase) {
        return getQuotationDAO(useInternalDatabase).getAuthorsQuotationCount();
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts(boolean useInternalDatabase, int authorCount) {
        return getQuotationDAO(useInternalDatabase).getAuthorsAndQuotationCounts(authorCount);
    }

    @NonNull
    public synchronized List<QuotationEntity> getSearchQuotations(boolean useInternalDatabase, int widgetId, @NonNull final String text, boolean favouritesOnly) {
        SearchCacheKey key = new SearchCacheKey(useInternalDatabase, text, favouritesOnly, false);
        List<QuotationEntity> cached = searchCache.get(key);
        List<QuotationEntity> searchQuotations;

        if (cached != null) {
            searchQuotations = new ArrayList<>(cached);
        } else {
            if (favouritesOnly) {
                final String lowerText = text.toLowerCase();
                searchQuotations = searchInternal(useInternalDatabase, true, quotationEntity ->
                    quotationEntity.author.toLowerCase().contains(lowerText)
                        || quotationEntity.quotation.toLowerCase().contains(lowerText));
            } else {
                searchQuotations = getQuotationDAO(useInternalDatabase).getQuotationsByText(text);
            }

            if (!searchQuotations.isEmpty()) {
                searchCache.put(key, new ArrayList<>(searchQuotations));
            }
        }

        return searchQuotations;
    }

    @NonNull
    private List<QuotationEntity> searchInternal(boolean useInternalDatabase, boolean favouritesOnly, SearchPredicate predicate) {
        List<QuotationEntity> results = new ArrayList<>();
        if (favouritesOnly) {
            for (String digest : getFavouritesDigests(useInternalDatabase)) {
                QuotationEntity quotationEntity = getQuotation(useInternalDatabase, digest);
                if (quotationEntity != null && predicate.test(quotationEntity)) {
                    results.add(quotationEntity);
                }
            }
            Collections.reverse(results);
        } else {
            for (QuotationEntity quotationEntity : getAllQuotations(useInternalDatabase)) {
                if (predicate.test(quotationEntity)) {
                    results.add(quotationEntity);
                }
            }
        }
        return results;
    }

    @NonNull
    public Integer countSearchTextRegEx(boolean useInternalDatabase, @NonNull final String regEx, boolean favouritesOnly) {
        return countSearchTextRegEx(useInternalDatabase, -1, regEx, favouritesOnly);
    }

    @NonNull
    public Integer countSearchTextRegEx(boolean useInternalDatabase, int widgetId, @NonNull final String regEx, boolean favouritesOnly) {
        return getSearchQuotationsRegEx(useInternalDatabase, widgetId, regEx, favouritesOnly).size();
    }

    @NonNull
    public Integer countSearchText(boolean useInternalDatabase, @NonNull final String text, boolean favouritesOnly) {
        return countSearchText(useInternalDatabase, -1, text, favouritesOnly);
    }

    @NonNull
    public Integer countSearchText(boolean useInternalDatabase, int widgetId, @NonNull final String text, boolean favouritesOnly) {
        return getSearchQuotations(useInternalDatabase, widgetId, text, favouritesOnly).size();
    }

    protected synchronized void clearCache(boolean useInternalDatabase) {
        searchCache.evictAll();
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.remove(useInternalDatabase);
        }
        if (cacheCurrentQuotation != null) {
            cacheCurrentQuotation.clear();
        }
        if (useInternalDatabase) {
            cacheInternalQuotations = null;
            cacheInternalQuotationsMap = null;
        } else {
            cacheExternalQuotations = null;
            cacheExternalQuotationsMap = null;
        }
    }

    @NonNull
    public synchronized List<QuotationEntity> getAllQuotations(boolean useInternalDatabase) {
        populateCache(useInternalDatabase);
        List<QuotationEntity> cache = useInternalDatabase ? cacheInternalQuotations : cacheExternalQuotations;
        if (cache == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cache);
    }

    private synchronized void populateCache(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            if (cacheInternalQuotations == null) {
                cacheInternalQuotations = new ArrayList<>();
                QuotationEntity defaultQuotation = getQuotationDAO(true).getQuotation(DatabaseRepository.getDefaultQuotationDigest(true));
                if (defaultQuotation != null) {
                    cacheInternalQuotations.add(defaultQuotation);
                }
                cacheInternalQuotations.addAll(getQuotationDAO(true).getAllQuotations());

                cacheInternalQuotationsMap = new LinkedHashMap<>();
                for (QuotationEntity entity : cacheInternalQuotations) {
                    cacheInternalQuotationsMap.put(entity.digest, entity);
                }
            }
        } else {
            if (cacheExternalQuotations == null) {
                cacheExternalQuotations = new ArrayList<>(getQuotationDAO(false).getAllQuotationsIncludingDefault());

                cacheExternalQuotationsMap = new LinkedHashMap<>();
                for (QuotationEntity entity : cacheExternalQuotations) {
                    cacheExternalQuotationsMap.put(entity.digest, entity);
                }
            }
        }
    }

    public synchronized void markAsFavourite(boolean useInternalDatabase, @NonNull final String digest) {
        searchCache.evictAll();
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.remove(useInternalDatabase);
        }
        if (getQuotation(useInternalDatabase, digest) != null) {
            if (getFavouriteDAO(useInternalDatabase).isFavourite(digest) == 0) {
                getFavouriteDAO(useInternalDatabase).markAsFavourite(new FavouriteEntity(digest));
            }
        }
    }

    public synchronized QuotationEntity getQuotation(boolean useInternalDatabase, @NonNull final String digest) {
        if (useInternalDatabase) {
            if (cacheInternalQuotationsMap != null) {
                return cacheInternalQuotationsMap.get(digest);
            }
        } else {
            if (cacheExternalQuotationsMap != null) {
                return cacheExternalQuotationsMap.get(digest);
            }
        }
        return getQuotationDAO(useInternalDatabase).getQuotation(digest);
    }

    public void markAsPrevious(
        boolean useInternalDatabase,
        final int widgetId,
        @NonNull final ContentSelection contentSelection,
        @NonNull final String digest) {
        if (getQuotation(useInternalDatabase, digest) != null) {
            getPreviousDAO(useInternalDatabase).markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
        }
    }

    public synchronized void eraseFavourite(boolean useInternalDatabase, final int widgetId, @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        searchCache.evictAll();
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.remove(useInternalDatabase);
        }

        getFavouriteDAO(useInternalDatabase).erase(digest);
        getPreviousDAO(useInternalDatabase).erase(widgetId, ContentSelection.FAVOURITES, digest);
    }

    public synchronized void markAsCurrent(
        boolean useInternalDatabase,
        final int widgetId,
        @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        getCurrentDAO(useInternalDatabase).erase(widgetId);
        getCurrentDAO(useInternalDatabase).markAsCurrent(new CurrentEntity(widgetId, digest));

        if (cacheCurrentQuotation != null) {
            String key = useInternalDatabase + ":" + widgetId;
            cacheCurrentQuotation.put(key, getQuotation(useInternalDatabase, digest));
        }
    }

    public synchronized QuotationEntity getCurrentQuotation(boolean useInternalDatabase, final int widgetId) {
        String key = useInternalDatabase + ":" + widgetId;
        if (cacheCurrentQuotation != null && cacheCurrentQuotation.containsKey(key)) {
            return cacheCurrentQuotation.get(key);
        }

        QuotationEntity entity = getQuotation(useInternalDatabase, getCurrentDAO(useInternalDatabase).getCurrentDigest(widgetId));
        if (entity != null && cacheCurrentQuotation != null) {
            cacheCurrentQuotation.put(key, entity);
        }
        return entity;
    }

    @NonNull
    public QuotationEntity getNextQuotation(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        return getQuotation(useInternalDatabase, getLastPreviousDigest(useInternalDatabase, widgetId, contentSelection));
    }

    @NonNull
    public QuotationEntity getNextQuotation(
        boolean useInternalDatabase,
        final int widgetId,
        @NonNull final ContentSelection contentSelection,
        @NonNull final String criteria,
        final boolean randomNext,
        QuotationsPreferences quotationsPreferences) {
        Timber.d("contentType=%d; criteria=%s; randomNext=%b",
            contentSelection.getContentSelection(), criteria, randomNext);

        List<String> nextDigests = getNextDigests(useInternalDatabase, widgetId, contentSelection, criteria, quotationsPreferences);
        List<String> previousDigests = getPreviousDigests(useInternalDatabase, widgetId, contentSelection, criteria);

        if (!randomNext) {
            // Next, Sequential
            if (nextDigests.size() == 0) {
                // recycle
                erasePrevious(useInternalDatabase, widgetId, contentSelection, previousDigests);
                nextDigests = getNextDigests(useInternalDatabase, widgetId, contentSelection, criteria, quotationsPreferences);
                previousDigests = getPreviousDigests(useInternalDatabase, widgetId, contentSelection, criteria);
            }
        }

        return getNextQuotation(
            useInternalDatabase,
            widgetId,
            randomNext,
            nextDigests,
            previousDigests,
            contentSelection
        );
    }

    @NonNull
    private QuotationEntity getNextQuotation(
        boolean useInternalDatabase,
        final int widgetId,
        final boolean randomNext,
        @Nullable final List<String> nextDigests,
        @Nullable final List<String> previousDigests,
        @NonNull final ContentSelection contentSelection) {

        QuotationEntity currentQuotation = getCurrentQuotation(useInternalDatabase, widgetId);
        QuotationEntity nextQuotation;

        if (!randomNext) {
            // next, sequential
            if (previousDigests.isEmpty()) {
                if (nextDigests.isEmpty()) {
                    return getQuotation(useInternalDatabase, getDefaultQuotationDigest(useInternalDatabase));
                }
                nextQuotation = getQuotation(useInternalDatabase, nextDigests.get(0));
            } else {
                int indexInPrevious = previousDigests.indexOf(currentQuotation.digest);

                if (indexInPrevious != -1 && indexInPrevious != 0) {
                    // move through previous quotations
                    indexInPrevious -= 1;
                    nextQuotation = getQuotation(useInternalDatabase, previousDigests.get(indexInPrevious));
                } else {
                    if (nextDigests.isEmpty()) {
                        return getQuotation(useInternalDatabase, getDefaultQuotationDigest(useInternalDatabase));
                    }
                    nextQuotation = getQuotation(useInternalDatabase, nextDigests.get(0));
                }
            }
        } else {
            // next, random
            if (!nextDigests.isEmpty()) {
                nextQuotation = getQuotation(useInternalDatabase, nextDigests.get(getRandomIndex(nextDigests)));
            } else {
                // we've run out of new quotations
                nextQuotation = currentQuotation;
                markAsCurrent(useInternalDatabase, widgetId, getLastPreviousDigest(useInternalDatabase, widgetId, contentSelection));
            }
        }

        return nextQuotation;
    }

    public synchronized List<String> getNextDigests(
        boolean useInternalDatabase,
        int widgetId,
        @NonNull ContentSelection contentSelection,
        @NonNull String criteria,
        QuotationsPreferences quotationsPreferences) {
        // insertion order required
        final LinkedHashSet<String> nextQuotationDigests;

        // no order needed
        final HashSet<String> previousDigests
            = new HashSet<>(getPreviousDigests(useInternalDatabase, widgetId, contentSelection, criteria));

        switch (contentSelection) {
            case FAVOURITES:
                nextQuotationDigests
                    = new LinkedHashSet<>(getFavouriteDAO(useInternalDatabase).getNextFavouriteDigests());
                nextQuotationDigests.removeAll(previousDigests);
                break;

            case AUTHOR:
                LinkedHashSet<String> authorDigests
                    = new LinkedHashSet<>(getQuotationDAO(useInternalDatabase).getNextAuthorDigest(criteria));
                authorDigests.removeAll(previousDigests);
                nextQuotationDigests = authorDigests;
                break;

            case SEARCH:
                if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                    nextQuotationDigests = getSearchDigestsRegEx(useInternalDatabase, quotationsPreferences, previousDigests);
                } else {
                    nextQuotationDigests = getSearchDigests(useInternalDatabase, quotationsPreferences, previousDigests);
                }
                break;

            default:
                // ALL:
                final LinkedHashSet<String> allAvailableDigests
                    = new LinkedHashSet<>(getQuotationDAO(useInternalDatabase).getNextAllDigests());

                allAvailableDigests.removeAll(getAllExcludedDigests(useInternalDatabase, criteria));

                allAvailableDigests.removeAll(previousDigests);
                nextQuotationDigests = allAvailableDigests;
                break;
        }

        return new ArrayList<>(nextQuotationDigests);
    }

    @NonNull
    private LinkedHashSet<String> getSearchDigestsRegEx(
        boolean useInternalDatabase,
        QuotationsPreferences quotationsPreferences, HashSet<String> previousDigests) {
        final LinkedHashSet<String> searchDigests = new LinkedHashSet<>();

        List<QuotationEntity> searchQuotations = getSearchQuotationsRegEx(
            useInternalDatabase,
            quotationsPreferences.getWidgetId(),
            quotationsPreferences.getContentSelectionSearch(),
            quotationsPreferences.getContentSelectionSearchFavouritesOnly());

        for (QuotationEntity quotationEntity : searchQuotations) {
            searchDigests.add(quotationEntity.digest);
        }

        searchDigests.removeAll(previousDigests);
        return searchDigests;
    }

    @NonNull
    private LinkedHashSet<String> getSearchDigests(
        boolean useInternalDatabase,
        QuotationsPreferences quotationsPreferences, HashSet<String> previousDigests) {
        final LinkedHashSet<String> searchDigests = new LinkedHashSet<>();

        List<QuotationEntity> searchQuotations = getSearchQuotations(
            useInternalDatabase,
            quotationsPreferences.getWidgetId(),
            quotationsPreferences.getContentSelectionSearch(),
            quotationsPreferences.getContentSelectionSearchFavouritesOnly());

        for (QuotationEntity quotationEntity : searchQuotations) {
            searchDigests.add(quotationEntity.digest);
        }

        searchDigests.removeAll(previousDigests);
        return searchDigests;
    }

    public int getRandomIndex(@NonNull final List<String> availableNextQuotations) {
        return secureRandom.nextInt(availableNextQuotations.size());
    }

    public synchronized void deleteFavourite(boolean useInternalDatabase, String digest) {
        searchCache.evictAll();
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.remove(useInternalDatabase);
        }
        getFavouriteDAO(useInternalDatabase).erase(digest);
    }

    public void erasePrevious(
        boolean useInternalDatabase,
        int widgetId,
        @NonNull ContentSelection contentSelection,
        @NonNull List<String> previousDigests) {
        for (String digest : previousDigests) {
            getPreviousDAO(useInternalDatabase).erase(widgetId, contentSelection, digest);
        }
    }

    public void erase(boolean useInternalDatabase) {
        clearCache(useInternalDatabase);
        getPreviousDAO(useInternalDatabase).erase();
        getCurrentDAO(useInternalDatabase).erase();
        getFavouriteDAO(useInternalDatabase).erase();

        if (!useInternalDatabase) {
            getQuotationDAO(false).eraseQuotations();
        }
    }

    public synchronized void eraseForRestore() {
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.clear();
        }
        if (cacheCurrentQuotation != null) {
            cacheCurrentQuotation.clear();
        }
        getPreviousDAO(true).erase();
        getCurrentDAO(true).erase();
        getFavouriteDAO(true).erase();
        getPreviousDAO(false).erase();
        getCurrentDAO(false).erase();
        getFavouriteDAO(false).erase();
    }

    public synchronized void erase(boolean useInternalDatabase, final int widgetId) {
        if (cacheCurrentQuotation != null) {
            cacheCurrentQuotation.remove(useInternalDatabase + ":" + widgetId);
        }
        getPreviousDAO(useInternalDatabase).erase(widgetId);
        getCurrentDAO(useInternalDatabase).erase(widgetId);
    }

    public synchronized void erase(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (cacheCurrentQuotation != null) {
            cacheCurrentQuotation.remove(useInternalDatabase + ":" + widgetId);
        }
        getPreviousDAO(useInternalDatabase).erase(widgetId, contentSelection);
        getCurrentDAO(useInternalDatabase).erase(widgetId);
    }

    @NonNull
    public Boolean isFavourite(boolean useInternalDatabase, @NonNull final String digest) {
        return getFavouriteDAO(useInternalDatabase).isFavourite(digest) > 0;
    }

    @Transaction
    public void insertQuotationsExternal(
        @NonNull final LinkedHashSet<QuotationEntity> quotationEntityList) {
        clearCache(false);
        getQuotationDAO(false).insertQuotations(new ArrayList<>(quotationEntityList));
    }

    public void insertQuotationExternal(QuotationEntity quotationEntity) {
        clearCache(false);
        try {
            getQuotationDAO(false).insertQuotation(quotationEntity);
        } catch (SQLiteConstraintException e) {
            Timber.e(e.getMessage());
        }
    }

    public void updateQuotationUsingDigest(boolean useInternalDatabase, String digest, String author, String quotation) {
        clearCache(useInternalDatabase);
        getQuotationDAO(useInternalDatabase).updateQuotationUsingDigest(
            digest,
            author,
            quotation);
    }

    public void updateQuotationUsingAuthorQuotation(boolean useInternalDatabase, String digest, String author, String quotation) {
        clearCache(useInternalDatabase);
        getQuotationDAO(useInternalDatabase).updateQuotationUsingAuthorQuotation(
            digest,
            author,
            quotation);
    }

    public void deleteQuotation(boolean useInternalDatabase, String digest) {
        clearCache(useInternalDatabase);
        getQuotationDAO(useInternalDatabase).eraseQuotations(digest);
    }

    public void deletePrevious(boolean useInternalDatabase, String digest) {
        getPreviousDAO(useInternalDatabase).erase(digest);
    }

    public synchronized void insertFavourites(@NonNull final List<FavouriteEntity> entities, final boolean internal) {
        if (cacheFavouriteDigests != null) {
            cacheFavouriteDigests.remove(internal);
        }
        // deduplicate by digest to prevent duplicate rows
        final List<FavouriteEntity> deduped = new ArrayList<>();
        final HashSet<String> seenDigests = new HashSet<>();
        for (final FavouriteEntity entity : entities) {
            if (seenDigests.add(entity.digest)) {
                deduped.add(entity);
            }
        }
        getFavouriteDAO(internal).insertFavourites(deduped);
    }

    public void insertPrevious(@NonNull final List<PreviousEntity> entities, final boolean internal) {
        // deduplicate by (widgetId, contentType, digest) to prevent duplicate rows
        final List<PreviousEntity> deduped = new ArrayList<>();
        final HashSet<String> seenKeys = new HashSet<>();
        for (final PreviousEntity entity : entities) {
            if (seenKeys.add(entity.widgetId + "|" + entity.contentType.getContentSelection() + "|" + entity.digest)) {
                deduped.add(entity);
            }
        }
        getPreviousDAO(internal).insertPrevious(deduped);
    }

    // # Batch restore methods

    public synchronized void insertCurrent(@NonNull final List<CurrentEntity> entities, final boolean internal) {
        if (cacheCurrentQuotation != null) {
            cacheCurrentQuotation.clear();
        }
        getCurrentDAO(internal).insertCurrents(entities);
    }

    private interface SearchPredicate {
        boolean test(QuotationEntity entity);
    }

    private static class SearchCacheKey {
        final boolean useInternalDatabase;
        @NonNull
        final String query;
        final boolean favouritesOnly;
        final boolean isRegEx;

        SearchCacheKey(boolean useInternalDatabase, @NonNull String query, boolean favouritesOnly, boolean isRegEx) {
            this.useInternalDatabase = useInternalDatabase;
            this.query = query;
            this.favouritesOnly = favouritesOnly;
            this.isRegEx = isRegEx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SearchCacheKey that = (SearchCacheKey) o;
            return useInternalDatabase == that.useInternalDatabase &&
                favouritesOnly == that.favouritesOnly &&
                isRegEx == that.isRegEx &&
                query.equals(that.query);
        }

        @Override
        public int hashCode() {
            int result = (useInternalDatabase ? 1 : 0);
            result = 31 * result + query.hashCode();
            result = 31 * result + (favouritesOnly ? 1 : 0);
            result = 31 * result + (isRegEx ? 1 : 0);
            return result;
        }
    }
}
