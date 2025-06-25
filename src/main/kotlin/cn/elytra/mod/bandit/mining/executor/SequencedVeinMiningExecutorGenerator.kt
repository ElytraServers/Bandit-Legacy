package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import cn.elytra.mod.bandit.mining.HarvestCollector
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.minecraft.entity.item.EntityXPOrb
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
        return context.filter.isBlockMatching(context, pos)
    }

    /**
     * Execute the actual block harvesting, the item dropping, and other things that not related to
     * iterating but to the certain "single" block. This function is called in Server Thread, so it is
     * safe to do anything!
     */
    protected open fun doBlockHarvestOn(context: VeinMiningContext, pos: BlockPos) {
        val world = context.world
        val (drops, xpValue) = HarvestCollector.withHarvestCollectorScope {
            context.player.theItemInWorldManager.tryHarvestBlock(pos.x, pos.y, pos.z)
        }

        // spawn drops
        drops.forEach {
            val p = Vec3.createVectorHelper(context.player.posX, context.player.posY, context.player.posZ)
            VeinMiningExecutorGenerator.spawnItemAsEntity(world, p, it)
        }
        // record dropped experiences
        if(xpValue > 0) {
            context.statXpValueCollected.addAndGet(xpValue)
        }

        context.statBlocksMined.getAndAdd(1)
        context.statItemsCollected.getAndAdd(drops.sumOf { it.stackSize })
    }

    protected open fun onBlockHarvestDone(context: VeinMiningContext) {
        // spawn xp orbs when finishing
        var xpValueRemaining = context.statXpValueCollected.get()
        while(xpValueRemaining > 0) {
            val xpSplit = EntityXPOrb.getXPSplit(xpValueRemaining)
            xpValueRemaining -= xpSplit
            context.world.spawnEntityInWorld(
                EntityXPOrb(context.world, context.player.posX, context.player.posY, context.player.posZ, xpSplit)
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
