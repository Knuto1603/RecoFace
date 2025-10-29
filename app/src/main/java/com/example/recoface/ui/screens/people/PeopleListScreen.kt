package com.example.recoface.ui.screens.people

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recoface.ui.components.MainScaffold
import com.example.recoface.ui.components.PersonListItem
// import androidx.compose.runtime.LaunchedEffect // No longer needed here
import com.example.recoface.ui.navigation.AppScreen

@Composable
fun PeopleListScreen(
    navController: NavController,
    viewModel: PeopleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current

    // 🛑 REMOVED: Redundant LaunchedEffect - ViewModel loads data in init
    // LaunchedEffect(Unit) {
    //     viewModel.loadPeople()
    // }

    MainScaffold(titulo = "Personas Registradas") { paddingValues ->
        Box(
            // ✅ ADDED: Modifiers for Box
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center // Center content like loading/error messages
        ) {
            when {
                // ✅ ADDED: Content for isLoading state
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                // ✅ ADDED: Content for error state
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // ✅ ADDED: Content for empty list state
                uiState.people.isEmpty() -> {
                    Text(
                        text = "No hay personas registradas.",
                        textAlign = TextAlign.Center
                    )
                }

                // Content for success state
                else -> {
                    LazyColumn(
                        // ✅ ADDED: Modifiers for LazyColumn
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between items
                    ) {
                        items(uiState.people, key = { it.id }) { person -> // Added key for better performance
                            PersonListItem(
                                person = person,
                                onEditClick = {
                                    navController.navigate(
                                        AppScreen.EditPerson.createRoute(it.id)
                                    )
                                },
                                onDeleteClick = {
                                    viewModel.deletePerson(it)
                                    // Optional: Show a confirmation Toast
                                    Toast.makeText(context, "${it.firstName} eliminado", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}