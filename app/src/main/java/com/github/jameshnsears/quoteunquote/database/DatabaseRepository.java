package com.github.jameshnsears.quoteunquote.database;

import android.annotation.SuppressLint;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import timber.log.Timber;

public class DatabaseRepository {
    @SuppressLint("StaticFieldLeak")
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
        abstractQuotationDatabase = AbstractQuotationDatabase.getDatabase(context);
        quotationDAO = abstractQuotationDatabase.quotationDAO();

        abstractQuotationExternalDatabase = AbstractQuotationExternalDatabase.getDatabase(context);
        quotationExternalDAO = abstractQuotationExternalDatabase.quotationExternalDAO();

        abstractHistoryDatabase = AbstractHistoryDatabase.getDatabase(context);
        previousDAO = abstractHistoryDatabase.previousDAO();
        favouriteDAO = abstractHistoryDatabase.favouritesDAO();
        currentDAO = abstractHistoryDatabase.currentDAO();

        abstractHistoryExternalDatabase = AbstractHistoryExternalDatabase.getDatabase(context);
        previousExternalDAO = abstractHistoryExternalDatabase.previousExternalDAO();
        favouriteExternalDAO = abstractHistoryExternalDatabase.favouritesExternalDAO();
        currentExternalDAO = abstractHistoryExternalDatabase.currentExternalDAO();

        this.context = context;
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
            return "1624c314";
        } else {
            return "00000000";
        }
    }

    public static boolean useInternalDatabase() {
        return useInternalDatabase;
    }

    @NonNull
    public Single<Integer> countAll() {
        if (useInternalDatabase()) {
            return quotationDAO.countAll();
        }

        return countAllExternal();
    }

    @NonNull
    public Single<Integer> countAllMinusExclusions(final String exclusions) {
        if ("".equals(exclusions)) {
            return countAll();
        }

        return Single.just(countAll().blockingGet() - getAllExcludedDigests(exclusions).size());
    }

    @NonNull
    public HashSet<String> getAllExcludedDigests(String exclusions) {
        HashSet<String> digestsExcluded = new HashSet<>();

        for (String exclusion : exclusions.split(";")) {
            // 4 is the smallest author entry
            if (!"".equals(exclusion) && exclusion.length() >= 4) {
                if (useInternalDatabase()) {
                    digestsExcluded.addAll(quotationDAO.getExclusionDigests(exclusion));
                } else {
                    digestsExcluded.addAll(quotationExternalDAO.getExclusionDigests(exclusion));
                }
            }
        }

        // we keep the default quotation
        digestsExcluded.remove(getDefaultQuotationDigest());
        return digestsExcluded;
    }

    @NonNull
    public Single<Integer> countAllExternal() {
        return quotationExternalDAO.countAll();
    }

    public int countPreviousCriteria(final int widgetId, @NonNull final ContentSelection contentSelection) {
        if (useInternalDatabase()) {
            return previousDAO.countPrevious(widgetId, contentSelection);
        }

        return previousExternalDAO.countPrevious(widgetId, contentSelection);
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

    public int countPreviousCriteria(final int widgetId) {
        if (useInternalDatabase()) {
            return previousDAO.countPrevious(widgetId);
        }

        return previousExternalDAO.countPrevious(widgetId);
    }

    public int findPositionInPrevious(
            final int widgetId,
            @NonNull final QuotationsPreferences quotationsPreferences) {

        List<String> allPrevious = getPreviousDigests(
                widgetId,
                quotationsPreferences.getContentSelection(),
                quotationsPreferences.getContentSelectionAllExclusion());

        Collections.reverse(allPrevious);

        int position = 0;
        if (!allPrevious.isEmpty()) {
            String currentDigest = getCurrentQuotation(widgetId).digest;
            position = allPrevious.indexOf(currentDigest) + 1;
        }

        return position;
    }

    public ArrayList<QuotationEntity> getQuotationsForAuthor(@NonNull final String author) {
        ArrayList<String> digestsForAuthor;
        ArrayList<QuotationEntity> quotationEntityList = new ArrayList<>();

        if (useInternalDatabase()) {
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
            @NonNull final QuotationsPreferences quotationsPreferences) {
        int countTotalNext;

        switch (quotationsPreferences.getContentSelection()) {
            case FAVOURITES:
                countTotalNext = countFavourites().blockingGet();
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
                if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                    countTotalNext = getSearchQuotationsRegEx(
                            quotationsPreferences.getContentSelectionSearch(),
                            quotationsPreferences.getContentSelectionSearchFavouritesOnly()).size();
                } else {
                    countTotalNext = getSearchQuotations(
                            quotationsPreferences.getContentSelectionSearch(),
                            quotationsPreferences.getContentSelectionSearchFavouritesOnly()).size();
                }
                break;

            default:
                // ALL:
                countTotalNext
                        = countAllMinusExclusions(
                        quotationsPreferences.getContentSelectionAllExclusion())
                        .blockingGet();
                break;
        }
        return countTotalNext;
    }

    public int countPreviousCriteria(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final QuotationsPreferences quotationsPreferences
    ) {
        HashSet<String> previousDigests;
        HashSet<String> availableDigests;

        if (contentSelection == ContentSelection.AUTHOR) {
            previousDigests = new HashSet<>(getPreviousDigests(widgetId, ContentSelection.AUTHOR, ""));
            if (useInternalDatabase()) {
                availableDigests = new HashSet<>(quotationDAO.getDigestsForAuthor(
                        quotationsPreferences.getContentSelectionAuthor()
                ));
            } else {
                availableDigests = new HashSet<>(quotationExternalDAO.getDigestsForAuthor(
                        quotationsPreferences.getContentSelectionAuthor()
                ));
            }
        } else {
            previousDigests = new HashSet<>(getPreviousDigests(widgetId, ContentSelection.SEARCH, ""));

            List<QuotationEntity> searchQuotations = getSearchQuotations(
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
    public Single<Integer> countFavourites() {
        if (useInternalDatabase()) {
            return favouriteDAO.countFavourites();
        } else {
            return favouriteExternalDAO.countFavourites();
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
    public List<String> getPreviousDigests(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String criteria) {

        List<String> previousDigests;

        if (useInternalDatabase()) {
            previousDigests = previousDAO.getPreviousDigests(widgetId, contentSelection);
        } else {
            previousDigests = previousExternalDAO.getPreviousDigests(widgetId, contentSelection);
        }

        if (contentSelection.equals(ContentSelection.ALL)) {
            previousDigests.removeAll(getAllExcludedDigests(criteria));
        }

        return previousDigests;
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
                    widgetId,
                    getLastPreviousDigest(widgetId, ContentSelection.ALL));
        }
    }

    @NonNull
    public List<String> getFavouritesDigests() {
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
    public Single<List<Integer>> getAuthorsQuotationCount() {
        if (useInternalDatabase()) {
            return quotationDAO.getAuthorsQuotationCount();
        } else {
            return quotationExternalDAO.getAuthorsQuotationCount();
        }
    }

    @NonNull
    public Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts(int authorCount) {
        if (useInternalDatabase()) {
            return quotationDAO.getAuthorsAndQuotationCounts(authorCount);
        } else {
            return quotationExternalDAO.getAuthorsAndQuotationCounts(authorCount);
        }
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotationsRegEx(@NonNull final String regEx, boolean favouritesOnly) {
        List<QuotationEntity> searchQuotations = new ArrayList<>();

        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);

        if (favouritesOnly) {
            if (useInternalDatabase()) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);
                    if (isFavourite(quotationEntity.digest)) {
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
                    if (isFavourite(quotationEntity.digest)) {
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
            if (useInternalDatabase()) {
                for (QuotationEntity quotationEntity : quotationDAO.getAllQuotations()) {
                    Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                    Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                    if (matcherAuthor.find() || matcherQuotation.find()) {
                        searchQuotations.add(quotationEntity);
                    }
                }
            } else {
                for (QuotationEntity quotationEntity : quotationExternalDAO.getAllQuotations()) {
                    Matcher matcherAuthor = pattern.matcher(quotationEntity.author);
                    Matcher matcherQuotation = pattern.matcher(quotationEntity.quotation);

                    if (matcherAuthor.find() || matcherQuotation.find()) {
                        searchQuotations.add(quotationEntity);
                    }
                }
            }
        }

        return searchQuotations;
    }

    @NonNull
    public List<QuotationEntity> getSearchQuotations(@NonNull final String text, boolean favouritesOnly) {
        List<QuotationEntity> searchQuotations = new ArrayList<>();

        if (favouritesOnly) {
            if (useInternalDatabase()) {
                for (String digest : favouriteDAO.getFavouriteDigests()) {
                    QuotationEntity quotationEntity = quotationDAO.getQuotation(digest);
                    if (isFavourite(quotationEntity.digest)) {
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
                    if (isFavourite(quotationEntity.digest)) {
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
            List<QuotationEntity> allQuotations;

            if (useInternalDatabase()) {
                allQuotations = quotationDAO.getAllQuotations();
            } else {
                allQuotations = quotationExternalDAO.getAllQuotations();
            }

            for (QuotationEntity currentQuotation : allQuotations) {
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
    public Integer countSearchTextRegEx(@NonNull final String regEx, boolean favouritesOnly) {
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        int searchCount = 0;

        if (favouritesOnly) {
            if (useInternalDatabase()) {
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
            List<QuotationEntity> allQuotations;

            if (useInternalDatabase()) {
                allQuotations = quotationDAO.getAllQuotations();
            } else {
                allQuotations = quotationExternalDAO.getAllQuotations();
            }

            for (QuotationEntity quotation : allQuotations) {
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
    public Integer countSearchText(@NonNull final String text, boolean favouritesOnly) {
        int searchCount = 0;

        if (favouritesOnly) {
            if (useInternalDatabase()) {
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
            List<QuotationEntity> allQuotations;

            if (useInternalDatabase()) {
                allQuotations = quotationDAO.getAllQuotations();
            } else {
                allQuotations = quotationExternalDAO.getAllQuotations();
            }

            for (QuotationEntity currentQuotation : allQuotations) {
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
    public List<QuotationEntity> getAllQuotations() {
        if (useInternalDatabase()) {
            return quotationDAO.getAllQuotations();
        } else {
            return quotationExternalDAO.getAllQuotations();
        }
    }

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

    public QuotationEntity getCurrentQuotation(final int widgetId) {
        QuotationEntity quotationEntity;

        if (useInternalDatabase()) {
            quotationEntity = getQuotation(currentDAO.getCurrentDigest(widgetId));
        } else {
            quotationEntity = getQuotation(currentExternalDAO.getCurrentDigest(widgetId));
        }

        return quotationEntity;
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
    public QuotationEntity getNextQuotation(
            final int widgetId,
            @NonNull final ContentSelection contentSelection,
            @NonNull final String criteria,
            final boolean randomNext,
            QuotationsPreferences quotationsPreferences) {
        Timber.d("contentType=%d; criteria=%s; randomNext=%b",
                contentSelection.getContentSelection(), criteria, randomNext);

        return getNextQuotation(
                widgetId,
                randomNext,
                getNextDigests(widgetId, contentSelection, criteria, quotationsPreferences),
                getPreviousDigests(widgetId, contentSelection, criteria),
                contentSelection
        );
    }

    @NonNull
    private QuotationEntity getNextQuotation(
            final int widgetId,
            final boolean randomNext,
            @Nullable final List<String> nextDigests,
            @Nullable final List<String> previousDigests,
            @NonNull final ContentSelection contentSelection) {

        QuotationEntity currentQuotation = getCurrentQuotation(widgetId);
        QuotationEntity nextQuotation;

        if (!randomNext) {
            if (previousDigests.isEmpty()) {
                nextQuotation = getQuotation(nextDigests.get(0));
            } else {
                int indexInPrevious = previousDigests.indexOf(currentQuotation.digest);

                if (indexInPrevious != 0) {
                    // move through previous quotations
                    indexInPrevious -= 1;
                    nextQuotation = getQuotation(previousDigests.get(indexInPrevious));
                } else {
                    if (!nextDigests.isEmpty()) {
                        // use a new quotation
                        nextQuotation = getQuotation(nextDigests.get(0));
                    } else {
                        // we've run out of new quotations
                        nextQuotation = currentQuotation;
                        markAsCurrent(widgetId, getLastPreviousDigest(widgetId, contentSelection));
                    }
                }
            }
        } else {
            if (!nextDigests.isEmpty()) {
                nextQuotation = getQuotation(nextDigests.get(getRandomIndex(nextDigests)));
            } else {
                // we've run out of new quotations
                nextQuotation = currentQuotation;
                markAsCurrent(widgetId, getLastPreviousDigest(widgetId, contentSelection));
            }
        }

        return nextQuotation;
    }

    public synchronized List<String> getNextDigests(
            int widgetId,
            @NonNull ContentSelection contentSelection,
            @NonNull String criteria,
            QuotationsPreferences quotationsPreferences) {
        // insertion order required
        final LinkedHashSet<String> nextQuotationDigests;

        // no order needed
        final HashSet<String> previousDigests
                = new HashSet<>(getPreviousDigests(widgetId, contentSelection, criteria));

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
                    nextQuotationDigests = getSearchDigestsRegEx(quotationsPreferences, previousDigests);
                } else {
                    nextQuotationDigests = getSearchDigests(quotationsPreferences, previousDigests);
                }
                break;

            default:
                // ALL:
                final LinkedHashSet<String> allAvailableDigests;
                if (useInternalDatabase()) {
                    allAvailableDigests = new LinkedHashSet<>(quotationDAO.getNextAllDigests());
                } else {
                    allAvailableDigests = new LinkedHashSet<>(quotationExternalDAO.getNextAllDigests());
                }

                allAvailableDigests.removeAll(getAllExcludedDigests(criteria));

                allAvailableDigests.removeAll(previousDigests);
                nextQuotationDigests = allAvailableDigests;
                break;
        }

        return new ArrayList<>(nextQuotationDigests);
    }

    @NonNull
    private LinkedHashSet<String> getSearchDigestsRegEx(
            QuotationsPreferences quotationsPreferences, HashSet<String> previousDigests) {
        final LinkedHashSet<String> searchDigests = new LinkedHashSet<>();

        List<QuotationEntity> searchQuotations = getSearchQuotationsRegEx(
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
            QuotationsPreferences quotationsPreferences, HashSet<String> previousDigests) {
        final LinkedHashSet<String> searchDigests = new LinkedHashSet<>();

        List<QuotationEntity> searchQuotations = getSearchQuotations(
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
            insertQuotationExternal(quotationEntity);
        }
    }

    public void insertQuotationExternal(QuotationEntity quotationEntity) {
        quotationExternalDAO.insertQuotation(quotationEntity);
    }
}
