package cn.elytra.mod.bandit.mining2.selector

import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import kotlinx.coroutines.flow.Flow

fun interface Selector {
    fun select(
        session: VeinMiningSession,
        matcher: Matcher,
    ): Flow<BlockPos>
}
