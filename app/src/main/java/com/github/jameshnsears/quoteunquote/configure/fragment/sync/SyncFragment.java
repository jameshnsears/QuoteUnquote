package com.github.jameshnsears.quoteunquote.configure.fragment.sync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.cloud.CloudService;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceBackup;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceRestore;
import com.github.jameshnsears.quoteunquote.cloud.CloudTransferHelper;
import com.github.jameshnsears.quoteunquote.cloud.transfer.Transfer;
import com.github.jameshnsears.quoteunquote.cloud.transfer.restore.TransferRestore;
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.databinding.FragmentSyncBinding;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import timber.log.Timber;

@Keep
public class SyncFragment extends FragmentCommon {
    @NonNull
    public static String CLOUD_SERVICE_COMPLETED = "CLOUD_SERVICE_COMPLETED";

    @Nullable
    public FragmentSyncBinding fragmentSyncBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    protected SyncPreferences syncPreferences;

    @Nullable
    private BroadcastReceiver receiver;

    @Nullable
    private ActivityResultLauncher<Intent> storageAccessFrameworkActivityResultBackup;

    @Nullable
    private ActivityResultLauncher<Intent> storageAccessFrameworkActivityResultRestore;

    @NonNull
    private TransferRestore transferRestore = new TransferRestore();

    public SyncFragment() {
        // dark mode support
    }

    public SyncFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static SyncFragment newInstance(final int widgetId) {
        final SyncFragment fragment = new SyncFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerButtonIntentReceiver();
    }

    private void registerButtonIntentReceiver() {
        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(CLOUD_SERVICE_COMPLETED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filterRefreshUpdate);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(widgetId, getContext());

        if (quoteUnquoteModel.countPrevious(widgetId) == 0) {
            quoteUnquoteModel.markAsCurrentDefault(widgetId);
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CLOUD_SERVICE_COMPLETED)) {
                    enableUI(true);

                    alignLocalCodeWithRestoredcode();
                }
            }
        };
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @NonNull final ViewGroup container,
            @NonNull final Bundle savedInstanceState) {
        syncPreferences = new SyncPreferences(widgetId, getContext());

        fragmentSyncBinding = FragmentSyncBinding.inflate(getLayoutInflater());
        return fragmentSyncBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        setSyncFields();

        setLocalCode();

        createListenerRadioGoogleCloud();
        createListenerRadioDevice();
        createListenerButtonBackup();
        createListenerButtonRestore();
        createListenerButtonNewCode();

        handleDeviceBackupResult();
        handleDeviceRestoreResult();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentSyncBinding = null;
    }

    private void setSyncFields() {
        if (CloudService.isRunning) {
            enableUI(false);
            return;
        }

        if (syncPreferences.getArchiveGoogleCloud()) {
            fragmentSyncBinding.radioButtonSyncGoogleCloud.setChecked(true);
            fragmentSyncBinding.radioButtonSyncDevice.setChecked(false);
            fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(true);
            fragmentSyncBinding.editTextRemoteCodeValue.setText("");
        } else {
            fragmentSyncBinding.radioButtonSyncGoogleCloud.setChecked(false);
            fragmentSyncBinding.radioButtonSyncDevice.setChecked(true);
            fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(false);
            fragmentSyncBinding.editTextRemoteCodeValue.setText("");
        }
    }

    private void createListenerRadioGoogleCloud() {
        RadioButton radioButtonGoogleCloud = fragmentSyncBinding.radioButtonSyncGoogleCloud;
        radioButtonGoogleCloud.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                syncPreferences.setArchiveGoogleCloud(true);
                syncPreferences.setArchiveSharedStorage(false);

                fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(true);
                fragmentSyncBinding.editTextRemoteCodeValue.setText("");
            }
        });
    }

    private void createListenerRadioDevice() {
        RadioButton radioButtonDevice = fragmentSyncBinding.radioButtonSyncDevice;
        radioButtonDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                syncPreferences.setArchiveGoogleCloud(false);
                syncPreferences.setArchiveSharedStorage(true);

                fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(false);
                fragmentSyncBinding.editTextRemoteCodeValue.setText("");
            }
        });
    }

    protected void setLocalCode() {
        if ("".equals(syncPreferences.getTransferLocalCode())) {
            // possible that user wiped storage via App Info settings
            syncPreferences.setTransferLocalCode(CloudTransferHelper.getLocalCode());
        }

        fragmentSyncBinding.textViewLocalCodeValue.setText(syncPreferences.getTransferLocalCode());
    }

    protected void createListenerButtonBackup() {
        fragmentSyncBinding.buttonBackup.setOnClickListener(v -> {
            enableUI(false);

            if (syncPreferences.getArchiveGoogleCloud()) {
                fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(false);
                backupGoogleCloud();
            } else {
                backupSharedStorage();
            }
        });
    }

    private void backupSharedStorage() {
        ConfigureActivity.launcherInvoked = true;

        final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // API 25 doesn't save the extension!
        intent.setType("application/json");

        intent.putExtra(Intent.EXTRA_TITLE, fragmentSyncBinding.textViewLocalCodeValue.getText().toString());
        storageAccessFrameworkActivityResultBackup.launch(intent);
    }

    private void handleDeviceBackupResult() {
        // default: /storage/emulated/0/Download/<10 character code>.json
        storageAccessFrameworkActivityResultBackup = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        ParcelFileDescriptor parcelFileDescriptor = null;
                        FileOutputStream fileOutputStream = null;
                        try {
                            parcelFileDescriptor
                                    = getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "w");
                            fileOutputStream
                                    = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                            final String exportableString = quoteUnquoteModel.transferBackup(getContext());
                            fileOutputStream.write(exportableString.getBytes());

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_archive_backup_success),
                                    Toast.LENGTH_SHORT).show();
                        } catch (final IOException e) {
                            Timber.e(e.getMessage());
                        } finally {
                            try {
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                            } catch (final IOException e) {
                                Timber.e(e.getMessage());
                            }
                        }
                    }

                    enableUI(true);
                    ConfigureActivity.launcherInvoked = false;
                });
    }

    private void restoreDevice() {
        ConfigureActivity.launcherInvoked = true;

        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fragmentSyncBinding.textViewLocalCodeValue.getText().toString());
        storageAccessFrameworkActivityResultRestore.launch(intent);
    }

    private void handleDeviceRestoreResult() {
        // default: /storage/emulated/0/Download/<10 character code>
        storageAccessFrameworkActivityResultRestore = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    Timber.d("%d", activityResult.getResultCode());

                    if (activityResult.getResultCode() == Activity.RESULT_CANCELED) {
                        Timber.d("cancelled");
                    } else {

                        try {
                            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                                try {
                                    final String jsonString = getRestoreJson(activityResult);

                                    Boolean isJsonValid = isRestoreJsonValid(jsonString);

                                    final Transfer transfer
                                            = new Gson().fromJson(jsonString, Transfer.class);

                                    if (isJsonValid && transfer != null) {
                                        restoreDeviceJson(transfer);

                                        quoteUnquoteModel.alignHistoryWithQuotations(widgetId);

                                        DatabaseRepository.useInternalDatabase = true;

                                        alignLocalCodeWithRestoredcode();

                                        Toast.makeText(
                                                getContext(),
                                                getContext().getString(R.string.fragment_archive_restore_success),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(
                                                getContext(),
                                                getContext().getString(R.string.fragment_archive_restore_saf_invalid),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (final IOException e) {
                                    Timber.e(e.getMessage());
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_archive_restore_saf_invalid),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    ConfigureActivity.launcherInvoked = false;
                    enableUI(true);
                });
    }

    private void restoreDeviceJson(Transfer transfer) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            SyncPreferences syncPreferences = new SyncPreferences(widgetId, getContext());
            boolean googleCloudRadio = syncPreferences.getArchiveGoogleCloud();
            boolean sharedStorageRadio = syncPreferences.getArchiveSharedStorage();

            transferRestore.restore(
                    getContext(),
                    DatabaseRepository.getInstance(getContext()),
                    transfer);

            syncPreferences.setArchiveGoogleCloud(googleCloudRadio);
            syncPreferences.setArchiveSharedStorage(sharedStorageRadio);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    private Boolean isRestoreJsonValid(String jsonString) {
        final Future<Boolean> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> SyncJsonSchemaValidation.Companion.isJsonValid(getContext(), jsonString));

        boolean isValid = false;

        try {
            isValid = future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }

        Timber.d("%b", isValid);
        return isValid;
    }

    @NonNull
    private String getRestoreJson(ActivityResult activityResult) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = null;
        FileInputStream fileInputStream = null;
        String jsonString = "";

        try {
            parcelFileDescriptor
                    = getContext().getContentResolver().openFileDescriptor(
                    activityResult.getData().getData(), "r");

            fileInputStream
                    = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

            jsonString = CharStreams.toString(new InputStreamReader(
                    fileInputStream, Charsets.UTF_8));
        } finally {
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }

            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return jsonString;
    }

    protected void createListenerButtonRestore() {
        fragmentSyncBinding.buttonRestore.setOnClickListener(v -> {
            enableUI(false);

            if (syncPreferences.getArchiveGoogleCloud()) {
                fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(false);
                restoreGoogleCloud();
            } else {
                ConfigureActivity.launcherInvoked = true;
                restoreDevice();
            }
        });
    }

    protected void createListenerButtonNewCode() {
        fragmentSyncBinding.buttonNewCode.setOnClickListener(v -> {
            syncPreferences.setTransferLocalCode(CloudTransferHelper.generateNewCode());

            fragmentSyncBinding.textViewLocalCodeValue.setText(syncPreferences.getTransferLocalCode());
            fragmentSyncBinding.textViewLocalCodeValue.invalidate();
        });
    }

    public void enableUI(boolean enableUI) {
        fragmentSyncBinding.radioButtonSyncGoogleCloud.setEnabled(enableUI);
        fragmentSyncBinding.radioButtonSyncDevice.setEnabled(enableUI);
        fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(enableUI);
        enableButton(fragmentSyncBinding.buttonBackup, enableUI);
        enableButton(fragmentSyncBinding.buttonRestore, enableUI);
        enableButton(fragmentSyncBinding.buttonNewCode, enableUI);

        if (syncPreferences.getArchiveGoogleCloud()) {
            fragmentSyncBinding.radioButtonSyncGoogleCloud.setChecked(true);
            fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(true);
        } else {
            fragmentSyncBinding.radioButtonSyncDevice.setChecked(true);
            fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(false);
        }
    }

    private void backupGoogleCloud() {
        final Intent serviceIntent = new Intent(getContext(), CloudServiceBackup.class);
        serviceIntent.putExtra(
                "localCodeValue", fragmentSyncBinding.textViewLocalCodeValue.getText().toString());

        getContext().startService(serviceIntent);
    }

    private void restoreGoogleCloud() {
        Timber.d("remoteCode=%s", fragmentSyncBinding.editTextRemoteCodeValue.getText().toString());

        // correct length?
        if (fragmentSyncBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.fragment_archive_restore_token_missing),
                    Toast.LENGTH_SHORT).show();
            enableUI(true);
            return;
        }

        // crc wrong?
        if (!CloudTransferHelper.isRemoteCodeValid(fragmentSyncBinding.editTextRemoteCodeValue.getText().toString())) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.fragment_archive_restore_token_invalid),
                    Toast.LENGTH_SHORT).show();
            enableUI(true);
            return;
        }

        fragmentSyncBinding.radioButtonSyncDevice.setEnabled(false);
        fragmentSyncBinding.editTextRemoteCodeValue.setEnabled(false);

        final Intent serviceIntent = new Intent(getContext(), CloudServiceRestore.class);
        serviceIntent.putExtra(
                "remoteCodeValue", fragmentSyncBinding.editTextRemoteCodeValue.getText().toString());
        serviceIntent.putExtra("widgetId", widgetId);

        getContext().startService(serviceIntent);
    }

    public void enableButton(@NonNull final Button button, @NonNull final Boolean enabled) {
        button.setEnabled(enabled);
        makeButtonAlpha(button, enabled);
    }

    public void alignLocalCodeWithRestoredcode() {
        fragmentSyncBinding.textViewLocalCodeValue.setText(syncPreferences.getTransferLocalCode());
        fragmentSyncBinding.editTextRemoteCodeValue.setText("");
    }
}
