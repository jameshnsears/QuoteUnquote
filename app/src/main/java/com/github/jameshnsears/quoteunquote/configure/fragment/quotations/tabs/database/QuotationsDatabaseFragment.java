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
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsFragmentStateAdapter;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseBinding;
import com.github.jameshnsears.quoteunquote.utils.CSVHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public QuotationsDatabaseFragment() {
        // dark mode support
    }

    public QuotationsDatabaseFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static QuotationsDatabaseFragment newInstance(int widgetId) {
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
        this.createListenerRadioExternal();
        this.createListenerImportButton();

        this.setHandleImport();
    }

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

    protected void createListenerImportButton() {
        // invoke Storage Access Framework
        fragmentQuotationsTabDatabaseBinding.buttonImport.setOnClickListener(v -> {
            if (fragmentQuotationsTabDatabaseBinding.buttonImport.isEnabled()) {
                ConfigureActivity.safCalled = true;

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

                                updateQuotationsUI();

                                Toast.makeText(
                                        this.getContext(),
                                        this.getContext().getString(R.string.fragment_quotations_database_import_success),
                                        Toast.LENGTH_SHORT).show();
                            } catch (final CSVHelper.CVSHelperException | FileNotFoundException e) {
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

                        ConfigureActivity.safCalled = false;
                    }
                });
    }

    private void updateQuotationsUI() {
        QuotationsFragmentStateAdapter.alignSelectionFragmentWithSelectedDatabase(widgetId, getContext());
    }
}
