package com.github.jameshnsears.quoteunquote.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import timber.log.Timber;

public class DatabaseRepository {
    @SuppressLint("StaticFieldLeak")
    @NonNull
    public static DatabaseRepository databaseRepository;

    @NonNull
    protected final SecureRandom secureRandom = new SecureRandom();

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

    public DatabaseRepository() {
        //
    }

    protected DatabaseRepository(@NonNull final Context context) {
        quotationDatabase = QuotationDatabase.getDatabase(context);
        quotationDAO = quotationDatabase.quotationDAO();

        externalDatabase = ExternalDatabase.getDatabase(context);
        quotationExternalDAO = externalDatabase.quotationExternalDAO();

        historyDatabase = HistoryDatabase.getDatabase(context);
        previousDAO = historyDatabase.previousDAO();
        favouriteDAO = historyDatabase.favouritesDAO();
        currentDAO = historyDatabase.currentDAO();

        historyExternalDatabase = HistoryExternalDatabase.getDatabase(context);
        previousExternalDAO = historyExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = historyExternalDatabase.favouritesExternalDAO();
        currentExternalDAO = historyExternalDatabase.currentExternalDAO();

        this.context = context;
    }

    public static void close(@NonNull final Context context) {
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
            databaseRepository = new DatabaseRepository(context);
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
    public Single<Integer> countAll(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return quotationDAO.countAll();
        }

        return countAllExternal();
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
                if (useInternalDatabase) {
                    digestsExcluded.addAll(quotationDAO.getExclusionDigests(exclusion));
                } else {
                    digestsExcluded.addAll(quotationExternalDAO.getExclusionDigests(exclusion));
                }
            }
        }

        // we keep the default quotation
        digestsExcluded.remove(getDefaultQuotationDigest(useInternalDatabase));
        return digestsExcluded;
    }

    @NonNull
    public Single<Integer> countAllExternal() {
        return quotationExternalDAO.countAll();
    }

    public int countPreviousCriteria(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase) {
            return previousDAO.countPrevious(widgetId, contentSelection);
        }

        return previousExternalDAO.countPrevious(widgetId, contentSelection);
    }

    public int countPreviousDigest(
            boolean useInternalDatabase,
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull String digest) {
        if (useInternalDatabase) {
            return previousDAO.countPreviousDigest(widgetId, contentSelection, digest);
        }

        return previousExternalDAO.countPreviousDigest(widgetId, contentSelection, digest);
    }

    public int countPreviousCriteria(boolean useInternalDatabase, final int widgetId) {
        if (useInternalDatabase) {
            return previousDAO.countPrevious(widgetId);
        }

        return previousExternalDAO.countPrevious(widgetId);
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
        ArrayList<String> digestsForAuthor;
        ArrayList<QuotationEntity> quotationEntityList = new ArrayList<>();

        if (useInternalDatabase) {
            digestsForAuthor = new ArrayList(quotationDAO.getDigestsForAuthor(author));

            for (String digest : digestsForAuthor) {
                quotationEntityList.add(quotationDAO.getQuotation(digest));
            }
        } else {
            digestsForAuthor = new ArrayList(quotationExternalDAO.getDigestsForAuthor(author));

            for (String digest : digestsForAuthor) {
                quotationEntityList.add(quotationExternalDAO.getQuotation(digest));
            }
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
                if (useInternalDatabase) {
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
                if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                    countTotalNext = getSearchQuotationsRegEx(
                            useInternalDatabase,
                            quotationsPreferences.getContentSelectionSearch(),
                            quotationsPreferences.getContentSelectionSearchFavouritesOnly()).size();
                } else {
                    countTotalNext = getSearchQuotations(
                            useInternalDatabase,
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
        HashSet<String> previousDigests;
        HashSet<String> availableDigests;

        if (contentSelection == ContentSelection.AUTHOR) {
            previousDigests = new HashSet<>(getPreviousDigests(useInternalDatabase, widgetId, ContentSelection.AUTHOR, ""));
            if (useInternalDatabase) {
                availableDigests = new HashSet<>(quotationDAO.getDigestsForAuthor(
                        quotationsPreferences.getContentSelectionAuthor()
                ));
            } else {
                availableDigests = new HashSet<>(quotationExternalDAO.getDigestsForAuthor(
                        quotationsPreferences.getContentSelectionAuthor()
                ));
            }
        } else {
            previousDigests = new HashSet<>(getPreviousDigests(useInternalDatabase, widgetId, ContentSelection.SEARCH, ""));

            List<QuotationEntity> searchQuotations = getSearchQuotations(
                    useInternalDatabase,
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
        if (useInternalDatabase) {
            return favouriteDAO.countFavourites();
        } else {
            return favouriteExternalDAO.countFavourites();
        }
    }

    @NonNull
    public String getLastPreviousDigest(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        PreviousEntity previousEntity;

        if (useInternalDatabase) {
            previousEntity = previousDAO.getLastPrevious(widgetId, contentSelection);
        } else {
            previousEntity = previousExternalDAO.getLastPrevious(widgetId, contentSelection);
        }

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

        List<String> previousDigests;

        if (useInternalDatabase) {
            previousDigests = previousDAO.getPreviousDigests(widgetId, contentSelection);
        } else {
            previousDigests = previousExternalDAO.getPreviousDigests(widgetId, contentSelection);
        }

        if (contentSelection.equals(ContentSelection.ALL)) {
            previousDigests.removeAll(getAllExcludedDigests(useInternalDatabase, criteria));
        }

        return previousDigests;
    }

    @NonNull
    public List<PreviousEntity> getPrevious(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return previousDAO.getAllPrevious();
        } else {
            return previousExternalDAO.getAllPrevious();
        }
    }

    @NonNull
    public void alignHistoryWithQuotations(boolean useInternalDatabase, int widgetId, @NonNull Context context) {
        boolean alignmentRequired = false;

        if (useInternalDatabase) {
            for (PreviousEntity previousEntity : previousDAO.getAllPrevious()) {
                if (quotationDAO.getQuotation(previousEntity.digest) == null) {
                    Timber.d("alignPrevious=%s", previousEntity.digest);
                    alignmentRequired = true;
                    currentDAO.erase(previousEntity.digest);
                    previousDAO.erase(previousEntity.digest);
                }
            }

            List<String> favouriteDigests = favouriteDAO.getFavouriteDigests();
            final HashSet<String> quotationDigests
                    = new HashSet<>(quotationDAO.getDigests());
            for (String favouriteDigest : favouriteDigests) {
                if (!quotationDigests.contains(favouriteDigest)) {
                    Timber.d("alignFavourite=%s", favouriteDigest);
                    alignmentRequired = true;

                    favouriteDAO.erase(favouriteDigest);
                }
            }
        } else {
            for (PreviousEntity previousEntity : previousExternalDAO.getAllPrevious()) {
                if (quotationExternalDAO.getQuotation(previousEntity.digest) == null) {
                    Timber.d("align=%s", previousEntity.digest);
                    alignmentRequired = true;
                    currentExternalDAO.erase(previousEntity.digest);
                    previousExternalDAO.erase(previousEntity.digest);
                }
            }

            List<String> favouriteDigests = favouriteExternalDAO.getFavouriteDigests();
            final HashSet<String> quotationDigests
                    = new HashSet<>(quotationExternalDAO.getDigests());
            for (String favouriteDigest : favouriteDigests) {
                if (!quotationDigests.contains(favouriteDigest)) {
                    Timber.d("alignFavourite=%s", favouriteDigest);
                    alignmentRequired = true;

                    favouriteExternalDAO.erase(favouriteDigest);
                }
            }
        }

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

    @NonNull
    public List<String> getFavouritesDigests(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return favouriteDAO.getFavouriteDigests();
        } else {
            return favouriteExternalDAO.getFavouriteDigests();
        }
    }

    @NonNull
    public List<FavouriteEntity> getFavourites(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return favouriteDAO.getFavourites();
        } else {
            return favouriteExternalDAO.getFavourites();
        }
    }

    @NonNull
    public Single<List<Integer>> getAuthorsQuotationCount(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            return quotationDAO.getAuthorsQuotationCount();
        } else {
            return quotationExternalDAO.getAuthorsQuotationCount();
        }
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts(boolean useInternalDatabase, int authorCount) {
        if (useInternalDatabase) {
            return quotationDAO.getAuthorsAndQuotationCounts(authorCount);
        } else {
            return quotationExternalDAO.getAuthorsAndQuotationCounts(authorCount);
        }
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotationsRegEx(boolean useInternalDatabase, @NonNull final String regEx, boolean favouritesOnly) {
        List<QuotationEntity> searchQuotations = new ArrayList<>();

        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);

        if (favouritesOnly) {
            if (useInternalDatabase) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);
                    if (isFavourite(useInternalDatabase, quotationEntity.digest)) {
                        Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                        Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                        if (matcherAuthor.find() || matcherQuotation.find()) {
                            searchQuotations.add(quotationEntity);
                        }
                    }
                }
            } else {
                for (String digest : favouriteExternalDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationExternalDAO.getQuotation(digest);
                    if (isFavourite(useInternalDatabase, quotationEntity.digest)) {
                        Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                        Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                        if (matcherAuthor.find() || matcherQuotation.find()) {
                            searchQuotations.add(quotationEntity);
                        }
                    }
                }
            }

            Collections.reverse(searchQuotations);
        } else {
            List<QuotationEntity> allQuotations = getAllQuotations(useInternalDatabase);

            for (QuotationEntity quotationEntity : allQuotations) {
                Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                if (matcherAuthor.find() || matcherQuotation.find()) {
                    searchQuotations.add(quotationEntity);
                }
            }
        }

        return searchQuotations;
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotations(boolean useInternalDatabase, @NonNull final String text, boolean favouritesOnly) {
        List<QuotationEntity> searchQuotations = new ArrayList<>();

        if (favouritesOnly) {
            if (useInternalDatabase) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);
                    if (isFavourite(useInternalDatabase, quotationEntity.digest)) {
                        String author = quotationEntity.author.toLowerCase();
                        String quotation = quotationEntity.quotation.toLowerCase();

                        if (author.indexOf(text.toLowerCase()) != -1
                                || quotation.indexOf(text.toLowerCase()) != -1) {
                            searchQuotations.add(quotationEntity);
                        }
                    }
                }
            } else {
                for (String digest : favouriteExternalDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationExternalDAO.getQuotation(digest);
                    if (isFavourite(useInternalDatabase, quotationEntity.digest)) {
                        String author = quotationEntity.author.toLowerCase();
                        String quotation = quotationEntity.quotation.toLowerCase();

                        if (author.indexOf(text.toLowerCase()) != -1
                                || quotation.indexOf(text.toLowerCase()) != -1) {
                            searchQuotations.add(quotationEntity);
                        }
                    }
                }
            }

            Collections.reverse(searchQuotations);
        } else {
            for (QuotationEntity currentQuotation : getAllQuotations(useInternalDatabase)) {
                String author = currentQuotation.author.toLowerCase();
                String quotation = currentQuotation.quotation.toLowerCase();

                if (author.indexOf(text.toLowerCase()) != -1
                        || quotation.indexOf(text.toLowerCase()) != -1) {
                    searchQuotations.add(currentQuotation);
                }
            }
        }

        return searchQuotations;
    }

    @NonNull
    public Integer countSearchTextRegEx(boolean useInternalDatabase, @NonNull final String regEx, boolean favouritesOnly) {
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        int searchCount = 0;

        if (favouritesOnly) {
            if (useInternalDatabase) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);

                    Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                    Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                    if (matcherAuthor.find() || matcherQuotation.find()) {
                        searchCount += 1;
                    }
                }
            } else {
                for (String digest : favouriteExternalDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationExternalDAO.getQuotation(digest);

                    Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                    Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                    if (matcherAuthor.find() || matcherQuotation.find()) {
                        searchCount += 1;
                    }
                }
            }
        } else {
            for (QuotationEntity quotation : getAllQuotations(useInternalDatabase)) {
                Matcher matcherAuthor = pattern.matcher(quotation.author);
                Matcher matcherQuotation = pattern.matcher(quotation.quotation);

                if (matcherAuthor.find() || matcherQuotation.find()) {
                    searchCount += 1;
                }
            }
        }

        return searchCount;
    }

    @NonNull
    public Integer countSearchText(boolean useInternalDatabase, @NonNull final String text, boolean favouritesOnly) {
        int searchCount = 0;

        if (favouritesOnly) {
            if (useInternalDatabase) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);
                    String author = quotationEntity.author.toLowerCase();
                    String quotation = quotationEntity.quotation.toLowerCase();

                    if (author.indexOf(text.toLowerCase()) != -1
                            || quotation.indexOf(text.toLowerCase()) != -1) {
                        searchCount += 1;
                    }
                }
            } else {
                for (String digest : favouriteExternalDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationExternalDAO.getQuotation(digest);
                    String author = quotationEntity.author.toLowerCase();
                    String quotation = quotationEntity.quotation.toLowerCase();

                    if (author.indexOf(text.toLowerCase()) != -1
                            || quotation.indexOf(text.toLowerCase()) != -1) {
                        searchCount += 1;
                    }
                }
            }
        } else {
            for (QuotationEntity currentQuotation : getAllQuotations(useInternalDatabase)) {
                String author = currentQuotation.author.toLowerCase();
                String quotation = currentQuotation.quotation.toLowerCase();

                if (author.indexOf(text.toLowerCase()) != -1
                        || quotation.indexOf(text.toLowerCase()) != -1) {
                    searchCount += 1;
                }
            }
        }

        return searchCount;
    }

    @NonNull
    public List<QuotationEntity> getAllQuotations(boolean useInternalDatabase) {
        // ensure order is same as that shown in widget when using Next, sequential
        List<QuotationEntity> getAllQuotations = new ArrayList<>();

        if (useInternalDatabase) {
            QuotationEntity defaultQuotation = quotationDAO.getQuotation(DatabaseRepository.getDefaultQuotationDigest(useInternalDatabase));
            if (defaultQuotation != null) {
                getAllQuotations.add(defaultQuotation);
            }
            getAllQuotations.addAll(quotationDAO.getAllQuotations());
        } else {
            // Issue 469: Sort all the external
            getAllQuotations.addAll(quotationExternalDAO.getAllQuotationsIncludingDefault());
        }

        return getAllQuotations;
    }

    public QuotationEntity getQuotation(boolean useInternalDatabase, @NonNull final String digest) {
        if (useInternalDatabase) {
            return quotationDAO.getQuotation(digest);
        } else {
            return quotationExternalDAO.getQuotation(digest);
        }
    }

    public void markAsPrevious(
            boolean useInternalDatabase,
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String digest) {
        if (useInternalDatabase) {
            if (getQuotation(useInternalDatabase, digest) != null) {
                previousDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
            }
        } else {
            if (getQuotation(useInternalDatabase, digest) != null) {
                previousExternalDAO.markAsPrevious(new PreviousEntity(widgetId, contentSelection, digest));
            }
        }
    }

    public void markAsFavourite(boolean useInternalDatabase, @NonNull final String digest) {
        if (useInternalDatabase) {
            if (favouriteDAO.isFavourite(digest) == 0) {
                if (getQuotation(useInternalDatabase, digest) != null) {
                    favouriteDAO.markAsFavourite(new FavouriteEntity(digest));
                }
            }
        } else {
            if (getQuotation(useInternalDatabase, digest) != null) {
                if (favouriteExternalDAO.isFavourite(digest) == 0) {
                    favouriteExternalDAO.markAsFavourite(new FavouriteEntity(digest));
                }
            }
        }
    }

    public void markAsCurrent(
            boolean useInternalDatabase,
            final int widgetId,
            @NonNull final String digest) {
        Timber.d("digest=%s", digest);
        if (useInternalDatabase) {
            currentDAO.erase(widgetId);
            currentDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
        } else {
            currentExternalDAO.erase(widgetId);
            currentExternalDAO.markAsCurrent(new CurrentEntity(widgetId, digest));
        }
    }

    public QuotationEntity getCurrentQuotation(boolean useInternalDatabase, final int widgetId) {
        QuotationEntity quotationEntity;

        if (useInternalDatabase) {
            quotationEntity = getQuotation(useInternalDatabase, currentDAO.getCurrentDigest(widgetId));
        } else {
            quotationEntity = getQuotation(useInternalDatabase, currentExternalDAO.getCurrentDigest(widgetId));
        }

        return quotationEntity;
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
                nextQuotation = getQuotation(useInternalDatabase, nextDigests.get(0));
            } else {
                int indexInPrevious = previousDigests.indexOf(currentQuotation.digest);

                if (indexInPrevious != 0) {
                    // move through previous quotations
                    indexInPrevious -= 1;
                    nextQuotation = getQuotation(useInternalDatabase, previousDigests.get(indexInPrevious));
                } else {
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
                if (useInternalDatabase) {
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
                if (useInternalDatabase) {
                    authorDigests
                            = new LinkedHashSet<>(quotationDAO.getNextAuthorDigest(criteria));
                } else {
                    authorDigests
                            = new LinkedHashSet<>(quotationExternalDAO.getNextAuthorDigest(criteria));
                }
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
                final LinkedHashSet<String> allAvailableDigests;
                if (useInternalDatabase) {
                    allAvailableDigests = new LinkedHashSet<>(quotationDAO.getNextAllDigests());
                } else {
                    allAvailableDigests = new LinkedHashSet<>(quotationExternalDAO.getNextAllDigests());
                }

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

    public void eraseFavourite(boolean useInternalDatabase, final int widgetId, @NonNull final String digest) {
        Timber.d("digest=%s", digest);

        if (useInternalDatabase) {
            favouriteDAO.erase(digest);
            previousDAO.erase(widgetId, ContentSelection.FAVOURITES, digest);
        } else {
            favouriteExternalDAO.erase(digest);
            previousExternalDAO.erase(widgetId, ContentSelection.FAVOURITES, digest);
        }
    }

    public void erasePrevious(
            boolean useInternalDatabase,
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @NonNull List<String> previousDigests) {
        for (String digest : previousDigests) {
            if (useInternalDatabase) {
                previousDAO.erase(widgetId, contentSelection, digest);
            } else {
                previousExternalDAO.erase(widgetId, contentSelection, digest);
            }
        }
    }

    public void erase(boolean useInternalDatabase) {
        if (useInternalDatabase) {
            previousDAO.erase();
            currentDAO.erase();
            favouriteDAO.erase();
        } else {
            quotationExternalDAO.eraseQuotations();

            previousExternalDAO.erase();
            currentExternalDAO.erase();
            favouriteExternalDAO.erase();
        }
    }

    public void eraseForRestore() {
        previousDAO.erase();
        currentDAO.erase();
        favouriteDAO.erase();
        previousExternalDAO.erase();
        currentExternalDAO.erase();
        favouriteExternalDAO.erase();
    }

    public void erase(boolean useInternalDatabase, final int widgetId) {
        if (useInternalDatabase) {
            previousDAO.erase(widgetId);
            currentDAO.erase(widgetId);
        } else {
            previousExternalDAO.erase(widgetId);
            currentExternalDAO.erase(widgetId);
        }
    }

    public void erase(boolean useInternalDatabase, final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase) {
            previousDAO.erase(widgetId, contentSelection);
            currentDAO.erase(widgetId);
        } else {
            previousExternalDAO.erase(widgetId, contentSelection);
            currentExternalDAO.erase(widgetId);
        }
    }

    @NonNull
    public Boolean isFavourite(boolean useInternalDatabase, @NonNull final String digest) {
        if (useInternalDatabase) {
            return favouriteDAO.isFavourite(digest) > 0;
        } else {
            return favouriteExternalDAO.isFavourite(digest) > 0;
        }
    }

    @Transaction
    public void insertQuotationsExternal(
            @NonNull final LinkedHashSet<QuotationEntity> quotationEntityList) {
        quotationExternalDAO.insertQuotations(new ArrayList<>(quotationEntityList));
    }

    public void insertQuotationExternal(QuotationEntity quotationEntity) {
        try {
            quotationExternalDAO.insertQuotation(quotationEntity);
        } catch (SQLiteConstraintException e) {
            Timber.e(e.getMessage());
        }
    }

    public void updateQuotationUsingDigest(boolean useInternalDatabase, String digest, String author, String quotation) {
        if (useInternalDatabase) {
            quotationDAO.updateQuotationUsingDigest(
                    digest,
                    author,
                    quotation);
        } else {
            quotationExternalDAO.updateQuotationUsingDigest(
                    digest,
                    author,
                    quotation);
        }
    }

    public void updateQuotationUsingAuthorQuotation(boolean useInternalDatabase, String digest, String author, String quotation) {
        if (useInternalDatabase) {
            quotationDAO.updateQuotationUsingAuthorQuotation(
                    digest,
                    author,
                    quotation);
        } else {
            quotationExternalDAO.updateQuotationUsingAuthorQuotation(
                    digest,
                    author,
                    quotation);
        }
    }

    public void deleteQuotation(boolean useInternalDatabase, String digest) {
        if (useInternalDatabase) {
            quotationDAO.eraseQuotations(digest);
        } else {
            quotationExternalDAO.eraseQuotations(digest);
        }
    }

    public void deleteFavourite(boolean useInternalDatabase, String digest) {
        if (useInternalDatabase) {
            favouriteDAO.erase(digest);
        } else {
            favouriteExternalDAO.erase(digest);
        }
    }

    public void deletePrevious(boolean useInternalDatabase, String digest) {
        if (useInternalDatabase) {
            previousDAO.erase(digest);
        } else {
            previousExternalDAO.erase(digest);
        }
    }

    // # Batch restore methods

    public void insertFavourites(@NonNull final List<FavouriteEntity> entities, final boolean internal) {
        // deduplicate by digest to prevent duplicate rows
        final List<FavouriteEntity> deduped = new ArrayList<>();
        final HashSet<String> seenDigests = new HashSet<>();
        for (final FavouriteEntity entity : entities) {
            if (seenDigests.add(entity.digest)) {
                deduped.add(entity);
            }
        }
        if (internal) {
            favouriteDAO.insertFavourites(deduped);
        } else {
            favouriteExternalDAO.insertFavourites(deduped);
        }
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
        if (internal) {
            previousDAO.insertPrevious(deduped);
        } else {
            previousExternalDAO.insertPrevious(deduped);
        }
    }

    public void insertCurrent(@NonNull final List<CurrentEntity> entities, final boolean internal) {
        if (internal) {
            currentDAO.insertCurrents(entities);
        } else {
            currentExternalDAO.insertCurrents(entities);
        }
    }
}
