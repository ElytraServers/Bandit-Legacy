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
