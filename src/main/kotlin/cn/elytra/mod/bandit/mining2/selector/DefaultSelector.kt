package cn.elytra.mod.bandit.mining2.selector

import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.matcher.matchesAndPostEvent
import cn.elytra.mod.bandit.mining2.selector.helper.BlockPosIteratorUtils
import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import it.unimi.dsi.fastutil.longs.LongArraySet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import java.util.*

abstract class DefaultSelector(
    val limitCount: Int,
    val limitRange: Double?,
    val allowCornerTouched: Boolean,
) : Selector {
    private val limitRangeSquared = limitRange?.let { (it * it).toLong() }

    override fun select(
        session: VeinMiningSession,
        matcher: Matcher,
    ): Flow<BlockPos> =
        flow {
            val cpos = session.triggerBlockPos
            val q = LinkedList<BlockPos>().also { it.offer(cpos) }
            val visited = LongArraySet.of()

            while (q.isNotEmpty()) {
                val bp = q.poll() ?: break
                // skip visited pos
                if (!visited.add(bp.asLong())) continue
                if (matcher.matchesAndPostEvent(session, bp) && isInLimitRange(cpos, bp)) {
                    emit(bp)
                    q.addAll(BlockPosIteratorUtils.getConnectedBlockPositions(bp, allowCornerTouched))
                }
            }
        }.take(limitCount)

    protected fun isInLimitRange(
        center: BlockPos,
        other: BlockPos,
    ): Boolean =
        when (limitRangeSquared) {
            null -> true
            else -> center.distanceSquared(other) <= limitRangeSquared
        }
}
