package com.healthinsights.feature.dashboard

import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.UserGoal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

internal class DashboardFormatter(
    private val locale: Locale = Locale("pt", "BR"),
) {
    private val numberFormat = NumberFormat.getIntegerInstance(locale)
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", locale)

    fun dayLabel(date: LocalDate): String =
        date.format(dateFormatter).replace(".", "")

    fun kcal(value: Int): String = numberFormat.format(value)

    fun signedKcal(value: Int): String =
        when {
            value > 0 -> "+${kcal(value)}"
            value < 0 -> "-${kcal(-value)}"
            else -> "0"
        }

    fun kg(value: Float): String =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format((value * 10f).roundToInt() / 10f)

    fun measuredAtLabel(
        measuredAt: Instant?,
        today: LocalDate,
        zone: ZoneId,
    ): String? {
        val instant = measuredAt ?: return null
        val date = instant.atZone(zone).toLocalDate()
        return when (date) {
            today -> "hoje"
            today.minusDays(1) -> "ontem"
            else -> date.format(DateTimeFormatter.ofPattern("d MMM", locale)).replace(".", "")
        }
    }

    fun goalLabel(goal: UserGoal): String =
        when (goal) {
            UserGoal.LOSE -> "emagrecer"
            UserGoal.MAINTAIN -> "manter"
            UserGoal.GAIN -> "ganhar massa"
        }

    fun statusLabel(status: BalanceStatus): String =
        when (status) {
            BalanceStatus.Deficit -> "Em déficit"
            BalanceStatus.Maintain -> "Em manutenção"
            BalanceStatus.Surplus -> "Em superávit"
            BalanceStatus.NoIntakeData -> "Sem ingestão"
            BalanceStatus.HealthConnectUnavailable -> "Indisponível"
        }
}
