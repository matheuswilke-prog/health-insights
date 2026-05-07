package com.healthinsights.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.healthinsights.app.ui.theme.HealthInsightsTheme
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.feature.onboarding.ConsentScreen
import com.healthinsights.feature.onboarding.ConnectingScreen
import com.healthinsights.feature.onboarding.GoalScreen
import com.healthinsights.feature.onboarding.ProfileFormData
import com.healthinsights.feature.onboarding.ProfileScreen
import com.healthinsights.feature.onboarding.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthInsightsTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val startRoute by mainViewModel.startRoute.collectAsState()

                val route = startRoute
                if (route != null) {
                    val navController = rememberNavController()
                    HealthInsightsNavHost(
                        navController = navController,
                        startDestination = route,
                        onOnboardingComplete = mainViewModel::markOnboardingComplete,
                    )
                }
                // While startRoute is null (DataStore loading), show nothing — transition is fast.
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NavHost — extracted for testability
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun HealthInsightsNavHost(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ephemeral onboarding state — survives screen transitions within a session.
    // If process is killed mid-onboarding the user restarts from Welcome (acceptable for MVP).
    var profileData by remember { mutableStateOf<ProfileFormData?>(null) }
    var selectedGoal by remember { mutableStateOf(UserGoal.MAINTAIN) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // ── T1 — Welcome ─────────────────────────────────────────────────────
        composable(ROUTE_WELCOME) {
            WelcomeScreen(
                onContinue = { navController.navigate(ROUTE_PROFILE) },
            )
        }

        // ── T2 — Profile ─────────────────────────────────────────────────────
        composable(ROUTE_PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onContinue = { data ->
                    profileData = data
                    navController.navigate(ROUTE_GOAL)
                },
            )
        }

        // ── T3 — Goal ────────────────────────────────────────────────────────
        composable(ROUTE_GOAL) {
            val data = profileData
            if (data != null) {
                GoalScreen(
                    profileData = data,
                    onBack = { navController.popBackStack() },
                    onContinue = { goal ->
                        selectedGoal = goal
                        navController.navigate(ROUTE_CONSENT)
                    },
                )
            }
        }

        // ── T4 — Consent ─────────────────────────────────────────────────────
        composable(ROUTE_CONSENT) {
            val data = profileData
            if (data != null) {
                ConsentScreen(
                    profileData = data,
                    goal = selectedGoal,
                    onContinue = { navController.navigate(ROUTE_CONNECTING) },
                    onSkip = { navController.navigate(ROUTE_CONNECTING) },
                )
            }
        }

        // ── T5 — Connecting ──────────────────────────────────────────────────
        composable(ROUTE_CONNECTING) {
            ConnectingScreen(
                onNavigateToDashboard = {
                    onOnboardingComplete()
                    navController.navigate(ROUTE_DASHBOARD) {
                        // Clear the entire onboarding back-stack
                        popUpTo(ROUTE_WELCOME) { inclusive = true }
                    }
                },
            )
        }

        // ── T6 — Dashboard (Sprint 2 placeholder) ────────────────────────────
        composable(ROUTE_DASHBOARD) {
            DashboardPlaceholder()
        }
    }
}

@Composable
private fun DashboardPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Dashboard — Sprint 2",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
