package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // PLAYER PROFILE
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfileFlow(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfileDirect(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfile)

    // OWNED SKINS
    @Query("SELECT * FROM owned_skin")
    fun getOwnedSkinsFlow(): Flow<List<OwnedSkin>>

    @Query("SELECT * FROM owned_skin WHERE id = :id LIMIT 1")
    suspend fun getOwnedSkinById(id: Long): OwnedSkin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnedSkin(ownedSkin: OwnedSkin): Long

    @Delete
    suspend fun deleteOwnedSkin(ownedSkin: OwnedSkin)

    // MARKET LISTINGS
    @Query("SELECT * FROM market_listing")
    fun getMarketListingsFlow(): Flow<List<MarketListing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketListing(listing: MarketListing): Long

    @Query("DELETE FROM market_listing WHERE id = :id")
    suspend fun deleteMarketListingById(id: Long)

    @Query("DELETE FROM market_listing WHERE ownedSkinId = :ownedSkinId")
    suspend fun deleteMarketListingByOwnedSkinId(ownedSkinId: Long)
}
