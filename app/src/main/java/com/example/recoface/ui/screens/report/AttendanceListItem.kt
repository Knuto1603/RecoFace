package com.example.recoface.ui.components

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.recoface.domain.model.AttendanceRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Muestra una sola fila en la lista de reporte de asistencia.
 */
@Composable
fun AttendanceListItem(
    record: AttendanceRecord,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = record.personName ?: "Nombre Desconocido",
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(text = "DNI: ${record.personDni ?: "N/A"}")
        },
        trailingContent = {
            Text(
                text = formatTimestamp(record.timestamp), // Mostramos la hora
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    )
}

/**
 * Función de utilidad para convertir el 'Long' del timestamp a una hora legible.
 * (Ej: "09:30 AM")
 */
private fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val netDate = Date(timestamp)
        sdf.format(netDate)
    } catch (e: Exception) {
        "??:?? ??"
    }
}