package cn.elytra.mod.bandit.common.player_data

import kotlinx.coroutines.Job

data class TypedJob(
    val job: Job,
    val type: JobType,
) {

    enum class JobType {
        VeinMining,
        VeinMiningPrecalculating,
    }

}
