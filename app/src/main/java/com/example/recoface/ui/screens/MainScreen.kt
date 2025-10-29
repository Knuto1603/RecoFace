package com.example.recoface.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import com.example.recoface.ui.components.MainScaffold

/**
 * Pantalla principal (Menú) de la aplicación.
 *
 * @param onNavigateToRegister Lambda que se ejecuta al pulsar "Registrar".
 * @param onNavigateToCheckIn Lambda que se ejecuta al pulsar "Marcar Asistencia".
 * @param onNavigateToReport Lambda que se ejecuta al pulsar "Ver Asistencias".
 */
@Composable
fun MainScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToPeopleList: () -> Unit
) {
    MainScaffold(
        titulo = "RecoFace"
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp), // Un poco de padding extra
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a RecoFace",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onNavigateToRegister, // <-- CONECTADO
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text("Registrar Nueva Persona")
            }

            Button(
                onClick = onNavigateToCheckIn, // <-- CONECTADO
                modifier = Modifier
                    .padding(top = 8.dp) // Ajuste leve
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text("Marcar Asistencia")
            }

            Button(
                onClick = onNavigateToReport, // <-- CONECTADO
                modifier = Modifier
                    .padding(top = 8.dp) // Ajuste leve
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text("Ver Asistencias")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tambien puedes revisar las personas registradas en la base de datos.",
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Ver Registros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = "Ver Registros"
                ) {
                    onNavigateToPeopleList()
                }
            )
        }
    }
}