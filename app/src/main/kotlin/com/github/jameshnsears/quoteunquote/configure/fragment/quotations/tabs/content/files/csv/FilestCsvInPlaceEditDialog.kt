package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files.csv

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R

interface OnDialogDismissedListener {
    fun onDialogDismissed()
}

open class ContentCsvInPlaceEditDialog : DialogFragment() {
    private var listener: OnDialogDismissedListener? = null

    fun setListener(listener: OnDialogDismissedListener) {
        this.listener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDialogDismissed()
    }

    protected var widgetId: Int = -1

    lateinit var quoteUnquoteModel: QuoteUnquoteModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme)

        arguments?.let {
            widgetId = it.getInt("widgetId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_quotations_tab_database_tab_files_csv_edit,
            container,
            false,
        )

        quoteUnquoteModel = QuoteUnquoteModel(widgetId, requireContext())

        val viewModelFactory = FilesCsvViewModelFactory(widgetId, quoteUnquoteModel)

        val composeView = view.findViewById<ComposeView>(R.id.composeViewCsv)
        composeView.setContent {
            InPlaceEdit(
                ViewModelProvider(this, viewModelFactory)
                    .get(FilesCsvViewModel::class.java),
            )
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton: ImageView = view.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            dismiss()
        }
    }
}
