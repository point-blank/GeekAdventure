package pl.pointblank.geekadventure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Zawsze jeden wiersz dla gracza
    val actionPoints: Int = 20,
    val lastRefillTime: Long = System.currentTimeMillis(),
    val chronocrystals: Int = 3, // Startowe kryształy do cofania czasu
    val isPremiumUser: Boolean = false
)
