package com.example.mutemate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DateTimeSelector(label: String, dateTime: String, minDateTime: String? = null,onDateTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember { mutableStateOf(false) }
    val timePickerDialog = remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf("") }
    val selectedCalendar = remember { mutableStateOf(Calendar.getInstance()) }

    OutlinedButton(
        onClick = { datePickerDialog.value = true },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = if (dateTime.isNotEmpty()) dateTime else label)
    }

    if (datePickerDialog.value) {
        val picker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("MMM", Locale.getDefault()) // "MMM" gives short month name (e.g., "Jan")
                val monthWord = sdf.format(selectedCalendar.time) // Now format the correct month
                selectedDate.value =
                    String.format(Locale.getDefault(), "%d %s %d", dayOfMonth, monthWord, year)
                timePickerDialog.value = true // Open Time Picker next
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        picker.datePicker.minDate = calendar.timeInMillis
        picker.show()

        datePickerDialog.value = false
    }

    if (timePickerDialog.value) {
        val isToday = selectedCalendar.value.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                selectedCalendar.value.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                selectedCalendar.value.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hour, minute ->
                if (isToday && (hour < currentHour || (hour == currentHour && minute < currentMinute))) {
                    Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT).show()
                } else {
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hour12 = if (hour % 12 == 0) 12 else hour % 12

                    val fullDateTime = "${selectedDate.value} ${String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, amPm)}"
                    onDateTimeSelected(fullDateTime)
                }
            },
            if (isToday) currentHour else 0, // Prevent past times for today
            if (isToday) currentMinute else 0,
            false
        ).show()

        timePickerDialog.value = false
    }
}
