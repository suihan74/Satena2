package com.suihan74.satena2.scene.entries.bottomSheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.suihan74.hatena.model.entry.SearchType
import com.suihan74.satena2.R
import com.suihan74.satena2.compose.BottomSheetMenuItem
import com.suihan74.satena2.compose.dialog.CustomDialog
import com.suihan74.satena2.compose.dialog.MenuDialog
import com.suihan74.satena2.compose.dialog.NumberPickerDialog
import com.suihan74.satena2.compose.dialog.dialogButton
import com.suihan74.satena2.compose.dialog.menuDialogItem
import com.suihan74.satena2.ui.theme.CurrentTheme
import com.suihan74.satena2.ui.theme.themed.themedCustomDialogColors
import com.suihan74.satena2.ui.theme.themed.themedDatePickerColors
import com.suihan74.satena2.ui.theme.themed.themedTextFieldColors
import com.suihan74.satena2.utility.hatena.textId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 検索設定
 */
data class SearchSetting(
    /** 検索キーワード */
    val query: String = "",
    /** 検索対象 */
    val searchType: SearchType = SearchType.TEXT,
    /** 最小ブクマ数 */
    val bookmarksCount: Int = 3,
    /** 期間指定（始点） */
    val dateBegin: Instant? = null,
    /** 期間指定（終点） */
    val dateEnd: Instant? = null,
    /** セーフサーチ */
    val safe: Boolean = false
)

// ------ //

@OptIn(ExperimentalMaterial3Api::class)
object SearchSettingSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val now = LocalDateTime.now()
        val tomorrowTimeMillis = now.plusDays(1).let {
            LocalDateTime.of(it.year, it.month, it.dayOfMonth, 0, 0, 0)
                .toEpochSecond(ZoneOffset.UTC) * 1000
        }
        return utcTimeMillis < tomorrowTimeMillis
    }

    override fun isSelectableYear(year: Int): Boolean {
        val now = LocalDateTime.now()
        return year >= 2005 && year <= now.year
    }
}

// ------ //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingContent(
    value: SearchSetting,
    dialogProperties: DialogProperties,
    onSearch: (SearchSetting)->Unit = {}
) {
    val loading = remember { mutableStateOf(true) }

    val searchType = remember(value) { mutableStateOf(value.searchType) }
    val bookmarksCount = remember(value) { mutableIntStateOf(value.bookmarksCount) }
    val safe = remember(value) { mutableStateOf(value.safe) }
    val textFieldValue = remember(value) { mutableStateOf(
        TextFieldValue(
            text = value.query,
            selection = TextRange(0, value.query.length)
        )
    ) }

    val searchTypeDialogVisible = remember { mutableStateOf(false) }
    val bookmarksCountDialogVisible = remember { mutableStateOf(false) }

    var datePickerVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState(
        yearRange = 2005..LocalDateTime.now().year,
        selectableDates = SearchSettingSelectableDates
    )
    var pickedDateStart by remember { mutableStateOf<ZonedDateTime?>(null) }
    var pickedDateEnd by remember { mutableStateOf<ZonedDateTime?>(null) }
    val datePickerText =
        if (pickedDateStart == null || pickedDateEnd == null) stringResource(R.string.search_setting_sheet_date_range_none)
        else {
            val f = DateTimeFormatter.ofPattern("uuuu/M/d")
            val startDate = f.format(pickedDateStart)
            val endDate = f.format(pickedDateEnd)
            "$startDate - $endDate"
        }

    fun searchAction() {
        loading.value = true
        val searchSetting = SearchSetting(
            query = textFieldValue.value.text,
            searchType = searchType.value,
            bookmarksCount = bookmarksCount.intValue,
            dateBegin = pickedDateStart?.toInstant(),
            dateEnd = pickedDateEnd?.toInstant(),
            safe = safe.value
        )
        onSearch(searchSetting)
        loading.value = false
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    val keyboardActions = KeyboardActions(onSearch = { searchAction() })

    LaunchedEffect(Unit) {
        loading.value = false
    }

    Box {
        Column {
            Text(
                text = stringResource(R.string.search_setting_sheet_title),
                fontSize = 18.sp,
                color = CurrentTheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            TextField(
                value = textFieldValue.value,
                onValueChange = { textFieldValue.value = it },
                placeholder = { Text(stringResource(R.string.search_setting_sheet_query_placeholder)) },
                colors = themedTextFieldColors(),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                maxLines = 1,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            BottomSheetMenuItem(
                onClick = { searchTypeDialogVisible.value = true }
            ) {
                Text(
                    stringResource(
                        R.string.search_setting_sheet_search_type_label,
                        stringResource(searchType.value.textId)
                    )
                )
            }
            BottomSheetMenuItem(
                onClick = { bookmarksCountDialogVisible.value = true }
            ) {
                Text(
                    stringResource(R.string.search_setting_sheet_bookmarks_count_label, bookmarksCount.intValue)
                )
            }
            BottomSheetMenuItem(
                onClick = {
                    datePickerState.displayMode = DisplayMode.Picker
                    datePickerState.setSelection(
                        startDateMillis = pickedDateStart?.let { it.toEpochSecond() * 1000 },
                        endDateMillis = pickedDateEnd?.let { it.toEpochSecond() * 1000 }
                    )
                    datePickerVisible = true
                }
            ) {
                Text(
                    stringResource(R.string.search_setting_sheet_date_range_label, datePickerText)
                )
            }
            BottomSheetMenuItem(
                onClick = { safe.value = !safe.value }
            ) {
                val (textId, color) =
                    if (safe.value) R.string.on to CurrentTheme.primary
                    else R.string.off to CurrentTheme.onBackground
                Row {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.search_setting_sheet_safe_label))
                            withStyle(
                                SpanStyle(color = color)
                            ) {
                                append(stringResource(textId))
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        AnimatedVisibility(
            visible = !loading.value,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            FloatingActionButton(
                backgroundColor = CurrentTheme.primary,
                contentColor = CurrentTheme.onPrimary,
                onClick = { searchAction() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_category_search),
                    contentDescription = "search",
                    colorFilter = ColorFilter.tint(CurrentTheme.onPrimary)
                )
            }
        }
    }

    if (searchTypeDialogVisible.value) {
        MenuDialog(
            titleText = stringResource(R.string.search_setting_sheet_search_type_title),
            menuItems = SearchType.entries
                .map {
                    menuDialogItem(textId = it.textId) {
                        searchType.value = it
                        true
                    }
                },
            negativeButton = dialogButton(R.string.cancel) { searchTypeDialogVisible.value = false },
            onDismissRequest = { searchTypeDialogVisible.value = false },
            colors = themedCustomDialogColors(),
            properties = dialogProperties
        )
    }

    if (bookmarksCountDialogVisible.value) {
        val countValue = remember(bookmarksCount) { mutableIntStateOf(bookmarksCount.intValue) }
        NumberPickerDialog(
            range = IntRange(0, 1000),
            current = countValue,
            titleText = stringResource(R.string.search_setting_sheet_bookmarks_count_title),
            positiveButton = dialogButton(R.string.ok) {
                bookmarksCount.intValue = countValue.intValue
                bookmarksCountDialogVisible.value = false
            },
            negativeButton = dialogButton(R.string.cancel) { bookmarksCountDialogVisible.value = false },
            onDismissRequest = { bookmarksCountDialogVisible.value = false },
            colors = themedCustomDialogColors(),
            properties = dialogProperties
        )
    }

    if (datePickerVisible) {
        CustomDialog(
            colors = themedCustomDialogColors(),
            properties = dialogProperties,
            title = { Spacer(Modifier.height(12.dp)) },
            onDismissRequest = { datePickerVisible = false },
            positiveButton = dialogButton(textId = R.string.ok) {
                if (datePickerState.selectedStartDateMillis == null) {
                    pickedDateStart = null
                    pickedDateEnd = null
                }
                else {
                    pickedDateStart =
                        ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(datePickerState.selectedStartDateMillis!! / 1000),
                            ZoneOffset.UTC
                        )
                    pickedDateEnd =
                        datePickerState.selectedEndDateMillis ?.let {
                            ZonedDateTime.ofInstant(
                                Instant.ofEpochSecond(it / 1000),
                                ZoneOffset.UTC
                            )
                        } ?: pickedDateStart
                }
                datePickerVisible = false
            },
            negativeButton = dialogButton(textId = R.string.cancel) {
                datePickerVisible = false
            },
            neutralButton = dialogButton(text = "リセット") {
                datePickerState.setSelection(null, null)
            }
        ) {
            DateRangePicker(
                colors = themedDatePickerColors(),
                state = datePickerState,
                modifier = Modifier.heightIn(max = 450.dp),
                title = null,
                headline = {
                    DateRangePickerDefaults.DateRangePickerHeadline(
                        selectedStartDateMillis = datePickerState.selectedStartDateMillis,
                        selectedEndDateMillis = datePickerState.selectedEndDateMillis,
                        displayMode = datePickerState.displayMode,
                        dateFormatter = remember { DatePickerDefaults.dateFormatter(selectedDateSkeleton = "yy/M/d)") },
                        modifier = Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp))
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun SearchSettingContentPreview() {
    Box(
        Modifier
            .background(CurrentTheme.tapGuard)
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .background(
                    color = CurrentTheme.background,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .align(Alignment.BottomCenter)
        ) {
                SearchSettingContent(
                    value = SearchSetting(),
                    dialogProperties = DialogProperties()
                )
        }
    }
}
