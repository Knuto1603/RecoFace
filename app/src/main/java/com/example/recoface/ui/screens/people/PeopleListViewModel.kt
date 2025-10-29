package com.example.recoface.ui.screens.people

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recoface.domain.model.Person
import com.example.recoface.domain.usecase.DeletePersonUseCase
import com.example.recoface.domain.usecase.GetAllPeopleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeopleListUiState(
    val isLoading: Boolean = false,
    val people: List<Person> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PeopleListViewModel @Inject constructor(
    private val getAllPeopleUseCase: GetAllPeopleUseCase,
    private val deletePersonUseCase: DeletePersonUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(PeopleListUiState())
    val uiState: State<PeopleListUiState> = _uiState

    init {
        loadPeople()
    }

    /**
     * Carga o recarga la lista de personas desde la base de datos.
     */
    fun loadPeople() {
        viewModelScope.launch {
            _uiState.value = PeopleListUiState(isLoading = true)
            val result = getAllPeopleUseCase()
            _uiState.value = if (result.isSuccess) {
                PeopleListUiState(people = result.getOrThrow())
            } else {
                PeopleListUiState(error = result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }

    /**
     * Elimina una persona y luego recarga la lista.
     */
    fun deletePerson(person: Person) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // Mostrar carga
            deletePersonUseCase(person)
            // Recargar la lista
            loadPeople()
        }
    }
}