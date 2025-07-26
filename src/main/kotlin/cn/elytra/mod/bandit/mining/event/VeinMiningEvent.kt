package cn.elytra.mod.bandit.mining.event

import cn.elytra.mod.bandit.common.mining.VeinMiningContext
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import cpw.mods.fml.common.eventhandler.Event
import net.minecraft.world.World

abstract class VeinMiningEvent(
    val context: VeinMiningContext,
    val pos: BlockPos,
) : Event() {

    val world: World get() = context.world

    /**
     * This event will be fired when the a block is check if it's a part of the vein.
     */
    class BlockMatching(
        context: VeinMiningContext,
        pos: BlockPos,
        var isBlockMatching: Boolean,
    ) : VeinMiningEvent(context, pos)

}
