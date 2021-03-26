package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceReceive;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceSend;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;
import com.github.jameshnsears.quoteunquote.databinding.FragmentContentBinding;
import com.github.jameshnsears.quoteunquote.utils.Preferences;
import com.github.jameshnsears.quoteunquote.utils.ToastHelper;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.microsoft.appcenter.Flags;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.ViewModelProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public final class FragmentContent extends FragmentCommon {
    private static final String LOG_TAG = FragmentContent.class.getSimpleName();

    public FragmentContentBinding fragmentContentBinding;
    public int countKeywords;
    private CompositeDisposable disposables = new CompositeDisposable();
    private DisposableObserver<Integer> disposableObserver;
    private FragmentViewModel fragmentViewModel;

    private CloudServiceReceive cloudServiceReceive;
    private boolean isServiceReceiveBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final CloudServiceReceive.LocalBinder binder = (CloudServiceReceive.LocalBinder) service;
            cloudServiceReceive = binder.getService();
            isServiceReceiveBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceReceiveBound = false;
        }
    };

    private FragmentContent(final int widgetId) {
        super(LOG_TAG, widgetId);
    }

    public static FragmentContent newInstance(final int widgetId) {
        final FragmentContent fragment = new FragmentContent(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentViewModel = new ViewModelProvider(this).get(FragmentViewModel.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final Intent intent = new Intent(getContext(), CloudServiceReceive.class);
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        fragmentContentBinding = FragmentContentBinding.inflate(getLayoutInflater());
        return fragmentContentBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        fragmentContentBinding = null;

        fragmentViewModel.shutdown();

        disposables.clear();
        disposableObserver.dispose();

        getContext().unbindService(serviceConnection);
        isServiceReceiveBound = false;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        setRadioButtonsViaSharedPreferences();

        createRadioGroupListener();
        createSpinnerAuthorsListener();
        createFavouriteButtonSendListener();
        createFavouriteButtonReceiveListener();

        enableFavouriteReceiveButton(true);

        setCountAll();
        setSpinnerAuthors();
        setCountFavourites();
        setLocalCode();
        setQuotationTextSearch();
    }

    private void setLocalCode() {
        fragmentContentBinding.textViewLocalCodeValue.setText(CloudFavouritesHelper.getSharedPreferenceLocalCode(getContext()));
    }

    private void setQuotationTextSearch() {
        disposableObserver = new DisposableObserver<Integer>() {
            @Override
            public void onNext(final Integer value) {
                fragmentContentBinding.radioButtonKeywords.setText(
                        getResources().getString(R.string.fragment_content_text, value));
                countKeywords = value;
            }

            @Override
            public void onError(final Throwable e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(LOG_TAG, "onComplete");
            }
        };

        RxTextView.textChanges(fragmentContentBinding.editTextKeywords)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(charSequence -> {
                    final String keywords = charSequence.toString();

                    Log.d(LOG_TAG, "apply:" + keywords);

                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_CONTENT, Preferences.EDIT_TEXT_KEYWORDS, keywords);

                    return fragmentViewModel.countQuotationWithText(keywords);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);

        final String editTextKeywords = preferences.getSharedPreferenceString(
                Preferences.FRAGMENT_CONTENT, Preferences.EDIT_TEXT_KEYWORDS);

        if (editTextKeywords != null && editTextKeywords.length() > 0) {
            final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
            properties.put("Text", editTextKeywords);
            AuditEventHelper.auditAppCenter(AuditEventHelper.QUOTATION, properties, Flags.NORMAL);
            fragmentContentBinding.editTextKeywords.setText(editTextKeywords);
        }
    }

    private void setCountAll() {
        disposables.add(fragmentViewModel.countAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(final Integer value) {
                                fragmentContentBinding.radioButtonAll.setText(
                                        getResources().getString(R.string.fragment_content_all, value));
                            }

                            @Override
                            public void onError(final Throwable e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        }));
    }

    private void setSpinnerAuthors() {
        disposables.add(fragmentViewModel.authors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<List<AuthorPOJO>>() {
                            @Override
                            public void onSuccess(final List<AuthorPOJO> authorPOJOList) {
                                final List<String> authors = fragmentViewModel.authorsSorted(authorPOJOList);

                                final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        getContext(),
                                        R.layout.spinner_item,
                                        authors);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                fragmentContentBinding.spinnerAuthors.setAdapter(adapter);

                                setSpinnerAuthorsViaSharedPreferences(authors.get(0));
                            }

                            @Override
                            public void onError(final Throwable e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        }));
    }

    private void setSpinnerAuthorsViaSharedPreferences(final String firstAuthorInSpinner) {
        final String sharedPreferenceAuthor = preferences.getSharedPreferenceString(
                Preferences.FRAGMENT_CONTENT, Preferences.SPINNER_AUTHORS);

        if ("".equals(sharedPreferenceAuthor)) {
            fragmentContentBinding.radioButtonAuthor.setText(
                    String.format(Locale.ENGLISH, "%s %d",
                            getResources().getString(R.string.fragment_content_author),
                            fragmentViewModel.countAuthorQuotations(firstAuthorInSpinner)));
        } else {
            fragmentContentBinding.spinnerAuthors.setSelection(fragmentViewModel.authorsIndex(sharedPreferenceAuthor));

            fragmentContentBinding.radioButtonAuthor.setText(
                    String.format(Locale.ENGLISH, "%s %d",
                            getResources().getString(R.string.fragment_content_author),
                            fragmentViewModel.countAuthorQuotations(sharedPreferenceAuthor)));
        }
    }

    public void setCountFavourites() {
        disposables.add(fragmentViewModel.countFavourites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(final Integer value) {
                                fragmentContentBinding.radioButtonFavourites.setEnabled(true);
                                if (value == 0) {
                                    fragmentContentBinding.radioButtonFavourites.setEnabled(false);
                                }

                                fragmentContentBinding.radioButtonFavourites.setText(
                                        getResources().getString(R.string.fragment_content_favourites, value));
                            }

                            @Override
                            public void onError(final Throwable e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        }));
    }

    private void setRadioButtonsViaSharedPreferences() {
        enableAuthor(false);
        enableFavourites(false);
        enableKeywords(false);

        final boolean booleanRadioButtonAll = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, true);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, booleanRadioButtonAll);
        fragmentContentBinding.radioButtonAll.setChecked(booleanRadioButtonAll);

        final boolean booleanRadioButtonFavourites = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES, booleanRadioButtonFavourites);
        fragmentContentBinding.radioButtonFavourites.setChecked(booleanRadioButtonFavourites);
        if (booleanRadioButtonFavourites) {
            enableFavourites(true);
        }

        final boolean booleanRadioButtonAuthor = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_AUTHOR);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_AUTHOR, booleanRadioButtonAuthor);
        fragmentContentBinding.radioButtonAuthor.setChecked(booleanRadioButtonAuthor);
        if (booleanRadioButtonAuthor) {
            enableAuthor(true);
        }

        final boolean booleanRadioButtonKeywords = preferences.getSharedPreferenceBoolean(
                Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT);
        preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT, booleanRadioButtonKeywords);
        fragmentContentBinding.radioButtonKeywords.setChecked(booleanRadioButtonKeywords);

        if (booleanRadioButtonKeywords) {
            enableKeywords(true);
            fragmentContentBinding.radioButtonKeywords.requestFocus();
        }

        final String editTextKeywords = preferences.getSharedPreferenceString(
                Preferences.FRAGMENT_CONTENT, Preferences.EDIT_TEXT_KEYWORDS);
        preferences.setSharedPreference(
                Preferences.FRAGMENT_CONTENT, Preferences.EDIT_TEXT_KEYWORDS, editTextKeywords);

        if (editTextKeywords != null) {
            final EditText editTextKeywordsSearch = fragmentContentBinding.editTextKeywords;
            editTextKeywordsSearch.setText(editTextKeywords);
        }
    }

    private void createRadioGroupListener() {
        final RadioGroup radioGroupContent = fragmentContentBinding.radioGroupContent;
        radioGroupContent.setOnCheckedChangeListener((group, checkedId) -> {
            preferences.setSharedPreference(
                    Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, false);
            preferences.setSharedPreference(
                    Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES, false);
            preferences.setSharedPreference(
                    Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_AUTHOR, false);
            preferences.setSharedPreference(
                    Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT, false);

            enableAuthor(false);
            enableFavourites(false);
            enableKeywords(false);

            switch (checkedId) {
                case R.id.radioButtonAll:
                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, true);
                    break;

                case R.id.radioButtonFavourites:
                    enableFavourites(true);
                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_FAVOURITES, true);
                    break;

                case R.id.radioButtonAuthor:
                    enableAuthor(true);
                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_AUTHOR, true);
                    break;

                case R.id.radioButtonKeywords:
                    enableKeywords(true);
                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT, true);
                    break;

                default:
                    Log.e(LOG_TAG, String.valueOf(checkedId));
                    break;
            }
        });
    }

    private void enableAuthor(final boolean enable) {
        fragmentContentBinding.spinnerAuthors.setEnabled(enable);
    }

    private void enableFavourites(final boolean enable) {
        fragmentContentBinding.textViewLocalCodeValue.setEnabled(enable);
        enableFavouriteSendButton(enable);
    }

    public void enableFavouriteReceiveButton(final boolean enable) {
        getActivity().runOnUiThread(() -> {
            fragmentContentBinding.buttonReceive.setEnabled(enable);
            setButtonAlpha(fragmentContentBinding.buttonReceive, enable);
            fragmentContentBinding.buttonReceive.setClickable(enable);
        });
    }

    private void enableFavouriteSendButton(final boolean enable) {
        fragmentContentBinding.buttonSend.setEnabled(enable);
        setButtonAlpha(fragmentContentBinding.buttonSend, enable);
        fragmentContentBinding.buttonSend.setClickable(enable);
    }

    private void setButtonAlpha(final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    private void enableKeywords(final boolean enableKeywordSearch) {
        fragmentContentBinding.editTextKeywords.setEnabled(enableKeywordSearch);
    }

    private void createSpinnerAuthorsListener() {
        fragmentContentBinding.spinnerAuthors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                final String author = fragmentContentBinding.spinnerAuthors.getSelectedItem().toString();
                fragmentContentBinding.radioButtonAuthor.setText(
                        getResources().getString(R.string.fragment_content_author,
                                fragmentViewModel.countAuthorQuotations(author)));

                if (!preferences.getSharedPreferenceString(Preferences.FRAGMENT_CONTENT, Preferences.SPINNER_AUTHORS).equals(author)) {
                    Log.d(LOG_TAG, "sending new event, author=" + author);
                    final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                    properties.put("Author", author);
                    AuditEventHelper.auditAppCenter(AuditEventHelper.AUTHOR, properties, Flags.NORMAL);

                    preferences.setSharedPreference(
                            Preferences.FRAGMENT_CONTENT, Preferences.SPINNER_AUTHORS, author);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void createFavouriteButtonSendListener() {
        fragmentContentBinding.buttonSend.setOnClickListener(v -> {
            if (fragmentContentBinding.buttonSend.isEnabled()) {

                if (!CloudServiceSend.isRunning(getContext())) {
                    final Intent serviceIntent = new Intent(getContext(), CloudServiceSend.class);
                    serviceIntent.putExtra("savePayload", fragmentViewModel.getSavePayload());
                    serviceIntent.putExtra(
                            "localCodeValue", fragmentContentBinding.textViewLocalCodeValue.getText().toString());
                    getContext().startService(serviceIntent);
                } else {
                    Log.w(LOG_TAG, "CloudServiceSend already running");
                }
            }
        });
    }

    private void createFavouriteButtonReceiveListener() {
        fragmentContentBinding.buttonReceive.setOnClickListener(v -> {
            if (fragmentContentBinding.buttonReceive.isEnabled()) {
                Log.d(LOG_TAG, "buttonReceive: remoteCode=" + fragmentContentBinding.editTextRemoteCodeValue.getText().toString());

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

                if (isServiceReceiveBound) {
                    cloudServiceReceive.receive(this, fragmentContentBinding.editTextRemoteCodeValue.getText().toString());
                    enableFavouriteReceiveButton(false);
                }
            }
        });
    }

}
