package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.BanditCoroutines
import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.max
import kotlin.math.min

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
                    for (y in max((y + radiusY), 256) downTo min((y - radiusY), 0)) {
                        if (isBlockMatchingVein(context, BlockPos(x, y, z))) {
                            emit(BlockPos(x, y, z))
                        }
                    }
                }
            }
        // FIX: the block can possibly not loaded, causing a CME when not loading it in the server thread.
        // this may impact on the performance of executing large-scan.
        }.flowOn(BanditCoroutines.ServerThreadDispatcher)

    override fun getUnlocalizedName(): String = "bandit.executor.large-scan"
}
