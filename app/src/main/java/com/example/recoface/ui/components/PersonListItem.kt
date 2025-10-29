package com.example.recoface.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.recoface.domain.model.Person
import coil.compose.AsyncImage
import java.io.File

/**
 * Muestra una sola fila para la lista de personas registradas.
 * Incluye botones para Editar y Eliminar.
 */
@Composable
fun PersonListItem(
    person: Person,
    onEditClick: (Person) -> Unit,
    onDeleteClick: (Person) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = "${person.lastName}, ${person.firstName}",
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(text = "DNI: ${person.dni}")
        },
        leadingContent = {
            AsyncImage(
                model = File(person.facePhotoPath), // Carga la imagen desde el archivo
                contentDescription = "Rostro de ${person.firstName}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = { onEditClick(person) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = { onDeleteClick(person) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    )
}