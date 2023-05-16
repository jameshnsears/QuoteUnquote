package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabFilterBinding;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
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
    @Nullable
    public static List<QuotationEntity> activityExportQuotationEntityList;

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

    @Override
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());

        RxJavaPlugins.setErrorHandler(e -> {
            Timber.e(e.getMessage());
        });
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

        createListenerCardAll();

        createListenerCardSource();
        createListenerCardSourceCount();
        createListenerCardSourceSelection();
        createListenerCardSourceBrowse();
        createListenerCardSourceExport();

        createListenerCardFavourite();
        createListenerCardFavouritesBrowse();
        createListenerCardFavouritesExport();

        createListenerCardSearch();
        createListenerCardSearchFavourites();
        createListenerCardSearchRegEx();
        createListenerCardSearchBrowse();
        createListenerCardSearchExport();

        createListenerCardForceEnableButtons();

        setCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ensureFragmentContentSearchConsistency(this.widgetId, this.getContext());

        this.fragmentQuotationsTabFilterBinding = null;

        this.shutdown();
    }

    ////////////////

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

    ////////////////

    public void initUI() {
        initCardCounts();

        setDisposableCardAllCount();
        setDisposableCardAllCountExclusions();
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

        fragmentQuotationsTabFilterBinding.radioButtonAuthor.setText(
                getResources().getString(R.string.fragment_quotations_selection_author,
                        0));

        fragmentQuotationsTabFilterBinding.radioButtonFavourites.setText(
                getResources().getString(R.string.fragment_quotations_selection_favourites,
                        0));

        fragmentQuotationsTabFilterBinding.radioButtonSearch.setText(
                getResources().getString(R.string.fragment_quotations_selection_search,
                        0));
    }

    ////////////////

    public void setCard() {
        setCardAll(false);
        setCardSource(false);
        setCardFavourite(false);
        setCardSearch(false);

        switch (quotationsPreferences.getContentSelection()) {
            case AUTHOR:
                setCardSource(true);
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

        setCardForceEnable();
    }

    private void setCardAll(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonAll.setChecked(enabled);

        final String editTextKeywords = quotationsPreferences.getContentSelectionAllExclusion();

        if (enabled && editTextKeywords.length() > 0) {
            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("EXCLUSIONS", properties);
        }

        fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setText(editTextKeywords);

        if (enabled) {
            if (quotationsPreferences.getContentSelection() == ContentSelection.ALL) {
                this.fragmentQuotationsTabFilterBinding.editTextResultsExclusion.requestFocus();
                int selectionPosition
                        = this.fragmentQuotationsTabFilterBinding.editTextResultsExclusion.getText().length();
                this.fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setSelection(selectionPosition);
            }
        }
        fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setEnabled(enabled);
        fragmentQuotationsTabFilterBinding.textViewExclusionInfo.setEnabled(enabled);
    }

    private void setCardSource(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonAuthor.setChecked(enabled);

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
        final ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item,
                authorCountList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setAdapter(adapter);

        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setSelection(
                authorCountList.indexOf(quotationsPreferences.getContentSelectionAuthorCount()));
    }

    void setCardSourceSelection(@NonNull List<AuthorPOJO> authorPOJOList) {
        final List<String> authors
                = quoteUnquoteModel.authorsSorted(authorPOJOList);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item,
                authors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setAdapter(adapter);

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

    private void setCardSourceSelectionName(final String authorPreference) {
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setSelection(
                quoteUnquoteModel.authorsIndex(authorPreference));

        fragmentQuotationsTabFilterBinding.radioButtonAuthor.setText(
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

        ///

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

        ///

        this.fragmentQuotationsTabFilterBinding.switchRegEx.setEnabled(enabled);
        this.fragmentQuotationsTabFilterBinding.switchRegEx
                .setChecked(quotationsPreferences.getContentSelectionSearchRegEx());

        ///

        String keywords = quotationsPreferences.getContentSelectionSearch();
        fragmentQuotationsTabFilterBinding.editTextSearchText.setText(keywords);

        this.fragmentQuotationsTabFilterBinding.editTextSearchText.setEnabled(enabled);
        if (enabled) {
            setCardSearchTextFocus();
        }

        ///

        this.fragmentQuotationsTabFilterBinding.textViewSearchMinimumInfo.setEnabled(enabled);

        ///

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

    private void setCardForceEnable() {
        fragmentQuotationsTabFilterBinding.switchForceEnableButtons.setChecked(
                quotationsPreferences.getContentSelectionSearchForceEnableButtons()
        );

        forceEnableButtons(quotationsPreferences.getContentSelectionSearchForceEnableButtons());
    }

    ////////////////

    private void alignCards() {
        int countFavourites = quoteUnquoteModel.countFavouritesWithoutRx();
        alignCardSource();
        alignCardFavourites(countFavourites);
        alignCardSearch(countFavourites);

        forceEnableButtons(quotationsPreferences.getContentSelectionSearchForceEnableButtons());
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

    ////////////////

    private void setDisposableCardAllCount() {
        disposables.add(quoteUnquoteModel.countAllMinusExclusions(widgetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull final Integer value) {
                                fragmentQuotationsTabFilterBinding.radioButtonAll.setText(
                                        getResources().getString(R.string.fragment_quotations_selection_all, value));
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    private void setDisposableCardAllCountExclusions() {
        disposableObserverAllExclusion = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer allMinusExclusions) {
                fragmentQuotationsTabFilterBinding.radioButtonAll.setText(
                        getResources().getString(R.string.fragment_quotations_selection_all, allMinusExclusions));
            }

            @Override
            public void onError(@NonNull final Throwable throwable) {
                Timber.d("onError=%s", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        };

        RxTextView.textChanges(fragmentQuotationsTabFilterBinding.editTextResultsExclusion)
                .startWith(quotationsPreferences.getContentSelectionAllExclusion())
                .debounce(25, TimeUnit.MILLISECONDS)
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
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    private void setDisposableCardSourceCount() {
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
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    private void setDisposableCardFavouriteCount() {
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
                    if (quotationsPreferences.getContentSelection() == ContentSelection.SEARCH
                        ||
                            quotationsPreferences.getContentSelectionSearchForceEnableButtons()) {
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
                Timber.d("onError=%s", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        };

        RxTextView.textChanges(fragmentQuotationsTabFilterBinding.editTextSearchText)
                .debounce(25, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    final String keywords = charSequence.toString();

                    quotationsPreferences.setContentSelectionSearch(keywords);

                    if (!keywords.equals("") && keywords.length() >= 4) {
                        if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                            try {
                                // https://regex101.com/
                                // mind.$ - find "mind." at end
                                // mind[ - invalid expression
                                Pattern pattern = Pattern.compile(keywords, Pattern.CASE_INSENSITIVE);

                                // remove any prior, different, search results in the history
                                if (!keywords.equals(quotationsPreferences.getContentSelectionSearch())) {
                                    quoteUnquoteModel.resetPrevious(this.widgetId, ContentSelection.SEARCH);
                                }

                                return quoteUnquoteModel.countQuotationWithSearchRegEx(
                                        keywords, quotationsPreferences.getContentSelectionSearchFavouritesOnly());
                            } catch (PatternSyntaxException e) {
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(() -> Toast.makeText(
                                        getContext(),
                                        getContext().getString(R.string.fragment_quotations_selection_search_switch_regex_invalid),
                                        Toast.LENGTH_SHORT).show());
                                return 0;
                            }
                        } else {
                            if (!keywords.equals(quotationsPreferences.getContentSelectionSearch())) {
                                quoteUnquoteModel.resetPrevious(this.widgetId, ContentSelection.SEARCH);
                            }

                            return quoteUnquoteModel.countQuotationWithSearchText(
                                    keywords, quotationsPreferences.getContentSelectionSearchFavouritesOnly());
                        }
                    } else {
                        return 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserverSearch);
    }

    ////////////////

    private void createListenerCardAll() {
        final RadioButton radioButtonAll = this.fragmentQuotationsTabFilterBinding.radioButtonAll;
        radioButtonAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                quotationsPreferences.setContentSelection(ContentSelection.ALL);
                setCard();
            }
        });
    }

    private void createListenerCardSource() {
        final RadioButton radioButtonAuthor = this.fragmentQuotationsTabFilterBinding.radioButtonAuthor;
        radioButtonAuthor.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
                fragmentQuotationsTabFilterBinding.radioButtonAuthor.setText(
                        getResources().getString(R.string.fragment_quotations_selection_author,
                                quoteUnquoteModel.countAuthorQuotations(author)
                        ));

                if (!quotationsPreferences.getContentSelectionAuthor().equals(author)) {
                    Timber.d("author=%s", author);
                    final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                    properties.put("Author", author);
                    AuditEventHelper.auditEvent("AUTHOR", properties);

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
            intent.setType("text/csv");

            activityExportQuotationEntityList = quoteUnquoteModel.getQuotationsForAuthor(
                    quotationsPreferences.getContentSelectionAuthor());

            intent.putExtra(
                    Intent.EXTRA_TITLE,
                    quotationsPreferences.getContentSelectionAuthor() + ".csv");
            activityExportSource.launch(intent);
        });
    }

    private void createListenerCardFavourite() {
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
                intent.setType("text/csv");

                activityExportQuotationEntityList = quoteUnquoteModel.exportFavourites();

                intent.putExtra(Intent.EXTRA_TITLE, "Favourite.csv");
                activityExportFavourites.launch(intent);
            }
        });
    }

    private void createListenerCardSearch() {
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

            quoteUnquoteModel.resetPrevious(widgetId, ContentSelection.SEARCH);

            String searchString = quotationsPreferences.getContentSelectionSearch();
            fragmentQuotationsTabFilterBinding.editTextSearchText.setText("");
            fragmentQuotationsTabFilterBinding.editTextSearchText.setText(searchString);
            quotationsPreferences.setContentSelectionSearch(searchString);

            setCardSearchTextFocus();
        });
    }

    private void createListenerCardSearchRegEx() {
        fragmentQuotationsTabFilterBinding.switchRegEx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            quotationsPreferences.setContentSelectionSearchRegEx(isChecked);

            quoteUnquoteModel.resetPrevious(widgetId, ContentSelection.SEARCH);

            String searchString = quotationsPreferences.getContentSelectionSearch();
            fragmentQuotationsTabFilterBinding.editTextSearchText.setText("");
            fragmentQuotationsTabFilterBinding.editTextSearchText.setText(searchString);
            quotationsPreferences.setContentSelectionSearch(searchString);

            setCardSearchTextFocus();
        });
    }

    private void createListenerCardSearchBrowse() {
        fragmentQuotationsTabFilterBinding.buttonSearchBrowse.setOnClickListener(v -> {

            setCardSearchButtonBrowse(false);
            setCardSearchButtonExport(false);

            BrowseSearchDialogFragment browseSearchDialogFragment = new BrowseSearchDialogFragment(
                    widgetId,
                    quoteUnquoteModel,
                    quotationsPreferences.getContentSelectionSearch());

            browseSearchDialogFragment.show(getParentFragmentManager(), "");;
        });
    }

    private void createListenerCardSearchExport() {
        // invoke Storage Access Framework
        fragmentQuotationsTabFilterBinding.buttonSearchExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabFilterBinding.buttonSearchExport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");

                QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getActivity());

                if (quotationsPreferences.getContentSelectionSearchRegEx()) {
                    activityExportQuotationEntityList = quoteUnquoteModel.getSearchQuotationsRegEx(
                            quotationsPreferences.getContentSelectionSearch(),
                            quotationsPreferences.getContentSelectionSearchFavouritesOnly()
                    );
                } else {
                    activityExportQuotationEntityList = quoteUnquoteModel.getSearchQuotations(
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

    private void createListenerCardForceEnableButtons() {
        fragmentQuotationsTabFilterBinding.switchForceEnableButtons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            fragmentQuotationsTabFilterBinding.switchForceEnableButtons.setChecked(isChecked);
            quotationsPreferences.setContentSelectionSearchForceEnableButtons(isChecked);

            forceEnableButtons(isChecked);
        });
    }

    private void forceEnableButtons(boolean isChecked) {
        if (isChecked) {
            if (quotationsPreferences.getContentSelection() != ContentSelection.AUTHOR) {
                fragmentQuotationsTabFilterBinding.textViewQuotationCount.setEnabled(true);
                fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setEnabled(true);
                fragmentQuotationsTabFilterBinding.spinnerAuthors.setEnabled(true);
                setCardSourceButtonBrowse(true);
                setCardSourceButtonExport(true);
            }

            if (quotationsPreferences.getContentSelection() != ContentSelection.FAVOURITES) {
                if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
                    setCardFavouriteButtonBrowse(true);
                    setCardFavouriteButtonExport(true);
                }
            }

            if (quotationsPreferences.getContentSelection() != ContentSelection.SEARCH) {
                if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
                    fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(true);
                }
                fragmentQuotationsTabFilterBinding.switchRegEx.setEnabled(true);
                fragmentQuotationsTabFilterBinding.editTextSearchText.setEnabled(true);
                fragmentQuotationsTabFilterBinding.editTextSearchText.setText(
                        quotationsPreferences.getContentSelectionSearch()
                );
                if (quotationsPreferences.getContentSelectionSearch().length() >= 4) {
                    setCardSearchButtonBrowse(true);
                    setCardSearchButtonExport(true);
                }
            }
        } else {
            if (quotationsPreferences.getContentSelection() != ContentSelection.AUTHOR) {
                fragmentQuotationsTabFilterBinding.textViewQuotationCount.setEnabled(false);
                fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setEnabled(false);
                fragmentQuotationsTabFilterBinding.spinnerAuthors.setEnabled(false);
                setCardSourceButtonBrowse(false);
                setCardSourceButtonExport(false);
            }

            if (quotationsPreferences.getContentSelection() != ContentSelection.FAVOURITES) {
                setCardFavouriteButtonBrowse(false);
                setCardFavouriteButtonExport(false);
            }

            if (quotationsPreferences.getContentSelection() != ContentSelection.SEARCH) {
                if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
                    fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(false);
                }
                fragmentQuotationsTabFilterBinding.switchRegEx.setEnabled(false);
                fragmentQuotationsTabFilterBinding.editTextSearchText.setEnabled(false);
                fragmentQuotationsTabFilterBinding.editTextSearchText.setText(
                        quotationsPreferences.getContentSelectionSearch()
                );
                setCardSearchButtonBrowse(false);
                setCardSearchButtonExport(false);
            }
        }
    }

    ////////////////

    private ActivityResultLauncher<Intent> activityExport() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {

                        try {
                            final ParcelFileDescriptor parcelFileDescriptor
                                    = getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "w");
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
                        } catch (final IOException e) {
                            Timber.e(e.getMessage());
                        }
                    }

                    ConfigureActivity.launcherInvoked = false;
                });
    }
}