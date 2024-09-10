package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.content.tabs.csv

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity
import timber.log.Timber

@Composable
fun InPlaceEdit(
    conventCsvViewModel: ConventCsvViewModel,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(4.dp)
            .verticalScroll(scrollState),
    ) {
        Column(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        ) {
            InPlaceEditList(conventCsvViewModel)

            HorizontalDivider(color = if (isDarkTheme) Color.DarkGray else Color.LightGray)

            InPlaceEditTextSourceQuotationFields(conventCsvViewModel)

            InPlaceEditSaveDeleteButtons(conventCsvViewModel)

            InPlaceEditInstructions()
        }
    }
}

@Composable
fun InPlaceEditList(
    conventCsvViewModel: ConventCsvViewModel,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val list by conventCsvViewModel.list.collectAsState()
    val selectedItemIndex by conventCsvViewModel.selectedItemIndex.collectAsState()

    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .height(410.dp)
            .padding(top = 4.dp, bottom = 14.dp),
    ) {
        LazyColumn() {
            itemsIndexed(list) { index, quotation ->
                val isSelected = selectedItemIndex == index

                LazyRow(
                    modifier = Modifier
                        .padding(6.dp)
                        .clickable {
                            Timber.d(quotation.digest)

                            conventCsvViewModel.setSelectedItemIndex(if (isSelected) null else index)

                            if (!isSelected) {
                                conventCsvViewModel.populateTextFields(
                                    quotation.digest,
                                    quotation.author,
                                    quotation.quotation,
                                )
                            } else {
                                conventCsvViewModel.populateTextFields()
                            }

                            focusManager.clearFocus()
                        }
                        .focusable()
                        .background(
                            color = if (isDarkTheme) {
                                if (isSelected) Color.LightGray else Color.Black
                            } else {
                                if (isSelected) Color.LightGray else Color.White
                            },
                        ),
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(6.dp),
                            text = (1 + index).toString(),
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontSize = 14.sp,
                        )

                        Text(
                            modifier = Modifier.padding(6.dp),
                            text = quotation.author,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontSize = 14.sp,
                        )

                        Text(
                            modifier = Modifier.padding(6.dp),
                            text = quotation.quotation,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InPlaceEditTextSourceQuotationFields(
    conventCsvViewModel: ConventCsvViewModel,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
        unfocusedTextColor = if (isDarkTheme) Color.Gray else Color.Black,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (isDarkTheme) Color.Gray else Color.Black,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = if (isDarkTheme) Color.Gray else Color.Black,
        focusedTrailingIconColor = if (isDarkTheme) Color.White else Color.Black,
        unfocusedTrailingIconColor = Color.Transparent,
    )

    val digest by conventCsvViewModel.digest.collectAsState()
    val textFieldSource by conventCsvViewModel.source.collectAsState()
    val textFieldQuotation by conventCsvViewModel.quotation.collectAsState()

    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            OutlinedTextField(
                colors = textFieldColors,
                value = textFieldSource,
                onValueChange = { newTextFieldSource ->
                    conventCsvViewModel.populateTextFields(
                        digest,
                        newTextFieldSource,
                        textFieldQuotation,
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.fragment_quotations_database_csv_inplace_source),
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .testTag("InPlaceEditTextFields.Source")
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                trailingIcon = {
                    if (textFieldSource.isNotEmpty()) {
                        IconButton(onClick = {
                            conventCsvViewModel.populateTextFields(
                                quotation = textFieldQuotation,
                            )
                        }) {
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

    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            OutlinedTextField(
                colors = textFieldColors,
                value = textFieldQuotation,
                onValueChange = { newTextFieldQuotation ->
                    conventCsvViewModel.populateTextFields(
                        digest,
                        textFieldSource,
                        newTextFieldQuotation,
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.fragment_quotations_database_csv_inplace_quotation),
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .testTag("InPlaceEditTextFields.Source")
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                trailingIcon = {
                    if (textFieldQuotation.isNotEmpty()) {
                        IconButton(onClick = {
                            conventCsvViewModel.populateTextFields(
                                author = textFieldSource,
                            )
                        }) {
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
private fun InPlaceEditSaveDeleteButtons(
    conventCsvViewModel: ConventCsvViewModel,
) {
    val context = LocalContext.current

    val digest by conventCsvViewModel.digest.collectAsState()
    val textFieldSource by conventCsvViewModel.source.collectAsState()
    val textFieldQuotation by conventCsvViewModel.quotation.collectAsState()

    val messageSaveUpdated = stringResource(R.string.fragment_quotations_database_csv_inplace_save_update)
    val messageSaveAppended = stringResource(R.string.fragment_quotations_database_csv_inplace_save_append)
    val messageWarningDuplicate = stringResource(R.string.fragment_quotations_database_csv_inplace_save_warning_duplicate)

    val messageDeleted = stringResource(R.string.fragment_quotations_database_csv_inplace_deleted)

    Row(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp),
        ) {
            Button(
                modifier = Modifier
                    .height(41.dp)
                    .width(120.dp),
                onClick = {
                    when (conventCsvViewModel.buttonSavePressed()) {
                        0 -> Toast.makeText(context, messageSaveUpdated, Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(context, messageSaveAppended, Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(context, messageWarningDuplicate, Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = (textFieldSource.isNotEmpty() && textFieldQuotation.isNotEmpty()),
            ) {
                Text(
                    stringResource(id = R.string.fragment_quotations_database_csv_inplace_save),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .height(41.dp)
                    .width(120.dp),
                onClick = {
                    conventCsvViewModel.buttonDeletePressed()

                    Toast.makeText(context, messageDeleted, Toast.LENGTH_SHORT).show()
                },
                enabled = digest.isNotEmpty(),
            ) {
                Text(
                    stringResource(id = R.string.fragment_quotations_database_csv_inplace_delete),
                )
            }
        }
    }
}

@Composable
private fun InPlaceEditInstructions() {
    val isDarkTheme = isSystemInDarkTheme()

    val darkThemeInstructions = Color(0xffcac4d0)
    val lightThemeInstructions = Color.Black

    Row(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "",
            tint = if (isDarkTheme) darkThemeInstructions else lightThemeInstructions,
        )
    }

    Row(
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.fragment_quotations_database_csv_inplace_instruction_01),
            fontSize = 14.sp,
            lineHeight = 16.sp,
            color = if (isDarkTheme) darkThemeInstructions else lightThemeInstructions,
        )
    }

    Row(
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.fragment_quotations_database_csv_inplace_instruction_02),
            fontSize = 14.sp,
            lineHeight = 16.sp,
            color = if (isDarkTheme) darkThemeInstructions else lightThemeInstructions,
        )
    }
}

@Preview(
    apiLevel = 34,
    widthDp = 400,
)
@Composable
fun PreviewInPlaceEdit() {
    class QuoteUnquoteModelDummy : QuoteUnquoteModel() {
        override fun getQuotationsAll(): List<QuotationEntity> {
            return mutableListOf(
                QuotationEntity(
                    "digest",
                    "wikipedia",
                    "author-1",
                    "quotation-1",
                ),
            )
        }
    }

    MaterialTheme {
        InPlaceEdit(
            ConventCsvViewModel(
                QuoteUnquoteModelDummy(),
            ),
        )
    }
}
