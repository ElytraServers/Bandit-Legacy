package cn.elytra.mod.bandit.mining2.event

import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import cpw.mods.fml.common.eventhandler.Event

class VeinMiningMatchEvent(
    val session: VeinMiningSession,
    val pos: BlockPos,
    var isBlockMatching: Boolean,
) : Event()
