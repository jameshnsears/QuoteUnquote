package com.github.jameshnsears.quoteunquote.configure.fragment.archive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
public class ArchiveFragment extends FragmentCommon {
    @NonNull
    public static String ENABLE_BUTTON_BACKUP = "ENABLE_BUTTON_BACKUP";

    @NonNull
    public static String ENABLE_BUTTON_RESTORE = "ENABLE_BUTTON_RESTORE";

    @Nullable
    public FragmentArchiveBinding fragmentArchiveBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    protected ArchivePreferences archivePreferences;

    @Nullable
    private BroadcastReceiver receiver;

    @Nullable
    private ActivityResultLauncher<Intent> storageAccessFrameworkActivityResultBackup;

    @Nullable
    private ActivityResultLauncher<Intent> storageAccessFrameworkActivityResultRestore;

    @NonNull
    public Handler handler = new Handler(Looper.getMainLooper());

    public ArchiveFragment() {
        // dark mode support
    }

    public ArchiveFragment(final int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static ArchiveFragment newInstance(final int widgetId) {
        final ArchiveFragment fragment = new ArchiveFragment(widgetId);
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
        filterRefreshUpdate.addAction(ENABLE_BUTTON_BACKUP);
        filterRefreshUpdate.addAction(ENABLE_BUTTON_RESTORE);
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

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ENABLE_BUTTON_BACKUP)) {
                    enableButtonBackupDependingUponDatabaseState();
                }
                if (intent.getAction().equals(ENABLE_BUTTON_RESTORE)) {
                    enableButton(fragmentArchiveBinding.buttonRestore, true);
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
        archivePreferences = new ArchivePreferences(widgetId, getContext());

        fragmentArchiveBinding = FragmentArchiveBinding.inflate(getLayoutInflater());
        return fragmentArchiveBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull final View view, @NonNull final Bundle savedInstanceState) {
        setSelectedArchive();

        createListenerGoogleCloud();
        createListenerSharedStorage();

        createListenerFavouriteButtonBackup();
        createListenerFavouriteButtonRestore();

        setTransferLocalCode();

        enableButtonBackupDependingUponDatabaseState();

        enableButtonsDependingUponServiceState();

        storageAccessFrameworkBackupResult();
        storageAccessFrameworkRestoreResult();
    }

    private void createListenerSharedStorage() {
        RadioButton radioButtonSharedStorage = fragmentArchiveBinding.radioButtonSharedStorage;
        radioButtonSharedStorage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (archivePreferences.getArchiveSharedStorage() != isChecked) {
                archivePreferences.setArchiveGoogleCloud(false);
                archivePreferences.setArchiveSharedStorage(true);
            }
        });
    }

    private void createListenerGoogleCloud() {
        RadioButton radioButtonGoogleCloud = fragmentArchiveBinding.radioButtonCloud;
        radioButtonGoogleCloud.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (archivePreferences.getArchiveGoogleCloud() != isChecked) {
                archivePreferences.setArchiveGoogleCloud(true);
                archivePreferences.setArchiveSharedStorage(false);            }
        });
    }

    private void setSelectedArchive() {
        fragmentArchiveBinding.radioButtonCloud.setChecked(archivePreferences.getArchiveGoogleCloud());
        fragmentArchiveBinding.radioButtonSharedStorage.setChecked(archivePreferences.getArchiveSharedStorage());
    }

    public void enableButtonBackupDependingUponDatabaseState() {
        enableButton(fragmentArchiveBinding.buttonBackup, quoteUnquoteModel.countPrevious(widgetId) != 0);
    }

    private void enableButtonsDependingUponServiceState() {
        if (CloudService.isRunning) {
            enableButton(fragmentArchiveBinding.buttonBackup, false);
        }

        if (CloudService.isRunning) {
            enableButton(fragmentArchiveBinding.buttonRestore, false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentArchiveBinding = null;
    }

    protected void setTransferLocalCode() {
        if ("".equals(archivePreferences.getTransferLocalCode())) {
            // possible that user wiped storage via App Info settings
            archivePreferences.setTransferLocalCode(CloudTransferHelper.getLocalCode());
        }

        fragmentArchiveBinding.textViewLocalCodeValue.setText(archivePreferences.getTransferLocalCode());
    }

    protected void createListenerFavouriteButtonBackup() {
        fragmentArchiveBinding.buttonBackup.setOnClickListener(v -> {
            if (fragmentArchiveBinding.buttonBackup.isEnabled()) {

                if (archivePreferences.getArchiveGoogleCloud()) {
                    backupGoogleCloud();
                } else {
                    backupSharedStorage();
                }
            }
        });
    }

    private void backupSharedStorage() {
        ConfigureActivity.safCalled = true;

        final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fragmentArchiveBinding.textViewLocalCodeValue.getText().toString());
        storageAccessFrameworkActivityResultBackup.launch(intent);
    }

    protected final void storageAccessFrameworkBackupResult() {
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

                        ConfigureActivity.safCalled = false;

                        enableButtons(true);
                    }
                });
    }

    private void restoreSharedStorage() {
        ConfigureActivity.safCalled = true;

        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fragmentArchiveBinding.textViewLocalCodeValue.getText().toString());
        storageAccessFrameworkActivityResultRestore.launch(intent);
    }

    private void storageAccessFrameworkRestoreResult() {
        // default: /storage/emulated/0/Download/<10 character code>
        storageAccessFrameworkActivityResultRestore = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    Timber.d("%d", activityResult.getResultCode());

                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        try {
                            final String jsonString = getJsonRestore(activityResult);

                            Boolean isJsonValid = isJsonRestoreValid(jsonString);

                            final Transfer transfer
                                    = new Gson().fromJson(jsonString, Transfer.class);

                            if (isJsonValid && transfer != null) {
                                restoreJsonSaf(transfer);

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

                    ConfigureActivity.safCalled = false;

                    enableButtons(true);
                });
    }

    private void restoreJsonSaf(Transfer transfer) {
        final Future future = QuoteUnquoteWidget.getExecutorService().submit(() -> {
            ArchivePreferences archivePreferences = new ArchivePreferences(widgetId, getContext());
            boolean googleCloudRadio = archivePreferences.getArchiveGoogleCloud();
            boolean sharedStorageRadio = archivePreferences.getArchiveSharedStorage();

            TransferRestore transferRestore = new TransferRestore();
            transferRestore.restore(
                    getContext(),
                    DatabaseRepository.getInstance(getContext()),
                    transfer);

            archivePreferences.setArchiveGoogleCloud(googleCloudRadio);
            archivePreferences.setArchiveSharedStorage(sharedStorageRadio);
        });

        try {
            future.get();
        } catch (@NonNull ExecutionException | InterruptedException e) {
            Timber.e(e);
            Thread.currentThread().interrupt();
        }
    }

    @NonNull
    private Boolean isJsonRestoreValid(String jsonString) {
        final Future<Boolean> future = QuoteUnquoteWidget.getExecutorService().submit(()
                -> ArchiveJsonSchemaValidation.Companion.isJsonValid(getContext(), jsonString));

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
    private String getJsonRestore(ActivityResult activityResult) throws IOException {
        final ParcelFileDescriptor parcelFileDescriptor
                = getContext().getContentResolver().openFileDescriptor(
                activityResult.getData().getData(), "r");

        final FileInputStream fileInputStream
                = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

        final String jsonString = CharStreams.toString(new InputStreamReader(
                fileInputStream, Charsets.UTF_8));

        fileInputStream.close();
        parcelFileDescriptor.close();
        return jsonString;
    }

    private void backupGoogleCloud() {
        Timber.d("ArchiveGoogleCloud");

        enableButtons(false);

        final Intent serviceIntent = new Intent(getContext(), CloudServiceBackup.class);
        serviceIntent.putExtra("asJson", quoteUnquoteModel.transferBackup(getContext()));
        serviceIntent.putExtra(
                "localCodeValue", fragmentArchiveBinding.textViewLocalCodeValue.getText().toString());

        getContext().startService(serviceIntent);
    }

    protected void createListenerFavouriteButtonRestore() {
        fragmentArchiveBinding.buttonRestore.setOnClickListener(v -> {
            if (fragmentArchiveBinding.buttonRestore.isEnabled()) {
                if (archivePreferences.getArchiveGoogleCloud()) {
                    restoreGoogleCloud();
                } else {
                    restoreSharedStorage();
                }
            }
        });
    }

    private void restoreGoogleCloud() {
        Timber.d("remoteCode=%s", fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString());

        // correct length?
        if (fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.fragment_archive_restore_token_missing),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // crc wrong?
        if (!CloudTransferHelper.isRemoteCodeValid(fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString())) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.fragment_archive_restore_token_invalid),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        final Intent serviceIntent = new Intent(getContext(), CloudServiceRestore.class);
        serviceIntent.putExtra(
                "remoteCodeValue", fragmentArchiveBinding.editTextRemoteCodeValue.getText().toString());
        serviceIntent.putExtra("widgetId", widgetId);

        getContext().startService(serviceIntent);
    }

    private void enableButtons(@NonNull final Boolean enabled) {
        enableButton(fragmentArchiveBinding.buttonBackup, enabled);
        enableButton(fragmentArchiveBinding.buttonRestore, enabled);
    }

    public void enableButton(@NonNull final Button button, @NonNull final Boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1 : 0.25f);
    }
}
