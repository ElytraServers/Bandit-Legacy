package cn.elytra.mod.bandit.mining

import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter

object BlockFilterRegistry {
    private val blockFilterMap = mutableMapOf<Int, VeinMiningBlockFilter>()
    private val blockFilterMapByName = mutableMapOf<String, VeinMiningBlockFilter>()

    init {
        register(0, VeinMiningBlockFilter.MATCH_BLOCK)
        register(1, VeinMiningBlockFilter.MATCH_BLOCK_AND_META)
        register(2, VeinMiningBlockFilter.ALL)
    }

    @Suppress("unused")
    fun register(
        id: Int,
        filter: VeinMiningBlockFilter,
    ) {
        if (id in blockFilterMap) {
            error("Duplicate VeinMiningBlockFilter: $id")
        }
        blockFilterMap[id] = filter
        blockFilterMapByName[filter.name] = filter
    }

    fun get(id: Int): VeinMiningBlockFilter? = blockFilterMap[id]

    fun get(name: String): VeinMiningBlockFilter? = blockFilterMapByName[name]

    fun getOrDefault(id: Int): VeinMiningBlockFilter = get(id) ?: blockFilterMap[0]!!

    fun all(): Map<Int, VeinMiningBlockFilter> = blockFilterMap.toMap()

    val VeinMiningBlockFilter.id: Int
        get() =
            blockFilterMap.entries.firstOrNull { (_, filter) -> filter == this }?.key
                ?: error("The filter wasn't registered.")
}
