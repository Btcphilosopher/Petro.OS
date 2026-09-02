import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun OilDrillSimulator() {

    var rpm by remember { mutableFloatStateOf(8f) }
    var running by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pumpjack")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (60_000 / rpm).toInt(),
                easing = LinearEasing
            )
        ),
        label = "crank"
    )

    val angle = sin(phase) * 0.45f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "OIL WELL DIGITAL SIMULATOR",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        OilPumpjack(
            beamAngle = if (running) angle else 0f,
            rpm = rpm
        )

        Spacer(Modifier.height(16.dp))

        Text("Pump Speed: ${"%.1f".format(rpm)} RPM")

        Slider(
            value = rpm,
            onValueChange = { rpm = it },
            valueRange = 1f..20f
        )

        Button(
            onClick = { running = !running }
        ) {
            Text(if (running) "STOP WELL" else "START WELL")
        }

        Spacer(Modifier.height(16.dp))

        SimulatorTelemetry(rpm)
    }
}


@Composable
fun OilPumpjack(
    beamAngle: Float,
    rpm: Float
) {

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {

        val groundY = size.height * 0.82f

        // -----------------------------
        // Ground
        // -----------------------------

        drawRect(
            color = Color(0xFF333333),
            topLeft = Offset(0f, groundY),
            size = androidx.compose.ui.geometry.Size(
                size.width,
                size.height - groundY
            )
        )

        // -----------------------------
        // Well casing
        // -----------------------------

        val wellX = size.width * 0.50f

        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(wellX - 9f, groundY - 40f),
            size = androidx.compose.ui.geometry.Size(18f, 40f)
        )

        // -----------------------------
        // Pumpjack geometry
        // -----------------------------

        val pivot = Offset(
            size.width * 0.52f,
            size.height * 0.42f
        )

        val beamLength = size.width * 0.48f

        // Beam endpoints calculated from angle
        val horsehead = Offset(
            pivot.x - cos(beamAngle) * beamLength,
            pivot.y - sin(beamAngle) * beamLength
        )

        val crankSide = Offset(
            pivot.x + cos(beamAngle) * beamLength,
            pivot.y + sin(beamAngle) * beamLength
        )

        // -----------------------------
        // A-frame support
        // -----------------------------

        val leftFoot = Offset(
            pivot.x - 70f,
            groundY
        )

        val rightFoot = Offset(
            pivot.x + 70f,
            groundY
        )

        drawLine(
            color = Color.Gray,
            start = leftFoot,
            end = pivot,
            strokeWidth = 14f
        )

        drawLine(
            color = Color.Gray,
            start = rightFoot,
            end = pivot,
            strokeWidth = 14f
        )

        // Cross brace
        drawLine(
            color = Color.LightGray,
            start = leftFoot,
            end = rightFoot,
            strokeWidth = 8f
        )

        // -----------------------------
        // Walking beam
        // -----------------------------

        drawLine(
            color = Color(0xFF8A8A8A),
            start = horsehead,
            end = crankSide,
            strokeWidth = 28f
        )

        // Beam highlight
        drawLine(
            color = Color(0xFFBBBBBB),
            start = horsehead,
            end = crankSide,
            strokeWidth = 5f
        )

        // -----------------------------
        // Horsehead
        // -----------------------------

        drawCircle(
            color = Color(0xFF555555),
            radius = 25f,
            center = horsehead
        )

        // -----------------------------
        // Polished rod
        // -----------------------------

        drawLine(
            color = Color(0xFFE0E0E0),
            start = horsehead,
            end = Offset(
                horsehead.x,
                groundY
            ),
            strokeWidth = 7f
        )

        // -----------------------------
        // Crank
        // -----------------------------

        drawCircle(
            color = Color.DarkGray,
            radius = 32f,
            center = Offset(
                pivot.x + 90f,
                groundY - 45f
            )
        )

        // Rotating crank arm
        val crankCenter = Offset(
            pivot.x + 90f,
            groundY - 45f
        )

        val crankRadius = 28f

        val crankPin = Offset(
            crankCenter.x + cos(phase) * crankRadius,
            crankCenter.y + sin(phase) * crankRadius
        )

        drawLine(
            color = Color.LightGray,
            start = crankCenter,
            end = crankPin,
            strokeWidth = 9f
        )

        drawCircle(
            color = Color.Red,
            radius = 7f,
            center = crankPin
        )

        // -----------------------------
        // Counterweight
        // -----------------------------

        drawRect(
            color = Color(0xFF444444),
            topLeft = Offset(
                crankPin.x - 18f,
                crankPin.y - 12f
            ),
            size = androidx.compose.ui.geometry.Size(
                36f,
                24f
            )
        )

        // -----------------------------
        // Oil flow indicator
        // -----------------------------

        val flow = abs(sin(phase))

        drawCircle(
            color = Color.Yellow,
            radius = 5f + flow * 5f,
            center = Offset(
                wellX,
                groundY + 15f
            )
        )
    }
}


@Composable
fun SimulatorTelemetry(rpm: Float) {

    val strokesPerMinute = rpm
    val estimatedRate = rpm * 4.2

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                "WELL TELEMETRY",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text("Pump strokes: %.1f / min".format(strokesPerMinute))

            Text("Estimated fluid rate: %.1f bbl/day"
                .format(estimatedRate))

            Text("Rod movement: ACTIVE")

            Text("Well status: PRODUCING")
        }
    }
}
