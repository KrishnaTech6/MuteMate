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
import java.util.Date
import java.util.Locale

@Composable
fun DateTimeSelector(
    label: String,
    dateTime: Date?,
    minDateTime: Date? = null, // The minimum date-time allowed (used for End Time validation)
    onDateTimeSelected: (Date) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember { mutableStateOf(false) }
    val timePickerDialog = remember { mutableStateOf(false) }
    val isChosen = remember { mutableStateOf(false) }
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
        Text(
            text = if (isChosen.value) {
                SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(dateTime ?: Date())
            } else label
        )
    }

    if (datePickerDialog.value) {
        val picker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedCalendar.value.set(year, month, dayOfMonth)
                timePickerDialog.value = true // Open Time Picker next
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        picker.datePicker.minDate = calendar.timeInMillis // Prevent past dates
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
                val selectedTimestamp = selectedCalendar.value.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }.time

                if (isToday && selectedTimestamp.time < System.currentTimeMillis()) {
                    Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                minDateTime?.let {
                    if (selectedTimestamp.time <= it.time) {
                        Toast.makeText(context, "End time must be after start time!", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                }

                isChosen.value = true
                onDateTimeSelected(selectedTimestamp)
            },
            if (isToday) currentHour else 0,
            if (isToday) currentMinute else 0,
            false
        ).show()

        timePickerDialog.value = false
    }
}
