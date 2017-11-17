package io.github.plenglin.questofcon.game.building

import com.badlogic.gdx.graphics.Texture
import io.github.plenglin.questofcon.Assets
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
enum class Improvement(
        val aquatic: Boolean = false,
        val terrestrial: Boolean = true,
        val buildable: Boolean = true,
        override var cost: Int = 0,
        val texture: (WorldCoords) -> Texture = { Assets[Assets.missing] },
        val canBuildPredicate: (WorldCoords, Team) -> Boolean = { _, _ -> true }) : BuildableType<WorldCoords>, Serializable {

    FOREST(buildable = false), JUNGLE(buildable = false),

    ROAD(false, true, cost = 35, texture = { Assets[Assets.road] }, canBuildPredicate = { c, _ ->
        c.tile!!.buildableByTerrestrial()
    }),
    CANAL(false, true, cost = 50, texture = { Assets[Assets.canal] }, canBuildPredicate = { c, _ ->
        c.tile!!.buildableByTerrestrial()
    }),
    BRIDGE(true, false, cost = 50, texture = { Assets[Assets.bridge] }, canBuildPredicate = { c, _ ->
        c.tile!!.buildableByAquatic()
    }),
    MINEFIELD(true, true);

    override val displayName: String
        get() = name.toLowerCase()

    override fun buildAt(coords: WorldCoords, team: Team): WorldCoords {
        coords.tile!!.let { it.improvement = this }
        return coords
    }

    override fun canBuildAt(coords: WorldCoords, team: Team): Boolean {
        return coords.tile!!.getTeam() == team && canBuildPredicate(coords, team)
    }

}