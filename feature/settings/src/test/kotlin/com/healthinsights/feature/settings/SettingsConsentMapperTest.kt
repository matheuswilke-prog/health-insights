package com.healthinsights.feature.settings

import com.healthinsights.core.domain.model.ConsentRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsConsentMapperTest {

    @Test
    fun map_returnsAllMvpDataTypesWithConsentState() {
        val result = SettingsConsentMapper.map(
            listOf(
                ConsentRecord(
                    dataType = "calories",
                    granted = true,
                    grantedAt = 1L,
                    policyVersion = "consent-copy-v1.1",
                ),
                ConsentRecord(
                    dataType = "weight",
                    granted = false,
                    grantedAt = 2L,
                    policyVersion = "consent-copy-v1.1",
                ),
            ),
        )

        assertEquals(listOf("calories", "weight", "exercise"), result.map { it.dataType })
        assertTrue(result.first { it.dataType == "calories" }.granted)
        assertFalse(result.first { it.dataType == "weight" }.granted)
        assertFalse(result.first { it.dataType == "exercise" }.granted)
        assertEquals("consent-copy-v1.1", result.first { it.dataType == "calories" }.policyVersion)
    }
}
