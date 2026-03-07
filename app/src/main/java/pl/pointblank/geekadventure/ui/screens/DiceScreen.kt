package pl.pointblank.geekadventure.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import pl.pointblank.geekadventure.viewmodel.DiceViewModel
import pl.pointblank.geekadventure.ui.theme.OrbitronFont

@Composable
fun DiceRollOverlay(
    viewModel: DiceViewModel,
    themeColor: Color,
    onResultConfirmed: (Int) -> Unit
) {
    val diceState by viewModel.diceState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Animacja rotacji podczas rzutu
    val infiniteTransition = rememberInfiniteTransition(label = "dice")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing), // Kręci się szybciej
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    // Efekt trzęsienia
    val shake by animateFloatAsState(
        targetValue = if (diceState.isRolling) 8f else 0f, // Trochę mniejszy shake X
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake"
    )

    // Animacja rozrostu kości po zatrzymaniu
    val scaleAnim by animateFloatAsState(
        targetValue = if (diceState.isRolling) 0.8f else 1.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    // Logika Sukces/Porażka do sterowania kolorami
    val isSuccess = remember(diceState.lastResult, diceState.targetDifficulty) {
        if (!diceState.isRolling && diceState.lastResult != null) {
            diceState.targetDifficulty == null || diceState.lastResult!! >= diceState.targetDifficulty!!
        } else null
    }

    // Dynamiczny kolor krawędzi kości
    val currentDiceColor by animateColorAsState(
        targetValue = when (isSuccess) {
            true -> Color(0xFF00E676) // Neon Green
            false -> Color(0xFFFF1744) // Neon Red
            null -> themeColor
        },
        animationSpec = tween(500), label = "diceColor"
    )

    // Wibracje podczas "rzucania"
    LaunchedEffect(diceState.isRolling) {
        if (diceState.isRolling) {
            while (diceState.isRolling) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Delikatne tykanie
                delay(100)
            }
        } else if (diceState.lastResult != null) {
            // Mocne tąpnięcie przy wyświetleniu wyniku
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Półprzezroczyste tło z lekkim rozbłyskiem zależnym od wyniku
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        // Promieniujący rozbłysk pod kością
        if (!diceState.isRolling && isSuccess != null) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(currentDiceColor.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TEST UMIEJĘTNOŚCI",
                fontFamily = OrbitronFont,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = themeColor, blurRadius = 10f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            diceState.targetDifficulty?.let {
                Text(
                    text = "WYMAGANY WYNIK: $it",
                    fontFamily = OrbitronFont,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            // KOSTKA 3D
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .offset(x = shake.dp, y = shake.dp) // Shake w obu osiach
                    .rotate(if (diceState.isRolling) rotation else 0f)
                    .scale(scaleAnim),
                contentAlignment = Alignment.Center
            ) {
                DiceShape20(color = currentDiceColor)

                if (!diceState.isRolling && diceState.lastResult != null) {
                    Text(
                        text = diceState.lastResult.toString(),
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = OrbitronFont,
                        modifier = Modifier.padding(bottom = 8.dp),
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(color = currentDiceColor, blurRadius = 20f)
                        )
                    )
                } else {
                    Text(
                        text = "?",
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = OrbitronFont,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            // WYNIK I PRZYCISK Z ZANIKANIEM (AnimatedVisibility)
            Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.BottomCenter) {
                if (!diceState.isRolling && isSuccess != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isSuccess) "SUKCES!" else "PORAŻKA",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = currentDiceColor,
                            fontFamily = OrbitronFont,
                            letterSpacing = 4.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(color = currentDiceColor.copy(alpha = 0.5f), blurRadius = 15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { onResultConfirmed(diceState.lastResult!!) },
                            colors = ButtonDefaults.buttonColors(containerColor = currentDiceColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .width(240.dp)
                        ) {
                            Text(
                                text = "ZATWIERDŹ",
                                color = if (isSuccess) Color.Black else Color.White,
                                fontWeight = FontWeight.Black,
                                fontFamily = OrbitronFont,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "RZUCANIE KOŚCIĄ...",
                        fontFamily = OrbitronFont,
                        color = themeColor.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
        val r = w * 0.45f // Odrobinę mniejsza, by nie dotykała brzegów Boxa

        // Punkty zewnętrzne (Hexagon)
        val p1 = Offset(cx, cy - r)
        val p2 = Offset(cx + r * 0.86f, cy - r * 0.5f)
        val p3 = Offset(cx + r * 0.86f, cy + r * 0.5f)
        val p4 = Offset(cx, cy + r)
        val p5 = Offset(cx - r * 0.86f, cy + r * 0.5f)
        val p6 = Offset(cx - r * 0.86f, cy - r * 0.5f)

        // Punkty wewnętrznego trójkąta (Frontalna ścianka, na której jest liczba)
        val innerR = r * 0.6f
        val f1 = Offset(cx, cy - innerR)
        val f2 = Offset(cx + innerR * 0.86f, cy + innerR * 0.5f)
        val f3 = Offset(cx - innerR * 0.86f, cy + innerR * 0.5f)

        // 1. Rysowanie ścianek bocznych (lekki wypełniacz dla efektu bryły)
        val sidePath = Path().apply {
            moveTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(f2.x, f2.y)
            lineTo(f1.x, f1.y); close()
        }
        drawPath(sidePath, color.copy(alpha = 0.05f))

        // 2. Główny obrys zewnętrzny (Neon Glow)
        val outlinePath = Path().apply {
            moveTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y); lineTo(p5.x, p5.y); lineTo(p6.x, p6.y); close()
        }
        drawPath(outlinePath, color, style = Stroke(width = 4.dp.toPx()))
        drawPath(outlinePath, color.copy(alpha = 0.2f), style = Stroke(width = 12.dp.toPx())) // Poświata

        // 3. Linie konstrukcyjne (tylko niezbędne dla efektu 3D, omijające środek)
        // Łączymy rogi zewnętrzne z rogami przedniej ścianki
        val strokeWidth = 2.dp.toPx()
        val lineAlpha = 0.4f

        drawLine(color.copy(alpha = lineAlpha), p1, f1, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p2, f1, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p2, f2, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p3, f2, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p4, f2, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p4, f3, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p5, f3, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p6, f3, strokeWidth)
        drawLine(color.copy(alpha = lineAlpha), p6, f1, strokeWidth)

        // 4. Obrys przedniej ścianki (tej, na której jest cyfra)
        val frontFacePath = Path().apply {
            moveTo(f1.x, f1.y); lineTo(f2.x, f2.y); lineTo(f3.x, f3.y); close()
        }
        drawPath(frontFacePath, color.copy(alpha = 0.6f), style = Stroke(width = 3.dp.toPx()))
    }
}