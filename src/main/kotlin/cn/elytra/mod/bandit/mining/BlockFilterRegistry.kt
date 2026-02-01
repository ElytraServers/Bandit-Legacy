package cn.elytra.mod.bandit.mining

import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter

object BlockFilterRegistry {

    private val blockFilterMap = mutableMapOf<Int, VeinMiningBlockFilter>()

    init {
        blockFilterMap[0] = VeinMiningBlockFilter.Companion.MATCH_BLOCK
        blockFilterMap[1] = VeinMiningBlockFilter.Companion.MATCH_BLOCK_AND_META
        blockFilterMap[2] = VeinMiningBlockFilter.Companion.ALL
    }

    @Suppress("unused")
    fun register(id: Int, block: VeinMiningBlockFilter) {
        if(id in blockFilterMap) {
            error("Duplicate VeinMiningBlockFilter: $id")
        }
        blockFilterMap[id] = block
    }

    fun get(id: Int): VeinMiningBlockFilter? {
        return blockFilterMap[id]
    }

    fun getOrDefault(id: Int): VeinMiningBlockFilter {
        return get(id) ?: blockFilterMap[0]!!
    }

    fun all(): Map<Int, VeinMiningBlockFilter> = blockFilterMap.toMap()

    fun isRegistered(id: Int): Boolean {
        return id in blockFilterMap
    }

    fun getUnlocalizedName(id: Int): String? {
        return blockFilterMap[id]?.getUnlocalizedName()
    }
}
