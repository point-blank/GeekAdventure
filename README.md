# GeekAdventure - AI Game Master 🎮🤖

**GeekAdventure** to interaktywna aplikacja mobilna na system Android, w której zaawansowany model AI (**Gemini 3.1 Flash Lite**) pełni rolę Twojego osobistego Mistrza Gry (GM). Przeżyj niesamowite przygody w światach fantasy, cyberpunk i wielu innych, gdzie każda Twoja decyzja realnie wpływa na historię.

## ✨ Kluczowe Funkcjonalności

- 🧠 **AI Game Master:** Dynamiczna narracja sterowana przez model `gemini-3.1-flash-lite-preview` od Google.
- 📊 **Dynamiczny System Statystyk:** AI aktualizuje Twój stan gry (HP, Złoto, Ekwipunek, Atrybuty) w czasie rzeczywistym.
- 📜 **Lorebook (Pamięć Świata):** System trwałego zapamiętywania faktów i ważnych wydarzeń, co zapewnia spójność fabularną przez całą rozgrywkę.
- 💾 **Zapis Stanu Gry:** Pełna historia przygód oraz lore są zapisywane lokalnie (Room DB), co pozwala wrócić do zabawy w dowolnym momencie.
- 🎨 **Tematyczne GUI:** Interfejs automatycznie zmienia styl (kolory, kształty, efekty wizualne) w zależności od wybranego scenariusza (np. neonowy glow dla Cyberpunka, styl pergaminu dla Fantasy).
- 🎲 **Wbudowana Kostka:** Zintegrowany system rzutów kostką dla kluczowych testów umiejętności.

## 🛠️ Tech Stack

- **Język:** Kotlin (2.1.10+)
- **UI:** Jetpack Compose (Material 3) z autorskimi efektami graficznymi (Canvas API).
- **Baza Danych:** Room Database (obsługa historii czatu i lore).
- **AI Engine:** Vertex AI for Firebase (Gemini SDK).
- **Serializacja:** Kotlinx.serialization (do obsługi danych strukturalnych od AI).
- **Architektura:** MVVM (Model-View-ViewModel).

## 🚀 Jak zacząć?

1. **Skonfiguruj Firebase:** Projekt wymaga integracji z Firebase oraz włączonej usługi Vertex AI.
2. **Dodaj google-services.json:** Umieść plik konfiguracyjny w folderze `app/`.
3. **Zbuduj projekt:** Otwórz projekt w Android Studio i uruchom na emulatorze lub fizycznym urządzeniu.

## 📝 Formatowanie AI (Protokół Komunikacji)

Aplikacja komunikuje się z modelem Gemini za pomocą ustrukturyzowanych tagów:
- `[Nagłówek: ...]` - Tytuł aktualnej sceny.
- `[GAME_STATE: {...}]` - Dane JSON o statystykach gracza.
- `[LORE_UPDATE: {...}]` - Nowe fakty do zapamiętania.
- `[IMAGE_PROMPT: ...]` - Opis do generowania obrazów tła (opcjonalnie).

---
*Projekt stworzony z pasją do gier RPG i nowoczesnych technologii AI.*
