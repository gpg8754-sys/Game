package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1, // Only 1 row ever exists
    val name: String = "Player1337",
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Double = 10.0,
    val silver: Double = 100.0,
    val clickUpgradeLevel: Int = 1,
    val autoClickUpgradeLevel: Int = 0,
    val totalClicks: Int = 0,
    val totalCasesOpened: Int = 0,
    val totalMiniGamesPlayed: Int = 0,
    val highestReactionTimeMs: Int = 9999, // default slow value
    val highestTargetScore: Int = 0,
    val highestTapRushScore: Int = 0
)

@Entity(tableName = "owned_skin")
data class OwnedSkin(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skinId: String,
    val isStattrak: Boolean = false,
    val isListedOnMarket: Boolean = false,
    val marketPrice: Double = 0.0 // Price in Gold if listed
)

@Entity(tableName = "market_listing")
data class MarketListing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skinId: String,
    val sellerName: String,
    val price: Double, // Price in Gold
    val isSimulated: Boolean = false,
    val ownedSkinId: Long? = null // points to OwnedSkin.id if user listed it
)
