package com.healthinsights.feature.dashboard

import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.UserGoal
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DashboardFormatterTest {
    private val formatter = DashboardFormatter()
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val today = LocalDate.of(2026, 5, 8)

    @Test
    fun formats_signed_kcal_without_treating_negative_as_zero() {
        assertEquals("-470", formatter.signedKcal(-470))
        assertEquals("+250", formatter.signedKcal(250))
        assertEquals("0", formatter.signedKcal(0))
    }

    @Test
    fun formats_weight_with_one_decimal() {
        assertEquals("82,4", formatter.kg(82.44f))
    }

    @Test
    fun formats_weight_measurement_date_without_trend() {
        val instant = Instant.parse("2026-05-08T12:00:00Z")

        assertEquals("hoje", formatter.measuredAtLabel(instant, today, zone))
    }

    @Test
    fun maps_user_facing_labels() {
        assertEquals("emagrecer", formatter.goalLabel(UserGoal.LOSE))
        assertEquals("Em manutenção", formatter.statusLabel(BalanceStatus.Maintain))
    }
}
