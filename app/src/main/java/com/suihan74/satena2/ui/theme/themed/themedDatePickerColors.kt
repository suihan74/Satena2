package com.suihan74.satena2.ui.theme.themed

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.suihan74.satena2.ui.theme.CurrentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun themedDatePickerColors(
    containerColor: Color = CurrentTheme.background,
    titleContentColor: Color = CurrentTheme.onBackground,
    headlineContentColor: Color = CurrentTheme.onBackground,
    weekdayContentColor: Color = CurrentTheme.onBackground,
    subheadContentColor: Color = CurrentTheme.onBackground,
    navigationContentColor: Color = CurrentTheme.onBackground,
    yearContentColor: Color = CurrentTheme.onBackground,
    disabledYearContentColor: Color = CurrentTheme.onBackground,
    currentYearContentColor: Color = CurrentTheme.primary,
    selectedYearContentColor: Color = CurrentTheme.primary,
    disabledSelectedYearContentColor: Color = Color.Unspecified,
    selectedYearContainerColor: Color = Color.Unspecified,
    disabledSelectedYearContainerColor: Color = Color.Unspecified,
    dayContentColor: Color = CurrentTheme.onBackground,
    disabledDayContentColor: Color = CurrentTheme.onBackground,
    selectedDayContentColor: Color = CurrentTheme.onPrimary,
    disabledSelectedDayContentColor: Color = Color.Unspecified,
    selectedDayContainerColor: Color = CurrentTheme.primary,
    disabledSelectedDayContainerColor: Color = Color.Unspecified,
    todayContentColor: Color = CurrentTheme.primary,
    todayDateBorderColor: Color = CurrentTheme.primary,
    dayInSelectionRangeContentColor: Color = CurrentTheme.onPrimary,
    dayInSelectionRangeContainerColor: Color = CurrentTheme.primary.copy(alpha = .65f),
    dividerColor: Color = Color.Unspecified,
    dateTextFieldColors: TextFieldColors? = themedTextFieldColors3()
) = DatePickerDefaults.colors(
    containerColor = containerColor,
    titleContentColor = titleContentColor,
    headlineContentColor = headlineContentColor,
    weekdayContentColor = weekdayContentColor,
    subheadContentColor = subheadContentColor,
    navigationContentColor = navigationContentColor,
    yearContentColor = yearContentColor,
    disabledYearContentColor = disabledYearContentColor,
    currentYearContentColor = currentYearContentColor,
    selectedYearContentColor = selectedYearContentColor,
    disabledSelectedYearContentColor = disabledSelectedYearContentColor,
    selectedYearContainerColor = selectedYearContainerColor,
    disabledSelectedYearContainerColor = disabledSelectedYearContainerColor,
    dayContentColor = dayContentColor,
    disabledDayContentColor = disabledDayContentColor,
    selectedDayContentColor = selectedDayContentColor,
    disabledSelectedDayContentColor = disabledSelectedDayContentColor,
    selectedDayContainerColor = selectedDayContainerColor,
    disabledSelectedDayContainerColor = disabledSelectedDayContainerColor,
    todayContentColor = todayContentColor,
    todayDateBorderColor = todayDateBorderColor,
    dayInSelectionRangeContentColor = dayInSelectionRangeContentColor,
    dayInSelectionRangeContainerColor = dayInSelectionRangeContainerColor,
    dividerColor = dividerColor,
    dateTextFieldColors = dateTextFieldColors
)
