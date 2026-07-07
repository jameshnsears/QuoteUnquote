package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.files

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.db.q.QuotationEntity
import timber.log.Timber

@Composable
fun inPlaceEdit(filesCsvViewModel: FilesCsvViewModel) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.statusBars),
        ) { innerPadding ->
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                color = MaterialTheme.colorScheme.background,
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(4.dp),
                ) {
                    OutlinedCard(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                        ) {
                            inPlaceEditList(filesCsvViewModel)

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            inPlaceEditSaveDeleteButtons(filesCsvViewModel)

                            inPlaceEditTextSourceQuotationFields(filesCsvViewModel)

                            inPlaceEditInstructions()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun inPlaceEditList(filesCsvViewModel: FilesCsvViewModel) {
    val list by filesCsvViewModel.list.collectAsState()
    val selectedItemIndex by filesCsvViewModel.selectedItemIndex.collectAsState()

    val focusManager = LocalFocusManager.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(bottom = 14.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(list) { index, quotation ->
                val isSelected = selectedItemIndex == index

                val itemBackgroundColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }

                val itemTextColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                LazyRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(color = itemBackgroundColor)
                            .clickable {
                                Timber.d(quotation.digest)

                                filesCsvViewModel.setSelectedItemIndex(if (isSelected) null else index)

                                if (!isSelected) {
                                    filesCsvViewModel.populateTextFields(
                                        quotation.digest,
                                        quotation.author,
                                        quotation.quotation,
                                    )
                                } else {
                                    filesCsvViewModel.populateTextFields()
                                }

                                focusManager.clearFocus()
                            }.padding(top = 3.dp)
                            .padding(3.dp)
                            .focusable(),
                ) {
                    item {
                        Column {
                            Row {
                                Text(
                                    modifier = Modifier.padding(3.dp),
                                    text = (1 + index).toString(),
                                    color = itemTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )

                                Text(
                                    modifier =
                                        Modifier
                                            .padding(start = 3.dp)
                                            .padding(3.dp),
                                    text = quotation.quotation,
                                    color = itemTextColor,
                                    fontSize = 14.sp,
                                )
                            }

                            Text(
                                modifier = Modifier.padding(start = 3.dp, top = 3.dp),
                                text = quotation.author,
                                color = itemTextColor,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun inPlaceEditTextSourceQuotationFields(filesCsvViewModel: FilesCsvViewModel) {
    val textFieldColors =
        OutlinedTextFieldDefaults.colors(
            unfocusedTrailingIconColor = Color.Transparent,
        )

    val digest by filesCsvViewModel.digest.collectAsState()
    val textFieldAuthor by filesCsvViewModel.author.collectAsState()
    val textFieldQuotation by filesCsvViewModel.quotation.collectAsState()

    Row(
        modifier =
            Modifier
                .padding(top = 6.dp, bottom = 6.dp)
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            OutlinedTextField(
                colors = textFieldColors,
                value = textFieldQuotation,
                onValueChange = { newTextFieldQuotation ->
                    filesCsvViewModel.populateTextFields(
                        digest,
                        textFieldAuthor,
                        newTextFieldQuotation,
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.fragment_quotations_database_file_csv_inplace_quotation),
                    )
                },
                maxLines = 4,
                minLines = 4,
                modifier =
                    Modifier
                        .testTag("InPlaceEditTextFields.Quotation")
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(top = 8.dp, bottom = 8.dp),
                trailingIcon = {
                    if (textFieldQuotation.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                filesCsvViewModel.populateTextFields(
                                    author = textFieldAuthor,
                                )
                            },
                        ) {
                            Icon(
                                painterResource(id = R.drawable.cancel_fill0_wght400_grad0_opsz24),
                                contentDescription = "",
                            )
                        }
                    }
                },
            )

            OutlinedTextField(
                colors = textFieldColors,
                value = textFieldAuthor,
                onValueChange = { newTextFieldAuthor ->
                    filesCsvViewModel.populateTextFields(
                        digest,
                        newTextFieldAuthor,
                        textFieldQuotation,
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.fragment_quotations_database_file_csv_inplace_source),
                    )
                },
                singleLine = true,
                modifier =
                    Modifier
                        .testTag("InPlaceEditTextFields.Source")
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                trailingIcon = {
                    if (textFieldAuthor.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                filesCsvViewModel.populateTextFields(
                                    quotation = textFieldQuotation,
                                )
                            },
                        ) {
                            Icon(
                                painterResource(id = R.drawable.cancel_fill0_wght400_grad0_opsz24),
                                contentDescription = "",
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun inPlaceEditSaveDeleteButtons(filesCsvViewModel: FilesCsvViewModel) {
    val saveButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        )

    val deleteButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = Color.White,
        )

    val context = LocalContext.current

    val digest by filesCsvViewModel.digest.collectAsState()
    val textFieldAuthor by filesCsvViewModel.author.collectAsState()
    val textFieldQuotation by filesCsvViewModel.quotation.collectAsState()

    val messageWarningDuplicate =
        stringResource(R.string.fragment_quotations_database_file_csv_inplace_save_warning_duplicate)

    Row(
        modifier =
            Modifier
                .padding(top = 16.dp, bottom = 0.dp)
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp),
        ) {
            Button(
                colors = saveButtonColors,
                modifier =
                    Modifier
                        .height(41.dp)
                        .width(120.dp),
                onClick = {
                    filesCsvViewModel.buttonSavePressed { result ->
                        when (result) {
                            2 -> {
                                Toast
                                    .makeText(context, messageWarningDuplicate, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                },
                enabled = (textFieldAuthor.isNotEmpty() && textFieldQuotation.isNotEmpty()),
            ) {
                Text(
                    stringResource(id = R.string.fragment_quotations_database_file_csv_inplace_save),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                colors = deleteButtonColors,
                modifier =
                    Modifier
                        .height(41.dp)
                        .width(120.dp),
                onClick = {
                    filesCsvViewModel.buttonDeletePressed()
                },
                enabled = digest.isNotEmpty(),
            ) {
                Text(
                    stringResource(id = R.string.fragment_quotations_database_file_csv_inplace_delete),
                )
            }
        }
    }
}

@Composable
private fun inPlaceEditInstructions() {
    val instructionsColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier =
            Modifier
                .padding(top = 10.dp, bottom = 12.dp)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "",
            tint = instructionsColor,
        )
    }

    Row(
        modifier =
            Modifier
                .padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.fragment_quotations_database_file_csv_inplace_instruction_01),
            color = instructionsColor,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    Row(
        modifier =
            Modifier
                .padding(start = 4.dp, top = 4.dp, bottom = 12.dp)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.fragment_quotations_database_file_csv_inplace_instruction_02),
            color = instructionsColor,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Preview(
    apiLevel = 37,
    widthDp = 400,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun previewInPlaceEditDark() {
    previewInPlaceEdit()
}

@Preview(
    apiLevel = 37,
    widthDp = 400,
)
@Composable
fun previewInPlaceEditLight() {
    previewInPlaceEdit()
}

@Composable
private fun previewInPlaceEdit() {
    class QuoteUnquoteModelDummy : QuoteUnquoteModel() {
        override fun getAllQuotations(): List<QuotationEntity> =
            mutableListOf(
                QuotationEntity(
                    "digest",
                    "wikipedia",
                    "author-1",
                    "quotation-1",
                ),
            )
    }

    inPlaceEdit(
        FilesCsvViewModel(
            1,
            QuoteUnquoteModelDummy(),
        ),
    )
}
