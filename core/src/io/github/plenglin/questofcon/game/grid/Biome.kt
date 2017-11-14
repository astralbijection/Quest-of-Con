package io.github.plenglin.questofcon.game.grid

import com.badlogic.gdx.graphics.Texture
import io.github.plenglin.questofcon.Assets
import io.github.plenglin.util.Registerable


data class Biome(
        override val name: String,
        val displayName: String,
        val passable: Boolean,
        val buildable: Boolean,
        val texture: () -> Texture = Assets.missingno,
        val movementCost: Int = 1,
        val aquatic: Boolean = false,
        val improvable: Boolean = true) : Registerable {
    override var id: Long = -1L

    override fun toString(): String {
        return "Biome($name)"
    }

}

