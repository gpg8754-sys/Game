package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PlayerProfile::class, OwnedSkin::class, MarketListing::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "standoff_clicker_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate when database is first created
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(database.gameDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: GameDao) {
            // Initial Profile
            if (dao.getPlayerProfileDirect() == null) {
                dao.insertOrUpdateProfile(
                    PlayerProfile(
                        id = 1,
                        name = "Player1337",
                        level = 1,
                        xp = 0,
                        gold = 100.0, // Give some starting gold to make it fun!
                        silver = 500.0, // starting silver
                        clickUpgradeLevel = 1,
                        autoClickUpgradeLevel = 0
                    )
                )
            }

            // Create some random simulated listings on the market
            val initialSimulatedListings = listOf(
                MarketListing(skinId = "p350_neon", sellerName = "AWM_King", price = 320.0, isSimulated = true),
                MarketListing(skinId = "akr_necromancer", sellerName = "Standoff_Pro_3", price = 1180.0, isSimulated = true),
                MarketListing(skinId = "fs_venom", sellerName = "SashaGamer", price = 780.0, isSimulated = true),
                MarketListing(skinId = "g22_nest", sellerName = "SilverMaster", price = 9.8, isSimulated = true),
                MarketListing(skinId = "ump45_cyber", sellerName = "Trapper", price = 14.5, isSimulated = true),
                MarketListing(skinId = "usp_genesis", sellerName = "DeagleFan", price = 48.0, isSimulated = true),
                MarketListing(skinId = "m9_blue_glass", sellerName = "Collector99", price = 4450.0, isSimulated = true),
                MarketListing(skinId = "m4_samurai", sellerName = "Shadow_Ninja", price = 1050.0, isSimulated = true)
            )

            for (listing in initialSimulatedListings) {
                dao.insertMarketListing(listing)
            }
        }
    }
}
