package com.github.jameshnsears.quoteunquote.configure.fragment.sync

import android.app.Application
import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceBackup
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceRestore
import com.github.jameshnsears.quoteunquote.cloud.CloudTransferHelper
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class SyncFragmentTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialSyncPreferences() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                val preferences = fragment.syncPreferences!!

                assertThat(
                    fragment.fragmentSyncBinding!!.radioButtonSyncGoogleCloud.isChecked,
                    equalTo(preferences.archiveGoogleCloud),
                )
                assertThat(
                    fragment.fragmentSyncBinding!!.radioButtonSyncDevice.isChecked,
                    equalTo(preferences.archiveSharedStorage),
                )
                assertThat(
                    fragment.fragmentSyncBinding!!.switchAutoCloudBackup.isChecked,
                    equalTo(preferences.autoCloudBackup),
                )
            }
        }
    }

    @Test
    fun changeSyncToDevice() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentSyncBinding!!.radioButtonSyncDevice.isChecked = true

                assertThat(fragment.syncPreferences!!.archiveSharedStorage, `is`(true))
                assertThat(fragment.syncPreferences!!.archiveGoogleCloud, `is`(false))

                assertThat(
                    fragment.fragmentSyncBinding!!.editTextRemoteCodeValueLayout.isEnabled,
                    `is`(false),
                )
            }
        }
    }

    @Test
    fun changeSyncToGoogleCloud() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentSyncBinding!!.radioButtonSyncGoogleCloud.isChecked = true

                assertThat(fragment.syncPreferences!!.archiveGoogleCloud, `is`(true))
                assertThat(fragment.syncPreferences!!.archiveSharedStorage, `is`(false))

                assertThat(
                    fragment.fragmentSyncBinding!!.editTextRemoteCodeValueLayout.isEnabled,
                    `is`(true),
                )
            }
        }
    }

    @Test
    fun clickNewCode() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                val oldCode = fragment.syncPreferences!!.transferLocalCode
                fragment.fragmentSyncBinding!!.buttonNewCode.performClick()

                val newCode = fragment.syncPreferences!!.transferLocalCode
                assertThat(newCode, not(equalTo(oldCode)))
                assertThat(
                    fragment.fragmentSyncBinding!!
                        .textViewLocalCodeValue.text
                        .toString(),
                    equalTo(newCode),
                )
            }
        }
    }

    @Test
    fun backupGoogleCloud() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.syncPreferences!!.archiveGoogleCloud = true
                fragment.fragmentSyncBinding!!.buttonBackup.performClick()

                val shadowApp = shadowOf(fragment.requireActivity().application)
                val intent = shadowApp.nextStartedService
                assertThat(intent.component?.className, equalTo(CloudServiceBackup::class.java.name))
                assertThat(
                    intent.getStringExtra("localCodeValue"),
                    equalTo(
                        fragment.fragmentSyncBinding!!
                            .textViewLocalCodeValue.text
                            .toString(),
                    ),
                )
            }
        }
    }

    @Test
    fun restoreGoogleCloud() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.syncPreferences!!.archiveGoogleCloud = true
                val validCode = CloudTransferHelper.generateNewCode()
                fragment.fragmentSyncBinding!!.editTextRemoteCodeValue.setText(validCode)

                fragment.fragmentSyncBinding!!.buttonRestore.performClick()

                val shadowApp = shadowOf(fragment.requireActivity().application)
                val intent = shadowApp.nextStartedService
                assertThat(intent.component?.className, equalTo(CloudServiceRestore::class.java.name))
                assertThat(intent.getStringExtra("remoteCodeValue"), equalTo(validCode))
            }
        }
    }

    @Test
    fun receiveCloudServiceCompleted() {
        launchFragment<SyncFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.enableUI(false)
                assertThat(fragment.fragmentSyncBinding!!.buttonBackup.isEnabled, `is`(false))

                com.github.jameshnsears.quoteunquote.cloud.CloudEventBus.post(SyncFragment.CLOUD_SERVICE_COMPLETED)

                ShadowLooper.idleMainLooper()

                assertThat(fragment.fragmentSyncBinding!!.buttonBackup.isEnabled, `is`(true))
            }
        }
    }
}
