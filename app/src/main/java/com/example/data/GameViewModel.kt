package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    private val repository = GameRepository(db.gameDao())

    // Backing states
    val playerProfile: StateFlow<PlayerProfile?> = repository.playerProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ownedSkins: StateFlow<List<OwnedSkinWithMetadata>> = repository.ownedSkinsFlow
        .map { list ->
            list.mapNotNull { owned ->
                val skin = SkinData.getSkinById(owned.skinId)
                if (skin != null) OwnedSkinWithMetadata(owned, skin) else null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketListings: StateFlow<List<MarketListingWithMetadata>> = repository.marketListingsFlow
        .map { list ->
            list.mapNotNull { listing ->
                val skin = SkinData.getSkinById(listing.skinId)
                if (skin != null) MarketListingWithMetadata(listing, skin) else null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    var currentTab = MutableStateFlow(2) // 0: Inventory, 1: Market, 2: Clicker, 3: Games, 4: Stats/Settings
        private set

    // Alert & Message states
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage = _uiMessage.asStateFlow()

    // Case Opening Roll state
    private val _caseOpeningResult = MutableStateFlow<CaseOpeningRollResult?>(null)
    val caseOpeningResult = _caseOpeningResult.asStateFlow()

    init {
        // Start passive auto-click ticker & market dynamic updates
        viewModelScope.launch(Dispatchers.IO) {
            var marketUpdateCounter = 0
            while (true) {
                delay(1000)
                repository.applyAutoClickTick()
                
                // Every 10 seconds, update market listings
                marketUpdateCounter++
                if (marketUpdateCounter >= 10) {
                    repository.updateSimulatedMarket()
                    marketUpdateCounter = 0
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        currentTab.value = tabIndex
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(message: String) {
        _uiMessage.value = message
    }

    // GAME ACTIONS
    fun performClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.click()
            if (result.leveledUp) {
                _uiMessage.value = result.levelUpMessage
            }
        }
    }

    fun buyClickUpgrade() {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buyClickUpgrade()
            if (success) {
                _uiMessage.value = "Улучшение клика куплено!"
            } else {
                _uiMessage.value = "Недостаточно серебра!"
            }
        }
    }

    fun buyAutoClickUpgrade() {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.buyAutoClickUpgrade()
            if (success) {
                _uiMessage.value = "Улучшение автокликера куплено!"
            } else {
                _uiMessage.value = "Недостаточно золота!"
            }
        }
    }

    // CASE OPENING
    fun openCase(caseId: String) {
        if (_caseOpeningResult.value?.isRolling == true) return

        viewModelScope.launch(Dispatchers.IO) {
            _caseOpeningResult.value = CaseOpeningRollResult(isRolling = true)
            
            val result = repository.openCase(caseId)
            if (result is OpenCaseResult.Success) {
                // Let's simulate a case opening scroll effect in UI
                // Wait 2.5 seconds before showing the result for maximum suspense!
                delay(2500)
                _caseOpeningResult.value = CaseOpeningRollResult(
                    isRolling = false,
                    pulledSkin = result.skin,
                    isStattrak = result.isStattrak,
                    ownedSkinId = result.ownedSkinId
                )
            } else if (result is OpenCaseResult.Error) {
                _caseOpeningResult.value = null
                _uiMessage.value = result.message
            }
        }
    }

    fun closeCaseOpeningDialog() {
        _caseOpeningResult.value = null
    }

    // QUICK SELL
    fun quickSellSkin(ownedSkinId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.quickSellSkin(ownedSkinId)
            if (success) {
                _uiMessage.value = "Скин продан!"
            } else {
                _uiMessage.value = "Не удалось продать скин"
            }
        }
    }

    // MARKET ACTIONS
    fun sellOnMarket(ownedSkinId: Long, priceGold: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.listSkinOnMarket(ownedSkinId, priceGold)
            if (success) {
                _uiMessage.value = "Скин выставлен на рынок за ${priceGold} G!"
            } else {
                _uiMessage.value = "Ошибка при выставлении скина на рынок"
            }
        }
    }

    fun buyMarketListing(listingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.buyMarketListing(listingId)
            if (result is BuyListingResult.Success) {
                _uiMessage.value = "Вы купили ${result.skin.weapon} | ${result.skin.name}!"
            } else if (result is BuyListingResult.Error) {
                _uiMessage.value = result.message
            }
        }
    }

    fun cancelMarketListing(listingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.cancelMarketListing(listingId)
            if (success) {
                _uiMessage.value = "Объявление отменено. Предмет возвращен в инвентарь"
            } else {
                _uiMessage.value = "Не удалось отменить объявление"
            }
        }
    }

    // MINI-GAMES REWARDS
    fun rewardMiniGame(score: Int, gameType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Determine rewards based on score & difficulty
            var gold = 0.0
            var silver = 0.0
            val cases = mutableListOf<String>()

            when (gameType) {
                "target_shooting" -> {
                    // Targets Shooting (Тир)
                    repository.submitTargetScore(score)
                    silver = score * 8.0
                    gold = if (score >= 20) (score - 19) * 1.5 else 0.0
                    if (score >= 30) {
                        cases.add("origin_case")
                    }
                    if (score >= 45) {
                        cases.add("furious_case")
                    }
                }
                "reaction_check" -> {
                    // Reaction Time (smaller ms is better)
                    // score represents reaction time in ms
                    repository.submitReactionTime(score)
                    if (score < 150) {
                        gold = 40.0
                        silver = 250.0
                        cases.add("sharp_case")
                    } else if (score < 200) {
                        gold = 20.0
                        silver = 150.0
                        cases.add("furious_case")
                    } else if (score < 280) {
                        gold = 8.0
                        silver = 100.0
                    } else if (score < 400) {
                        silver = 50.0
                    } else {
                        silver = 10.0
                    }
                }
                "tap_rush" -> {
                    // Tap Rush (Быстрый Клик / Скорострельность)
                    repository.submitTapRushScore(score)
                    silver = score * 3.5
                    if (score >= 60) {
                        gold = 5.0
                    }
                    if (score >= 90) {
                        gold += 15.0
                        cases.add("empire_case")
                    }
                }
                "element_puzzle" -> {
                    // Elements Match Puzzle (score represents completion time in seconds)
                    silver = 150.0 // Standard reward for puzzle solving
                    if (score < 15) {
                        gold = 40.0
                        cases.add("sharp_case")
                    } else if (score < 25) {
                        gold = 20.0
                        cases.add("furious_case")
                    } else if (score < 40) {
                        gold = 5.0
                    }
                }
            }

            val rewardMessage = repository.rewardMiniGame(gold, silver, cases)
            _uiMessage.value = "Игра завершена! Счёт: $score.\n$rewardMessage"
        }
    }

    // SETTINGS ACTIONS
    fun resetProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetProgress()
            _uiMessage.value = "Прогресс игры полностью сброшен!"
        }
    }

    fun changePlayerName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.renameProfile(newName.trim())
            _uiMessage.value = "Никнейм изменен на ${newName.trim()}"
        }
    }
}

// Wrapper data models
data class OwnedSkinWithMetadata(
    val ownedSkin: OwnedSkin,
    val skin: Skin
)

data class MarketListingWithMetadata(
    val listing: MarketListing,
    val skin: Skin
)

data class CaseOpeningRollResult(
    val isRolling: Boolean,
    val pulledSkin: Skin? = null,
    val isStattrak: Boolean = false,
    val ownedSkinId: Long = 0
)
