package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.BrowseFavouritesDialogFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.BrowseSearchDialogFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.BrowseSourceDialogFragment;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabFilterBinding;
import com.github.jameshnsears.quoteunquote.db.q.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Keep
public class QuotationsFilterFragment extends FragmentCommon {
    public static final String TEXT_CSV = "text/csv";
    @Nullable
    public static List<QuotationEntity> activityExportQuotationEntityList;
    @NonNull
    public CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    public FragmentQuotationsTabFilterBinding fragmentQuotationsTabFilterBinding;
    @Nullable
    public QuotationsPreferences quotationsPreferences;
    @Nullable
    private DisposableObserver<Integer> disposableObserverAllExclusion;
    @Nullable
    private DisposableObserver<Integer> disposableObserverSearch;
    @NonNull
    private ActivityResultLauncher<Intent> activityExportFavourites = activityExport();
    @NonNull
    private ActivityResultLauncher<Intent> activityExportSearch = activityExport();
    @NonNull
    private ActivityResultLauncher<Intent> activityExportSource = activityExport();

    private boolean isDatabaseInternal;
    private boolean resetSpinners = false;

    private String lastSearchText = "";
    private boolean lastSearchFavouritesOnly = false;
    private boolean lastSearchRegEx = false;

    public QuotationsFilterFragment() {
    }

    public QuotationsFilterFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsFilterFragment newInstance(final int widgetId) {
        final QuotationsFilterFragment fragment = new QuotationsFilterFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    public static void ensureFragmentContentSearchConsistency(
        final int widgetId,
        @NonNull Context context
    ) {
        final QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

        if (quotationsPreferences.getContentSelection() == ContentSelection.SEARCH
            && quotationsPreferences.getContentSelectionSearchCount() == 0) {
            quotationsPreferences.setContentSelection(ContentSelection.ALL);
            Toast.makeText(
                context,
                context.getString(R.string.fragment_quotations_selection_search_no_results),
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        rememberScreen(Screen.QuotationsFilter, getContext());
        initUI();
    }

    @Override
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());
        isDatabaseInternal = new QuotationsPreferences(widgetId, getContext()).getDatabaseInternal();

        RxJavaPlugins.setErrorHandler(e ->
            Timber.e(e.getMessage())
        );
    }

    @Override
    @NonNull
    public View onCreateView(
        @NonNull final LayoutInflater inflater,
        @NonNull final ViewGroup container,
        @NonNull final Bundle savedInstanceState) {
        this.quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());

        this.fragmentQuotationsTabFilterBinding = FragmentQuotationsTabFilterBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsTabFilterBinding.getRoot();
    }

    @Override
    public void onViewCreated(
        @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        initUI();

        createListenerCardAllRadio();

        createListenerCardSourceRadio();
        createListenerCardSourceCount();
        createListenerCardSourceSelection();
        createListenerCardSourceBrowse();
        createListenerCardSourceExport();

        createListenerCardFavouriteRadio();
        createListenerCardFavouritesBrowse();
        createListenerCardFavouritesExport();

        createListenerCardSearchRadio();
        createListenerCardSearchFavourites();
        createListenerCardSearchRegEx();
        createListenerCardSearchBrowse();
        createListenerCardSearchExport();

        setCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.fragmentQuotationsTabFilterBinding = null;

        this.shutdown();
    }

    public void shutdown() {
        this.disposables.clear();
        this.disposables.dispose();

        if (this.disposableObserverAllExclusion != null) {
            this.disposableObserverAllExclusion.dispose();
        }

        if (this.disposableObserverSearch != null) {
            this.disposableObserverSearch.dispose();
        }
    }

    public void initUI() {
        if (this.disposables.isDisposed()) {
            this.disposables = new CompositeDisposable();
        }
        this.disposables.clear();

        if (this.quotationsPreferences != null && this.quoteUnquoteModel != null) {
            boolean currentDatabaseInternal = this.quotationsPreferences.getDatabaseInternal();
            if (this.isDatabaseInternal != currentDatabaseInternal) {
                this.isDatabaseInternal = currentDatabaseInternal;
                this.resetSpinners = true;
            }
            this.quoteUnquoteModel.setUseInternalDatabase(this.isDatabaseInternal);
        }

        if (this.quotationsPreferences != null) {
            lastSearchText = quotationsPreferences.getContentSelectionSearch();
            lastSearchFavouritesOnly = quotationsPreferences.getContentSelectionSearchFavouritesOnly();
            lastSearchRegEx = quotationsPreferences.getContentSelectionSearchRegEx();
        }

        initCardCounts();

        setDisposableCardAllCount();
        setDisposableCardSourceCount();
        setDisposableCardSource();
        setDisposableCardFavouriteCount();
        setDisposableCardSearchCount();

        alignCards();
    }

    private void initCardCounts() {
        if (fragmentQuotationsTabFilterBinding == null) {
            fragmentQuotationsTabFilterBinding
                = FragmentQuotationsTabFilterBinding.inflate(this.getLayoutInflater());
        }

        fragmentQuotationsTabFilterBinding.radioButtonAll.setText(
            getResources().getString(R.string.fragment_quotations_selection_all,
                0));

        fragmentQuotationsTabFilterBinding.radioButtonAuthorIndividual.setText(
            getResources().getString(R.string.fragment_quotations_selection_author,
                0));

        fragmentQuotationsTabFilterBinding.radioButtonFavourites.setText(
            getResources().getString(R.string.fragment_quotations_selection_favourites,
                0));

        fragmentQuotationsTabFilterBinding.radioButtonSearch.setText(
            getResources().getString(R.string.fragment_quotations_selection_search,
                0));
    }

    public void setCard() {
        setCardAll(false);
        setCardSourceIndividual(false);
        setCardFavourite(false);
        setCardSearch(false);

        switch (quotationsPreferences.getContentSelection()) {
            case AUTHOR:
                setCardSourceIndividual(true);
                break;
            case FAVOURITES:
                setCardFavourite(true);
                break;
            case SEARCH:
                setCardSearch(true);
                break;
            default:
                setCardAll(true);
                break;
        }
    }

    private void setCardAll(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonAll.setChecked(enabled);

        final String editTextKeywords = quotationsPreferences.getContentSelectionAllExclusion();

        fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setText(editTextKeywords);

        if (enabled) {
            if (quotationsPreferences.getContentSelection() == ContentSelection.ALL) {
                this.fragmentQuotationsTabFilterBinding.editTextResultsExclusion.requestFocus();
                int selectionPosition
                    = this.fragmentQuotationsTabFilterBinding.editTextResultsExclusion.getText().length();
                this.fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setSelection(selectionPosition);
            }
        }
        fragmentQuotationsTabFilterBinding.editTextResultsExclusionLayout.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.textViewExclusionInfo.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.textViewExclusion1.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.textViewExclusion2.setEnabled(enabled);
    }

    private void setCardSourceIndividual(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonAuthorIndividual.setChecked(enabled);

        fragmentQuotationsTabFilterBinding.textViewQuotationCount.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setEnabled(enabled);

        setCardSourceButtonBrowse(enabled);
        setCardSourceButtonExport(enabled);
    }

    private void setCardSourceButtonBrowse(boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.buttonSourceBrowse.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonSourceBrowse, enabled);
    }

    private void setCardSourceButtonExport(boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.buttonSourceExport.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonSourceExport, enabled);
    }

    void setCardSourceCount(@NonNull List<Integer> authorCountList) {
        if (quotationsPreferences == null || fragmentQuotationsTabFilterBinding == null || authorCountList.isEmpty()) {
            return;
        }

        final ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
            getContext(),
            R.layout.spinner_item,
            authorCountList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setAdapter(adapter);

        int selection;
        if (resetSpinners) {
            selection = 0;
            quotationsPreferences.setContentSelectionAuthorCount(authorCountList.get(0));
        } else {
            selection = authorCountList.indexOf(quotationsPreferences.getContentSelectionAuthorCount());
        }
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setSelection(selection);
    }

    void setCardSourceSelection(@NonNull List<AuthorPOJO> authorPOJOList) {
        if (quotationsPreferences == null || fragmentQuotationsTabFilterBinding == null || quoteUnquoteModel == null) {
            return;
        }

        final List<String> authors
            = quoteUnquoteModel.authorsSorted(authorPOJOList);

        if (authors.isEmpty()) {
            return;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            R.layout.spinner_item,
            authors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setAdapter(adapter);

        if (resetSpinners) {
            fragmentQuotationsTabFilterBinding.spinnerAuthors.setSelection(0);
            quotationsPreferences.setContentSelectionAuthor(authors.get(0));
            setCardSourceSelectionName(authors.get(0));
            resetSpinners = false;
        } else {
            // first time usage
            if ("".equals(quotationsPreferences.getContentSelectionAuthor())) {
                quotationsPreferences.setContentSelectionAuthor(authors.get(0));
            }

            if (authors.contains(quotationsPreferences.getContentSelectionAuthor())) {
                // author is within quotation count range
                setCardSourceSelectionName(quotationsPreferences.getContentSelectionAuthor());
            } else {
                quotationsPreferences.setContentSelectionAuthor(authors.get(0));
                setCardSourceSelectionName(authors.get(0));
            }
        }
    }

    private void setCardSourceSelectionName(final String authorPreference) {
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setSelection(
            quoteUnquoteModel.authorsIndex(authorPreference));

        fragmentQuotationsTabFilterBinding.radioButtonAuthorIndividual.setText(
            getResources().getString(
                R.string.fragment_quotations_selection_author,
                quoteUnquoteModel.countAuthorQuotations(authorPreference)));
    }

    private void setCardFavourite(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonFavourites.setChecked(enabled);

        setCardFavouriteButtonBrowse(enabled);
        setCardFavouriteButtonExport(enabled);
    }

    private void setCardFavouriteButtonBrowse(boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse, enabled);
    }

    private void setCardFavouriteButtonExport(boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.buttonFavouritesExport.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonFavouritesExport, enabled);
    }

    private void setCardSearch(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonSearch.setChecked(enabled);

        //

        if (enabled && quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(true);

            if (quotationsPreferences.getContentSelectionSearchFavouritesOnly()) {
                this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(true);
            }
        }
        if (!enabled && quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(false);
        }
        if (quoteUnquoteModel.countFavouritesWithoutRx() == 0) {
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(false);
            quotationsPreferences.setContentSelectionSearchFavouritesOnly(false);
        }

        //

        this.fragmentQuotationsTabFilterBinding.switchRegEx.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.switchRegEx
            .setChecked(quotationsPreferences.getContentSelectionSearchRegEx());

        //

        String keywords = quotationsPreferences.getContentSelectionSearch();
        fragmentQuotationsTabFilterBinding.editTextSearchText.setText(keywords);

        this.fragmentQuotationsTabFilterBinding.editTextSearchTextLayout.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.editTextSearchText.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.textViewSearchMinimumInfo.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.textViewSearchMinimum1.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.textViewSearchMinimum2.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.textViewSearchMinimum3.setEnabled(enabled);
        if (enabled) {
            setCardSearchTextFocus();
        }

        //

        if (enabled && quotationsPreferences.getContentSelectionSearchCount() > 0) {
            setCardSearchButtonBrowse(true);
            setCardSearchButtonExport(true);
        }
        if (!enabled || quotationsPreferences.getContentSelectionSearchCount() == 0) {
            setCardSearchButtonBrowse(false);
            setCardSearchButtonExport(false);
        }
    }

    private void setCardSearchTextFocus() {
        int selectionPosition
            = this.fragmentQuotationsTabFilterBinding.editTextSearchText.getText().length();
        this.fragmentQuotationsTabFilterBinding.editTextSearchText.setSelection(selectionPosition);
        this.fragmentQuotationsTabFilterBinding.editTextSearchText.requestFocus();
    }

    void setCardSearchButtonBrowse(boolean enabled) {
        fragmentQuotationsTabFilterBinding.buttonSearchBrowse.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonSearchBrowse, enabled);
    }

    void setCardSearchButtonExport(boolean enabled) {
        fragmentQuotationsTabFilterBinding.buttonSearchExport.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonSearchExport, enabled);
    }

    private void alignCards() {
        int countFavourites = quoteUnquoteModel.countFavouritesWithoutRx();
        alignCardSource();
        alignCardFavourites(countFavourites);
        alignCardSearch(countFavourites);
    }

    private void alignCardSource() {
        if (quotationsPreferences.getContentSelection() == ContentSelection.AUTHOR) {
            setCardSourceButtonBrowse(true);
            setCardSourceButtonExport(true);
        }
    }

    private void alignCardFavourites(int countFavourites) {
        fragmentQuotationsTabFilterBinding.radioButtonFavourites.setText(
            getResources().getString(R.string.fragment_quotations_selection_favourites, countFavourites));

        if (countFavourites == 0) {
            setCardFavouriteButtonBrowse(false);
            setCardFavouriteButtonExport(false);

            if (quotationsPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
                setCardAll(true);
            }
        } else {
            if (quotationsPreferences.getContentSelection() == ContentSelection.FAVOURITES) {
                setCardFavouriteButtonBrowse(true);
                setCardFavouriteButtonExport(true);
            } else {
                setCardFavouriteButtonBrowse(false);
                setCardFavouriteButtonExport(false);
            }
        }
    }

    private void alignCardSearch(int countFavourites) {
        if (countFavourites == 0) {
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(false);
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(false);
            quotationsPreferences.setContentSelectionSearchFavouritesOnly(false);

            setCardSearchButtonBrowse(false);
            setCardSearchButtonExport(false);
        } else {
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(
                quotationsPreferences.getContentSelectionSearchFavouritesOnly()
            );

            if (quotationsPreferences.getContentSelection() == ContentSelection.SEARCH) {
                this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(true);
            }
        }
    }

    private void setDisposableCardAllCount() {
        if (fragmentQuotationsTabFilterBinding == null || quotationsPreferences == null || quoteUnquoteModel == null) {
            // Fragment view not ready or model/preferences missing - skip setup
            return;
        }

        fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setText(
            quotationsPreferences.getContentSelectionAllExclusion()
        );

        disposableObserverAllExclusion = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                Timber.d("value=%d", value);
                if (value != 0) {
                    fragmentQuotationsTabFilterBinding.radioButtonAll.setText(
                        getResources().getString(R.string.fragment_quotations_selection_all, value));
                }
            }

            @Override
            public void onError(@NonNull final Throwable throwable) {
                if (throwable != null && throwable.getMessage() != null) {
                    Timber.d("onError=%s", throwable.getMessage());
                } else {
                    Timber.d("onError: null throwable");
                }
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        };

        RxTextView.textChanges(fragmentQuotationsTabFilterBinding.editTextResultsExclusion)
            .debounce(250, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .map(charSequence -> {
                String exclusions = charSequence.toString();

                quotationsPreferences.setContentSelectionAllExclusion(exclusions);

                if (quoteUnquoteModel.getCurrentQuotation(widgetId) != null) {
                    quoteUnquoteModel.markAsCurrentLastPrevious(widgetId);
                }

                return quoteUnquoteModel.countAllMinusExclusions(widgetId).blockingGet();
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(disposableObserverAllExclusion);
    }

    void setDisposableCardSource() {
        if (quoteUnquoteModel == null || quotationsPreferences == null || fragmentQuotationsTabFilterBinding == null) {
            return;
        }

        disposables.add(quoteUnquoteModel.authors(
                quotationsPreferences.getContentSelectionAuthorCount().intValue()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                new DisposableSingleObserver<List<AuthorPOJO>>() {
                    @Override
                    public void onSuccess(@NonNull final List<AuthorPOJO> authorPOJOList) {
                        setCardSourceSelection(authorPOJOList);
                    }

                    @Override
                    public void onError(@NonNull final Throwable throwable) {
                        if (throwable != null && throwable.getMessage() != null) {
                            Timber.d("onError=%s", throwable.getMessage());
                        } else {
                            Timber.d("onError: null throwable");
                        }
                    }
                }));
    }

    private void setDisposableCardSourceCount() {
        if (quoteUnquoteModel == null || fragmentQuotationsTabFilterBinding == null) {
            return;
        }

        disposables.add(quoteUnquoteModel.authorsQuotationCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                new DisposableSingleObserver<List<Integer>>() {
                    @Override
                    public void onSuccess(@NonNull final List<Integer> authorCountList) {
                        setCardSourceCount(authorCountList);
                    }

                    @Override
                    public void onError(@NonNull final Throwable throwable) {
                        if (throwable != null && throwable.getMessage() != null) {
                            Timber.d("onError=%s", throwable.getMessage());
                        } else {
                            Timber.d("onError: null throwable");
                        }
                    }
                }));
    }

    private void setDisposableCardFavouriteCount() {
        if (quoteUnquoteModel == null || fragmentQuotationsTabFilterBinding == null) {
            return;
        }

        disposables.add(quoteUnquoteModel.countFavourites()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(@NonNull final Integer value) {
                        fragmentQuotationsTabFilterBinding.radioButtonFavourites.setEnabled(true);
                        if (value == 0) {
                            fragmentQuotationsTabFilterBinding.radioButtonFavourites.setEnabled(false);

                            // in case another widget instance changes favourites
                            if (QuotationsFilterFragment.this.quotationsPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)) {
                                QuotationsFilterFragment.this.quotationsPreferences.setContentSelection(ContentSelection.ALL);
                            }
                        }

                        fragmentQuotationsTabFilterBinding.radioButtonFavourites.setText(
                            getResources().getString(R.string.fragment_quotations_selection_favourites, value));
                    }

                    @Override
                    public void onError(@NonNull final Throwable throwable) {
                        Timber.d("onError=%s", throwable.getMessage());
                    }
                }));
    }

    private void setDisposableCardSearchCount() {
        if (fragmentQuotationsTabFilterBinding == null || quotationsPreferences == null || quoteUnquoteModel == null) {
            return;
        }

        fragmentQuotationsTabFilterBinding.editTextSearchText.setText(
            quotationsPreferences.getContentSelectionSearch()
        );

        this.disposableObserverSearch = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                fragmentQuotationsTabFilterBinding.radioButtonSearch.setText(
                    getResources().getString(R.string.fragment_quotations_selection_search, value));
                quotationsPreferences.setContentSelectionSearchCount(value);

                if (value > 0) {
                    if (quotationsPreferences.getContentSelection() == ContentSelection.SEARCH) {
                        setCardSearchButtonBrowse(true);
                        setCardSearchButtonExport(true);
                    } else {
                        setCardSearchButtonBrowse(false);
                        setCardSearchButtonExport(false);
                    }
                } else {
                    setCardSearchButtonBrowse(false);
                    setCardSearchButtonExport(false);
                }
            }

            @Override
            public void onError(@NonNull final Throwable throwable) {
                if (throwable != null && throwable.getMessage() != null) {
                    Timber.d("onError=%s", throwable.getMessage());
                } else {
                    Timber.d("onError: null throwable");
                }
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        };

        RxTextView.textChanges(fragmentQuotationsTabFilterBinding.editTextSearchText)
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .map(charSequence -> {
                final String keywords = charSequence.toString();

                final boolean favouritesOnly = quotationsPreferences.getContentSelectionSearchFavouritesOnly();
                final boolean isRegEx = quotationsPreferences.getContentSelectionSearchRegEx();

                int count = 0;
                if (!keywords.equals("") && keywords.length() >= 4) {
                    if (isRegEx) {
                        try {
                            count = quoteUnquoteModel.countQuotationWithSearchTextRegEx(
                                this.widgetId, keywords, favouritesOnly);
                        } catch (PatternSyntaxException e) {
                            count = 0;
                        }
                    } else {
                        count = quoteUnquoteModel.countQuotationWithSearchText(
                            this.widgetId, keywords, favouritesOnly);
                    }
                }

                boolean parametersChanged = !keywords.equals(lastSearchText)
                    || favouritesOnly != lastSearchFavouritesOnly
                    || isRegEx != lastSearchRegEx;

                if (parametersChanged) {
                    if (count > 0) {
                        quoteUnquoteModel.resetPrevious(this.widgetId, ContentSelection.SEARCH);

                        lastSearchText = keywords;
                        lastSearchFavouritesOnly = favouritesOnly;
                        lastSearchRegEx = isRegEx;

                        quotationsPreferences.setContentSelectionSearch(keywords);
                        quoteUnquoteModel.markAsCurrentDefault(this.widgetId);
                    } else {
                        quotationsPreferences.setContentSelectionSearch(keywords);
                        lastSearchText = keywords;
                        lastSearchFavouritesOnly = favouritesOnly;
                        lastSearchRegEx = isRegEx;
                    }
                }

                return count;
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(disposableObserverSearch);
    }

    private void createListenerCardAllRadio() {
        final RadioButton radioButtonAll = this.fragmentQuotationsTabFilterBinding.radioButtonAll;
        radioButtonAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                quotationsPreferences.setContentSelection(ContentSelection.ALL);
                setCard();
            }
        });
    }

    private void createListenerCardSourceRadio() {
        final RadioButton radioButtonAuthorIndividual = this.fragmentQuotationsTabFilterBinding.radioButtonAuthorIndividual;
        radioButtonAuthorIndividual.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                quotationsPreferences.setContentSelection(ContentSelection.AUTHOR);
                setCard();
            }
        });
    }

    private void createListenerCardSourceCount() {
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String authorCount = fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.getSelectedItem().toString();

                quotationsPreferences.setContentSelectionAuthorCount(Integer.valueOf(authorCount));

                setDisposableCardSource();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerCardSourceSelection() {
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String author = fragmentQuotationsTabFilterBinding.spinnerAuthors.getSelectedItem().toString();
                fragmentQuotationsTabFilterBinding.radioButtonAuthorIndividual.setText(
                    getResources().getString(R.string.fragment_quotations_selection_author,
                        quoteUnquoteModel.countAuthorQuotations(author)
                    ));

                if (!quotationsPreferences.getContentSelectionAuthor().equals(author)) {
                    quotationsPreferences.setContentSelectionAuthor(author);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createListenerCardSourceBrowse() {
        fragmentQuotationsTabFilterBinding.buttonSourceBrowse.setOnClickListener(v -> {
            setCardSourceButtonBrowse(false);
            setCardSourceButtonExport(false);

            BrowseSourceDialogFragment browseSourceDialogFragment = new BrowseSourceDialogFragment(
                widgetId,
                quoteUnquoteModel,
                quotationsPreferences.getContentSelectionAuthor());
            browseSourceDialogFragment.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerCardSourceExport() {
        fragmentQuotationsTabFilterBinding.buttonSourceExport.setOnClickListener(v -> {
            ConfigureActivity.launcherInvoked = true;

            final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(TEXT_CSV);

            activityExportQuotationEntityList = quoteUnquoteModel.getQuotationsForAuthor(
                quotationsPreferences.getContentSelectionAuthor());

            intent.putExtra(
                Intent.EXTRA_TITLE,
                quotationsPreferences.getContentSelectionAuthor() + ".csv");
            activityExportSource.launch(intent);
        });
    }

    private void createListenerCardFavouriteRadio() {
        final RadioButton radioButtonFavourites = this.fragmentQuotationsTabFilterBinding.radioButtonFavourites;
        radioButtonFavourites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                quotationsPreferences.setContentSelection(ContentSelection.FAVOURITES);
                setCard();
            }
        });
    }

    private void createListenerCardFavouritesBrowse() {
        fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse.setOnClickListener(v -> {
            setCardFavouriteButtonBrowse(false);
            setCardFavouriteButtonExport(false);

            BrowseFavouritesDialogFragment browseFavouritesDialogFragment = new BrowseFavouritesDialogFragment(
                widgetId,
                quoteUnquoteModel,
                R.string.fragment_quotations_selection_dialog_browse_favourites);
            browseFavouritesDialogFragment.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerCardFavouritesExport() {
        fragmentQuotationsTabFilterBinding.buttonFavouritesExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabFilterBinding.buttonFavouritesExport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TEXT_CSV);

                activityExportQuotationEntityList = quoteUnquoteModel.exportFavourites();

                intent.putExtra(Intent.EXTRA_TITLE, "Favourite.csv");
                activityExportFavourites.launch(intent);
            }
        });
    }

    private void createListenerCardSearchRadio() {
        final RadioButton radioButtonSearch = this.fragmentQuotationsTabFilterBinding.radioButtonSearch;
        radioButtonSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                quotationsPreferences.setContentSelection(ContentSelection.SEARCH);
                setCard();
            }
        });
    }

    private void createListenerCardSearchFavourites() {
        fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            quotationsPreferences.setContentSelectionSearchFavouritesOnly(isChecked);

            resetSearch();
            setCardSearchTextFocus();
        });
    }

    private void createListenerCardSearchRegEx() {
        fragmentQuotationsTabFilterBinding.switchRegEx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            quotationsPreferences.setContentSelectionSearchRegEx(isChecked);

            resetSearch();
            setCardSearchTextFocus();
        });
    }

    private void resetSearch() {
        String searchString = quotationsPreferences.getContentSelectionSearch();
        fragmentQuotationsTabFilterBinding.editTextSearchText.setText("");
        fragmentQuotationsTabFilterBinding.editTextSearchText.setText(searchString);
        quotationsPreferences.setContentSelectionSearch(searchString);
    }

    private void createListenerCardSearchBrowse() {
        fragmentQuotationsTabFilterBinding.buttonSearchBrowse.setOnClickListener(v -> {

            setCardSearchButtonBrowse(false);
            setCardSearchButtonExport(false);

            BrowseSearchDialogFragment browseSearchDialogFragment = new BrowseSearchDialogFragment(
                widgetId,
                quoteUnquoteModel,
                quotationsPreferences.getContentSelectionSearch());

            browseSearchDialogFragment.show(getParentFragmentManager(), "");
        });
    }

    private void createListenerCardSearchExport() {
        // invoke Storage Access Framework
        fragmentQuotationsTabFilterBinding.buttonSearchExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabFilterBinding.buttonSearchExport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TEXT_CSV);

                QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getActivity());

                if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                    activityExportQuotationEntityList = quoteUnquoteModel.getSearchQuotationsRegEx(
                        this.widgetId,
                        quotationsPreferences.getContentSelectionSearch(),
                        quotationsPreferences.getContentSelectionSearchFavouritesOnly()
                    );
                } else {
                    activityExportQuotationEntityList = quoteUnquoteModel.getSearchQuotations(
                        this.widgetId,
                        quotationsPreferences.getContentSelectionSearch(),
                        quotationsPreferences.getContentSelectionSearchFavouritesOnly()
                    );
                }

                intent.putExtra(
                    Intent.EXTRA_TITLE,
                    quotationsPreferences.getContentSelectionSearch() + ".csv");

                activityExportSearch.launch(intent);
            }
        });
    }

    private ActivityResultLauncher<Intent> activityExport() {
        return registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            activityResult -> {
                if (activityResult.getResultCode() == Activity.RESULT_OK) {

                    try {
                        if (activityResult.getData() == null || activityResult.getData().getData() == null) {
                            Timber.d("activityExport: no data returned from create document");
                            Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.fragment_quotations_selection_export_failed),
                                Toast.LENGTH_SHORT).show();
                        } else if (activityExportQuotationEntityList == null) {
                            Timber.d("activityExport: no quotations to export");
                            Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.fragment_quotations_selection_export_failed),
                                Toast.LENGTH_SHORT).show();
                        } else {
                            final ParcelFileDescriptor parcelFileDescriptor
                                = getContext().getContentResolver().openFileDescriptor(
                                activityResult.getData().getData(), "wt");
                            final FileOutputStream fileOutputStream
                                = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                            new ImportHelper().csvExport(
                                fileOutputStream,
                                (ArrayList<QuotationEntity>) activityExportQuotationEntityList);

                            fileOutputStream.close();
                            parcelFileDescriptor.close();

                            Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.fragment_quotations_selection_export_success),
                                Toast.LENGTH_SHORT).show();
                        }
                    } catch (final IOException e) {
                        Timber.e(e.getMessage());
                        Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.fragment_quotations_selection_export_failed),
                            Toast.LENGTH_SHORT).show();
                    }
                }

                ConfigureActivity.launcherInvoked = false;
            });
    }
}

