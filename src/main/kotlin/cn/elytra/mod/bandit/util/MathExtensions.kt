package cn.elytra.mod.bandit.util

import kotlin.math.abs

/**
 * @return the remainder but always positive
 */
internal infix fun Int.remEuclid(other: Int): Int {
    val r = this % other
    return if (r < 0) r + abs(other) else r
}
