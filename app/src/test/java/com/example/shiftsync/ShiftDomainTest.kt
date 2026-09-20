package com.example.shiftsync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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

    @Test
    fun estimatePay_holiday_paysDoubleTheDailyRate() {
        val pay = PayrollCalculator.estimatePay(
            totalDurationMinutes = 8 * 60L, unpaidBreakMinutes = 0, hourlyRate = 20.0,
            shiftType = ShiftType.HOLIDAY, monthlySalary = 4000.0, workDayHours = 8.0
        )
        // dailyRate (4000/20=200) * holiday multiplier 2.0
        assertEquals(400.0, pay, 0.001)
    }

    @Test
    fun estimatePay_sickAndFormationAndCompanyFunDay_payFullDailyRateLikeVacation() {
        val settings = mapOf(
            ShiftType.SICK to 200.0,
            ShiftType.FORMATION to 200.0,
            ShiftType.COMPANY_FUN_DAY to 200.0
        )
        settings.forEach { (type, expected) ->
            val pay = PayrollCalculator.estimatePay(
                totalDurationMinutes = 8 * 60L, unpaidBreakMinutes = 0, hourlyRate = 20.0,
                shiftType = type, monthlySalary = 4000.0, workDayHours = 8.0
            )
            assertEquals(expected, pay, 0.001)
        }
    }

    @Test
    fun shiftType_isDayType_matchesExpectedSet() {
        assertTrue(ShiftType.VACATION.isDayType)
        assertTrue(ShiftType.SICK.isDayType)
        assertTrue(ShiftType.FORMATION.isDayType)
        assertTrue(ShiftType.HOLIDAY.isDayType)
        assertTrue(ShiftType.COMPANY_FUN_DAY.isDayType)
        assertEquals(false, ShiftType.REGULAR.isDayType)
        assertEquals(false, ShiftType.OVERTIME.isDayType)
        assertEquals(false, ShiftType.NIGHT.isDayType)
    }

    // MARK: - parseLegacyEntries (pre-Room delimited format, used for migration + backup import)

    @Test
    fun parseLegacyEntries_roundTripsIdAndFields() {
        val payload = serializeEntries(listOf(entry(startedAtMillis = 1000L, durationMinutes = 500L, id = "fixed-id")))

        val parsed = parseLegacyEntries(payload)

        assertEquals(1, parsed.size)
        assertEquals("fixed-id", parsed[0].id)
        assertEquals(500L, parsed[0].durationMinutes)
    }

    @Test
    fun parseLegacyEntries_rowMissingId_getsGeneratedId() {
        // 6-field legacy row with no id/notes suffix
        val parsed = parseLegacyEntries("1000,REGULAR,480,0,20.0,160.0")

        assertEquals(1, parsed.size)
        assertTrue(parsed[0].id.isNotBlank())
    }

    @Test
    fun parseLegacyEntries_blankOrNullPayload_returnsEmpty() {
        assertEquals(0, parseLegacyEntries(null).size)
        assertEquals(0, parseLegacyEntries("").size)
    }

    // MARK: - reapplyOvertimeRulesToEntries

    @Test
    fun reapplyOvertimeRules_splitsEligiblePastRegularEntry() {
        val entries = listOf(entry(startedAtMillis = 0L, shiftType = ShiftType.REGULAR, durationMinutes = 600L, id = "long"))
        val settings = AppSettings(overtimeEnabled = true, overtimeDailyThresholdHours = 8.0, overtimeMultiplier = 1.5, paymentType = PaymentType.HOURLY, salaryAmount = 20.0)

        val result = reapplyOvertimeRulesToEntries(entries, settings).sortedBy { it.startedAtMillis }

        assertEquals(2, result.size)
        assertEquals(ShiftType.REGULAR, result[0].shiftType)
        assertEquals(480L, result[0].durationMinutes)
        assertEquals(ShiftType.OVERTIME, result[1].shiftType)
        assertEquals(120L, result[1].durationMinutes)
    }

    @Test
    fun reapplyOvertimeRules_leavesShortAndDayTypeEntriesUntouched() {
        val short = entry(shiftType = ShiftType.REGULAR, durationMinutes = 300L, id = "short")
        val vacation = entry(shiftType = ShiftType.VACATION, durationMinutes = 480L, id = "vac")
        val settings = AppSettings(overtimeEnabled = true, overtimeDailyThresholdHours = 8.0, overtimeMultiplier = 1.5)

        val result = reapplyOvertimeRulesToEntries(listOf(short, vacation), settings)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "short" })
        assertTrue(result.any { it.id == "vac" })
    }

    @Test
    fun reapplyOvertimeRules_noOpWhenOvertimeDisabled() {
        val entries = listOf(entry(shiftType = ShiftType.REGULAR, durationMinutes = 600L, id = "long"))
        val settings = AppSettings(overtimeEnabled = false, overtimeDailyThresholdHours = 8.0)

        val result = reapplyOvertimeRulesToEntries(entries, settings)

        assertEquals(1, result.size)
        assertEquals(600L, result[0].durationMinutes)
    }
}
