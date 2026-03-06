package com.example.shiftsync

import org.junit.Assert.assertEquals
import org.junit.Test

class PayrollCalculatorTest {
    @Test
    fun regularShift_subtractsBreak() {
        val pay = PayrollCalculator.estimatePay(
            totalDurationMinutes = 8 * 60L,
            unpaidBreakMinutes = 30,
            hourlyRate = 20.0,
            shiftType = ShiftType.MORNING
        )

        assertEquals(150.0, pay, 0.001)
    }

    @Test
    fun regularShift_appliesOvertimeAfterEightHours() {
        val pay = PayrollCalculator.estimatePay(
            totalDurationMinutes = 10 * 60L,
            unpaidBreakMinutes = 0,
            hourlyRate = 20.0,
            shiftType = ShiftType.NIGHT
        )

        assertEquals(220.0, pay, 0.001)
    }

    @Test
    fun overtimeShift_marksAllPayableTimeAsTimeAndAHalf() {
        val pay = PayrollCalculator.estimatePay(
            totalDurationMinutes = 4 * 60L,
            unpaidBreakMinutes = 0,
            hourlyRate = 20.0,
            shiftType = ShiftType.OVERTIME
        )

        assertEquals(120.0, pay, 0.001)
    }
}

