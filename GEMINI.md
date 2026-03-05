# GeekAdventure - AI Game Master

Interaktywna aplikacja mobilna (Android), w której zaawansowany model AI (Gemini 3.1 Flash Lite) pełni rolę Mistrza Gry (GM).

## Kluczowe Funkcjonalności

- **AI Game Master:** Dynamiczna narracja sterowana przez model `gemini-3.1-flash-lite-preview`.
- **System Statystyk (JSON):** GM przesyła stan gry w tagu `[GAME_STATE: {...}]`. Aplikacja parsuje go i wyświetla w bocznym pasku (HP, Złoto, Ekwipunek, Atrybuty).
- **Pamięć Świata (Lorebook):** System trwałego zapamiętywania faktów. Nowe informacje są wyciągane z tagu `[LORE_UPDATE: {...}]` i wstrzykiwane do każdego kolejnego zapytania AI, co zapewnia spójność fabularną.
- **Wznawianie Gry:** Historia rozmowy oraz fakty o świecie są zapisywane w bazie danych Room, co pozwala na kontynuację przygody po zamknięciu aplikacji.
- **Tematyczne GUI:** Interfejs (karty, tła, paski narzędzi, okna dialogowe) zmienia swój wygląd, kształt i efekty (np. neonowy glow, scanlines, styl pergaminu) w zależności od wybranego `ScenarioStyle`.

## Architektura i Standardy

### 1. Zarządzanie danymi
- **Baza Danych:** Room Database (`AppDatabase`). Tabele: `chat_messages` (historia) i `lore_entries` (pamięć świata).
- **Version Catalog:** Wszystkie biblioteki w `gradle/libs.versions.toml`.
- **Serialization:** Używamy `kotlinx.serialization` do obsługi JSONów od AI.

### 2. Tech Stack
- **Kotlin:** 2.2.10+ (zgodnie z `libs.versions.toml`).
- **UI:** Jetpack Compose (Material 3) z zaawansowanym Custom Drawing (`drawBehind`, `Canvas`).
- **AI Engine:** Vertex AI for Firebase.

### 3. Logika GM (Master Prompt)
- Zasady gry i formatowania są zdefiniowane w `GameViewModel.kt`.
- Każda odpowiedź AI musi zawierać tagi: `[Nagłówek: ...]`, `[GAME_STATE: ...]` oraz opcjonalnie `[LORE_UPDATE: ...]` i `[IMAGE_PROMPT: ...]`.

## Instrukcje Operacyjne (Mandates)

- **Budowanie:** Użytkownik buduje projekt samodzielnie.
- **Rozbudowa UI:** Przy dodawaniu nowych elementów interfejsu należy zawsze uwzględniać `ScenarioStyle`, aby zachować spójność tematyczną (np. inne kolory/kształty dla Cyberpunka i Fantasy).
- **Prompt:** Przy modyfikacji logiki gry, dbaj o to, aby instrukcje dla AI nie wymuszały generowania opcji "E" (Własna akcja), gdyż interfejs posiada do tego dedykowane pole.
