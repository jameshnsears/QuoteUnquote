package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.ConfigureActivity
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.ContentFragment
import com.github.jameshnsears.quoteunquote.databinding.FragmentQuotationsTabDatabaseTabFilesBinding
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import com.github.jameshnsears.quoteunquote.utils.ImportHelper
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

open class ContentExternalFileFragment(
    widgetId: Int,
) : ContentFragment(widgetId) {
    private var _binding: FragmentQuotationsTabDatabaseTabFilesBinding? = null

    val binding get() = _binding!!

    private var storageAccessFrameworkActivityImportCSV: ActivityResultLauncher<Intent>? = null

    private var storageAccessFrameworkActivityExportCsv: ActivityResultLauncher<Intent>? = null

    private var storageAccessFrameworkActivityImportFortune: ActivityResultLauncher<Intent>? = null

    override fun onResume() {
        super.onResume()
        rememberScreen(Screen.ContentFiles, context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (this.quotationsPreferences == null) {
            this.quotationsPreferences =
                QuotationsPreferences(
                    this.widgetId,
                    this.requireContext(),
                )
        }

        _binding = FragmentQuotationsTabDatabaseTabFilesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        if (quotationsPreferences!!.databaseExternalCsv) {
            initButtons(true)
        } else {
            initButtons(false)
        }

        enableRadioIfExternalDatabaseContainsData()

        createListenerRadioCsv()

        setHandleImportCsv()

        setHandleExportCsv()

        setHandlerImportFortune()

        createListenerButtonImportCsv()

        createListenerButtonInPlaceExport()

        createListenerButtonInPlaceEdit()

        createListenerButtonInPlaceImportFortune()
    }

    private fun initButtons(enabled: Boolean) {
        binding.radioButtonDatabaseExternalFile.isEnabled =
            enabled
        binding.radioButtonDatabaseExternalFile.isChecked =
            enabled

        binding.buttonExport.isEnabled = enabled
        makeButtonAlpha(binding.buttonExport, enabled)

        binding.buttonEdit.isEnabled = enabled
        makeButtonAlpha(binding.buttonEdit, enabled)

        binding.exportEditWarning.isEnabled = enabled
        binding.exportEditInfo.isEnabled = enabled
    }

    private fun enableRadioIfExternalDatabaseContainsData() {
        if (quoteUnquoteModel!!.externalDatabaseContainsQuotations()) {
            binding.radioButtonDatabaseExternalFile.isEnabled =
                true
        }
    }

    private fun createListenerRadioCsv() {
        val radioButtonDatabaseInternal =
            binding.radioButtonDatabaseExternalFile
        radioButtonDatabaseInternal.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                usingExternalFile()

                binding.buttonExport.isEnabled = true
                makeButtonAlpha(binding.buttonExport, true)

                binding.buttonEdit.isEnabled = true
                makeButtonAlpha(binding.buttonEdit, true)
            }
        }
    }

    private fun usingExternalFile() {
        quotationsPreferences!!.databaseInternal = false
        quotationsPreferences!!.databaseExternalCsv = true
        quotationsPreferences!!.databaseExternalWeb = false
        quotationsPreferences!!.databaseExternalContent = QuotationsPreferences.DATABASE_EXTERNAL

        binding.buttonEdit.isEnabled = true
        binding.buttonExport.isEnabled = true

        updateQuotationsPreferences()
    }

    private fun createListenerButtonImportCsv() {
        // adb push app/src/androidTest/assets/Favourites.csv /sdcard/Download

        // invoke Storage Access Framework
        binding.buttonImport.setOnClickListener {
            if (binding.buttonImport.isEnabled) {
                ConfigureActivity.launcherInvoked = true

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("text/comma-separated-values")
                this.storageAccessFrameworkActivityImportCSV!!.launch(intent)
            }
        }
    }

    private fun createListenerButtonInPlaceExport() {
        binding.buttonExport.setOnClickListener {
            ConfigureActivity.launcherInvoked = true

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("text/comma-separated-values")
            intent.putExtra(Intent.EXTRA_TITLE, "CSVfile.csv")

            this.storageAccessFrameworkActivityExportCsv!!.launch(intent)
        }
    }

    private fun createListenerButtonInPlaceImportFortune() {
        binding.buttonImportFortune.setOnClickListener {
            if (binding.buttonImport.isEnabled) {
                ConfigureActivity.launcherInvoked = true

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("*/*")
                this.storageAccessFrameworkActivityImportFortune!!.launch(intent)
            }
        }
    }

    fun dialogDismissed() {
        if (quoteUnquoteModel!!.allQuotations.isEmpty()) {
            initButtons(false)

            useInternalDatabase()

            Toast
                .makeText(
                    this.context,
                    this
                        .requireContext()
                        .getString(R.string.fragment_quotations_database_file_csv_inplace_warning_empty_database),
                    Toast.LENGTH_LONG,
                ).show()
        }
    }

    private fun createListenerButtonInPlaceEdit() {
        binding.buttonEdit.setOnClickListener {
            val contentCsvInPlaceEditDialog =
                ContentCsvInPlaceEditDialog().apply {
                    arguments =
                        Bundle().apply {
                            putInt("widgetId", widgetId)
                        }
                }

            contentCsvInPlaceEditDialog.setListener(
                object : OnDialogDismissedListener {
                    override fun onDialogDismissed() {
                        dialogDismissed()
                    }
                },
            )

            contentCsvInPlaceEditDialog.show(parentFragmentManager, "ContentCsvInPlaceEditDialog")
        }
    }

    private fun import(
        activityResult: ActivityResult,
        isCsvFile: Boolean,
    ) {
        Timber.d("%d", activityResult.resultCode)
        val toast =
            Toast.makeText(
                this.context,
                this
                    .requireContext()
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
                        activityResult.data!!.data!!,
                        "r",
                    )
                fileInputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)

                val importHelper = ImportHelper()
                if (isCsvFile) {
                    quoteUnquoteModel!!.insertQuotationsExternal(
                        importHelper.importCsv(fileInputStream),
                    )
                } else {
                    quoteUnquoteModel!!.insertQuotationsExternal(
                        importHelper.importFortune(
                            activityResult.data!!.data!!.path,
                            fileInputStream,
                        ),
                    )
                }

                initButtons(true)

                importWasSuccessful()

                toast.cancel()
                Toast
                    .makeText(
                        this.context,
                        this
                            .requireContext()
                            .getString(R.string.fragment_quotations_database_import_success),
                        Toast.LENGTH_SHORT,
                    ).show()
            } catch (e: ImportHelper.ImportHelperException) {
                toast.cancel()

                initButtons(false)

                useInternalDatabase()

                val message =
                    if (-1 == e.lineNumber) {
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

                Snackbar
                    .make(
                        binding.root,
                        message,
                        BaseTransientBottomBar.LENGTH_LONG,
                    ).show()
            } catch (e: FileNotFoundException) {
                toast.cancel()

                binding.radioButtonDatabaseExternalFile.isEnabled =
                    false
                binding.radioButtonDatabaseExternalFile.isChecked =
                    false

                useInternalDatabase()

                Snackbar
                    .make(
                        binding.root,
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

    private fun setHandlerImportFortune() {
        this.storageAccessFrameworkActivityImportFortune =
            this.registerForActivityResult<Intent, ActivityResult>(
                ActivityResultContracts.StartActivityForResult(),
            ) { activityResult: ActivityResult ->
                import(
                    activityResult,
                    false,
                )
            }
    }

    private fun setHandleImportCsv() {
        this.storageAccessFrameworkActivityImportCSV =
            this.registerForActivityResult<Intent, ActivityResult>(
                ActivityResultContracts.StartActivityForResult(),
            ) { activityResult: ActivityResult ->
                import(
                    activityResult,
                    true,
                )
            }
    }

    private fun setHandleExportCsv() {
        this.storageAccessFrameworkActivityExportCsv =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (Activity.RESULT_OK == activityResult.resultCode) {
                    try {
                        val parcelFileDescriptor =
                            requireContext().contentResolver.openFileDescriptor(
                                activityResult.data!!.data!!,
                                "wt",
                            )

                        val fileOutputStream =
                            FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

                        ImportHelper().csvExport(
                            fileOutputStream,
                            quoteUnquoteModel!!.allQuotations as ArrayList<QuotationEntity>,
                        )

                        fileOutputStream.close()
                        parcelFileDescriptor.close()

                        Toast
                            .makeText(
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
