package pl.pointblank.geekadventure.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import pl.pointblank.geekadventure.R
import pl.pointblank.geekadventure.model.ScenarioStyle

// Provider
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Definicje czcionek z JAWNYMI fallbackami do systemowych
val OrbitronFont = FontFamily(
    Font(googleFont = GoogleFont("Orbitron"), fontProvider = fontProvider),
    androidx.compose.ui.text.font.Font(resId = 0, weight = FontWeight.Normal) // Placeholder
).let { FontFamily.Monospace } // WYMUSZAMY MONOSPACE DLA TESTU

val CinzelFont = FontFamily.Serif // WYMUSZAMY SERIF DLA TESTU

val ComicFont = FontFamily.SansSerif // WYMUSZAMY SANS DLA TESTU

val SpecialEliteFont = FontFamily.Monospace

val CyberpunkShape = GenericShape { size, _ ->
    moveTo(0f, 15f); lineTo(15f, 0f); lineTo(size.width, 0f)
    lineTo(size.width, size.height - 15f); lineTo(size.width - 15f, size.height); lineTo(0f, size.height); close()
}

data class GameThemeData(
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val contentColor: Color,
    val surfaceColor: Color,
    val fontFamily: FontFamily,
    val containerShape: Shape,
    val buttonShape: Shape,
    val glowStrength: Float,
    val isDarkTheme: Boolean = true
)

object ThemeEngine {
    fun getTheme(style: ScenarioStyle, basePrimary: Color, baseSecondary: Color): GameThemeData {
        return when (style) {
            ScenarioStyle.CYBERPUNK -> GameThemeData(
                primaryColor = basePrimary,
                secondaryColor = baseSecondary,
                backgroundColor = Color(0xFF050505),
                contentColor = basePrimary,
                surfaceColor = Color(0xFF121212),
                fontFamily = FontFamily.Monospace, // Gwarantowany techniczny wygląd
                containerShape = CyberpunkShape,
                buttonShape = CyberpunkShape,
                glowStrength = 0.8f
            )
            ScenarioStyle.FANTASY -> GameThemeData(
                primaryColor = Color(0xFF4B2C20),
                secondaryColor = Color(0xFF8B4513),
                backgroundColor = Color(0xFFF5E6D3),
                contentColor = Color(0xFF2B1B17),
                surfaceColor = Color(0xFFEBDCB2).copy(alpha = 0.6f),
                fontFamily = FontFamily.Serif, // Gwarantowany elegancki wygląd
                containerShape = RoundedCornerShape(16.dp),
                buttonShape = RoundedCornerShape(50),
                glowStrength = 0.1f,
                isDarkTheme = false
            )
            ScenarioStyle.HORROR -> GameThemeData(
                primaryColor = Color(0xFF8B0000),
                secondaryColor = Color.Gray,
                backgroundColor = Color(0xFF0A0A0A),
                contentColor = Color(0xFFD3D3D3),
                surfaceColor = Color(0xFF1A1A1A),
                fontFamily = FontFamily.Monospace,
                containerShape = RoundedCornerShape(2.dp),
                buttonShape = RoundedCornerShape(2.dp),
                glowStrength = 0.0f
            )
            ScenarioStyle.SUPERHERO -> GameThemeData(
                primaryColor = Color(0xFFE53935),
                secondaryColor = Color(0xFFFFD600),
                backgroundColor = Color(0xFFF5F5F5),
                contentColor = Color.Black,
                surfaceColor = Color.White,
                fontFamily = FontFamily.SansSerif,
                containerShape = RoundedCornerShape(0.dp),
                buttonShape = RoundedCornerShape(4.dp),
                glowStrength = 0.0f,
                isDarkTheme = false
            )
            ScenarioStyle.WESTERN -> GameThemeData(
                primaryColor = Color(0xFF5D4037),
                secondaryColor = Color(0xFFD2B48C),
                backgroundColor = Color(0xFFD2B48C),
                contentColor = Color(0xFF2B1B17),
                surfaceColor = Color(0xFFEBDCB2),
                fontFamily = FontFamily.Serif,
                containerShape = RoundedCornerShape(8.dp),
                buttonShape = RoundedCornerShape(8.dp),
                glowStrength = 0.0f,
                isDarkTheme = false
            )
            else -> GameThemeData(
                primaryColor = basePrimary,
                secondaryColor = baseSecondary,
                backgroundColor = Color(0xFF121212),
                contentColor = Color.White,
                surfaceColor = Color(0xFF1E1E1E),
                fontFamily = FontFamily.Default,
                containerShape = RoundedCornerShape(8.dp),
                buttonShape = RoundedCornerShape(8.dp),
                glowStrength = 0.2f
            )
        }
    }
}
