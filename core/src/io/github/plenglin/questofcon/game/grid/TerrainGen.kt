package io.github.plenglin.questofcon.game.grid

import com.badlogic.gdx.math.Vector2
import io.github.plenglin.questofcon.linMap
import ktx.math.times
import java.util.*


class DiamondSquareHeightGenerator(scale: Int, val initialOffsets: Double = 0.5, val iterativeRescale: Double = 0.5, seed: Long = 0) {

    val iterations = scale + 1
    val side = (2 shl scale)
    val length = side + 1
    val grid = Array<Array<Double>>(length, { Array(length, {0.0}) })
    val random = Random(seed)

    fun generate(): DiamondSquareHeightGenerator {
        // Initialize corners
        grid[0][0] = rand()
        grid[side][0] = rand()
        grid[0][side] = rand()
        grid[side][side] = rand()

        var offset = initialOffsets

        for (i in 1..iterations) {
            val hlen = side shr i

            val affected = getSquaresAffected(i)
            val diamonds = getDiamonds(i)
            val squares = affected.subtract(diamonds)

            diamonds.forEach {
                diamond(it.first, it.second, hlen, offset * rand())
            }

            squares.forEach {
                square(it.first, it.second, hlen, offset * rand())
            }

            offset *= iterativeRescale
        }
        return this
    }

    fun rand(): Double {
        return 2 * random.nextDouble() - 1
    }

    fun getSquaresAffected(iteration: Int, total: Boolean = false): Set<Pair<Int, Int>> {
        val jump = side shr iteration
        val accumulated = (0..side step jump).map { i ->
            (0..side step jump).map { j ->
                Pair(i, j)
            }
        }.flatten().toSet()
        return if (total || iteration == 0) {
            accumulated
        } else {
            accumulated.subtract(getSquaresAffected(iteration - 1, true))
        }
    }

    fun getDiamonds(iteration: Int, total: Boolean = false): Set<Pair<Int, Int>> {
        val start = side shr (iteration)
        val jump = start * 2
        val accumulated = (start..side step jump).map { i ->
            (start..side step jump).map { j ->
                Pair(i, j)
            }
        }.flatten().toSet()
        return if (total || iteration == 1) {
            accumulated
        } else {
            accumulated.subtract(getDiamonds(iteration - 1, true))
        }
    }

    fun diamond(i: Int, j: Int, step_size: Int, offset: Double) {
        val avg = listOf(
                grid[i - step_size][j - step_size],
                grid[i + step_size][j - step_size],
                grid[i - step_size][j + step_size],
                grid[i + step_size][j + step_size]
        ).average()
        grid[i][j] = avg + offset
    }

    fun square(i: Int, j: Int, step_size: Int, offset: Double) {
        val avg = listOf(
                grid.getOrNull(i)?.getOrNull(j - step_size),
                grid.getOrNull(i + step_size)?.getOrNull(j),
                grid.getOrNull(i)?.getOrNull(j + step_size),
                grid.getOrNull(i - step_size)?.getOrNull(j)
        ).filterNotNull().average()
        grid[i][j] = avg + offset
    }

}

class HeightMap(val grid: Array<Array<Double>>) {

    constructor(grid: Collection<Collection<Double>>): this(grid.map { it.toTypedArray() }.toTypedArray())

    constructor(width: Int, fill: Double = 0.0): this(Array(width, { Array(width, { fill }) }))

    val width = grid.size

    val normalized get(): HeightMap {
        val max = grid.flatten().max()!!
        val min = grid.flatten().min()!!
        //println("max: $max, min: $min")
        return HeightMap(grid.map { col ->
            col.map { linMap(it, min, max, 0.0, 1.0) }.toTypedArray()
        }.toTypedArray())
    }

    operator fun plus(other: HeightMap): HeightMap {
        val newWidth = maxOf(this.width, other.width)
        return HeightMap((0 until newWidth).map { i ->
            (0 until newWidth).map { j ->
                val x = i.toDouble() / newWidth
                val y = j.toDouble() / newWidth
                this[x, y] + other[x, y]
            }
        })
    }

    operator fun plus(other: Double): HeightMap {
        return HeightMap((0 until width).map { i ->
            (0 until width).map { j ->
                this[i][j] + other
            }
        })
    }

    operator fun times(other: Double): HeightMap {
        return HeightMap((0 until width).map { i ->
            (0 until width).map { j ->
                this[i][j] * other
            }
        })
    }

    operator fun times(other: HeightMap): HeightMap {
        val newWidth = maxOf(this.width, other.width)
        return HeightMap((0 until newWidth).map { i ->
            (0 until newWidth).map { j ->
                val x = i.toDouble() / newWidth
                val y = j.toDouble() / newWidth
                this[x, y] * other[x, y]
            }
        })
    }

    operator fun get(i: Int): Array<Double> {
        return grid[i]
    }

    /**
     * Interpolate the data.
     * @param x A number in [0, 1]
     * @param y A number in [0, 1]
     */
    operator fun get(x: Double, y: Double): Double {
        // Numbers and the cells
        val i = x * (width - 1)
        val j = y * (width - 1)
        val i1 = i.toInt()
        val j1 = j.toInt()
        val i2 = i1 + 1
        val j2 = j1 + 1

        // Get the sides
        val s1 = linMap(i, i1.toDouble(), i2.toDouble(), grid[i1][j1], grid[i2][j1])
        val s2 = linMap(i, i1.toDouble(), i2.toDouble(), grid[i1][j2], grid[i2][j1])

        // Interpolate the values at the sides
        return linMap(j, j1.toDouble(), j2.toDouble(), s1, s2)
    }

    /**
     * The approximate partial derivative w/ respect to i
     */
    fun partialI(): HeightMap {
        return HeightMap((0 until width).map { i ->
            (0 until width).map { j ->
                (grid.getOrElse(i + 1, { grid[i] })[j] - grid.getOrElse(i - 1, { grid[i] })[j]) / width / 2
            }
        })
    }

    /**
     * The approximate partial derivative w/ respect to j
     */
    fun partialJ(): HeightMap {
        return HeightMap((0 until width).map { i ->
            (0 until width).map { j ->
                (grid[i].getOrElse(j + 1, { grid[i][j] }) - grid[i].getOrElse(j - 1, { grid[i][j] })) / width / 2
            }
        })
    }

    fun slopeField(): Array<Array<Vector2>> {
        val di = this.partialJ().partialI()
        val dj = this.partialI().partialJ()
        return (0 until width).map { i ->
            (0 until width).map { j ->
                Vector2(di[i][j].toFloat(), dj[i][j].toFloat())
            }.toTypedArray()
        }.toTypedArray()
    }

    override fun toString(): String {
        return grid.map { col ->
            col.map {
                "%.2f\t".format(it)
            }.reduceRight({ a, b -> "$a$b"})
        }.reduceRight({ a, b -> "$a\n$b"})
    }

}

/**
 * Takes a terrain height map and turns it into a [World].
 */
class MapToHeight(val world: World, val grid: HeightMap) {

    fun doHeightMap() {
        world.forEach {
            // Interpolate the values at the sides
            val h = grid[it.i.toDouble() / world.width, it.j.toDouble() / world.height]
            //println("${it.i.toDouble() / world.width}")
            val tile = it.tile!!
            tile.terrain = when {
                h > 0.85 -> Terrains.mountains
                h > 0.65 -> Terrains.hills
                h > 0.25 -> Terrains.grass
                else -> Terrains.lowlands
            }
        }
    }
}

/**
 * Populate the world with vegetation.
 */
class VegetationGenerator(val world: World, val height: HeightMap,
                          val drainIterations: Int = 16, val simulationIterations: Int = 3,
                          val drainRetention: Double = 0.9, val waterPropagation: Double = 500.0,
                          seed: Long = 0) {

    val random = Random(seed)

    fun generate() {
        val slopeField = (height).slopeField()

        println("slopefield")
        slopeField.forEachIndexed { i, col ->
            col.forEachIndexed { j, v ->
                val d = v * 1000f
                print("<${d.x.toInt() * 1000}\t${d.y.toInt() * 1000}>\t")
            }
            println()
        }

        var totalWater = HeightMap(17)
        for (iter in (1..simulationIterations)) {

            println("simulation iteration #$iter")

            val invertedHeight = (height * -1.0 + 1.0)
            val rainfall = HeightMap(DiamondSquareHeightGenerator(3, seed = random.nextLong()).generate().grid).normalized  // We get this much base rainfall
            var water = (rainfall * invertedHeight).normalized  // Higher regions get less rainfall

            println("rainfall this month: \n$rainfall")
            println("invHeight:\n $invertedHeight")
            println("water:\n $water")
            for (jter in (1..drainIterations)) {

                println("drain iteration #$jter")
                val oldWater = water
                water *= drainRetention
                (0 until water.width).map { i ->
                    (0 until water.width).map { j ->
                        val x = i / water.width.toDouble()
                        val y = j / water.width.toDouble()

                        // Where to push water?
                        val slope = slopeField[i][j].cpy() * waterPropagation.toFloat()
                        val dx = -slope.x.toDouble()
                        val dy = -slope.y.toDouble()
                        val amt = slope.len2().toDouble()

                        water[i][j] -= amt

                        // Push water to its rightful destination
                        try {
                            if (dx > 0) {
                                water[i + 1][j] += dx
                            } else {
                                water[i - 1][j] += dx
                            }
                        } catch (e: ArrayIndexOutOfBoundsException) {}

                        try {
                            if (dy > 0) {
                                water[i][j + 1] += dy
                            } else {
                                water[i][j - 1] += dy
                            }
                        } catch (e: ArrayIndexOutOfBoundsException) {}
                    }
                }
                println(water)
            }

            totalWater += water.normalized
            println(totalWater.normalized)
            println("=====")
        }
    }

}

fun main(args: Array<String>) {
    val height = HeightMap(DiamondSquareHeightGenerator(3, seed = 0).generate().grid).normalized
    val world = World(32, 32)
    println("height:\n$height")
    MapToHeight(world, height).doHeightMap()
    VegetationGenerator(world, height).generate()
    println("height:\n$height")

}
