package com.example.recoface.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recoface.ui.screens.MainScreen
import com.example.recoface.ui.screens.checkin.CheckInScreen
import com.example.recoface.ui.screens.edit.EditPersonScreen
import com.example.recoface.ui.screens.people.PeopleListScreen
import com.example.recoface.ui.screens.register.RegisterScreen
import com.example.recoface.ui.screens.report.ReportScreen


/**
 * Define todas las rutas de la aplicación para evitar errores de tipeo.
 */
sealed class AppScreen(val route: String) {
    object Main : AppScreen("main")
    object Register : AppScreen("register")
    object CheckIn : AppScreen("checkin")
    object Report : AppScreen("report")
    object PeopleList : AppScreen("people_list")
    object EditPerson : AppScreen("edit_person/{personId}") {
        fun createRoute(personId: Int) = "edit_person/$personId"
    }
    // Podrías añadir una pantalla de "Ver Personas" aquí
}

/**
 * El controlador de navegación principal (NavHost).
 * Aquí es donde se define qué Composable mostrar para cada ruta.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.Main.route // Empezamos en la pantalla principal
    ) {
        // --- Pantalla Principal (Tu MainScreen) ---
        composable(AppScreen.Main.route) {
            MainScreen(
                onNavigateToRegister = { navController.navigate(AppScreen.Register.route) },
                onNavigateToCheckIn = { navController.navigate(AppScreen.CheckIn.route) },
                onNavigateToReport = { navController.navigate(AppScreen.Report.route) },
                onNavigateToPeopleList = { navController.navigate(AppScreen.PeopleList.route) }
                // Añadir navegación para "Ver Registros"
            )
        }

        // --- Pantalla de Registro ---
        composable(AppScreen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // --- Pantalla de Marcar Asistencia ---
        composable(AppScreen.CheckIn.route) {
            CheckInScreen(navController = navController)
        }

        // --- Pantalla de Reporte ---
        composable(AppScreen.Report.route) {
            // Aquí irá tu ReportScreen()
            ReportScreen(navController = navController)
        }
        // --- Pantalla de Lista de Personas ---
        composable(AppScreen.PeopleList.route) {
            PeopleListScreen(navController = navController)
        }
        // --- Pantalla de Edición de Persona ---
        composable(
            route = AppScreen.EditPerson.route,
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) {
            // HiltViewModel se encargará de leer el 'personId'
            EditPersonScreen(navController = navController)
        }
    }
}