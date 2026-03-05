package pl.pointblank.geekadventure.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE scenarioId = :scenarioId ORDER BY timestamp ASC")
    suspend fun getMessagesForScenario(scenarioId: String): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages WHERE scenarioId = :scenarioId")
    suspend fun deleteMessagesForScenario(scenarioId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE scenarioId = :scenarioId")
    suspend fun getMessageCount(scenarioId: String): Int

    // Lore Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLore(lore: LoreEntry)

    @Query("SELECT * FROM lore_entries WHERE scenarioId = :scenarioId ORDER BY timestamp ASC")
    suspend fun getLoreForScenario(scenarioId: String): List<LoreEntry>

    @Query("DELETE FROM lore_entries WHERE scenarioId = :scenarioId")
    suspend fun deleteLoreForScenario(scenarioId: String)

    // USER STATS (ENERGY, PREMIUM, CRYSTALS)
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    // TIME REVERSAL
    @Query("DELETE FROM chat_messages WHERE id IN (SELECT id FROM chat_messages WHERE scenarioId = :scenarioId ORDER BY timestamp DESC LIMIT 2)")
    suspend fun deleteLastTwoMessages(scenarioId: String)
}
