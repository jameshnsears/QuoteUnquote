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
import com.github.jameshnsears.quoteunquote.databinding.FragmentArchiveBinding;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

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
    public FragmentArchiveBinding fragmentArchiveBinding;

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

        quoteUnquoteModel = new QuoteUnquoteModel(getContext());

        if (quoteUnquoteModel.countPrevious(widgetId) == 0) {
            quoteUnquoteModel.markAsCurrentDefault(widgetId);
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CLOUD_SERVICE_COMPLETED)) {
                    enableUI(true);
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

        fragmentArchiveBinding = FragmentArchiveBinding.inflate(getLayoutInflater());
        return fragmentArchiveBinding.getRoot();
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

        handleDeviceBackupResult();
        handleDeviceRestoreResult();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentArchiveBinding = null;
    }

    private void setSyncFields() {
        if (CloudService.isRunning) {
            enableUI(false);
            return;
        }

        if (syncPreferences.getArchiveGoogleCloud()) {
            fragmentArchiveBinding.radioButtonGoogleCloud.setChecked(true);
            fragmentArchiveBinding.radioButtonDevice.setChecked(false);
            fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(true);
            fragmentArchiveBinding.editTextRemoteCodeValue.setText("");
        } else {
            fragmentArchiveBinding.radioButtonGoogleCloud.setChecked(false);
            fragmentArchiveBinding.radioButtonDevice.setChecked(true);
            fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(false);
            fragmentArchiveBinding.editTextRemoteCodeValue.setText("");
        }
    }

    private void createListenerRadioGoogleCloud() {
        RadioButton radioButtonGoogleCloud = fragmentArchiveBinding.radioButtonGoogleCloud;
        radioButtonGoogleCloud.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                syncPreferences.setArchiveGoogleCloud(true);
                syncPreferences.setArchiveSharedStorage(false);

                fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(true);
                fragmentArchiveBinding.editTextRemoteCodeValue.setText("");
            }
        });
    }

    private void createListenerRadioDevice() {
        RadioButton radioButtonDevice = fragmentArchiveBinding.radioButtonDevice;
        radioButtonDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                syncPreferences.setArchiveGoogleCloud(false);
                syncPreferences.setArchiveSharedStorage(true);

                fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(false);
                fragmentArchiveBinding.editTextRemoteCodeValue.setText("");
            }
        });
    }

    protected void setLocalCode() {
        if ("".equals(syncPreferences.getTransferLocalCode())) {
            // possible that user wiped storage via App Info settings
            syncPreferences.setTransferLocalCode(CloudTransferHelper.getLocalCode());
        }

        fragmentArchiveBinding.textViewLocalCodeValue.setText(syncPreferences.getTransferLocalCode());
    }

    protected void createListenerButtonBackup() {
        fragmentArchiveBinding.buttonBackup.setOnClickListener(v -> {
            enableUI(false);

            if (syncPreferences.getArchiveGoogleCloud()) {
                fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(false);
                backupGoogleCloud();
            } else {
                backupSharedStorage();
            }
        });
    }

    private void backupSharedStorage() {
        ConfigureActivity.safCalled = true;

        final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // API 25 doesn't save the extension!
        intent.setType("application/json");

        intent.putExtra(Intent.EXTRA_TITLE, fragmentArchiveBinding.textViewLocalCodeValue.getText().toString());
        storageAccessFrameworkActivityResultBackup.launch(intent);
    }

    private final void handleDeviceBackupResult() {
        // default: /storage/emulated/0/Download/<10 character code>.json
        storageAccessFrameworkActivityResultBackup = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        try {
                            final ParcelFileDescriptor parcelFileDescriptor
                                    = getContext().getContentResolver().openFileDescriptor(
                                    activityResult.getData().getData(), "w");
                            final FileOutputStream fileOutputStream
                                    = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                            final String exportableString = quoteUnquoteModel.transferBackup(getContext());
                            fileOutputStream.write(exportableString.getBytes());

                            fileOutputStream.close();
                            parcelFileDescriptor.close();

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.fragment_archive_backup_success),
                                    Toast.LENGTH_SHORT).show();
                        } catch (final IOException e) {
                            Timber.e(e.getMessage());
                        }
                    }

                    enableUI(true);
                    ConfigureActivity.safCalled = false;
                });
    }

    private void restoreDevice() {
        ConfigureActivity.safCalled = true;

        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fragmentArchiveBinding.textViewLocalCodeValue.getText().toString());
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

                        if (activityResult.getResultCode() == Activity.RESULT_OK) {
                            try {
                                final String jsonString = getRestoreJson(activityResult);

                                Boolean isJsonValid = isRestoreJsonValid(jsonString);

                                final Transfer transfer
                                        = new Gson().fromJson(jsonString, Transfer.class);

                                if (isJsonValid && transfer != null) {
                                    restoreDeviceJson(transfer);

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
                    }

                    ConfigureActivity.safCalled = false;
                    enableUI(true);
                });
    }

    private void restoreDeviceJson(Transfer transfer) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            SyncPreferences syncPreferences = new SyncPreferences(widgetId, getContext());
            boolean googleCloudRadio = syncPreferences.getArchiveGoogleCloud();
            boolean sharedStorageRadio = syncPreferences.getArchiveSharedStorage();

            TransferRestore transferRestore = new TransferRestore();
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

    private void backupGoogleCloud() {
        final Intent serviceIntent = new Intent(getContext(), CloudServiceBackup.class);
        serviceIntent.putExtra("asJson", quoteUnquoteModel.transferBackup(getContext()));
        serviceIntent.putExtra(
                "localCodeValue", fragmentArchiveBinding.textViewLocalCodeValue.getText().toString());

        getContext().startService(serviceIntent);
    }

    protected void createListenerButtonRestore() {
        fragmentArchiveBinding.buttonRestore.setOnClickListener(v -> {
            enableUI(false);

            if (syncPreferences.getArchiveGoogleCloud()) {
                fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(false);
                restoreGoogleCloud();
            } else {
                ConfigureActivity.safCalled = true;
                restoreDevice();
            }
        });
    }

    public void enableUI(boolean enableUI) {
        fragmentArchiveBinding.radioButtonGoogleCloud.setEnabled(enableUI);
        fragmentArchiveBinding.radioButtonDevice.setEnabled(enableUI);
        fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(enableUI);
        enableButton(fragmentArchiveBinding.buttonBackup, enableUI);
        enableButton(fragmentArchiveBinding.buttonRestore, enableUI);

        if (syncPreferences.getArchiveGoogleCloud()) {
            fragmentArchiveBinding.radioButtonGoogleCloud.setChecked(true);
            fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(true);
        } else {
            fragmentArchiveBinding.radioButtonDevice.setChecked(true);
            fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(false);
        }

        if (quoteUnquoteModel.countPrevious(widgetId) == 0) {
            fragmentArchiveBinding.buttonBackup.setEnabled(false);
            enableButton(fragmentArchiveBinding.buttonBackup, false);
        }
    }

    private void restoreGoogleCloud() {
        Timber.d("remoteCode=%s", fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString());

        // correct length?
        if (fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.fragment_archive_restore_token_missing),
                    Toast.LENGTH_SHORT).show();
            enableUI(true);
            return;
        }

        // crc wrong?
        if (!CloudTransferHelper.isRemoteCodeValid(fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString())) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.fragment_archive_restore_token_invalid),
                    Toast.LENGTH_SHORT).show();
            enableUI(true);
            return;
        }

        fragmentArchiveBinding.radioButtonDevice.setEnabled(false);
        fragmentArchiveBinding.editTextRemoteCodeValue.setEnabled(false);

        final Intent serviceIntent = new Intent(getContext(), CloudServiceRestore.class);
        serviceIntent.putExtra(
                "remoteCodeValue", fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString());
        serviceIntent.putExtra("widgetId", widgetId);

        getContext().startService(serviceIntent);
    }

    public void enableButton(@NonNull final Button button, @NonNull final Boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1 : 0.25f);
    }
}
