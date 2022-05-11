package com.github.jameshnsears.quoteunquote.configure.fragment.transfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceBackup;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceRestore;
import com.github.jameshnsears.quoteunquote.cloud.CloudTransferHelper;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.databinding.FragmentTransferBinding;

import timber.log.Timber;

@Keep
public class TransferFragment extends FragmentCommon {
    @Nullable
    public FragmentTransferBinding fragmentTransferBinding;

    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;

    @Nullable
    protected TransferPreferences transferPreferences;

    @Nullable
    private BroadcastReceiver receiver;

    @NonNull
    public static String ENABLE_BUTTON_BACKUP = "ENABLE_BUTTON_BACKUP";

    @NonNull
    public static String ENABLE_BUTTON_RESTORE = "ENABLE_BUTTON_RESTORE";

    public TransferFragment() {
        // dark mode support
    }

    public TransferFragment(int widgetId) {
        super(widgetId);
    }

    @NonNull
    public static TransferFragment newInstance(int widgetId) {
        TransferFragment fragment = new TransferFragment(widgetId);
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

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
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteModel = new QuoteUnquoteModel(getContext());

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ENABLE_BUTTON_BACKUP)) {
                    enableButtonBackupDependingUponDatabaseState();
                }
                if (intent.getAction().equals(ENABLE_BUTTON_RESTORE)) {
                    enableButton(fragmentTransferBinding.buttonRestore, true);
                }
            }
        };
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @NonNull Bundle savedInstanceState) {
        Intent intent = new Intent(getContext(), CloudServiceRestore.class);

        transferPreferences = new TransferPreferences(getContext());

        fragmentTransferBinding = FragmentTransferBinding.inflate(getLayoutInflater());
        return fragmentTransferBinding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @NonNull Bundle savedInstanceState) {

        createListenerFavouriteButtonBackup();
        createListenerFavouriteButtonRestore();

        setTransferLocalCode();

        enableButtonBackupDependingUponDatabaseState();

        enableButtonsDependingUponServiceState();
    }

    void enableButtonBackupDependingUponDatabaseState() {
        if (quoteUnquoteModel.countPrevious(widgetId) == 0) {
            enableButton(fragmentTransferBinding.buttonBackup, false);
        } else {
            enableButton(fragmentTransferBinding.buttonBackup, true);
        }
    }

    private void enableButtonsDependingUponServiceState() {
        if (CloudServiceBackup.isRunning) {
            enableButton(fragmentTransferBinding.buttonBackup, false);
        }

        if (CloudServiceRestore.isRunning) {
            enableButton(fragmentTransferBinding.buttonRestore, false);
        }
    }

    public void enableButton(@NonNull Button button, @NonNull Boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1 : 0.25f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentTransferBinding = null;
    }

    protected void setTransferLocalCode() {
        if ("".equals(transferPreferences.getTransferLocalCode())) {
            // possible that user wiped storage via App Info settings
            transferPreferences.setTransferLocalCode(CloudTransferHelper.getLocalCode());
        }

        fragmentTransferBinding.textViewLocalCodeValue.setText(transferPreferences.getTransferLocalCode());
    }

    protected void createListenerFavouriteButtonBackup() {
        fragmentTransferBinding.buttonBackup.setOnClickListener(v -> {
            if (fragmentTransferBinding.buttonBackup.isEnabled()) {

                enableButton(fragmentTransferBinding.buttonBackup, false);
                enableButton(fragmentTransferBinding.buttonRestore, false);

                Intent serviceIntent = new Intent(getContext(), CloudServiceBackup.class);
                serviceIntent.putExtra("asJson", quoteUnquoteModel.transferBackup(getContext()));
                serviceIntent.putExtra(
                        "localCodeValue", fragmentTransferBinding.textViewLocalCodeValue.getText().toString());

                getContext().startService(serviceIntent);
            }
        });
    }

    protected void createListenerFavouriteButtonRestore() {
        fragmentTransferBinding.buttonRestore.setOnClickListener(v -> {
            if (fragmentTransferBinding.buttonRestore.isEnabled()) {
                Timber.d("remoteCode=%s", fragmentTransferBinding.editTextRemoteCodeValue.getText().toString());

                // correct length?
                if (fragmentTransferBinding.editTextRemoteCodeValue.getText().toString().length() != 10) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.fragment_transfer_restore_token_missing),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // crc wrong?
                if (!CloudTransferHelper.isRemoteCodeValid(fragmentTransferBinding.editTextRemoteCodeValue.getText().toString())) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.fragment_transfer_restore_token_invalid),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                enableButton(fragmentTransferBinding.buttonBackup, false);
                enableButton(fragmentTransferBinding.buttonRestore, false);

                Intent serviceIntent = new Intent(getContext(), CloudServiceRestore.class);
                serviceIntent.putExtra(
                        "remoteCodeValue", fragmentTransferBinding.editTextRemoteCodeValue.getText().toString());

                getContext().startService(serviceIntent);

            }
        });
    }
}
