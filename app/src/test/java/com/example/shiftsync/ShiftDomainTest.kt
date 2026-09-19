package com.example.shiftsync

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Minimal in-memory SharedPreferences fake covering only the String-based get/put calls that
 * ShiftDomain's entry storage functions use — enough for plain JUnit tests without pulling in
 * Robolectric.
 */
private class FakeSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getAll(): MutableMap<String, *> = map
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String>) ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor()
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class FakeEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var cleared = false
        override fun putString(key: String?, value: String?) = apply { pending[key!!] = value }
        override fun putStringSet(key: String?, values: MutableSet<String>?) = apply { pending[key!!] = values }
        override fun putInt(key: String?, value: Int) = apply { pending[key!!] = value }
        override fun putLong(key: String?, value: Long) = apply { pending[key!!] = value }
        override fun putFloat(key: String?, value: Float) = apply { pending[key!!] = value }
        override fun putBoolean(key: String?, value: Boolean) = apply { pending[key!!] = value }
        override fun remove(key: String?) = apply { pending[key!!] = null }
        override fun clear() = apply { cleared = true }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            if (cleared) map.clear()
            pending.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }
}

class ShiftDomainTest {

    private fun entry(
        startedAtMillis: Long = 0L,
        shiftType: ShiftType = ShiftType.REGULAR,
        durationMinutes: Long = 480L,
        id: String = java.util.UUID.randomUUID().toString()
    ) = ShiftEntry(
        startedAtMillis = startedAtMillis,
        shiftType = shiftType,
        durationMinutes = durationMinutes,
        unpaidBreakMinutes = 0,
        hourlyRate = 20.0,
        estimatedPay = 0.0,
        id = id
    )

    // MARK: - daysLabel

    @Test
    fun daysLabel_empty() {
        assertEquals("No days selected", daysLabel(emptySet()))
    }

    @Test
    fun daysLabel_allSeven() {
        assertEquals("Every day", daysLabel(setOf("1", "2", "3", "4", "5", "6", "7")))
    }

    @Test
    fun daysLabel_defaultWeekdays() {
        assertEquals("Weekdays (Mon–Fri)", daysLabel(DEFAULT_REMINDER_DAYS))
    }

    @Test
    fun daysLabel_customSubsetOrderedMondayFirst() {
        assertEquals("Mon, Wed, Fri", daysLabel(setOf("6", "2", "4")))
    }

    // MARK: - PayrollCalculator.hourlyRate

    @Test
    fun hourlyRate_hourlyPaymentType_returnsRateDirectly() {
        val settings = AppSettings(paymentType = PaymentType.HOURLY, salaryAmount = 25.0)
        assertEquals(25.0, PayrollCalculator.hourlyRate(settings), 0.001)
    }

    @Test
    fun hourlyRate_monthlyPaymentType_derivesFromWorkDayHours() {
        val settings = AppSettings(paymentType = PaymentType.MONTHLY, salaryAmount = 4400.0, workDayHours = 8.0)
        // 4400 / (8 * 22) = 25.0
        assertEquals(25.0, PayrollCalculator.hourlyRate(settings), 0.001)
    }

    // MARK: - PayrollCalculator.estimatePay

    @Test
    fun estimatePay_vacation_fallsBackToMonthlySalaryOverTwenty() {
        val pay = PayrollCalculator.estimatePay(
            totalDurationMinutes = 8 * 60L, unpaidBreakMinutes = 0, hourlyRate = 20.0,
            shiftType = ShiftType.VACATION, monthlySalary = 4000.0
        )
        assertEquals(200.0, pay, 0.001)
    }

    @Test
    fun estimatePay_regularUnderThreshold_noOvertimeApplied() {
        val pay = PayrollCalculator.estimatePay(
            totalDurationMinutes = 6 * 60L, unpaidBreakMinutes = 0, hourlyRate = 20.0,
            shiftType = ShiftType.REGULAR, overtimeEnabled = true, overtimeThresholdMinutes = 8 * 60L
        )
        assertEquals(120.0, pay, 0.001)
    }

    // MARK: - saveEntries / loadEntries round-trip

    @Test
    fun saveAndLoadEntries_roundTripsIdAndFields() {
        val prefs = FakeSharedPreferences()
        val e = entry(startedAtMillis = 1000L, durationMinutes = 500L, id = "fixed-id")
        saveEntries(prefs, listOf(e))

        val loaded = loadEntries(prefs)
        assertEquals(1, loaded.size)
        assertEquals("fixed-id", loaded[0].id)
        assertEquals(500L, loaded[0].durationMinutes)
    }

    @Test
    fun loadEntries_legacyRowMissingId_getsGeneratedId() {
        val prefs = FakeSharedPreferences()
        // 6-field legacy row with no id/notes suffix
        prefs.edit().putString(KEY_ENTRIES, "1000,REGULAR,480,0,20.0,160.0").apply()

        val loaded = loadEntries(prefs)
        assertEquals(1, loaded.size)
        assertTrue(loaded[0].id.isNotBlank())
    }

    // MARK: - updateEntry / deleteEntry

    @Test
    fun updateEntry_replacesMatchingEntryById() {
        val prefs = FakeSharedPreferences()
        val original = entry(id = "abc", durationMinutes = 100L)
        saveEntries(prefs, listOf(original))

        updateEntry(prefs, "abc", original.copy(durationMinutes = 200L))

        val loaded = loadEntries(prefs)
        assertEquals(1, loaded.size)
        assertEquals(200L, loaded[0].durationMinutes)
    }

    @Test
    fun deleteEntry_removesMatchingEntryById() {
        val prefs = FakeSharedPreferences()
        val a = entry(id = "a")
        val b = entry(id = "b")
        saveEntries(prefs, listOf(a, b))

        deleteEntry(prefs, "a")

        val loaded = loadEntries(prefs)
        assertEquals(1, loaded.size)
        assertEquals("b", loaded[0].id)
    }

    // MARK: - reapplyOvertimeRulesToExistingEntries

    @Test
    fun reapplyOvertimeRules_splitsEligiblePastRegularEntry() {
        val prefs = FakeSharedPreferences()
        val long = entry(startedAtMillis = 0L, shiftType = ShiftType.REGULAR, durationMinutes = 600L, id = "long")
        saveEntries(prefs, listOf(long))

        val settings = AppSettings(overtimeEnabled = true, overtimeDailyThresholdHours = 8.0, overtimeMultiplier = 1.5, paymentType = PaymentType.HOURLY, salaryAmount = 20.0)
        reapplyOvertimeRulesToExistingEntries(prefs, settings)

        val loaded = loadEntries(prefs).sortedBy { it.startedAtMillis }
        assertEquals(2, loaded.size)
        assertEquals(ShiftType.REGULAR, loaded[0].shiftType)
        assertEquals(480L, loaded[0].durationMinutes)
        assertEquals(ShiftType.OVERTIME, loaded[1].shiftType)
        assertEquals(120L, loaded[1].durationMinutes)
    }

    @Test
    fun reapplyOvertimeRules_leavesShortAndDayTypeEntriesUntouched() {
        val prefs = FakeSharedPreferences()
        val short = entry(shiftType = ShiftType.REGULAR, durationMinutes = 300L, id = "short")
        val vacation = entry(shiftType = ShiftType.VACATION, durationMinutes = 480L, id = "vac")
        saveEntries(prefs, listOf(short, vacation))

        val settings = AppSettings(overtimeEnabled = true, overtimeDailyThresholdHours = 8.0, overtimeMultiplier = 1.5)
        reapplyOvertimeRulesToExistingEntries(prefs, settings)

        val loaded = loadEntries(prefs)
        assertEquals(2, loaded.size)
        assertTrue(loaded.any { it.id == "short" })
        assertTrue(loaded.any { it.id == "vac" })
    }

    @Test
    fun reapplyOvertimeRules_noOpWhenOvertimeDisabled() {
        val prefs = FakeSharedPreferences()
        val long = entry(shiftType = ShiftType.REGULAR, durationMinutes = 600L, id = "long")
        saveEntries(prefs, listOf(long))

        val settings = AppSettings(overtimeEnabled = false, overtimeDailyThresholdHours = 8.0)
        reapplyOvertimeRulesToExistingEntries(prefs, settings)

        val loaded = loadEntries(prefs)
        assertEquals(1, loaded.size)
        assertEquals(600L, loaded[0].durationMinutes)
    }
}
