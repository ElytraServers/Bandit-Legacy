package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class LargeScanExecutorGenerator(
    private val radiusXZ: Int,
    private val radiusY: Int,
) : SequencedVeinMiningExecutorGenerator() {
    constructor(radius: Int) : this(radius, radius)

    override fun createSequence(context: VeinMiningContext): Flow<BlockPos> =
        flow {
            val x = context.center.x
            val y = context.center.y
            val z = context.center.z

            for (x in x - radiusXZ until x + radiusXZ) {
                for (z in z - radiusXZ until z + radiusXZ) {
                    // y iteration reversed from up to down
                    for (y in y + radiusY downTo y - radiusY) {
                        // FIX: the block can possibly not loaded, causing a CME when not loading it in the server thread.
                        // this may impact on the performance of executing large-scan.
                        withContext(BanditCoroutines.ServerThreadDispatcher) {
                            if (isBlockMatchingVein(context, BlockPos(x, y, z))) {
                                emit(BlockPos(x, y, z))
                            }
                        }
                    }
                }
            }
        }

    override fun getUnlocalizedName(): String = "bandit.executor.large-scan"
}
