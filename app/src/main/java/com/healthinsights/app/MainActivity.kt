package com.healthinsights.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.healthinsights.app.ui.theme.HealthInsightsTheme
import com.healthinsights.feature.onboarding.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthInsightsTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "welcome",
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onContinue = { navController.navigate("profile") },
                        )
                    }
                    composable("profile") {
                        // T2 — implemented in item 9
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Profile", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    }
}
