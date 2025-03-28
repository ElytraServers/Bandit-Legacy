package cn.elytra.mod.bandit.mining

import cn.elytra.mod.bandit.mining.executor.LargeScanExecutorGenerator
import cn.elytra.mod.bandit.mining.executor.ManhattanExecutorGenerator
import cn.elytra.mod.bandit.mining.executor.VeinMiningExecutorGenerator

object ExecutorGeneratorRegistry {

    private val executorGeneratorMap = mutableMapOf<Int, VeinMiningExecutorGenerator>()

    init {
        executorGeneratorMap[0] = ManhattanExecutorGenerator("bandit.executor.manhattan", 8)
        executorGeneratorMap[1] = ManhattanExecutorGenerator("bandit.executor.manhattan-plus", 8, true)
        executorGeneratorMap[2] = LargeScanExecutorGenerator(32)
        executorGeneratorMap[3] = ManhattanExecutorGenerator("bandit.executor.manhattan-large", 16, true)
    }

    @Suppress("unused")
    fun register(id: Int, executor: VeinMiningExecutorGenerator) {
        if(id in executorGeneratorMap) {
            error("Duplicate executor: $id")
        }
        executorGeneratorMap[id] = executor
    }

    fun get(id: Int): VeinMiningExecutorGenerator? {
        return executorGeneratorMap[id]
    }

    fun getOrDefault(id: Int): VeinMiningExecutorGenerator {
        return get(id) ?: executorGeneratorMap[0]!!
    }

    fun all(): Map<Int, VeinMiningExecutorGenerator> = executorGeneratorMap.toMap()
}
