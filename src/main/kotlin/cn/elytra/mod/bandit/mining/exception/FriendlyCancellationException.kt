package cn.elytra.mod.bandit.mining.exception

import kotlinx.coroutines.CancellationException

/**
 * The exception represents the intentional cancellation by the program for vein-mining related jobs.
 */
@Suppress("unused")
sealed class FriendlyCancellationException : CancellationException {
    constructor() : super()
    constructor(message: String?) : super(message)
}

/**
 * The exception represents the vein-mining task was cancelled by the player because they released the key while the task is executing.
 */
class KeyReleaseCancellation : FriendlyCancellationException("Cancelled because the vein-mining key was released")

/**
 * The exception represents the vein-mining task was cancelled by the player because they typed the command
 */
class CommandCancellation : FriendlyCancellationException("Cancelled because of the stop command")

/**
 * The exception represents the vein-mining task was cancelled by the player because the player left
 */
class PlayerLeftCancellation : FriendlyCancellationException("Cancelled because the player left")

/**
 * The exception represents the vein-mining task was cancelled because the player failed to destroy the blocks
 */
class ToolMaxDamage : FriendlyCancellationException("Cancelled because the player failed to destroy the blocks.")
