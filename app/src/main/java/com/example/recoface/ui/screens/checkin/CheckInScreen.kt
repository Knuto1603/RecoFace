package com.example.recoface.ui.screens.checkin

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.recoface.data.ml.FaceAnalyzer
import com.example.recoface.data.ml.FaceNetModel
import com.example.recoface.ui.components.CameraView
import com.example.recoface.ui.components.MainScaffold

@Composable
fun CheckInScreen(
    navController: NavController,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    // 1. Instanciamos el modelo y el analizador
    val faceNetModel = remember { FaceNetModel(context) }
    val faceAnalyzer = remember {
        FaceAnalyzer(faceNetModel) { faces ->
            // 2. Pasamos las caras detectadas al ViewModel
            viewModel.onFaceAnalyzed(faces)
        }
    }

    // 3. Observador para mostrar los mensajes de resultado
    LaunchedEffect(uiState.checkInResult) {
        uiState.checkInResult?.let { result ->
            val message = if (result.isSuccess) {
                // Mostramos el nombre de la persona que marcó
                "¡Bienvenido, ${result.getOrNull()?.personName}!"
            } else {
                // Mostramos el error (ej. "Persona no reconocida")
                result.exceptionOrNull()?.message ?: "Error desconocido"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    MainScaffold(titulo = "Marcar Asistencia") { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Coloque su rostro frente a la cámara",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Contenedor de la Cámara
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Cámara cuadrada
                    .border(
                        width = 3.dp,
                        // Verde si hay cara, Rojo si hay error, por defecto si no
                        color = when {
                            uiState.checkInResult?.isFailure == true -> Color.Red
                            uiState.hasFace -> Color.Green
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                CameraView(
                    modifier = Modifier.fillMaxSize(),
                    analysis = faceAnalyzer // Pasamos el analizador
                )

                // 5. Indicador de carga
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 6.dp
                    )
                }
            }
        }
    }
}