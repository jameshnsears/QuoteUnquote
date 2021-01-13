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
import androidx.lifecycle.ViewModelProvider;

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
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    public FragmentContentBinding fragmentContentBinding;
    public volatile CountDownLatch latchAllCount = new CountDownLatch(1);
    public volatile CountDownLatch latchAuthor = new CountDownLatch(1);
    public volatile CountDownLatch latchFavouriteCount = new CountDownLatch(1);
    public int countSearchResults;
    @Nullable
    protected ContentViewModel contentViewModel;

    @Nullable
    protected ContentPreferences contentPreferences;

    protected ContentFragment(final int widgetId) {
        super(widgetId);
    }

    @Nullable
    private DisposableObserver<Integer> disposableObserver;
    @Nullable
    protected ContentCloud contentCloud;

    @NonNull
    public static ContentFragment newInstance(final int widgetId) {
        final ContentFragment fragment = new ContentFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentViewModel = getContentViewModel();
        contentCloud = new ContentCloud();
    }

    @NonNull
    public ContentViewModel getContentViewModel() {
        return new ViewModelProvider(this).get(ContentViewModel.class);
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

        contentViewModel.shutdown();
    }

    protected void setFavouriteLocalCode() {
        final ContentPreferences contentPreferences = new ContentPreferences(0, getContext());

        String localCode = contentPreferences.getContentFavouritesLocalCode();

        if ("".equals(localCode) && !contentPreferences.getContentFavouritesLocalCode().equals(CloudFavouritesHelper.getLocalCode())) {
            contentPreferences.setContentFavouritesLocalCode(CloudFavouritesHelper.getLocalCode());
            localCode = contentPreferences.getContentFavouritesLocalCode();
        }

        fragmentContentBinding.textViewLocalCodeValue.setText(localCode);
    }

    protected void setSearch() {
        setSearchObserver();

        final String editTextKeywords = contentPreferences.getContentSelectionSearchText();

        if (editTextKeywords != null && editTextKeywords.length() > 0) {
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
                        Timber.d("apply:%s", keywords);

                        if (!contentPreferences.getContentSelectionSearchText().equals(keywords)) {
                            contentPreferences.setContentSelectionSearchText(keywords);
                        }

                        return contentViewModel.countQuotationWithText(keywords);
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

        enableFavouriteButtonReceive(true);

        setAllCount();
        setAuthor();
        setFavouriteCount();
        setFavouriteLocalCode();
        setSearch();
    }

    public void setAllCount() {
        disposables.add(contentViewModel.countAll()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())     // AndroidSchedulers.mainThread()
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull final Integer value) {
                                fragmentContentBinding.radioButtonAll.setText(
                                        getResources().getString(R.string.fragment_content_all, value));

                                latchAllCount.countDown();
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d(throwable);
                            }
                        }));
    }

    protected void setAuthor() {
        disposables.add(contentViewModel.authors()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())     // AndroidSchedulers.mainThread()
                .subscribeWith(
                        new DisposableSingleObserver<List<AuthorPOJO>>() {
                            @Override
                            public void onSuccess(@NonNull final List<AuthorPOJO> authorPOJOList) {
                                final List<String> authors = contentViewModel.authorsSorted(authorPOJOList);

                                final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        getContext(),
                                        R.layout.spinner_item,
                                        authors);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                fragmentContentBinding.spinnerAuthors.setAdapter(adapter);

                                setAuthorName(authors.get(0));

                                latchAuthor.countDown();
                            }

                            @Override
                            public void onError(@NonNull final Throwable throwable) {
                                Timber.d(throwable);
                            }
                        }));
    }

    protected void setAuthorName(@NonNull final String firstAuthor) {
        final String authorPreference = contentPreferences.getContentSelectionAuthorName();

        if ("".equals(authorPreference) && !authorPreference.equals(firstAuthor)) {
            fragmentContentBinding.radioButtonAuthor.setText(
                    getResources().getString(
                            R.string.fragment_content_author,
                            contentViewModel.countAuthorQuotations(firstAuthor)));


            if (!contentPreferences.getContentSelectionAuthorName().equals(firstAuthor)) {
                contentPreferences.setContentSelectionAuthorName(firstAuthor);
            }
        } else {
            fragmentContentBinding.spinnerAuthors.setSelection(contentViewModel.authorsIndex(authorPreference));

            fragmentContentBinding.radioButtonAuthor.setText(
                    getResources().getString(
                            R.string.fragment_content_author,
                            contentViewModel.countAuthorQuotations(authorPreference)));
        }
    }

    public void setFavouriteCount() {
        disposables.add(contentViewModel.countFavourites()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())      // AndroidSchedulers.mainThread()
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

                                latchFavouriteCount.countDown();
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

        final String searchText = contentPreferences.getContentSelectionSearchText();

        if (!searchText.equals("") && !contentPreferences.getContentSelectionSearchText().equals(searchText)) {
            contentPreferences.setContentSelectionSearchText(searchText);

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

    public void enableFavouriteButtonReceive(final boolean enable) {
        getActivity().runOnUiThread(() -> {
            fragmentContentBinding.buttonReceive.setEnabled(enable);
            makeButtonAlpha(fragmentContentBinding.buttonReceive, enable);
            fragmentContentBinding.buttonReceive.setClickable(enable);
        });
    }

    private void makeButtonAlpha(final Button button, final boolean enable) {
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
                                contentViewModel.countAuthorQuotations(author)));

                if (!contentPreferences.getContentSelectionAuthorName().equals(author)) {
                    Timber.d("sending new event, author=%s", author);
                    final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                    properties.put("Author", author);
                    AuditEventHelper.auditEvent("AUTHOR", properties);

                    contentPreferences.setContentSelectionAuthorName(author);
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

                if (!CloudServiceSend.isRunning(getContext())) {
                    final Intent serviceIntent = new Intent(getContext(), CloudServiceSend.class);
                    serviceIntent.putExtra("savePayload", contentViewModel.getFavouritesToSend());
                    serviceIntent.putExtra(
                            "localCodeValue", fragmentContentBinding.textViewLocalCodeValue.getText().toString());
                    getContext().startService(serviceIntent);
                } else {
                    Timber.w("CloudServiceSend already running");
                }
            }
        });
    }

    protected void createListenerFavouriteButtonReceive() {
        fragmentContentBinding.buttonReceive.setOnClickListener(v -> {
            if (fragmentContentBinding.buttonReceive.isEnabled()) {
                Timber.d("buttonReceive: remoteCode=%s", fragmentContentBinding.editTextRemoteCodeValue.getText().toString());

                if (fragmentContentBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
                    ToastHelper.makeToast(
                            getContext(), getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_SHORT);
                    return;
                }

                if (fragmentContentBinding.editTextRemoteCodeValue.getText().toString().equals(fragmentContentBinding.textViewLocalCodeValue.getText().toString())) {
                    ToastHelper.makeToast(
                            getContext(), getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_SHORT);
                    return;
                }

                if (!CloudFavouritesHelper.isRemoteCodeValid(fragmentContentBinding.editTextRemoteCodeValue.getText().toString())) {
                    ToastHelper.makeToast(
                            getContext(), getContext().getString(R.string.fragment_content_favourites_share_remote_code_general), Toast.LENGTH_SHORT);
                    return;
                }

                if (contentCloud.isServiceReceiveBound) {
                    contentCloud.cloudServiceReceive.receive(this, fragmentContentBinding.editTextRemoteCodeValue.getText().toString());
                    enableFavouriteButtonReceive(false);
                }
            }
        });
    }
}
