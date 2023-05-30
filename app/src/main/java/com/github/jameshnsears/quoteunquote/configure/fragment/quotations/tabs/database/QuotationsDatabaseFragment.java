package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.database;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseBinding;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.ImportHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.scraper.ScraperData;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

@Keep
public class QuotationsDatabaseFragment extends FragmentCommon {
    @Nullable
    public FragmentQuotationsTabDatabaseBinding fragmentQuotationsTabDatabaseBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    public QuotationsPreferences quotationsPreferences;

    @Nullable
    private ActivityResultLauncher<Intent> storageAccessFrameworkActivityResultCSV;

    public QuotationsDatabaseFragment() {
        // dark mode support
    }

    public QuotationsDatabaseFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsDatabaseFragment newInstance(
            int widgetId) {
        QuotationsDatabaseFragment fragment = new QuotationsDatabaseFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        this.quotationsPreferences = new QuotationsPreferences(this.widgetId, this.getContext());

        this.fragmentQuotationsTabDatabaseBinding = FragmentQuotationsTabDatabaseBinding.inflate(this.getLayoutInflater());
        return this.fragmentQuotationsTabDatabaseBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {
        this.fragmentQuotationsTabDatabaseBinding.textViewExamples.setMovementMethod(LinkMovementMethod.getInstance());

        this.setDatabase();

        this.createListenerRadioInternal();

        this.createListenerRadioExternalCsv();
        this.createListenerButtonImportCsv();

        this.createListenerRadioExternalWeb();
        this.createListenerSwitchKeepLatestReponseOnly();
        this.createListenerButtonImportWebPage();

        this.setHandleImportCsv();

        createExternalEditTextChangeListeners();

//        if (BuildConfig.DEBUG) {
            String url = "https://www.bible.com/verse-of-the-day";
//            if (BuildConfig.DATABASE_QUOTATIONS.contains(".db.prod")) {
//                // javalin - Listening on http://localhost:7070/
//                url = "http://10.0.2.2:7070/verse-of-the-day";
//            }
//            fragmentQuotationsTabDatabaseBinding.editTextUrl.setText(url);
//            fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation
//                    .setText(getContext().getString(R.string.fragment_quotations_database_scrape_quotation_example));
//            fragmentQuotationsTabDatabaseBinding.editTextXpathSource.setText(
//                    getContext().getString(R.string.fragment_quotations_database_scrape_source_example));
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        quotationsPreferences.setDatabaseWebUrl(
                fragmentQuotationsTabDatabaseBinding.editTextUrl.getText().toString()
        );
        quotationsPreferences.setDatabaseWebXpathQuotation(
                fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.getText().toString()
        );
        quotationsPreferences.setDatabaseWebXpathSource(
                fragmentQuotationsTabDatabaseBinding.editTextXpathSource.getText().toString()
        );

        this.fragmentQuotationsTabDatabaseBinding = null;
    }

    private void createExternalEditTextChangeListeners() {
        fragmentQuotationsTabDatabaseBinding.editTextUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Timber.d("editTextUrl=%s",
                        fragmentQuotationsTabDatabaseBinding.editTextUrl.getText().toString());
                quotationsPreferences.setDatabaseWebUrl(
                        fragmentQuotationsTabDatabaseBinding.editTextUrl.getText().toString());
            }
        });

        fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Timber.d("editTextXpathQuotation=%s",
                        fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.getText().toString());
                quotationsPreferences.setDatabaseWebXpathQuotation(
                        fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.getText().toString());
            }
        });

        fragmentQuotationsTabDatabaseBinding.editTextXpathSource.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Timber.d("editTextXpathSource=%s",
                        fragmentQuotationsTabDatabaseBinding.editTextXpathSource.getText().toString());
                quotationsPreferences.setDatabaseWebXpathSource(
                        fragmentQuotationsTabDatabaseBinding.editTextXpathSource.getText().toString());
            }
        });
    }

    private void setDatabase() {
        if (this.quotationsPreferences.getDatabaseInternal()) {
            setDatabaseInternal();

            String databseExternalContent = quotationsPreferences.getDatabaseExternalContent();
            if (databseExternalContent.equals(QuotationsPreferences.DATABASE_EXTERNAL)) {
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setEnabled(true);
            }

            if (databseExternalContent.equals(QuotationsPreferences.DATABASE_EXTERNAL_WEB)) {
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setEnabled(true);
            }
        }

        if (this.quotationsPreferences.getDatabaseExternalCsv()) {
            setDatabaseExternalCsv();
        }

        if (this.quotationsPreferences.getDatabaseExternalWeb()) {
            setDatabaseExternalWeb();
        }

        this.fragmentQuotationsTabDatabaseBinding.switchKeepLatestResponseOnly.setChecked(
                this.quotationsPreferences.getDatabaseWebKeepLatestOnly()
        );

        fragmentQuotationsTabDatabaseBinding.editTextUrl.setText(quotationsPreferences.getDatabaseWebUrl());
        fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.setText(quotationsPreferences.getDatabaseWebXpathQuotation());
        fragmentQuotationsTabDatabaseBinding.editTextXpathSource.setText(quotationsPreferences.getDatabaseWebXpathSource());
    }

    private void setDatabaseInternal() {
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(true);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setChecked(false);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setChecked(false);

        this.quotationsPreferences.setDatabaseInternal(true);
        this.quotationsPreferences.setDatabaseExternalCsv(false);
        this.quotationsPreferences.setDatabaseExternalWeb(false);

        DatabaseRepository.useInternalDatabase = true;
    }

    private void setDatabaseExternalCsv() {
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(false);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setChecked(true);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setEnabled(true);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setChecked(false);

        this.quotationsPreferences.setDatabaseInternal(false);
        this.quotationsPreferences.setDatabaseExternalCsv(true);
        this.quotationsPreferences.setDatabaseExternalWeb(false);
        this.quotationsPreferences.setDatabaseExternalContent(QuotationsPreferences.DATABASE_EXTERNAL);

        DatabaseRepository.useInternalDatabase = false;
    }

    private void setDatabaseExternalWeb() {
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(false);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setChecked(false);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setChecked(true);
        this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setEnabled(true);

        this.quotationsPreferences.setDatabaseInternal(false);
        this.quotationsPreferences.setDatabaseExternalCsv(false);
        this.quotationsPreferences.setDatabaseExternalWeb(true);
        this.quotationsPreferences.setDatabaseExternalContent(QuotationsPreferences.DATABASE_EXTERNAL_WEB);

        fragmentQuotationsTabDatabaseBinding.editTextUrl.setText(quotationsPreferences.getDatabaseWebUrl());
        fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.setText(quotationsPreferences.getDatabaseWebXpathQuotation());
        fragmentQuotationsTabDatabaseBinding.editTextXpathSource.setText(quotationsPreferences.getDatabaseWebXpathSource());

        DatabaseRepository.useInternalDatabase = false;
    }

    protected void createListenerButtonImportCsv() {
        // adb push app/src/androidTest/assets/Favourites.csv /sdcard/Download

        // invoke Storage Access Framework
        fragmentQuotationsTabDatabaseBinding.buttonImport.setOnClickListener(v -> {
            if (fragmentQuotationsTabDatabaseBinding.buttonImport.isEnabled()) {
                ConfigureActivity.launcherInvoked = true;

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/comma-separated-values");
                this.storageAccessFrameworkActivityResultCSV.launch(intent);
            }
        });
    }

    private void createListenerRadioInternal() {
        final RadioButton radioButtonDatabaseInternal = this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal;
        radioButtonDatabaseInternal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setDatabaseInternal();
                updateQuotationsUI();
            }
        });
    }

    private void createListenerRadioExternalCsv() {
        final RadioButton radioButtonDatabaseExternalCsv = this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv;
        radioButtonDatabaseExternalCsv.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setDatabaseExternalCsv();
                updateQuotationsUI();
            }
        });
    }

    private void createListenerRadioExternalWeb() {
        final RadioButton radioButtonDatabaseExternalWeb = this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb;
        radioButtonDatabaseExternalWeb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setDatabaseExternalWeb();
                updateQuotationsUI();
            }
        });
    }

    private String getWebUrl() {
        String url = fragmentQuotationsTabDatabaseBinding.editTextUrl.getText().toString();
        Timber.d("url=%s", url);
        this.quotationsPreferences.setDatabaseWebUrl(url);
        return url;
    }

    private String getWebXpathQuotation() {
        String xpathQuotation = fragmentQuotationsTabDatabaseBinding.editTextXpathQuotation.getText().toString();
        Timber.d("xpathQuotation=%s", xpathQuotation);
        this.quotationsPreferences.setDatabaseWebXpathQuotation(xpathQuotation);
        return xpathQuotation;
    }

    private String getWebXpathSource() {
        String xpathSource = fragmentQuotationsTabDatabaseBinding.editTextXpathSource.getText().toString();
        Timber.d("xpathSource=%s", xpathSource);
        this.quotationsPreferences.setDatabaseWebXpathSource(xpathSource);
        return xpathSource;
    }


    private void createListenerSwitchKeepLatestReponseOnly() {
        fragmentQuotationsTabDatabaseBinding.switchKeepLatestResponseOnly.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.quotationsPreferences.setDatabaseWebKeepLatestOnly(isChecked)
        );
    }

    private void createListenerButtonImportWebPage() {
        fragmentQuotationsTabDatabaseBinding.buttonImportWebPage.setOnClickListener(v -> {
            if (fragmentQuotationsTabDatabaseBinding.buttonImportWebPage.isPressed()) {
                if (fragmentQuotationsTabDatabaseBinding.buttonImportWebPage.isEnabled()) {

                    String url = getWebUrl();
                    String xpathQuotation = getWebXpathQuotation();
                    String xpathSource = getWebXpathSource();

                    if (url.equals("") || xpathQuotation.equals("") | xpathSource.equals("")
                    || url.length() < 10) {
                        Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.fragment_quotations_database_scrape_fields_error_incomplete),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                this.getContext(),
                                this.getContext().getString(R.string.fragment_quotations_database_scrape_importing),
                                Toast.LENGTH_SHORT).show();

                        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
                        properties.put("WebPage",
                                "url=" + url + "; xpathQuotation=" + xpathQuotation + "; xpathSource=" + xpathSource);
                        AuditEventHelper.auditEvent("WEB_PAGE", properties);

                        ScraperData scraperData = quoteUnquoteModel.getWebPage(
                                getContext(), url, xpathQuotation, xpathSource);

                        if (scraperData.getScrapeResult()) {
                            quoteUnquoteModel.insertWebPage(
                                    widgetId,
                                    scraperData.getQuotation(),
                                    scraperData.getSource(),
                                    ImportHelper.DEFAULT_DIGEST
                            );

                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setEnabled(false);
                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setChecked(false);

                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setEnabled(true);
                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setChecked(true);

                            importWasSuccessful();

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_quotations_database_scrape_test_success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void importWasSuccessful() {
        quotationsPreferences.setContentSelectionSearchCount(0);
        quotationsPreferences.setContentSelectionSearch("");

        DatabaseRepository.useInternalDatabase = false;

        updateQuotationsUI();
    }

    private void setHandleImportCsv() {
        // default: /storage/emulated/0/Download/
        this.storageAccessFrameworkActivityResultCSV = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    Timber.d("%d", activityResult.getResultCode());

                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(
                                this.getContext(),
                                this.getContext().getString(R.string.fragment_quotations_database_import_importing),
                                Toast.LENGTH_SHORT).show();

                        ParcelFileDescriptor parcelFileDescriptor = null;
                        FileInputStream fileInputStream = null;

                        try {
                            parcelFileDescriptor = this.getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "r");
                            fileInputStream
                                    = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                            final ImportHelper importHelper = new ImportHelper();
                            final LinkedHashSet<QuotationEntity> quotations = importHelper.csvImportDatabase(fileInputStream);
                            quoteUnquoteModel.insertQuotationsExternal(quotations);

                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setEnabled(true);
                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalCsv.setChecked(true);

                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setEnabled(false);
                            fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternalWeb.setChecked(false);

                            importWasSuccessful();

                            Toast.makeText(
                                    this.getContext(),
                                    this.getContext().getString(R.string.fragment_quotations_database_import_success),
                                    Toast.LENGTH_SHORT).show();

                        } catch (final ImportHelper.ImportHelperException | IOException e) {
                            Toast.makeText(
                                    this.getContext(),
                                    this.getContext().getString(
                                            R.string.fragment_quotations_database_import_contents,
                                            e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        } finally {
                            try {
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                            } catch (IOException e) {
                                Timber.e(e.getMessage());
                            }
                        }
                    }

                    ConfigureActivity.launcherInvoked = false;
                });
    }

    private void updateQuotationsUI() {
        QuotationsPreferences quotationsPreferences = new QuotationsPreferences(widgetId, getContext());
        quotationsPreferences.setContentSelection(ContentSelection.ALL);
        quotationsPreferences.setContentSelectionAuthorCount(-1);
        quotationsPreferences.setContentSelectionAuthor("");
    }
}
