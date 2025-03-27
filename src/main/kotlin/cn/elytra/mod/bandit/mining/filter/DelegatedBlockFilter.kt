package cn.elytra.mod.bandit.mining.filter

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos

@Suppress("FunctionName")
fun DelegatedBlockFilter(
    unlocalizedName: String,
    delegate: (context: VeinMiningContext, pos: BlockPos) -> Boolean,
): VeinMiningBlockFilter {
    return object : VeinMiningBlockFilter {
        override fun isBlockMatching(
            context: VeinMiningContext,
            pos: BlockPos,
        ): Boolean {
            return delegate(context, pos)
        }

        override fun toString(): String {
            return "DelegatedBlockFilter{name=${unlocalizedName}, delegate=${delegate}}"
        }

        override fun getUnlocalizedName(): String {
            return unlocalizedName
        }
    }
}
