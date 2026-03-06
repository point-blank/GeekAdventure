package pl.pointblank.geekadventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pl.pointblank.geekadventure.ui.theme.OrbitronFont
import kotlin.random.Random

data class PixelParticle(
    val startPos: Offset,
    val targetPos: Offset,
    val color: Color
)

@Composable
fun PointBlankSplashScreen(onAnimationFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    val particles = remember { mutableStateListOf<PixelParticle>() }
    
    LaunchedEffect(Unit) {
        val red = Color(0xFFFF1744)
        val cyan = Color(0xFF00E5FF)
        
        // Generujemy prawdziwe logo z pikseli
        particles.addAll(generateLogoParticles(red, cyan))
        
        progress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        alpha.animateTo(1f, animationSpec = tween(400))
        delay(800)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val pixelSize = 8f // Nieco większe piksele dla retro efektu
            
            particles.forEach { particle ->
                val currentX = center.x + (particle.startPos.x + (particle.targetPos.x - particle.startPos.x) * progress.value)
                val currentY = center.y + (particle.startPos.y + (particle.targetPos.y - particle.startPos.y) * progress.value)
                
                drawRect(
                    color = particle.color,
                    topLeft = Offset(currentX, currentY),
                    size = Size(pixelSize, pixelSize)
                )
                
                if (progress.value > 0.9f) {
                    drawRect(
                        color = particle.color.copy(alpha = 0.4f),
                        topLeft = Offset(currentX - 2f, currentY - 2f),
                        size = Size(pixelSize + 4f, pixelSize + 4f)
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (progress.value > 0.95f) {
                Text(
                    "POINT-BLANK STUDIO",
                    color = Color.White.copy(alpha = alpha.value),
                    fontFamily = OrbitronFont,
                    fontSize = 14.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "PRODUCTION",
                    color = Color.Gray.copy(alpha = alpha.value),
                    fontFamily = OrbitronFont,
                    fontSize = 10.sp,
                    letterSpacing = 10.sp
                )
            }
        }
    }
}

// Mapa liter Pixel Art (5x5 dla każdej litery)
private val pixelMaps = mapOf(
    'P' to listOf("XXXX ", "X   X", "XXXX ", "X    ", "X    "),
    'O' to listOf(" XXX ", "X   X", "X   X", "X   X", " XXX "),
    'I' to listOf(" XXX ", "  X  ", "  X  ", "  X  ", " XXX "),
    'N' to listOf("X   X", "XX  X", "X X X", "X  XX", "X   X"),
    'T' to listOf("XXXXX", "  X  ", "  X  ", "  X  ", "  X  "),
    '-' to listOf("     ", "     ", " XXX ", "     ", "     "),
    'B' to listOf("XXXX ", "X   X", "XXXX ", "X   X", "XXXX "),
    'L' to listOf("X    ", "X    ", "X    ", "X    ", "XXXXX"),
    'A' to listOf(" XXX ", "X   X", "XXXXX", "X   X", "X   X"),
    'K' to listOf("X  X ", "X X  ", "XX   ", "X X  ", "X  X ")
)

private fun generateLogoParticles(red: Color, cyan: Color): List<PixelParticle> {
    val particles = mutableListOf<PixelParticle>()
    val spacing = 12f // Odstęp między pikselami
    val letterSpacing = 70f // Odstęp między literami
    
    val text1 = "POINT-"
    val text2 = "BLANK"
    
    // Generowanie POINT-
    text1.forEachIndexed { lIdx, char ->
        val map = pixelMaps[char] ?: return@forEachIndexed
        val xOffset = -400f + (lIdx * letterSpacing)
        
        map.forEachIndexed { yIdx, row ->
            row.forEachIndexed { xIdx, symbol ->
                if (symbol == 'X') {
                    val target = Offset(xOffset + xIdx * spacing, yIdx * spacing - 50f)
                    val start = Offset(Random.nextInt(-1500, 1500).toFloat(), Random.nextInt(-1500, 1500).toFloat())
                    particles.add(PixelParticle(start, target, red))
                }
            }
        }
    }
    
    // Generowanie BLANK
    text2.forEachIndexed { lIdx, char ->
        val map = pixelMaps[char] ?: return@forEachIndexed
        val xOffset = 50f + (lIdx * letterSpacing)
        
        map.forEachIndexed { yIdx, row ->
            row.forEachIndexed { xIdx, symbol ->
                if (symbol == 'X') {
                    val target = Offset(xOffset + xIdx * spacing, yIdx * spacing - 50f)
                    val start = Offset(Random.nextInt(-1500, 1500).toFloat(), Random.nextInt(-1500, 1500).toFloat())
                    particles.add(PixelParticle(start, target, cyan))
                }
            }
        }
    }
    
    return particles
}
