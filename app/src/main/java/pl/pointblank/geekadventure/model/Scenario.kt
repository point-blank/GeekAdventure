package pl.pointblank.geekadventure.model

import androidx.compose.ui.graphics.Color

enum class ScenarioStyle {
    CYBERPUNK, // Neon, Glow, Sci-fi font
    FANTASY,   // Parchment, Gold/Wood, Serif font
    HORROR,    // Dark, Blood/Ink, Distorted font
    PIRATES,   // Sea, Sand, Weathered Wood
    SUPERHERO, // Comic book, Bold colors, Sans-serif
    WESTERN,   // Wood, Dust, Slab font
    DEFAULT
}

data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val themeColor: Color,
    val secondaryColor: Color,
    val basePrompt: String,
    val visualStyle: ScenarioStyle = ScenarioStyle.DEFAULT,
    val imageUrl: String? = null,
    val isPremium: Boolean = false,
    val isForKids: Boolean = false,
    val iconRes: Int? = null // Nowe pole
)
