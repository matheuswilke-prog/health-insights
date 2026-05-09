package com.healthinsights.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h892dp-xxhdpi")
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun content_displaysPrivacyPolicyAndConsents() {
        composeRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState.Content(sampleModel),
                onBack = {},
                onOpenHealthConnect = {},
            )
        }

        composeRule.onNodeWithText("Privacidade").assertIsDisplayed()
        composeRule.onNodeWithText("Seus dados ficam no aparelho").assertIsDisplayed()
        composeRule.onNodeWithText("CONSENTIMENTOS").assertIsDisplayed()
        composeRule.onNodeWithText("Calorias").assertIsDisplayed()
        composeRule.onNodeWithText("Peso").assertIsDisplayed()
        composeRule.onNodeWithText("POLÍTICA DE PRIVACIDADE").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun actions_invokeCallbacks() {
        var backCount = 0
        var healthConnectCount = 0
        composeRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState.Content(sampleModel),
                onBack = { backCount++ },
                onOpenHealthConnect = { healthConnectCount++ },
            )
        }

        composeRule.onNodeWithContentDescription("Voltar").performClick()
        composeRule.onNodeWithTag("open_health_connect_button").performClick()

        assertEquals(1, backCount)
        assertEquals(1, healthConnectCount)
    }

    private val sampleModel = SettingsUiModel(
        consents = listOf(
            ConsentItemUiModel(
                dataType = "calories",
                title = "Calorias",
                description = "Gasto ativo e ingestão calórica do Health Connect.",
                granted = true,
                policyVersion = "consent-copy-v1.1",
            ),
            ConsentItemUiModel(
                dataType = "weight",
                title = "Peso",
                description = "Peso corporal e data da medição.",
                granted = false,
                policyVersion = "consent-copy-v1.1",
            ),
        ),
    )
}
