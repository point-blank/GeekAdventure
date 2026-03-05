package pl.pointblank.geekadventure.data

import androidx.compose.ui.graphics.Color
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle

object ScenarioRepository {
    val scenarios = listOf(
        Scenario(
            id = "pirates_adventure",
            title = "Klątwa Czarnej Perły",
            description = "Wyrusz w rejs jako pirat, szukaj skarbów i mierz się z morskimi potworami.",
            themeColor = Color(0xFF008080),
            secondaryColor = Color(0xFFF4A460),
            basePrompt = "Świat piratów: statki, rum, papugi, ukryte wyspy i klątwy Azteków. Klimat karaibski.",
            visualStyle = ScenarioStyle.PIRATES
        ),
        Scenario(
            id = "cyberpunk_neon",
            title = "Neon City 2099",
            description = "Mroczny świat przyszłości, wszczepy, hakerzy i wielkie korporacje.",
            themeColor = Color(0xFF00FFFF),
            secondaryColor = Color(0xFFFF00FF),
            basePrompt = "Świat Cyberpunk: neony, deszcz, brudne zaułki, hakerzy i wszczepy. Styl mroczny sci-fi.",
            visualStyle = ScenarioStyle.CYBERPUNK
        ),
        Scenario(
            id = "fantasy_lord",
            title = "Powiernik Pierścienia",
            description = "Klasyczne High Fantasy. Smoki, elfy, krasnoludy i epicka walka dobra ze złem.",
            themeColor = Color(0xFFD4AF37),
            secondaryColor = Color(0xFF006400),
            basePrompt = "Świat High Fantasy: magia, starożytne artefakty, zamki i niebezpieczne bestie.",
            visualStyle = ScenarioStyle.FANTASY
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
        Scenario(
            id = "wild_west",
            title = "Słońce nad Deadwood",
            description = "Dziki Zachód, rewolwerowcy, napady na banki i poszukiwacze złota.",
            themeColor = Color(0xFFA0522D),
            secondaryColor = Color(0xFFFFE4B5),
            basePrompt = "Świat Westernu: Dziki Zachód, saloony, pojedynki w południe, bandyci i złoto.",
            visualStyle = ScenarioStyle.WESTERN
        )
    )
}
