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

    data class DiceRequest(
        val type: Int = 20,
        val difficulty: Int? = null
    )

    data class ParsedResponse(
        val cleanText: String,
        val chapterTitle: String? = null,
        val mechanicsTag: String? = null,
        val imagePrompt: String? = null,
        val gameState: PlayerStats? = null,
        val loreUpdate: Map<String, String>? = null,
        val diceRequest: DiceRequest? = null,
        val options: List<String> = emptyList()
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(rawText: String): ParsedResponse {
        var cleanText = rawText

        // 1. Wyciąganie Nagłówka Rozdziału
        val chapterRegex = Regex("\\[Nagłówka: ([^]]+)]")
        val chapterMatch = chapterRegex.find(cleanText)
        val chapterTitle = chapterMatch?.groupValues?.get(1)
        cleanText = cleanText.replace(chapterRegex, "")

        // 2. Wyciąganie Mechaniki
        val mechanicsRegex = Regex("\\[Mechanika: ([^]]+)]")
        val mechanicsMatch = mechanicsRegex.find(cleanText)
        val mechanicsTag = mechanicsMatch?.groupValues?.get(1)
        cleanText = cleanText.replace(mechanicsRegex, "")

        // 3. Wyciąganie RZUTU [RZUT: d20, trudność: 15]
        val diceRegex = Regex("\\[RZUT: d(\\d+)(?:,\\s*trudność:\\s*(\\d+))?]")
        val diceMatch = diceRegex.find(cleanText)
        val diceRequest = diceMatch?.let {
            DiceRequest(
                type = it.groupValues[1].toIntOrNull() ?: 20,
                difficulty = it.groupValues[2].toIntOrNull()
            )
        }
        cleanText = cleanText.replace(diceRegex, "")

        // 4. Wyciąganie IMAGE_PROMPT
        val imageRegex = Regex("\\[IMAGE_PROMPT: ([^]]+)]")
        val imageMatch = imageRegex.find(cleanText)
        val imagePrompt = imageMatch?.groupValues?.get(1)
        cleanText = cleanText.replace(imageRegex, "")

        // 5. Wyciąganie GAME_STATE (JSON)
        val gameStateRegex = Regex("\\[GAME_STATE: (\\{.*?\\})]")
        val gameStateMatch = gameStateRegex.find(cleanText)
        val gameStateJson = gameStateMatch?.groupValues?.get(1)
        val gameState = try {
            gameStateJson?.let { json.decodeFromString<PlayerStats>(it) }
        } catch (e: Exception) {
            null
        }
        cleanText = cleanText.replace(gameStateRegex, "")

        // 6. Wyciąganie LORE_UPDATE
        val loreRegex = Regex("\\[LORE_UPDATE: (\\{.*?\\})]")
        val loreMatch = loreRegex.find(cleanText)
        val loreJsonString = loreMatch?.groupValues?.get(1)
        val loreUpdate = try {
            loreJsonString?.let { json.decodeFromString<Map<String, String>>(it) }
        } catch (e: Exception) {
            null
        }
        cleanText = cleanText.replace(loreRegex, "")

// 7. Wyciąganie opcji (PANCERNY REGEX)
        val optionRegex = Regex("(?m)^[\\s]*[*-]*\\s*\\*?([A-Ea-e1-9])\\*?[:.)]\\s*(.*)$")
        val options = mutableListOf<String>()
        val matches = optionRegex.findAll(cleanText)

        matches.forEach { match ->
            val identifier = match.groupValues[1].uppercase()
            val fullContent = match.groupValues[2].replace("*", "").trim()

            // Konwertujemy cyfry na litery dla spójności interfejsu (1 -> A)
            val mappedLetter = when (identifier) {
                "1" -> "A"
                "2" -> "B"
                "3" -> "C"
                "4" -> "D"
                "5" -> "E"
                else -> identifier
            }

            // SMART SHORTENER - Wyciągamy krótki tytuł do przycisku!
            var shortContent = fullContent

            // Scenariusz A: Tytuł jest w nawiasach kwadratowych, np. "[Właz serwisowy] - Ryzykujesz..."
            val bracketMatch = Regex("\\[(.*?)\\]").find(fullContent)
            if (bracketMatch != null) {
                shortContent = bracketMatch.groupValues[1]
            } else {
                // Scenariusz B: Użyto myślnika lub dwukropka, np. "Właz serwisowy - Ryzykujesz..."
                val dashIndex = fullContent.indexOfFirst { it == '-' || it == ':' }
                if (dashIndex > 0) {
                    shortContent = fullContent.substring(0, dashIndex).trim()
                } else {
                    // Scenariusz C: Zwykłe, długie zdanie. Bierzemy pierwsze 4 słowa.
                    val words = fullContent.split("\\s+".toRegex())
                    if (words.size > 5) {
                        shortContent = words.take(4).joinToString(" ") + "..."
                    }
                }
            }

            // Ekstremalne zabezpieczenie na długość (żeby na 100% nie rozsadziło przycisku)
            if (shortContent.length > 25) {
                shortContent = shortContent.take(22) + "..."
            }

            options.add("$mappedLetter: $shortContent")
        }

        return ParsedResponse(
            cleanText = cleanText.trim().replace(Regex("---$"), "").trim(),
            chapterTitle = chapterTitle,
            mechanicsTag = mechanicsTag,
            imagePrompt = imagePrompt,
            gameState = gameState,
            loreUpdate = loreUpdate,
            diceRequest = diceRequest,
            options = options
        )
    }
}