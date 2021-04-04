package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

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

public class ContentFragment extends FragmentCommon {
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    public FragmentContentBinding fragmentContentBinding;
    @NonNull
    public CountDownLatch latchAllCount = new CountDownLatch(1);
    @NonNull
    public CountDownLatch latchAuthor = new CountDownLatch(1);
    @NonNull
    public CountDownLatch latchFavouriteCount = new CountDownLatch(1);
    public int countSearchResults;
    @Nullable
    protected ContentPreferences contentPreferences;
    @Nullable
    protected ContentCloud contentCloud;
    @Nullable
    private DisposableObserver<Integer> disposableObserver;

    protected ContentFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static ContentFragment newInstance(final int widgetId) {
        final ContentFragment fragment = new ContentFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentCloud = new ContentCloud();
        quoteUnquoteModel = new QuoteUnquoteModel(getContext());
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        final Intent intent = new Intent(getContext(), CloudServiceReceive.class);
        getContext().bindService(intent, contentCloud.serviceConnection, Context.BIND_AUTO_CREATE);

        contentPreferences = new ContentPreferences(this.widgetId, this.getContext());

        fragmentContentBinding = FragmentContentBinding.inflate(getLayoutInflater());
        return fragmentContentBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentContentBinding = null;

        shutdown();

        getContext().unbindService(contentCloud.serviceConnection);
        contentCloud.isServiceReceiveBound = false;
    }

    public void shutdown() {
        disposables.clear();
        disposables.dispose();

        if (disposableObserver != null) {
            disposableObserver.dispose();
        }
    }

    protected void setFavouriteLocalCode() {
        if ("".equals(contentPreferences.getContentFavouritesLocalCode())) {
            // possible that user wiped storage via OS settings
            contentPreferences.setContentFavouritesLocalCode(CloudFavouritesHelper.getLocalCode());
        }

        fragmentContentBinding.textViewLocalCodeValue.setText(contentPreferences.getContentFavouritesLocalCode());
    }

    protected void setSearch() {
        setSearchObserver();

        final String editTextKeywords = contentPreferences.getContentSelectionSearch();

        if (editTextKeywords.length() > 0) {
            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditEvent("SEARCH", properties);
            fragmentContentBinding.editTextSearchText.setText(editTextKeywords);
        }
    }

    protected void setSearchObserver() {
        disposableObserver = new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull final Integer value) {
                fragmentContentBinding.radioButtonSearch.setText(
                        getResources().getString(R.string.fragment_content_text, value));
                countSearchResults = value;
            }

            @Override
            public void onError(@NonNull final Throwable throwable) {
                Timber.d(throwable);
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
            }
        };

        RxTextView.textChanges(fragmentContentBinding.editTextSearchText)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    final String keywords = charSequence.toString();

                    if (!keywords.equals("")) {
                        Timber.d("%s", keywords);

                        if (!contentPreferences.getContentSelectionSearch().equals(keywords)) {
                            contentPreferences.setContentSelectionSearch(keywords);
                        }

                        return quoteUnquoteModel.countQuotationWithSearchText(keywords);
                    } else {
                        return 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, final Bundle savedInstanceState) {
        createListenerRadioGroup();
        createListenerAuthor();
        createListenerFavouriteButtonSend();
        createListenerFavouriteButtonReceive();

        setSelection();
        setAllCount();
        setAuthor();
        setFavouriteCount();
        setFavouriteLocalCode();
        setSearch();
    }

    public void setAllCount() {
        disposables.add(quoteUnquoteModel.countAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull final Integer value) {
                                fragmentContentBinding.radioButtonAll.setText(
                                        getResources().getString(R.string.fragment_content_all, value));

                                synchronized (this) {
                                    latchAllCount.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d(throwable);
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
                                fragmentContentBinding.spinnerAuthors.setAdapter(adapter);

                                if ("".equals(contentPreferences.getContentSelectionAuthor())) {
                                    contentPreferences.setContentSelectionAuthor(authors.get(0));
                                }

                                setAuthorName();

                                synchronized (this) {
                                    latchAuthor.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d(throwable);
                            }
                        }));
    }

    protected void setAuthorName() {
        final String authorPreference = contentPreferences.getContentSelectionAuthor();

        fragmentContentBinding.spinnerAuthors.setSelection(
                quoteUnquoteModel.authorsIndex(authorPreference));

        fragmentContentBinding.radioButtonAuthor.setText(
                getResources().getString(
                        R.string.fragment_content_author,
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
                                fragmentContentBinding.radioButtonFavourites.setEnabled(true);
                                if (value == 0) {
                                    fragmentContentBinding.radioButtonFavourites.setEnabled(false);
                                }

                                fragmentContentBinding.radioButtonFavourites.setText(
                                        getResources().getString(R.string.fragment_content_favourites, value));

                                synchronized (this) {
                                    latchFavouriteCount.countDown();
                                }
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d(throwable);
                            }
                        }));
    }

    protected void setSelection() {
        enableAuthor(false);
        enableFavourites(false);
        enableSearch(false);

        switch (contentPreferences.getContentSelection()) {
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
        fragmentContentBinding.radioButtonAll.setChecked(true);
    }

    private void setSelectionAuthor() {
        fragmentContentBinding.radioButtonAuthor.setChecked(true);
        enableAuthor(true);
    }

    private void setSelectionFavourites() {
        fragmentContentBinding.radioButtonFavourites.setChecked(true);
        enableFavourites(true);
    }

    private void setSelectionSearch() {
        fragmentContentBinding.radioButtonSearch.setChecked(true);
        enableSearch(true);

        fragmentContentBinding.radioButtonSearch.requestFocus();

        final String searchText = contentPreferences.getContentSelectionSearch();

        if (!searchText.equals("") && !contentPreferences.getContentSelectionSearch().equals(searchText)) {
            contentPreferences.setContentSelectionSearch(searchText);

            final EditText editTextKeywordsSearch = fragmentContentBinding.editTextSearchText;
            editTextKeywordsSearch.setText(searchText);
        }
    }

    protected void createListenerRadioGroup() {
        final RadioGroup radioGroupContent = fragmentContentBinding.radioGroupContent;
        radioGroupContent.setOnCheckedChangeListener((group, checkedId) -> {

            enableAuthor(false);
            enableFavourites(false);
            enableSearch(false);

            if (checkedId == fragmentContentBinding.radioButtonAll.getId()) {
                contentPreferences.setContentSelection(ContentSelection.ALL);
            }

            if (checkedId == fragmentContentBinding.radioButtonAuthor.getId()) {
                enableAuthor(true);
                contentPreferences.setContentSelection(ContentSelection.AUTHOR);
            }

            if (checkedId == fragmentContentBinding.radioButtonFavourites.getId()) {
                enableFavourites(true);
                contentPreferences.setContentSelection(ContentSelection.FAVOURITES);
            }

            if (checkedId == fragmentContentBinding.radioButtonSearch.getId()) {
                enableSearch(true);
                contentPreferences.setContentSelection(ContentSelection.SEARCH);
            }
        });
    }

    private void enableAuthor(final boolean enable) {
        fragmentContentBinding.spinnerAuthors.setEnabled(enable);
    }

    private void enableFavourites(final boolean enable) {
        fragmentContentBinding.textViewLocalCodeValue.setEnabled(enable);

        fragmentContentBinding.buttonSend.setEnabled(enable);
        makeButtonAlpha(fragmentContentBinding.buttonSend, enable);
        fragmentContentBinding.buttonSend.setClickable(enable);
    }

    private void makeButtonAlpha(@NonNull final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    private void enableSearch(final boolean enable) {
        fragmentContentBinding.editTextSearchText.setEnabled(enable);
    }

    protected void createListenerAuthor() {
        fragmentContentBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long selectedItemId) {
                final String author = fragmentContentBinding.spinnerAuthors.getSelectedItem().toString();
                fragmentContentBinding.radioButtonAuthor.setText(
                        getResources().getString(R.string.fragment_content_author,
                                quoteUnquoteModel.countAuthorQuotations(author)));

                if (!contentPreferences.getContentSelectionAuthor().equals(author)) {
                    Timber.d("author=%s", author);
                    final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                    properties.put("Author", author);
                    AuditEventHelper.auditEvent("AUTHOR", properties);

                    contentPreferences.setContentSelectionAuthor(author);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    protected void createListenerFavouriteButtonSend() {
        fragmentContentBinding.buttonSend.setOnClickListener(v -> {
            if (fragmentContentBinding.buttonSend.isEnabled()) {

                final Intent serviceIntent = new Intent(getContext(), CloudServiceSend.class);
                serviceIntent.putExtra("savePayload", quoteUnquoteModel.getFavouritesToSend(getContext()));
                serviceIntent.putExtra(
                        "localCodeValue", fragmentContentBinding.textViewLocalCodeValue.getText().toString());

                getContext().startService(serviceIntent);

            }
        });
    }

    protected void createListenerFavouriteButtonReceive() {
        fragmentContentBinding.buttonReceive.setOnClickListener(v -> {
            if (fragmentContentBinding.buttonReceive.isEnabled()) {
                Timber.d("remoteCode=%s", fragmentContentBinding.editTextRemoteCodeValue.getText().toString());

                // correct length?
                if (fragmentContentBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
                    ToastHelper.makeToast(
                            getContext(), getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_LONG);
                    return;
                }

                // crc wrong?
                if (!CloudFavouritesHelper.isRemoteCodeValid(fragmentContentBinding.editTextRemoteCodeValue.getText().toString())) {
                    ToastHelper.makeToast(
                            getContext(), getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_LONG);
                    return;
                }

                // same as code on this device?
                if (!BuildConfig.DEBUG
                    && fragmentContentBinding.editTextRemoteCodeValue.getText().toString().equals(
                            fragmentContentBinding.textViewLocalCodeValue.getText().toString())) {
                    ToastHelper.makeToast(
                            getContext(), getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_LONG);
                    return;
                }


                if (contentCloud.isServiceReceiveBound) {
                    contentCloud.cloudServiceReceive.receive(this, fragmentContentBinding.editTextRemoteCodeValue.getText().toString());
                }
            }
        });
    }
}
