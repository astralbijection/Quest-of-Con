package io.github.plenglin.questofcon.game.building

import io.github.plenglin.questofcon.game.BuildableType
import io.github.plenglin.questofcon.game.Team
import io.github.plenglin.questofcon.game.grid.Tile
import io.github.plenglin.questofcon.game.grid.WorldCoords
import java.io.Serializable

/**
 * @param aquatic Can it be built over water?
 * @param terrestrial Can it be built over land?
 * @param buildable Can you even build it?
 */
enum class Improvement(val aquatic: Boolean = false, val terrestrial: Boolean = true, val buildable: Boolean = true) : BuildableType<Tile>, Serializable {

    FOREST(buildable = false), JUNGLE(buildable = false),

    ROAD(false, true), CANAL(false, true), BRIDGE(true, false), MINEFIELD(true, true);

    override fun buildAt(coords: WorldCoords, team: Team): Tile {
        return coords.tile!!
    }
}