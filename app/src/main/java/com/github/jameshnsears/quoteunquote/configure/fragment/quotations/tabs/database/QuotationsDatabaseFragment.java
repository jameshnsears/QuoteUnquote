package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.database;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.core.content.ContextCompat;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsFragmentStateAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.QuotationsFilterFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseBinding;
import com.github.jameshnsears.quoteunquote.utils.CSVHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;

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

    @Nullable
    private QuotationsFilterFragment quotationsFilterFragment;

    public QuotationsDatabaseFragment() {
        // dark mode support
    }

    public QuotationsDatabaseFragment(int widgetId,
                                      QuotationsFilterFragment quotationsFilterFragment) {
        super(widgetId);
        this.quotationsFilterFragment = quotationsFilterFragment;
    }

    @NonNull
    public static QuotationsDatabaseFragment newInstance(
            int widgetId,
            QuotationsFilterFragment quotationsFilterFragment) {
        QuotationsDatabaseFragment fragment = new QuotationsDatabaseFragment(
                widgetId, quotationsFilterFragment);
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
        this.createListenerRadioExternal();
        this.createListenerButtonImport();
        this.createListenerSwitchWatch();

        this.setHandleImport();

        setSwitchWatch();
    }

    private void setSwitchWatch() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setVisibility(VISIBLE);

            if (quotationsPreferences.getDatabaseExternalWatch()) {
                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setChecked(true);
            } else {
                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setChecked(false);
            }

            if (quotationsPreferences.getDatabaseInternal()) {
                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setEnabled(false);
            } else {
                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setEnabled(true);
            }
        }
    }

    private void createListenerSwitchWatch() {
        fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (buttonView.isPressed()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(
                            getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {

                        ConfigureActivity.launcherInvoked = true;
                        requestPermissionLauncher.launch(
                                Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }

                quotationsPreferences.setDatabaseExternalWatch(isChecked);
            }
        });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isPermissionAllowed -> {
                if (!isPermissionAllowed) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.fragment_quotations_database_external_watch_permission),
                            Toast.LENGTH_LONG).show();

                    fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setChecked(false);
                    quotationsPreferences.setDatabaseExternalWatch(false);
                } else {
                    quotationsPreferences.setDatabaseExternalWatch(true);
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.fragmentQuotationsTabDatabaseBinding = null;
    }

    private void setDatabase() {
        if (this.quotationsPreferences.getDatabaseInternal()) {
            this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(true);
            this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setChecked(false);
            this.quotationsPreferences.setDatabaseInternal(true);
            this.quotationsPreferences.setDatabaseExternal(false);

            DatabaseRepository.useInternalDatabase = true;
        } else {
            this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(false);
            this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setChecked(true);
            this.quotationsPreferences.setDatabaseInternal(false);
            this.quotationsPreferences.setDatabaseExternal(true);

            DatabaseRepository.useInternalDatabase = false;
        }

        if (quoteUnquoteModel.externalDatabaseContainsQuotations()) {
            this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setEnabled(true);
        } else {
            this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setEnabled(false);
        }
    }

    protected void createListenerButtonImport() {
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
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(true);
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setChecked(false);
                this.quotationsPreferences.setDatabaseInternal(true);
                this.quotationsPreferences.setDatabaseExternal(false);

                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setEnabled(false);

                DatabaseRepository.useInternalDatabase = true;

                updateQuotationsUI();
            }
        });
    }

    private void createListenerRadioExternal() {
        final RadioButton radioButtonDatabaseCSV = this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal;
        radioButtonDatabaseCSV.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseInternal.setChecked(false);
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setChecked(true);
                this.fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setEnabled(true);

                this.quotationsPreferences.setDatabaseInternal(false);
                this.quotationsPreferences.setDatabaseExternal(true);

                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setEnabled(true);

                DatabaseRepository.useInternalDatabase = false;

                updateQuotationsUI();
            }
        });
    }

    private void setHandleImport() {
        // default: /storage/emulated/0/Download/
        this.storageAccessFrameworkActivityResultCSV = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    Timber.d("%d", activityResult.getResultCode());

                    if (activityResult.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast.makeText(
                                this.getContext(),
                                this.getContext().getString(R.string.fragment_quotations_database_import_no_csv_selected),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (activityResult.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(
                                    this.getContext(),
                                    this.getContext().getString(R.string.fragment_quotations_database_import_importing),
                                    Toast.LENGTH_SHORT).show();

                            stopExternalObserver();

                            ParcelFileDescriptor parcelFileDescriptor = null;
                            FileInputStream fileInputStream = null;

                            try {
                                parcelFileDescriptor = this.getContext().getContentResolver().openFileDescriptor(
                                        activityResult.getData().getData(), "r");

                                fileInputStream
                                        = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                                final CSVHelper csvHelper = new CSVHelper();
                                final LinkedHashSet<QuotationEntity> quotations = csvHelper.csvImportDatabase(fileInputStream);

                                quoteUnquoteModel.insertQuotationsExternal(quotations);

                                fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setEnabled(true);
                                fragmentQuotationsTabDatabaseBinding.radioButtonDatabaseExternal.setChecked(true);

                                quotationsPreferences.setContentSelectionSearchCount(0);
                                quotationsPreferences.setContentSelectionSearch("");

                                DatabaseRepository.useInternalDatabase = false;

                                File file = new File("/proc/self/fd/" + parcelFileDescriptor.getFd());
                                quotationsPreferences.setDatabaseExternalPath(file.getCanonicalPath());

                                fragmentQuotationsTabDatabaseBinding.switchExternalWatch.setEnabled(true);

                                updateQuotationsUI();

                                Toast.makeText(
                                        this.getContext(),
                                        this.getContext().getString(R.string.fragment_quotations_database_import_success),
                                        Toast.LENGTH_SHORT).show();

                            } catch (final CSVHelper.CVSHelperException | IOException e) {
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
                    }
                });
    }

    public void stopExternalObserver() {
        if (fragmentQuotationsTabDatabaseBinding.switchExternalWatch.isChecked()) {

            try {
                if (QuoteUnquoteWidget.externalObserver != null) {
                    Timber.d("ExternalObserver.stop.request");
                    QuoteUnquoteWidget.externalObserver.cancel(true);
                    Thread.sleep(QuoteUnquoteWidget.externalObserverInternal);
                    QuoteUnquoteWidget.externalObserver = null;
                }
            } catch (InterruptedException e) {
                Timber.e(e.getMessage());
            }
        }
    }

    private void updateQuotationsUI() {
        QuotationsFragmentStateAdapter.alignSelectionFragmentWithSelectedDatabase(widgetId, getContext());
        quotationsFilterFragment.shutdown();
        quotationsFilterFragment.initUi();
    }
}
