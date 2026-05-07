package com.healthinsights.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.healthinsights.core.domain.model.BiologicalSex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h892dp-xxhdpi")
class ProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ── Display tests ─────────────────────────────────────────────────────────

    @Test
    fun displaysHeadline() {
        composeRule.setContent { ProfileScreen(onBack = {}, onContinue = {}) }
        composeRule
            .onNodeWithText("Vamos calcular\nseu metabolismo basal.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun displaysSubheadline() {
        composeRule.setContent { ProfileScreen(onBack = {}, onContinue = {}) }
        composeRule
            .onNodeWithText("Esses dados ficam no seu aparelho", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun displaysSexOptions() {
        composeRule.setContent { ProfileScreen(onBack = {}, onContinue = {}) }
        composeRule.onNodeWithText("Masculino").assertIsDisplayed()
        composeRule.onNodeWithText("Feminino").assertIsDisplayed()
    }

    @Test
    fun displaysCta() {
        composeRule.setContent { ProfileScreen(onBack = {}, onContinue = {}) }
        composeRule.onNodeWithText("Continuar").assertIsDisplayed()
    }

    // ── CTA / validation tests ────────────────────────────────────────────────

    @Test
    fun ctaEnabled_whenFieldsEmpty_butDoesNotInvokeOnContinue() {
        // CTA is always enabled — validation fires on click (submit-time)
        var called = false
        composeRule.setContent { ProfileScreen(onBack = {}, onContinue = { called = true }) }
        composeRule.onNodeWithText("Continuar").assertIsEnabled()
        composeRule.onNodeWithText("Continuar").performClick()
        assertEquals(false, called) // form invalid — onContinue NOT called
    }

    @Test
    fun showsValidationError_onSubmitWithEmptyFields() {
        composeRule.setContent { ProfileScreen(onBack = {}, onContinue = {}) }
        composeRule.onNodeWithText("Continuar").performClick()
        composeRule.onNodeWithText("Informe um peso válido").assertIsDisplayed()
        composeRule.onNodeWithText("Informe uma altura válida").assertIsDisplayed()
        composeRule.onNodeWithText("Informe uma idade válida").assertIsDisplayed()
    }

    @Test
    fun ctaClick_invokesOnContinue_whenFormValid() {
        var result: ProfileFormData? = null
        composeRule.setContent {
            ProfileScreen(onBack = {}, onContinue = { result = it })
        }

        composeRule.onNodeWithTag("field_weight").performTextInput("78")
        composeRule.onNodeWithTag("field_height").performTextInput("178")
        composeRule.onNodeWithTag("field_age").performTextInput("30")
        composeRule.onNodeWithText("Continuar").performClick()

        assertNotNull(result)
        assertEquals(78f, result!!.weightKg)
        assertEquals(178, result!!.heightCm)
        assertEquals(30, result!!.ageYears)
        assertEquals(BiologicalSex.MALE, result!!.sex) // default
    }

    @Test
    fun selectFemale_changesSexInResult() {
        var result: ProfileFormData? = null
        composeRule.setContent {
            ProfileScreen(onBack = {}, onContinue = { result = it })
        }

        composeRule.onNodeWithText("Feminino").performClick()
        composeRule.onNodeWithTag("field_weight").performTextInput("60")
        composeRule.onNodeWithTag("field_height").performTextInput("165")
        composeRule.onNodeWithTag("field_age").performTextInput("25")
        composeRule.onNodeWithText("Continuar").performClick()

        assertNotNull(result)
        assertEquals(BiologicalSex.FEMALE, result!!.sex)
    }

    @Test
    fun backClick_invokesOnBack() {
        var called = false
        composeRule.setContent { ProfileScreen(onBack = { called = true }, onContinue = {}) }
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        assertEquals(true, called)
    }

    // ── Validation unit tests (pure logic, no UI) ─────────────────────────────

    @Test
    fun validateWeight_emptyString_returnsError() {
        assertNotNull(validateWeight(""))
    }

    @Test
    fun validateWeight_belowMin_returnsError() {
        assertNotNull(validateWeight("29"))
    }

    @Test
    fun validateWeight_aboveMax_returnsError() {
        assertNotNull(validateWeight("301"))
    }

    @Test
    fun validateWeight_validValue_returnsNull() {
        assertNull(validateWeight("78,4"))
        assertNull(validateWeight("78.4"))
        assertNull(validateWeight("30"))
        assertNull(validateWeight("300"))
    }

    @Test
    fun validateHeight_belowMin_returnsError() {
        assertNotNull(validateHeight("99"))
    }

    @Test
    fun validateHeight_aboveMax_returnsError() {
        assertNotNull(validateHeight("251"))
    }

    @Test
    fun validateHeight_validValue_returnsNull() {
        assertNull(validateHeight("178"))
    }

    @Test
    fun validateAge_belowMin_returnsError() {
        assertNotNull(validateAge("12"))
    }

    @Test
    fun validateAge_aboveMax_returnsError() {
        assertNotNull(validateAge("121"))
    }

    @Test
    fun validateAge_validValue_returnsNull() {
        assertNull(validateAge("30"))
    }

}
