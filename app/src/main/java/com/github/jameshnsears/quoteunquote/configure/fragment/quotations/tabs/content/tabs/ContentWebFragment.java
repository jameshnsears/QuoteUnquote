package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseTabWebBinding;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperData;

import timber.log.Timber;

@Keep
public class ContentWebFragment extends ContentFragment {
    @Nullable
    public FragmentQuotationsTabDatabaseTabWebBinding fragmentQuotationsTabDatabaseTabWebBinding;

    @Nullable
    public QuotationsPreferences quotationsPreferences;

    public ContentWebFragment(int widgetId) {
        super(widgetId);
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        this.quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());

        this.fragmentQuotationsTabDatabaseTabWebBinding = FragmentQuotationsTabDatabaseTabWebBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsTabDatabaseTabWebBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {
        Timber.d("onViewCreated.ContentWebFragment");

        if (this.quotationsPreferences.getDatabaseExternalWeb()) {
            this.fragmentQuotationsTabDatabaseTabWebBinding.radioButtonDatabaseExternalWeb.setChecked(true);
            this.fragmentQuotationsTabDatabaseTabWebBinding.radioButtonDatabaseExternalWeb.setEnabled(true);
        } else {
            this.fragmentQuotationsTabDatabaseTabWebBinding.radioButtonDatabaseExternalWeb.setChecked(false);
            this.fragmentQuotationsTabDatabaseTabWebBinding.radioButtonDatabaseExternalWeb.setEnabled(false);
        }

        fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.setText(quotationsPreferences.getDatabaseWebUrl());
        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.setText(quotationsPreferences.getDatabaseWebXpathQuotation());
        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.setText(quotationsPreferences.getDatabaseWebXpathSource());
        this.fragmentQuotationsTabDatabaseTabWebBinding.switchKeepLatestResponseOnly.setChecked(
                this.quotationsPreferences.getDatabaseWebKeepLatestOnly()
        );

        createListenerRadioWebPage();

        createExternalEditTextChangeListeners();

        createListenerSwitchKeepLatestReponseOnly();

        createListenerButtonImportWebPage();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        quotationsPreferences.setDatabaseWebUrl(
                fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.getText().toString()
        );
        quotationsPreferences.setDatabaseWebXpathQuotation(
                fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.getText().toString()
        );
        quotationsPreferences.setDatabaseWebXpathSource(
                fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.getText().toString()
        );

        this.fragmentQuotationsTabDatabaseTabWebBinding = null;
    }

    private void createListenerRadioWebPage() {
        final RadioButton radioButtonDatabaseInternal = this.fragmentQuotationsTabDatabaseTabWebBinding.radioButtonDatabaseExternalWeb;
        radioButtonDatabaseInternal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            usingWebPage();
        });

        radioButtonDatabaseInternal.setOnClickListener(v -> {
            fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.clearFocus();
            fragmentQuotationsTabDatabaseTabWebBinding.editTextUrlLayout.clearFocus();
            fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.clearFocus();
            fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotationLayout.clearFocus();
            fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.clearFocus();
            fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSourceLayout.clearFocus();
        });
    }

    private void usingWebPage() {
        this.quotationsPreferences.setDatabaseInternal(false);
        this.quotationsPreferences.setDatabaseExternalCsv(false);
        this.quotationsPreferences.setDatabaseExternalWeb(true);
        this.quotationsPreferences.setDatabaseExternalContent(QuotationsPreferences.DATABASE_EXTERNAL_WEB);
        DatabaseRepository.useInternalDatabase = false;

        updateQuotationsUI();
    }

    private void createExternalEditTextChangeListeners() {
        fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Timber.d("editTextUrl=%s",
                        fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.getText().toString());
                quotationsPreferences.setDatabaseWebUrl(
                        fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.getText().toString());
            }
        });

        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Timber.d("editTextXpathQuotation=%s",
                        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.getText().toString());
                quotationsPreferences.setDatabaseWebXpathQuotation(
                        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.getText().toString());
            }
        });

        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Timber.d("editTextXpathSource=%s",
                        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.getText().toString());
                quotationsPreferences.setDatabaseWebXpathSource(
                        fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.getText().toString());
            }
        });
    }

    private String getWebUrl() {
        String url = fragmentQuotationsTabDatabaseTabWebBinding.editTextUrl.getText().toString();
        Timber.d("url=%s", url);
        this.quotationsPreferences.setDatabaseWebUrl(url);
        return url;
    }

    private String getWebXpathQuotation() {
        String xpathQuotation = fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathQuotation.getText().toString();
        Timber.d("xpathQuotation=%s", xpathQuotation);
        this.quotationsPreferences.setDatabaseWebXpathQuotation(xpathQuotation);
        return xpathQuotation;
    }

    private String getWebXpathSource() {
        String xpathSource = fragmentQuotationsTabDatabaseTabWebBinding.editTextXpathSource.getText().toString();
        Timber.d("xpathSource=%s", xpathSource);
        this.quotationsPreferences.setDatabaseWebXpathSource(xpathSource);
        return xpathSource;
    }

    private void createListenerSwitchKeepLatestReponseOnly() {
        fragmentQuotationsTabDatabaseTabWebBinding.switchKeepLatestResponseOnly.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.quotationsPreferences.setDatabaseWebKeepLatestOnly(isChecked)
        );
    }

    private void createListenerButtonImportWebPage() {
        fragmentQuotationsTabDatabaseTabWebBinding.buttonImportWebPage.setOnClickListener(v -> {
            if (fragmentQuotationsTabDatabaseTabWebBinding.buttonImportWebPage.isPressed()) {

                    String url = getWebUrl();
                    String xpathQuotation = getWebXpathQuotation();
                    String xpathSource = getWebXpathSource();

                    if ("".equals(url) || "".equals(xpathQuotation) | "".equals(xpathSource)
                            || 10 > url.length()) {
                        useInternalDatabase();

                        Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.fragment_quotations_database_scrape_fields_error_incomplete),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                this.getContext(),
                                this.getContext().getString(R.string.fragment_quotations_database_scrape_importing),
                                Toast.LENGTH_SHORT).show();

                        ScraperData scraperData = quoteUnquoteModel.getWebPage(
                                getContext(), url, xpathQuotation, xpathSource);

                        if (scraperData.getScrapeResult()) {
                            quoteUnquoteModel.insertWebPage(
                                    widgetId,
                                    scraperData.getQuotation(),
                                    scraperData.getSource(),
                                    ImportHelper.DEFAULT_DIGEST
                            );

                            usingWebPage();

                            importWasSuccessful();

                            this.fragmentQuotationsTabDatabaseTabWebBinding.radioButtonDatabaseExternalWeb.setEnabled(false);

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_quotations_database_scrape_test_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            useInternalDatabase();
                        }
                    }
                }
        });
    }
}
