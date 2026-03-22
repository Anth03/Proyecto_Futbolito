package com.example.futbolito

import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex

@Composable
fun GameScreen() {
    val context = LocalContext.current
    var ballPosition by remember { mutableStateOf(Offset(540f, 960f)) } // Centro inicial de la pelota
    var velocity by remember { mutableStateOf(Offset(0f, 0f)) } // Velocidad inicial
    var screenSize by remember { mutableStateOf(Offset(1080f, 1920f)) } // Tamaño de la pantalla
    var scoreTop by remember { mutableStateOf(0) } // Marcador de la parte superior
    var scoreBottom by remember { mutableStateOf(0) } // Marcador de la parte inferior
    val ballRadius = 30f // Radio de la pelota
    val goalWidth = 250f  // Ancho de la portería
    val goalHeight = 50f  // Altura de la portería
    val frameThickness = 30f // Grosor de los postes de la portería
    val openingSize = 100f  // Abertura en el centro de la portería

    val goalLeft = screenSize.x / 2 - goalWidth / 2 // Límite izquierdo de la portería
    val openingLeft = screenSize.x / 2 - openingSize / 2 // Límite izquierdo de la abertura
    val openingRight = screenSize.x / 2 + openingSize / 2 // Límite derecho de la abertura

    // Obtén los obstáculos desde el archivo de Obstáculo (se recalculan si cambia el tamaño de pantalla)
    val obstacles = remember(screenSize) { getObstacles() + getLShapedObstacles(screenSize.y) }

    // Inicializa el gestor de sensores
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val sensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // Carga el bitmap del fondo UNA sola vez (no dentro del Canvas)
    val backgroundBitmap = remember(context) { BitmapFactory.decodeResource(context.resources, R.drawable.cesped) }

    // Contenedor principal de la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates -> // Obtiene el tamaño de la pantalla
                screenSize = Offset(
                    layoutCoordinates.size.width.toFloat(),
                    layoutCoordinates.size.height.toFloat()
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = screenSize.x
            val height = screenSize.y
            // Dibuja el fondo en la pantalla (bitmap cargado fuera del Canvas)
            for (i in 0..(width / backgroundBitmap.width).toInt()) {
                for (j in 0..(height / backgroundBitmap.height).toInt()) {
                    drawImage(
                        image = backgroundBitmap.asImageBitmap(),
                        topLeft = Offset(i * backgroundBitmap.width.toFloat(), j * backgroundBitmap.height.toFloat())
                    )
                }
            }

            // Dibuja la pelota
            drawCircle(color = Color.White, center = ballPosition, radius = ballRadius)

            // Dibuja los obstáculos en el campo
            for (obstacle in obstacles) {
                drawRect(Color.White, obstacle.position, obstacle.size)
            }

            // Dibuja la portería superior (roja) - el equipo rojo defiende arriba
            drawRect(Color.Red, Offset(goalLeft, 0f), Size(goalWidth, frameThickness))

            // Dibuja la portería inferior (azul) - el equipo azul defiende abajo
            drawRect(Color.Blue, Offset(goalLeft, screenSize.y - frameThickness), Size(goalWidth, frameThickness))
        }

        // Dibuja el marcador de puntos
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            Text("$scoreTop", color = Color.Red, style = TextStyle(fontSize = 40.sp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("$scoreBottom", color = Color.Blue, style = TextStyle(fontSize = 40.sp))
        }

        // Maneja la interacción del usuario con el acelerómetro
        DisposableEffect(sensorManager) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        val x = event.values[0] // Movimiento en el eje X
                        val y = event.values[1] // Movimiento en el eje Y

                        // Modifica la velocidad de la pelota según los movimientos del acelerómetro
                        velocity = Offset(velocity.x - x * 0.2f, velocity.y + y * 0.2f)

                        // Aplica fricción para un movimiento más natural
                        velocity = Offset(velocity.x * 0.99f, velocity.y * 0.99f)

                        // Limita la velocidad máxima para evitar aceleración descontrolada
                        val maxSpeed = 25f
                        velocity = Offset(
                            velocity.x.coerceIn(-maxSpeed, maxSpeed),
                            velocity.y.coerceIn(-maxSpeed, maxSpeed)
                        )

                        var newX = ballPosition.x + velocity.x
                        val newY = ballPosition.y + velocity.y

                        // Colisiones con los límites laterales de la pantalla
                        newX = when {
                            newX <= ballRadius -> { // Limite izquierdo
                                velocity = velocity.copy(x = -velocity.x * 0.8f)
                                ballRadius
                            }
                            newX >= screenSize.x - ballRadius -> { // Limite derecho
                                velocity = velocity.copy(x = -velocity.x * 0.8f)
                                screenSize.x - ballRadius
                            }
                            else -> newX
                        }

                        var newPosition = Offset(newX, newY)
                        var newVelocity = velocity

                        // Verifica la colisión con los obstáculos
                        val (updatedPosition, updatedVelocity) = checkCollisionWithObstacles(newPosition, ballRadius, velocity, obstacles)
                        newPosition = updatedPosition
                        newVelocity = updatedVelocity

                        // Verifica si la pelota entra en la portería superior
                        if (newPosition.y <= goalHeight) {
                            if (newPosition.x in openingLeft..openingRight) {
                                // Gol en la portería superior
                                scoreBottom++
                                newPosition = Offset(screenSize.x / 2, screenSize.y / 2) // Resetea la posición
                                newVelocity = Offset(0f, 0f) // Resetea la velocidad
                            } else {
                                // Rebote en la portería superior
                                newVelocity = newVelocity.copy(y = -newVelocity.y * 0.8f)
                                newPosition = newPosition.copy(y = goalHeight)
                            }
                        } else if (newPosition.y >= screenSize.y - goalHeight) { // Verifica portería inferior
                            if (newPosition.x in openingLeft..openingRight) {
                                // Gol en la portería inferior
                                scoreTop++
                                newPosition = Offset(screenSize.x / 2, screenSize.y / 2)
                                newVelocity = Offset(0f, 0f)
                            } else {
                                // Rebote en la portería inferior
                                newVelocity = newVelocity.copy(y = -newVelocity.y * 0.8f)
                                newPosition = newPosition.copy(y = screenSize.y - goalHeight)
                            }
                        }

                        // Actualiza la posición y velocidad de la pelota
                        ballPosition = newPosition
                        velocity = newVelocity
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            // Registra el listener del sensor para detectar el movimiento
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

            // Desregistra el listener cuando ya no se necesite
            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        }
    }
}
