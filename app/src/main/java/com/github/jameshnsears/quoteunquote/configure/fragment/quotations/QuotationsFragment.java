package com.github.jameshnsears.quoteunquote.configure.fragment.quotations;

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
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsBinding;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Keep
public class QuotationsFragment extends FragmentCommon {
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    public FragmentQuotationsBinding fragmentQuotationsBinding;
    @NonNull
    public CountDownLatch latchAllCount = new CountDownLatch(1);
    @NonNull
    public CountDownLatch latchAuthor = new CountDownLatch(1);
    @NonNull
    public CountDownLatch latchFavouriteCount = new CountDownLatch(1);
    @Nullable
    protected QuotationsPreferences quotationsPreferences;
    @Nullable
    private DisposableObserver<Integer> disposableObserver;
    @Nullable
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public QuotationsFragment() {
        // dark mode support
    }

    public QuotationsFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsFragment newInstance(final int widgetId) {
        final QuotationsFragment fragment = new QuotationsFragment(widgetId);
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
                    context.getString(R.string.fragment_quotations_search_no_results),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.quoteUnquoteModel = new QuoteUnquoteModel(this.getContext());
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @NonNull final ViewGroup container,
            @NonNull final Bundle savedInstanceState) {
        this.quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());

        this.fragmentQuotationsBinding = FragmentQuotationsBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ensureFragmentContentSearchConsistency(this.widgetId, this.getContext());

        this.fragmentQuotationsBinding = null;

        this.shutdown();
    }

    public void shutdown() {
        this.disposables.clear();
        this.disposables.dispose();

        if (this.disposableObserver != null) {
            this.disposableObserver.dispose();
        }
    }

    protected void setSearch() {
        this.setSearchObserver();

        final String editTextKeywords = this.quotationsPreferences.getContentSelectionSearch();

        if (editTextKeywords.length() > 0) {
            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("SEARCH", properties);
            this.fragmentQuotationsBinding.editTextSearchText.setText(editTextKeywords);
        }
    }

    protected void setSearchObserver() {
        this.disposableObserver = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                fragmentQuotationsBinding.radioButtonSearch.setText(
                        getResources().getString(R.string.fragment_quotations_search, value));
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

        RxTextView.textChanges(fragmentQuotationsBinding.editTextSearchText)
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

                        return quoteUnquoteModel.countQuotationWithSearchText(keywords);
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
        setInitialCounts();

        setFavouriteCount();
        setAllCount();
        setAddToPreviousAll();
        setAuthor();
        setSearch();

        createListenerRadioGroup();
        createListenerAddToPreviousAll();
        createListenerAuthor();
        createListenerFavouriteButtonExport();

        handleStorageAccessFrameworkResult();

        setSelection();
    }

    private void setInitialCounts() {
        fragmentQuotationsBinding.radioButtonAll.setText(
                getResources().getString(R.string.fragment_quotations_all,
                        0));

        fragmentQuotationsBinding.radioButtonAuthor.setText(
                getResources().getString(R.string.fragment_quotations_author,
                        0));

        fragmentQuotationsBinding.radioButtonFavourites.setText(
                getResources().getString(R.string.fragment_quotations_favourites,
                        0));

        fragmentQuotationsBinding.radioButtonSearch.setText(
                getResources().getString(R.string.fragment_quotations_search,
                        0));
    }

    protected void setAddToPreviousAll() {
        fragmentQuotationsBinding.switchAddToPreviousAll.setChecked(quotationsPreferences.getContentAddToPreviousAll());
    }

    public void setAllCount() {
        disposables.add(quoteUnquoteModel.countAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull final Integer value) {
                                fragmentQuotationsBinding.radioButtonAll.setText(
                                        getResources().getString(R.string.fragment_quotations_all, value));

                                synchronized (this) {
                                    latchAllCount.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setAuthor() {
        disposables.add(quoteUnquoteModel.authors()
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
                                fragmentQuotationsBinding.spinnerAuthors.setAdapter(adapter);

                                if ("".equals(quotationsPreferences.getContentSelectionAuthor())) {
                                    quotationsPreferences.setContentSelectionAuthor(authors.get(0));
                                }

                                setAuthorName();

                                synchronized (this) {
                                    latchAuthor.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setAuthorName() {
        final String authorPreference = quotationsPreferences.getContentSelectionAuthor();

        fragmentQuotationsBinding.spinnerAuthors.setSelection(
                quoteUnquoteModel.authorsIndex(authorPreference));

        fragmentQuotationsBinding.radioButtonAuthor.setText(
                getResources().getString(
                        R.string.fragment_quotations_author,
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
                                fragmentQuotationsBinding.radioButtonFavourites.setEnabled(true);
                                if (value == 0) {
                                    fragmentQuotationsBinding.radioButtonFavourites.setEnabled(false);

                                    fragmentQuotationsBinding.buttonExport.setEnabled(false);
                                    QuotationsFragment.this.makeButtonAlpha(fragmentQuotationsBinding.buttonExport, false);
                                    fragmentQuotationsBinding.textViewLocalStorageInstructions.setEnabled(false);

                                    // in case another widget instance changes favourites
                                    if (QuotationsFragment.this.quotationsPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)) {
                                        QuotationsFragment.this.quotationsPreferences.setContentSelection(ContentSelection.ALL);
                                    }
                                } else {
                                    fragmentQuotationsBinding.buttonExport.setEnabled(true);
                                    QuotationsFragment.this.makeButtonAlpha(fragmentQuotationsBinding.buttonExport, true);
                                    fragmentQuotationsBinding.textViewLocalStorageInstructions.setEnabled(true);
                                }

                                fragmentQuotationsBinding.radioButtonFavourites.setText(
                                        getResources().getString(R.string.fragment_quotations_favourites, value));

                                synchronized (this) {
                                    latchFavouriteCount.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setSelection() {
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
        fragmentQuotationsBinding.radioButtonAll.setChecked(true);
    }

    private void setSelectionAuthor() {
        fragmentQuotationsBinding.radioButtonAuthor.setChecked(true);
        enableAuthor(true);
    }

    private void setSelectionFavourites() {
        fragmentQuotationsBinding.radioButtonFavourites.setChecked(true);
    }

    private void setSelectionSearch() {
        fragmentQuotationsBinding.radioButtonSearch.setChecked(true);
        enableSearch(true);

        fragmentQuotationsBinding.radioButtonSearch.requestFocus();

        final String searchText = quotationsPreferences.getContentSelectionSearch();

        if (!searchText.equals("") && !quotationsPreferences.getContentSelectionSearch().equals(searchText)) {
            quotationsPreferences.setContentSelectionSearch(searchText);

            final EditText editTextKeywordsSearch = fragmentQuotationsBinding.editTextSearchText;
            editTextKeywordsSearch.setText(searchText);
        }
    }

    protected void createListenerRadioGroup() {
        final RadioGroup radioGroupContent = fragmentQuotationsBinding.radioGroupContent;
        radioGroupContent.setOnCheckedChangeListener((group, checkedId) -> {

            enableAuthor(false);
            enableSearch(false);

            if (checkedId == fragmentQuotationsBinding.radioButtonAll.getId()) {
                quotationsPreferences.setContentSelection(ContentSelection.ALL);
            }

            if (checkedId == fragmentQuotationsBinding.radioButtonAuthor.getId()) {
                enableAuthor(true);
                quotationsPreferences.setContentSelection(ContentSelection.AUTHOR);
            }

            if (checkedId == fragmentQuotationsBinding.radioButtonFavourites.getId()) {
                quotationsPreferences.setContentSelection(ContentSelection.FAVOURITES);
            }

            if (checkedId == fragmentQuotationsBinding.radioButtonSearch.getId()) {
                enableSearch(true);
                quotationsPreferences.setContentSelection(ContentSelection.SEARCH);
            }
        });
    }

    private void enableAuthor(final boolean enable) {
        fragmentQuotationsBinding.spinnerAuthors.setEnabled(enable);
    }

    public void makeButtonAlpha(@NonNull final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    private void enableSearch(final boolean enable) {
        fragmentQuotationsBinding.editTextSearchText.setEnabled(enable);
    }

    protected void createListenerAuthor() {
        fragmentQuotationsBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String author = fragmentQuotationsBinding.spinnerAuthors.getSelectedItem().toString();
                fragmentQuotationsBinding.radioButtonAuthor.setText(
                        getResources().getString(R.string.fragment_quotations_author,
                                quoteUnquoteModel.countAuthorQuotations(author)));

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
        fragmentQuotationsBinding.switchAddToPreviousAll.setOnCheckedChangeListener((buttonView, isChecked) ->
                quotationsPreferences.setContentAddToPreviousAll(isChecked)
        );
    }

    protected void createListenerFavouriteButtonExport() {
        // invoke Storage Access Framework
        fragmentQuotationsBinding.buttonExport.setOnClickListener(v -> {
            if (fragmentQuotationsBinding.buttonExport.isEnabled()) {
                ConfigureActivity.safCalled = true;

                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "Favourites.txt");
                activityResultLauncher.launch(intent);
            }
        });
    }

    protected final void handleStorageAccessFrameworkResult() {
        // default: /storage/emulated/0/Download/Favourites.txt
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

                            final ArrayList<String> exportableFavourites = (ArrayList) this.quoteUnquoteModel.exportFavourites();
                            Collections.reverse(exportableFavourites);

                            int favouriteIndex = 1;
                            for (final String exportFavourite : exportableFavourites) {
                                final String exportableString = "" + favouriteIndex + "\n" + exportFavourite;
                                fileOutputStream.write(exportableString.getBytes());
                                favouriteIndex++;
                            }

                            fileOutputStream.close();
                            parcelFileDescriptor.close();

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_quotations_favourites_export_success),
                                    Toast.LENGTH_SHORT).show();
                        } catch (final IOException e) {
                            Timber.e(e.getMessage());
                        }
                    }

                    ConfigureActivity.safCalled = false;
                });
    }
}
