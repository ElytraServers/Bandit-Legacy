package cn.elytra.mod.bandit.mining.executor

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LargeScanExecutorGenerator(private val radius: Int) : SequencedVeinMiningExecutorGenerator() {

    override fun createSequence(context: VeinMiningContext): Flow<BlockPos> {
        return flow {
            val x = context.center.x
            val y = context.center.y
            val z = context.center.z

            for(x in x - radius until x + radius) {
                for(z in z - radius until z + radius) {
                    // y iteration reversed from up to down
                    for(y in y + radius downTo y - radius) {
                        if(isBlockMatchingVein(context, BlockPos(x, y, z))) {
                            emit(BlockPos(x, y, z))
                        }
                    }
                }
            }
        }
    }

    override fun getUnlocalizedName(): String {
        return "bandit.executor.large-scan"
    }
}
