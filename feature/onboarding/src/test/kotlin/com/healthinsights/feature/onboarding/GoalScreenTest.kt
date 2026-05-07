package com.healthinsights.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.ui.theme.HealthInsightsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h892dp-xxhdpi")
class GoalScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultProfile = ProfileFormData(
        sex = BiologicalSex.MALE,
        weightKg = 80f,
        heightCm = 180,
        ageYears = 30,
    )

    // ── Display tests ─────────────────────────────────────────────────────────

    @Test
    fun displaysHeadline() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        composeRule.onNodeWithText("Qual é o seu objetivo?").assertIsDisplayed()
    }

    @Test
    fun displaysAllThreeGoalCards() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        composeRule.onNodeWithText("Perder peso").assertIsDisplayed()
        composeRule.onNodeWithText("Manter peso").assertIsDisplayed()
        composeRule.onNodeWithText("Ganhar peso").assertIsDisplayed()
    }

    @Test
    fun displaysDailyTargetPreview() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        // "Sua meta diária" header present
        composeRule.onNodeWithText("SUA META DIÁRIA").assertIsDisplayed()
        // Unit suffix present
        composeRule.onNodeWithText("kcal/dia").assertIsDisplayed()
    }

    @Test
    fun displaysCta() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        composeRule.onNodeWithText("Continuar").assertIsDisplayed()
    }

    // ── Default selection ─────────────────────────────────────────────────────

    @Test
    fun defaultGoalIsMaintain() {
        var result: UserGoal? = null
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(
                    profileData = defaultProfile,
                    onBack = {},
                    onContinue = { result = it },
                )
            }
        }
        composeRule.onNodeWithText("Continuar").performClick()
        assertNotNull(result)
        assertEquals(UserGoal.MAINTAIN, result)
    }

    // ── Goal selection ────────────────────────────────────────────────────────

    @Test
    fun selectLose_thenContinue_passesLoseGoal() {
        var result: UserGoal? = null
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(
                    profileData = defaultProfile,
                    onBack = {},
                    onContinue = { result = it },
                )
            }
        }
        composeRule.onNodeWithText("Perder peso").performClick()
        composeRule.onNodeWithText("Continuar").performClick()
        assertEquals(UserGoal.LOSE, result)
    }

    @Test
    fun selectGain_thenContinue_passesGainGoal() {
        var result: UserGoal? = null
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(
                    profileData = defaultProfile,
                    onBack = {},
                    onContinue = { result = it },
                )
            }
        }
        composeRule.onNodeWithText("Ganhar peso").performClick()
        composeRule.onNodeWithText("Continuar").performClick()
        assertEquals(UserGoal.GAIN, result)
    }

    @Test
    fun selectLoseThenMaintain_thenContinue_passesMaintainGoal() {
        var result: UserGoal? = null
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(
                    profileData = defaultProfile,
                    onBack = {},
                    onContinue = { result = it },
                )
            }
        }
        composeRule.onNodeWithText("Perder peso").performClick()
        composeRule.onNodeWithText("Manter peso").performClick()
        composeRule.onNodeWithText("Continuar").performClick()
        assertEquals(UserGoal.MAINTAIN, result)
    }

    // ── Daily target updates ──────────────────────────────────────────────────

    @Test
    fun selectLose_showsNegativeDeltaLabel() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        composeRule.onNodeWithText("Perder peso").performClick()
        // Delta label for LOSE is "−500 kcal/dia"
        composeRule.onNodeWithText("−500 kcal/dia").assertIsDisplayed()
    }

    @Test
    fun selectGain_showsPositiveDeltaLabel() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        composeRule.onNodeWithText("Ganhar peso").performClick()
        composeRule.onNodeWithText("+300 kcal/dia").assertIsDisplayed()
    }

    @Test
    fun defaultMaintain_showsZeroDelta() {
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(profileData = defaultProfile, onBack = {}, onContinue = {})
            }
        }
        composeRule.onNodeWithText("±0 kcal/dia").assertIsDisplayed()
    }

    // ── Back navigation ───────────────────────────────────────────────────────

    @Test
    fun backClick_invokesOnBack() {
        var called = false
        composeRule.setContent {
            HealthInsightsTheme {
                GoalScreen(
                    profileData = defaultProfile,
                    onBack = { called = true },
                    onContinue = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        assertEquals(true, called)
    }
}
