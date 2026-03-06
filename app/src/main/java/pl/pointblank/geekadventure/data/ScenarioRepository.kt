package pl.pointblank.geekadventure.data

import androidx.compose.ui.graphics.Color
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.R

object ScenarioRepository {
    val scenarios = listOf(
        Scenario(
            id = "neo_katowice",
            title = "Neo-Katowice 2077",
            description = "Cyberpunkowa wizja Śląska. Spodka nie ma, są korporacje i hakerzy w cieniu hałd.",
            themeColor = Color(0xFF00FFFF),
            secondaryColor = Color(0xFFFF00FF),
            basePrompt = "Świat Cyberpunka w Neo-Katowicach: technologia, neony, korporacje i śląski klimat.",
            visualStyle = ScenarioStyle.CYBERPUNK,
            iconRes = R.drawable.ic_spodek
        ),
        Scenario(
            id = "fantasy_realm",
            title = "Smocze Przymierze",
            description = "Klasyczne high fantasy. Rycerze, magia i pradawne smoki budzące się ze snu.",
            themeColor = Color(0xFF8B4513),
            secondaryColor = Color(0xFFFFD700),
            basePrompt = "Świat High Fantasy: magia, miecze, zamki i smoki.",
            visualStyle = ScenarioStyle.FANTASY,
            iconRes = R.drawable.ic_dragon
        ),
        Scenario(
            id = "cursed_pearl",
            title = "Klątwa Czarnej Perły",
            description = "Piracka przygoda na Karaibach. Ukryte skarby, rum i nieumarła załoga.",
            themeColor = Color(0xFF008080),
            secondaryColor = Color(0xFFC2B280),
            basePrompt = "Świat Piratów: morze, statki, skarby i pirackie legendy.",
            visualStyle = ScenarioStyle.PIRATES,
            iconRes = R.drawable.ic_anchor
        ),
        Scenario(
            id = "dnd_classic",
            title = "Lochy i Smoki (D&D)",
            description = "Klasyczna sesja RPG. Wybierz Wojownika, Maga lub Łotra i ruszaj do podziemi!",
            themeColor = Color(0xFFE64A19),
            secondaryColor = Color(0xFF212121),
            basePrompt = "Klasyczne Dungeons & Dragons. Rozpocznij od wyboru klasy: Wojownik, Mag, Łotr, Kapłan. Używaj zasad d20.",
            visualStyle = ScenarioStyle.FANTASY,
            isPremium = true,
            iconRes = R.drawable.ic_dragon
        ),
        Scenario(
            id = "arkham_horror",
            title = "Cienie nad Innsmouth",
            description = "Horror Lovecraftowski. Tajemnicze zniknięcia, kulty i przedwieczni bogowie.",
            themeColor = Color(0xFF2F4F4F),
            secondaryColor = Color(0xFF000000),
            basePrompt = "Świat Lovecraftowski horror: mrok, obłęd, kultyści i istoty spoza czasu.",
            visualStyle = ScenarioStyle.HORROR
        ),
        Scenario(
            id = "superhero_league",
            title = "Liga Sprawiedliwych",
            description = "Bądź bohaterem z supermocami i chroń miasto przed superzłoczyńcami.",
            themeColor = Color(0xFFE53935),
            secondaryColor = Color(0xFF0D47A1),
            basePrompt = "Świat Superbohaterów: moce, kostiumy, tajne tożsamości i epickie bitwy o miasto.",
            visualStyle = ScenarioStyle.SUPERHERO
        ),
        
        // SEKCJA DLA DZIECI
        Scenario(
            id = "dino_island",
            title = "Wyspa Dinozaurów",
            description = "Zostań badaczem dinozaurów! Poznaj przyjaznego Triceratopsa i szukaj jaj T-Rexa.",
            themeColor = Color(0xFF4CAF50),
            secondaryColor = Color(0xFF8BC34A),
            basePrompt = "PRZYGODA DLA DZIECI: Świat dinozaurów. Język prosty, edukacyjny, zero przemocy. Dinozaury są przyjazne.",
            visualStyle = ScenarioStyle.DEFAULT,
            isForKids = true,
            iconRes = R.drawable.ic_dino
        ),
        Scenario(
            id = "princess_kingdom",
            title = "Królestwo Księżniczek",
            description = "Pomóż królewnie odnaleźć zaginioną koronę i przygotuj bal w magicznym zamku.",
            themeColor = Color(0xFFE91E63),
            secondaryColor = Color(0xFFF06292),
            basePrompt = "PRZYGODA DLA DZIECI: Magiczne królestwo, wróżki i zamki. Język bajkowy, bardzo bezpieczny i kolorowy.",
            visualStyle = ScenarioStyle.FANTASY,
            isPremium = true,
            isForKids = true,
            iconRes = R.drawable.ic_crown
        ),
        Scenario(
            id = "sea_adventure",
            title = "Podwodny Świat",
            description = "Zanurkuj z delfinami i pomóż małej rybce odnaleźć drogę do domu w rafie koralowej.",
            themeColor = Color(0xFF0288D1),
            secondaryColor = Color(0xFF4FC3F7),
            basePrompt = "PRZYGODA DLA DZIECI: Podwodny świat, rybki i wieloryby. Edukacja o oceanie, zero zagrożeń, radosny klimat.",
            visualStyle = ScenarioStyle.PIRATES,
            isPremium = true,
            isForKids = true,
            iconRes = R.drawable.ic_anchor
        ),
        Scenario(
            id = "cat_land",
            title = "Kocia Kraina",
            description = "Witaj w krainie, gdzie rządzą kotki! Pomóż puszystym przyjaciołom zbudować najwyższy drapak.",
            themeColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFFFFB74D),
            basePrompt = "PRZYGODA DLA DZIECI: Świat pełen kotków. Ciepły, zabawny język, skupienie na opiece nad zwierzętami i zabawie.",
            visualStyle = ScenarioStyle.DEFAULT,
            isPremium = true,
            isForKids = true,
            iconRes = R.drawable.ic_paw
        )
    )
}
