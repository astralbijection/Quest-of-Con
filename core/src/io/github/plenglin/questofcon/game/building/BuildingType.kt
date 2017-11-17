package io.github.plenglin.questofcon.game.building

import com.badlogic.gdx.graphics.Texture
import io.github.plenglin.questofcon.Assets
import io.github.plenglin.questofcon.game.BuildableType
import io.github.plenglin.questofcon.game.Team
import io.github.plenglin.questofcon.game.grid.WorldCoords
import io.github.plenglin.questofcon.game.pawn.PawnType
import io.github.plenglin.util.Registerable

class BuildingType(_name: String) : Registerable, BuildableType<Building> {

    override fun buildAt(coords: WorldCoords, team: Team): Building {
        val building = Building(this, team, coords).applyToPosition()
        return building
    }

    override fun canBuildAt(coords: WorldCoords, team: Team): Boolean {
        val tile = coords.tile!!
        val thisTeam = tile.getTeam()
        if ((thisTeam != null && thisTeam != team) || !tile.biome.buildable) {
            return false
        }
        return (tile.buildableByAquatic() && aquatic) || (tile.buildableByTerrestrial() && terrestrial)    }

    override var id: Long = -1
    override val name: String = _name

    override var displayName = ""
    override var cost = 0

    var maxHp = 1

    var aquatic = false
    var terrestrial = true

    var upkeep = 0

    var powerConsumption = 0

    var buildable: (Team) -> List<PawnType> = { emptyList() }

    var texture: (WorldCoords) -> Texture = { Assets.missingno() }

}