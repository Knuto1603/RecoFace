package com.example.recoface.ui.screens.checkin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recoface.data.ml.AnalyzedFace
import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.usecase.CheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckInUiState(
    val hasFace: Boolean = false,
    val isProcessing: Boolean = false,
    val checkInResult: Result<AttendanceRecord>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val checkInUseCase: CheckInUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(CheckInUiState())
    val uiState: State<CheckInUiState> = _uiState

    private var processingJob: Job? = null

    companion object {
        private const val COOLDOWN_DURATION_MS = 3000L
    }

    /**
     * Llamado por el FaceAnalyzer con cada frame.
     */
    fun onFaceAnalyzed(faces: List<AnalyzedFace>) {
        // Actualizar si hay cara visible
        val hasFace = faces.isNotEmpty()
        _uiState.value = _uiState.value.copy(hasFace = hasFace)

        // Solo procesar si:
        // 1. Hay al menos una cara
        // 2. No estamos procesando actualmente
        // 3. No hay un cooldown activo
        if (hasFace && processingJob?.isActive != true) {
            processCheckIn(faces.first())
        }
    }

    private fun processCheckIn(face: AnalyzedFace) {
        processingJob = viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isProcessing = true,
                    errorMessage = null
                )

                val embedding = FaceEmbedding(face.embedding)
                val result = checkInUseCase(embedding)

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    checkInResult = result,
                    errorMessage = if (result.isFailure) {
                        result.exceptionOrNull()?.message ?: "Error desconocido"
                    } else null
                )

                // Cooldown antes de permitir otro check-in
                delay(COOLDOWN_DURATION_MS)

                // Limpiar resultado solo si seguimos en la misma pantalla
                if (processingJob?.isActive == true) {
                    _uiState.value = _uiState.value.copy(
                        checkInResult = null,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    checkInResult = Result.failure(e),
                    errorMessage = "Error al procesar: ${e.message}"
                )

                // Cooldown también en caso de error
                delay(COOLDOWN_DURATION_MS)
                _uiState.value = _uiState.value.copy(
                    checkInResult = null,
                    errorMessage = null
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            checkInResult = null,
            errorMessage = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        processingJob?.cancel()
    }
}