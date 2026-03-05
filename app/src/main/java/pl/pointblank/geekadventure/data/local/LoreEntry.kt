package pl.pointblank.geekadventure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lore_entries")
data class LoreEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val key: String, // np. "NPC: Karczmarz"
    val description: String, // np. "Barnaba, ma bliznę na oku, lubi złoto"
    val timestamp: Long = System.currentTimeMillis()
)
