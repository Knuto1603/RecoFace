package com.example.recoface.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    titulo: String,
    modifier: Modifier = Modifier,
    // Este es el "slot" para tu contenido
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        //Header dinamico
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Alburqueque Antón, Sergio Fernando\n" +
                            "Aranda Zapata Madeley Mariella\n" +
                            "Moran Palacios Alembert Nick",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        content(innerPadding)
    }
}