package pl.pointblank.geekadventure.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.pointblank.geekadventure.data.local.AppDatabase
import pl.pointblank.geekadventure.data.local.ChatMessageEntity
import pl.pointblank.geekadventure.data.local.LoreEntry
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.util.ResponseParser

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val chatDao = db.chatDao()

    private val _uiState = MutableStateFlow<GameState>(GameState.Loading)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _loreEntries = MutableStateFlow<List<LoreEntry>>(emptyList())
    val loreEntries: StateFlow<List<LoreEntry>> = _loreEntries.asStateFlow()

    private var generativeModel: GenerativeModel? = null
    private var chatSession: Chat? = null
    private var currentScenario: Scenario? = null
    private var imageGenerationEnabled: Boolean = false

    private val masterPromptBase = """
        Jesteś zaawansowanym silnikiem RPG "Geek Adventure". Twoim zadaniem jest pełnienie roli Mistrza Gry.

        KONTEKST ŚWIATA:
        Scenariusz: [SCENARIO_TITLE]
        Prompt bazowy: [SCENARIO_PROMPT]
        Opis: [SCENARIO_DESC]
        WAŻNE: Nie używaj nazw zastrzeżonych (np. Cyberpunk 2077, Gandalf, Batman). Stwórz własne, unikalne uniwersum inspirowane tymi gatunkami.

        ZASADY PROWADZENIA:
        1. PERSPEKTYWA: Pisz w 2. osobie liczby pojedynczej ("Wchodzisz", "Czujesz"). Styl ma być immersyjny i zwięzły.
        2. STRUKTURA KAŻDEJ ODPOWIEDZI: 
           - [Nagłówek: Numer i Tytuł Rozdziału]
           - Treść fabularna (2-3 krótkie akapity).
           - [Mechanika: Wynik testu, jeśli dotyczy, np. [Test Zręczności: POWODZENIE]].
           - LISTA WYBORU: A, B, C, D (zawsze proponuj 4 konkretne, zróżnicowane podejścia: siła, spryt, dyplomacja, ostrożność).
           - UWAGA: Nigdy nie dodawaj opcji "E" ani przycisku "Własna akcja" do listy. Gracz posiada stałe pole tekstowe na dole ekranu do wpisywania autorskich pomysłów.
           - ORAZ ZAWSZE na samym końcu dodaj tag [GAME_STATE: json_obiekt] ze statystykami gracza.
           - NOWOŚĆ: Jeśli w tej turze gracz poznał ważny fakt, imię NPC lub zdobył unikalny przedmiot, dodaj tag [LORE_UPDATE: {"Klucz": "Krótki opis"}].
        
        FORMAT GAME_STATE JSON:
        {"hp": Int, "gold": Int, "inventory": [String], "class": String, "stats": {"Str": Int, "Dex": Int, "Int": Int}}

        3. MECHANIKA: Ty decydujesz o trudności zadań. Używaj tagów np. [Utrata HP: -10] w tekście, ale ZAWSZE aktualizuj te dane w JSONie GAME_STATE.
        4. KONSEKWENCJE: Jeśli gracz podejmie głupią decyzję, nie bój się go ukarać utratą zasobów lub śmiercią postaci (co kończy grę).
        5. ZŁOTA ZASADA: Nigdy nie opisuj myśli ani działań gracza. Czekaj na jego input.

        PROMPT STARTOWY:
        Zanim zaczniesz fabułę, przedstaw 3 unikalne archetypy postaci pasujące do wybranego świata. Każdy musi mieć:
        - Nazwę klasy.
        - Krótki opis.
        - Statystyki bazowe (Siła, Zręczność, Inteligencja).
        - Startowy ekwipunek.
        Prezentuj te klasy jako opcje A, B, C.
    """.trimIndent()

    suspend fun hasSavedGame(scenarioId: String): Boolean {
        return chatDao.getMessageCount(scenarioId) > 0
    }

    fun initGame(scenario: Scenario, enableImages: Boolean = false, resume: Boolean = false) {
        currentScenario = scenario
        imageGenerationEnabled = enableImages
        
        val config = generationConfig {
            temperature = 0.8f
            topP = 0.95f
            maxOutputTokens = 2048
        }

        generativeModel = Firebase.ai.generativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            generationConfig = config
        )
        
        if (resume) {
            resumeAdventure()
        } else {
            startNewAdventure()
        }
    }

    private fun startNewAdventure() {
        val scenario = currentScenario ?: return
        val model = generativeModel ?: return

        viewModelScope.launch {
            chatDao.deleteMessagesForScenario(scenario.id)
            chatDao.deleteLoreForScenario(scenario.id)
            _loreEntries.value = emptyList()
            
            var systemInstruction = masterPromptBase
                .replace("[SCENARIO_TITLE]", scenario.title)
                .replace("[SCENARIO_PROMPT]", scenario.basePrompt)
                .replace("[SCENARIO_DESC]", scenario.description)
            
            if (imageGenerationEnabled) {
                systemInstruction += "\n\nDODATKOWA ZASADA DLA GRAFIKI: Na samym końcu odpowiedzi, po linii '---', dodaj tag [IMAGE_PROMPT: opis wizualny sceny po angielsku]. Tag musi być opisowy i pasować do klimatu sceny."
            }
            
            systemInstruction += "\n\nRozpocznij teraz od powitania i przedstawienia 3 archetypów klas postaci (A, B, C)."

            chatSession = model.startChat(history = emptyList())
            sendPrompt(systemInstruction, saveToHistory = false)
        }
    }

    private fun resumeAdventure() {
        val scenario = currentScenario ?: return
        val model = generativeModel ?: return

        viewModelScope.launch {
            val savedMessages = chatDao.getMessagesForScenario(scenario.id)
            val history = savedMessages.map { entity ->
                content(role = entity.role) { text(entity.content) }
            }

            chatSession = model.startChat(history = history)
            _loreEntries.value = chatDao.getLoreForScenario(scenario.id)
            
            if (history.isNotEmpty()) {
                val lastResponse = history.last().parts.first().asTextOrNull() ?: ""
                _uiState.value = GameState.Success(
                    scenario = scenario,
                    history = history,
                    latestResponse = lastResponse
                )
            } else {
                startNewAdventure()
            }
        }
    }

    fun sendPrompt(userMessage: String, saveToHistory: Boolean = true) {
        val chat = chatSession ?: return
        val scenario = currentScenario ?: return
        
        viewModelScope.launch {
            try {
                val loreEntriesList = chatDao.getLoreForScenario(scenario.id)
                val loreContext = if (loreEntriesList.isNotEmpty()) {
                    "\n\nPOPRZEDNIE FAKTY O ŚWIECIE (LORE):\n" + 
                    loreEntriesList.joinToString("\n") { "- ${it.key}: ${it.description}" }
                } else ""

                val messageWithLore = if (saveToHistory) userMessage + loreContext else userMessage

                if (saveToHistory) {
                    chatDao.insertMessage(ChatMessageEntity(
                        scenarioId = scenario.id,
                        role = "user",
                        content = userMessage
                    ))
                }

                _uiState.value = GameState.Processing(scenario, chat.history)
                
                val response = chat.sendMessage(messageWithLore)
                val responseText = response.text ?: "Mistrz Gry milczy..."
                
                val parsed = ResponseParser.parse(responseText)
                
                parsed.loreUpdate?.forEach { entry ->
                    chatDao.insertLore(LoreEntry(scenarioId = scenario.id, key = entry.key, description = entry.value))
                }
                _loreEntries.value = chatDao.getLoreForScenario(scenario.id)

                chatDao.insertMessage(ChatMessageEntity(
                    scenarioId = scenario.id,
                    role = "model",
                    content = responseText
                ))

                _uiState.value = GameState.Success(
                    scenario = scenario,
                    history = chat.history,
                    latestResponse = responseText
                )
            } catch (e: Exception) {
                _uiState.value = GameState.Error(e.message ?: "Błąd AI")
            }
        }
    }
}

sealed class GameState {
    object Loading : GameState()
    data class Processing(val scenario: Scenario, val history: List<Content>) : GameState()
    data class Success(
        val scenario: Scenario,
        val history: List<Content>,
        val latestResponse: String
    ) : GameState()
    data class Error(val message: String) : GameState()
}
