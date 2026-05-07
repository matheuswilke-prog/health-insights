package com.healthinsights.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WelcomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysHeadline() {
        composeRule.setContent { WelcomeScreen(onContinue = {}) }
        composeRule
            .onNodeWithText("Saiba se está em déficit ou superávit calórico.")
            .assertIsDisplayed()
    }

    @Test
    fun displaysSubheadline() {
        composeRule.setContent { WelcomeScreen(onContinue = {}) }
        composeRule
            .onNodeWithText(
                "O Health Insights conecta seus dados do Samsung Health e mostra, em números, se você está perdendo, mantendo ou ganhando peso.",
                substring = true,
            )
            .assertIsDisplayed()
    }

    @Test
    fun displaysPrivacyMicrocopy() {
        composeRule.setContent { WelcomeScreen(onContinue = {}) }
        composeRule
            .onNodeWithText("Seus dados ficam só no seu aparelho.")
            .assertIsDisplayed()
    }

    @Test
    fun displaysCta() {
        composeRule.setContent { WelcomeScreen(onContinue = {}) }
        composeRule.onNodeWithText("Começar").assertIsDisplayed()
    }

    @Test
    fun ctaClick_invokesOnContinue() {
        var called = false
        composeRule.setContent { WelcomeScreen(onContinue = { called = true }) }
        composeRule.onNodeWithText("Começar").performClick()
        assertTrue(called)
    }
}