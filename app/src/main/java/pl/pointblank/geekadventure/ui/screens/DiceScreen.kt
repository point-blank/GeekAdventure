package pl.pointblank.geekadventure.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import pl.pointblank.geekadventure.viewmodel.DiceViewModel
import pl.pointblank.geekadventure.ui.theme.OrbitronFont

@Composable
fun DiceRollOverlay(
    viewModel: DiceViewModel,
    themeColor: Color,
    onResultConfirmed: (Int) -> Unit
) {
    val diceState by viewModel.diceState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "dice")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    
    val shake by animateFloatAsState(
        targetValue = if (diceState.isRolling) 15f else 0f,
        animationSpec = infiniteRepeatable(tween(40, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "TEST UMIEJĘTNOŚCI",
                fontFamily = OrbitronFont,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            diceState.targetDifficulty?.let {
                Text(
                    "WYMAGANY WYNIK: $it",
                    fontFamily = OrbitronFont,
                    color = themeColor.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // KOSTKA 3D (UDAWANA)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .offset(x = shake.dp)
                    .rotate(if (diceState.isRolling) rotation else 0f)
                    .scale(if (diceState.isRolling) 1.1f else 1.0f),
                contentAlignment = Alignment.Center
            ) {
                DiceShape20(color = themeColor)
                
                Text(
                    text = diceState.lastResult?.toString() ?: "?",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = OrbitronFont,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            if (!diceState.isRolling && diceState.lastResult != null) {
                val success = diceState.targetDifficulty == null || diceState.lastResult!! >= diceState.targetDifficulty!!
                
                LaunchedEffect(Unit) {
                    // Tutaj można dodać haptic feedback
                }

                Text(
                    text = if (success) "SUKCES!" else "PORAŻKA",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = if (success) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontFamily = OrbitronFont
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = { onResultConfirmed(diceState.lastResult!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(56.dp).width(200.dp)
                ) {
                    Text("ZATWIERDŹ", color = Color.Black, fontWeight = FontWeight.Black, fontFamily = OrbitronFont)
                }
            } else {
                Text(
                    "RZUCANIE KOSTKĄ...",
                    fontFamily = OrbitronFont,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DiceShape20(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val r = w / 2

        val p1 = Offset(cx, cy - r)
        val p2 = Offset(cx + r * 0.86f, cy - r * 0.5f)
        val p3 = Offset(cx + r * 0.86f, cy + r * 0.5f)
        val p4 = Offset(cx, cy + r)
        val p5 = Offset(cx - r * 0.86f, cy + r * 0.5f)
        val p6 = Offset(cx - r * 0.86f, cy - r * 0.5f)
        val center = Offset(cx, cy)

        // Rysujemy tylko szkielet (krawędzie)
        val outlinePath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); lineTo(p4.x, p4.y)
            lineTo(p5.x, p5.y); lineTo(p6.x, p6.y); close()
        }
        
        // Główny obrys z lekką poświatą
        drawPath(outlinePath, color, style = Stroke(width = 6.dp.toPx()))
        
        // Krawędzie wewnętrzne tworzące bryłę 3D
        drawLine(color, center, p1, strokeWidth = 2.dp.toPx())
        drawLine(color, center, p2, strokeWidth = 2.dp.toPx())
        drawLine(color, center, p3, strokeWidth = 2.dp.toPx())
        drawLine(color, center, p4, strokeWidth = 2.dp.toPx())
        drawLine(color, center, p5, strokeWidth = 2.dp.toPx())
        drawLine(color, center, p6, strokeWidth = 2.dp.toPx())
        
        // Dodatkowe linie łączące wierzchołki dla pełnego efektu d20
        drawLine(color.copy(alpha = 0.5f), p1, p3, strokeWidth = 1.dp.toPx())
        drawLine(color.copy(alpha = 0.5f), p1, p5, strokeWidth = 1.dp.toPx())
        drawLine(color.copy(alpha = 0.5f), p4, p2, strokeWidth = 1.dp.toPx())
        drawLine(color.copy(alpha = 0.5f), p4, p6, strokeWidth = 1.dp.toPx())
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTriangle(a: Offset, b: Offset, c: Offset, color: Color) {
    val path = Path().apply {
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        close()
    }
    drawPath(path, color)
}
