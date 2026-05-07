package com.healthinsights.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.healthinsights.core.ui.theme.HealthInsightsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h892dp-xxhdpi")
class ConnectingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ── Checking state ────────────────────────────────────────────────────────

    @Test
    fun checking_showsLoadingIndicator() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.Checking,
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeRule.onNodeWithText("Verificando Health Connect…").assertIsDisplayed()
    }

    // ── NotAvailable state ────────────────────────────────────────────────────

    @Test
    fun notAvailable_showsInstallTitle() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.NotAvailable,
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("not_available_title").assertIsDisplayed()
        composeRule.onNodeWithText("Instalar Health Connect").assertIsDisplayed()
    }

    @Test
    fun notAvailable_skipButton_invokesOnNavigateToDashboard() {
        var called = false
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.NotAvailable,
                    onNavigateToDashboard = { called = true },
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithText("Pular por agora — usar sem dados de saúde").performClick()
        assertTrue(called)
    }

    // ── UpdateRequired state ──────────────────────────────────────────────────

    @Test
    fun updateRequired_showsUpdateTitle() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.UpdateRequired,
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("update_required_title").assertIsDisplayed()
        composeRule.onNodeWithText("Atualizar na Play Store").assertIsDisplayed()
    }

    @Test
    fun updateRequired_skipButton_invokesOnNavigateToDashboard() {
        var called = false
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.UpdateRequired,
                    onNavigateToDashboard = { called = true },
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithText("Pular por agora — usar sem dados de saúde").performClick()
        assertTrue(called)
    }

    // ── PermissionResult — Full success ───────────────────────────────────────

    @Test
    fun permissionResult_fullSuccess_showsSuccessTitle() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 5, totalRequested = 5),
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("full_success_title").assertIsDisplayed()
    }

    @Test
    fun permissionResult_fullSuccess_noRetryButton() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 3, totalRequested = 3),
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        // Retry button should NOT appear on full success
        composeRule.onNodeWithTag("retry_button").assertDoesNotExist()
        composeRule.onNodeWithTag("continue_button").assertIsDisplayed()
    }

    @Test
    fun permissionResult_fullSuccess_continueButton_invokesOnNavigateToDashboard() {
        var called = false
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 5, totalRequested = 5),
                    onNavigateToDashboard = { called = true },
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("continue_button").performClick()
        assertTrue(called)
    }

    // ── PermissionResult — Partial success ────────────────────────────────────

    @Test
    fun permissionResult_partial_showsPartialTitle() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 2, totalRequested = 5),
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("partial_success_title").assertIsDisplayed()
    }

    @Test
    fun permissionResult_partial_showsRetryButton() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 2, totalRequested = 5),
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("retry_button").assertIsDisplayed()
    }

    @Test
    fun permissionResult_partial_retryButton_invokesOnRetry() {
        var called = false
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 1, totalRequested = 3),
                    onNavigateToDashboard = {},
                    onRetry = { called = true },
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("retry_button").performClick()
        assertTrue(called)
    }

    // ── PermissionResult — None granted ──────────────────────────────────────

    @Test
    fun permissionResult_noneGranted_showsNoneTitle() {
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 0, totalRequested = 3),
                    onNavigateToDashboard = {},
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("none_granted_title").assertIsDisplayed()
        composeRule.onNodeWithTag("retry_button").assertIsDisplayed()
    }

    @Test
    fun permissionResult_noneGranted_continueButton_invokesOnNavigateToDashboard() {
        var called = false
        composeRule.setContent {
            HealthInsightsTheme {
                ConnectingScreenContent(
                    uiState = ConnectingUiState.PermissionResult(grantedCount = 0, totalRequested = 3),
                    onNavigateToDashboard = { called = true },
                    onRetry = {},
                    onLaunchPermissions = {},
                )
            }
        }
        composeRule.onNodeWithTag("continue_button").performClick()
        assertTrue(called)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ConnectingViewModel unit tests (pure logic, no UI)
// ─────────────────────────────────────────────────────────────────────────────

class ConnectingViewModelLogicTest {

    @Test
    fun consentKeyToPermissions_calories_returnsCaloriesBurnedAndIntake() {
        val permissions = ConnectingViewModel.consentKeyToPermissions("calories")
        assertTrue(permissions.isNotEmpty())
        // Should contain permissions for both CALORIES_BURNED and CALORIE_INTAKE
        assertTrue(permissions.size >= 2)
    }

    @Test
    fun consentKeyToPermissions_weight_returnsWeightPermission() {
        val permissions = ConnectingViewModel.consentKeyToPermissions("weight")
        assertTrue(permissions.isNotEmpty())
        assertEquals(1, permissions.size)
    }

    @Test
    fun consentKeyToPermissions_exercise_returnsExercisePermission() {
        val permissions = ConnectingViewModel.consentKeyToPermissions("exercise")
        assertTrue(permissions.isNotEmpty())
        assertEquals(1, permissions.size)
    }

    @Test
    fun consentKeyToPermissions_unknown_returnsEmpty() {
        val permissions = ConnectingViewModel.consentKeyToPermissions("unknown_key")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun consentKeyToPermissions_allKeys_returnDistinctPermissions() {
        val calories = ConnectingViewModel.consentKeyToPermissions("calories")
        val weight = ConnectingViewModel.consentKeyToPermissions("weight")
        val exercise = ConnectingViewModel.consentKeyToPermissions("exercise")
        // No overlap between weight and exercise permissions
        assertTrue((weight intersect exercise).isEmpty())
    }
}
