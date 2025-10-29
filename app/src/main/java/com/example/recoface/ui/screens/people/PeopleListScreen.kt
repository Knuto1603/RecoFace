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
import androidx.compose.runtime.LaunchedEffect

import com.example.recoface.ui.navigation.AppScreen

@Composable
fun PeopleListScreen(
    navController: NavController, // <-- Ya lo recibe
    viewModel: PeopleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPeople()
    }

    MainScaffold(titulo = "Personas Registradas") { paddingValues ->
        Box(
            // ... (modificador de Box)
        ) {
            when {
                // ... (when isLoading, error, empty)

                else -> {
                    LazyColumn(
                        // ... (modificador de LazyColumn)
                    ) {
                        items(uiState.people) { person ->
                            PersonListItem(
                                person = person,
                                onEditClick = {
                                    // 2. CONECTA LA NAVEGACIÓN
                                    navController.navigate(
                                        AppScreen.EditPerson.createRoute(it.id)
                                    )
                                },
                                onDeleteClick = {
                                    viewModel.deletePerson(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}