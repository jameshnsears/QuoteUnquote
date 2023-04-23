package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    @Nullable
    private ActivityResultLauncher<Intent> activitExportFavouritesResultsLauncher;
    @Nullable
    private ActivityResultLauncher<Intent> activitExportSearchResultsLauncher;

    public QuotationsFilterFragment() {
        // dark mode support
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
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());

        /*
        Firebase, possible fix to:
        Fatal Exception: e9.c
        The exception could not be delivered to the consumer because it has already canceled/disposed the flow or the exception has nowhere to go to begin with. Further reading: https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling | java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        io.reactivex.plugins.RxJavaPlugins.onError (RxJavaPlugins.java:42)
         */
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
    public void onDestroyView() {
        super.onDestroyView();

        ensureFragmentContentSearchConsistency(this.widgetId, this.getContext());

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

    public void setSearch() {
        setSearchObserver();

        final String editTextKeywords = this.quotationsPreferences.getContentSelectionSearch();

        if (editTextKeywords.length() > 0) {
            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("SEARCH", properties);
            this.fragmentQuotationsTabFilterBinding.editTextSearchText.setText(editTextKeywords);
        } else {
            this.fragmentQuotationsTabFilterBinding.editTextSearchText.setText("");
        }

        alignFavouriteWidgetsWithAvailability();
    }

    protected void setSearchObserver() {
        this.disposableObserverSearch = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                fragmentQuotationsTabFilterBinding.radioButtonSearch.setText(
                        getResources().getString(R.string.fragment_quotations_selection_search, value));
                quotationsPreferences.setContentSelectionSearchCount(value);

                if (value > 0) {
                    setButtonSearchBrowse(true);
                    setButtonSearchExport(true);
                } else {
                    setButtonSearchBrowse(false);
                    setButtonSearchExport(false);
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

                    if (!keywords.equals("") && keywords.length() >= 4) {
                        Timber.d("%s", keywords);

                        // remove any prior, different, search results in the history
                        if (!keywords.equals(quotationsPreferences.getContentSelectionSearch())) {
                            quoteUnquoteModel.resetPrevious(this.widgetId, ContentSelection.SEARCH);
                        }

                        quotationsPreferences.setContentSelectionSearch(keywords);

                        return quoteUnquoteModel.countQuotationWithSearchText(
                                keywords, quotationsPreferences.getContentSelectionSearchFavouritesOnly());
                    } else {
                        quotationsPreferences.setContentSelectionSearch("");
                        return 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserverSearch);
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        initUI();

        createListenerRadioAll();
        createListenerRadioSource();
        createListenerRadioFavourite();
        createListenerRadioSearch();

        createListenerAuthorsQuotationCount();
        createListenerAuthor();

        createListenerButtonFavouritesBrowse();
        createListenerButtonFavouritesExport();
        handleExportFavouritesResult();

        createListenerSearchFavouritesOnly();
        createListenerButtonSearchBrowse();
        createListenerButtonSearchExport();
        handleExportSearchResult();

        setSelection();
    }

    public void initUI() {
        setInitialCounts();

        setAllCount();
        setExclusions();

        setAuthorsQuotationCount();
        setAuthors();

        setFavouriteCount();

        setSearch();
    }

    private void setInitialCounts() {
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

    public void setAllCount() {
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

    public void setExclusions() {
        setExclusionsObserver();

        final String editTextKeywords = quotationsPreferences.getContentSelectionAllExclusion();

        if (editTextKeywords.length() > 0) {
            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("EXCLUSIONS", properties);
            fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setText(editTextKeywords);
        } else {
            fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setText("");
        }
    }

    public void setExclusionsObserver() {
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
                .debounce(25, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    final String exclusions = charSequence.toString();

                    quotationsPreferences.setContentSelectionAllExclusion(exclusions);

                    if (quoteUnquoteModel.getCurrentQuotation(widgetId) != null) {
                        quoteUnquoteModel.markAsCurrentLastPrevious(widgetId);
                    }

                    return quoteUnquoteModel.countAllMinusExclusions(widgetId).blockingGet();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserverAllExclusion);
    }

    public void setAuthorsQuotationCount() {
        disposables.add(quoteUnquoteModel.authorsQuotationCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<List<Integer>>() {
                            @Override
                            public void onSuccess(@NonNull final List<Integer> authorCountList) {
                                populateAuthorsQuotationCount(authorCountList);
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    public void populateAuthorsQuotationCount(@NonNull List<Integer> authorCountList) {
        final ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item,
                authorCountList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setAdapter(adapter);

        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setSelection(
                authorCountList.indexOf(quotationsPreferences.getContentSelectionAuthorCount()));
    }

    public void setAuthors() {
        disposables.add(quoteUnquoteModel.authors(
                        quotationsPreferences.getContentSelectionAuthorCount().intValue()
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<List<AuthorPOJO>>() {
                            @Override
                            public void onSuccess(@NonNull final List<AuthorPOJO> authorPOJOList) {
                                populateAuthors(authorPOJOList);
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    public void populateAuthors(@NonNull List<AuthorPOJO> authorPOJOList) {
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
            setAuthorName(quotationsPreferences.getContentSelectionAuthor());
        } else {
            quotationsPreferences.setContentSelectionAuthor(authors.get(0));
            setAuthorName(authors.get(0));
        }
    }

    protected void setAuthorName(final String authorPreference) {
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setSelection(
                quoteUnquoteModel.authorsIndex(authorPreference));

        fragmentQuotationsTabFilterBinding.radioButtonAuthor.setText(
                getResources().getString(
                        R.string.fragment_quotations_selection_author,
                        quoteUnquoteModel.countAuthorQuotations(authorPreference)));
    }

    public void setFavouriteCount() {
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

    public void setSelection() {
        enableExclusion(false);
        enableAuthor(false);
        enableSearch();

        switch (quotationsPreferences.getContentSelection()) {
            case ALL:
                setSelectionAll();
                break;
            case AUTHOR:
                setSelectionAuthor();
                break;
            case FAVOURITES:
                setSelectionFavourites();
                break;
            case SEARCH:
                setSelectionSearch();
                break;
            default:
                Timber.e("unknown switch");
                break;
        }
    }

    private void setSelectionAll() {
        fragmentQuotationsTabFilterBinding.radioButtonAll.setChecked(true);
        enableExclusion(true);
    }

    private void setSelectionAuthor() {
        fragmentQuotationsTabFilterBinding.radioButtonAuthor.setChecked(true);
        enableExclusion(false);
        enableAuthor(true);
    }

    private void setSelectionFavourites() {
        fragmentQuotationsTabFilterBinding.radioButtonFavourites.setChecked(true);
        enableExclusion(false);
    }

    private void setSelectionSearch() {
        fragmentQuotationsTabFilterBinding.radioButtonSearch.setChecked(true);
        enableExclusion(false);
        enableSearch();

        fragmentQuotationsTabFilterBinding.radioButtonSearch.requestFocus();

        fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(
                quotationsPreferences.getContentSelectionSearchFavouritesOnly());

        final String searchText = quotationsPreferences.getContentSelectionSearch();

        if (!searchText.equals("") && !quotationsPreferences.getContentSelectionSearch().equals(searchText)) {
            quotationsPreferences.setContentSelectionSearch(searchText);

            final EditText editTextKeywordsSearch = fragmentQuotationsTabFilterBinding.editTextSearchText;
            editTextKeywordsSearch.setText(searchText);
        }
    }

    private void setCardAll(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonAll.setChecked(enabled);
        enableExclusion(enabled);

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.ALL);
        }
    }

    private void setCardAuthor(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonAuthor.setChecked(enabled);
        enableAuthor(enabled);

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.AUTHOR);
        }
    }

    private void setCardFavourite(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonFavourites.setChecked(enabled);

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.FAVOURITES);
        }

        alignFavouriteWidgetsWithAvailability();
    }

    private void setCardSearch(final boolean enabled) {
        this.fragmentQuotationsTabFilterBinding.radioButtonSearch.setChecked(enabled);
        enableSearch();

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.SEARCH);
        }
    }

    protected void createListenerRadioAll() {
        final RadioButton radioButtonAll = this.fragmentQuotationsTabFilterBinding.radioButtonAll;
        radioButtonAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setCardAll(true);
                setCardAuthor(false);
                setCardFavourite(false);
                setCardSearch(false);
            }
        });
    }

    protected void createListenerRadioSource() {
        final RadioButton radioButtonAuthor = this.fragmentQuotationsTabFilterBinding.radioButtonAuthor;
        radioButtonAuthor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setCardAll(false);
                setCardAuthor(true);
                setCardFavourite(false);
                setCardSearch(false);
            }
        });
    }

    protected void createListenerRadioFavourite() {
        final RadioButton radioButtonFavourites = this.fragmentQuotationsTabFilterBinding.radioButtonFavourites;
        radioButtonFavourites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setCardAll(false);
                setCardAuthor(false);
                setCardFavourite(true);
                setCardSearch(false);
            }
        });
    }

    protected void createListenerRadioSearch() {
        final RadioButton radioButtonSearch = this.fragmentQuotationsTabFilterBinding.radioButtonSearch;
        radioButtonSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setCardAll(false);
                setCardAuthor(false);
                setCardFavourite(false);
                setCardSearch(true);
            }
        });
    }

    private void enableExclusion(final boolean enable) {
        fragmentQuotationsTabFilterBinding.editTextResultsExclusion.setEnabled(enable);
        fragmentQuotationsTabFilterBinding.textViewExclusionInfo.setEnabled(enable);
    }

    private void enableAuthor(final boolean enable) {
        fragmentQuotationsTabFilterBinding.textViewQuotationCount.setEnabled(enable);
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setEnabled(enable);
        fragmentQuotationsTabFilterBinding.spinnerAuthors.setEnabled(enable);
    }

    private void enableSearch() {
        fragmentQuotationsTabFilterBinding.editTextSearchText.setEnabled(true);

        if (quotationsPreferences.getContentSelectionSearchFavouritesOnly()) {
            fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(true);
        } else {
            fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(false);
        }

        alignFavouriteWidgetsWithAvailability();
    }

    private void alignFavouriteWidgetsWithAvailability() {
        if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse.setEnabled(true);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse, true);

            fragmentQuotationsTabFilterBinding.buttonFavouritesExport.setEnabled(true);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonFavouritesExport, true);

            ////////

            fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(true);
        } else {
            fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse.setEnabled(false);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse, false);

            fragmentQuotationsTabFilterBinding.buttonFavouritesExport.setEnabled(false);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonFavouritesExport, false);

            ////////

            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setEnabled(false);
            this.fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setChecked(false);
        }
    }

    public void setButtonSearchBrowse(boolean enabled) {
        fragmentQuotationsTabFilterBinding.buttonSearchBrowse.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonSearchBrowse, enabled);
    }

    public void setButtonSearchExport(boolean enabled) {
        fragmentQuotationsTabFilterBinding.buttonSearchExport.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabFilterBinding.buttonSearchExport, enabled);
    }

    public void createListenerAuthorsQuotationCount() {
        fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String authorCount = fragmentQuotationsTabFilterBinding.spinnerAuthorsCount.getSelectedItem().toString();

                quotationsPreferences.setContentSelectionAuthorCount(Integer.valueOf(authorCount));

                setAuthors();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    protected void createListenerAuthor() {
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

    protected void createListenerButtonFavouritesBrowse() {
        fragmentQuotationsTabFilterBinding.buttonFavouritesBrowse.setOnClickListener(v -> {
            BrowseFavouritesDialogFragment appearanceTextDialogQuotation = new BrowseFavouritesDialogFragment(
                    widgetId,
                    quoteUnquoteModel,
                    R.string.fragment_quotations_selection_dialog_browse_favourites);
            appearanceTextDialogQuotation.show(getParentFragmentManager(), "");
        });
    }

    protected void createListenerButtonSearchBrowse() {
        fragmentQuotationsTabFilterBinding.buttonSearchBrowse.setOnClickListener(v -> {
            BrowseSearchDialogFragment appearanceTextDialogQuotation = new BrowseSearchDialogFragment(
                    widgetId,
                    quoteUnquoteModel,
                    R.string.fragment_quotations_selection_dialog_browse_search);
            appearanceTextDialogQuotation.show(getParentFragmentManager(), "");
        });
    }

    protected void createListenerButtonSearchExport() {
        // invoke Storage Access Framework
        fragmentQuotationsTabFilterBinding.buttonSearchExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabFilterBinding.buttonSearchExport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "SearchResults.csv");
                activitExportSearchResultsLauncher.launch(intent);
            }
        });
    }

    protected void createListenerButtonFavouritesExport() {
        // invoke Storage Access Framework
        fragmentQuotationsTabFilterBinding.buttonFavouritesExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabFilterBinding.buttonFavouritesExport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "Favourites.csv");
                activitExportFavouritesResultsLauncher.launch(intent);
            }
        });
    }

    protected void createListenerSearchFavouritesOnly() {
        fragmentQuotationsTabFilterBinding.switchSearchFavouritesOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            quotationsPreferences.setContentSelectionSearchFavouritesOnly(isChecked);

            Editable priorText = fragmentQuotationsTabFilterBinding.editTextSearchText.getText();
            fragmentQuotationsTabFilterBinding.editTextSearchText.setText("");
            fragmentQuotationsTabFilterBinding.editTextSearchText.append(priorText);
        });
    }

    protected final void handleExportFavouritesResult() {
        // default: /storage/emulated/0/Download/Favourites.csv
        activitExportFavouritesResultsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {

                        try {
                            final ParcelFileDescriptor parcelFileDescriptor
                                    = getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "w");
                            final FileOutputStream fileOutputStream
                                    = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                            new ImportHelper()
                                    .csvExport(
                                            fileOutputStream,
                                            (ArrayList) quoteUnquoteModel.exportFavourites());

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

    protected final void handleExportSearchResult() {
        // default: /storage/emulated/0/Download/SearchResults.csv
        activitExportSearchResultsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {

                        try {
                            final ParcelFileDescriptor parcelFileDescriptor
                                    = getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "w");
                            final FileOutputStream fileOutputStream
                                    = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                            List<QuotationEntity> searchResultsList = quoteUnquoteModel.getSearchQuotations(
                                    quotationsPreferences.getContentSelectionSearch(),
                                    quotationsPreferences.getContentSelectionSearchFavouritesOnly()
                            );

                            new ImportHelper()
                                    .csvExport(
                                            fileOutputStream,
                                            (ArrayList) searchResultsList);

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
