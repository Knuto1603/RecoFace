package com.example.recoface.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Componente de Jetpack Compose que muestra una vista previa de la cámara
 * y permite cambiar entre la cámara frontal y trasera.
 */
@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    analysis: ImageAnalysis.Analyzer? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para controlar qué cámara está activa
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }

    // Executor para las operaciones de la cámara
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Estado de los permisos de la cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para pedir permisos
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasCameraPermission = isGranted
        }
    )

    // Si no tenemos permisos, solicitarlos al inicio
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso de cámara para continuar.")
        }
        return
    }

    // Contenedor para la vista previa de la cámara y el botón de cambio
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindCameraUseCases(
                        cameraProvider,
                        lifecycleOwner,
                        previewView,
                        lensFacing,
                        cameraExecutor,
                        analysis
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            update = { previewView ->
                // Este bloque se ejecuta cuando lensFacing cambia
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindCameraUseCases(
                        cameraProvider,
                        lifecycleOwner,
                        previewView,
                        lensFacing,
                        cameraExecutor,
                        analysis
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Botón para cambiar de cámara
        FloatingActionButton(
            onClick = {
                // Simplemente cambia el estado - el bloque update se encargará del resto
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                Log.d("CameraView", "Cambiando a cámara: $lensFacing")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Cameraswitch, contentDescription = "Cambiar cámara")
        }
    }

    // Liberar el executor cuando el Composable se destruye
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

/**
 * Función auxiliar para enlazar los casos de uso de la cámara (Preview y ImageAnalysis).
 */
private fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    lensFacing: Int,
    cameraExecutor: ExecutorService,
    analysis: ImageAnalysis.Analyzer?
) {
    try {
        // Desenlazar todo antes de re-enlazar (importante para cambiar de cámara)
        cameraProvider.unbindAll()

        // Crear selector para la cámara especificada
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Verificar que la cámara exista
        if (!cameraProvider.hasCamera(cameraSelector)) {
            Log.e("CameraView", "El dispositivo no tiene la cámara solicitada (facing=$lensFacing)")
            return
        }

        // Obtener la rotación actual del display
        val rotation = previewView.display?.rotation ?: android.view.Surface.ROTATION_0

        // Preview con rotación correcta
        val preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // ImageAnalysis con rotación correcta
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                if (analysis != null) {
                    it.setAnalyzer(cameraExecutor, analysis)
                } else {
                    it.clearAnalyzer()
                }
            }

        // Enlazar la cámara con los casos de uso
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )

        Log.d("CameraView", "Cámara vinculada exitosamente: lensFacing=$lensFacing")

    } catch (e: Exception) {
        Log.e("CameraView", "Error al configurar casos de uso de cámara: ${e.message}", e)
    }
}