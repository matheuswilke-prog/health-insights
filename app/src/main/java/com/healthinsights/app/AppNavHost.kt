package com.healthinsights.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.feature.dashboard.DashboardScreen
import com.healthinsights.feature.onboarding.ConsentScreen
import com.healthinsights.feature.onboarding.ConnectingScreen
import com.healthinsights.feature.onboarding.GoalScreen
import com.healthinsights.feature.onboarding.ProfileFormData
import com.healthinsights.feature.onboarding.ProfileScreen
import com.healthinsights.feature.onboarding.WelcomeScreen
import com.healthinsights.feature.settings.SettingsScreen
import java.io.IOException

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"
private const val EXPORT_MIME_TYPE = "application/json"

@Suppress("LongMethod") // NavHost functions enumerate all routes; structural length is expected.
@Composable
internal fun HealthInsightsNavHost(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit,
    onOnboardingInvalid: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ephemeral onboarding state; survives screen transitions within a session.
    var profileData by remember { mutableStateOf<ProfileFormData?>(null) }
    var selectedGoal by remember { mutableStateOf(UserGoal.MAINTAIN) }
    val context = LocalContext.current
    var pendingExportContent by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(EXPORT_MIME_TYPE),
    ) { uri ->
        val content = pendingExportContent
        pendingExportContent = null
        if (uri != null && content != null) {
            writeExportFile(context, uri, content)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(ROUTE_WELCOME) {
            WelcomeScreen(
                onContinue = { navController.navigate(ROUTE_PROFILE) },
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onContinue = { data ->
                    profileData = data
                    navController.navigate(ROUTE_GOAL)
                },
            )
        }

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

        composable(ROUTE_CONNECTING) {
            ConnectingScreen(
                onNavigateToDashboard = {
                    onOnboardingComplete()
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_WELCOME) { inclusive = true }
                    }
                },
            )
        }

        composable(ROUTE_DASHBOARD) {
            DashboardScreen(
                onReconfigureOnboarding = {
                    onOnboardingInvalid()
                    navController.navigate(ROUTE_WELCOME) {
                        popUpTo(ROUTE_DASHBOARD) { inclusive = true }
                    }
                },
                onSettingsClick = {
                    navController.navigate(ROUTE_SETTINGS)
                },
            )
        }

        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenHealthConnect = { openHealthConnect(context) },
                onExportDataReady = { fileName, content ->
                    pendingExportContent = content
                    exportLauncher.launch(fileName)
                },
                onLocalDataDeleted = {
                    onOnboardingInvalid()
                    navController.navigate(ROUTE_WELCOME) {
                        popUpTo(ROUTE_DASHBOARD) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun openHealthConnect(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(HEALTH_CONNECT_PACKAGE)
    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
    try {
        context.startActivity(launchIntent ?: fallbackIntent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(fallbackIntent)
    }
}

private fun writeExportFile(
    context: Context,
    uri: Uri,
    content: String,
) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
        } ?: throw IOException("Unable to open export destination")
        Toast.makeText(context, "Dados exportados", Toast.LENGTH_SHORT).show()
    } catch (_: IOException) {
        Toast.makeText(context, "Nao foi possivel salvar o arquivo", Toast.LENGTH_SHORT).show()
    }
}
