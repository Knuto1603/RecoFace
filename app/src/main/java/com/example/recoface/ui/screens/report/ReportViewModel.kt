package com.example.recoface.ui.screens.report

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.usecase.GetAttendanceReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// 1. Estado de la UI
data class ReportUiState(
    val isLoading: Boolean = false,
    val attendanceList: List<AttendanceRecord> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getAttendanceReportUseCase: GetAttendanceReportUseCase // 2. Inyectamos el Caso de Uso
) : ViewModel() {

    private val _uiState = mutableStateOf(ReportUiState())
    val uiState: State<ReportUiState> = _uiState

    // 3. Cuando el ViewModel se inicia, carga el reporte de hoy
    init {
        loadTodaysReport()
    }

    fun loadTodaysReport() {
        viewModelScope.launch {
            _uiState.value = ReportUiState(isLoading = true) // Empezar a cargar

            val result = getAttendanceReportUseCase(Calendar.getInstance()) // Pedimos el reporte de HOY

            // 4. Actualizar el estado con el resultado
            if (result.isSuccess) {
                _uiState.value = ReportUiState(attendanceList = result.getOrThrow())
            } else {
                _uiState.value = ReportUiState(error = result.exceptionOrNull()?.message ?: "Error al cargar el reporte")
            }
        }
    }
}