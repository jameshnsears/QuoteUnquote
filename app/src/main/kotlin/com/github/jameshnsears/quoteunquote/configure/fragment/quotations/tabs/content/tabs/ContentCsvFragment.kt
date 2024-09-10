package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseTabCsvBinding
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.ImportHelper.ImportHelperException
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class ContentCsvFragment(widgetId: Int) : ContentFragment(widgetId) {
    private var _binding: FragmentQuotationsTabDatabaseTabCsvBinding? = null

    private val fragmentQuotationsTabDatabaseTabCsvBinding get() = _binding!!

    var quotationsPreferences: QuotationsPreferences? = null

    private var storageAccessFrameworkActivityResultCSV: ActivityResultLauncher<Intent>? = null

    private val _stateFlow = MutableStateFlow(false)
    private val stateFlow: StateFlow<Boolean> = _stateFlow

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        this.quotationsPreferences = QuotationsPreferences(
            this.widgetId,
            this.requireContext(),
        )

        // https://developer.android.com/develop/ui/compose/migrate/strategy
        // https://developer.android.com/codelabs/basic-android-kotlin-training-compose-add-compose-to-a-view-based-app
        // https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views
        _binding = FragmentQuotationsTabDatabaseTabCsvBinding.inflate(inflater, container, false)
        val view = fragmentQuotationsTabDatabaseTabCsvBinding.root
//        fragmentQuotationsTabDatabaseTabCsvBinding.composeViewCsv.apply {
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//
//            _stateFlow.value = quotationsPreferences!!.databaseExternalCsv
//            setContent {
//                MaterialTheme {
//                    Greeting(
//                        stateFlow,
//                        ":-)",
//                    )
//                }
//            }
//        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated..ContentCsvFragment")

        if (quotationsPreferences!!.databaseExternalCsv) {
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                true
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                true
        } else {
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                false
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                false
        }

        createListenerRadioCsv()

        setHandleImportCsv()

        createListenerButtonImportCsv()
    }

    private fun createListenerRadioCsv() {
        val radioButtonDatabaseInternal =
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv
        radioButtonDatabaseInternal.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            usingCsv()
        }
    }

    private fun usingCsv() {
        quotationsPreferences!!.databaseInternal = false
        quotationsPreferences!!.databaseExternalCsv = true
        quotationsPreferences!!.databaseExternalWeb = false
        quotationsPreferences!!.databaseExternalContent = QuotationsPreferences.DATABASE_EXTERNAL
        DatabaseRepository.useInternalDatabase = false

        updateQuotationsUI()
    }

    protected fun createListenerButtonImportCsv() {
        // adb push app/src/androidTest/assets/Favourites.csv /sdcard/Download

        // invoke Storage Access Framework
        fragmentQuotationsTabDatabaseTabCsvBinding.buttonImport.setOnClickListener { v ->
            if (fragmentQuotationsTabDatabaseTabCsvBinding.buttonImport.isEnabled) {
                ConfigureActivity.launcherInvoked = true

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("text/comma-separated-values")
                this.storageAccessFrameworkActivityResultCSV!!.launch(intent)
            }
        }
    }

    private fun setHandleImportCsv() {
        // default: /storage/emulated/0/Download/
        this.storageAccessFrameworkActivityResultCSV =
            this.registerForActivityResult<Intent, ActivityResult>(
                ActivityResultContracts.StartActivityForResult(),
            ) { activityResult: ActivityResult ->
                Timber.d("%d", activityResult.resultCode)
                val toast = Toast.makeText(
                    this.context,
                    this.requireContext()
                        .getString(R.string.fragment_quotations_database_import_importing),
                    Toast.LENGTH_SHORT,
                )

                if (Activity.RESULT_OK == activityResult.resultCode) {
                    toast.show()

                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    var fileInputStream: FileInputStream? = null

                    try {
                        parcelFileDescriptor =
                            this.requireContext().contentResolver.openFileDescriptor(
                                activityResult.data!!.data!!, "r",
                            )
                        fileInputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)

                        val importHelper = ImportHelper()
                        val quotations =
                            importHelper.csvImportDatabase(fileInputStream)
                        quoteUnquoteModel!!.insertQuotationsExternal(quotations)

                        fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                            true
                        fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                            true

                        importWasSuccessful()

                        toast.cancel()
                        Toast.makeText(
                            this.context,
                            this.requireContext()
                                .getString(R.string.fragment_quotations_database_import_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } catch (e: ImportHelperException) {
                        toast.cancel()

                        fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                            false
                        fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                            false

                        useInternalDatabase()

                        var message = ""
                        message = if (-1 == e.lineNumber) {
                            this.requireContext().getString(
                                R.string.fragment_quotations_database_import_contents_0,
                                e.message,
                            )
                        } else {
                            this.requireContext().getString(
                                R.string.fragment_quotations_database_import_contents_1,
                                e.lineNumber,
                                e.message,
                            )
                        }

                        Snackbar.make(
                            fragmentQuotationsTabDatabaseTabCsvBinding.root,
                            message,
                            BaseTransientBottomBar.LENGTH_LONG,
                        ).show()
                    } catch (e: FileNotFoundException) {
                        toast.cancel()

                        fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                            false
                        fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                            false

                        useInternalDatabase()

                        Snackbar.make(
                            fragmentQuotationsTabDatabaseTabCsvBinding.root,
                            this.requireContext().getString(
                                R.string.fragment_quotations_database_import_contents_0,
                                e.message,
                            ),
                            BaseTransientBottomBar.LENGTH_LONG,
                        ).show()
                    } finally {
                        _stateFlow.value = quotationsPreferences!!.databaseExternalCsv

                        try {
                            fileInputStream?.close()
                            parcelFileDescriptor?.close()
                        } catch (e: IOException) {
                            Timber.e(e.message)
                        }
                    }
                }
                ConfigureActivity.launcherInvoked = false
            }
    }
}
