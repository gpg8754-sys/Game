package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GameRepository(private val dao: GameDao) {

    val playerProfileFlow: Flow<PlayerProfile?> = dao.getPlayerProfileFlow()
    val ownedSkinsFlow: Flow<List<OwnedSkin>> = dao.getOwnedSkinsFlow()
    val marketListingsFlow: Flow<List<MarketListing>> = dao.getMarketListingsFlow()

    // Retrieve profile directly
    suspend fun getProfileDirect(): PlayerProfile {
        return dao.getPlayerProfileDirect() ?: createDefaultProfile()
    }

    private suspend fun createDefaultProfile(): PlayerProfile {
        val default = PlayerProfile()
        dao.insertOrUpdateProfile(default)
        return default
    }

    // CLICK MECHANIC
    suspend fun click(): ClickResult {
        val profile = getProfileDirect()
        val clickMul = profile.clickUpgradeLevel
        
        // Base reward: upgrade level * 1.5 Silver
        val silverReward = clickMul * 1.5
        val xpReward = 1 // 1 XP per click
        
        var newSilver = profile.silver + silverReward
        var newXp = profile.xp + xpReward
        var newLevel = profile.level
        var leveledUp = false
        var levelUpRewardMessage = ""

        // Level up formula: level * 100 XP
        val xpNeeded = newLevel * 100
        if (newXp >= xpNeeded) {
            newXp -= xpNeeded
            newLevel += 1
            leveledUp = true
            
            // Level up reward: a random Case!
            val randomCase = CaseData.cases.random()
            levelUpRewardMessage = "Поздравляем! Вы достигли уровня $newLevel и получили ${randomCase.name}!"
            
            // Give reward: we will handle adding it by either granting silver/gold directly,
            // or we can gift them enough Silver/Gold to open it, or directly add a case.
            // Since cases are purchased, let's gift 15 Gold and 200 Silver as a level up bonus!
            newSilver += 200.0
            profile.let {
                dao.insertOrUpdateProfile(profile.copy(gold = profile.gold + 15.0))
            }
        }

        val updatedProfile = profile.copy(
            silver = newSilver,
            xp = newXp,
            level = newLevel,
            totalClicks = profile.totalClicks + 1
        )
        dao.insertOrUpdateProfile(updatedProfile)

        return ClickResult(
            silverEarned = silverReward,
            xpEarned = xpReward,
            leveledUp = leveledUp,
            levelUpMessage = levelUpRewardMessage,
            newLevel = newLevel
        )
    }

    // PASSIVE INCOME (Auto-clicker tick)
    suspend fun applyAutoClickTick() {
        val profile = dao.getPlayerProfileDirect() ?: return
        if (profile.autoClickUpgradeLevel <= 0) return

        // Each level generates 0.5 Silver per tick (run every 1 sec)
        val passiveReward = profile.autoClickUpgradeLevel * 0.5
        val updatedProfile = profile.copy(
            silver = profile.silver + passiveReward
        )
        dao.insertOrUpdateProfile(updatedProfile)
    }

    // BUY UPGRADES
    suspend fun buyClickUpgrade(): Boolean {
        val profile = getProfileDirect()
        // Formula for cost of Clicker Multiplier (Silver)
        val cost = 100.0 * Math.pow(1.6, (profile.clickUpgradeLevel - 1).toDouble())
        
        if (profile.silver >= cost) {
            val updated = profile.copy(
                silver = profile.silver - cost,
                clickUpgradeLevel = profile.clickUpgradeLevel + 1
            )
            dao.insertOrUpdateProfile(updated)
            return true
        }
        return false
    }

    suspend fun buyAutoClickUpgrade(): Boolean {
        val profile = getProfileDirect()
        // Formula for cost of Auto Clicker (Gold)
        val cost = 10.0 * Math.pow(1.8, profile.autoClickUpgradeLevel.toDouble())
        
        if (profile.gold >= cost) {
            val updated = profile.copy(
                gold = profile.gold - cost,
                autoClickUpgradeLevel = profile.autoClickUpgradeLevel + 1
            )
            dao.insertOrUpdateProfile(updated)
            return true
        }
        return false
    }

    // OPEN CASE
    suspend fun openCase(caseId: String): OpenCaseResult {
        val case = CaseData.getCaseById(caseId) ?: return OpenCaseResult.Error("Кейс не найден")
        val profile = getProfileDirect()

        // Check prices
        if (case.costGold > 0 && profile.gold < case.costGold) {
            return OpenCaseResult.Error("Недостаточно золота (Требуется ${case.costGold} G)")
        }
        if (case.costSilver > 0 && profile.silver < case.costSilver) {
            return OpenCaseResult.Error("Недостаточно серебра (Требуется ${case.costSilver} S)")
        }

        // Deduct cost
        val updatedProfile = profile.copy(
            gold = profile.gold - case.costGold,
            silver = profile.silver - case.costSilver,
            totalCasesOpened = profile.totalCasesOpened + 1,
            xp = profile.xp + 15 // opening cases gives 15 XP
        )
        dao.insertOrUpdateProfile(updatedProfile)

        // Select random skin based on Rarity weight
        val possibleSkins = case.possibleSkinIds.mapNotNull { SkinData.getSkinById(it) }
        if (possibleSkins.isEmpty()) {
            return OpenCaseResult.Error("В кейсе нет скинов")
        }

        val rolledSkin = rollSkinByWeight(possibleSkins)
        
        // 10% chance to be StatTrak (adds extra value)
        val isStattrak = Random.nextFloat() < 0.10f

        // Insert skin to inventory
        val ownedSkinId = dao.insertOwnedSkin(
            OwnedSkin(
                skinId = rolledSkin.id,
                isStattrak = isStattrak,
                isListedOnMarket = false
            )
        )

        return OpenCaseResult.Success(rolledSkin, isStattrak, ownedSkinId)
    }

    private fun rollSkinByWeight(possibleSkins: List<Skin>): Skin {
        // We will assign weights based on Rarity
        // Common: 600, Uncommon: 300, Rare: 100, Epic: 40, Legendary: 15, Arcana: 3
        val skinsWithWeights = possibleSkins.map { skin ->
            val weight = when (skin.rarity) {
                Rarity.COMMON -> 600
                Rarity.UNCOMMON -> 300
                Rarity.RARE -> 120
                Rarity.EPIC -> 40
                Rarity.LEGENDARY -> 12
                Rarity.ARCANA -> 3
            }
            Pair(skin, weight)
        }

        val totalWeight = skinsWithWeights.sumOf { it.second }
        var randomWeight = Random.nextInt(totalWeight)

        for (pair in skinsWithWeights) {
            randomWeight -= pair.second
            if (randomWeight < 0) {
                return pair.first
            }
        }
        return possibleSkins.last()
    }

    // QUICK SELL SKIN
    suspend fun quickSellSkin(ownedSkinId: Long): Boolean {
        val ownedSkin = dao.getOwnedSkinById(ownedSkinId) ?: return false
        val skin = SkinData.getSkinById(ownedSkin.skinId) ?: return false

        // Base quick sell values
        // Common/Uncommon/Rare sell for Silver
        // Epic/Legendary/Arcana sell for Gold
        val multiplier = if (ownedSkin.isStattrak) 1.5 else 1.0
        val isSilverSale = skin.rarity == Rarity.COMMON || skin.rarity == Rarity.UNCOMMON || skin.rarity == Rarity.RARE
        
        val value = if (isSilverSale) {
            skin.baseValueSilver * 0.5 * multiplier // sell back for 50% of value
        } else {
            skin.baseValueGold * 0.4 * multiplier // sell back for 40% of value
        }

        // Delete skin
        dao.deleteOwnedSkin(ownedSkin)
        // If listed on market, clean up listing
        dao.deleteMarketListingByOwnedSkinId(ownedSkinId)

        // Add funds to player
        val profile = getProfileDirect()
        val updatedProfile = if (isSilverSale) {
            profile.copy(silver = profile.silver + value)
        } else {
            profile.copy(gold = profile.gold + value)
        }
        dao.insertOrUpdateProfile(updatedProfile)
        return true
    }

    // MARKET LISTING
    // List skin on the market for Gold
    suspend fun listSkinOnMarket(ownedSkinId: Long, priceGold: Double): Boolean {
        if (priceGold <= 0) return false
        val ownedSkin = dao.getOwnedSkinById(ownedSkinId) ?: return false
        if (ownedSkin.isListedOnMarket) return false

        // Update skin to listed state
        val updatedSkin = ownedSkin.copy(isListedOnMarket = true, marketPrice = priceGold)
        dao.insertOwnedSkin(updatedSkin)

        // Add listing
        dao.insertMarketListing(
            MarketListing(
                skinId = ownedSkin.skinId,
                sellerName = "Вы (Игрок)",
                price = priceGold,
                isSimulated = false,
                ownedSkinId = ownedSkinId
            )
        )
        return true
    }

    // Buy a listing from market
    suspend fun buyMarketListing(listingId: Long): BuyListingResult {
        // Get listing
        val listings = dao.getMarketListingsFlow().first()
        val listing = listings.find { it.id == listingId } ?: return BuyListingResult.Error("Объявление не найдено")

        val profile = getProfileDirect()
        if (profile.gold < listing.price) {
            return BuyListingResult.Error("Недостаточно золота. Требуется ${listing.price} G")
        }

        // Buy item
        val skin = SkinData.getSkinById(listing.skinId) ?: return BuyListingResult.Error("Скин не найден в базе")

        // Subtract gold from profile
        val updatedProfile = profile.copy(gold = profile.gold - listing.price)
        dao.insertOrUpdateProfile(updatedProfile)

        if (listing.isSimulated) {
            // Simulated seller: create new owned skin for user
            dao.insertOwnedSkin(
                OwnedSkin(
                    skinId = listing.skinId,
                    isStattrak = false,
                    isListedOnMarket = false
                )
            )
        } else {
            // User listed it? They are buying their own? Let's check:
            if (listing.ownedSkinId != null) {
                // If it is their own listing, they get it back
                val owned = dao.getOwnedSkinById(listing.ownedSkinId)
                if (owned != null) {
                    dao.insertOwnedSkin(owned.copy(isListedOnMarket = false, marketPrice = 0.0))
                }
            }
        }

        // Delete listing
        dao.deleteMarketListingById(listingId)
        return BuyListingResult.Success(skin)
    }

    // Cancel a listing
    suspend fun cancelMarketListing(listingId: Long): Boolean {
        val listings = dao.getMarketListingsFlow().first()
        val listing = listings.find { it.id == listingId } ?: return false

        if (listing.ownedSkinId != null) {
            val ownedSkin = dao.getOwnedSkinById(listing.ownedSkinId)
            if (ownedSkin != null) {
                // Return to inventory
                dao.insertOwnedSkin(ownedSkin.copy(isListedOnMarket = false, marketPrice = 0.0))
            }
        }

        dao.deleteMarketListingById(listingId)
        return true
    }

    // SIMULATED MARKET EVENTS
    // Periodically buys player's skins, list random items, or remove stale items!
    suspend fun updateSimulatedMarket() {
        val listings = dao.getMarketListingsFlow().first()
        
        // 1. Chance to buy User listed skins if priced fairly!
        val userListings = listings.filter { !it.isSimulated && it.ownedSkinId != null }
        if (userListings.isNotEmpty() && Random.nextFloat() < 0.25f) {
            val randomUserListing = userListings.random()
            val skin = SkinData.getSkinById(randomUserListing.skinId)
            if (skin != null) {
                // Check if price is reasonable (e.g., less than 3x base value)
                val baseVal = skin.baseValueGold
                val listingPrice = randomUserListing.price
                val purchaseProbability = when {
                    listingPrice <= baseVal -> 0.90
                    listingPrice <= baseVal * 1.5 -> 0.60
                    listingPrice <= baseVal * 2.5 -> 0.25
                    else -> 0.05
                }

                if (Random.nextDouble() < purchaseProbability) {
                    // System buys the user's skin!
                    // Add gold to player profile (with 20% commission fee!)
                    val commission = 0.20
                    val moneyEarned = listingPrice * (1.0 - commission)
                    val profile = getProfileDirect()
                    dao.insertOrUpdateProfile(profile.copy(gold = profile.gold + moneyEarned))

                    // Delete listing and delete owned skin (since it's sold)
                    dao.deleteMarketListingById(randomUserListing.id)
                    randomUserListing.ownedSkinId?.let { skinId ->
                        val owned = dao.getOwnedSkinById(skinId)
                        if (owned != null) dao.deleteOwnedSkin(owned)
                    }
                }
            }
        }

        // 2. Chance to add new simulated listings (up to max 12 listings)
        if (listings.size < 15 && Random.nextFloat() < 0.40f) {
            val randomSkin = SkinData.skins.random()
            // Random price around base value
            val variance = Random.nextDouble(0.85, 1.45)
            val price = Math.round(randomSkin.baseValueGold * variance * 10.0) / 10.0
            
            val nicknames = listOf(
                "Gamer_2009", "StandoffKiller", "M9_Lover", "DragonGlass_PRO", 
                "KnifeCollector", "SniperGod", "RarityHunter", "Sasha_Tornado"
            )
            
            dao.insertMarketListing(
                MarketListing(
                    skinId = randomSkin.id,
                    sellerName = nicknames.random(),
                    price = if (price <= 0) 1.0 else price,
                    isSimulated = true
                )
            )
        }

        // 3. Chance to remove a simulated listing (stale listing expired/bought)
        val simulatedListings = listings.filter { it.isSimulated }
        if (simulatedListings.size > 5 && Random.nextFloat() < 0.20f) {
            val toRemove = simulatedListings.random()
            dao.deleteMarketListingById(toRemove.id)
        }
    }

    // REWARD FOR MINI GAMES
    suspend fun rewardMiniGame(goldReward: Double, silverReward: Double, casesToReward: List<String>): String {
        val profile = getProfileDirect()
        var updatedProfile = profile.copy(
            gold = profile.gold + goldReward,
            silver = profile.silver + silverReward,
            totalMiniGamesPlayed = profile.totalMiniGamesPlayed + 1,
            xp = profile.xp + 30 // playing minigame gives 30 XP
        )
        
        dao.insertOrUpdateProfile(updatedProfile)

        // Award cases directly to user's inventory by granting them the corresponding skin inside the case or silver to purchase!
        // Standoff has cases that you own. In our system, let's keep cases simple:
        // When we reward cases, we can immediately open it and show what skin they got! Or we can gift them Silver/Gold to buy that case.
        // Let's reward cases by giving them the exact amount of Silver/Gold to buy it, so they can open it themselves! That is clean and fun.
        var message = "Награда: +${String.format("%.1f", silverReward)} S, +${String.format("%.1f", goldReward)} G!"
        
        for (caseId in casesToReward) {
            val case = CaseData.getCaseById(caseId)
            if (case != null) {
                if (case.costGold > 0) {
                    updatedProfile = updatedProfile.copy(gold = updatedProfile.gold + case.costGold)
                    message += "\nПолучен кейс: ${case.name} (+${case.costGold} G для открытия!)"
                } else if (case.costSilver > 0) {
                    updatedProfile = updatedProfile.copy(silver = updatedProfile.silver + case.costSilver)
                    message += "\nПолучен кейс: ${case.name} (+${case.costSilver} S для открытия!)"
                }
            }
        }
        
        dao.insertOrUpdateProfile(updatedProfile)
        return message
    }

    // HIGH SCORE SUBMISSIONS
    suspend fun submitReactionTime(timeMs: Int) {
        val profile = getProfileDirect()
        if (timeMs < profile.highestReactionTimeMs) {
            dao.insertOrUpdateProfile(profile.copy(highestReactionTimeMs = timeMs))
        }
    }

    suspend fun submitTargetScore(score: Int) {
        val profile = getProfileDirect()
        if (score > profile.highestTargetScore) {
            dao.insertOrUpdateProfile(profile.copy(highestTargetScore = score))
        }
    }

    suspend fun submitTapRushScore(score: Int) {
        val profile = getProfileDirect()
        if (score > profile.highestTapRushScore) {
            dao.insertOrUpdateProfile(profile.copy(highestTapRushScore = score))
        }
    }

    // RESET PROGRESS
    suspend fun resetProgress() {
        val profile = getProfileDirect()
        dao.insertOrUpdateProfile(
            PlayerProfile(
                id = 1,
                name = profile.name,
                level = 1,
                xp = 0,
                gold = 10.0,
                silver = 100.0,
                clickUpgradeLevel = 1,
                autoClickUpgradeLevel = 0,
                totalClicks = 0,
                totalCasesOpened = 0,
                totalMiniGamesPlayed = 0,
                highestReactionTimeMs = 9999,
                highestTargetScore = 0,
                highestTapRushScore = 0
            )
        )
        // Delete all owned skins
        val ownedSkins = dao.getOwnedSkinsFlow().first()
        for (skin in ownedSkins) {
            dao.deleteOwnedSkin(skin)
        }
        // Delete market listings
        val listings = dao.getMarketListingsFlow().first()
        for (listing in listings) {
            dao.deleteMarketListingById(listing.id)
        }
        // Re-populate market
        GameDatabase.populateInitialData(dao)
    }

    suspend fun renameProfile(newName: String) {
        val profile = getProfileDirect()
        dao.insertOrUpdateProfile(profile.copy(name = newName))
    }
}

// Result containers
data class ClickResult(
    val silverEarned: Double,
    val xpEarned: Int,
    val leveledUp: Boolean,
    val levelUpMessage: String,
    val newLevel: Int
)

sealed class OpenCaseResult {
    data class Success(val skin: Skin, val isStattrak: Boolean, val ownedSkinId: Long) : OpenCaseResult()
    data class Error(val message: String) : OpenCaseResult()
}

sealed class BuyListingResult {
    data class Success(val skin: Skin) : BuyListingResult()
    data class Error(val message: String) : BuyListingResult()
}
