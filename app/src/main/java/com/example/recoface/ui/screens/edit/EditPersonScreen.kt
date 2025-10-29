package com.example.recoface.ui.screens.edit

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recoface.ui.components.MainScaffold

@Composable
fun EditPersonScreen(
    navController: NavController,
    viewModel: EditPersonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current

    // 1. Observador para mostrar el Toast de resultado
    LaunchedEffect(uiState.updateResult) {
        if (uiState.updateResult != null) {
            val message = if (uiState.updateResult!!.isSuccess) {
                "¡Datos actualizados!"
            } else {
                uiState.updateResult!!.exceptionOrNull()?.message ?: "Error al actualizar"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

            // Si fue exitoso, regresa a la pantalla anterior
            if (uiState.updateResult!!.isSuccess) {
                navController.popBackStack()
            }
        }
    }

    MainScaffold(titulo = "Editar Persona") { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading && uiState.person == null) {
                // 2. Estado de carga inicial
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                // 3. Estado de error de carga
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                // 4. Formulario de edición
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = uiState.dni,
                        onValueChange = viewModel::onDniChanged,
                        label = { Text("DNI") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.firstName,
                        onValueChange = viewModel::onFirstNameChanged,
                        label = { Text("Nombres") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.lastName,
                        onValueChange = viewModel::onLastNameChanged,
                        label = { Text("Apellidos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::onUpdateClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading // Deshabilitado mientras se guarda
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}