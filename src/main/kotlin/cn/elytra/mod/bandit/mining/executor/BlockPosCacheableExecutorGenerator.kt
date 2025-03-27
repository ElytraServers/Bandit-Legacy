package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos

/**
 * Implementing this interface indicates that the executor generator will harvest same blocks with same input of context.
 * The precalculated block pos list will be stored in the player data, and used for vein mining.
 */
interface BlockPosCacheableExecutorGenerator {

    suspend fun precalculateBlockPosList(context: VeinMiningContext): List<BlockPos>

}
