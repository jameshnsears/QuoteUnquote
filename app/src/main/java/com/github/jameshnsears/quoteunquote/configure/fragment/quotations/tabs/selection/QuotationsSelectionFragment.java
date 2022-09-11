package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.selection;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabSelectionBinding;
import com.github.jameshnsears.quoteunquote.utils.CSVHelper;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
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
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Keep
public class QuotationsSelectionFragment extends FragmentCommon {
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    public FragmentQuotationsTabSelectionBinding fragmentQuotationsTabSelectionBinding;
    @Nullable
    public QuotationsPreferences quotationsPreferences;
    @Nullable
    private DisposableObserver<Integer> disposableObserver;
    @Nullable
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public QuotationsSelectionFragment() {
        // dark mode support
    }

    public QuotationsSelectionFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsSelectionFragment newInstance(final int widgetId) {
        final QuotationsSelectionFragment fragment = new QuotationsSelectionFragment(widgetId);
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

        if (this.disposableObserver != null) {
            this.disposableObserver.dispose();
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
        }
    }

    protected void setSearchObserver() {
        this.disposableObserver = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                fragmentQuotationsTabSelectionBinding.radioButtonSearch.setText(
                        getResources().getString(R.string.fragment_quotations_selection_search, value));
                quotationsPreferences.setContentSelectionSearchCount(value);
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
                .subscribe(disposableObserver);
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        initUi();

        createListenerRadioGroup();

        createListenerAddToPreviousAll();

        createListenerAuthorsQuotationCount();
        createListenerAuthor();

        createListenerFavouriteButtonExport();

        createListenerSearchFavouritesOnly();

        handleExportResult();

        setSelection();
    }

    public void initUi() {
        setInitialCounts();

        setAllCount();
        setAddToPreviousAll();

        setAuthorsQuotationCount();

        setFavouriteCount();

        setSearch();

        setSelection();
    }

    private void setInitialCounts() {
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

    public void setAddToPreviousAll() {
        fragmentQuotationsTabSelectionBinding.switchAddToPreviousAll.setChecked(quotationsPreferences.getContentAddToPreviousAll());
    }

    public void setAllCount() {
        disposables.add(quoteUnquoteModel.countAll()
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

    public void setAuthorsQuotationCount() {
        disposables.add(quoteUnquoteModel.authorsQuotationCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<List<Integer>>() {
                            @Override
                            public void onSuccess(@NonNull final List<Integer> authorCountList) {
                                final ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                                        getContext(),
                                        R.layout.spinner_item,
                                        authorCountList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setAdapter(adapter);

                                fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setSelection(
                                        authorCountList.indexOf(quotationsPreferences.getContentSelectionAuthorCount()));
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
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

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
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

                                    fragmentQuotationsTabSelectionBinding.buttonExport.setEnabled(false);
                                    QuotationsSelectionFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonExport, false);
                                    fragmentQuotationsTabSelectionBinding.textViewLocalStorageInstructions.setEnabled(false);

                                    // in case another widget instance changes favourites
                                    if (QuotationsSelectionFragment.this.quotationsPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)) {
                                        QuotationsSelectionFragment.this.quotationsPreferences.setContentSelection(ContentSelection.ALL);
                                    }
                                } else {
                                    fragmentQuotationsTabSelectionBinding.buttonExport.setEnabled(true);
                                    QuotationsSelectionFragment.this.makeButtonAlpha(fragmentQuotationsTabSelectionBinding.buttonExport, true);
                                    fragmentQuotationsTabSelectionBinding.textViewLocalStorageInstructions.setEnabled(true);
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
    }

    private void setSelectionAuthor() {
        fragmentQuotationsTabSelectionBinding.radioButtonAuthor.setChecked(true);
        enableAuthor(true);
    }

    private void setSelectionFavourites() {
        fragmentQuotationsTabSelectionBinding.radioButtonFavourites.setChecked(true);
    }

    private void setSelectionSearch() {
        fragmentQuotationsTabSelectionBinding.radioButtonSearch.setChecked(true);
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

    protected void createListenerRadioGroup() {
        final RadioGroup radioGroupContent = fragmentQuotationsTabSelectionBinding.radioGroupContent;
        radioGroupContent.setOnCheckedChangeListener((group, checkedId) -> {

            enableAuthor(false);
            enableSearch(false);

            if (checkedId == fragmentQuotationsTabSelectionBinding.radioButtonAll.getId()) {
                quotationsPreferences.setContentSelection(ContentSelection.ALL);
            }

            if (checkedId == fragmentQuotationsTabSelectionBinding.radioButtonAuthor.getId()) {
                enableAuthor(true);
                quotationsPreferences.setContentSelection(ContentSelection.AUTHOR);
            }

            if (checkedId == fragmentQuotationsTabSelectionBinding.radioButtonFavourites.getId()) {
                quotationsPreferences.setContentSelection(ContentSelection.FAVOURITES);
            }

            if (checkedId == fragmentQuotationsTabSelectionBinding.radioButtonSearch.getId()) {
                enableSearch(true);
                quotationsPreferences.setContentSelection(ContentSelection.SEARCH);
            }
        });
    }

    private void enableAuthor(final boolean enable) {
        fragmentQuotationsTabSelectionBinding.textViewQuotationCount.setEnabled(enable);
        fragmentQuotationsTabSelectionBinding.spinnerAuthorsCount.setEnabled(enable);
        fragmentQuotationsTabSelectionBinding.spinnerAuthors.setEnabled(enable);

    }

    public void makeButtonAlpha(@NonNull final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    private void enableSearch(final boolean enable) {
        fragmentQuotationsTabSelectionBinding.editTextSearchText.setEnabled(enable);

        if (quoteUnquoteModel.countFavouritesWithoutRx() > 0) {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setEnabled(enable);
        } else {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setEnabled(false);
        }

        if (quotationsPreferences.getContentSelectionSearchFavouritesOnly()) {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setChecked(true);
        } else {
            fragmentQuotationsTabSelectionBinding.switchSearchFavouritesOnly.setChecked(false);
        }
    }

    protected void createListenerAuthorsQuotationCount() {
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

    protected void createListenerAddToPreviousAll() {
        fragmentQuotationsTabSelectionBinding.switchAddToPreviousAll.setOnCheckedChangeListener((buttonView, isChecked) ->
                quotationsPreferences.setContentAddToPreviousAll(isChecked)
        );
    }

    protected void createListenerFavouriteButtonExport() {
        // invoke Storage Access Framework
        fragmentQuotationsTabSelectionBinding.buttonExport.setOnClickListener(v -> {
            if (fragmentQuotationsTabSelectionBinding.buttonExport.isEnabled()) {
                ConfigureActivity.safCalled = true;

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

                            new CSVHelper()
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

                    ConfigureActivity.safCalled = false;
                });
    }
}
