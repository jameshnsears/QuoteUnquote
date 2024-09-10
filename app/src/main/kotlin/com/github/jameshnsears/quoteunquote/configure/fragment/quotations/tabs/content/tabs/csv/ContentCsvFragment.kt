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
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseTabCsvBinding
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.github.jameshnsears.quoteunquote.utils.ImportHelper.ImportHelperException
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class ContentCsvFragment(widgetId: Int) : ContentFragment(widgetId) {
    private var _binding: FragmentQuotationsTabDatabaseTabCsvBinding? = null

    private val fragmentQuotationsTabDatabaseTabCsvBinding get() = _binding!!

    var quotationsPreferences: QuotationsPreferences? = null

    private var storageAccessFrameworkActivityResultCSV: ActivityResultLauncher<Intent>? = null

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

        createListenerButtonInPlaceEdit()
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

        updateQuotationsUI()
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
                this.storageAccessFrameworkActivityResultCSV!!.launch(intent)
            }
        }
    }

    private fun createListenerButtonInPlaceEdit() {
        fragmentQuotationsTabDatabaseTabCsvBinding.buttonInPlaceEdit.setOnClickListener {

            val contentCsvInPlaceEditDialog = ContentCsvInPlaceEditDialog().apply {
                arguments = Bundle().apply {
                    putInt("widgetId", widgetId)
                }
            }

            contentCsvInPlaceEditDialog.show(parentFragmentManager, "ContentCsvInPlaceEditDialog")
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
                        // TODO enable / disable the in-place edit button

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
