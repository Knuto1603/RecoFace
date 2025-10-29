package com.example.recoface.ui.screens.register

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recoface.data.ml.AnalyzedFace
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.usecase.RegisterPersonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val dni: String = "",
    val firstName: String = "",
    val lastName: String = "",
    // Cara detectada en tiempo real (preview)
    val detectedFace: AnalyzedFace? = null,
    // Cara capturada/congelada para el registro
    val capturedFace: AnalyzedFace? = null,
    val isLoading: Boolean = false,
    val registrationResult: Result<Unit>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerPersonUseCase: RegisterPersonUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(RegisterUiState())
    val uiState: State<RegisterUiState> = _uiState

    fun onDniChanged(dni: String) {
        _uiState.value = _uiState.value.copy(
            dni = dni,
            registrationResult = null,
            errorMessage = null
        )
    }

    fun onFirstNameChanged(firstName: String) {
        _uiState.value = _uiState.value.copy(
            firstName = firstName,
            registrationResult = null,
            errorMessage = null
        )
    }

    fun onLastNameChanged(lastName: String) {
        _uiState.value = _uiState.value.copy(
            lastName = lastName,
            registrationResult = null,
            errorMessage = null
        )
    }

    /**
     * Actualiza la cara detectada en tiempo real.
     * Solo actualiza si NO hay una cara ya capturada.
     */
    fun onFaceAnalyzed(faces: List<AnalyzedFace>) {
        // No actualizar si ya capturamos una foto
        if (_uiState.value.capturedFace != null) return

        _uiState.value = _uiState.value.copy(
            detectedFace = faces.firstOrNull()
        )
    }

    /**
     * Captura/congela la cara actual para el registro.
     */
    fun onCaptureFace() {
        val currentFace = _uiState.value.detectedFace
        if (currentFace != null) {
            _uiState.value = _uiState.value.copy(
                capturedFace = currentFace,
                errorMessage = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No se detectó ningún rostro. Intente nuevamente."
            )
        }
    }

    /**
     * Permite tomar la foto de nuevo.
     */
    fun onRecaptureFace() {
        _uiState.value = _uiState.value.copy(
            capturedFace = null,
            registrationResult = null,
            errorMessage = null
        )
    }

    fun onRegisterClicked() {
        val currentState = _uiState.value

        // Validaciones
        val validationError = validateInputs(currentState)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = validationError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Usar la cara capturada (no la detectada en tiempo real)
                val result = registerPersonUseCase(
                    dni = currentState.dni.trim(),
                    firstName = currentState.firstName.trim(),
                    lastName = currentState.lastName.trim(),
                    embedding = FaceEmbedding(currentState.capturedFace!!.embedding),
                    faceBitmap = currentState.capturedFace.bitmap
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registrationResult = result
                )

                if (result.isSuccess) {
                    // Resetear estado después de registro exitoso
                    _uiState.value = RegisterUiState(
                        registrationResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registrationResult = Result.failure(e),
                    errorMessage = "Error al registrar: ${e.message}"
                )
            }
        }
    }

    private fun validateInputs(state: RegisterUiState): String? {
        return when {
            state.capturedFace == null -> "Debe capturar un rostro primero"
            state.dni.isBlank() -> "El DNI es obligatorio"
            state.dni.length < 8 -> "El DNI debe tener al menos 8 dígitos"
            state.firstName.isBlank() -> "El nombre es obligatorio"
            state.lastName.isBlank() -> "El apellido es obligatorio"
            else -> null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}