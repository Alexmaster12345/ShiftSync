package com.example.shiftsync

import android.content.SharedPreferences
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ShiftEntry(
    val startedAtMillis: Long,
    val shiftType: ShiftType,
    val durationMinutes: Long,
    val unpaidBreakMinutes: Int,
    val hourlyRate: Double,
    val estimatedPay: Double
)

enum class ShiftType(val label: String) {
    MORNING("Morning"),
    NIGHT("Night"),
    OVERTIME("Overtime")
}

object PayrollCalculator {
    private const val OVERTIME_THRESHOLD_MINUTES = 8 * 60L
    private const val OVERTIME_MULTIPLIER = 1.5

    fun estimatePay(
        totalDurationMinutes: Long,
        unpaidBreakMinutes: Int,
        hourlyRate: Double,
        shiftType: ShiftType
    ): Double {
        val payableMinutes = (totalDurationMinutes - unpaidBreakMinutes).coerceAtLeast(0)
        val overtimeMinutes = when {
            shiftType == ShiftType.OVERTIME -> payableMinutes
            payableMinutes > OVERTIME_THRESHOLD_MINUTES -> payableMinutes - OVERTIME_THRESHOLD_MINUTES
            else -> 0L
        }
        val regularMinutes = (payableMinutes - overtimeMinutes).coerceAtLeast(0)

        val regularPay = regularMinutes / 60.0 * hourlyRate
        val overtimePay = overtimeMinutes / 60.0 * hourlyRate * OVERTIME_MULTIPLIER
        return roundToCents(regularPay + overtimePay)
    }

    private fun roundToCents(value: Double): Double =
        kotlin.math.round(value * 100.0) / 100.0
}

const val PREFS_NAME = "shift_sync_prefs"
const val KEY_ENTRIES = "entries"
const val KEY_HOURLY_RATE = "hourly_rate"
const val KEY_UNPAID_BREAK = "unpaid_break"
const val KEY_ACTIVE_START_MILLIS = "active_start_millis"
const val KEY_ACTIVE_SHIFT_TYPE = "active_shift_type"
const val NO_ACTIVE_SHIFT = -1L

private const val ENTRY_DELIMITER = ";"
private const val FIELD_DELIMITER = ","

fun saveEntries(prefs: SharedPreferences, entries: List<ShiftEntry>) {
    val payload = entries.joinToString(ENTRY_DELIMITER) { entry ->
        listOf(
            entry.startedAtMillis,
            entry.shiftType.name,
            entry.durationMinutes,
            entry.unpaidBreakMinutes,
            entry.hourlyRate,
            entry.estimatedPay
        ).joinToString(FIELD_DELIMITER)
    }
    prefs.edit().putString(KEY_ENTRIES, payload).apply()
}

fun loadEntries(prefs: SharedPreferences): List<ShiftEntry> {
    val payload = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
    if (payload.isBlank()) return emptyList()

    return payload.split(ENTRY_DELIMITER).mapNotNull { row ->
        val fields = row.split(FIELD_DELIMITER)
        if (fields.size != 6) return@mapNotNull null

        val startedAt = fields[0].toLongOrNull() ?: return@mapNotNull null
        val shiftType = runCatching { ShiftType.valueOf(fields[1]) }.getOrNull() ?: return@mapNotNull null
        val durationMinutes = fields[2].toLongOrNull() ?: return@mapNotNull null
        val unpaidBreakMinutes = fields[3].toIntOrNull() ?: return@mapNotNull null
        val hourlyRate = fields[4].toDoubleOrNull() ?: return@mapNotNull null
        val estimatedPay = fields[5].toDoubleOrNull() ?: return@mapNotNull null

        ShiftEntry(
            startedAtMillis = startedAt,
            shiftType = shiftType,
            durationMinutes = durationMinutes,
            unpaidBreakMinutes = unpaidBreakMinutes,
            hourlyRate = hourlyRate,
            estimatedPay = estimatedPay
        )
    }
}

fun formatDate(epochMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

fun formatDuration(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
}

fun formatCurrency(amount: Double): String {
    val decimalFormat = DecimalFormat("#,##0.00")
    return "$${decimalFormat.format(amount)}"
}

