package pl.pointblank.geekadventure.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlin.random.Random

data class DiceState(
    val isRolling: Boolean = false,
    val lastResult: Int? = null,
    val targetDifficulty: Int? = null,
    val type: Int = 20 // domyślnie d20
)

class DiceViewModel : ViewModel() {
    private val _diceState = MutableStateFlow(DiceState())
    val diceState = _diceState.asStateFlow()

    suspend fun rollDice(target: Int? = null, diceType: Int = 20): Int {
        _diceState.value = DiceState(isRolling = true, targetDifficulty = target, type = diceType)
        
        // Symulacja trwania rzutu (animacja)
        repeat(10) {
            _diceState.value = _diceState.value.copy(lastResult = Random.nextInt(1, diceType + 1))
            delay(100)
        }
        
        val finalResult = Random.nextInt(1, diceType + 1)
        _diceState.value = _diceState.value.copy(isRolling = false, lastResult = finalResult)
        
        return finalResult
    }

    fun reset() {
        _diceState.value = DiceState()
    }
}
