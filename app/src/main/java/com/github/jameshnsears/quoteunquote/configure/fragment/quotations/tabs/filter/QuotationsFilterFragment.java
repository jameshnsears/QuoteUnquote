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
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabSelectionBinding;
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
    public FragmentQuotationsTabSelectionBinding fragmentQuotationsTabSelectionBinding;
    @Nullable
    public QuotationsPreferences quotationsPreferences;
    @Nullable
    private DisposableObserver<Integer> disposableObserverExclusion;
    @Nullable
    private DisposableObserver<Integer> disposableObserverSearch;
    @Nullable
    private ActivityResultLauncher<Intent> activityResultLauncher;

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

        this.fragmentQuotationsTabSelectionBinding = FragmentQuotationsTabSelectionBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsTabSelectionBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ensureFragmentContentSearchConsistency(this.widgetId, this.getContext());

        this.fragmentQuotationsTabSelectionBinding = null;

        this.shutdown();
    }

    public void shutdown() {
        this.disposables.clear();
        this.disposables.dispose();

        if (this.disposableObserverExclusion != null) {
            this.disposableObserverExclusion.dispose();
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
            this.fragmentQuotationsTabSelectionBinding.editTextSearchText.setText(editTextKeywords);
        } else {
            this.fragmentQuotationsTabSelectionBinding.editTextSearchText.setText("");
        }
    }

    protected void setSearchObserver() {
        this.disposableObserverSearch = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                fragmentQuotationsTabSelectionBinding.radioButtonSearch.setText(
                        getResources().getString(R.string.fragment_quotations_selection_search, value));
                quotationsPreferences.setContentSelectionSearchCount(value);

                if (value > 0) {
                    fragmentQuotationsTabSelectionBinding.buttonBrowseSearch.setEnabled(true);
                    QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonBrowseSearch, true);
                } else {
                    fragmentQuotationsTabSelectionBinding.buttonBrowseSearch.setEnabled(false);
                    QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonBrowseSearch, false);
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

        RxTextView.textChanges(fragmentQuotationsTabSelectionBinding.editTextSearchText)
                .debounce(25, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    final String keywords = charSequence.toString();

                    if (!keywords.equals("")) {
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

        createListenerBrowseFavouriteButton();
        createListenerFavouriteButtonExport();

        createListenerSearchFavouritesOnly();
        createListenerBrowseSearchButton();

        handleExportResult();

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
        if (fragmentQuotationsTabSelectionBinding == null) {
            fragmentQuotationsTabSelectionBinding
                    = FragmentQuotationsTabSelectionBinding.inflate(this.getLayoutInflater());
        }

        fragmentQuotationsTabSelectionBinding.radioButtonAll.setText(
                getResources().getString(R.string.fragment_quotations_selection_all,
                        0));

        fragmentQuotationsTabSelectionBinding.radioButtonAuthor.setText(
                getResources().getString(R.string.fragment_quotations_selection_author,
                        0));

        fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setText(
                getResources().getString(R.string.fragment_quotations_selection_favourites,
                        0));

        fragmentQuotationsTabSelectionBinding.radioButtonSearch.setText(
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
                                fragmentQuotationsTabSelectionBinding.radioButtonAll.setText(
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
            fragmentQuotationsTabSelectionBinding.editTextResultsExclusion.setText(editTextKeywords);
        } else {
            fragmentQuotationsTabSelectionBinding.editTextResultsExclusion.setText("");
        }
    }

    public void setExclusionsObserver() {
        disposableObserverExclusion = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer allMinusExclusions) {
                fragmentQuotationsTabSelectionBinding.radioButtonAll.setText(
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

        RxTextView.textChanges(fragmentQuotationsTabSelectionBinding.editTextResultsExclusion)
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
                .subscribe(disposableObserverExclusion);
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
        fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setAdapter(adapter);

        fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setSelection(
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
        fragmentQuotationsTabSelectionBinding.spinnerAuthors.setAdapter(adapter);

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
        fragmentQuotationsTabSelectionBinding.spinnerAuthors.setSelection(
                quoteUnquoteModel.authorsIndex(authorPreference));

        fragmentQuotationsTabSelectionBinding.radioButtonAuthor.setText(
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
                                fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setEnabled(true);
                                if (value == 0) {
                                    fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setEnabled(false);

                                    // in case another widget instance changes favourites
                                    if (QuotationsFilterFragment.this.quotationsPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)) {
                                        QuotationsFilterFragment.this.quotationsPreferences.setContentSelection(ContentSelection.ALL);
                                    }
                                }

                                fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setText(
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
        enableSearch(false);

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
        fragmentQuotationsTabSelectionBinding.radioButtonAll.setChecked(true);
        enableExclusion(true);
    }

    private void setSelectionAuthor() {
        fragmentQuotationsTabSelectionBinding.radioButtonAuthor.setChecked(true);
        enableExclusion(false);
        enableAuthor(true);
    }

    private void setSelectionFavourites() {
        fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setChecked(true);
        enableExclusion(false);
    }

    private void setSelectionSearch() {
        fragmentQuotationsTabSelectionBinding.radioButtonSearch.setChecked(true);
        enableExclusion(false);
        enableSearch(true);

        fragmentQuotationsTabSelectionBinding.radioButtonSearch.requestFocus();

        if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setEnabled(true);
        } else {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setEnabled(false);
        }

        fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setChecked(
                quotationsPreferences.getContentSelectionSearchFavouritesOnly());

        final String searchText = quotationsPreferences.getContentSelectionSearch();

        if (!searchText.equals("") && !quotationsPreferences.getContentSelectionSearch().equals(searchText)) {
            quotationsPreferences.setContentSelectionSearch(searchText);

            final EditText editTextKeywordsSearch = fragmentQuotationsTabSelectionBinding.editTextSearchText;
            editTextKeywordsSearch.setText(searchText);
        }
    }

    private void setCardAll(final boolean enabled) {
        this.fragmentQuotationsTabSelectionBinding.radioButtonAll.setChecked(enabled);
        enableExclusion(enabled);

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.ALL);
        }
    }

    private void setCardAuthor(final boolean enabled) {
        this.fragmentQuotationsTabSelectionBinding.radioButtonAuthor.setChecked(enabled);
        enableAuthor(enabled);

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.AUTHOR);
        }
    }

    private void setCardFavourite(final boolean enabled) {
        this.fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setChecked(enabled);

        fragmentQuotationsTabSelectionBinding.buttonBrowseFavourites.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonBrowseFavourites, enabled);

        fragmentQuotationsTabSelectionBinding.buttonExport.setEnabled(enabled);
        QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonExport, enabled);

        fragmentQuotationsTabSelectionBinding.textViewInformationExternal.setEnabled(enabled);

        if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            fragmentQuotationsTabSelectionBinding.buttonBrowseFavourites.setEnabled(true);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonBrowseFavourites, true);

            fragmentQuotationsTabSelectionBinding.buttonExport.setEnabled(true);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonExport, true);

            fragmentQuotationsTabSelectionBinding.textViewInformationExternal.setEnabled(true);
        }

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.FAVOURITES);
        }
    }

    private void setCardSearch(final boolean enabled) {
        this.fragmentQuotationsTabSelectionBinding.radioButtonSearch.setChecked(enabled);
        enableSearch(enabled);

        if (enabled) {
            quotationsPreferences.setContentSelection(ContentSelection.SEARCH);
        }

        fragmentQuotationsTabSelectionBinding.buttonBrowseSearch.setEnabled(false);
    }

    protected void createListenerRadioAll() {
        final RadioButton radioButtonAll = this.fragmentQuotationsTabSelectionBinding.radioButtonAll;
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
        final RadioButton radioButtonAuthor = this.fragmentQuotationsTabSelectionBinding.radioButtonAuthor;
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
        final RadioButton radioButtonFavourites = this.fragmentQuotationsTabSelectionBinding.radioButtonFavourites;
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
        final RadioButton radioButtonSearch = this.fragmentQuotationsTabSelectionBinding.radioButtonSearch;
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
        fragmentQuotationsTabSelectionBinding.editTextResultsExclusion.setEnabled(enable);
        fragmentQuotationsTabSelectionBinding.textViewExclusionInfo.setEnabled(enable);
    }

    private void enableAuthor(final boolean enable) {
        fragmentQuotationsTabSelectionBinding.textViewQuotationCount.setEnabled(enable);
        fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setEnabled(enable);
        fragmentQuotationsTabSelectionBinding.spinnerAuthors.setEnabled(enable);

    }

    private void enableSearch(final boolean enable) {
        fragmentQuotationsTabSelectionBinding.editTextSearchText.setEnabled(enable);

        if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setEnabled(enable);

            fragmentQuotationsTabSelectionBinding.buttonBrowseSearch.setEnabled(true);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonBrowseSearch, true);
        } else {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setEnabled(false);

            fragmentQuotationsTabSelectionBinding.buttonBrowseSearch.setEnabled(false);
            QuotationsFilterFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonBrowseSearch, false);
        }

        if (quotationsPreferences.getContentSelectionSearchFavouritesOnly()) {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setChecked(true);
        } else {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setChecked(false);
        }
    }

    public void createListenerAuthorsQuotationCount() {
        fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String authorCount = fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.getSelectedItem().toString();

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
        fragmentQuotationsTabSelectionBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String author = fragmentQuotationsTabSelectionBinding.spinnerAuthors.getSelectedItem().toString();
                fragmentQuotationsTabSelectionBinding.radioButtonAuthor.setText(
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

    protected void createListenerBrowseFavouriteButton() {
        fragmentQuotationsTabSelectionBinding.buttonBrowseFavourites.setOnClickListener(v -> {
            BrowseFavouritesDialogFragment appearanceTextDialogQuotation = new BrowseFavouritesDialogFragment(
                    widgetId,
                    quoteUnquoteModel,
                    R.string.fragment_quotations_selection_dialog_browse_favourites);
            appearanceTextDialogQuotation.show(getParentFragmentManager(), "");
        });
    }

    protected void createListenerBrowseSearchButton() {
        fragmentQuotationsTabSelectionBinding.buttonBrowseSearch.setOnClickListener(v -> {
            BrowseSearchDialogFragment appearanceTextDialogQuotation = new BrowseSearchDialogFragment(
                    widgetId,
                    quoteUnquoteModel,
                    R.string.fragment_quotations_selection_dialog_browse_search);
            appearanceTextDialogQuotation.show(getParentFragmentManager(), "");
        });
    }

    protected void createListenerFavouriteButtonExport() {
        // invoke Storage Access Framework
        fragmentQuotationsTabSelectionBinding.buttonExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabSelectionBinding.buttonExport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "Favourites.csv");
                activityResultLauncher.launch(intent);
            }
        });
    }

    protected void createListenerSearchFavouritesOnly() {
        fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            quotationsPreferences.setContentSelectionSearchFavouritesOnly(isChecked);

            Editable priorText = fragmentQuotationsTabSelectionBinding.editTextSearchText.getText();
            fragmentQuotationsTabSelectionBinding.editTextSearchText.setText("");
            fragmentQuotationsTabSelectionBinding.editTextSearchText.append(priorText);
        });
    }

    protected final void handleExportResult() {
        // default: /storage/emulated/0/Download/Favourites.csv
        activityResultLauncher = registerForActivityResult(
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
                                    .csvExportFavourites(
                                            fileOutputStream,
                                            (ArrayList) quoteUnquoteModel.exportFavourites());

                            fileOutputStream.close();
                            parcelFileDescriptor.close();

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_quotations_selection_favourites_export_success),
                                    Toast.LENGTH_SHORT).show();
                        } catch (final IOException e) {
                            Timber.e(e.getMessage());
                        }
                    }

                    ConfigureActivity.launcherInvoked = false;
                });
    }
}
