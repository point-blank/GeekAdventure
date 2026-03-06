package pl.pointblank.geekadventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pl.pointblank.geekadventure.ui.theme.GameThemeData

@Composable
fun TypewriterText(
    text: String,
    theme: GameThemeData,
    speedMillis: Long = 10,
    isLarge: Boolean = false,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { _, char ->
            displayedText += char
            if (char !in listOf(' ', '.', ',')) {
                delay(speedMillis)
            } else {
                delay(speedMillis / 2)
            }
        }
        onComplete()
    }

    val annotatedString = buildAnnotatedString {
        val raw = displayedText
        var cursor = 0
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = boldRegex.findAll(raw)
        
        matches.forEach { match ->
            append(raw.substring(cursor, match.range.first))
            withStyle(SpanStyle(
                fontWeight = FontWeight.Bold, 
                color = theme.primaryColor,
                fontFamily = theme.fontFamily
            )) {
                append(match.groupValues[1])
            }
            cursor = match.range.last + 1
        }
        
        if (cursor < raw.length) {
            append(raw.substring(cursor))
        }
    }

    Text(
        text = annotatedString,
        style = TextStyle(
            fontFamily = theme.fontFamily,
            lineHeight = if (isLarge) 36.sp else 30.sp,
            fontSize = if (isLarge) 24.sp else 18.sp,
            letterSpacing = 0.5.sp
        ),
        color = theme.contentColor
    )
}

@Composable
fun ImmersiveButton(
    text: String,
    theme: GameThemeData,
    isLarge: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "scale")
    val glowAlpha by animateFloatAsState(targetValue = if (isPressed) 0.8f else theme.glowStrength, label = "glow")

    val attrRegex = Regex("\\[(.*?)]")
    val attrMatch = attrRegex.find(text)
    val attribute = attrMatch?.groupValues?.get(1)
    
    val cleanText = text
        .replace(Regex("^[A-E][:.)]\\s*"), "")
        .replace(attrRegex, "")
        .trim()

    val icon = getIconForOption(attribute ?: cleanText)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (isLarge) 80.dp else 64.dp)
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isPressed) 2.dp else 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        theme.primaryColor.copy(alpha = 0.5f),
                        theme.secondaryColor.copy(alpha = glowAlpha),
                        theme.primaryColor.copy(alpha = 0.5f)
                    )
                ),
                shape = theme.buttonShape
            )
            .background(
                color = theme.surfaceColor.copy(alpha = if (isPressed) 0.4f else 0.2f), 
                shape = theme.buttonShape
            )
            .clip(theme.buttonShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.primaryColor,
                    modifier = Modifier.size(if (isLarge) 32.dp else 24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column {
                if (attribute != null) {
                    Text(
                        text = attribute.uppercase(),
                        style = TextStyle(
                            fontSize = if (isLarge) 14.sp else 10.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryColor,
                            fontFamily = theme.fontFamily,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Text(
                    text = cleanText,
                    style = TextStyle(
                        fontSize = if (isLarge) 20.sp else 14.sp,
                        color = theme.contentColor,
                        fontFamily = theme.fontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

fun getIconForOption(text: String): ImageVector? {
    val lower = text.lowercase()
    return when {
        "siła" in lower || "atak" in lower || "walcz" in lower || "miecz" in lower -> Icons.Default.Hardware
        "zwinność" in lower || "unik" in lower || "biegnij" in lower || "uciekaj" in lower -> Icons.AutoMirrored.Filled.ArrowForward
        "charyzma" in lower || "rozmawiaj" in lower || "pytaj" in lower -> Icons.Default.RecordVoiceOver
        "inteligencja" in lower || "zbadaj" in lower || "szukaj" in lower || "patrz" in lower -> Icons.Default.Search
        "skradanie" in lower || "cisza" in lower -> Icons.Default.VisibilityOff
        else -> null
    }
}

@Composable
fun ShimmerImagePlaceholder(theme: GameThemeData) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer_x"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            theme.surfaceColor.copy(alpha = 0.6f),
            theme.primaryColor.copy(alpha = 0.3f),
            theme.surfaceColor.copy(alpha = 0.6f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(theme.containerShape)
            .background(brush)
            .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), theme.containerShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Image, 
            contentDescription = null, 
            tint = theme.primaryColor.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
    }
}

fun Modifier.vignette(color: Color): Modifier = this.drawWithContent {
    drawContent()
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, color),
            radius = size.maxDimension / 1.1f,
            center = center
        )
    )
}
