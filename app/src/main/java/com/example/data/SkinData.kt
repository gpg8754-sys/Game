package com.example.data

import androidx.compose.ui.graphics.Color

enum class Rarity(val displayName: String, val color: Color, val priority: Int) {
    COMMON("Common", Color(0xFF9E9E9E), 1),
    UNCOMMON("Uncommon", Color(0xFF4CAF50), 2),
    RARE("Rare", Color(0xFF2196F3), 3),
    EPIC("Epic", Color(0xFF9C27B0), 4),
    LEGENDARY("Legendary", Color(0xFFE91E63), 5),
    ARCANA("Arcana", Color(0xFFFF9800), 6)
}

data class Skin(
    val id: String,
    val name: String,
    val weapon: String,
    val rarity: Rarity,
    val baseValueGold: Double, // Value in Gold
    val baseValueSilver: Double, // Value in Silver
    val isKnife: Boolean = false
)

object SkinData {
    val skins = listOf(
        // ARCANAS (Knives & High Tier)
        Skin("m9_blue_glass", "Blue Glass", "M9 Bayonet", Rarity.ARCANA, 4500.0, 90000.0, isKnife = true),
        Skin("karambit_gold", "Gold", "Karambit", Rarity.ARCANA, 5000.0, 100000.0, isKnife = true),
        Skin("butterfly_dragon", "Dragon Glass", "Butterfly", Rarity.ARCANA, 4200.0, 84000.0, isKnife = true),
        Skin("jcommando_floral", "Floral", "JCommando", Rarity.ARCANA, 3800.0, 76000.0, isKnife = true),
        Skin("fang_obsidian", "Obsidian", "Fang", Rarity.ARCANA, 3200.0, 64000.0, isKnife = true),
        Skin("awm_genesis", "Genesis", "AWM", Rarity.ARCANA, 2500.0, 50000.0),

        // LEGENDARIES (Red)
        Skin("akr_necromancer", "Necromancer", "AKR", Rarity.LEGENDARY, 1200.0, 24000.0),
        Skin("m4_samurai", "Samurai", "M4", Rarity.LEGENDARY, 1100.0, 22000.0),
        Skin("fs_venom", "Venom", "F/S", Rarity.LEGENDARY, 800.0, 16000.0),
        Skin("awm_sport", "Sport", "AWM", Rarity.LEGENDARY, 950.0, 19000.0),

        // EPICS (Purple)
        Skin("p350_neon", "Neon", "P350", Rarity.EPIC, 300.0, 6000.0),
        Skin("m16_winged", "Winged", "M16", Rarity.EPIC, 250.0, 5000.0),
        Skin("mp7_graffiti", "Graffiti", "MP7", Rarity.EPIC, 200.0, 4000.0),
        Skin("g22_scale", "Scale", "G22", Rarity.EPIC, 150.0, 3000.0),

        // RARES (Blue)
        Skin("usp_genesis", "Genesis", "USP", Rarity.RARE, 50.0, 1000.0),
        Skin("fabm_beast", "Beast", "FABM", Rarity.RARE, 40.0, 800.0),
        Skin("akr_tiger", "Tiger", "AKR", Rarity.RARE, 60.0, 1200.0),
        Skin("m40_grip", "Grip", "M40", Rarity.RARE, 35.0, 700.0),

        // UNCOMMONS (Light Blue)
        Skin("g22_nest", "Nest", "G22", Rarity.UNCOMMON, 10.0, 200.0),
        Skin("ap_sector", "Sector", "AP", Rarity.UNCOMMON, 12.0, 240.0),
        Skin("ump45_cyber", "Cyberpunk", "UMP45", Rarity.UNCOMMON, 15.0, 300.0),

        // COMMONS (Grey)
        Skin("sm1014_quake", "Quake", "SM1014", Rarity.COMMON, 2.0, 40.0),
        Skin("mp5_zone", "Zone", "MP5", Rarity.COMMON, 3.0, 60.0),
        Skin("p350_desert", "Desert", "P350", Rarity.COMMON, 1.5, 30.0)
    )

    val skinsMap = skins.associateBy { it.id }

    fun getSkinById(id: String): Skin? {
        return skinsMap[id]
    }
}

data class Case(
    val id: String,
    val name: String,
    val costGold: Double,
    val costSilver: Double,
    val possibleSkinIds: List<String>,
    val imageUrl: String = "" // represented geometrically in Compose
)

object CaseData {
    val cases = listOf(
        Case(
            id = "origin_case",
            name = "Origin Case",
            costGold = 0.0,
            costSilver = 100.0,
            possibleSkinIds = listOf(
                "sm1014_quake", "mp5_zone", "p350_desert", // commons
                "g22_nest", "ap_sector", // uncommons
                "usp_genesis", "fabm_beast", // rares
                "p350_neon", // epic
                "karambit_gold" // super rare knife chance!
            )
        ),
        Case(
            id = "furious_case",
            name = "Furious Case",
            costGold = 0.0,
            costSilver = 250.0,
            possibleSkinIds = listOf(
                "g22_nest", "ap_sector", "ump45_cyber", // uncommons
                "usp_genesis", "fabm_beast", "akr_tiger", "m40_grip", // rares
                "p350_neon", "m16_winged", "mp7_graffiti", // epics
                "fs_venom", "m4_samurai", // legendaries
                "butterfly_dragon", "m9_blue_glass" // knives!
            )
        ),
        Case(
            id = "sharp_case",
            name = "Sharp Case",
            costGold = 10.0,
            costSilver = 0.0,
            possibleSkinIds = listOf(
                "usp_genesis", "fabm_beast", "akr_tiger", // rares
                "p350_neon", "m16_winged", "mp7_graffiti", "g22_scale", // epics
                "fs_venom", "m4_samurai", "awm_sport", "akr_necromancer", // legendaries
                "butterfly_dragon", "m9_blue_glass", "karambit_gold", "jcommando_floral", "fang_obsidian" // arcana knives!
            )
        ),
        Case(
            id = "empire_case",
            name = "Empire Case",
            costGold = 35.0,
            costSilver = 0.0,
            possibleSkinIds = listOf(
                "p350_neon", "m16_winged", "mp7_graffiti", "g22_scale", // epics
                "fs_venom", "m4_samurai", "awm_sport", "akr_necromancer", "awm_genesis", // legendaries
                "butterfly_dragon", "m9_blue_glass", "karambit_gold", "jcommando_floral", "fang_obsidian" // arcana knives!
            )
        )
    )

    val casesMap = cases.associateBy { it.id }

    fun getCaseById(id: String): Case? {
        return casesMap[id]
    }
}
