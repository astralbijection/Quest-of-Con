package io.github.plenglin.questofcon.game

import io.github.plenglin.questofcon.Assets
import io.github.plenglin.questofcon.Constants
import io.github.plenglin.questofcon.TerrainTextures
import io.github.plenglin.questofcon.game.building.*
import io.github.plenglin.questofcon.game.grid.Biome
import io.github.plenglin.questofcon.game.pawn.*
import io.github.plenglin.util.ObjectRegistry


object GameData {

    val pawns = ObjectRegistry<PawnType>()
    val buildings = ObjectRegistry<BuildingType>()
    val biomes = ObjectRegistry<Biome>()

    val grunt = PawnType("inf-grunt").apply {
        displayName = "grunt"
        cost = 75
        baseHp = 30
        baseAtk = 20
        type = PawnClass.INFANTRY
        texture = { Assets[Assets.grunt] }
    }

    val drill = PawnType("mech-drill").apply {
        displayName = "drill mech"
        cost = 150
        baseHp = 50
        baseAtk = 40
        type = PawnClass.MECH
        texture = { Assets[Assets.drillmech] }
    }

    val beam = PawnType("mech-beam").apply {
        displayName = "beam mech"
        cost = 150
        baseHp = 40
        baseAtk = 30
        maxRange = 2
        type = PawnClass.MECH
        texture = { Assets[Assets.drillmech] }
    }

    val tankdes = PawnType("veh-td").apply {
        displayName = "tank destroyer"
        cost = 300
        baseAtk = 50
        baseHp = 20
        maxRange = 4
        maxAp = 2
        type = PawnClass.VEHICLE
        texture = { Assets[Assets.tankdestroyer] }
    }

    val defender = PawnType("mech-defender").apply {
        displayName = "defender"
        cost = 250
        baseAtk = 30
        baseHp = 100
        maxAp = 2
        type = PawnClass.MECH
        texture = { Assets[Assets.defender] }
    }

    val scout = PawnType("veh-scout").apply {
        displayName = "scout"
        cost = 200
        baseAtk = 20
        baseHp = 30
        maxAp = 5
        maxRange = 2
        type = PawnClass.VEHICLE
        texture = { Assets[Assets.scout] }
    }

    val artillery = PawnType("mech-artillery").apply {
        displayName = "artillery"
        cost = 500
        baseAtk = 50
        baseHp = 20
        maxAp = 1
        maxRange = 5
        minRange = 4
        targetRadius = 1
        type = PawnClass.MECH
        texture = { Assets[Assets.artillery] }
    }

    val hq = BuildingType("bldg-hq").apply {
        displayName = "headquarters"
        upkeep = -Constants.BASE_ECO
        power = -100
    }

    val factory = BuildingType("bldg-factory").apply {
        displayName = "factory"
        upkeep = 0
        power = 10
        /*buildable = listOf(

        )*/
    }

    val mine = BuildingType("bldg-mine").apply {
        displayName = "headquarters"
        upkeep = -30
        power = 10
    }

    val beach       = Biome("biome-beach", "beach", TerrainTextures.SAND, true, false)
    val desert      = Biome("biome-desert", "desert", TerrainTextures.SAND, true, false)
    val grass       = Biome("biome-grassland", "grassland", TerrainTextures.GRASS, true, true)
    val highlands   = Biome("biome-highlands", "highlands", TerrainTextures.BIGHILL, true, false, movementCost = 2)
    val water       = Biome("biome-water", "water", TerrainTextures.WATER, false, false)
    val mountains   = Biome("biome-mountains", "mountains", TerrainTextures.MOUNTAIN, false, false)

    fun register() {
        pawns.register(grunt)
        pawns.register(drill)
        pawns.register(beam)
        pawns.register(tankdes)
        pawns.register(defender)
        pawns.register(scout)
        pawns.register(artillery)

        buildings.register(hq)
        buildings.register(factory)
        buildings.register(mine)

        biomes.register(beach)
        biomes.register(desert)
        biomes.register(grass)
        biomes.register(highlands)
        biomes.register(water)
        biomes.register(mountains)
    }

}