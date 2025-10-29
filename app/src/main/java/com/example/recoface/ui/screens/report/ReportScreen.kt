package com.example.recoface.ui.screens.report

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recoface.ui.components.AttendanceListItem
import com.example.recoface.ui.components.MainScaffold

@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    MainScaffold(titulo = "Reporte de Hoy") { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. Estado de Carga
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                // 2. Estado de Error
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // 3. Estado Vacío
                uiState.attendanceList.isEmpty() -> {
                    Text(
                        text = "Aún no hay registros de asistencia para el día de hoy.",
                        textAlign = TextAlign.Center
                    )
                }

                // 4. Estado Exitoso (Mostrar Lista)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.attendanceList) { record ->
                            AttendanceListItem(record = record)
                        }
                    }
                }
            }
        }
    }
}