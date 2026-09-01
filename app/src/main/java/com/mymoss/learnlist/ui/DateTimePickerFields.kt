package com.mymoss.learnlist.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale

/**
 * Date input with a native Android picker. The text remains editable so old
 * backups and keyboard users still have a predictable YYYY-MM-DD fallback.
 */
@Composable
internal fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false,
) {
    var showPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("YYYY-MM-DD") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            Row {
                if (allowClear && value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除日期", modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "选择日期", modifier = Modifier.size(19.dp))
                }
            }
        },
    )

    if (showPicker) {
        NativeDatePickerHost(
            context = context,
            initialDate = latestValue.toLocalDateOrNull() ?: LocalDate.now(),
            onDateSelected = { selected -> latestOnValueChange(selected.toString()); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

/**
 * Time input with the native Android picker. Text entry remains available as
 * a fallback and is normalized to HH:MM only after a picker selection.
 */
@Composable
internal fun TimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("HH:MM") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.AccessTime, contentDescription = "选择时间", modifier = Modifier.size(20.dp))
            }
        },
    )

    if (showPicker) {
        val initial = latestValue.toLocalTimeOrNull() ?: LocalTime.of(9, 0)
        NativeTimePickerHost(
            context = context,
            initialTime = initial,
            onTimeSelected = { hour, minute ->
                latestOnValueChange(formatTime(hour, minute))
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun NativeDatePickerHost(
    context: android.content.Context,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val latestOnDateSelected by rememberUpdatedState(onDateSelected)
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    DisposableEffect(Unit) {
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                latestOnDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        )
        dialog.setOnDismissListener { latestOnDismiss() }
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}

@Composable
private fun NativeTimePickerHost(
    context: android.content.Context,
    initialTime: LocalTime,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val latestOnTimeSelected by rememberUpdatedState(onTimeSelected)
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    DisposableEffect(Unit) {
        val dialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute -> latestOnTimeSelected(hourOfDay, minute) },
            initialTime.hour,
            initialTime.minute,
            true,
        )
        dialog.setOnDismissListener { latestOnDismiss() }
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}

internal fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(trim()) }.getOrNull()

internal fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(trim()) }.getOrNull()

internal fun formatTime(hour: Int, minute: Int): String = String.format(Locale.ROOT, "%02d:%02d", hour, minute)

internal fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
