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

    public QuotationsFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsFragment newInstance(int widgetId) {
        QuotationsFragment fragment = new QuotationsFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    public static void ensureFragmentContentSearchConsistency(
            int widgetId,
            @NonNull final Context context
    ) {
        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, context);

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
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(getContext());
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        quotationsPreferences = new QuotationsPreferences(widgetId, getContext());

        fragmentQuotationsBinding = FragmentQuotationsBinding.inflate(getLayoutInflater());
        return fragmentQuotationsBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        QuotationsFragment.ensureFragmentContentSearchConsistency(widgetId, getContext());

        fragmentQuotationsBinding = null;

        shutdown();
    }

    public void shutdown() {
        disposables.clear();
        disposables.dispose();

        if (disposableObserver != null) {
            disposableObserver.dispose();
        }
    }

    protected void setSearch() {
        setSearchObserver();

        String editTextKeywords = quotationsPreferences.getContentSelectionSearch();

        if (editTextKeywords.length() > 0) {
            ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("SEARCH", properties);
            fragmentQuotationsBinding.editTextSearchText.setText(editTextKeywords);
        }
    }

    protected void setSearchObserver() {
        disposableObserver = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull Integer value) {
                QuotationsFragment.this.fragmentQuotationsBinding.radioButtonSearch.setText(
                        QuotationsFragment.this.getResources().getString(R.string.fragment_quotations_search, value));
                QuotationsFragment.this.quotationsPreferences.setContentSelectionSearchCount(value);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                Timber.d("onError=%s", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        };

        RxTextView.textChanges(this.fragmentQuotationsBinding.editTextSearchText)
                .debounce(25, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    String keywords = charSequence.toString();

                    if (!keywords.equals("")) {
                        Timber.d("%s", keywords);

                        // remove any prior, different, search results in the history
                        if (!keywords.equals(this.quotationsPreferences.getContentSelectionSearch())) {
                            this.quoteUnquoteModel.resetPrevious(widgetId, ContentSelection.SEARCH);
                        }

                        this.quotationsPreferences.setContentSelectionSearch(keywords);

                        return this.quoteUnquoteModel.countQuotationWithSearchText(keywords);
                    } else {
                        this.quotationsPreferences.setContentSelectionSearch("");
                        return 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.disposableObserver);
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {
        this.setInitialCounts();

        this.setFavouriteCount();
        this.setAllCount();
        this.setAddToPreviousAll();
        this.setAuthor();
        this.setSearch();

        this.createListenerRadioGroup();
        this.createListenerAddToPreviousAll();
        this.createListenerAuthor();
        this.createListenerFavouriteButtonExport();

        this.handleStorageAccessFrameworkResult();

        this.setSelection();
    }

    private void setInitialCounts() {
        QuotationsFragment.this.fragmentQuotationsBinding.radioButtonAll.setText(
                QuotationsFragment.this.getResources().getString(R.string.fragment_quotations_all,
                        0));

        this.fragmentQuotationsBinding.radioButtonAuthor.setText(
                this.getResources().getString(R.string.fragment_quotations_author,
                        0));

        QuotationsFragment.this.fragmentQuotationsBinding.radioButtonFavourites.setText(
                QuotationsFragment.this.getResources().getString(R.string.fragment_quotations_favourites,
                        0));

        this.fragmentQuotationsBinding.radioButtonSearch.setText(
                this.getResources().getString(R.string.fragment_quotations_search,
                        0));
    }

    protected void setAddToPreviousAll() {
        this.fragmentQuotationsBinding.switchAddToPreviousAll.setChecked(this.quotationsPreferences.getContentAddToPreviousAll());
    }

    public void setAllCount() {
        this.disposables.add(this.quoteUnquoteModel.countAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull Integer value) {
                                QuotationsFragment.this.fragmentQuotationsBinding.radioButtonAll.setText(
                                        QuotationsFragment.this.getResources().getString(R.string.fragment_quotations_all, value));

                                synchronized (this) {
                                    QuotationsFragment.this.latchAllCount.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setAuthor() {
        this.disposables.add(this.quoteUnquoteModel.authors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<List<AuthorPOJO>>() {
                            @Override
                            public void onSuccess(@NonNull List<AuthorPOJO> authorPOJOList) {
                                List<String> authors
                                        = QuotationsFragment.this.quoteUnquoteModel.authorsSorted(authorPOJOList);

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        QuotationsFragment.this.getContext(),
                                        R.layout.spinner_item,
                                        authors);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                QuotationsFragment.this.fragmentQuotationsBinding.spinnerAuthors.setAdapter(adapter);

                                if ("".equals(QuotationsFragment.this.quotationsPreferences.getContentSelectionAuthor())) {
                                    QuotationsFragment.this.quotationsPreferences.setContentSelectionAuthor(authors.get(0));
                                }

                                QuotationsFragment.this.setAuthorName();

                                synchronized (this) {
                                    QuotationsFragment.this.latchAuthor.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setAuthorName() {
        String authorPreference = this.quotationsPreferences.getContentSelectionAuthor();

        this.fragmentQuotationsBinding.spinnerAuthors.setSelection(
                this.quoteUnquoteModel.authorsIndex(authorPreference));

        this.fragmentQuotationsBinding.radioButtonAuthor.setText(
                this.getResources().getString(
                        R.string.fragment_quotations_author,
                        this.quoteUnquoteModel.countAuthorQuotations(authorPreference)));

    }

    public void setFavouriteCount() {
        this.disposables.add(this.quoteUnquoteModel.countFavourites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull Integer value) {
                                QuotationsFragment.this.fragmentQuotationsBinding.radioButtonFavourites.setEnabled(true);
                                if (value == 0) {
                                    QuotationsFragment.this.fragmentQuotationsBinding.radioButtonFavourites.setEnabled(false);

                                    QuotationsFragment.this.fragmentQuotationsBinding.buttonExport.setEnabled(false);
                                    makeButtonAlpha(QuotationsFragment.this.fragmentQuotationsBinding.buttonExport, false);
                                    QuotationsFragment.this.fragmentQuotationsBinding.textViewLocalStorageInstructions.setEnabled(false);

                                    // in case another widget instance changes favourites
                                    if (quotationsPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)) {
                                        quotationsPreferences.setContentSelection(ContentSelection.ALL);
                                    }
                                } else {
                                    QuotationsFragment.this.fragmentQuotationsBinding.buttonExport.setEnabled(true);
                                    makeButtonAlpha(QuotationsFragment.this.fragmentQuotationsBinding.buttonExport, true);
                                    QuotationsFragment.this.fragmentQuotationsBinding.textViewLocalStorageInstructions.setEnabled(true);
                                }

                                QuotationsFragment.this.fragmentQuotationsBinding.radioButtonFavourites.setText(
                                        QuotationsFragment.this.getResources().getString(R.string.fragment_quotations_favourites, value));

                                synchronized (this) {
                                    QuotationsFragment.this.latchFavouriteCount.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setSelection() {
        this.enableAuthor(false);
        this.enableSearch(false);

        switch (this.quotationsPreferences.getContentSelection()) {
            case ALL:
                this.setSelectionAll();
                break;
            case AUTHOR:
                this.setSelectionAuthor();
                break;
            case FAVOURITES:
                this.setSelectionFavourites();
                break;
            case SEARCH:
                this.setSelectionSearch();
                break;
            default:
                Timber.e("unknown switch");
                break;
        }
    }

    private void setSelectionAll() {
        this.fragmentQuotationsBinding.radioButtonAll.setChecked(true);
    }

    private void setSelectionAuthor() {
        this.fragmentQuotationsBinding.radioButtonAuthor.setChecked(true);
        this.enableAuthor(true);
    }

    private void setSelectionFavourites() {
        this.fragmentQuotationsBinding.radioButtonFavourites.setChecked(true);
    }

    private void setSelectionSearch() {
        this.fragmentQuotationsBinding.radioButtonSearch.setChecked(true);
        this.enableSearch(true);

        this.fragmentQuotationsBinding.radioButtonSearch.requestFocus();

        String searchText = this.quotationsPreferences.getContentSelectionSearch();

        if (!searchText.equals("") && !this.quotationsPreferences.getContentSelectionSearch().equals(searchText)) {
            this.quotationsPreferences.setContentSelectionSearch(searchText);

            EditText editTextKeywordsSearch = this.fragmentQuotationsBinding.editTextSearchText;
            editTextKeywordsSearch.setText(searchText);
        }
    }

    protected void createListenerRadioGroup() {
        RadioGroup radioGroupContent = this.fragmentQuotationsBinding.radioGroupContent;
        radioGroupContent.setOnCheckedChangeListener((group, checkedId) -> {

            this.enableAuthor(false);
            this.enableSearch(false);

            if (checkedId == this.fragmentQuotationsBinding.radioButtonAll.getId()) {
                this.quotationsPreferences.setContentSelection(ContentSelection.ALL);
            }

            if (checkedId == this.fragmentQuotationsBinding.radioButtonAuthor.getId()) {
                this.enableAuthor(true);
                this.quotationsPreferences.setContentSelection(ContentSelection.AUTHOR);
            }

            if (checkedId == this.fragmentQuotationsBinding.radioButtonFavourites.getId()) {
                this.quotationsPreferences.setContentSelection(ContentSelection.FAVOURITES);
            }

            if (checkedId == this.fragmentQuotationsBinding.radioButtonSearch.getId()) {
                this.enableSearch(true);
                this.quotationsPreferences.setContentSelection(ContentSelection.SEARCH);
            }
        });
    }

    private void enableAuthor(boolean enable) {
        this.fragmentQuotationsBinding.spinnerAuthors.setEnabled(enable);
    }

    public void makeButtonAlpha(@NonNull Button button, boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    private void enableSearch(boolean enable) {
        this.fragmentQuotationsBinding.editTextSearchText.setEnabled(enable);
    }

    protected void createListenerAuthor() {
        this.fragmentQuotationsBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long selectedItemId) {
                String author = QuotationsFragment.this.fragmentQuotationsBinding.spinnerAuthors.getSelectedItem().toString();
                QuotationsFragment.this.fragmentQuotationsBinding.radioButtonAuthor.setText(
                        QuotationsFragment.this.getResources().getString(R.string.fragment_quotations_author,
                                QuotationsFragment.this.quoteUnquoteModel.countAuthorQuotations(author)));

                if (!QuotationsFragment.this.quotationsPreferences.getContentSelectionAuthor().equals(author)) {
                    Timber.d("author=%s", author);
                    ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                    properties.put("Author", author);
                    AuditEventHelper.auditEvent("AUTHOR", properties);

                    QuotationsFragment.this.quotationsPreferences.setContentSelectionAuthor(author);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    protected void createListenerAddToPreviousAll() {
        this.fragmentQuotationsBinding.switchAddToPreviousAll.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.quotationsPreferences.setContentAddToPreviousAll(isChecked)
        );
    }

    protected void createListenerFavouriteButtonExport() {
        // invoke Storage Access Framework
        this.fragmentQuotationsBinding.buttonExport.setOnClickListener(v -> {
            if (this.fragmentQuotationsBinding.buttonExport.isEnabled()) {
                ConfigureActivity.exportCalled = true;

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "Favourites.txt");
                this.activityResultLauncher.launch(intent);
            }
        });
    }

    protected final void handleStorageAccessFrameworkResult() {
        // default: /storage/emulated/0/Download/Favourites.txt
        this.activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {

                        try {
                            ParcelFileDescriptor parcelFileDescriptor
                                    = this.getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "w");
                            FileOutputStream fileOutputStream
                                    = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                            ArrayList<String> exportableFavourites = (ArrayList) quoteUnquoteModel.exportFavourites();
                            Collections.reverse(exportableFavourites);

                            int favouriteIndex = 1;
                            for (String exportFavourite : exportableFavourites) {
                                String exportableString = "" + favouriteIndex + "\n" + exportFavourite;
                                fileOutputStream.write(exportableString.getBytes());
                                favouriteIndex++;
                            }

                            fileOutputStream.close();
                            parcelFileDescriptor.close();

                            Toast.makeText(
                                    this.getContext(),
                                    this.getContext().getString(R.string.fragment_archive_backup_success),
                                    Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Timber.e(e.getMessage());
                        }
                    }

                    ConfigureActivity.exportCalled = false;
                });
    }
}
