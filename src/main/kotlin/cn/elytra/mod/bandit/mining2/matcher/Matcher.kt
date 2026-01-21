package cn.elytra.mod.bandit.mining2.matcher

import cn.elytra.mod.bandit.BanditMod
import cn.elytra.mod.bandit.mining2.event.VeinMiningMatchEvent
import cn.elytra.mod.bandit.mining2.session.VeinMiningSession
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.BlockSnapshot
import org.joml.component1
import org.joml.component2
import org.joml.component3

fun interface Matcher {
    suspend fun matches(
        session: VeinMiningSession,
        source: BlockSnapshot,
        block: Block,
        blockMetadata: Int,
        blockTileEntity: TileEntity?,
        blockPos: BlockPos,
    ): Boolean
}

internal suspend fun Matcher.matches(
    session: VeinMiningSession,
    blockPos: BlockPos,
): Boolean {
    val (x, y, z) = blockPos
    val world = session.world
    return matches(
        session,
        session.triggerSnapshot,
        world.getBlock(x, y, z),
        world.getBlockMetadata(x, y, z),
        world.getTileEntity(x, y, z),
        blockPos,
    )
}

internal suspend fun Matcher.matchesAndPostEvent(
    session: VeinMiningSession,
    blockPos: BlockPos,
): Boolean {
    val value = this.matches(session, blockPos)
    val event = VeinMiningMatchEvent(session, blockPos, value)
    BanditMod.bus.post(event)
    return event.isBlockMatching
}
