package cn.elytra.mod.bandit.common.player_data

/**
 * Vein mining notification types.
 *
 * Enumerates different states and events in the vein mining process
 * that can be displayed as HUD notifications to the player.
 */
enum class VeinMiningNoticeType(val id: Int) {
    /**
     * Mining task has started.
     * Triggered when player initiates a vein mining sequence.
     */
    TASK_STARTING(1),

    /**
     * Hint that the task stopped.
     * Indicates the player can voluntarily end the mining chain.
     */
    TASK_HALT_HINT(2),

    /**
     * Task stopped due to key release.
     * Mining chain was terminated because the player released the activation key.
     */
    TASK_STOP_KEY_RELEASE(3),

    /**
     * Task stopped by command.
     * Mining chain was terminated via a game command or external trigger.
     */
    TASK_STOP_FOR_COMMAND(4),

    /**
     * Task stopped because the player failed to destroy the blocks.
     * Mining chain was terminated via tool is broken or something.
     */
    TASK_STOP_BECAUSE_TOOL_MAX_DAMAGE(5),

    /**
     * Task completed successfully.
     * Mining chain finished naturally and requires statistical processing.
     */
    TASK_DONE(5),

    /**
     * Task not start cause there is another vein mining job.
     */
    TASK_BLOCKED(6);

    companion object {
        /**
         * Retrieves a [VeinMiningNoticeType] by its numeric identifier.
         *
         * @param id The numeric identifier of the notice type.
         * @return The corresponding [VeinMiningNoticeType], or `null` if no match is found.
         */
        fun fromId(id: Int): VeinMiningNoticeType? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

