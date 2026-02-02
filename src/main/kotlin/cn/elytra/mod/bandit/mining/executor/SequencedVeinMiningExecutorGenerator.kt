package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import cn.elytra.mod.bandit.common.mining.VeinMiningContext.DropPosition
import cn.elytra.mod.bandit.common.player_data.veinMiningData
import cn.elytra.mod.bandit.mining.HarvestCollector
import cn.elytra.mod.bandit.mining.event.VeinMiningEvent
import cn.elytra.mod.bandit.mining.exception.DestroyBlockFail
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.item.ItemStack
import net.minecraft.util.Vec3

abstract class SequencedVeinMiningExecutorGenerator : VeinMiningExecutorGenerator, BlockPosCacheableExecutorGenerator {

    /**
     * Create a sequence that provides all the valid block positions that may be vein-mined. The
     * restriction of vein mining size and other numeric things are limited in [generate]. Here you'll
     * need to check if certain block is valid for the vein by either calling [isBlockMatchingVein]
     * or handle it yourself.
     */
    abstract fun createSequence(context: VeinMiningContext): Flow<BlockPos>

    override suspend fun precalculateBlockPosList(context: VeinMiningContext): List<BlockPos> {
        return createSequence(context).toList()
    }

    /**
     * Return `true` to harvest the block in given [pos].
     * Override this function for executor-specific filter.
     */
    protected open fun isBlockMatchingVein(context: VeinMiningContext, pos: BlockPos): Boolean {
        val value = context.filter.isBlockMatching(context, pos)
        val event = VeinMiningEvent.BlockMatching(context, pos, value).also { BanditMod.bus.post(it) }
        return event.isBlockMatching
    }

    protected open fun equippedToolWithMaxDamage(context: VeinMiningContext): Boolean {
        val equippedItem: ItemStack? = context.getPlayer().currentEquippedItem
        if (equippedItem != null && equippedItem.isItemStackDamageable) {
            return (equippedItem.maxDamage - equippedItem.itemDamage < 1)
        }
        return false
    }

    /**
     * Execute the actual block harvesting, the item dropping, and other things that not related to
     * iterating but to the certain "single" block. This function is called in Server Thread, so it is
     * safe to do anything!
     */
    protected open fun doBlockHarvestOn(context: VeinMiningContext, pos: BlockPos) {
        if (equippedToolWithMaxDamage(context)) {
            context.getPlayer().veinMiningData.cancelJob(DestroyBlockFail())
            return
        }

        val (drops, xpValue) = HarvestCollector.withHarvestCollectorScope {
            context.getPlayer().theItemInWorldManager.tryHarvestBlock(pos.x, pos.y, pos.z)
        }

        when(context.harvestedDropTiming) {
            VeinMiningContext.DropTiming.IMMEDIATELY -> {
                dropItemAt(context, drops)
                dropXpAt(context, xpValue)
            }

            VeinMiningContext.DropTiming.ITEM_IMMEDIATELY_XP_EVENTUALLY -> {
                dropItemAt(context, drops)
            }

            VeinMiningContext.DropTiming.EVENTUALLY -> {
            }
        }

        context.statBlocksMined.getAndAdd(1)
        drops.forEach { drop ->
            context.statItemDropped.merge(drop, drop.stackSize) { existing, new -> existing + new }
        }
        context.statXpValueCollected.addAndGet(xpValue)
    }

    /**
     * Invoked at the end of the vein mining, which is used to handle the finalization like dropping harvested objects
     * if the context prefers. This function is called in Server Thread, so it is safe to do anything!
     */
    protected open fun onBlockHarvestDone(context: VeinMiningContext) {
        when(context.harvestedDropTiming) {
            VeinMiningContext.DropTiming.IMMEDIATELY -> {
            }

            VeinMiningContext.DropTiming.ITEM_IMMEDIATELY_XP_EVENTUALLY -> {
                dropXpAt(context, context.statXpValueCollected.get())
            }

            VeinMiningContext.DropTiming.EVENTUALLY -> {
                dropItemAt(
                    context,
                    context.statItemDropped.map { (itemstack, amount) ->
                        itemstack.copy().also { it.stackSize = amount }
                    })
                dropXpAt(context, context.statXpValueCollected.get())
            }
        }
    }

    /**
     * Return the drop position for the given context.
     *
     * @see dropItemAt
     * @see dropXpAt
     */
    protected fun getDropPosition(context: VeinMiningContext): Vec3 {
        return when(context.harvestedDropPosition) {
            DropPosition.DROP_AT_START -> Vec3.createVectorHelper(
                context.center.x.toDouble(),
                context.center.y.toDouble(),
                context.center.z.toDouble()
            )

            DropPosition.DROP_TO_PLAYER -> {
                val player = context.getPlayer()
                Vec3.createVectorHelper(player.posX, player.posY, player.posZ)
            }
        }
    }

    /**
     * Drop the harvested items to the place where the context gives.
     */
    protected open fun dropItemAt(context: VeinMiningContext, items: List<ItemStack>) {
        val pos = getDropPosition(context)
        items.forEach { item ->
            VeinMiningExecutorGenerator.spawnItemAsEntity(context.world, pos, item)
        }
    }

    /**
     * Drop the experience orbs with given value to the place where the context gives.
     */
    protected open fun dropXpAt(context: VeinMiningContext, xpValue: Int) {
        val pos = getDropPosition(context)
        var xpValueRemaining = xpValue
        while(xpValueRemaining > 0) {
            val xpSplit = EntityXPOrb.getXPSplit(xpValueRemaining)
            xpValueRemaining -= xpSplit
            context.world.spawnEntityInWorld(
                EntityXPOrb(context.world, pos.xCoord, pos.yCoord, pos.zCoord, xpSplit)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun generate(context: VeinMiningContext): suspend () -> Unit {
        return suspend {
            val blockPosFlow = context.precalculatedBlockPosList?.asFlow() ?: createSequence(context)
            blockPosFlow
                .take(context.veinMiningMaxCountLimit)
                .chunked(context.veinMiningCountPerOperation)
                .collect { blockPosChunk ->
                    // run in server thread
                    withContext(BanditCoroutines.ServerThreadDispatcher) {
                        // do the actual block harvesting
                        blockPosChunk.forEach { doBlockHarvestOn(context, it) }
                    }
                }
            // do the clean-up
            onBlockHarvestDone(context)
        }
    }
}
