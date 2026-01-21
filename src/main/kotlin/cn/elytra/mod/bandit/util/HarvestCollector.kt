package cn.elytra.mod.bandit.util

import net.minecraft.item.ItemStack
import org.jetbrains.annotations.ApiStatus

/**
 * The place to hold the dropping items and xps when executing Vein Mining.
 */
object HarvestCollector {
    private val collected = mutableListOf<ItemStack>()
    private var xpValue = 0

    internal var shouldCollect = false

    @ApiStatus.Internal
    fun addItemStack(item: ItemStack) {
        collected.add(item)
    }

    @ApiStatus.Internal
    fun addXpValue(value: Int) {
        xpValue += value
    }

    fun clear() {
        collected.clear()
        xpValue = 0
    }

    fun getAndClear(): Pair<List<ItemStack>, Int> {
        val collectedItems = collected.toList()
        val collectedXpValue = xpValue
        collected.clear()
        xpValue = 0
        return collectedItems to collectedXpValue
    }

    /**
     * Run the given [func], and collect all the drops and exps during the func.
     *
     * The dropping items and exps are collected by [cn.elytra.mod.bandit.common.listener.VeinMiningEventListener.onEntitySpawn].
     *
     * @return the drops *to* the xp value
     */
    fun withHarvestCollectorScope(func: () -> Unit): Pair<List<ItemStack>, Int> {
        // reset the harvest collector
        clear()
        shouldCollect = true
        // run the harvest in my scope,
        // so that the block breaking events and entity spawning events are not handled by bandit,
        // as they're passing to other event listeners normally.
        // note that calling this function out of Server Thread will cause exception thrown.
        func()
        // collect the drops and exps to return
        shouldCollect = false
        return getAndClear()
    }
}
