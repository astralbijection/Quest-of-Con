package io.github.plenglin.questofcon.game

import io.github.plenglin.questofcon.game.grid.WorldCoords

interface BuildableType<out Output> {

    fun buildAt(coords: WorldCoords, team: Team): Output

    fun canBuildAt(coords: WorldCoords): Boolean = true

}