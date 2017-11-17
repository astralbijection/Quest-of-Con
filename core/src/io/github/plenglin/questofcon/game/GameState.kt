package io.github.plenglin.questofcon.game

import com.badlogic.gdx.graphics.Color
import io.github.plenglin.util.ListenerManager
import io.github.plenglin.questofcon.game.building.Building
import io.github.plenglin.questofcon.game.building.BuildingType
import io.github.plenglin.questofcon.game.grid.Tile
import io.github.plenglin.questofcon.game.grid.World
import io.github.plenglin.questofcon.game.grid.WorldCoords
import io.github.plenglin.questofcon.game.pawn.Pawn

/**
 *
 */
open class GameState(val teams: List<Team>) {

    val world = World(32, 32)
    private var teamIndex = 0

    val turnChange = ListenerManager<Team>()
    val pawnChange = ListenerManager<Pawn>()
    val buildingChange = ListenerManager<Building>()
    val worldChange = ListenerManager<WorldCoords>()
    val onSomethingBuilt = ListenerManager<Any>()

    init {
        teams.forEach {
            it.world = world
        }

        onSomethingBuilt.addListener {
            when (it) {
                is Pawn -> pawnChange.fire(it)
                is Building -> buildingChange.fire(it)
                is WorldCoords -> worldChange.fire(it)
            }
        }
        getCurrentTeam().startTurn()
    }

    fun getCurrentTeam(): Team {
        return teams[teamIndex]
    }

    fun nextTurn() {
        getCurrentTeam().endTurn()
        teamIndex = (teamIndex + 1) % teams.size
        getCurrentTeam().startTurn()
        getAllPawns().forEach { pawnChange.fire(it) }
        getAllBuildings().forEach { buildingChange.fire(it) }
        turnChange.fire(getCurrentTeam())
    }

    fun getAllPawns(): Sequence<Pawn> {
        return world.map { it.tile!!.pawn }.filterNotNull()
    }

    fun getAllBuildings(): Sequence<Building> {
        return world.map { it.tile!!.building }.filterNotNull()
    }

}

class DummyGameState : GameState(listOf(Team("heu", Color.CLEAR))) {
    init {

    }
}