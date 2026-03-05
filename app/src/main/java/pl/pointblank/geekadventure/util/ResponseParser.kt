package pl.pointblank.geekadventure.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ResponseParser {

    @Serializable
    data class PlayerStats(
        val hp: Int = 100,
        val gold: Int = 0,
        val inventory: List<String> = emptyList(),
        val `class`: String = "None",
        val stats: Map<String, Int> = emptyMap()
    )

    data class ParsedResponse(
        val cleanText: String,
        val chapterTitle: String? = null,
        val mechanicsTag: String? = null,
        val imagePrompt: String? = null,
        val gameState: PlayerStats? = null,
        val loreUpdate: Map<String, String>? = null,
        val options: List<String> = emptyList()
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(rawText: String): ParsedResponse {
        var cleanText = rawText
        
        // 1. Wyciąganie Nagłówka Rozdziału [Nagłówek: Numer i Tytuł Rozdziału]
        val chapterRegex = Regex("\\[Nagłówek: ([^]]+)]")
        val chapterMatch = chapterRegex.find(cleanText)
        val chapterTitle = chapterMatch?.groupValues?.get(1)
        cleanText = cleanText.replace(chapterRegex, "")

        // 2. Wyciąganie Mechaniki [Mechanika: ...]
        val mechanicsRegex = Regex("\\[Mechanika: ([^]]+)]")
        val mechanicsMatch = mechanicsRegex.find(cleanText)
        val mechanicsTag = mechanicsMatch?.groupValues?.get(1)
        cleanText = cleanText.replace(mechanicsRegex, "")

        // 3. Wyciąganie IMAGE_PROMPT
        val imageRegex = Regex("\\[IMAGE_PROMPT: ([^]]+)]")
        val imageMatch = imageRegex.find(cleanText)
        val imagePrompt = imageMatch?.groupValues?.get(1)
        cleanText = cleanText.replace(imageRegex, "")

        // 4. Wyciąganie GAME_STATE (JSON)
        val gameStateRegex = Regex("\\[GAME_STATE: (\\{.*?\\})]")
        val gameStateMatch = gameStateRegex.find(cleanText)
        val gameStateJson = gameStateMatch?.groupValues?.get(1)
        val gameState = try {
            gameStateJson?.let { json.decodeFromString<PlayerStats>(it) }
        } catch (e: Exception) {
            null
        }
        cleanText = cleanText.replace(gameStateRegex, "")

        // 5. Wyciąganie LORE_UPDATE
        val loreRegex = Regex("\\[LORE_UPDATE: (\\{.*?\\})]")
        val loreMatch = loreRegex.find(cleanText)
        val loreJsonString = loreMatch?.groupValues?.get(1)
        val loreUpdate = try {
            loreJsonString?.let { json.decodeFromString<Map<String, String>>(it) }
        } catch (e: Exception) {
            null
        }
        cleanText = cleanText.replace(loreRegex, "")

        // 6. Wyciąganie opcji A, B, C, D, E
        // Regex szuka linii zaczynających się od spacji, gwiazdek, myślników, litery A-E, a potem separatora : . lub )
        val optionRegex = Regex("(?m)^[\\s*-]*\\*?([A-E])\\*?[:.)] (.*)$")
        val options = mutableListOf<String>()
        
        val matches = optionRegex.findAll(cleanText)
        matches.forEach { match ->
            val letter = match.groupValues[1]
            // Usuwamy gwiazdki markdowna z treści opcji
            val content = match.groupValues[2].replace("*", "").trim()
            options.add("$letter: $content")
        }

        return ParsedResponse(
            cleanText = cleanText.trim().replace(Regex("---$"), "").trim(),
            chapterTitle = chapterTitle,
            mechanicsTag = mechanicsTag,
            imagePrompt = imagePrompt,
            gameState = gameState,
            loreUpdate = loreUpdate,
            options = options
        )
    }
}
