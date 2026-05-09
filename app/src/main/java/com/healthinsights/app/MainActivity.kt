package com.healthinsights.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.healthinsights.core.ui.theme.HealthInsightsTheme
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
                    HealthInsightsNavHost(
                        navController = rememberNavController(),
                        startDestination = route,
                        onOnboardingComplete = mainViewModel::markOnboardingComplete,
                        onOnboardingInvalid = mainViewModel::clearOnboardingComplete,
                    )
                }
            }
        }
    }
}
