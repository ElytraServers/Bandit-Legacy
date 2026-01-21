package cn.elytra.mod.bandit.mining2.selector

import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.matcher.matchesAndPostEvent
import cn.elytra.mod.bandit.mining2.selector.helper.BlockPosIteratorUtils
import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take

abstract class LargeScanSelector(
    val radiusXZ: Int,
    val radiusY: Int,
    val limitCount: Int,
) : Selector {
    override fun select(
        session: VeinMiningSession,
        matcher: Matcher,
    ): Flow<BlockPos> =
        BlockPosIteratorUtils
            .getLargeScan(session.triggerBlockPos, radiusXZ, radiusY)
            .asFlow()
            .filter { blockPos -> matcher.matchesAndPostEvent(session, blockPos) }
            .take(limitCount)
}
