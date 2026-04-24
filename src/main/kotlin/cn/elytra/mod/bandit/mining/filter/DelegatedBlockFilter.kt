package cn.elytra.mod.bandit.mining.filter

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos

@Suppress("FunctionName")
fun DelegatedBlockFilter(
    name: String,
    delegate: (context: VeinMiningContext, pos: BlockPos) -> Boolean,
): VeinMiningBlockFilter =
    object : VeinMiningBlockFilter {
        override val name: String = name

        override fun isBlockMatching(
            context: VeinMiningContext,
            pos: BlockPos,
        ): Boolean = delegate(context, pos)

        override fun toString(): String = "DelegatedBlockFilter{name=$name, delegate=$delegate}"
    }
