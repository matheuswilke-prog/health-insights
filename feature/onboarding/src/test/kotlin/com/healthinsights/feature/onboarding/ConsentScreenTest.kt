package com.healthinsights.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.ui.theme.HealthInsightsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h892dp-xxhdpi")
class ConsentScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultProfile = ProfileFormData(
        sex = BiologicalSex.MALE,
        weightKg = 80f,
        heightCm = 180,
        ageYears = 30,
    )

    private fun setConsentContent(
        goal: UserGoal = UserGoal.LOSE,
        onConfirm: (ProfileFormData, Set<ConsentDataType>) -> Unit = { _, _ -> },
        onSkip: () -> Unit = {},
    ) {
        composeRule.setContent {
            HealthInsightsTheme {
                ConsentScreenContent(
                    profileData = defaultProfile,
                    goal = goal,
                    onConfirm = onConfirm,
                    onSkip = onSkip,
                )
            }
        }
    }

    /** Scrolls the screen to the bottom to unlock the CTA. */
    private fun scrollToBottom() {
        repeat(5) {
            composeRule.onNodeWithText("Antes de começar — seus dados de saúde")
                .performTouchInput { swipeUp() }
        }
    }

    // ── Display tests ─────────────────────────────────────────────────────────

    @Test
    fun displaysHeadline() {
        setConsentContent()
        composeRule
            .onNodeWithText("Antes de começar — seus dados de saúde")
            .assertIsDisplayed()
    }

    @Test
    fun displaysAllThreeToggleTitles() {
        setConsentContent()
        composeRule.onNodeWithText("Calorias gastas e ingeridas").assertIsDisplayed()
        composeRule.onNodeWithText("Peso corporal").assertIsDisplayed()
        composeRule.onNodeWithText("Treinos").assertIsDisplayed()
    }

    @Test
    fun displaysPrivacyGuarantee() {
        setConsentContent()
        scrollToBottom()
        composeRule
            .onNodeWithText("Seus dados nunca saem do seu aparelho", substring = true)
            .assertIsDisplayed()
    }

    // ── LGPD Art. 8 §3 — toggles default OFF ─────────────────────────────────

    @Test
    fun ctaDefaultText_isNoHealth_whenAllTogglesOff() {
        setConsentContent()
        scrollToBottom()
        composeRule
            .onNodeWithText("Continuar sem dados de saúde")
            .assertIsDisplayed()
    }

    // ── Toggle interaction → CTA text changes ─────────────────────────────────

    @Test
    fun toggleCalories_changesCta_toGranted() {
        setConsentContent()
        composeRule.onNodeWithTag("switch_calories").performClick()
        scrollToBottom()
        composeRule
            .onNodeWithText("Concordo — permitir acesso")
            .assertIsDisplayed()
    }

    @Test
    fun toggleWeight_changesCta_toGranted() {
        setConsentContent()
        composeRule.onNodeWithTag("switch_weight").performClick()
        scrollToBottom()
        composeRule
            .onNodeWithText("Concordo — permitir acesso")
            .assertIsDisplayed()
    }

    @Test
    fun toggleExercise_changesCta_toGranted() {
        setConsentContent()
        composeRule.onNodeWithTag("switch_exercise").performClick()
        scrollToBottom()
        composeRule
            .onNodeWithText("Concordo — permitir acesso")
            .assertIsDisplayed()
    }

    @Test
    fun toggleOnThenOff_revertsCta_toNoHealth() {
        setConsentContent()
        // Toggle on
        composeRule.onNodeWithTag("switch_calories").performClick()
        // Toggle off again
        composeRule.onNodeWithTag("switch_calories").performClick()
        scrollToBottom()
        composeRule
            .onNodeWithText("Continuar sem dados de saúde")
            .assertIsDisplayed()
    }

    // ── onConfirm receives correct granted set ─────────────────────────────────

    @Test
    fun confirmWithNoToggles_passesEmptySet() {
        var capturedSet: Set<ConsentDataType>? = null
        setConsentContent(onConfirm = { _, types -> capturedSet = types })
        scrollToBottom()
        composeRule.onNodeWithText("Continuar sem dados de saúde").performClick()
        assertNotNull(capturedSet)
        assertTrue(capturedSet!!.isEmpty())
    }

    @Test
    fun confirmWithCaloriesOnly_passesCaloriesInSet() {
        var capturedSet: Set<ConsentDataType>? = null
        setConsentContent(onConfirm = { _, types -> capturedSet = types })
        composeRule.onNodeWithTag("switch_calories").performClick()
        scrollToBottom()
        composeRule.onNodeWithText("Concordo — permitir acesso").performClick()
        assertNotNull(capturedSet)
        assertTrue(capturedSet!!.contains(ConsentDataType.CALORIES))
        assertFalse(capturedSet!!.contains(ConsentDataType.WEIGHT))
        assertFalse(capturedSet!!.contains(ConsentDataType.EXERCISE))
    }

    @Test
    fun confirmWithAllToggles_passesFullSet() {
        var capturedSet: Set<ConsentDataType>? = null
        setConsentContent(onConfirm = { _, types -> capturedSet = types })
        composeRule.onNodeWithTag("switch_calories").performClick()
        composeRule.onNodeWithTag("switch_weight").performClick()
        composeRule.onNodeWithTag("switch_exercise").performClick()
        scrollToBottom()
        composeRule.onNodeWithText("Concordo — permitir acesso").performClick()
        assertNotNull(capturedSet)
        assertEquals(3, capturedSet!!.size)
        assertTrue(capturedSet!!.containsAll(ConsentDataType.entries.toSet()))
    }

    @Test
    fun confirmReceivesCorrectProfileData() {
        var capturedProfile: ProfileFormData? = null
        setConsentContent(onConfirm = { profile, _ -> capturedProfile = profile })
        scrollToBottom()
        composeRule.onNodeWithText("Continuar sem dados de saúde").performClick()
        assertNotNull(capturedProfile)
        assertEquals(defaultProfile, capturedProfile)
    }

    // ── onSkip callback ───────────────────────────────────────────────────────

    @Test
    fun backButton_invokesOnSkip() {
        var called = false
        setConsentContent(onSkip = { called = true })
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        assertEquals(true, called)
    }

    @Test
    fun skipTextButton_invokesOnSkip() {
        var called = false
        setConsentContent(onSkip = { called = true })
        scrollToBottom()
        composeRule
            .onNodeWithText("Agora não — usar com funções limitadas")
            .performClick()
        assertEquals(true, called)
    }
}
