package com.example.futbolito

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

// Clase que representa un obstáculo rectangular en el juego
data class Obstacle(val position: Offset, val size: Size)

// Función que verifica si la pelota ha colisionado con los obstáculos
fun checkCollisionWithObstacles(
    ballPosition: Offset,
    ballRadius: Float,
    ballVelocity: Offset,
    obstacles: List<Obstacle>
): Pair<Offset, Offset> {
    var newVelocity = ballVelocity
    var newBallPosition = ballPosition

    // Iterar sobre todos los obstáculos y verificar si la pelota colide con ellos
    for (obstacle in obstacles) {
        val obstacleLeft = obstacle.position.x
        val obstacleRight = obstacle.position.x + obstacle.size.width
        val obstacleTop = obstacle.position.y
        val obstacleBottom = obstacle.position.y + obstacle.size.height

        val ballLeft = ballPosition.x - ballRadius
        val ballRight = ballPosition.x + ballRadius
        val ballTop = ballPosition.y - ballRadius
        val ballBottom = ballPosition.y + ballRadius

        // Comprobar si la pelota ha colisionado con el obstáculo
        if (ballRight > obstacleLeft && ballLeft < obstacleRight &&
            ballBottom > obstacleTop && ballTop < obstacleBottom) {

            // Calcular la cantidad de solapamiento con cada borde del obstáculo
            val overlapLeft = ballRight - obstacleLeft
            val overlapRight = obstacleRight - ballLeft
            val overlapTop = ballBottom - obstacleTop
            val overlapBottom = obstacleBottom - ballTop

            // Determinar el solapamiento mínimo (la dirección del rebote)
            val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)

            // Cambiar la dirección de la velocidad según el solapamiento
            if (minOverlap == overlapLeft || minOverlap == overlapRight) {
                newVelocity = newVelocity.copy(x = -newVelocity.x * 0.8f) // Rebote con amortiguación en los lados
            }
            if (minOverlap == overlapTop || minOverlap == overlapBottom) {
                newVelocity = newVelocity.copy(y = -newVelocity.y * 0.8f) // Rebote con amortiguación arriba/abajo
            }

            // Evitar que la pelota quede atrapada dentro del obstáculo
            if (minOverlap == overlapLeft) {
                newBallPosition = newBallPosition.copy(x = obstacleLeft - ballRadius)
            } else if (minOverlap == overlapRight) {
                newBallPosition = newBallPosition.copy(x = obstacleRight + ballRadius)
            } else if (minOverlap == overlapTop) {
                newBallPosition = newBallPosition.copy(y = obstacleTop - ballRadius)
            } else if (minOverlap == overlapBottom) {
                newBallPosition = newBallPosition.copy(y = obstacleBottom + ballRadius)
            }
        }
    }

    return Pair(newBallPosition, newVelocity) // Retornar la nueva posición y velocidad de la pelota
}

// Función para obtener una lista de obstáculos en línea recta (horizontal o vertical)
fun getObstacles(): List<Obstacle> {
    return listOf(
        //--------------------Obstáculos en la parte superior-----------------------------
        Obstacle(Offset(150f, 300f), Size(80f, 25f)), // Obstáculo a la izquierda
        Obstacle(Offset(860f, 300f), Size(80f, 25f)), // Obstáculo a la derecha
        Obstacle(Offset(300f, 300f), Size(200f, 25f)), // Obstáculo central
        Obstacle(Offset(600f, 300f), Size(200f, 25f)), // Obstáculo central

        //--------------------Obstáculos en la parte inferior-----------------------------
        Obstacle(Offset(150f, 2000f), Size(80f, 25f)), // Obstáculo a la izquierda
        Obstacle(Offset(860f, 2000f), Size(80f, 25f)), // Obstáculo a la derecha
        Obstacle(Offset(300f, 2000f), Size(200f, 25f)), // Obstáculo central
        Obstacle(Offset(600f, 2000f), Size(200f, 25f)), // Obstáculo central

        //--------------------Obstáculos en el medio-----------------------------
        Obstacle(Offset(700f, 1156f), Size(200f, 25f)), // Obstáculo izquierdo
        Obstacle(Offset(200f, 1156f), Size(200f, 25f)), // Obstáculo derecho
        Obstacle(Offset(500f, 1156f), Size(100f, 25f)), // Obstáculo central
        Obstacle(Offset(500f, 200f), Size(100f, 25f)), // Obstáculo superior
        Obstacle(Offset(500f, 500f), Size(100f, 25f)), // Obstáculo intermedio
        Obstacle(Offset(500f, 2080f), Size(100f, 25f)), // Obstáculo inferior
        Obstacle(Offset(500f, 1800f), Size(100f, 25f)), // Obstáculo inferior

        // Otros obstáculos distribuidos en varias posiciones
        Obstacle(Offset(450f, 800f), Size(200f, 25f)),
        Obstacle(Offset(450f, 1600f), Size(200f, 25f)),
        Obstacle(Offset(700f, 600f), Size(200f, 25f)),
        Obstacle(Offset(200f, 600f), Size(200f, 25f)),
        Obstacle(Offset(700f, 1800f), Size(200f, 25f)),
        Obstacle(Offset(200f, 1800f), Size(200f, 25f)),
        Obstacle(Offset(700f, 1450f), Size(200f, 25f)),
        Obstacle(Offset(200f, 1450f), Size(200f, 25f)),
        Obstacle(Offset(700f, 950f), Size(200f, 25f)),
        Obstacle(Offset(200f, 950f), Size(200f, 25f)),
    )
}

// Función para obtener obstáculos en forma de "L"
fun getLShapedObstacles(): List<Obstacle> {
    return listOf(
        //--------------------Obstáculos en forma de L-----------------------------
        // Obstáculo en L en el extremo superior derecho
        Obstacle(Offset(800f, 100f), Size(200f, 25f)), // Parte horizontal de la L
        Obstacle(Offset(1000f, 100f), Size(25f, 200f)),  // Parte vertical de la L

        // Obstáculo en L en el extremo superior izquierdo
        Obstacle(Offset(60f, 100f), Size(200f, 25f)), // Parte horizontal de la L
        Obstacle(Offset(60f, 100f), Size(25f, 200f)),  // Parte vertical de la L

        // Obstáculo en L en el extremo inferior izquierdo
        Obstacle(Offset(60f, 2142f), Size(200f, 25f)), // Parte horizontal de la L
        Obstacle(Offset(60f, 1950f), Size(25f, 200f)),  // Parte vertical de la L

        // Obstáculo en L en el extremo inferior derecho
        Obstacle(Offset(825f, 2142f), Size(200f, 25f)), // Parte horizontal de la L
        Obstacle(Offset(1000f, 1950f), Size(25f, 200f)),  // Parte vertical de la L

        //--------------------Obstáculos verticales-----------------------------
        Obstacle(Offset(1000f, 1600f), Size(25f, 200f)),
        Obstacle(Offset(1000f, 1200f), Size(25f, 200f)),
        Obstacle(Offset(1000f, 800f), Size(25f, 200f)),
        Obstacle(Offset(1000f, 400f), Size(25f, 200f)),
        Obstacle(Offset(60f, 1600f), Size(25f, 200f)),
        Obstacle(Offset(60f, 1200f), Size(25f, 200f)),
        Obstacle(Offset(60f, 800f), Size(25f, 200f)),
        Obstacle(Offset(60f, 400f), Size(25f, 200f)),

        //--------------------Porterías-----------------------------
        // Porterías inferiores
        Obstacle(Offset(350f, 2190f), Size(25f, 200f)),
        Obstacle(Offset(710f, 2190f), Size(25f, 200f)),
        Obstacle(Offset(350f, 2170f), Size(140f, 25f)),
        Obstacle(Offset(595f, 2170f), Size(140f, 25f)),

        // Porterías superiores
        Obstacle(Offset(350f, -125f), Size(25f, 200f)),
        Obstacle(Offset(710f, -125f), Size(25f, 200f)),
        Obstacle(Offset(350f, 60f), Size(140f, 25f)),
        Obstacle(Offset(595f, 60f), Size(140f, 25f)),
    )
}
