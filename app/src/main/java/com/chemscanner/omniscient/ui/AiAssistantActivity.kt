package com.chemscanner.omniscient.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chemscanner.omniscient.ui.compose.screens.AiAssistantScreen
import com.chemscanner.omniscient.ui.compose.screens.DemonstrationARScreen
import com.chemscanner.omniscient.ui.compose.screens.ExperimentDetailsScreen
import com.chemscanner.omniscient.ui.compose.screens.ExperimentListScreen
import com.chemscanner.omniscient.ui.compose.screens.HealthProfileScreen
import com.chemscanner.omniscient.ui.compose.screens.ProductScannerScreen
import com.chemscanner.omniscient.ui.viewmodels.AiAssistantViewModel
import com.chemscanner.omniscient.marrow.utils.LanguageManager // FIXED IMPORT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiAssistantActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageManager = LanguageManager(newBase)
        super.attachBaseContext(languageManager.updateContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OmniscientNavHost()
        }
    }
}

@Composable
fun OmniscientNavHost() {
    val navController = rememberNavController()
    val aiViewModel: AiAssistantViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "assistant") {
        composable("assistant") {
            AiAssistantScreen(
                viewModel = aiViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHealthProfile = { navController.navigate("health_profile") },
                onNavigateToProductScanner = { navController.navigate("product_scanner") },
                onNavigateToExperiments = { navController.navigate("experiment_list") },
                onNavigateToProblemSolver = { navController.navigate("problem_solver") }
            )
        }
        composable("health_profile") {
            HealthProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("product_scanner") {
            ProductScannerScreen(
                onBarcodeScanned = {
                    // Handle barcode scan result
                },
                onTextRecognized = { text ->
                    aiViewModel.analyzeIngredients(text)
                    navController.popBackStack()
                }
            )
        }
        composable("problem_solver") {
            ProductScannerScreen(
                onBarcodeScanned = {
                    // Not used in this context
                },
                onTextRecognized = { text ->
                    aiViewModel.solveChemistryProblem(text)
                    navController.popBackStack()
                }
            )
        }
        composable("experiment_list") {
            ExperimentListScreen(
                onExperimentClick = { experimentId ->
                    navController.navigate("experiment_details/$experimentId")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "experiment_details/{experimentId}",
            arguments = listOf(navArgument("experimentId") { type = NavType.StringType })
        ) {
            ExperimentDetailsScreen(
                onBack = { navController.popBackStack() },
                onStartARExperiment = { experimentId ->
                    navController.navigate("demonstration_ar/$experimentId")
                }
            )
        }
        composable(
            route = "demonstration_ar/{experimentId}",
            arguments = listOf(navArgument("experimentId") { type = NavType.StringType })
        ) {
            DemonstrationARScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
