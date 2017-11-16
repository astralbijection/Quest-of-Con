package io.github.plenglin.questofcon.game

import io.github.plenglin.questofcon.game.grid.WorldCoords

interface BuildableType<out Output> {

    var cost: Int
    val displayName: String

    fun buildAt(coords: WorldCoords, team: Team): Output

    fun canBuildAt(coords: WorldCoords): Boolean = true

}