package cn.elytra.mod.bandit.common.player_data

/**
 * 连锁挖掘通知类型
 */
enum class VeinMiningNoticeType(val id: Int) {
    /**
     * 任务开始
     */
    TASK_STARTING(1),

    /**
     * 提示可以停止任务
     */
    TASK_HALT_HINT(2),

    /**
     * 按键释放导致停止
     */
    TASK_STOP_KEY_RELEASE(3),

    /**
     * 任务完成（需要统计数据）
     */
    TASK_DONE(4);

    companion object {
        fun fromId(id: Int): VeinMiningNoticeType? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

