package com.example.recoface.ui.screens.edit

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recoface.domain.model.Person
import com.example.recoface.domain.repository.PersonRepository
import com.example.recoface.domain.usecase.UpdatePersonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI para la pantalla de edición
data class EditPersonUiState(
    val isLoading: Boolean = true,
    val person: Person? = null,
    val dni: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val updateResult: Result<Unit>? = null,
    val error: String? = null
)

@HiltViewModel
class EditPersonViewModel @Inject constructor(
    private val personRepository: PersonRepository, // Necesario para 'getPersonById'
    private val updatePersonUseCase: UpdatePersonUseCase,
    savedStateHandle: SavedStateHandle // Hilt provee esto para leer argumentos de navegación
) : ViewModel() {

    private val _uiState = mutableStateOf(EditPersonUiState())
    val uiState: State<EditPersonUiState> = _uiState

    // El 'personId' original que recibimos
    private var originalPerson: Person? = null

    init {
        // 1. Leer el ID de la persona desde los argumentos de navegación
        val personId: Int? = savedStateHandle["personId"]
        if (personId != null) {
            loadPerson(personId)
        } else {
            _uiState.value = EditPersonUiState(error = "No se pudo cargar la persona.")
        }
    }

    /**
     * Carga los datos de la persona desde la BD.
     */
    private fun loadPerson(id: Int) {
        viewModelScope.launch {
            try {
                // Usamos el repositorio directamente (o podrías crear un GetPersonByIdUseCase)
                originalPerson = personRepository.getPersonById(id)
                if (originalPerson != null) {
                    _uiState.value = EditPersonUiState(
                        isLoading = false,
                        person = originalPerson,
                        dni = originalPerson!!.dni,
                        firstName = originalPerson!!.firstName,
                        lastName = originalPerson!!.lastName
                    )
                } else {
                    _uiState.value = EditPersonUiState(error = "Persona no encontrada.")
                }
            } catch (e: Exception) {
                _uiState.value = EditPersonUiState(error = e.message ?: "Error al cargar.")
            }
        }
    }

    // --- Funciones para actualizar el formulario ---
    fun onDniChanged(dni: String) {
        _uiState.value = _uiState.value.copy(dni = dni, updateResult = null)
    }
    fun onFirstNameChanged(firstName: String) {
        _uiState.value = _uiState.value.copy(firstName = firstName, updateResult = null)
    }
    fun onLastNameChanged(lastName: String) {
        _uiState.value = _uiState.value.copy(lastName = lastName, updateResult = null)
    }

    /**
     * Se llama al presionar "Guardar Cambios".
     */
    fun onUpdateClicked() {
        if (originalPerson == null) return // No debería pasar

        // 1. Crea el objeto 'Person' actualizado
        val updatedPerson = originalPerson!!.copy(
            dni = uiState.value.dni,
            firstName = uiState.value.firstName,
            lastName = uiState.value.lastName
        )

        // 2. Llama al Caso de Uso
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = updatePersonUseCase(updatedPerson)

            // 3. Actualiza el estado con el resultado
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                updateResult = result
            )
        }
    }
}