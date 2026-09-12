package com.example.shiftsync

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.round

data class ShiftEntry(
    val startedAtMillis: Long,
    val shiftType: ShiftType,
    val durationMinutes: Long,
    val unpaidBreakMinutes: Int,
    val hourlyRate: Double,
    val estimatedPay: Double,
    val notes: String = ""
)

enum class ShiftType(val label: String) {
    REGULAR("Regular"),
    VACATION("Vacation"),
    NIGHT("Night Shift"),
    OVERTIME("Overtime")
}

enum class AppearanceMode(val prefValue: String) { DARK("dark"), LIGHT("light"), SYSTEM("system") }
enum class PaymentType(val prefValue: String) { MONTHLY("monthly"), HOURLY("hourly") }

data class AppSettings(
    val displayName: String = "Guest",
    val email: String = "",
    val jobTitle: String = "Guest",
    val branch: String = "",
    val appearance: AppearanceMode = AppearanceMode.LIGHT,
    val use24HourClock: Boolean = false,
    val arrivalDepartureAlerts: Boolean = true,
    val overtimeEnabled: Boolean = false,
    val overtimeDailyThresholdHours: Double = 8.5,
    val overtimeWeeklyThresholdHours: Double = 40.0,
    val overtimeMultiplier: Double = 1.5,
    val currencySymbol: String = "€",
    val paymentType: PaymentType = PaymentType.MONTHLY,
    val salaryAmount: Double = 4000.0,
    val workDayHours: Double = 8.5,
    val workplaceLabel: String = "",
    val workplaceSet: Boolean = false,
    val autoGeofencingEnabled: Boolean = false,
    val workplaceLat: Double = 0.0,
    val workplaceLng: Double = 0.0,
    val vacationDaysPerYear: Int = 16
)

object PayrollCalculator {
    fun estimatePay(
        totalDurationMinutes: Long,
        unpaidBreakMinutes: Int,
        hourlyRate: Double,
        shiftType: ShiftType,
        overtimeEnabled: Boolean = false,
        overtimeThresholdMinutes: Long = (8.5 * 60).toLong(),
        overtimeMultiplier: Double = 1.5,
        workDayHours: Double = 8.5,
        monthlySalary: Double = 4000.0
    ): Double {
        if (shiftType == ShiftType.VACATION) {
            return roundToCents(monthlySalary / 20.0)
        }
        val payableMinutes = (totalDurationMinutes - unpaidBreakMinutes).coerceAtLeast(0)
        val overtimeMinutes = when {
            shiftType == ShiftType.OVERTIME -> payableMinutes
            overtimeEnabled && payableMinutes > overtimeThresholdMinutes -> payableMinutes - overtimeThresholdMinutes
            else -> 0L
        }
        val regularMinutes = (payableMinutes - overtimeMinutes).coerceAtLeast(0)
        val regularPay = regularMinutes / 60.0 * hourlyRate
        val overtimePay = overtimeMinutes / 60.0 * hourlyRate * overtimeMultiplier
        val vacationFallback = if (shiftType == ShiftType.VACATION) monthlySalary / 20.0 else 0.0
        return roundToCents((regularPay + overtimePay).takeIf { it > 0 } ?: vacationFallback)
    }

    fun hourlyRate(settings: AppSettings): Double {
        return when (settings.paymentType) {
            PaymentType.HOURLY -> settings.salaryAmount
            PaymentType.MONTHLY -> settings.salaryAmount / ((settings.workDayHours * 22.0).coerceAtLeast(1.0))
        }
    }

    private fun roundToCents(value: Double): Double = round(value * 100.0) / 100.0
}

const val PREFS_NAME = "shift_sync_prefs"
const val KEY_ENTRIES = "entries"
const val KEY_ACTIVE_START_MILLIS = "active_start_millis"
const val NO_ACTIVE_SHIFT = -1L
const val KEY_NOTIFY_SHIFT_START = "notify_shift_start"
const val KEY_NOTIFY_SHIFT_END = "notify_shift_end"
const val KEY_NOTIFY_SOUND = "notify_sound"
const val KEY_NOTIFY_VIBRATE = "notify_vibrate"
const val KEY_REMINDER_CLOCK_IN_ENABLED = "reminder_clock_in_enabled"
const val KEY_REMINDER_CLOCK_IN_HOUR = "reminder_clock_in_hour"
const val KEY_REMINDER_CLOCK_IN_MINUTE = "reminder_clock_in_minute"
const val KEY_REMINDER_CLOCK_IN_DAYS = "reminder_clock_in_days"
const val KEY_REMINDER_CLOCK_OUT_ENABLED = "reminder_clock_out_enabled"
const val KEY_REMINDER_CLOCK_OUT_HOUR = "reminder_clock_out_hour"
const val KEY_REMINDER_CLOCK_OUT_MINUTE = "reminder_clock_out_minute"
const val KEY_REMINDER_CLOCK_OUT_DAYS = "reminder_clock_out_days"
const val KEY_APPEARANCE = "appearance"
const val KEY_USE_24_HOUR_CLOCK = "use_24_hour_clock"
const val KEY_DISPLAY_NAME = "display_name"
const val KEY_EMAIL = "email"
const val KEY_JOB_TITLE = "job_title"
const val KEY_BRANCH = "branch"
const val KEY_OVERTIME_ENABLED = "overtime_enabled"
const val KEY_OVERTIME_DAILY_THRESHOLD_HOURS = "overtime_daily_threshold_hours"
const val KEY_OVERTIME_WEEKLY_THRESHOLD_HOURS = "overtime_weekly_threshold_hours"
const val KEY_OVERTIME_MULTIPLIER = "overtime_multiplier"
const val KEY_CURRENCY_SYMBOL = "currency_symbol"
const val KEY_PAYMENT_TYPE = "payment_type"
const val KEY_SALARY_AMOUNT = "salary_amount"
const val KEY_WORK_DAY_HOURS = "work_day_hours"
const val KEY_WORKPLACE_SET = "workplace_set"
const val KEY_WORKPLACE_LABEL = "workplace_label"
const val KEY_WORKPLACE_LAT = "workplace_lat"
const val KEY_WORKPLACE_LNG = "workplace_lng"
const val KEY_AUTO_GEOFENCING = "auto_geofencing"
const val KEY_VACATION_DAYS_PER_YEAR = "vacation_days_per_year"

val DEFAULT_REMINDER_DAYS: Set<String> = setOf("2", "3", "4", "5", "6")
private const val ENTRY_DELIMITER = ";"
private const val FIELD_DELIMITER = ","

fun loadSettings(prefs: SharedPreferences): AppSettings = AppSettings(
    displayName = prefs.getString(KEY_DISPLAY_NAME, "Guest").orEmpty().ifBlank { "Guest" },
    email = prefs.getString(KEY_EMAIL, "").orEmpty(),
    jobTitle = prefs.getString(KEY_JOB_TITLE, "Guest").orEmpty().ifBlank { "Guest" },
    branch = prefs.getString(KEY_BRANCH, "").orEmpty(),
    appearance = AppearanceMode.entries.firstOrNull { it.prefValue == prefs.getString(KEY_APPEARANCE, AppearanceMode.LIGHT.prefValue) } ?: AppearanceMode.LIGHT,
    use24HourClock = prefs.getBoolean(KEY_USE_24_HOUR_CLOCK, false),
    arrivalDepartureAlerts = prefs.getBoolean(KEY_NOTIFY_SHIFT_START, true) || prefs.getBoolean(KEY_NOTIFY_SHIFT_END, true),
    overtimeEnabled = prefs.getBoolean(KEY_OVERTIME_ENABLED, false),
    overtimeDailyThresholdHours = prefs.getFloat(KEY_OVERTIME_DAILY_THRESHOLD_HOURS, 8.5f).toDouble(),
    overtimeWeeklyThresholdHours = prefs.getFloat(KEY_OVERTIME_WEEKLY_THRESHOLD_HOURS, 40f).toDouble(),
    overtimeMultiplier = prefs.getFloat(KEY_OVERTIME_MULTIPLIER, 1.5f).toDouble(),
    currencySymbol = prefs.getString(KEY_CURRENCY_SYMBOL, "€").orEmpty().ifBlank { "€" },
    paymentType = PaymentType.entries.firstOrNull { it.prefValue == prefs.getString(KEY_PAYMENT_TYPE, PaymentType.MONTHLY.prefValue) } ?: PaymentType.MONTHLY,
    salaryAmount = prefs.getString(KEY_SALARY_AMOUNT, "4000.0")?.toDoubleOrNull() ?: 4000.0,
    workDayHours = prefs.getFloat(KEY_WORK_DAY_HOURS, 8.5f).toDouble(),
    workplaceLabel = prefs.getString(KEY_WORKPLACE_LABEL, "").orEmpty(),
    workplaceSet = prefs.getBoolean(KEY_WORKPLACE_SET, false),
    autoGeofencingEnabled = prefs.getBoolean(KEY_AUTO_GEOFENCING, false),
    workplaceLat = prefs.getString(KEY_WORKPLACE_LAT, "0.0")?.toDoubleOrNull() ?: 0.0,
    workplaceLng = prefs.getString(KEY_WORKPLACE_LNG, "0.0")?.toDoubleOrNull() ?: 0.0,
    vacationDaysPerYear = prefs.getInt(KEY_VACATION_DAYS_PER_YEAR, 16)
)

fun formatDate(epochMillis: Long): String = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(epochMillis))
fun formatEntryDate(epochMillis: Long): String = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
fun formatTime(epochMillis: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
fun formatTime12(epochMillis: Long): String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(epochMillis))
fun formatShiftTime(epochMillis: Long, use24HourClock: Boolean): String =
    if (use24HourClock) formatTime(epochMillis) else formatTime12(epochMillis)

fun formatDuration(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
        hours > 0L -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatCurrency(amount: Double, symbol: String = "€"): String {
    val decimalFormat = DecimalFormat("#,##0.00")
    return "$symbol${decimalFormat.format(amount)}"
}

fun saveEntries(prefs: SharedPreferences, entries: List<ShiftEntry>) {
    val payload = entries.joinToString(ENTRY_DELIMITER) { entry ->
        listOf(
            entry.startedAtMillis,
            entry.shiftType.name,
            entry.durationMinutes,
            entry.unpaidBreakMinutes,
            entry.hourlyRate,
            entry.estimatedPay,
            URLEncoder.encode(entry.notes, "UTF-8")
        ).joinToString(FIELD_DELIMITER)
    }
    prefs.edit().putString(KEY_ENTRIES, payload).apply()
}

fun loadEntries(prefs: SharedPreferences): List<ShiftEntry> {
    val payload = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
    if (payload.isBlank()) return emptyList()
    return payload.split(ENTRY_DELIMITER).mapNotNull { row ->
        val fields = row.split(FIELD_DELIMITER)
        if (fields.size < 6) return@mapNotNull null
        val startedAt = fields[0].toLongOrNull() ?: return@mapNotNull null
        val legacyType = fields[1]
        val shiftType = when (legacyType) {
            "MORNING" -> ShiftType.REGULAR
            "NIGHT" -> ShiftType.NIGHT
            else -> runCatching { ShiftType.valueOf(legacyType) }.getOrNull()
        } ?: return@mapNotNull null
        ShiftEntry(
            startedAtMillis = startedAt,
            shiftType = shiftType,
            durationMinutes = fields[2].toLongOrNull() ?: return@mapNotNull null,
            unpaidBreakMinutes = fields[3].toIntOrNull() ?: return@mapNotNull null,
            hourlyRate = fields[4].toDoubleOrNull() ?: return@mapNotNull null,
            estimatedPay = fields[5].toDoubleOrNull() ?: return@mapNotNull null,
            notes = fields.getOrNull(6)?.let { URLDecoder.decode(it, "UTF-8") }.orEmpty()
        )
    }.sortedByDescending { it.startedAtMillis }
}

fun entriesForMonth(entries: List<ShiftEntry>, year: Int, month: Int): List<ShiftEntry> = entries.filter {
    val cal = Calendar.getInstance().apply { timeInMillis = it.startedAtMillis }
    cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
}

fun vacationDaysUsedThisYear(entries: List<ShiftEntry>): Int {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return entries.count {
        it.shiftType == ShiftType.VACATION &&
            Calendar.getInstance().apply { timeInMillis = it.startedAtMillis }.get(Calendar.YEAR) == currentYear
    }
}

fun exportPrefsToJson(prefs: SharedPreferences): JSONObject {
    val root = JSONObject()
    prefs.all.forEach { (key, value) ->
        when (value) {
            is Set<*> -> root.put(key, JSONArray(value.toList()))
            else -> root.put(key, value)
        }
    }
    return root
}

fun importPrefsFromJson(context: Context, prefs: SharedPreferences, json: String) {
    val root = JSONObject(json)
    prefs.edit().clear().apply()
    val editor = prefs.edit()
    root.keys().forEach { key ->
        when (val value = root.get(key)) {
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Double -> editor.putString(key, value.toString())
            is Boolean -> editor.putBoolean(key, value)
            is JSONArray -> {
                val set = buildSet {
                    for (i in 0 until value.length()) add(value.getString(i))
                }
                editor.putStringSet(key, set)
            }
            else -> editor.putString(key, value.toString())
        }
    }
    editor.apply()
}
