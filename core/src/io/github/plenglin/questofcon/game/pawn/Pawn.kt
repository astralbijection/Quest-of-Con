package io.github.plenglin.questofcon.game.pawn

import io.github.plenglin.questofcon.Constants
import io.github.plenglin.questofcon.game.GameState
import io.github.plenglin.questofcon.game.Team
import io.github.plenglin.questofcon.game.grid.WorldCoords
import io.github.plenglin.questofcon.net.DataPawn
import io.github.plenglin.questofcon.ui.PawnActionManager
import io.github.plenglin.questofcon.ui.UI
import io.github.plenglin.questofcon.ui.elements.BuildableSpawningDialog
import io.github.plenglin.questofcon.ui.elements.ConfirmationDialog
import io.github.plenglin.questofcon.ui.elements.RadialMenuItem


private var nextPawnId = 0L

class Pawn(val type: PawnType, var team: Team, _pos: WorldCoords, var level: Int = 1) {

    val displayName: String = type.displayName
    val maxAp = type.maxAp
    val maxHealth get() = type.maxHp(level)
    val maxAttacks = type.maxAtks

    val isHealer = type.baseAtk < 0

    var gameState: GameState? = null

    var id = nextPawnId++

    var attacksRemaining = 0
    var ap: Int = 0
    var health: Int = type.maxHp(level)
        set(value) {
            field = value
            if (health <= 0) {
                pos.tile!!.pawn = null
            }
            gameState?.pawnChange?.fire(this)
        }

    var pos: WorldCoords = _pos
        set(value) {
            field.tile!!.pawn = null
            field = value
            value.tile!!.pawn = this
        }

    fun applyToPosition(gameState: GameState? = null): Pawn {
        pos.tile!!.pawn = this
        this.gameState = gameState
        return this
    }

    fun getMovableSquares(): Map<WorldCoords, Int> {
        // Breadth-First
        val dist = mutableMapOf<WorldCoords, Int>(pos to 0)  // coord, cost
        val unvisited = mutableListOf(pos)

        while (unvisited.isNotEmpty()) {
            val coord = unvisited.removeAt(0)  // Pop this new coordinate
            val tile = coord.tile!!

            val coordDist = dist[coord]!!

            if (tile.passableBy(this) && ap - coordDist > 0) {  // Can we even get past this tile? Do we have the AP to leave it?
                coord.surrounding().forEach { neighbor ->  // If we can, then calculate distances to neighbors
                    val neighborDist = dist[neighbor]
                    val neighborElevationCost = if (neighbor.tile!!.biome.aquatic) 0 else maxOf(neighbor.tile.elevation - tile.elevation, 0)
                    val neighborBiomeCost = neighbor.tile.biome.movementCost
                    val newCost = coordDist + neighborBiomeCost + neighborElevationCost
                    if (neighbor.tile.passableBy(this)) {
                        if (neighborDist == null) {  // If we haven't added the neighbor, add it now
                            unvisited.add(neighbor)
                            dist[neighbor] = newCost
                        } else if (neighborDist > newCost) {  // Is going through coord to neighbor faster than before?
                            dist[neighbor] = newCost  // Put it in
                        }
                    }
                }
            }

        }

        return dist
    }

    fun getAttackableSquares(): Set<WorldCoords> {
        return pos.floodfill(type.maxRange) - pos.floodfill(type.minRange)
    }

    fun getTargetingRadius(coords: WorldCoords): Set<WorldCoords> {
        return coords.floodfill(type.targetRadius)
    }

    fun damageTo(coords: WorldCoords): Int {
        val difference = maxOf(coords.tile!!.elevation - pos.tile!!.elevation, 0)
        val coeff = 1 - Constants.ELEVATION_DEF_BONUS_PER_DELTA * difference
        //println("$difference, $coeff")
        return maxOf(Math.round(type.baseAtk * coeff).toInt(), 5)
    }

    fun attemptMoveTo(coords: WorldCoords, movementData: Map<WorldCoords, Int>): Boolean {
        val cost = movementData[coords]
        if (cost != null) {
            return attemptMoveTo(coords, cost)
        } else {
            return false
        }
    }

    fun attemptMoveTo(coords: WorldCoords, apCost: Int): Boolean {
        if (ap > 0 && coords.tile!!.pawn == null) {
            ap -= apCost
            pos = coords
            gameState?.pawnChange?.fire(this)
            return true
        }
        return false
    }

    fun getProperties(): Map<String, Any> {
        return mapOf("type" to type.displayName, "team" to team.name, "health" to "$health/$maxHealth", "actions" to "$ap/$maxAp", "attacks" to "$attacksRemaining/$maxAttacks")
    }

    fun attemptAttack(coords: WorldCoords): Boolean {
        ap -= 1
        val dist = coords.manhattanDist(pos)
        var success = false
        if (dist in type.minRange..type.maxRange) {
            if (type.targetRadius == 0) {
                if (coords.tile!!.getTeam() == this.team && isHealer) {
                    success = coords.tile.doDamage(type.baseAtk)
                } else if (coords.tile.getTeam() != null) {
                    success = coords.tile.doDamage(damageTo(coords))
                }
            } else {
                (coords.floodfill(type.maxRange) - coords.floodfill(type.minRange)).filter { it.tile!!.getTeam() != team }.forEach {
                    it.tile!!.doDamage(damageTo(it))
                }
                attacksRemaining--
                success = true
            }
        }

        if (success) {
            attacksRemaining--
        }
        return success
    }

    fun getRadialActions(): List<RadialMenuItem> {

        val actions = mutableListOf<RadialMenuItem>(RadialMenuItem("Disband $displayName", {
            ConfirmationDialog("Disband $displayName", UI.skin, {
                UI.targetPlayerInterface.disbandPawn(this.id)
            }).show(UI.stage)
        }))

        if (ap > 0) {
            actions.add(RadialMenuItem("Move $displayName", {
                PawnActionManager.beginMoving(this)
            }))
            if (attacksRemaining > 0) {

                if (isHealer) {
                    RadialMenuItem("Heal with $displayName", {
                        PawnActionManager.beginAttacking(this)
                    })
                } else {
                    actions.add(RadialMenuItem("Attack with $displayName", {
                        PawnActionManager.beginAttacking(this)
                    }))
                }

            }

        }

        if (type.canBuild.isNotEmpty()) {
            actions.add(RadialMenuItem("Build with $displayName", {
                BuildableSpawningDialog(type.canBuild.filter { it.canBuildAt(pos, team) }, UI.skin, pos).show(UI.stage)
            }))
        }
        return actions
    }

    fun serialized(): DataPawn = DataPawn(id, team.id, type.id, health, ap, attacksRemaining, pos.serialized())

    override fun toString(): String = "Pawn($id@${type.name})"

    val texture get() = type.texture()

}