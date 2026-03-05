package pl.pointblank.geekadventure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val role: String, // "user" lub "model"
    val content: String,
    val gameStateJson: String? = null, // Przechowuje stan HP, złota i ekwipunku w formacie JSON
    val timestamp: Long = System.currentTimeMillis()
)
