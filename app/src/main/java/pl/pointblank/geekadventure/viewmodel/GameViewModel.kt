package pl.pointblank.geekadventure.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import pl.pointblank.geekadventure.data.local.*
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.util.ResponseParser
import pl.pointblank.geekadventure.util.BillingManager
import pl.pointblank.geekadventure.util.AdsManager

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val chatDao = db.chatDao()
    private val billingManager = BillingManager(application)
    private val adsManager = AdsManager(application)

    private val _uiState = MutableStateFlow<GameState>(GameState.Loading)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    val products = billingManager.products
    val isAdLoaded = adsManager.isAdLoaded

    private val masterPromptBase = """
        Jesteś zaawansowanym silnikiem RPG "Geek Adventure". Twoim zadaniem jest pełnienie roli Mistrza Gry.

        KONTEKST ŚWIATA:
        Scenariusz: [SCENARIO_TITLE]
        Prompt bazowy: [SCENARIO_PROMPT]
        Opis: [SCENARIO_DESC]
        WAŻNE: Nie używaj nazw zastrzeżonych (np. Cyberpunk 2077, Gandalf, Batman). Stwórz własne, unikalne uniwersum inspirowane tymi gatunkami.

        ZASADY PROWADZENIA:
        1. PERSPEKTYWA: Pisz w 2. osobie liczby pojedynczej. Styl ma być immersyjny i zwięzły.
        2. STRUKTURA KAŻDEJ ODPOWIEDZI: 
           - [Nagłówek: Tytuł Sceny]
           - Treść fabularna (2-3 akapity).
           - OPISY MOŻLIWOŚCI: W treści narracji, przed listą wyboru, opisz barwnie co postać może zrobić (np. "Możesz spróbować wyważyć te zardzewiałe drzwi siłą, albo użyć wytrychów, by uniknąć hałasu...").
           - LISTA WYBORU: A, B, C, D. Każda opcja musi być skrajnie krótka: [Atrybut] Akcja. Przykład: "A: [Siła] Wyważenie", "B: [Zwinność] Skok", "C: [Inteligencja] Hakowanie".
           - ORAZ ZAWSZE na samym końcu dodaj tag [GAME_STATE: json_obiekt].
           - JEŚLI WYMAGANY TEST: Dodaj tag [RZUT: d20, trudność: X]. ZAKAZ decydowania o wyniku testu przez AI.
        
        FORMAT GAME_STATE JSON:
        {"hp": Int, "gold": Int, "inventory": [String], "class": String, "stats": {"Str": Int, "Dex": Int, "Int": Int}}

        3. MECHANIKA RZUTÓW: Kiedy użyjesz tagu [RZUT: ...], silnik gry wyświetli kostkę graczowi. Po rzucie otrzymasz od gracza wiadomość "RZUT KOSTKĄ: Wynik to Y". Dopiero WTEDY opisujesz sukces lub porażkę.
        4. KONSEKWENCJE: Jeśli gracz podejmie głupią decyzję lub zawali rzut, nie bój się go ukarać utratą zasobów lub śmiercią.
        5. ZŁOTA ZASADA: Nigdy nie opisuj myśli ani działań gracza. Czekaj na jego input.

        PROMPT STARTOWY:
        Zanim zaczniesz fabułę, przedstaw 3 unikalne archetypy postaci pasujące do wybranego świata. Każdy musi mieć:
        - Nazwę klasy.
        - Krótki opis.
        - Statystyki bazowe (Siła, Zręczność, Inteligencja).
        - Startowy ekwipunek.
        Prezentuj te klasy jako opcje A, B, C.
    """.trimIndent()

    fun showRewardedAd(activity: Activity) {
        adsManager.showRewardedAd(activity) { amount ->
            viewModelScope.launch {
                val current = userStats.value ?: UserStats()
                val updated = current.copy(actionPoints = (current.actionPoints + 1).coerceAtMost(10))
                chatDao.insertUserStats(updated)
            }
        }
    }

    fun buyProduct(activity: Activity, productId: String) {
        billingManager.launchPurchaseFlow(activity, productId)
    }

    fun processPurchaseSuccess(productId: String) {
        viewModelScope.launch {
            val current = userStats.value ?: UserStats()
            val updated = when (productId) {
                BillingManager.ENERGY_PACK -> current.copy(actionPoints = (current.actionPoints + 10).coerceAtMost(10))
                BillingManager.CRYSTAL_PACK -> current.copy(chronocrystals = current.chronocrystals + 5)
                BillingManager.PREMIUM_SUB -> current.copy(isPremiumUser = true)
                else -> current
            }
            chatDao.insertUserStats(updated)
        }
    }

    private val _loreEntries = MutableStateFlow<List<LoreEntry>>(emptyList())
    val loreEntries: StateFlow<List<LoreEntry>> = _loreEntries.asStateFlow()

    val userStats: StateFlow<UserStats?> = chatDao.getUserStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var generativeModel: GenerativeModel? = null
    private var chatSession: Chat? = null
    private var currentScenario: Scenario? = null
    private var imageGenerationEnabled: Boolean = false

    init {
        viewModelScope.launch {
            chatDao.getUserStats().collect {
                if (it == null) {
                    chatDao.insertUserStats(UserStats())
                }
            }
        }
    }

    private suspend fun checkAndRefillEnergy(): UserStats {
        val current = userStats.value ?: UserStats()
        val now = System.currentTimeMillis()
        val minutesPassed = (now - current.lastRefillTime) / (1000 * 60)
        val pointsToAdd = (minutesPassed / 15).toInt()

        if (pointsToAdd > 0) {
            val newPoints = (current.actionPoints + pointsToAdd).coerceAtMost(10)
            val newStats = current.copy(
                actionPoints = newPoints,
                lastRefillTime = now - (minutesPassed % 15) * 60 * 1000
            )
            chatDao.insertUserStats(newStats)
            return newStats
        }
        return current
    }

    suspend fun hasSavedGame(scenarioId: String): Boolean {
        return chatDao.getMessageCount(scenarioId) > 0
    }

    fun initGame(scenario: Scenario, enableImages: Boolean = false, resume: Boolean = false) {
        currentScenario = scenario
        imageGenerationEnabled = enableImages
        
        val config = generationConfig {
            temperature = 0.8f
            topP = 0.95f
            maxOutputTokens = 1024
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

    fun undoLastAction() {
        val scenario = currentScenario ?: return
        val stats = userStats.value ?: return

        if (stats.chronocrystals <= 0) {
            _uiState.value = GameState.Error("Brak Chronokryształów! Kup je lub obejrzyj reklamę.")
            return
        }

        viewModelScope.launch {
            chatDao.deleteLastTwoMessages(scenario.id)
            chatDao.insertUserStats(stats.copy(chronocrystals = stats.chronocrystals - 1))
            resumeAdventure()
        }
    }

    private fun startNewAdventure() {
        val scenario = currentScenario ?: return
        val model = generativeModel ?: return

        viewModelScope.launch {
            val stats = userStats.value ?: UserStats()
            if (scenario.isPremium && !stats.isPremiumUser) {
                _uiState.value = GameState.Error("Ten scenariusz wymaga subskrypcji Geek Master!")
                return@launch
            }

            chatDao.deleteMessagesForScenario(scenario.id)
            chatDao.deleteLoreForScenario(scenario.id)
            _loreEntries.value = emptyList()
            
            var systemInstruction = masterPromptBase
                .replace("[SCENARIO_TITLE]", scenario.title)
                .replace("[SCENARIO_PROMPT]", scenario.basePrompt)
                .replace("[SCENARIO_DESC]", scenario.description)
            
            if (scenario.isForKids) {
                systemInstruction += "\n\n!!! WAŻNE - TRYB DLA DZIECI !!!\n" +
                    "1. Używaj prostego, radosnego i zrozumiałego języka.\n" +
                    "2. Całkowity zakaz przemocy, straszenia i negatywnych zakończeń.\n" +
                    "3. LOGIKA PORAŻKI: Jeśli gracz rzuci kostką i wynik będzie niski, NIE pisz o porażce. Zamiast tego opisz zabawną sytuację lub małą przeszkodę, z której dziecko może się czegoś nauczyć, a następnie daj mu drugą szansę lub pomóż mu odnieść sukces w inny, ciekawy sposób.\n" +
                    "4. EDUKACJA: Zachęcaj do ciekawości i empatii wobec napotkanych postaci i zwierząt.\n" +
                    "5. Cel gry to zawsze dobra zabawa i poczucie sprawstwa u dziecka."
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
                val stats = checkAndRefillEnergy()
                if (saveToHistory && stats.actionPoints <= 0 && !stats.isPremiumUser) {
                    _uiState.value = GameState.Error("Brak energii! Odczekaj 15 minut lub obejrzyj reklamę w Sklepie.")
                    return@launch
                }

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
                    if (!stats.isPremiumUser) {
                        chatDao.insertUserStats(stats.copy(actionPoints = stats.actionPoints - 1))
                    }
                }

                _uiState.value = GameState.Processing(scenario, chat.history)
                
                val response = chat.sendMessage(messageWithLore)
                val responseText = response.text ?: "Mistrz Gry milczy..."
                
                val parsed = try {
                    ResponseParser.parse(responseText)
                } catch (e: Exception) {
                    Log.e("GameViewModel", "BŁĄD PARSOWANIA AI: ${e.message}")
                    Log.e("GameViewModel", "SUROWY TEKST: $responseText")
                    ResponseParser.ParsedResponse(cleanText = responseText)
                }
                
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
                Log.e("GameViewModel", "BŁĄD PROMPTU: ${e.message}", e)
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
