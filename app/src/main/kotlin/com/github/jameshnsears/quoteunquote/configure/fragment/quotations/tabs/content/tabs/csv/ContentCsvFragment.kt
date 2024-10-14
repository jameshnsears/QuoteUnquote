package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs.csv

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
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs.ContentFragment
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseTabCsvBinding
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.ImportHelper.ImportHelperException
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ContentCsvFragment(widgetId: Int) : ContentFragment(widgetId) {
    private var _binding: FragmentQuotationsTabDatabaseTabCsvBinding? = null

    private val fragmentQuotationsTabDatabaseTabCsvBinding get() = _binding!!

    var quotationsPreferences: QuotationsPreferences? = null

    private var storageAccessFrameworkActivityImportCSV: ActivityResultLauncher<Intent>? = null

    private var storageAccessFrameworkActivityExportCsv: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        this.quotationsPreferences = QuotationsPreferences(
            this.widgetId,
            this.requireContext(),
        )

        _binding = FragmentQuotationsTabDatabaseTabCsvBinding.inflate(inflater, container, false)

        return fragmentQuotationsTabDatabaseTabCsvBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated.ContentCsvFragment")

        if (quotationsPreferences!!.databaseExternalCsv) {
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                true
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                true
            fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceExport.isEnabled = true
            fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceEdit.isEnabled = true
        } else {
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                false
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                false
            fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceExport.isEnabled = false
            fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceEdit.isEnabled = false
        }

        enableRadioIfExternalDatabaseContainsData()

        createListenerRadioCsv()

        setHandleImportCsv()

        setHandleExportCsv()

        createListenerButtonImportCsv()

        createListenerButtonInPlaceExport()

        createListenerButtonInPlaceEdit()
    }

    private fun enableRadioIfExternalDatabaseContainsData() {
        if (quoteUnquoteModel!!.externalDatabaseContainsQuotations()) {
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled = true
        }
    }

    private fun createListenerRadioCsv() {
        val radioButtonDatabaseInternal =
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv
        radioButtonDatabaseInternal.setOnCheckedChangeListener { buttonView: CompoundButton?, _: Boolean ->
            usingCsv()
        }
    }

    private fun usingCsv() {
        quotationsPreferences!!.databaseInternal = false
        quotationsPreferences!!.databaseExternalCsv = true
        quotationsPreferences!!.databaseExternalWeb = false
        quotationsPreferences!!.databaseExternalContent = QuotationsPreferences.DATABASE_EXTERNAL
        DatabaseRepository.useInternalDatabase = false

        fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceEdit.isEnabled = true
        fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceExport.isEnabled = true

        updateQuotationsPreferences()
    }

    private fun createListenerButtonImportCsv() {
        // adb push app/src/androidTest/assets/Favourites.csv /sdcard/Download

        // invoke Storage Access Framework
        fragmentQuotationsTabDatabaseTabCsvBinding.buttonImport.setOnClickListener {
            if (fragmentQuotationsTabDatabaseTabCsvBinding.buttonImport.isEnabled) {
                ConfigureActivity.launcherInvoked = true

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("text/comma-separated-values")
                this.storageAccessFrameworkActivityImportCSV!!.launch(intent)
            }
        }
    }

    private fun createListenerButtonInPlaceExport() {
        fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceExport.setOnClickListener {
            ConfigureActivity.launcherInvoked = true

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("text/comma-separated-values")
            intent.putExtra(Intent.EXTRA_TITLE, "CSVfile.csv")

            this.storageAccessFrameworkActivityExportCsv!!.launch(intent)
        }
    }

    fun dialogDismissed() {
        if (quoteUnquoteModel!!.allQuotations.isEmpty()) {
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isEnabled =
                false
            fragmentQuotationsTabDatabaseTabCsvBinding.radioButtonDatabaseExternalCsv.isChecked =
                false
            fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceExport.isEnabled =
                false
            fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceEdit.isEnabled =
                false

            useInternalDatabase()

            Toast.makeText(
                this.context,
                this.requireContext()
                    .getString(R.string.fragment_quotations_database_csv_inplace_warning_empty_database),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun createListenerButtonInPlaceEdit() {
        fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceEdit.setOnClickListener {
            val contentCsvInPlaceEditDialog = ContentCsvInPlaceEditDialog().apply {
                arguments = Bundle().apply {
                    putInt("widgetId", widgetId)
                }
            }

            contentCsvInPlaceEditDialog.setListener(object : OnDialogDismissedListener {
                override fun onDialogDismissed() {
                    dialogDismissed()
                }
            })

            contentCsvInPlaceEditDialog.show(parentFragmentManager, "ContentCsvInPlaceEditDialog")
        }
    }

    private fun setHandleImportCsv() {
        // default: /storage/emulated/0/Download/
        this.storageAccessFrameworkActivityImportCSV =
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

                        val message = if (-1 == e.lineNumber) {
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

    private fun setHandleExportCsv() {
        this.storageAccessFrameworkActivityExportCsv =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (Activity.RESULT_OK == activityResult.resultCode) {
                    try {
                        val parcelFileDescriptor =
                            requireContext().contentResolver.openFileDescriptor(
                                activityResult.data!!.data!!, "w",
                            )

                        val fileOutputStream =
                            FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

                        ImportHelper().csvExport(
                            fileOutputStream,
                            quoteUnquoteModel!!.allQuotations as ArrayList<QuotationEntity>,
                        )

                        fileOutputStream.close()
                        parcelFileDescriptor!!.close()

                        Toast.makeText(
                            context,
                            requireContext().getString(R.string.fragment_quotations_selection_export_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } catch (e: IOException) {
                        Timber.e(e.message)
                    }
                }
                ConfigureActivity.launcherInvoked = false
            }
    }
}
