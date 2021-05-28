package com.github.jameshnsears.quoteunquote.configure.fragment.content;

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

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceReceive;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceSend;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.databinding.FragmentContentBinding;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;
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
public class ContentFragment extends FragmentCommon {
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @Nullable
    public FragmentContentBinding fragmentContentBinding;
    @NonNull
    public CountDownLatch latchAllCount = new CountDownLatch(1);
    @NonNull
    public CountDownLatch latchAuthor = new CountDownLatch(1);
    @NonNull
    public CountDownLatch latchFavouriteCount = new CountDownLatch(1);
    @Nullable
    protected ContentPreferences contentPreferences;
    @Nullable
    protected ContentCloud contentCloud;
    @Nullable
    private DisposableObserver<Integer> disposableObserver;
    @Nullable
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public ContentFragment() {
        // dark mode support
    }

    public ContentFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static ContentFragment newInstance(int widgetId) {
        ContentFragment fragment = new ContentFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    public static void ensureFragmentContentSearchConsistency(
            int widgetId,
            @NonNull final Context context
    ) {
        ContentPreferences contentPreferences = new ContentPreferences(widgetId, context);

        if (contentPreferences.getContentSelection() == ContentSelection.SEARCH
                && contentPreferences.getContentSelectionSearchCount() == 0) {
            contentPreferences.setContentSelection(ContentSelection.ALL);
            ToastHelper.makeToast(context,
                    context.getString(R.string.fragment_content_text_no_search_results), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.contentCloud = new ContentCloud();
        this.quoteUnquoteModel = new QuoteUnquoteModel(this.getContext());
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        Intent intent = new Intent(this.getContext(), CloudServiceReceive.class);
        this.getContext().bindService(intent, this.contentCloud.serviceConnection, Context.BIND_AUTO_CREATE);

        this.contentPreferences = new ContentPreferences(widgetId, getContext());

        this.fragmentContentBinding = FragmentContentBinding.inflate(this.getLayoutInflater());
        return this.fragmentContentBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ContentFragment.ensureFragmentContentSearchConsistency(this.widgetId, this.getContext());

        this.fragmentContentBinding = null;

        this.shutdown();

        this.getContext().unbindService(this.contentCloud.serviceConnection);
        this.contentCloud.isServiceReceiveBound = false;
    }

    public void shutdown() {
        this.disposables.clear();
        this.disposables.dispose();

        if (this.disposableObserver != null) {
            this.disposableObserver.dispose();
        }
    }

    protected void setFavouriteLocalCode() {
        if ("".equals(this.contentPreferences.getContentFavouritesLocalCode())) {
            // possible that user wiped storage via OS settings
            this.contentPreferences.setContentFavouritesLocalCode(CloudFavouritesHelper.getLocalCode());
        }

        this.fragmentContentBinding.textViewLocalCodeValue.setText(this.contentPreferences.getContentFavouritesLocalCode());
    }

    protected void setSearch() {
        this.setSearchObserver();

        String editTextKeywords = this.contentPreferences.getContentSelectionSearch();

        if (editTextKeywords.length() > 0) {
            ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("SEARCH", properties);
            this.fragmentContentBinding.editTextSearchText.setText(editTextKeywords);
        }
    }

    protected void setSearchObserver() {
        this.disposableObserver = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull Integer value) {
                ContentFragment.this.fragmentContentBinding.radioButtonSearch.setText(
                        ContentFragment.this.getResources().getString(R.string.fragment_content_text, value));
                ContentFragment.this.contentPreferences.setContentSelectionSearchCount(value);
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

        RxTextView.textChanges(this.fragmentContentBinding.editTextSearchText)
                .debounce(25, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    String keywords = charSequence.toString();

                    if (!keywords.equals("")) {
                        Timber.d("%s", keywords);

                        // remove any prior, different, search results in the history
                        if (!keywords.equals(this.contentPreferences.getContentSelectionSearch())) {
                            this.quoteUnquoteModel.resetPrevious(widgetId, ContentSelection.SEARCH);
                        }

                        this.contentPreferences.setContentSelectionSearch(keywords);

                        return this.quoteUnquoteModel.countQuotationWithSearchText(keywords);
                    } else {
                        this.contentPreferences.setContentSelectionSearch("");
                        return 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.disposableObserver);
    }

    @Override
    public void onViewCreated(
            @NonNull View view, Bundle savedInstanceState) {
        this.setInitialCounts();

        this.setFavouriteCount();
        this.setAllCount();
        this.setAuthor();
        this.setSearch();

        this.createListenerRadioGroup();
        this.createListenerAuthor();
        this.createListenerFavouriteButtonExport();
        this.createListenerFavouriteButtonSend();
        this.createListenerFavouriteButtonReceive();

        this.handleStorageAccessFrameworkResult();

        this.setFavouriteLocalCode();
        this.setSelection();
    }

    private void setInitialCounts() {
        ContentFragment.this.fragmentContentBinding.radioButtonAll.setText(
                ContentFragment.this.getResources().getString(R.string.fragment_content_all,
                        0));

        this.fragmentContentBinding.radioButtonAuthor.setText(
                this.getResources().getString(R.string.fragment_content_author,
                        0));

        ContentFragment.this.fragmentContentBinding.radioButtonFavourites.setText(
                ContentFragment.this.getResources().getString(R.string.fragment_content_favourites,
                        0));

        this.fragmentContentBinding.radioButtonSearch.setText(
                this.getResources().getString(R.string.fragment_content_text,
                        0));
    }

    public void setAllCount() {
        this.disposables.add(this.quoteUnquoteModel.countAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull Integer value) {
                                ContentFragment.this.fragmentContentBinding.radioButtonAll.setText(
                                        ContentFragment.this.getResources().getString(R.string.fragment_content_all, value));

                                synchronized (this) {
                                    ContentFragment.this.latchAllCount.countDown();
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
                                        = ContentFragment.this.quoteUnquoteModel.authorsSorted(authorPOJOList);

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ContentFragment.this.getContext(),
                                        R.layout.spinner_item,
                                        authors);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                ContentFragment.this.fragmentContentBinding.spinnerAuthors.setAdapter(adapter);

                                if ("".equals(ContentFragment.this.contentPreferences.getContentSelectionAuthor())) {
                                    ContentFragment.this.contentPreferences.setContentSelectionAuthor(authors.get(0));
                                }

                                ContentFragment.this.setAuthorName();

                                synchronized (this) {
                                    ContentFragment.this.latchAuthor.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.d("onError=%s", throwable.getMessage());
                            }
                        }));
    }

    protected void setAuthorName() {
        String authorPreference = this.contentPreferences.getContentSelectionAuthor();

        this.fragmentContentBinding.spinnerAuthors.setSelection(
                this.quoteUnquoteModel.authorsIndex(authorPreference));

        this.fragmentContentBinding.radioButtonAuthor.setText(
                this.getResources().getString(
                        R.string.fragment_content_author,
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
                                ContentFragment.this.fragmentContentBinding.radioButtonFavourites.setEnabled(true);
                                if (value == 0) {
                                    ContentFragment.this.fragmentContentBinding.radioButtonFavourites.setEnabled(false);

                                    ContentFragment.this.fragmentContentBinding.buttonExport.setEnabled(false);
                                    ContentFragment.this.makeButtonAlpha(ContentFragment.this.fragmentContentBinding.buttonExport, false);

                                    ContentFragment.this.fragmentContentBinding.buttonSend.setEnabled(false);
                                    ContentFragment.this.makeButtonAlpha(ContentFragment.this.fragmentContentBinding.buttonSend, false);

                                    // in case another widget instance changes favourites
                                    if (contentPreferences.getContentSelection().equals(ContentSelection.FAVOURITES)) {
                                        contentPreferences.setContentSelection(ContentSelection.ALL);
                                    }
                                } else {
                                    ContentFragment.this.fragmentContentBinding.buttonExport.setEnabled(true);
                                    ContentFragment.this.makeButtonAlpha(ContentFragment.this.fragmentContentBinding.buttonExport, true);

                                    ContentFragment.this.fragmentContentBinding.buttonSend.setEnabled(true);
                                    ContentFragment.this.makeButtonAlpha(ContentFragment.this.fragmentContentBinding.buttonSend, true);
                                }

                                ContentFragment.this.fragmentContentBinding.radioButtonFavourites.setText(
                                        ContentFragment.this.getResources().getString(R.string.fragment_content_favourites, value));

                                synchronized (this) {
                                    ContentFragment.this.latchFavouriteCount.countDown();
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
        this.enableFavourites(false);
        this.enableSearch(false);

        switch (this.contentPreferences.getContentSelection()) {
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
        this.fragmentContentBinding.radioButtonAll.setChecked(true);
    }

    private void setSelectionAuthor() {
        this.fragmentContentBinding.radioButtonAuthor.setChecked(true);
        this.enableAuthor(true);
    }

    private void setSelectionFavourites() {
        this.fragmentContentBinding.radioButtonFavourites.setChecked(true);
        this.enableFavourites(true);
    }

    private void setSelectionSearch() {
        this.fragmentContentBinding.radioButtonSearch.setChecked(true);
        this.enableSearch(true);

        this.fragmentContentBinding.radioButtonSearch.requestFocus();

        String searchText = this.contentPreferences.getContentSelectionSearch();

        if (!searchText.equals("") && !this.contentPreferences.getContentSelectionSearch().equals(searchText)) {
            this.contentPreferences.setContentSelectionSearch(searchText);

            EditText editTextKeywordsSearch = this.fragmentContentBinding.editTextSearchText;
            editTextKeywordsSearch.setText(searchText);
        }
    }

    protected void createListenerRadioGroup() {
        RadioGroup radioGroupContent = this.fragmentContentBinding.radioGroupContent;
        radioGroupContent.setOnCheckedChangeListener((group, checkedId) -> {

            this.enableAuthor(false);
            this.enableFavourites(false);
            this.enableSearch(false);

            if (checkedId == this.fragmentContentBinding.radioButtonAll.getId()) {
                this.contentPreferences.setContentSelection(ContentSelection.ALL);
            }

            if (checkedId == this.fragmentContentBinding.radioButtonAuthor.getId()) {
                this.enableAuthor(true);
                this.contentPreferences.setContentSelection(ContentSelection.AUTHOR);
            }

            if (checkedId == this.fragmentContentBinding.radioButtonFavourites.getId()) {
                this.enableFavourites(true);
                this.contentPreferences.setContentSelection(ContentSelection.FAVOURITES);
            }

            if (checkedId == this.fragmentContentBinding.radioButtonSearch.getId()) {
                this.enableSearch(true);
                this.contentPreferences.setContentSelection(ContentSelection.SEARCH);
            }
        });
    }

    private void enableAuthor(boolean enable) {
        this.fragmentContentBinding.spinnerAuthors.setEnabled(enable);
    }

    private void enableFavourites(boolean enable) {
        this.fragmentContentBinding.textViewLocalStorageInstructions.setEnabled(enable);

        this.fragmentContentBinding.textViewLocalCodeValue.setEnabled(enable);
    }

    private void makeButtonAlpha(@NonNull Button button, boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    private void enableSearch(boolean enable) {
        this.fragmentContentBinding.editTextSearchText.setEnabled(enable);
    }

    protected void createListenerAuthor() {
        this.fragmentContentBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long selectedItemId) {
                String author = ContentFragment.this.fragmentContentBinding.spinnerAuthors.getSelectedItem().toString();
                ContentFragment.this.fragmentContentBinding.radioButtonAuthor.setText(
                        ContentFragment.this.getResources().getString(R.string.fragment_content_author,
                                ContentFragment.this.quoteUnquoteModel.countAuthorQuotations(author)));

                if (!ContentFragment.this.contentPreferences.getContentSelectionAuthor().equals(author)) {
                    Timber.d("author=%s", author);
                    ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                    properties.put("Author", author);
                    AuditEventHelper.auditEvent("AUTHOR", properties);

                    ContentFragment.this.contentPreferences.setContentSelectionAuthor(author);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    protected void createListenerFavouriteButtonExport() {
        // invoke Storage Access Framework
        this.fragmentContentBinding.buttonExport.setOnClickListener(v -> {
            if (this.fragmentContentBinding.buttonExport.isEnabled()) {
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

                            ArrayList<String> exportableFavourites = quoteUnquoteModel.exportFavourites();
                            Collections.reverse(exportableFavourites);

                            int favouriteIndex = 1;
                            for (String exportFavourite : exportableFavourites) {
                                String exportableString = "" + favouriteIndex + "\n" + exportFavourite;
                                fileOutputStream.write(exportableString.getBytes());
                                favouriteIndex++;
                            }

                            fileOutputStream.close();
                            parcelFileDescriptor.close();

                            ToastHelper.makeToast(
                                    this.getContext(),
                                    this.getContext().getString(R.string.fragment_content_favourites_export_success),
                                    Toast.LENGTH_SHORT);
                        } catch (IOException e) {
                            Timber.e(e.getMessage());
                        }
                    }
                });
    }

    protected void createListenerFavouriteButtonSend() {
        this.fragmentContentBinding.buttonSend.setOnClickListener(v -> {
            if (this.fragmentContentBinding.buttonSend.isEnabled()) {

                Intent serviceIntent = new Intent(this.getContext(), CloudServiceSend.class);
                serviceIntent.putExtra("savePayload", this.quoteUnquoteModel.getFavouritesToSend(this.getContext()));
                serviceIntent.putExtra(
                        "localCodeValue", this.fragmentContentBinding.textViewLocalCodeValue.getText().toString());

                this.getContext().startService(serviceIntent);

            }
        });
    }

    protected void createListenerFavouriteButtonReceive() {
        this.fragmentContentBinding.buttonReceive.setOnClickListener(v -> {
            if (this.fragmentContentBinding.buttonReceive.isEnabled()) {
                Timber.d("remoteCode=%s", this.fragmentContentBinding.editTextRemoteCodeValue.getText().toString());

                // correct length?
                if (this.fragmentContentBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
                    ToastHelper.makeToast(
                            this.getContext(), this.getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_SHORT);
                    return;
                }

                // crc wrong?
                if (!CloudFavouritesHelper.isRemoteCodeValid(this.fragmentContentBinding.editTextRemoteCodeValue.getText().toString())) {
                    ToastHelper.makeToast(
                            this.getContext(), this.getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_SHORT);
                    return;
                }

                // same as code on this device?
                if (!BuildConfig.DEBUG
                        && this.fragmentContentBinding.editTextRemoteCodeValue.getText().toString().equals(
                        this.fragmentContentBinding.textViewLocalCodeValue.getText().toString())) {
                    ToastHelper.makeToast(
                            this.getContext(), this.getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_SHORT);
                    return;
                }

                if (this.contentCloud.isServiceReceiveBound) {
                    this.contentCloud.cloudServiceReceive.receive(this, this.fragmentContentBinding.editTextRemoteCodeValue.getText().toString());
                }
            }
        });
    }
}
