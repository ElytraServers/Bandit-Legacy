package cn.elytra.mod.bandit.mining

import cn.elytra.mod.bandit.BanditConfig
import cn.elytra.mod.bandit.mining.executor.LargeScanExecutorGenerator
import cn.elytra.mod.bandit.mining.executor.ManhattanExecutorGenerator
import cn.elytra.mod.bandit.mining.executor.VeinMiningExecutorGenerator

object ExecutorGeneratorRegistry {
    private val executorGeneratorMap = mutableMapOf<Int, VeinMiningExecutorGenerator>()
    private val executorGeneratorMapByName = mutableMapOf<String, VeinMiningExecutorGenerator>()

    init {
        register(0, ManhattanExecutorGenerator("manhattan", BanditConfig.manhattanRadius))
        register(1, ManhattanExecutorGenerator("manhattan-plus", BanditConfig.manhattanRadius, true))
        register(2, LargeScanExecutorGenerator(BanditConfig.largeScanRadiusXZ, BanditConfig.largeScanRadiusY))
        register(3, ManhattanExecutorGenerator("manhattan-large", BanditConfig.manhattanLargeRadius, true))
    }

    @Suppress("unused")
    fun register(
        id: Int,
        executor: VeinMiningExecutorGenerator,
    ) {
        if (id in executorGeneratorMap) {
            error("Duplicate executor: $id")
        }
        executorGeneratorMap[id] = executor
        executorGeneratorMapByName[executor.name] = executor
    }

    fun get(id: Int): VeinMiningExecutorGenerator? = executorGeneratorMap[id]

    fun get(name: String): VeinMiningExecutorGenerator? = executorGeneratorMapByName[name]

    fun getOrDefault(id: Int): VeinMiningExecutorGenerator = get(id) ?: executorGeneratorMap[0]!!

    fun all(): Map<Int, VeinMiningExecutorGenerator> = executorGeneratorMap.toMap()

    val VeinMiningExecutorGenerator.id: Int
        get() =
            executorGeneratorMap.entries.firstOrNull { (_, executorGenerator) -> executorGenerator == this }?.key
                ?: error("The executor wasn't registered.")
}
