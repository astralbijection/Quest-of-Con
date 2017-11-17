package io.github.plenglin.questofcon.game.grid

import io.github.plenglin.questofcon.game.GameData
import io.github.plenglin.questofcon.game.Team
import io.github.plenglin.questofcon.game.building.Building
import io.github.plenglin.questofcon.game.building.BuildingType
import io.github.plenglin.questofcon.game.building.Improvement
import io.github.plenglin.questofcon.game.pawn.Pawn
import io.github.plenglin.questofcon.net.DataTile

/**
 *
 */
class Tile {

    var biome: Biome = GameData.grass
    var elevation: Int = 0
    var pawn: Pawn? = null
    var building: Building? = null
    var improvement: Improvement? = null

    fun getTeam(): Team? {
        return pawn?.team ?: building?.team
    }

    fun canBuildOn(team: Team): Boolean {
        return biome.buildable && building == null && pawn?.let { it.team == team } != false
    }

    fun doDamage(hp: Int): Boolean {
        val bldg = building
        var output = false

        if (bldg != null) {
            bldg.health -= hp
            output = true
        }

        if (pawn != null) {
            pawn!!.health -= hp
            output = true
        }
        return output
    }

    fun cost(): Int {
        if (improvement == Improvement.ROAD) {
            return 1
        }
        return biome.movementCost
    }

    private fun passableByAquatic(): Boolean {
        return biome.aquatic || improvement == Improvement.CANAL
    }

    private fun passableByTerrestrial(): Boolean {
        return !biome.aquatic || improvement == Improvement.BRIDGE
    }

    fun passableBy(pawn: Pawn): Boolean {
        val team = getTeam()
        if ((team != null && team != pawn.team) || !biome.passable) {
            return false
        }
        return (passableByAquatic() && pawn.type.aquatic) || (passableByTerrestrial() && pawn.type.terrestrial)
    }

    fun buildableByAquatic(): Boolean {
        return biome.aquatic
    }

    fun buildableByTerrestrial(): Boolean {
        return !biome.aquatic
    }

    fun serialized(): DataTile {
        return DataTile(biome.id, elevation)
    }

}