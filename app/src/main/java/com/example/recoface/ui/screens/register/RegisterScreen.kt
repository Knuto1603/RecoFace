package com.example.recoface.ui.screens.register

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recoface.data.ml.FaceAnalyzer
import com.example.recoface.data.ml.FaceNetModel
import com.example.recoface.ui.components.CameraView
import com.example.recoface.ui.components.MainScaffold
import coil.compose.AsyncImage

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    // Crear instancias para el análisis de caras
    val faceNetModel = remember { FaceNetModel(context) }
    val faceAnalyzer = remember {
        FaceAnalyzer(faceNetModel) { faces ->
            viewModel.onFaceAnalyzed(faces)
        }
    }

    // Limpiar recursos cuando se destruya el composable
    DisposableEffect(Unit) {
        onDispose {
            faceAnalyzer.close()
            faceNetModel.close()
        }
    }

    // Manejar resultados de registro
    LaunchedEffect(uiState.registrationResult) {
        uiState.registrationResult?.let { result ->
            val message = if (result.isSuccess) {
                "¡Persona registrada con éxito!"
            } else {
                result.exceptionOrNull()?.message ?: "Error desconocido"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Mostrar errores de validación
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    MainScaffold(titulo = "Registrar Persona") { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- VISTA DE CÁMARA O FOTO CAPTURADA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(
                        width = 2.dp,
                        color = when {
                            uiState.capturedFace != null -> Color.Green
                            uiState.detectedFace != null -> Color.Yellow
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
            ) {
                // Si ya capturamos una foto, mostrarla
                if (uiState.capturedFace != null) {
                    AsyncImage(
                        model = uiState.capturedFace!!.bitmap,
                        contentDescription = "Rostro capturado",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Sino, mostrar la cámara en vivo
                    CameraView(
                        modifier = Modifier.fillMaxSize(),
                        analysis = faceAnalyzer
                    )
                }

                // Indicador de detección en tiempo real
                if (uiState.detectedFace != null && uiState.capturedFace == null) {
                    Text(
                        text = "✓ Rostro detectado",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp),
                        color = Color.Green,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BOTÓN DE CAPTURA ---
            if (uiState.capturedFace == null) {
                Button(
                    onClick = viewModel::onCaptureFace,
                    enabled = uiState.detectedFace != null && !uiState.isLoading
                ) {
                    Text("Capturar Rostro")
                }
            } else {
                OutlinedButton(
                    onClick = viewModel::onRecaptureFace,
                    enabled = !uiState.isLoading
                ) {
                    Text("Volver a Capturar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMPOS DE FORMULARIO ---
            OutlinedTextField(
                value = uiState.dni,
                onValueChange = viewModel::onDniChanged,
                label = { Text("DNI") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.firstName,
                onValueChange = viewModel::onFirstNameChanged,
                label = { Text("Nombres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = viewModel::onLastNameChanged,
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN DE REGISTRO ---
            Button(
                onClick = viewModel::onRegisterClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading && uiState.capturedFace != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar")
                }
            }
        }
    }
}